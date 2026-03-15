import type {
  Execution,
  ExecutionPage,
  ManualExecutionRequest,
  Schedule,
  SchedulePage,
  ScheduleRequest,
  Task,
} from "./types";

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function fetchTasks(): Promise<Task[]> {
  return request("/api/tasks");
}

export function fetchSchedules(page = 1, size = 20): Promise<SchedulePage> {
  return request(`/api/schedules?page=${page}&size=${size}`);
}

export function fetchSchedule(id: string): Promise<Schedule> {
  return request(`/api/schedules/${id}`);
}

export function createSchedule(payload: ScheduleRequest): Promise<Schedule> {
  return request("/api/schedules", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateSchedule(id: string, payload: ScheduleRequest): Promise<Schedule> {
  return request(`/api/schedules/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function enableSchedule(id: string): Promise<Schedule> {
  return request(`/api/schedules/${id}/enable`, { method: "PATCH" });
}

export function disableSchedule(id: string): Promise<Schedule> {
  return request(`/api/schedules/${id}/disable`, { method: "PATCH" });
}

export function deleteSchedule(id: string): Promise<void> {
  return request(`/api/schedules/${id}`, { method: "DELETE" });
}

export function manualExecute(payload: ManualExecutionRequest): Promise<Execution> {
  return request("/api/executions/manual", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function fetchExecutions(cursor?: string | null, limit = 50): Promise<ExecutionPage> {
  const params = new URLSearchParams();
  params.set("limit", String(limit));
  if (cursor) {
    params.set("cursor", cursor);
  }
  return request(`/api/executions?${params.toString()}`);
}
