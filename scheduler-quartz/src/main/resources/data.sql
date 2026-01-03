INSERT INTO tasks (task_id, name, description) VALUES
(
  'sampleCleanup',
  'Sample Cleanup',
  'Example task that simulates cleanup work.'
),
(
  'sampleEmail',
  'Sample Email',
  'Example task that simulates sending email.'
)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description);

INSERT INTO schedules (
    id, name, schedule_type, cron_expression, run_at, enabled, task_id, payload,
    next_run_at, locked_by, locked_until, created_at, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Sample Cleanup Cron',
    'CRON',
    '0/30 * * * * ?',
    NULL,
    TRUE,
    'sampleCleanup',
    '{"source":"data.sql","type":"cron"}',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 SECOND),
    NULL,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
),
(
    '22222222-2222-2222-2222-222222222222',
    'Sample Email Once',
    'ONCE',
    NULL,
    CURRENT_TIMESTAMP(6),
    TRUE,
    'sampleEmail',
    '{"source":"data.sql","type":"once"}',
    DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 SECOND),
    NULL,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    schedule_type = VALUES(schedule_type),
    cron_expression = VALUES(cron_expression),
    run_at = VALUES(run_at),
    enabled = VALUES(enabled),
    task_id = VALUES(task_id),
    payload = VALUES(payload),
    next_run_at = DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 SECOND),
    locked_by = NULL,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP(6);
