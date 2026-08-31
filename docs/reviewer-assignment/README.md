# 자동 리뷰어 배정 시스템

`woowacourse-teams/2025-bom-bom`의 백엔드 PR(base: `server-dev`, `email-server-dev`)이 열리면
**2025-bom-bom 레포의 GitHub Actions가 즉시** 리뷰어 1명을 자동 배정하고,
**이 레포(Bombom-Team/admin)는 리뷰 기한 초과 알림(cron)만** 담당합니다.

```
[2025-bom-bom 워크플로우]  ← 자동 발급 GITHUB_TOKEN (PAT 불필요)
  · PR 열림 → 즉시 배정 + Discord 알림
  · 리뷰 제출 / PR 닫힘 → 배정 완료 처리
[이 레포 워크플로우]        ← GitHub API 접근 없음
  · 매시간 기한 초과 배정 확인 → Discord 알림
[Supabase + admin 대시보드]
  · 리뷰어 명단/순번/휴가/기한 설정/배정 이력 저장 및 운영
```

## 구성 요소

| 파일 | 역할 |
|---|---|
| (이 레포) `.github/workflows/review-deadline-check.yml` | 매시간 cron: 기한 초과 배정 Discord 알림 |
| (이 레포) `.github/scripts/check-overdue.js` | 기한 초과 조회 + 알림 메시지 조립 |
| (이 레포) `.github/notify_ids.json` | GitHub username → Discord user ID 매핑 |
| (이 레포) `.github/actions/discord-notify/` | Discord 웹훅 전송 composite action |
| (2025-bom-bom) `.github/workflows/assign-reviewer.yml` | PR 열림 즉시 round-robin 배정 + Discord 알림 |
| (2025-bom-bom) `.github/workflows/review-complete.yml` | 리뷰 제출/PR close 시 배정 완료 처리 |
| `docs/reviewer-assignment/schema.sql` | Supabase 테이블 스키마 (콘솔 관리, 근거 문서) |

## 배정 규칙 (2025-bom-bom 쪽 워크플로우)

- 대상: `server-dev`, `email-server-dev`로 열린 PR (draft 제외)
- 제외: `No Review` 라벨(설정 가능) / 이미 리뷰어 지정됨 / 기존 OPEN 배정 존재
- 선택: 휴가 아님 + 작성자 제외 중 `last_assigned_at`이 가장 오래된 사람(NULL 최우선, 동률이면 `rotation_order`) → round-robin
- 기한: `review_setting.deadline_hours` (기본 96h = 4일, admin 대시보드에서 변경)
- 전원 휴가 등 후보 0명이면 PR에 수동 지정 요청 코멘트

## 필요한 Secrets

**2025-bom-bom** (배정/완료 처리용): `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `DISCORD_REVIEW_WEBHOOK_URL`

**이 레포** (기한 체크용): `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `DISCORD_REVIEW_WEBHOOK_URL`

GitHub 토큰(PAT)은 어느 쪽에도 필요 없습니다 — 리뷰어 지정은 2025-bom-bom 자기 레포 안에서 자동 발급 `GITHUB_TOKEN`으로 수행됩니다.

## 운영 가이드

- **휴가 설정 / 리뷰어 추가·삭제·이름 변경 / 기한·제외 라벨 변경**: admin 대시보드(`/reviewers`)
- **특정 PR 자동 배정 제외**: PR에 제외 라벨(기본 `No Review`)을 붙이고 열기
- **수동 운영과의 공존**: 이미 리뷰어가 지정됐거나 배정 기록이 있는 PR에는 개입하지 않음
- **기한 초과 알림은 배정당 1회만** (`overdue_notified_at` 마커)

## 주의사항

- 이 레포의 cron은 **default 브랜치(main)의 워크플로우 기준**으로 동작합니다. server 머지 후 main 반영 필요
- 2025-bom-bom의 `email-server-dev` 대상 배정은 해당 브랜치에 워크플로우가 전파된 후부터 동작합니다
- 리뷰어 명단 변경 시 Discord 멘션을 받으려면 **양쪽 레포의 `notify_ids.json`** 모두 갱신 필요
