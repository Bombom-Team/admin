# 자동 리뷰어 배정 시스템 — 백엔드 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** GitHub Webhook으로 PR이 열릴 때 round-robin 방식으로 리뷰어를 자동 배정하고, Supabase에 이력을 저장하는 백엔드 시스템 구축

**Architecture:** GitHub Webhook → Spring Boot (시그니처 검증 → Supabase에서 리뷰어 조회 → 순환 선택 → GitHub API로 배정 → Supabase에 이력 저장). Supabase PostgREST API를 WebClient로 직접 호출. GitHub 팀 멤버는 매일 새벽 2시 스케줄러로 동기화.

**Tech Stack:** Java 21, Spring Boot 3.5.3, WebClient(WebFlux), Supabase PostgREST API, GitHub REST API v3, JUnit 5, Mockito

**Spec:** `docs/superpowers/specs/2026-06-17-auto-reviewer-assignment-design.md`

---

## 파일 맵

| 파일 | 역할 |
|---|---|
| `src/main/java/me/bombom/api/v1/common/config/SecurityConfig.java` | 수정: webhook 경로를 인증 제외 목록에 추가 |
| `src/main/java/me/bombom/api/v1/github/config/GitHubReviewerProperties.java` | 신규: GitHub 리뷰어 관련 설정 (`@ConfigurationProperties`) |
| `src/main/java/me/bombom/api/v1/github/config/SupabaseProperties.java` | 신규: Supabase 연결 설정 |
| `src/main/java/me/bombom/api/v1/github/client/SupabaseClient.java` | 신규: Supabase PostgREST API 래퍼 |
| `src/main/java/me/bombom/api/v1/github/client/GitHubApiClient.java` | 신규: GitHub API (PR 리뷰어 배정, 팀 멤버 조회) |
| `src/main/java/me/bombom/api/v1/github/dto/ReviewerDto.java` | 신규: Supabase reviewer 행 매핑 DTO |
| `src/main/java/me/bombom/api/v1/github/dto/PullRequestOpenedEvent.java` | 신규: Webhook payload DTO |
| `src/main/java/me/bombom/api/v1/github/security/GitHubWebhookVerifier.java` | 신규: HMAC-SHA256 시그니처 검증 |
| `src/main/java/me/bombom/api/v1/github/service/ReviewerAssignmentService.java` | 신규: round-robin 선택 로직 |
| `src/main/java/me/bombom/api/v1/github/service/ReviewerSyncService.java` | 신규: GitHub 팀 멤버 → Supabase upsert |
| `src/main/java/me/bombom/api/v1/github/scheduler/ReviewerSyncScheduler.java` | 신규: 매일 새벽 2시 동기화 스케줄러 |
| `src/main/java/me/bombom/api/v1/github/controller/GitHubWebhookController.java` | 신규: Webhook 수신 엔드포인트 |
| `src/test/java/me/bombom/api/v1/github/security/GitHubWebhookVerifierTest.java` | 신규: 시그니처 검증 단위 테스트 |
| `src/test/java/me/bombom/api/v1/github/service/ReviewerAssignmentServiceTest.java` | 신규: round-robin 로직 단위 테스트 |
| `src/test/java/me/bombom/api/v1/github/controller/GitHubWebhookControllerTest.java` | 신규: Controller 테스트 |

---

## Task 1: Supabase 프로젝트 설정 (수동 작업)

**Files:** Supabase 대시보드 (코드 없음)

