-- 자동 리뷰어 배정 시스템 Supabase 스키마
-- 이 파일은 Supabase 콘솔에서 관리되는 테이블의 근거 문서입니다.
-- 워크플로우(.github/workflows/assign-reviewer.yml 등)가 이 스키마에 의존합니다.

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
  id                  BIGSERIAL PRIMARY KEY,
  reviewer_id         BIGINT NOT NULL REFERENCES reviewer(id),
  pr_number           INTEGER NOT NULL,
  pr_title            TEXT NOT NULL,
  pr_author           TEXT NOT NULL,           -- GitHub username
  pr_url              TEXT NOT NULL,
  assigned_at         TIMESTAMPTZ DEFAULT NOW(),
  status              TEXT NOT NULL DEFAULT 'OPEN',  -- 'OPEN' | 'CLOSED'
  deadline_at         TIMESTAMPTZ NOT NULL,    -- 리뷰 기한 (배정 시각 + 24시간)
  completed_at        TIMESTAMPTZ,             -- 리뷰 제출 또는 PR close 시각
  overdue_notified_at TIMESTAMPTZ              -- 기한 초과 Discord 알림 발송 마커 (중복 방지)
);

-- ============================================================
-- 기존 테이블에 기한 관련 컬럼을 추가하는 마이그레이션
-- (테이블이 이미 위 컬럼 없이 존재하는 경우에만 실행)
-- ============================================================
-- ALTER TABLE review_assignment
--   ADD COLUMN deadline_at timestamptz,
--   ADD COLUMN completed_at timestamptz,
--   ADD COLUMN overdue_notified_at timestamptz;
--
-- UPDATE review_assignment
--   SET deadline_at = assigned_at + interval '24 hours'
--   WHERE deadline_at IS NULL;
--
-- ALTER TABLE review_assignment ALTER COLUMN deadline_at SET NOT NULL;
