# 자동 리뷰어 배정 시스템 설계

**작성일**: 2026-06-17  
**대상 레포**: woowacourse-teams/2025-bom-bom  
**구현 대상**: admin (Spring Boot 백엔드) + client/admin (React 프론트엔드)

---

## 1. 개요

PR이 열릴 때마다 팀 멤버 중 한 명을 자동으로 리뷰어로 배정하는 시스템.  
순환(round-robin) 방식으로 공평하게 배정하며, 휴가 중인 멤버는 건너뜀.  
admin UI에서 휴가 설정 및 리뷰 통계를 확인할 수 있음.

---

## 2. 전체 아키텍처

```
woowacourse-teams/2025-bom-bom
    ↓ PR opened/reopened → GitHub Webhook
    ↓ POST /api/v1/github/webhook
admin Spring Boot
    ├── X-Hub-Signature-256 검증
    ├── Supabase에서 활성 리뷰어 목록 조회
    ├── 순환 로직으로 다음 리뷰어 결정
    ├── GitHub API로 PR에 reviewer 배정
    └── Supabase review_assignment에 이력 저장

GitHub Sync Scheduler (매일 새벽 2시)
    → GitHub API (woowacourse-teams/bom-bom 팀 멤버 조회)
    → Supabase reviewer 테이블에 upsert

admin React (client/admin)
    → @supabase/supabase-js
        ├── 리뷰어 목록 조회 + 휴가 토글 (직접 RW)
        └── 월/주별 통계 + 현재 배정 PR 목록 조회
```

---

## 3. 데이터 저장소 — Supabase

운영 DB(production)에는 영향 없음. Supabase 프로젝트를 별도 생성하여 admin 전용 데이터 저장.

### 3.1 테이블 스키마

```sql
-- 리뷰어 목록
CREATE TABLE reviewer (
  id               BIGSERIAL PRIMARY KEY,
  github_username  TEXT NOT NULL UNIQUE,
  display_name     TEXT NOT NULL,
  rotation_order   INTEGER NOT NULL,      -- 순환 순서, 낮을수록 먼저
  is_on_vacation   BOOLEAN NOT NULL DEFAULT FALSE,
  last_assigned_at TIMESTAMPTZ,           -- 순환 판단 기준
  created_at       TIMESTAMPTZ DEFAULT NOW(),
  updated_at       TIMESTAMPTZ DEFAULT NOW()
);

-- 배정 이력
CREATE TABLE review_assignment (
  id          BIGSERIAL PRIMARY KEY,
  reviewer_id BIGINT NOT NULL REFERENCES reviewer(id),
  pr_number   INTEGER NOT NULL,
  pr_title    TEXT NOT NULL,
  pr_author   TEXT NOT NULL,           -- GitHub username
  pr_url      TEXT NOT NULL,
  assigned_at TIMESTAMPTZ DEFAULT NOW(),
  status      TEXT NOT NULL DEFAULT 'OPEN'  -- 'OPEN' | 'CLOSED'
);
```

### 3.2 Row Level Security (RLS)

| 역할 | reviewer | review_assignment |
|---|---|---|
| `service_role` (Spring Boot) | 전체 RW | 전체 RW |
| `authenticated` (admin 로그인 유저) | SELECT + UPDATE(is_on_vacation) | SELECT |
| `anon` | SELECT | SELECT |

---

## 4. Spring Boot 백엔드

### 4.1 패키지 구조

```
me.bombom.api.v1/
  github/
    controller/   GitHubWebhookController.java
    dto/          PullRequestOpenedEvent.java
    client/       GitHubApiClient.java         -- WebClient 기반 GitHub REST API 호출
    security/     GitHubWebhookVerifier.java   -- HMAC-SHA256 시그니처 검증
  reviewer/
    service/      ReviewerAssignmentService.java  -- 순환 배정 로직
                  ReviewerSyncService.java         -- GitHub 팀 멤버 → Supabase sync
    scheduler/    ReviewerSyncScheduler.java       -- @Scheduled 매일 02:00
    client/       SupabaseClient.java              -- Supabase REST API (WebClient)
```

### 4.2 Webhook 처리 흐름

```
POST /api/v1/github/webhook
  1. X-Hub-Signature-256 검증 (HMAC-SHA256, GITHUB_WEBHOOK_SECRET)
  2. action 확인: "opened" 또는 "reopened"만 처리
  3. PR 작성자 github_username 추출
  4. Supabase에서 활성 리뷰어 조회
       WHERE is_on_vacation = false
         AND github_username != PR 작성자
       ORDER BY last_assigned_at ASC NULLS FIRST,
                rotation_order ASC
  5. 1번째 결과를 다음 리뷰어로 선택
  6. GitHub API: PATCH /repos/woowacourse-teams/2025-bom-bom/pulls/{pr_number}/requested_reviewers
       body: { "reviewers": ["<github_username>"] }
  7. reviewer.last_assigned_at 업데이트 (Supabase)
  8. review_assignment 레코드 삽입 (Supabase)
```

