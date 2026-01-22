# COMMIT_CONVENTION.md

## 1. Commit Message

### 기본 형식

```text
type : subject

body (optional)
```

- type과 subject는 한 줄
- body는 필요한 경우에만 작성
- body는 subject로부터 두 줄 아래에 작성

---

### 1.1 Commit Type 규칙
아래 type만 사용을 허용한다.

```text
feat     : 새로운 기능 추가
fix      : 수정 (버그, 타입 변경, 로직 수정 등)
hotfix   : 긴급 수정
refactor : 프로덕션 코드 리팩토링 (기능 변경 없음)
style    : 코드 의미에 영향을 주지 않는 변경
           (포맷, 세미콜론, 네이밍 정리 등)
docs     : 문서 추가, 수정, 삭제
test     : 테스트 추가, 수정, 삭제
chore    : 기타 변경사항
           (빌드 설정, 패키지 매니저, 설정 파일 등)
```

규칙:
- type은 소문자 사용
- 하나의 커밋에는 하나의 type만 사용
- 의미 없는 다중 변경 커밋 ❌

--- 

### 1.2 Subject 규칙
- 작업 내용을 간결하고 명확하게 작성
- 마침표(.) 사용 ❌
- 명령형 사용 권장

예시:
```text
feat: 아티클 목록 조회 기능 추가
fix: 관리자 권한 검증 로직 수정
```

---

### 1.3 Body 작성 규칙 (Optional)

다음 경우에만 작성한다.
- 변경 이유 설명이 필요한 경우
- 설계 결정 배경을 남겨야 하는 경우
- 테스트 생략 사유를 명시해야 하는 경우

규칙:
- subject와 빈 줄 2줄 간격
- 변경 이유 중심으로 작성
- 코드 설명 나열 ❌

---

## 2. Pull Request Convention

### 2.1 PR 제목 규칙

형식:
```text
[{issue_key}] {type}:{subject}
```

구성 요소:
- issue_key: Jira에서 발급받은 티켓 키
- type: commit type
- subject: 작업 내용 요약

예시:
```text
[BOM-5] feat: 아티클 목록 조회 기능 추가
```

---

### 2.2 PR 작성 원칙
- PR 제목만 보고 변경 목적이 파악되어야 한다
- commit message와 의미가 일관되어야 한다
- 테스트 생략 시 PR 본문에 사유 명시 필수

---

## 3. Codex 자동화 적용 규칙
- Codex는 본 문서를 커밋 / PR 생성 규칙으로 학습해야 한다
- 규칙을 위반하는 커밋 메시지 자동 생성 ❌
- type 판단이 불가능한 경우 작업 중단 후 사용자 질문

---

### 4. 핵심 요약
- 커밋은 변경 이력의 최소 단위
- PR은 리뷰 단위
- 규칙을 지키지 않는 자동화는 허용하지 않는다