- [ ] **Step 1: Supabase 프로젝트 생성**

  [supabase.com](https://supabase.com) → New Project → 프로젝트명 `bombom-admin` → 리전 Northeast Asia (Tokyo) 선택

- [ ] **Step 2: 테이블 생성**

  Supabase 대시보드 → SQL Editor에서 실행:

  ```sql
  CREATE TABLE reviewer (
    id               BIGSERIAL PRIMARY KEY,
    github_username  TEXT NOT NULL UNIQUE,
    display_name     TEXT NOT NULL,
    rotation_order   INTEGER NOT NULL DEFAULT 0,
    is_on_vacation   BOOLEAN NOT NULL DEFAULT FALSE,
    last_assigned_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW()
  );

  CREATE TABLE review_assignment (
    id          BIGSERIAL PRIMARY KEY,
    reviewer_id BIGINT NOT NULL REFERENCES reviewer(id),
    pr_number   INTEGER NOT NULL,
    pr_title    TEXT NOT NULL,
    pr_author   TEXT NOT NULL,
    pr_url      TEXT NOT NULL,
    assigned_at TIMESTAMPTZ DEFAULT NOW(),
    status      TEXT NOT NULL DEFAULT 'OPEN'
  );
  ```

- [ ] **Step 3: RLS 설정**

  ```sql
  ALTER TABLE reviewer ENABLE ROW LEVEL SECURITY;
  ALTER TABLE review_assignment ENABLE ROW LEVEL SECURITY;

  CREATE POLICY "reviewer_select" ON reviewer FOR SELECT TO anon, authenticated USING (true);
  CREATE POLICY "assignment_select" ON review_assignment FOR SELECT TO anon, authenticated USING (true);

  CREATE POLICY "reviewer_vacation_update" ON reviewer
    FOR UPDATE TO authenticated
    USING (true)
    WITH CHECK (true);
  ```

- [ ] **Step 4: 환경변수 메모**

  Supabase 대시보드 → Settings → API에서 다음 값을 메모:
  - `SUPABASE_URL` (Project URL)
  - `SUPABASE_SERVICE_ROLE_KEY` (service_role secret)
  - `SUPABASE_ANON_KEY` (anon public — 프론트엔드용)

---

## Task 2: Spring Boot 설정 파일 및 Properties 추가

**Files:**
- Modify: `src/main/java/me/bombom/api/v1/common/config/SecurityConfig.java`
- Create: `src/main/java/me/bombom/api/v1/github/config/GitHubReviewerProperties.java`
- Create: `src/main/java/me/bombom/api/v1/github/config/SupabaseProperties.java`

- [ ] **Step 1: SecurityConfig에 webhook 경로 인증 제외 추가**

  `src/main/java/me/bombom/api/v1/common/config/SecurityConfig.java`의 `webSecurityCustomizer()` 수정:

  ```java
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
      return web -> web.ignoring()
              .requestMatchers(
                      "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health",
                      "/admin/v3/api-docs/**", "/admin/swagger-ui/**", "/admin/swagger-ui.html",
                      "/admin/api-docs/**",
                      "/admin/api/v1/github/webhook");  // 추가
  }
  ```

- [ ] **Step 2: GitHubReviewerProperties 생성**

  ```java
  package me.bombom.api.v1.github.config;

  import org.springframework.boot.context.properties.ConfigurationProperties;

  @ConfigurationProperties(prefix = "github.reviewer")
  public record GitHubReviewerProperties(
          String token,
          String org,
          String teamSlug,
          String repo,
          String webhookSecret
  ) {}
  ```

- [ ] **Step 3: SupabaseProperties 생성**

  ```java
  package me.bombom.api.v1.github.config;

  import org.springframework.boot.context.properties.ConfigurationProperties;

  @ConfigurationProperties(prefix = "supabase")
  public record SupabaseProperties(
          String url,
          String serviceRoleKey
  ) {}
  ```

- [ ] **Step 4: BomBomServerApplication에 @EnableConfigurationProperties + @EnableScheduling 추가**

  `src/main/java/me/bombom/BomBomServerApplication.java` 수정:

  ```java
  package me.bombom;

  import me.bombom.api.v1.github.config.GitHubReviewerProperties;
  import me.bombom.api.v1.github.config.SupabaseProperties;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.boot.context.properties.EnableConfigurationProperties;
  import org.springframework.scheduling.annotation.EnableScheduling;

  @SpringBootApplication
  @EnableScheduling
  @EnableConfigurationProperties({GitHubReviewerProperties.class, SupabaseProperties.class})
  public class BomBomServerApplication {
      public static void main(String[] args) {
          SpringApplication.run(BomBomServerApplication.class, args);
      }
  }
  ```

- [ ] **Step 5: 환경변수 설정 (로컬 개발)**

  IntelliJ Run Configuration 또는 `.env` 파일에 추가 (gitignore 확인):
  ```
  GITHUB_REVIEWER_TOKEN=ghp_...
  GITHUB_REVIEWER_ORG=woowacourse-teams
  GITHUB_REVIEWER_TEAM_SLUG=2025-bom-bom
  GITHUB_REVIEWER_REPO=2025-bom-bom
  GITHUB_REVIEWER_WEBHOOK_SECRET=your-webhook-secret
  SUPABASE_URL=https://xxxx.supabase.co
  SUPABASE_SERVICE_ROLE_KEY=eyJhbGci...
  ```

  Spring Boot는 환경변수 `GITHUB_REVIEWER_TOKEN` → `github.reviewer.token`으로 자동 바인딩.

- [ ] **Step 6: 빌드 확인**

  ```bash
  ./gradlew compileJava
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: 커밋**

  ```bash
  git add src/main/java/me/bombom/BomBomServerApplication.java \
          src/main/java/me/bombom/api/v1/common/config/SecurityConfig.java \
          src/main/java/me/bombom/api/v1/github/config/
  git commit -m "feat: GitHub webhook 보안 설정 및 Properties 추가"
  ```

---

## Task 3: ReviewerDto 및 SupabaseClient

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/dto/ReviewerDto.java`
- Create: `src/main/java/me/bombom/api/v1/github/client/SupabaseClient.java`

- [ ] **Step 1: ReviewerDto 생성**

  ```java
  package me.bombom.api.v1.github.dto;

  import com.fasterxml.jackson.annotation.JsonProperty;
  import java.time.OffsetDateTime;

  public record ReviewerDto(
          Long id,
          @JsonProperty("github_username") String githubUsername,
          @JsonProperty("display_name") String displayName,
          @JsonProperty("rotation_order") int rotationOrder,
          @JsonProperty("is_on_vacation") boolean isOnVacation,
          @JsonProperty("last_assigned_at") OffsetDateTime lastAssignedAt
  ) {}
  ```

- [ ] **Step 2: SupabaseClient 생성**

  ```java
  package me.bombom.api.v1.github.client;

  import com.fasterxml.jackson.annotation.JsonProperty;
  import java.time.OffsetDateTime;
  import java.util.List;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.config.SupabaseProperties;
  import me.bombom.api.v1.github.dto.ReviewerDto;
  import org.springframework.http.HttpHeaders;
  import org.springframework.http.MediaType;
  import org.springframework.stereotype.Component;
  import org.springframework.web.reactive.function.client.WebClient;

  @Slf4j
  @Component
  @RequiredArgsConstructor
  public class SupabaseClient {

      private final WebClient.Builder webClientBuilder;
      private final SupabaseProperties supabaseProperties;

      public List<ReviewerDto> getActiveReviewers() {
          return createClient()
                  .get()
                  .uri("/rest/v1/reviewer?is_on_vacation=eq.false" +
                          "&order=last_assigned_at.asc.nullsfirst%2Crotation_order.asc")
                  .retrieve()
                  .bodyToFlux(ReviewerDto.class)
                  .collectList()
                  .block();
      }

      public void updateLastAssignedAt(Long reviewerId) {
          createClient()
                  .patch()
                  .uri("/rest/v1/reviewer?id=eq.{id}", reviewerId)
                  .bodyValue(new UpdateLastAssignedAtRequest(OffsetDateTime.now()))
                  .retrieve()
                  .toBodilessEntity()
                  .block();
      }

      public void insertAssignment(InsertAssignmentRequest request) {
          createClient()
                  .post()
                  .uri("/rest/v1/review_assignment")
                  .header("Prefer", "return=minimal")
                  .bodyValue(request)
                  .retrieve()
                  .toBodilessEntity()
                  .block();
      }

      public void upsertReviewers(List<UpsertReviewerRequest> reviewers) {
          if (reviewers.isEmpty()) return;
          createClient()
                  .post()
                  .uri("/rest/v1/reviewer")
                  .header("Prefer", "resolution=merge-duplicates,return=minimal")
                  .bodyValue(reviewers)
                  .retrieve()
                  .toBodilessEntity()
                  .block();
      }

      private WebClient createClient() {
          return webClientBuilder
                  .baseUrl(supabaseProperties.url())
                  .defaultHeader("apikey", supabaseProperties.serviceRoleKey())
                  .defaultHeader(HttpHeaders.AUTHORIZATION,
                          "Bearer " + supabaseProperties.serviceRoleKey())
                  .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .build();
      }

      public record UpdateLastAssignedAtRequest(
              @JsonProperty("last_assigned_at") OffsetDateTime lastAssignedAt,
              @JsonProperty("updated_at") OffsetDateTime updatedAt
      ) {
          public UpdateLastAssignedAtRequest(OffsetDateTime lastAssignedAt) {
              this(lastAssignedAt, OffsetDateTime.now());
          }
      }

      public record InsertAssignmentRequest(
              @JsonProperty("reviewer_id") Long reviewerId,
              @JsonProperty("pr_number") int prNumber,
              @JsonProperty("pr_title") String prTitle,
              @JsonProperty("pr_author") String prAuthor,
              @JsonProperty("pr_url") String prUrl
      ) {}

      public record UpsertReviewerRequest(
              @JsonProperty("github_username") String githubUsername,
              @JsonProperty("display_name") String displayName,
              @JsonProperty("rotation_order") int rotationOrder
      ) {}
  }
  ```

- [ ] **Step 3: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/dto/ReviewerDto.java \
          src/main/java/me/bombom/api/v1/github/client/SupabaseClient.java
  git commit -m "feat: Supabase REST API 클라이언트 추가"
  ```

---

## Task 4: ReviewerAssignmentService (round-robin 로직)

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/service/ReviewerAssignmentService.java`
- Create: `src/test/java/me/bombom/api/v1/github/service/ReviewerAssignmentServiceTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

  ```java
  package me.bombom.api.v1.github.service;

  import me.bombom.api.v1.github.client.SupabaseClient;
  import me.bombom.api.v1.github.client.SupabaseClient.InsertAssignmentRequest;
  import me.bombom.api.v1.github.dto.ReviewerDto;
  import org.junit.jupiter.api.Test;
  import org.junit.jupiter.api.extension.ExtendWith;
  import org.mockito.InjectMocks;
  import org.mockito.Mock;
  import org.mockito.junit.jupiter.MockitoExtension;

  import java.time.OffsetDateTime;
  import java.util.List;
  import java.util.Optional;

  import static org.assertj.core.api.Assertions.assertThat;
  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.ArgumentMatchers.anyLong;
  import static org.mockito.BDDMockito.given;
  import static org.mockito.Mockito.never;
  import static org.mockito.Mockito.verify;

  @ExtendWith(MockitoExtension.class)
  class ReviewerAssignmentServiceTest {

      @Mock
      private SupabaseClient supabaseClient;

      @InjectMocks
      private ReviewerAssignmentService reviewerAssignmentService;

      private ReviewerDto reviewer(long id, String username, OffsetDateTime lastAssigned) {
          return new ReviewerDto(id, username, username, 0, false, lastAssigned);
      }

      @Test
      void PR_작성자를_제외하고_last_assigned_at_가장_오래된_리뷰어_선택() {
          OffsetDateTime older = OffsetDateTime.now().minusDays(2);
          OffsetDateTime newer = OffsetDateTime.now().minusDays(1);
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", older),
                  reviewer(2L, "bob", newer)
          ));

          Optional<ReviewerDto> result = reviewerAssignmentService.selectReviewer("charlie");

          assertThat(result).isPresent();
          assertThat(result.get().githubUsername()).isEqualTo("alice");
      }

      @Test
      void PR_작성자는_리뷰어_선택에서_제외() {
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", null),
                  reviewer(2L, "bob", null)
          ));

          Optional<ReviewerDto> result = reviewerAssignmentService.selectReviewer("alice");

          assertThat(result).isPresent();
          assertThat(result.get().githubUsername()).isEqualTo("bob");
      }

      @Test
      void 활성_리뷰어가_모두_PR_작성자면_빈_Optional_반환() {
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", null)
          ));

          Optional<ReviewerDto> result = reviewerAssignmentService.selectReviewer("alice");

          assertThat(result).isEmpty();
      }

      @Test
      void last_assigned_at_null이면_가장_먼저_선택() {
          OffsetDateTime recent = OffsetDateTime.now();
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", recent),
                  reviewer(2L, "bob", null)
          ));

          Optional<ReviewerDto> result = reviewerAssignmentService.selectReviewer("charlie");

          assertThat(result.get().githubUsername()).isEqualTo("bob");
      }

      @Test
      void 배정_후_last_assigned_at_업데이트와_이력_저장() {
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", null)
          ));

          reviewerAssignmentService.assignAndRecord("pr_author",
                  42, "Fix bug", "https://github.com/test/pull/42");

          verify(supabaseClient).updateLastAssignedAt(1L);
          verify(supabaseClient).insertAssignment(any(InsertAssignmentRequest.class));
      }

      @Test
      void 배정_가능한_리뷰어_없으면_Supabase_업데이트_안함() {
          given(supabaseClient.getActiveReviewers()).willReturn(List.of(
                  reviewer(1L, "alice", null)
          ));

          reviewerAssignmentService.assignAndRecord("alice",
                  42, "Fix bug", "https://github.com/test/pull/42");

          verify(supabaseClient, never()).updateLastAssignedAt(anyLong());
          verify(supabaseClient, never()).insertAssignment(any());
      }
  }
  ```

- [ ] **Step 2: 테스트 실패 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.service.ReviewerAssignmentServiceTest"
  ```
  Expected: FAIL — `ReviewerAssignmentService` 클래스 없음