### 4.3 순환 알고리즘 상세

- `last_assigned_at`이 null이거나 가장 오래된 사람 = 다음 차례
- 동점 시 `rotation_order`가 낮은 사람 우선
- 휴가자, PR 작성자 제외
- 모두 휴가 중이면: 에러 로깅 후 배정 스킵 (Discord/Slack 알림 옵션)

### 4.4 GitHub 팀 동기화 (스케줄러)

```
매일 02:00 (KST)
  GET /orgs/woowacourse-teams/teams/bom-bom/members
  → Supabase reviewer upsert (ON CONFLICT github_username DO UPDATE display_name)
  → 탈퇴한 멤버: is_on_vacation = true (삭제 대신 비활성화, 이력 보존)
  → 신규 멤버: rotation_order = 현재 최대값 + 1
```

### 4.5 환경변수

```
GITHUB_WEBHOOK_SECRET       GitHub Webhook 시크릿
GITHUB_TOKEN                GitHub Personal Access Token (repo, read:org 권한)
GITHUB_ORG                  woowacourse-teams
GITHUB_TEAM_SLUG            bom-bom
GITHUB_REPO                 2025-bom-bom
SUPABASE_URL                Supabase 프로젝트 URL
SUPABASE_SERVICE_ROLE_KEY   Supabase service_role 키 (백엔드 전용)
```

### 4.6 신규 API 엔드포인트 (필요 시 추가)

대부분의 조회/수정은 프론트엔드가 Supabase 직접 호출로 처리하므로 백엔드 API는 최소화.

```
POST /api/v1/github/webhook   -- Webhook 수신 (공개, 시그니처 검증으로 보안)
```

---

## 5. admin 프론트엔드 (client/admin)

### 5.1 신규 라우트

```
client/admin/src/routes/_admin/
  reviewers.tsx           -- 레이아웃 (서브 네비게이션)
  reviewers/
    index.tsx             -- 리뷰어 목록 + 휴가 토글
    stats.tsx             -- 통계 대시보드
```

### 5.2 리뷰어 목록 페이지 (`/reviewers`)

- 테이블 컬럼: 이름 | GitHub | 이번 달 리뷰 수 | 이번 주 리뷰 수 | 현재 배정 PR 수 | 휴가 토글
- 데이터: `@supabase/supabase-js`로 `reviewer` + `review_assignment` 조인 조회
- 휴가 토글: `UPDATE reviewer SET is_on_vacation = !current, updated_at = NOW() WHERE id = ?`
- 인증: Supabase Auth (admin 로그인 유저만 토글 가능, RLS로 보호)

### 5.3 통계 페이지 (`/reviewers/stats`)

- 월 선택기 → 해당 월 리뷰어별 리뷰 수 바 차트
- 주차 선택기 → 해당 주 리뷰어별 리뷰 수
- 현재 `status = 'OPEN'`인 배정 PR 목록 (PR 제목 클릭 → GitHub 링크)

### 5.4 환경변수 (client/admin)

```
VITE_SUPABASE_URL          Supabase 프로젝트 URL
VITE_SUPABASE_ANON_KEY     Supabase anon 키 (공개 읽기용, RLS로 보호)
```

---

## 6. GitHub 설정

### 6.1 Webhook 설정 (woowacourse-teams/2025-bom-bom)

- URL: `https://admin.bombom.me/api/v1/github/webhook` (예시)
- Content type: `application/json`
- Secret: `GITHUB_WEBHOOK_SECRET`
- Events: `Pull requests` 체크

### 6.2 GitHub Token 권한

- `repo` (PR reviewer 배정)
- `read:org` (팀 멤버 조회)

---

## 7. 에러 처리

| 상황 | 처리 |
|---|---|
| 모든 리뷰어가 휴가 중 | 배정 스킵, 로그 기록 |
| GitHub API 실패 | 재시도 없음, 에러 로그 (Webhook은 GitHub가 재전송) |
| Supabase 연결 실패 | 500 반환 → GitHub가 Webhook 재전송 |
| 시그니처 검증 실패 | 403 즉시 반환 |

---

## 8. 구현 우선순위

1. Supabase 테이블 생성 + RLS 설정
2. Spring Boot: GitHub Webhook 수신 + 시그니처 검증
3. Spring Boot: Supabase 클라이언트 + 순환 배정 로직
4. Spring Boot: GitHub API 리뷰어 배정
5. Spring Boot: GitHub 팀 동기화 스케줄러
6. admin React: 리뷰어 목록 + 휴가 토글 페이지
7. admin React: 통계 페이지
