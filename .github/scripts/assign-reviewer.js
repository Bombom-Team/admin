'use strict';

const fs = require('fs');
const path = require('path');
const { sbFetch } = require('./supabase');

const DEADLINE_HOURS = 24;

/**
 * round-robin 후보 선정: 휴가 아님 + PR 작성자 제외 중
 * last_assigned_at이 가장 오래된 사람(null 최우선), 동률이면 rotation_order 낮은 순.
 */
function pickCandidate(reviewers, prAuthor) {
  const candidates = reviewers.filter(
    (reviewer) => !reviewer.is_on_vacation && reviewer.github_username !== prAuthor,
  );
  if (candidates.length === 0) {
    return null;
  }

  return [...candidates].sort((a, b) => {
    if (a.last_assigned_at === null && b.last_assigned_at !== null) return -1;
    if (a.last_assigned_at !== null && b.last_assigned_at === null) return 1;
    if (a.last_assigned_at !== b.last_assigned_at) {
      return a.last_assigned_at < b.last_assigned_at ? -1 : 1;
    }
    return a.rotation_order - b.rotation_order;
  })[0];
}

function loadNotifyIds() {
  const filePath = path.join(__dirname, '..', 'notify_ids.json');
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

/**
 * TARGET_REPO(2025-bom-bom)의 열린 PR을 폴링해서 미배정 PR에 리뷰어를 배정한다.
 * - 이미 OPEN 배정 기록이 있는 PR 스킵
 * - 수동으로 리뷰어가 지정됐거나 이미 리뷰가 달린 PR 스킵 (수동 운영 존중)
 */
async function run({ github, core }) {
  const [owner, repo] = process.env.TARGET_REPO.split('/');
  const baseBranches = process.env.TARGET_BASE_BRANCHES.split(',').map((s) =>
    s.trim(),
  );

  const reviewers = await sbFetch('reviewer?select=*');
  const openAssignments = await sbFetch(
    'review_assignment?status=eq.OPEN&select=pr_number',
  );
  const assignedPrNumbers = new Set(openAssignments.map((a) => a.pr_number));

  const prs = await github.paginate(github.rest.pulls.list, {
    owner,
    repo,
    state: 'open',
    per_page: 100,
  });

  const notifyIds = loadNotifyIds();
  const assigned = [];

  for (const pr of prs) {
    if (pr.draft) continue;
    if (!baseBranches.includes(pr.base.ref)) continue;
    if (assignedPrNumbers.has(pr.number)) continue;
    if (pr.requested_reviewers && pr.requested_reviewers.length > 0) continue;

    const { data: reviews } = await github.rest.pulls.listReviews({
      owner,
      repo,
      pull_number: pr.number,
    });
    if (reviews.length > 0) continue;

    const candidate = pickCandidate(reviewers, pr.user.login);
    if (!candidate) {
      core.warning(`PR #${pr.number}: 배정 가능한 리뷰어가 없습니다 (전원 휴가 등)`);
      continue;
    }

    // GitHub API 지정을 DB 기록보다 먼저 수행 — 실패 시 유령 배정 방지
    await github.rest.pulls.requestReviewers({
      owner,
      repo,
      pull_number: pr.number,
      reviewers: [candidate.github_username],
    });

    const now = new Date();
    const deadline = new Date(now.getTime() + DEADLINE_HOURS * 60 * 60 * 1000);

    await sbFetch('review_assignment', {
      method: 'POST',
      body: {
        reviewer_id: candidate.id,
        pr_number: pr.number,
        pr_title: pr.title,
        pr_author: pr.user.login,
        pr_url: pr.html_url,
        assigned_at: now.toISOString(),
        deadline_at: deadline.toISOString(),
        status: 'OPEN',
      },
    });
    await sbFetch(`reviewer?id=eq.${candidate.id}`, {
      method: 'PATCH',
      body: { last_assigned_at: now.toISOString(), updated_at: now.toISOString() },
    });

    // 같은 실행 안에서 여러 PR을 배정할 때도 로테이션이 이어지도록 로컬 상태 갱신
    candidate.last_assigned_at = now.toISOString();

    assigned.push({
      prNumber: pr.number,
      title: pr.title,
      reviewer: candidate,
    });
    core.notice(`PR #${pr.number} 리뷰어 배정: ${candidate.github_username}`);
  }

  if (assigned.length === 0) {
    core.setOutput('assigned', 'false');
    core.notice('새로 배정할 PR이 없습니다');
    return;
  }

  const mentions = [
    ...new Set(
      assigned.map((a) => {
        const discordId = notifyIds[a.reviewer.github_username];
        return discordId ? `<@${discordId}>` : a.reviewer.github_username;
      }),
    ),
  ].join(' ');
  const fields = assigned.map((a) => ({
    name: `#${a.prNumber} ${a.title}`,
    value: `리뷰어: ${a.reviewer.display_name} · 기한 24시간`,
  }));

  core.setOutput('assigned', 'true');
  core.setOutput('mentions', mentions);
  core.setOutput('fields', JSON.stringify(fields));
}

module.exports = { pickCandidate, run, DEADLINE_HOURS };