- [ ] **Step 3: ReviewerAssignmentService 구현**

  ```java
  package me.bombom.api.v1.github.service;

  import java.util.Comparator;
  import java.util.List;
  import java.util.Optional;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.client.SupabaseClient;
  import me.bombom.api.v1.github.client.SupabaseClient.InsertAssignmentRequest;
  import me.bombom.api.v1.github.dto.ReviewerDto;
  import org.springframework.stereotype.Service;

  @Slf4j
  @Service
  @RequiredArgsConstructor
  public class ReviewerAssignmentService {

      private final SupabaseClient supabaseClient;

      public Optional<ReviewerDto> selectReviewer(String prAuthor) {
          List<ReviewerDto> candidates = supabaseClient.getActiveReviewers().stream()
                  .filter(r -> !r.githubUsername().equalsIgnoreCase(prAuthor))
                  .sorted(Comparator.comparing(ReviewerDto::lastAssignedAt,
                          Comparator.nullsFirst(Comparator.naturalOrder()))
                          .thenComparingInt(ReviewerDto::rotationOrder))
                  .toList();

          return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.getFirst());
      }

      public Optional<ReviewerDto> assignAndRecord(String prAuthor, int prNumber,
              String prTitle, String prUrl) {
          Optional<ReviewerDto> selected = selectReviewer(prAuthor);
          selected.ifPresentOrElse(
                  reviewer -> {
                      supabaseClient.updateLastAssignedAt(reviewer.id());
                      supabaseClient.insertAssignment(new InsertAssignmentRequest(
                              reviewer.id(), prNumber, prTitle, prAuthor, prUrl));
                      log.info("PR #{} 리뷰어 배정: {}", prNumber, reviewer.githubUsername());
                  },
                  () -> log.warn("PR #{} 배정 가능한 리뷰어 없음 (작성자: {})", prNumber, prAuthor)
          );
          return selected;
      }
  }
  ```

