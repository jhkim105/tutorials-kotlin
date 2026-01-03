import { useEffect, useMemo, useState } from "react";
import {
  createSchedule,
  deleteSchedule,
  disableSchedule,
  enableSchedule,
  fetchExecutions,
  fetchSchedules,
  fetchTasks,
  manualExecute,
  updateSchedule,
} from "./api";
import type { Execution, Schedule, ScheduleRequest, Task } from "./types";
import "./styles.css";

type ViewKey = "schedules" | "form" | "manual" | "executions";

const views: { key: ViewKey; label: string }[] = [
  { key: "schedules", label: "Schedules" },
  { key: "form", label: "Create / Edit" },
  { key: "manual", label: "Manual Execution" },
  { key: "executions", label: "Execution History" },
];

const emptyForm: ScheduleRequest = {
  name: "",
  scheduleType: "CRON",
  cronExpression: "",
  runAt: null,
  enabled: true,
  actionKey: "",
  payload: "",
};

function toDatetimeLocal(value: string | null) {
  if (!value) return "";
  const date = new Date(value);
  const offset = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offset * 60 * 1000);
  return local.toISOString().slice(0, 16);
}

function toIso(value: string) {
  if (!value) return null;
  return new Date(value).toISOString();
}

export default function App() {
  const [view, setView] = useState<ViewKey>("schedules");
  const [tasks, setTasks] = useState<Task[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [executions, setExecutions] = useState<Execution[]>([]);
  const [scheduleTotal, setScheduleTotal] = useState(0);
  const [scheduleLimit, setScheduleLimit] = useState(20);
  const [schedulePage, setSchedulePage] = useState(1);
  const [executionCursor, setExecutionCursor] = useState<string | null>(null);
  const [executionHasMore, setExecutionHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<ScheduleRequest>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [manualPayload, setManualPayload] = useState("{}");
  const [manualTask, setManualTask] = useState("");
  const [manualResult, setManualResult] = useState<Execution | null>(null);

  const taskOptions = useMemo(() => tasks, [tasks]);

  const refreshAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [taskList, schedulePage, executionPage] = await Promise.all([
        fetchTasks(),
        fetchSchedules(1, 20),
        fetchExecutions(null, 50),
      ]);
      setTasks(taskList);
      setSchedules(schedulePage.items);
      setScheduleTotal(schedulePage.total);
      setScheduleLimit(schedulePage.limit);
      setSchedulePage(1);
      setExecutions(executionPage.items);
      setExecutionCursor(executionPage.nextCursor);
      setExecutionHasMore(Boolean(executionPage.nextCursor));
      if (!manualTask && taskList.length > 0) {
        setManualTask(taskList[0].taskId);
      }
      if (!form.actionKey && taskList.length > 0) {
        setForm((prev) => ({ ...prev, actionKey: taskList[0].taskId }));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAll();
  }, []);

  const handleSubmit = async () => {
    setError(null);
    const payload = form.payload?.trim() ? form.payload.trim() : null;
    const request: ScheduleRequest = {
      ...form,
      cronExpression: form.scheduleType === "CRON" ? form.cronExpression?.trim() || null : null,
      runAt: form.scheduleType === "ONCE" ? toIso(form.runAt ?? "") : null,
      payload,
    };

    if (!request.name || !request.actionKey) {
      setError("Name and actionKey are required.");
      return;
    }

    if (request.scheduleType === "CRON" && !request.cronExpression) {
      setError("cronExpression is required for CRON schedule.");
      return;
    }

    if (request.scheduleType === "ONCE" && !request.runAt) {
      setError("runAt is required for ONCE schedule.");
      return;
    }

    setLoading(true);
    try {
      if (editingId) {
        await updateSchedule(editingId, request);
      } else {
        await createSchedule(request);
      }
      await refreshAll();
      setForm(emptyForm);
      setEditingId(null);
      setView("schedules");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save schedule");
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (schedule: Schedule) => {
    setEditingId(schedule.id);
    setForm({
      name: schedule.name,
      scheduleType: schedule.scheduleType,
      cronExpression: schedule.cronExpression ?? "",
      runAt: toDatetimeLocal(schedule.runAt),
      enabled: schedule.enabled,
      actionKey: schedule.actionKey,
      payload: schedule.payload ?? "",
    });
    setView("form");
  };

  const handleManual = async () => {
    if (!manualTask) {
      setError("Select a task to execute.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const payload = manualPayload.trim() ? manualPayload.trim() : null;
      const result = await manualExecute({ actionKey: manualTask, payload });
      setManualResult(result);
      setExecutions((prev) => [result, ...prev]);
      setExecutionHasMore(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Manual execution failed");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNew = () => {
    setEditingId(null);
    setForm(emptyForm);
    setView("form");
  };

  const totalSchedulePages =
    scheduleTotal === 0 ? 1 : Math.max(1, Math.ceil(scheduleTotal / scheduleLimit));

  const loadSchedulePage = async (page: number) => {
    const safePage = Math.max(1, Math.min(page, totalSchedulePages));
    setLoading(true);
    setError(null);
    try {
      const pageData = await fetchSchedules(safePage, scheduleLimit);
      setSchedules(pageData.items);
      setScheduleTotal(pageData.total);
      setScheduleLimit(pageData.limit);
      setSchedulePage(safePage);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load schedules");
    } finally {
      setLoading(false);
    }
  };

  const loadMoreExecutions = async () => {
    if (!executionCursor) return;
    setLoading(true);
    setError(null);
    try {
      const page = await fetchExecutions(executionCursor, 50);
      setExecutions((prev) => [...prev, ...page.items]);
      setExecutionCursor(page.nextCursor);
      setExecutionHasMore(Boolean(page.nextCursor));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load executions");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <header>
        <div className="title">
          <span className="badge">Control Room</span>
          <h1>Dynamic Scheduler</h1>
        </div>
        <div className="subtitle">
          Orchestrate schedules, run tasks on demand, and audit executions in one place.
        </div>
        <nav>
          {views.map((item) => (
            <button
              key={item.key}
              type="button"
              className={`nav-pill ${view === item.key ? "active" : ""}`}
              onClick={() => setView(item.key)}
            >
              {item.label}
            </button>
          ))}
          <button type="button" className="nav-pill" onClick={refreshAll}>
            Refresh
          </button>
        </nav>
        {error && <div className="notice">Error: {error}</div>}
      </header>

      {view === "schedules" && (
        <main>
          <section className="panel" style={{ gridColumn: "1 / -1" }}>
            <div className="card-row">
              <h2>Schedules</h2>
                <div className="notice">
                  {loading ? "Loading schedules..." : `${schedules.length} schedules loaded.`}
                </div>
              <div className="actions">
                <button onClick={handleCreateNew}>Create Schedule</button>
              </div>
            </div>
            <div style={{ overflowX: "auto" }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Timing</th>
                    <th>Enabled</th>
                    <th>Action</th>
                    <th>Updated</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {schedules.map((schedule) => (
                    <tr key={schedule.id}>
                      <td><code>{schedule.id}</code></td>
                      <td>{schedule.name}</td>
                      <td>{schedule.scheduleType}</td>
                      <td>
                        {schedule.scheduleType === "CRON" ? schedule.cronExpression : schedule.runAt}
                      </td>
                      <td>{schedule.enabled ? "ON" : "OFF"}</td>
                      <td>{schedule.actionKey}</td>
                      <td>{new Date(schedule.updatedAt).toLocaleString()}</td>
                      <td>
                        <div className="actions">
                          <button className="secondary" onClick={() => handleEdit(schedule)}>
                            Edit
                          </button>
                          {schedule.enabled ? (
                            <button className="tone" onClick={() => disableSchedule(schedule.id).then(refreshAll)}>
                              Disable
                            </button>
                          ) : (
                            <button className="tone" onClick={() => enableSchedule(schedule.id).then(refreshAll)}>
                              Enable
                            </button>
                          )}
                          <button className="ghost" onClick={() => deleteSchedule(schedule.id).then(refreshAll)}>
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="actions">
              {Array.from({ length: totalSchedulePages }, (_, index) => index + 1).map((page) => (
                <button
                  key={page}
                  className={page === schedulePage ? "tone" : "secondary"}
                  onClick={() => loadSchedulePage(page)}
                  disabled={loading || page === schedulePage}
                >
                  {page}
                </button>
              ))}
            </div>
          </section>
        </main>
      )}

      {view === "form" && (
        <main>
          <section className="panel">
            <h2>{editingId ? "Edit Schedule" : "Create Schedule"}</h2>
            <div className="form-grid">
              <label className="field">
                Name
                <input
                  value={form.name}
                  onChange={(event) => setForm({ ...form, name: event.target.value })}
                  placeholder="Morning cleanup"
                />
              </label>
              <label className="field">
                Schedule Type
                <select
                  value={form.scheduleType}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      scheduleType: event.target.value as ScheduleRequest["scheduleType"],
                    })
                  }
                >
                  <option value="CRON">CRON</option>
                  <option value="ONCE">ONCE</option>
                </select>
              </label>
              {form.scheduleType === "CRON" && (
                <label className="field">
                  Cron Expression
                  <input
                    value={form.cronExpression ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, cronExpression: event.target.value })
                    }
                    placeholder="0/30 * * * * ?"
                  />
                </label>
              )}
              {form.scheduleType === "ONCE" && (
                <label className="field">
                  Run At
                  <input
                    type="datetime-local"
                    value={form.runAt ?? ""}
                    onChange={(event) => setForm({ ...form, runAt: event.target.value })}
                  />
                </label>
              )}
              <label className="field">
                Enabled
                <select
                  value={form.enabled ? "true" : "false"}
                  onChange={(event) =>
                    setForm({ ...form, enabled: event.target.value === "true" })
                  }
                >
                  <option value="true">Enabled</option>
                  <option value="false">Disabled</option>
                </select>
              </label>
              <label className="field">
                Action Key
                <select
                  value={form.actionKey}
                  onChange={(event) => setForm({ ...form, actionKey: event.target.value })}
                >
                  {taskOptions.map((task) => (
                    <option key={task.taskId} value={task.taskId}>
                      {task.name} ({task.taskId})
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                Payload (JSON)
                <textarea
                  value={form.payload ?? ""}
                  onChange={(event) => setForm({ ...form, payload: event.target.value })}
                />
              </label>
              <div className="actions">
                <button onClick={handleSubmit} disabled={loading}>
                  {editingId ? "Update" : "Create"}
                </button>
                <button
                  className="secondary"
                  onClick={() => {
                    setEditingId(null);
                    setForm(emptyForm);
                  }}
                >
                  Reset
                </button>
              </div>
            </div>
          </section>
          <section className="panel">
            <h2>Task Catalog</h2>
            <div className="notice">Available actions from backend registry.</div>
            <div style={{ overflowX: "auto" }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Task ID</th>
                    <th>Name</th>
                    <th>Description</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((task) => (
                    <tr key={task.taskId}>
                      <td><code>{task.taskId}</code></td>
                      <td>{task.name}</td>
                      <td>{task.description}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </main>
      )}

      {view === "manual" && (
        <main>
          <section className="panel">
            <h2>Manual Execution</h2>
            <div className="form-grid">
              <label className="field">
                Action Key
                <select
                  value={manualTask}
                  onChange={(event) => setManualTask(event.target.value)}
                >
                  {taskOptions.map((task) => (
                    <option key={task.taskId} value={task.taskId}>
                      {task.name} ({task.taskId})
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                Payload (JSON)
                <textarea
                  value={manualPayload}
                  onChange={(event) => setManualPayload(event.target.value)}
                />
              </label>
              <div className="actions">
                <button onClick={handleManual} disabled={loading}>
                  Execute Now
                </button>
                <button className="secondary" onClick={() => setManualPayload("{}")}
                >
                  Clear Payload
                </button>
              </div>
            </div>
            {manualResult && (
              <div className="panel" style={{ marginTop: 12 }}>
                <h2>Execution Result</h2>
                <div className="card-row">
                  <div><strong>ID:</strong> {manualResult.executionId}</div>
                  <div><strong>Status:</strong> {manualResult.status}</div>
                  <div><strong>Created:</strong> {new Date(manualResult.createdAt).toLocaleString()}</div>
                </div>
              </div>
            )}
          </section>
        </main>
      )}

      {view === "executions" && (
        <main>
          <section className="panel" style={{ gridColumn: "1 / -1" }}>
            <div className="card-row">
              <h2>Execution History</h2>
              <div className="notice">
                {loading
                  ? "Loading executions..."
                  : `${executions.length} executions loaded.`}
              </div>
            </div>
            <div style={{ overflowX: "auto" }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Execution ID</th>
                    <th>Task</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Attempt</th>
                    <th>Created</th>
                    <th>Schedule ID</th>
                    <th>Payload</th>
                    <th>Started</th>
                    <th>Completed</th>
                  </tr>
                </thead>
                <tbody>
                  {executions.map((execution) => (
                    <tr key={execution.executionId}>
                      <td><code>{execution.executionId}</code></td>
                      <td>{execution.taskId}</td>
                      <td>{execution.executionType}</td>
                      <td>
                        <span className={`status ${execution.status}`}>
                          {execution.status}
                        </span>
                      </td>
                      <td>{execution.attemptCount}</td>
                      <td>{new Date(execution.createdAt).toLocaleString()}</td>
                      <td><code>{execution.scheduleId ?? "-"}</code></td>
                      <td><code>{execution.payload ?? "-"}</code></td>
                      <td>{execution.startedAt ? new Date(execution.startedAt).toLocaleString() : "-"}</td>
                      <td>{execution.completedAt ? new Date(execution.completedAt).toLocaleString() : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="actions">
              <button
                className="secondary"
                onClick={loadMoreExecutions}
                disabled={!executionHasMore || loading}
              >
                {executionHasMore ? "Load more" : "No more"}
              </button>
            </div>
          </section>
        </main>
      )}
    </div>
  );
}
