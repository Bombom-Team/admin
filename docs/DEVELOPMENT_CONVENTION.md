# DEVELOPMENT_CONVENTION.md

## 0. 적용 범위

- 본 문서는 **모든 Codex 자동화 작업의 기준 규칙**이다.
- Codex는 본 문서를 **위반해서는 안 된다**.
- 판단 불가 시 **즉시 중단**한다.

---

## 1. 코드 스타일 규칙 (강제)

### 줄바꿈
- record, DTO: **항상 줄바꿈**
- 메서드 파라미터: **3개 초과 시 줄바꿈**
- 체이닝: **한 줄에 하나의 체이닝만 허용**

### 클래스 / 메서드
- 클래스 선언부 아래 **빈 줄 필수**
- 접근자 순서  
  `public static → public → private static → private`
- 파라미터에 `final` **사용 금지**

### 조건문
- 부정 조건식 사용 금지  
  (예: `!isAvailable` ❌ → `isUnavailable` ⭕)
- 복잡한 조건은 **메서드로 추출**

### 기타
- Early Return 적극 사용
- 축약어 지양
- 숫자 증가 시 `+=` 사용 (`++` 금지)

---

## 2. 객체 생성 규칙

- 기본 생성 방식: **Builder**
- 정적 팩토리 메서드는 **명확한 목적이 있을 때만 허용**

---

## 3. Controller 규칙

- 반환 타입: **DTO**
- `ResponseEntity`는 아래 경우만 허용
    - status code 동적 변경
    - header 추가
- Controller 메서드명 = Service 메서드명
- `@RequestParam` 2개 이상 → `@ModelAttribute`
- Request / Response는 **항상 DTO 사용**

---

## 4. Service 규칙

- Controller DTO를 그대로 전달
- DTO에서 **요청값 검증**
- Service에서 **비즈니스 룰 검증**
- 반환 타입은 DTO

---

## 5. 트랜잭션 규칙

- `@Transactional`은 **클래스 상단에 선언**
- EventListener 사용 시
    - 재사용 가능 Service  
      → Service(`REQUIRED`), Listener(`REQUIRES_NEW`)
    - 재사용 불가 Service  
      → Service(`REQUIRES_NEW`)

---

## 6. 계층 의존성

- Service 간 의존은 **단방향만 허용**
- OSIV: **OFF**

---

## 7. 예외 처리

- 추상화된 커스텀 예외 사용
    - `BadRequestException`
    - `NotFoundException`

---

## 8. 테스트 규칙

- 최소 보장 범위: **Service → Repository**
- 단위 테스트는 **기능 중요도 기반 선택**
- Controller 테스트는 아래 경우만 작성
    - Swagger – Controller 일치 검증
    - Validation 검증
    - Query String 엣지 케이스

---

## 9. 패키지 구조 (고정)

```text
me.bombom
└── api
    └── v1
        └── {domain}
            ├── controller
            ├── service
            ├── repository
            ├── dto
            │   ├── request
            │   └── response
            └── domain
```
---

## 10. DTO 규칙

- 요청 DTO: `*Request`
- 응답 DTO: `*Response`
- 변환 메서드 규칙
    - 파라미터 1개 → `from`
    - 파라미터 2개 이상 → `of`
- Entity → DTO 변환은 **DTO 내부에서 수행**

---

## 11. 메서드 네이밍

| 계층 | 규칙 |
|---|---|
| Repository | JPA CRUD 네이밍 |
| Service | 비즈니스 의미 기반 |
| Controller | Service와 동일 |

---

## 12. JPA / DB 규칙

- 메서드 네이밍 쿼리 우선
- ASC 정렬은 생략
- 테이블명에 **예약어 사용 금지**