- [ ] **Step 4: 테스트 통과 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.service.ReviewerAssignmentServiceTest"
  ```
  Expected: 6 tests passed

- [ ] **Step 5: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/service/ReviewerAssignmentService.java \
          src/test/java/me/bombom/api/v1/github/service/ReviewerAssignmentServiceTest.java
  git commit -m "feat: round-robin 리뷰어 선택 로직 구현"
  ```

---

## Task 5: GitHubWebhookVerifier (HMAC-SHA256 시그니처 검증)

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/security/GitHubWebhookVerifier.java`
- Create: `src/test/java/me/bombom/api/v1/github/security/GitHubWebhookVerifierTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

  ```java
  package me.bombom.api.v1.github.security;

  import me.bombom.api.v1.github.config.GitHubReviewerProperties;
  import org.junit.jupiter.api.Test;

  import java.nio.charset.StandardCharsets;
  import javax.crypto.Mac;
  import javax.crypto.spec.SecretKeySpec;

  import static org.assertj.core.api.Assertions.assertThat;

  class GitHubWebhookVerifierTest {

      private static final String SECRET = "my-secret";
      private final GitHubWebhookVerifier verifier = new GitHubWebhookVerifier(
              new GitHubReviewerProperties("token", "org", "team", "repo", SECRET)
      );

      @Test
      void 유효한_시그니처_검증_성공() throws Exception {
          String payload = "{\"action\":\"opened\"}";
          String signature = "sha256=" + computeHmac(SECRET, payload);
          assertThat(verifier.verify(payload.getBytes(StandardCharsets.UTF_8), signature)).isTrue();
      }

      @Test
      void 잘못된_시그니처_검증_실패() {
          String payload = "{\"action\":\"opened\"}";
          assertThat(verifier.verify(payload.getBytes(StandardCharsets.UTF_8), "sha256=invalid")).isFalse();
      }

      @Test
      void 시그니처_헤더_없으면_검증_실패() {
          assertThat(verifier.verify("payload".getBytes(), null)).isFalse();
      }

      private String computeHmac(String secret, String payload) throws Exception {
          Mac mac = Mac.getInstance("HmacSHA256");
          mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
          byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
          StringBuilder sb = new StringBuilder();
          for (byte b : bytes) sb.append(String.format("%02x", b));
          return sb.toString();
      }
  }
  ```

