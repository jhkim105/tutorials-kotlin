CREATE TABLE IF NOT EXISTS schedules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    schedule_type VARCHAR(10) NOT NULL,
    cron_expression VARCHAR(120),
    run_at DATETIME(6),
    enabled BOOLEAN NOT NULL,
    task_id VARCHAR(100) NOT NULL,
    payload TEXT,
    next_run_at DATETIME(6),
    locked_by VARCHAR(64),
    locked_until DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_schedules_next_run ON schedules (next_run_at);
CREATE INDEX IF NOT EXISTS idx_schedules_task ON schedules (task_id);

CREATE TABLE IF NOT EXISTS tasks (
    task_id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS executions (
    execution_id VARCHAR(36) PRIMARY KEY,
    schedule_id VARCHAR(36),
    task_id VARCHAR(100) NOT NULL,
    execution_type VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL,
    payload TEXT,
    attempt_count INT NOT NULL,
    locked_by VARCHAR(64),
    locked_until DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    started_at DATETIME(6),
    completed_at DATETIME(6)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_executions_execution_id ON executions (execution_id);
CREATE INDEX IF NOT EXISTS idx_executions_status ON executions (status);
CREATE INDEX IF NOT EXISTS idx_executions_task ON executions (task_id);
