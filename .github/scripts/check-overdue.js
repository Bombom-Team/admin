'use strict';

const fs = require('fs');
const path = require('path');
const { sbFetch } = require('./supabase');

/**
 * 기한 초과 배정 목록을 discord-notify 액션 입력(mentions, fields)으로 변환.
 * (로컬 단위 테스트용 순수 함수)
 */
function buildNotification(assignments, notifyIds, now = new Date()) {
  const mentions = [
    ...new Set(
      assignments.map((assignment) => {
        const username = assignment.reviewer.github_username;
        const discordId = notifyIds[username];
        return discordId ? `<@${discordId}>` : username;
      }),
    ),
  ].join(' ');

  const fields = assignments.map((assignment) => {
    const overdueHours = Math.floor(
      (now.getTime() - new Date(assignment.deadline_at).getTime()) / (60 * 60 * 1000),
    );
    return {
      name: `#${assignment.pr_number} ${assignment.pr_title}`,
      value: `담당: ${assignment.reviewer.display_name} · ${overdueHours}시간 초과`,
    };
  });

  return { mentions, fields };
}

function loadNotifyIds() {
  const filePath = path.join(__dirname, '..', 'notify_ids.json');
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

async function run({ core }) {
  const nowIso = new Date().toISOString();
  const assignments = await sbFetch(
    `review_assignment?status=eq.OPEN&deadline_at=lt.${nowIso}&overdue_notified_at=is.null` +
      `&select=*,reviewer(github_username,display_name)`,
  );

  if (assignments.length === 0) {
    core.notice('기한 초과 배정이 없습니다');
    core.setOutput('has_overdue', 'false');
    return;
  }

  const { mentions, fields } = buildNotification(assignments, loadNotifyIds());
  core.setOutput('has_overdue', 'true');
  core.setOutput('mentions', mentions);
  core.setOutput('fields', JSON.stringify(fields));
  core.setOutput('ids', assignments.map((assignment) => assignment.id).join(','));
  core.notice(`기한 초과 배정 ${assignments.length}건 발견`);
}

/** Discord 발송 성공 후에만 호출 — 중복 알림 방지 마커 기록 */
async function markNotified({ core }, ids) {
  await sbFetch(`review_assignment?id=in.(${ids})`, {
    method: 'PATCH',
    body: { overdue_notified_at: new Date().toISOString() },
  });
  core.notice(`overdue_notified_at 마킹 완료: ${ids}`);
}

module.exports = { buildNotification, run, markNotified };