- [ ] **Step 2: 테스트 실패 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.security.GitHubWebhookVerifierTest"
  ```
  Expected: FAIL

- [ ] **Step 3: GitHubWebhookVerifier 구현**

  ```java
  package me.bombom.api.v1.github.security;

  import java.nio.charset.StandardCharsets;
  import javax.crypto.Mac;
  import javax.crypto.spec.SecretKeySpec;
  import lombok.RequiredArgsConstructor;
  import me.bombom.api.v1.github.config.GitHubReviewerProperties;
  import org.springframework.stereotype.Component;

  @Component
  @RequiredArgsConstructor
  public class GitHubWebhookVerifier {

      private final GitHubReviewerProperties properties;

      public boolean verify(byte[] payload, String signatureHeader) {
          if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
              return false;
          }
          String expected = "sha256=" + computeHmac(properties.webhookSecret(), payload);
          return constantTimeEquals(expected, signatureHeader);
      }

      private String computeHmac(String secret, byte[] payload) {
          try {
              Mac mac = Mac.getInstance("HmacSHA256");
              mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
              byte[] result = mac.doFinal(payload);
              StringBuilder sb = new StringBuilder();
              for (byte b : result) sb.append(String.format("%02x", b));
              return sb.toString();
          } catch (Exception e) {
              throw new RuntimeException("HMAC 계산 실패", e);
          }
      }

      private boolean constantTimeEquals(String a, String b) {
          if (a.length() != b.length()) return false;
          int diff = 0;
          for (int i = 0; i < a.length(); i++) {
              diff |= a.charAt(i) ^ b.charAt(i);
          }
          return diff == 0;
      }
  }
  ```

- [ ] **Step 4: 테스트 통과 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.security.GitHubWebhookVerifierTest"
  ```
  Expected: 3 tests passed

- [ ] **Step 5: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/security/GitHubWebhookVerifier.java \
          src/test/java/me/bombom/api/v1/github/security/GitHubWebhookVerifierTest.java
  git commit -m "feat: GitHub Webhook HMAC-SHA256 시그니처 검증기 추가"
  ```

---

## Task 6: GitHubApiClient (PR에 리뷰어 배정 + 팀 멤버 조회)

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/client/GitHubApiClient.java`

