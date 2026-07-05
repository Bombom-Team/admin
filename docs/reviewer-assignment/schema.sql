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

-- 배정 설정 (단일 행 — admin 대시보드에서 수정)
CREATE TABLE review_setting (
  id             BIGINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),  -- 단일 행 강제
  deadline_hours INTEGER NOT NULL DEFAULT 96,          -- 리뷰 기한 (기본 4일)
  exclude_label  TEXT NOT NULL DEFAULT 'No Review',    -- 이 라벨이 붙은 PR은 배정 제외
  updated_at     TIMESTAMPTZ DEFAULT NOW()
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
-- 권한 설정 (RLS + GRANT)
-- 대시보드(anon publishable key): reviewer 관리(추가/수정/삭제) + 설정 수정 + 조회
-- GitHub Actions(service_role secret key): 전체 권한
-- ============================================================
-- ALTER TABLE reviewer ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE review_assignment ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE review_setting ENABLE ROW LEVEL SECURITY;
--
-- CREATE POLICY "anon read reviewer" ON reviewer FOR SELECT TO anon USING (true);
-- CREATE POLICY "anon insert reviewer" ON reviewer FOR INSERT TO anon WITH CHECK (true);
-- CREATE POLICY "anon update reviewer" ON reviewer FOR UPDATE TO anon USING (true) WITH CHECK (true);
-- CREATE POLICY "anon delete reviewer" ON reviewer FOR DELETE TO anon USING (true);
-- CREATE POLICY "anon read assignment" ON review_assignment FOR SELECT TO anon USING (true);
-- CREATE POLICY "anon read setting" ON review_setting FOR SELECT TO anon USING (true);
-- CREATE POLICY "anon update setting" ON review_setting FOR UPDATE TO anon USING (true) WITH CHECK (true);
--
-- GRANT SELECT, INSERT, UPDATE, DELETE ON public.reviewer TO anon;
-- GRANT SELECT ON public.review_assignment TO anon;
-- GRANT SELECT, UPDATE ON public.review_setting TO anon;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon;
-- GRANT ALL ON public.reviewer, public.review_assignment, public.review_setting TO service_role;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO service_role;
--
-- INSERT INTO review_setting (id) VALUES (1);

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
