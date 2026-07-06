'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const { buildNotification } = require('./check-overdue');

test('buildNotification: 멘션과 초과 시간 필드를 생성한다', () => {
  const now = new Date('2026-07-03T12:00:00Z');
  const assignments = [
    {
      id: 1,
      pr_number: 10,
      pr_title: '기능 추가',
      deadline_at: '2026-07-03T09:00:00Z',
      reviewer: { github_username: 'a', display_name: '유저A' },
    },
  ];
  const { mentions, fields } = buildNotification(assignments, { a: '111' }, now);
  assert.equal(mentions, '<@111>');
  assert.deepEqual(fields, [
    { name: '#10 기능 추가', value: '담당: 유저A · 3시간 초과' },
  ]);
});

test('buildNotification: 매핑 없는 유저는 username으로, 중복 멘션은 제거한다', () => {
  const now = new Date('2026-07-03T12:00:00Z');
  const assignments = [
    {
      id: 1,
      pr_number: 10,
      pr_title: 'A',
      deadline_at: '2026-07-03T11:00:00Z',
      reviewer: { github_username: 'x', display_name: 'X' },
    },
    {
      id: 2,
      pr_number: 11,
      pr_title: 'B',
      deadline_at: '2026-07-03T10:00:00Z',
      reviewer: { github_username: 'x', display_name: 'X' },
    },
  ];
  const { mentions, fields } = buildNotification(assignments, {}, now);
  assert.equal(mentions, 'x');
  assert.equal(fields.length, 2);
});