- [ ] **Step 1: GitHubApiClient 구현**

  기존 `LambdaPlaywrightService.java`와 동일한 WebClient 패턴 사용.

  ```java
  package me.bombom.api.v1.github.client;

  import java.util.List;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.config.GitHubReviewerProperties;
  import org.springframework.http.HttpHeaders;
  import org.springframework.stereotype.Component;
  import org.springframework.web.reactive.function.client.WebClient;
  import org.springframework.web.reactive.function.client.WebClientResponseException;

  @Slf4j
  @Component
  @RequiredArgsConstructor
  public class GitHubApiClient {

      private static final String GITHUB_API_BASE = "https://api.github.com";

      private final WebClient.Builder webClientBuilder;
      private final GitHubReviewerProperties properties;

      public void assignReviewer(int prNumber, String reviewerUsername) {
          try {
              createClient()
                      .post()
                      .uri("/repos/{org}/{repo}/pulls/{pr}/requested_reviewers",
                              properties.org(), properties.repo(), prNumber)
                      .bodyValue(new AssignReviewerRequest(List.of(reviewerUsername)))
                      .retrieve()
                      .toBodilessEntity()
                      .block();
              log.info("PR #{} GitHub 리뷰어 배정 성공: {}", prNumber, reviewerUsername);
          } catch (WebClientResponseException e) {
              log.error("GitHub PR 리뷰어 배정 실패 (pr={}, reviewer={}, status={})",
                      prNumber, reviewerUsername, e.getStatusCode().value(), e);
          }
      }

      public List<GitHubTeamMember> getTeamMembers() {
          return createClient()
                  .get()
                  .uri("/orgs/{org}/teams/{team}/members",
                          properties.org(), properties.teamSlug())
                  .retrieve()
                  .bodyToFlux(GitHubTeamMember.class)
                  .collectList()
                  .block();
      }

      private WebClient createClient() {
          return webClientBuilder
                  .baseUrl(GITHUB_API_BASE)
                  .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
                  .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                  .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                  .build();
      }

      public record AssignReviewerRequest(List<String> reviewers) {}
      public record GitHubTeamMember(String login, String name) {}
  }
  ```

- [ ] **Step 2: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/client/GitHubApiClient.java
  git commit -m "feat: GitHub API 클라이언트 (리뷰어 배정, 팀 멤버 조회)"
  ```

---

## Task 7: PullRequestOpenedEvent + GitHubWebhookController

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/dto/PullRequestOpenedEvent.java`
- Create: `src/main/java/me/bombom/api/v1/github/controller/GitHubWebhookController.java`
- Create: `src/test/java/me/bombom/api/v1/github/controller/GitHubWebhookControllerTest.java`

- [ ] **Step 1: PullRequestOpenedEvent DTO 생성**

  ```java
  package me.bombom.api.v1.github.dto;

  import com.fasterxml.jackson.annotation.JsonProperty;

  public record PullRequestOpenedEvent(
          String action,
          @JsonProperty("pull_request") PullRequestInfo pullRequest
  ) {
      public record PullRequestInfo(
              int number,
              String title,
              @JsonProperty("html_url") String htmlUrl,
              User user
      ) {}

      public record User(String login) {}
  }
  ```

- [ ] **Step 2: 실패하는 Controller 테스트 작성**

  ```java
  package me.bombom.api.v1.github.controller;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import me.bombom.api.v1.github.client.GitHubApiClient;
  import me.bombom.api.v1.github.dto.ReviewerDto;
  import me.bombom.api.v1.github.security.GitHubWebhookVerifier;
  import me.bombom.api.v1.github.service.ReviewerAssignmentService;
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
  import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
  import org.springframework.http.MediaType;
  import org.springframework.test.context.bean.override.mockito.MockitoBean;
  import org.springframework.test.web.servlet.MockMvc;

  import java.util.Optional;

  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.ArgumentMatchers.anyInt;
  import static org.mockito.ArgumentMatchers.anyString;
  import static org.mockito.BDDMockito.given;
  import static org.mockito.Mockito.never;
  import static org.mockito.Mockito.verify;
  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

  @WebMvcTest(controllers = GitHubWebhookController.class)
  class GitHubWebhookControllerTest {

      @Autowired
      private MockMvc mockMvc;

      @Autowired
      private ObjectMapper objectMapper;

      @MockitoBean
      private GitHubWebhookVerifier verifier;

      @MockitoBean
      private ReviewerAssignmentService assignmentService;

      @MockitoBean
      private GitHubApiClient gitHubApiClient;

      @MockitoBean
      private JpaMetamodelMappingContext jpaMetamodelMappingContext;

      private static final String WEBHOOK_URL = "/admin/api/v1/github/webhook";

      @Test
      void 유효한_시그니처와_PR_opened시_리뷰어_배정() throws Exception {
          String payload = """
                  {"action":"opened","pull_request":{"number":1,"title":"Fix bug",
                  "html_url":"https://github.com/test/repo/pull/1","user":{"login":"alice"}}}
                  """;
          given(verifier.verify(any(), anyString())).willReturn(true);
          given(assignmentService.assignAndRecord(anyString(), anyInt(), anyString(), anyString()))
                  .willReturn(Optional.of(new ReviewerDto(1L, "bob", "Bob", 0, false, null)));

          mockMvc.perform(post(WEBHOOK_URL)
                          .contentType(MediaType.APPLICATION_JSON)
                          .header("X-Hub-Signature-256", "sha256=valid")
                          .header("X-GitHub-Event", "pull_request")
                          .content(payload))
                  .andExpect(status().isOk());

          verify(gitHubApiClient).assignReviewer(1, "bob");
      }

      @Test
      void 잘못된_시그니처면_403_반환() throws Exception {
          given(verifier.verify(any(), anyString())).willReturn(false);

          mockMvc.perform(post(WEBHOOK_URL)
                          .contentType(MediaType.APPLICATION_JSON)
                          .header("X-Hub-Signature-256", "sha256=invalid")
                          .header("X-GitHub-Event", "pull_request")
                          .content("{\"action\":\"opened\"}"))
                  .andExpect(status().isForbidden());

          verify(assignmentService, never()).assignAndRecord(anyString(), anyInt(), anyString(), anyString());
      }

      @Test
      void PR_opened가_아닌_action이면_배정_없이_200_반환() throws Exception {
          given(verifier.verify(any(), anyString())).willReturn(true);

          mockMvc.perform(post(WEBHOOK_URL)
                          .contentType(MediaType.APPLICATION_JSON)
                          .header("X-Hub-Signature-256", "sha256=valid")
                          .header("X-GitHub-Event", "pull_request")
                          .content("{\"action\":\"closed\",\"pull_request\":{\"number\":1,\"title\":\"t\",\"html_url\":\"u\",\"user\":{\"login\":\"a\"}}}"))
                  .andExpect(status().isOk());

          verify(assignmentService, never()).assignAndRecord(anyString(), anyInt(), anyString(), anyString());
      }
  }
  ```

