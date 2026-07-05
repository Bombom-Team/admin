'use strict';

const fs = require('fs');
const path = require('path');
const { sbFetch } = require('./supabase');

const DEADLINE_HOURS = 24;

/**
 * round-robin 후보 선정: 휴가 아님 + PR 작성자 제외 중
 * last_assigned_at이 가장 오래된 사람(null 최우선), 동률이면 rotation_order 낮은 순.
 * (Supabase 쿼리와 동일한 규칙 — 로컬 단위 테스트용 순수 함수)
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

async function run({ github, context, core }) {
  const pr = context.payload.pull_request;
  const prAuthor = pr.user.login;

  const existing = await sbFetch(
    `review_assignment?pr_number=eq.${pr.number}&status=eq.OPEN&select=id`,
  );
  if (existing.length > 0) {
    core.notice(`PR #${pr.number}에 이미 OPEN 배정이 있어 스킵합니다`);
    return;
  }

  const candidates = await sbFetch(
    `reviewer?is_on_vacation=eq.false&github_username=neq.${encodeURIComponent(prAuthor)}` +
      `&order=last_assigned_at.asc.nullsfirst,rotation_order.asc&limit=1&select=*`,
  );
  if (candidates.length === 0) {
    core.warning('자동 배정 가능한 리뷰어가 없습니다 (전원 휴가 등)');
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: pr.number,
      body: '🙋 자동 배정 가능한 리뷰어가 없습니다. 리뷰어를 수동으로 지정해주세요.',
    });
    return;
  }
  const reviewer = candidates[0];

  // GitHub API 지정을 DB 기록보다 먼저 수행 — 실패 시 유령 배정 방지
  await github.rest.pulls.requestReviewers({
    owner: context.repo.owner,
    repo: context.repo.repo,
    pull_number: pr.number,
    reviewers: [reviewer.github_username],
  });

  const now = new Date();
  const deadline = new Date(now.getTime() + DEADLINE_HOURS * 60 * 60 * 1000);

  await sbFetch('review_assignment', {
    method: 'POST',
    body: {
      reviewer_id: reviewer.id,
      pr_number: pr.number,
      pr_title: pr.title,
      pr_author: prAuthor,
      pr_url: pr.html_url,
      assigned_at: now.toISOString(),
      deadline_at: deadline.toISOString(),
      status: 'OPEN',
    },
  });

  await sbFetch(`reviewer?id=eq.${reviewer.id}`, {
    method: 'PATCH',
    body: { last_assigned_at: now.toISOString(), updated_at: now.toISOString() },
  });

  const notifyIds = loadNotifyIds();
  const discordId = notifyIds[reviewer.github_username];
  core.setOutput('assigned', 'true');
  core.setOutput('reviewer_name', reviewer.display_name);
  core.setOutput('reviewer_mention', discordId ? `<@${discordId}>` : reviewer.github_username);
  core.notice(`PR #${pr.number} 리뷰어 배정: ${reviewer.github_username}`);
}

module.exports = { pickCandidate, run, DEADLINE_HOURS };
