'use strict';

const { sbFetch } = require('./supabase');

/**
 * OPEN 배정들의 PR 상태를 TARGET_REPO에서 확인해 완료 처리한다.
 * - PR이 닫혔으면(머지 포함) 무조건 CLOSED — orphan 방지
 * - 배정된 리뷰어가 배정 이후 리뷰를 제출했으면 CLOSED + completed_at 기록
 */
async function run({ github, core }) {
  const [owner, repo] = process.env.TARGET_REPO.split('/');

  const assignments = await sbFetch(
    'review_assignment?status=eq.OPEN&select=*,reviewer(github_username)',
  );
  if (assignments.length === 0) {
    core.notice('진행 중인 배정이 없습니다');
    return;
  }

  for (const assignment of assignments) {
    const { data: pr } = await github.rest.pulls.get({
      owner,
      repo,
      pull_number: assignment.pr_number,
    });

    if (pr.state === 'closed') {
      await sbFetch(`review_assignment?id=eq.${assignment.id}`, {
        method: 'PATCH',
        body: {
          status: 'CLOSED',
          completed_at: pr.merged_at ?? pr.closed_at ?? new Date().toISOString(),
        },
      });
      core.notice(`PR #${assignment.pr_number} close → 배정 종료`);
      continue;
    }

    const { data: reviews } = await github.rest.pulls.listReviews({
      owner,
      repo,
      pull_number: assignment.pr_number,
    });
    const review = reviews.find(
      (r) =>
        r.user &&
        r.user.login === assignment.reviewer.github_username &&
        new Date(r.submitted_at) >= new Date(assignment.assigned_at),
    );
    if (review) {
      await sbFetch(`review_assignment?id=eq.${assignment.id}`, {
        method: 'PATCH',
        body: { status: 'CLOSED', completed_at: review.submitted_at },
      });
      core.notice(`PR #${assignment.pr_number} 리뷰 제출 → 배정 완료`);
    }
  }
}

module.exports = { run };