- [ ] **Step 3: 테스트 실패 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.controller.GitHubWebhookControllerTest"
  ```
  Expected: FAIL

- [ ] **Step 4: GitHubWebhookController 구현**

  ```java
  package me.bombom.api.v1.github.controller;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import java.util.Optional;
  import java.util.Set;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.client.GitHubApiClient;
  import me.bombom.api.v1.github.dto.PullRequestOpenedEvent;
  import me.bombom.api.v1.github.dto.ReviewerDto;
  import me.bombom.api.v1.github.security.GitHubWebhookVerifier;
  import me.bombom.api.v1.github.service.ReviewerAssignmentService;
  import org.springframework.http.HttpStatus;
  import org.springframework.web.bind.annotation.PostMapping;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestHeader;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.ResponseStatus;
  import org.springframework.web.bind.annotation.RestController;
  import org.springframework.web.server.ResponseStatusException;

  @Slf4j
  @RestController
  @RequiredArgsConstructor
  @RequestMapping("/admin/api/v1/github")
  public class GitHubWebhookController {

      private static final Set<String> ASSIGNABLE_ACTIONS = Set.of("opened", "reopened");

      private final GitHubWebhookVerifier verifier;
      private final ReviewerAssignmentService assignmentService;
      private final GitHubApiClient gitHubApiClient;
      private final ObjectMapper objectMapper;

      @PostMapping("/webhook")
      @ResponseStatus(HttpStatus.OK)
      public void handleWebhook(
              @RequestHeader("X-Hub-Signature-256") String signature,
              @RequestHeader(value = "X-GitHub-Event", defaultValue = "") String event,
              @RequestBody byte[] rawPayload) {

          if (!verifier.verify(rawPayload, signature)) {
              throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid signature");
          }

          if (!"pull_request".equals(event)) {
              return;
          }

          PullRequestOpenedEvent body = parsePayload(rawPayload);
          if (!ASSIGNABLE_ACTIONS.contains(body.action())) {
              return;
          }

          PullRequestOpenedEvent.PullRequestInfo pr = body.pullRequest();
          Optional<ReviewerDto> reviewer = assignmentService.assignAndRecord(
                  pr.user().login(), pr.number(), pr.title(), pr.htmlUrl());

          reviewer.ifPresent(r -> gitHubApiClient.assignReviewer(pr.number(), r.githubUsername()));
      }

      private PullRequestOpenedEvent parsePayload(byte[] rawPayload) {
          try {
              return objectMapper.readValue(rawPayload, PullRequestOpenedEvent.class);
          } catch (Exception e) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
          }
      }
  }
  ```

- [ ] **Step 5: 테스트 통과 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.controller.GitHubWebhookControllerTest"
  ```
  Expected: 3 tests passed

- [ ] **Step 6: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/ \
          src/test/java/me/bombom/api/v1/github/controller/
  git commit -m "feat: GitHub Webhook 컨트롤러 및 DTO 구현"
  ```

---

## Task 8: ReviewerSyncService + ReviewerSyncScheduler

**Files:**
- Create: `src/main/java/me/bombom/api/v1/github/service/ReviewerSyncService.java`
- Create: `src/main/java/me/bombom/api/v1/github/scheduler/ReviewerSyncScheduler.java`
- Create: `src/test/java/me/bombom/api/v1/github/service/ReviewerSyncServiceTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

  ```java
  package me.bombom.api.v1.github.service;

  import me.bombom.api.v1.github.client.GitHubApiClient;
  import me.bombom.api.v1.github.client.GitHubApiClient.GitHubTeamMember;
  import me.bombom.api.v1.github.client.SupabaseClient;
  import me.bombom.api.v1.github.client.SupabaseClient.UpsertReviewerRequest;
  import org.junit.jupiter.api.Test;
  import org.junit.jupiter.api.extension.ExtendWith;
  import org.mockito.ArgumentCaptor;
  import org.mockito.InjectMocks;
  import org.mockito.Mock;
  import org.mockito.junit.jupiter.MockitoExtension;

  import java.util.List;

  import static org.assertj.core.api.Assertions.assertThat;
  import static org.mockito.BDDMockito.given;
  import static org.mockito.Mockito.verify;

  @ExtendWith(MockitoExtension.class)
  class ReviewerSyncServiceTest {

      @Mock
      private GitHubApiClient gitHubApiClient;

      @Mock
      private SupabaseClient supabaseClient;

      @InjectMocks
      private ReviewerSyncService reviewerSyncService;

      @Test
      void GitHub_팀_멤버를_Supabase에_upsert() {
          given(gitHubApiClient.getTeamMembers()).willReturn(List.of(
                  new GitHubTeamMember("alice", "Alice Kim"),
                  new GitHubTeamMember("bob", null)
          ));

          reviewerSyncService.sync();

          ArgumentCaptor<List<UpsertReviewerRequest>> captor = ArgumentCaptor.captor();
          verify(supabaseClient).upsertReviewers(captor.capture());
          List<UpsertReviewerRequest> upserted = captor.getValue();

          assertThat(upserted).hasSize(2);
          assertThat(upserted.get(0).githubUsername()).isEqualTo("alice");
          assertThat(upserted.get(0).displayName()).isEqualTo("Alice Kim");
          assertThat(upserted.get(1).displayName()).isEqualTo("bob"); // name null이면 login 사용
      }
  }
  ```

