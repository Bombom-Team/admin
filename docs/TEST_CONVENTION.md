# TEST_CONVENTION.md
## 어드민 서버 테스트 코드 컨벤션

---

## 1. 기본 원칙

### 1.1 테스트 프레임워크
- **JUnit 5**
- **AssertJ**

모든 테스트는 JUnit 5 기반으로 작성하며,  
Assertion은 AssertJ 사용을 **원칙**으로 한다.

---

### 1.2 테스트 유형별 설정

#### Controller 테스트
- 어노테이션: `@ControllerTest`
- `@WebMvcTest` + `@WithMockUser`가 결합된 **커스텀 어노테이션**
- 목적:
    - Request 매핑
    - 파라미터 바인딩
    - Response Status / Body 검증
- Service 계층은 **Mock**으로 처리

#### Service / Repository 테스트
- 어노테이션: `@DataJpaTest`
- JPA 관련 컴포넌트만 로드
- 필요 시 `@Import`로 Service, Querydsl 설정 추가
- 목적:
    - 비즈니스 로직 검증
    - 실제 DB 쿼리 동작 검증
    - 트랜잭션 처리 검증

---

### 1.3 DB 격리 전략

- `@DataJpaTest`의 **기본 트랜잭션 롤백**에 의존
- `setUp()`에서 `deleteAllInBatch()` 호출 ❌
- 각 테스트는 **독립적인 트랜잭션**으로 실행됨을 전제로 한다

---

## 2. 네이밍 규칙

### 2.1 테스트 클래스명
- 형식: `{대상클래스}Test`
- 예:
    - `MemberServiceTest`
    - `NewsletterControllerTest`

---

### 2.2 테스트 메서드명
- **한글 스네이크 케이스**
- 형식:
    - `행위_조건_결과`
    - 또는 `행위_설명`

예시:
- `회원_목록_조회_성공`
- `존재하지_않는_회원_조회_시_예외_발생`
- `관리자_권한으로만_삭제_가능`

---

## 3. 테스트 구조 (BDD)

모든 테스트 메서드는 **BDD 3단 구조**를 따른다.

- `given` : 테스트 준비
- `when` : 실행
- `then` : 검증

```java
@Test
void 회원_목록_조회_성공() {
    // given
    // 테스트 데이터 및 Mock 설정

    // when
    // 테스트 대상 메서드 실행

    // then
    // 결과 검증
}
```
---

## 4. 계층별 테스트 가이드

테스트는 **계층의 책임을 넘지 않도록 분리**하여 작성한다.  
각 테스트는 검증 대상과 범위가 명확해야 하며,  
다른 계층의 동작을 함께 검증하려는 시도를 금지한다.

---

### 4.1 Controller 테스트

#### 기본 규칙
- `@ControllerTest` 사용
- `MockMvc` 기반 테스트
- Service 계층은 **반드시 Mock 처리**

#### 검증 대상
- HTTP Status
- Request / Response 매핑
- Validation 동작
- Query String 엣지 케이스

#### 주의 사항
- 비즈니스 로직 검증 ❌
- Repository 접근 ❌

```java
@ControllerTest(controllers = MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 회원_조회_성공() throws Exception {
        // given
        given(memberService.getMember(any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/members/{id}", 1L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1L));
    }
}
```

---

### 4.2 Service / Repository 테스트

#### 기본 규칙
- 어노테이션: `@DataJpaTest`
- JPA 관련 컴포넌트만 로드하여 테스트한다
- 필요 시 아래와 같이 `@Import`를 사용하여 대상 Service 및 설정 클래스를 명시적으로 추가한다
    - 예: Service 클래스, Querydsl 설정 등
- 전체 Spring Context를 띄우는 테스트는 지양한다

#### 목적
- 비즈니스 로직 검증
- 트랜잭션 처리 검증
- 실제 DB 쿼리 동작 검증 (JPA / Querydsl)

#### DB 격리 전략
- `@DataJpaTest`가 제공하는 **기본 트랜잭션 롤백**을 사용한다
- `@BeforeEach` 또는 `setUp()`에서
    - `deleteAll()`
    - `deleteAllInBatch()`
      와 같은 명시적 데이터 정리 로직을 **작성하지 않는다**

각 테스트는 독립적인 트랜잭션으로 실행됨을 전제로 한다.

#### 금지 사항
- Controller 계층 테스트 ❌
- Mock 기반 Repository 테스트 ❌
- DB 동작을 Mock으로 대체 ❌

---

#### 예시

