export type ScheduleType = "CRON" | "ONCE";
export type ExecutionStatus = "PENDING" | "RUNNING" | "SUCCESS" | "FAILED";
export type ExecutionType = "SCHEDULE" | "MANUAL";

export type Task = {
  taskId: string;
  name: string;
  description: string;
};

export type Schedule = {
  id: string;
  name: string;
  scheduleType: ScheduleType;
  cronExpression: string | null;
  runAt: string | null;
  enabled: boolean;
  actionKey: string;
  payload: string | null;
  nextRunAt: string | null;
  updatedAt: string;
};

export type Execution = {
  executionId: string;
  scheduleId: string | null;
  taskId: string;
  executionType: ExecutionType;
  status: ExecutionStatus;
  payload: string | null;
  attemptCount: number;
  createdAt: string;
  updatedAt: string;
  startedAt: string | null;
  completedAt: string | null;
};

export type SchedulePage = {
  items: Schedule[];
  total: number;
  limit: number;
  offset: number;
};

export type ExecutionPage = {
  items: Execution[];
  nextCursor: string | null;
};

export type ScheduleRequest = {
  name: string;
  scheduleType: ScheduleType;
  cronExpression: string | null;
  runAt: string | null;
  enabled: boolean;
  actionKey: string;
  payload: string | null;
};

export type ManualExecutionRequest = {
  actionKey: string;
  payload: string | null;
};