- [ ] **Step 2: 테스트 실패 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.service.ReviewerSyncServiceTest"
  ```
  Expected: FAIL

- [ ] **Step 3: ReviewerSyncService 구현**

  ```java
  package me.bombom.api.v1.github.service;

  import java.util.List;
  import java.util.concurrent.atomic.AtomicInteger;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.client.GitHubApiClient;
  import me.bombom.api.v1.github.client.GitHubApiClient.GitHubTeamMember;
  import me.bombom.api.v1.github.client.SupabaseClient;
  import me.bombom.api.v1.github.client.SupabaseClient.UpsertReviewerRequest;
  import org.springframework.stereotype.Service;

  @Slf4j
  @Service
  @RequiredArgsConstructor
  public class ReviewerSyncService {

      private final GitHubApiClient gitHubApiClient;
      private final SupabaseClient supabaseClient;

      public void sync() {
          List<GitHubTeamMember> members = gitHubApiClient.getTeamMembers();
          AtomicInteger order = new AtomicInteger(0);
          List<UpsertReviewerRequest> requests = members.stream()
                  .map(m -> new UpsertReviewerRequest(
                          m.login(),
                          m.name() != null ? m.name() : m.login(),
                          order.getAndIncrement()))
                  .toList();
          supabaseClient.upsertReviewers(requests);
          log.info("리뷰어 동기화 완료: {}명", requests.size());
      }
  }
  ```

- [ ] **Step 4: ReviewerSyncScheduler 구현**

  ```java
  package me.bombom.api.v1.github.scheduler;

  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import me.bombom.api.v1.github.service.ReviewerSyncService;
  import org.springframework.scheduling.annotation.Scheduled;
  import org.springframework.stereotype.Component;

  @Slf4j
  @Component
  @RequiredArgsConstructor
  public class ReviewerSyncScheduler {

      private final ReviewerSyncService reviewerSyncService;

      @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
      public void syncReviewers() {
          log.info("리뷰어 동기화 시작");
          try {
              reviewerSyncService.sync();
          } catch (Exception e) {
              log.error("리뷰어 동기화 실패", e);
          }
      }
  }
  ```

- [ ] **Step 5: 테스트 통과 확인**

  ```bash
  ./gradlew test --tests "me.bombom.api.v1.github.service.ReviewerSyncServiceTest"
  ```
  Expected: 1 test passed

- [ ] **Step 6: 커밋**

  ```bash
  git add src/main/java/me/bombom/api/v1/github/service/ReviewerSyncService.java \
          src/main/java/me/bombom/api/v1/github/scheduler/ReviewerSyncScheduler.java \
          src/test/java/me/bombom/api/v1/github/service/ReviewerSyncServiceTest.java
  git commit -m "feat: GitHub 팀 멤버 자동 동기화 스케줄러 추가"
  ```

---

## Task 9: GitHub Webhook 등록 (수동)

- [ ] **Step 1: GitHub 레포 Webhook 등록**

  `https://github.com/woowacourse-teams/2025-bom-bom` → Settings → Webhooks → Add webhook:
  - Payload URL: `https://<admin-domain>/admin/api/v1/github/webhook`
  - Content type: `application/json`
  - Secret: `GITHUB_REVIEWER_WEBHOOK_SECRET` 값과 동일
  - Events: `Pull requests` 체크

- [ ] **Step 2: GitHub Token 권한 확인**

  `GITHUB_REVIEWER_TOKEN` PAT에 필요한 권한:
  - `repo` (PR reviewer 배정)
  - `read:org` (팀 멤버 조회)

---

## Task 10: 전체 테스트 실행

- [ ] **Step 1: 전체 테스트 실행**

  ```bash
  ./gradlew test
  ```
  Expected: BUILD SUCCESSFUL, 모든 신규 테스트 통과

---

## 다음 단계

프론트엔드 구현 플랜:
`/Users/wonmac/code/client/docs/superpowers/plans/2026-06-17-auto-reviewer-frontend.md`