```java
@DataJpaTest
@Import({MemberService.class, QuerydslConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원_생성_성공() {
        // given
        CreateMemberRequest request =
                new CreateMemberRequest("user", "ADMIN");

        // when
        Long savedId = memberService.createMember(request);

        // then
        Member member = memberRepository.findById(savedId).orElseThrow();
        assertThat(member.getNickname()).isEqualTo("user");
    }
}
```

## 5. 검증 (Assertion)

테스트의 검증은 **명확하고 실패 원인이 드러나도록** 작성해야 한다.  
Assertion은 **AssertJ 사용을 원칙**으로 한다.

---

### 5.1 기본 원칙

- `assertThat` 사용을 기본으로 한다
- JUnit 기본 assertion(`assertEquals`, `assertTrue` 등) 사용 ❌
- 한 검증은 하나의 의미만 가지도록 작성한다

```java
assertThat(result).isEqualTo(expected);
```

---

### 5.2 다중 검증 규칙

여러 필드를 동시에 검증해야 하는 경우
assertSoftly를 사용하여 한 번에 검증한다.
- 첫 번째 실패에서 중단 ❌
- 모든 실패를 한 번에 확인 ⭕

```java
assertSoftly(softly -> {
    softly.assertThat(response.name()).isEqualTo("test");
    softly.assertThat(response.age()).isEqualTo(20);
});
```

---

### 5.3 검증 범위 제한
- 하나의 테스트에서 여러 책임을 동시에 검증하지 않는다
- 상태 + 부수 효과 + 예외를 한 테스트에서 모두 검증 ❌
- 필요 시 테스트를 분리한다

--- 

### 5.4 예외 검증

예외 검증 시에도 AssertJ 스타일을 유지한다.
```java
assertThatThrownBy(() -> service.findById(1L))
        .isInstanceOf(NotFoundException.class);
```

---

### 5.5 금지 사항
- Assertion 없이 실행만 하는 테스트 ❌
- 로그 출력으로 결과를 확인하는 테스트 ❌
- 조건문(if)으로 검증 분기 ❌

---

## 6. Fixture (테스트 데이터 생성)

테스트 데이터는 **테스트 의도와 맥락이 코드에 그대로 드러나도록** 생성한다.  
재사용성보다 **가독성과 명확성**을 우선한다.

---

### 6.1 기본 원칙

- 공용 `TestFixture` 클래스 **미사용**
- 테스트 데이터는 **각 테스트 클래스 내부에서 생성**
- 테스트 외부로 Fixture를 추출하지 않는다

이유:
- 테스트 맥락 파악이 어려워짐
- 의미 없는 범용 Fixture 양산 방지
- 테스트 수정 시 파급 범위 최소화

---

### 6.2 데이터 생성 방식

아래 방식 중 하나를 사용한다.

- Entity의 **Builder 패턴**
- 테스트 클래스 내부의 **private helper method**

---

### 6.3 Builder 패턴 사용 예시

```java
Member member = Member.builder()
        .nickname("admin")
        .role(Role.ADMIN)
        .build();
```

---

### 6.4 Private Helper Method 사용 예시

테스트 클래스 하단에 위치시키며,
테스트 의도를 드러내는 이름을 사용한다.

```java
private Member createAdminMember() {
    return Member.builder()
            .nickname("admin")
            .role(Role.ADMIN)
            .build();
}
```

---

6.5 금지 사항
- 의미 없는 기본값 Fixture ❌
- 여러 테스트 클래스에서 공유되는 Fixture ❌
- 실제 운영 데이터와 유사한 대용량 Fixture ❌

---

## 7. 금지 사항 요약

아래 항목은 **테스트 컨벤션 위반**으로 간주하며, Codex 자동화에서도 허용하지 않는다.

- 하나의 테스트에서 여러 계층을 동시에 검증 ❌
- Mock 과 실제 DB 접근을 혼용한 테스트 ❌
- Assertion 없이 실행만 하는 테스트 ❌
- 로그 출력으로 결과를 판단하는 테스트 ❌
- 조건문(`if`)을 사용한 검증 로직 ❌

---

## 8. 핵심 요약

- 테스트는 **설계 문서의 일부**이며, 구현의 부수물이 아니다.
- 빠른 작성보다 **의도와 책임이 명확한 테스트**를 우선한다.
- 테스트는 반드시 **계층별 책임을 분리**하여 작성한다.
- Codex는 본 문서를 **테스트 코드 작성 규칙으로 학습**해야 한다.
- 규칙을 충족하지 못할 경우 **자동 구현을 중단**한다.
