# 자동 리뷰어 배정 시스템

PR이 올라오면 GitHub Actions가 Supabase의 리뷰어 로테이션 정보를 조회해 리뷰어 1명을 자동 배정하고,
리뷰 기한(24시간) 초과 시 Discord로 알림을 보냅니다. 서버(Spring) 코드와 무관하게 동작합니다.

## 구성 요소

| 파일 | 역할 |
|---|---|
| `.github/workflows/assign-reviewer.yml` | PR open/reopen/ready_for_review 시 리뷰어 자동 배정 |
| `.github/workflows/review-complete.yml` | 리뷰 제출 / PR close 시 배정 완료 처리 |
| `.github/workflows/review-deadline-check.yml` | 매시간 기한 초과 배정 확인 후 Discord 알림 |
| `.github/scripts/*.js` | 워크플로우 로직 (순수 함수 분리, `node --test`로 검증 가능) |
| `.github/notify_ids.json` | GitHub username → Discord user ID 매핑 |
| `.github/actions/discord-notify/` | Discord 웹훅 전송 composite action |
| `docs/reviewer-assignment/schema.sql` | Supabase 테이블 스키마 (콘솔 관리, 근거 문서) |

## 배정 알고리즘

1. 같은 PR 번호의 `OPEN` 배정이 이미 있으면 스킵 (reopen 중복 방지)
2. 후보 = 휴가 아님(`is_on_vacation = false`) + PR 작성자 제외
3. `last_assigned_at`이 가장 오래된 사람(NULL 최우선), 동률이면 `rotation_order` 낮은 순 → round-robin
4. GitHub API로 리뷰어 지정 → 성공 시 `review_assignment` INSERT(기한 = 배정 + 24h) + `last_assigned_at` 갱신
5. 후보가 0명(전원 휴가 등)이면 PR에 수동 지정 요청 코멘트만 남기고 종료

## 필요한 Secrets

| Secret | 값 |
|---|---|
| `SUPABASE_URL` | Supabase 프로젝트 URL |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase service_role key (쓰기 필요, anon 아님) |
| `DISCORD_REVIEW_WEBHOOK_URL` | 리뷰 알림 Discord 채널 웹훅 |

등록: `gh secret set <NAME> -R Bombom-Team/admin`

## 운영 가이드

- **휴가 설정**: admin 대시보드(`/reviewers`)에서 토글. 휴가 중에는 배정되지 않음.
- **리뷰어 추가/제거**: Supabase 콘솔에서 `reviewer` 테이블 직접 수정. 추가 시 `rotation_order`는 기존 최대값 + 1.
  Discord 알림을 받으려면 `.github/notify_ids.json`에도 매핑 추가.
- **GitHub에서 리뷰어를 수동 변경한 경우**: 배정 이력과 동기화되지 않음(의도된 한계).
  필요하면 Supabase 콘솔에서 `review_assignment`를 수동 수정. PR이 close되면 OPEN 배정은 자동으로 CLOSED 처리되므로 방치해도 orphan은 남지 않음.
- **기한 초과 알림은 배정당 1회만** 발송됨 (`overdue_notified_at` 마커).

## 주의사항

- `review-deadline-check.yml`의 cron은 **default 브랜치(main)에 있는 워크플로우만 실행**됩니다.
  `server` 머지 후 main에도 반영해야 기한 체크가 동작합니다.
- 동시에 PR이 2개 열리면 같은 리뷰어에게 몰릴 수 있는 이론적 race가 있으나 허용 범위로 판단 (락 없음).
