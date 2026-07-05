# 자동 리뷰어 배정 시스템

`woowacourse-teams/2025-bom-bom` 레포의 백엔드 PR(base: `server-dev`, `email-server-dev`)을
**이 레포(Bombom-Team/admin)의 GitHub Actions가 10분 간격으로 폴링**해서 리뷰어 1명을 자동 배정하고,
리뷰 기한(24시간) 초과 시 Discord로 알림을 보냅니다.

감시 대상 레포(2025-bom-bom)에는 아무 파일도 추가하지 않습니다 — 모든 동작이 이 레포에서 실행됩니다.

## 구성 요소

| 파일 | 역할 |
|---|---|
| `.github/workflows/reviewer-rotation.yml` | 10분 간격 cron: 완료 동기화 → 신규 배정 → 기한 초과 알림 |
| `.github/scripts/assign-reviewer.js` | 미배정 PR 탐색 + round-robin 배정 |
| `.github/scripts/sync-completed.js` | 리뷰 제출/PR close 감지해 배정 완료 처리 |
| `.github/scripts/check-overdue.js` | 기한 초과 배정 Discord 알림 |
| `.github/notify_ids.json` | GitHub username → Discord user ID 매핑 |
| `.github/actions/discord-notify/` | Discord 웹훅 전송 composite action |
| `docs/reviewer-assignment/schema.sql` | Supabase 테이블 스키마 (콘솔 관리, 근거 문서) |

## 매 실행(10분)마다 하는 일

1. **완료 동기화**: OPEN 배정들의 PR 상태 확인 — PR이 닫혔거나(머지 포함) 배정된 리뷰어가 리뷰를 제출했으면 `CLOSED + completed_at` 기록
2. **신규 배정**: 대상 브랜치의 열린 PR 중 ① OPEN 배정 기록 없음 ② 수동 지정된 리뷰어 없음 ③ 아직 리뷰가 없음 인 PR에 대해:
   - 휴가 아님 + PR 작성자 제외 중 `last_assigned_at`이 가장 오래된 사람(NULL 최우선, 동률이면 `rotation_order`) 선택 → round-robin
   - GitHub 리뷰어 지정 → `review_assignment` INSERT(기한 = +24h) → `last_assigned_at` 갱신 → Discord 멘션 알림
3. **기한 체크**: OPEN & 기한 초과 & 미알림 배정을 Discord로 멘션 (배정당 1회만)

## 필요한 Secrets

| Secret | 값 |
|---|---|
| `SUPABASE_URL` | Supabase 프로젝트 URL |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase secret key (쓰기 필요, publishable 아님) |
| `DISCORD_REVIEW_WEBHOOK_URL` | 리뷰 알림 Discord 채널 웹훅 |
| `BOMBOM_REPO_TOKEN` | **2025-bom-bom에 쓰기 권한이 있는 PAT** — 리뷰어 지정(requestReviewers)에 필요. Fine-grained라면 `woowacourse-teams/2025-bom-bom`에 Pull requests Read/Write |

등록: `gh secret set <NAME> -R Bombom-Team/admin`

## 운영 가이드

- **휴가 설정**: admin 대시보드(`/reviewers`)에서 토글. 휴가 중에는 배정되지 않음
- **리뷰어 추가/제거**: Supabase 콘솔에서 `reviewer` 테이블 직접 수정 (추가 시 `rotation_order` = 최대값 + 1, `.github/notify_ids.json`에도 Discord ID 추가)
- **수동 운영과의 공존**: 이미 리뷰어가 수동 지정됐거나 리뷰가 달린 PR에는 개입하지 않음
- **수동 실행**: Actions 탭 → Reviewer Rotation → Run workflow (main 반영 후 가능)
- **기한 초과 알림은 배정당 1회만** (`overdue_notified_at` 마커)

## 주의사항

- **cron/workflow_dispatch는 default 브랜치(main)의 워크플로우 기준으로 동작**합니다.
  `server` 머지 후 main에도 반영해야 시스템이 실제로 돌기 시작합니다.
- 배정 지연: cron 특성상 PR 오픈 후 최대 10~15분 안에 배정됩니다 (GitHub cron은 정시 보장이 없음)
- draft PR은 배정하지 않으며, ready_for_review로 전환되면 다음 폴링에서 배정됩니다
