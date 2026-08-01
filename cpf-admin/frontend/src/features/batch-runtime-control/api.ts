import { admApi, CpfApiError } from "../../shared/cpfApi";

export interface RuntimeInstance {
  instance_id: string
  runtime_role: string
  service_id: string
  host_alias?: string
  zone_id?: string
  pool_id?: string
  artifact_version: string
  desired_state: string
  effective_state: string
  last_heartbeat_at?: string
  fencing_token: number
}
export interface RuntimeEnvelope {
  fetchedAt: string
  stale: boolean
  partial: boolean
  errorCode?: string
  items: RuntimeInstance[]
}
export interface BatchViewEnvelope {
  fetchedAt: string
  stale: boolean
  partial: boolean
  errorCode?: string
  view: string
  items: Array<Record<string, unknown>>
}
async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  try {
    return await admApi<T>(url, options)
  } catch (error) {
    if (error instanceof CpfApiError && error.status === 503 && error.payload) {
      return error.payload as T
    }
    throw error
  }
}
export async function fetchRuntimeInstances(): Promise<RuntimeEnvelope> {
  return request('/adm/api/batch-runtime/instances', { credentials: 'same-origin' })
}
export async function fetchBatchView(view: string): Promise<BatchViewEnvelope> {
  return request(`/adm/api/batch-runtime/views/${encodeURIComponent(view)}`, { credentials: 'same-origin' })
}
export async function createDeploymentPlan(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/deployment-plans', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  })
}

export async function fetchJobDefinitions(jobId = "", state = ""): Promise<BatchViewEnvelope> {
  const params = new URLSearchParams({ limit: "500" });
  if (jobId) params.set("jobId", jobId); if (state) params.set("state", state);
  return request(`/adm/api/batch-runtime/job-definitions?${params.toString()}`, { credentials: "same-origin" });
}

export async function fetchJobDefinitionDetail(jobId:string, version:number): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch-runtime/job-definitions/${encodeURIComponent(jobId)}/versions/${version}`, { credentials: "same-origin" });
}
export async function validateJobDefinition(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/job-definitions/validate', { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}
export async function saveJobDefinition(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/job-definitions/drafts', { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}
export async function transitionJobDefinition(jobId:string, version:number, body:unknown): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch-runtime/job-definitions/${encodeURIComponent(jobId)}/versions/${version}/transition`, { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}


export interface BatchExecutionSearch {
  jobId?: string
  transactionId?: string
  springBatchJobInstanceId?: number
  workerId?: string
  serverInstanceId?: string
  status?: string
  fromDate?: string
  toDate?: string
  page: number
  size: number
}
export interface BatchExecutionPage {
  items: Array<Record<string, unknown>>
  page: number
  size: number
  hasNext: boolean
  totalKnown: boolean
  total?: number
  pagingMode: string
}
export interface BatchExecutionDetail extends Record<string, unknown> {
  steps?: Array<Record<string, unknown>>
}
export type BatchExecutionAction = 'retry' | 'stop'

export async function fetchBatchExecutionPage(search: BatchExecutionSearch): Promise<BatchExecutionPage> {
  const params = new URLSearchParams({
    page: String(Math.max(0, search.page)),
    size: String(Math.max(10, Math.min(200, search.size))),
  })
  for (const [key, value] of Object.entries(search)) {
    if (key === 'page' || key === 'size' || value === undefined || value === null || String(value).trim() === '') continue
    params.set(key, String(value))
  }
  return request(`/adm/api/batch/executions/page?${params.toString()}`, { credentials: 'same-origin' })
}

export async function fetchBatchExecutionDetail(executionId: number): Promise<BatchExecutionDetail> {
  if (!Number.isSafeInteger(executionId) || executionId <= 0) throw new Error('유효한 executionId가 필요합니다.')
  return request(`/adm/api/batch/executions/${executionId}`, { credentials: 'same-origin' })
}


export interface BatchRuntimeCommandRequest {
  commandId: string
  idempotencyKey: string
  commandType: 'START' | 'STOP' | 'RESTART' | 'DRAIN' | 'RESUME' | 'ROLLBACK'
  targetType: 'INSTANCE' | 'POOL' | 'AGENT'
  targetIds: string[]
  targetSnapshot?: string
  targetSnapshotHash?: string
  expectedVersion: number
  reason: string
  requestedAt: string
  approvalPolicyVersion: string
  approvalRequestId: string
  approvedBy: string
  expiresAt: string
  executionState?: string
  executionAttempt?: number
  parameters?: Record<string, unknown>
  transactionId?: string
  evidenceRef?: string
}

export async function fetchBatchSchedules(): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/batch/schedules', { credentials: 'same-origin' })
}
export async function simulateBatchSchedule(scheduleId: string, baseDate = '', days = 14): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams({ days: String(Math.max(1, Math.min(days, 90))) })
  if (baseDate) params.set('baseDate', baseDate)
  return request(`/adm/api/batch/schedules/${encodeURIComponent(scheduleId)}/simulation?${params}`, { credentials: 'same-origin' })
}
export async function enableBatchSchedule(scheduleId: string, body: Record<string, unknown>): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/schedules/${encodeURIComponent(requiredId(scheduleId, 'scheduleId'))}/enable`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  })
}
export async function disableBatchSchedule(scheduleId: string, body: Record<string, unknown>): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/schedules/${encodeURIComponent(requiredId(scheduleId, 'scheduleId'))}/disable`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  })
}
export async function setBatchScheduleEnabled(scheduleId: string, enabled: boolean, reason: string): Promise<Record<string, unknown>> {
  const body = { reason: reason.trim() }
  return enabled ? enableBatchSchedule(scheduleId, body) : disableBatchSchedule(scheduleId, body)
}
export async function runBatchSchedulerOnce(reason: string): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/batch/scheduler/run-once', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ reason: reason.trim() }),
  })
}
export async function fetchBatchWorkers(heartbeatTimeoutSeconds = 120): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/batch/workers?heartbeatTimeoutSeconds=${Math.max(10, heartbeatTimeoutSeconds)}`, { credentials: 'same-origin' })
}
export async function fetchBatchInstances(): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/batch/instances', { credentials: 'same-origin' })
}
export async function fetchBatchJobs(): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/batch/jobs', { credentials: 'same-origin' })
}
export async function fetchBatchSteps(executionId: string): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams({ executionId: requiredId(executionId, 'executionId') })
  return request(`/adm/api/batch/steps?${params}`, { credentials: 'same-origin' })
}
export async function fetchBatchLocks(jobId = ''): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams(); if (jobId) params.set('jobId', jobId)
  return request(`/adm/api/batch/locks?${params}`, { credentials: 'same-origin' })
}
export async function releaseBatchLock(lockKey: string, reason: string): Promise<Record<string, unknown>> {
  return request('/adm/api/batch/locks/release', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ lockKey, reason: reason.trim() }),
  })
}
export async function fetchBatchGhostCandidates(heartbeatTimeoutSeconds = 120): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/batch/ghost-candidates?heartbeatTimeoutSeconds=${Math.max(10, heartbeatTimeoutSeconds)}`, { credentials: 'same-origin' })
}
export async function actBatchGhostExecution(executionId: number, actionType: string, reason: string): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/ghost-candidates/${executionId}/actions`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ actionType, reason: reason.trim() }),
  })
}
export async function submitBatchRuntimeCommand(command: BatchRuntimeCommandRequest): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/commands', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(command),
  })
}
export async function fetchBatchRuntimeCommandState(key: string): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch-runtime/commands/${encodeURIComponent(key)}`, { credentials: 'same-origin' })
}

export async function fetchBatchJobDetail(jobId: string): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/jobs/${encodeURIComponent(jobId)}`, { credentials: 'same-origin' })
}
export async function fetchBatchRelations(jobId = ''): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams(); if (jobId) params.set('jobId', jobId)
  return request(`/adm/api/batch/relations?${params}`, { credentials: 'same-origin' })
}
export async function fetchBatchExecutionTargets(jobId = '', dispatchStatus = '', limit = 100): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams({ limit: String(Math.max(1, Math.min(limit, 500))) })
  if (jobId) params.set('jobId', jobId); if (dispatchStatus) params.set('dispatchStatus', dispatchStatus)
  return request(`/adm/api/batch/execution-targets?${params}`, { credentials: 'same-origin' })
}
export async function fetchBatchOperationLogs(jobId = '', executionId?: number, limit = 100): Promise<Array<Record<string, unknown>>> {
  const params = new URLSearchParams({ limit: String(Math.max(1, Math.min(limit, 500))) })
  if (jobId) params.set('jobId', jobId); if (executionId) params.set('executionId', String(executionId))
  return request(`/adm/api/batch/operations?${params}`, { credentials: 'same-origin' })
}


export interface AdmPage<T extends Record<string, unknown>> {
  items: T[]
  page: number
  size: number
  total: number
  hasNext: boolean
  totalKnown?: boolean
  pagingMode?: string
  fetchedAt?: string
  partial?: boolean
  stale?: boolean
}
export interface BatchInfrastructureWorkspace extends Record<string, unknown> {
  instances: Array<Record<string, unknown>>
  workers: Array<Record<string, unknown>>
  targets: Array<Record<string, unknown>>
  fetchedAt?: string
  partial?: boolean
  stale?: boolean
}
export interface BatchRecoveryWorkspace extends Record<string, unknown> {
  ghostCandidates: Array<Record<string, unknown>>
  unknownResults: Array<Record<string, unknown>>
  locks: Array<Record<string, unknown>>
  operations: Array<Record<string, unknown>>
  fetchedAt?: string
  partial?: boolean
  stale?: boolean
}
export interface CenterCutWorkspace extends Record<string, unknown> {
  job: Record<string, unknown>
  summary: Record<string, unknown>
  results: Array<Record<string, unknown>>
  resultDetail?: Record<string, unknown>
  targets: Array<Record<string, unknown>>
  parameters: Array<Record<string, unknown>>
}
export interface BatchAlertsWorkspace extends Record<string, unknown> {
  unknownResults: Array<Record<string, unknown>>
  dlq: Array<Record<string, unknown>>
  outbox: Array<Record<string, unknown>>
  operations: Array<Record<string, unknown>>
}
export interface BatchAuditWorkspace extends Record<string, unknown> {
  auditLogs: Array<Record<string, unknown>>
  deliveries: Array<Record<string, unknown>>
  operations: Array<Record<string, unknown>>
}
export interface DangerousBatchCommand {
  reason: string
  approvalId: string
  expectedVersion?: number
  idempotencyKey: string
}

function pageParams(search: Record<string, unknown>): URLSearchParams {
  const params = new URLSearchParams()
  for (const [key, raw] of Object.entries(search)) {
    if (raw === undefined || raw === null || String(raw).trim() === '') continue
    const value = key === 'size' ? Math.max(10, Math.min(200, Number(raw))) : key === 'page' ? Math.max(0, Number(raw)) : raw
    params.set(key, String(value))
  }
  return params
}
function requiredId(value: string, label: string): string {
  const normalized = value.trim()
  if (!normalized || normalized === '-') throw new Error(`${label}가 필요합니다.`)
  return normalized
}
function commandBody(command: DangerousBatchCommand): Record<string, unknown> {
  const reason = command.reason.trim()
  const approvalRequestId = command.approvalId.trim()
  if (reason.length < 5) throw new Error('감사 가능한 사유를 5자 이상 입력하세요.')
  if (!approvalRequestId) throw new Error('승인 ID가 필요합니다.')
  if (!command.idempotencyKey.trim()) throw new Error('멱등 키가 필요합니다.')
  return {
    reason,
    approvalRequestId,
    expectedVersion: command.expectedVersion,
    idempotencyKey: command.idempotencyKey.trim(),
  }
}

export async function fetchExecutionPage(search: Record<string, unknown>): Promise<AdmPage<Record<string, unknown>>> {
  return request(`/adm/api/batch/workbench/executions?${pageParams(search)}`, { credentials: 'same-origin' })
}
export async function fetchExecutionWorkspace(executionId: string): Promise<Record<string, unknown>> {
  const normalized = requiredId(executionId, 'executionId')
  const [workspace, steps] = await Promise.all([
    request<Record<string, unknown>>(`/adm/api/batch/workbench/executions/${encodeURIComponent(normalized)}`, { credentials: 'same-origin' }),
    fetchBatchSteps(normalized),
  ])
  return { ...workspace, steps: Array.isArray(workspace.steps) ? workspace.steps : steps }
}
export async function fetchJobPage(search: Record<string, unknown>): Promise<AdmPage<Record<string, unknown>>> {
  return request(`/adm/api/batch/workbench/jobs?${pageParams(search)}`, { credentials: 'same-origin' })
}
export async function fetchJobWorkspace(jobId: string): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/workbench/jobs/${encodeURIComponent(requiredId(jobId, 'jobId'))}`, { credentials: 'same-origin' })
}
export async function fetchSchedulePage(search: Record<string, unknown>): Promise<AdmPage<Record<string, unknown>>> {
  return request(`/adm/api/batch/workbench/schedules?${pageParams(search)}`, { credentials: 'same-origin' })
}
export async function fetchInfrastructure(heartbeatTimeoutSeconds = 120, limit = 500): Promise<BatchInfrastructureWorkspace> {
  const params = pageParams({ heartbeatTimeoutSeconds, limit })
  return request(`/adm/api/batch/workbench/infrastructure?${params}`, { credentials: 'same-origin' })
}
export async function fetchRecovery(heartbeatTimeoutSeconds = 120, limit = 500): Promise<BatchRecoveryWorkspace> {
  const params = pageParams({ heartbeatTimeoutSeconds, limit })
  const [workspace, unknownResults] = await Promise.all([
    request<BatchRecoveryWorkspace>(`/adm/api/batch/workbench/recovery?${params}`, { credentials: 'same-origin' }),
    fetchUnknownResults('', limit),
  ])
  return { ...workspace, unknownResults }
}
export async function fetchBatchOverview(): Promise<Record<string, unknown>> {
  const [overview, jobs, locks] = await Promise.all([
    request<Record<string, unknown>>('/adm/api/batch/workbench/overview', { credentials: 'same-origin' }),
    fetchBatchJobs(),
    fetchBatchLocks(),
  ])
  return { ...overview, jobs, locks }
}
export async function simulateSchedule(scheduleId: string, baseDate: string, days: number): Promise<Array<Record<string, unknown>>> {
  return simulateBatchSchedule(requiredId(scheduleId, 'scheduleId'), baseDate, days)
}
export async function retryExecution(executionId: string, command: DangerousBatchCommand): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/executions/${encodeURIComponent(requiredId(executionId, 'executionId'))}/retry`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(commandBody(command)),
  })
}
export async function stopExecution(executionId: string, command: DangerousBatchCommand): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/executions/${encodeURIComponent(requiredId(executionId, 'executionId'))}/stop`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(commandBody(command)),
  })
}
export async function runJob(jobId: string, command: DangerousBatchCommand): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch/jobs/${encodeURIComponent(requiredId(jobId, 'jobId'))}/run`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...commandBody(command), jobParameters: '{}' }),
  })
}
export async function setScheduleEnabled(scheduleId: string, enabled: boolean, command: DangerousBatchCommand): Promise<Record<string, unknown>> {
  const body = commandBody(command)
  return enabled ? enableBatchSchedule(scheduleId, body) : disableBatchSchedule(scheduleId, body)
}
export async function runSchedulerOnce(command: DangerousBatchCommand): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/batch/scheduler/run-once', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(commandBody(command)),
  })
}
export async function releaseLock(command: DangerousBatchCommand & { lockKey: string }): Promise<Record<string, unknown>> {
  return request('/adm/api/batch/locks/release', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...commandBody(command), lockKey: requiredId(command.lockKey, 'lockKey') }),
  })
}
export async function actGhostExecution(executionId: string, command: DangerousBatchCommand & { actionType: string }): Promise<Record<string, unknown>> {
  const actionType = command.actionType.trim().toUpperCase()
  if (!['FAIL', 'ABANDON', 'RELEASE_LOCK'].includes(actionType)) throw new Error(`지원하지 않는 Ghost 조치입니다: ${actionType}`)
  return request(`/adm/api/batch/ghost-candidates/${encodeURIComponent(requiredId(executionId, 'executionId'))}/actions`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...commandBody(command), actionType }),
  })
}

export async function fetchCenterCutJobs(): Promise<Array<Record<string, unknown>>> {
  return request('/adm/api/center-cut/jobs', { credentials: 'same-origin' })
}
export async function fetchCenterCutWorkspace(centerCutJobId: string): Promise<CenterCutWorkspace> {
  const id = encodeURIComponent(requiredId(centerCutJobId, 'centerCutJobId'))
  const [job, summary, results, targets, parameters] = await Promise.all([
    request<Record<string, unknown>>(`/adm/api/center-cut/jobs/${id}`, { credentials: 'same-origin' }),
    request<Record<string, unknown>>(`/adm/api/center-cut/jobs/${id}/summary`, { credentials: 'same-origin' }),
    request<Array<Record<string, unknown>>>(`/adm/api/center-cut/jobs/${id}/results`, { credentials: 'same-origin' }),
    request<Array<Record<string, unknown>>>(`/adm/api/center-cut/jobs/${id}/targets`, { credentials: 'same-origin' }),
    request<Array<Record<string, unknown>>>(`/adm/api/center-cut/jobs/${id}/parameters`, { credentials: 'same-origin' }),
  ])
  const firstResultId = results.length ? String(results[0].resultId ?? results[0].result_id ?? '').trim() : ''
  const resultDetail = firstResultId
    ? await request<Record<string, unknown>>(`/adm/api/center-cut/results/${encodeURIComponent(firstResultId)}`, { credentials: 'same-origin' })
    : undefined
  return { job, summary, results, resultDetail, targets, parameters }
}
export async function fetchUnknownResults(status = '', limit = 200): Promise<Array<Record<string, unknown>>> {
  const params = pageParams({ status, limit })
  return request(`/adm/api/reliability/unknown-results?${params}`, { credentials: 'same-origin' })
}
export async function resolveUnknownResult(unknownId: string, resolution: string, command: DangerousBatchCommand): Promise<Record<string, unknown>> {
  const normalizedResolution = resolution.trim().toUpperCase()
  if (!['CONFIRMED_SUCCESS', 'CONFIRMED_FAILURE', 'COMPENSATED', 'REPLAYED'].includes(normalizedResolution)) {
    throw new Error(`지원하지 않는 결과불명 판정입니다: ${normalizedResolution}`)
  }
  return request(`/adm/api/reliability/unknown-results/${encodeURIComponent(requiredId(unknownId, 'unknownId'))}/resolve`, {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...commandBody(command), resolution: normalizedResolution }),
  })
}
export async function fetchBrokerDlq(limit = 200): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/reliability/broker/dlq?${pageParams({ limit })}`, { credentials: 'same-origin' })
}
export async function fetchBrokerOutbox(limit = 200): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/reliability/broker/outbox?${pageParams({ limit })}`, { credentials: 'same-origin' })
}
export async function fetchAuditLogs(limit = 200): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/audit-logs?${pageParams({ limit })}`, { credentials: 'same-origin' })
}
export async function fetchAuditDeliveries(limit = 200): Promise<Array<Record<string, unknown>>> {
  return request(`/adm/api/audit-logs/deliveries?${pageParams({ limit })}`, { credentials: 'same-origin' })
}
export async function fetchBatchAlertsWorkspace(limit = 200): Promise<BatchAlertsWorkspace> {
  const [unknownResults, dlq, outbox, operations] = await Promise.all([
    fetchUnknownResults('', limit), fetchBrokerDlq(limit), fetchBrokerOutbox(limit), fetchBatchOperationLogs('', undefined, limit),
  ])
  return { unknownResults, dlq, outbox, operations }
}
export async function fetchBatchAuditWorkspace(limit = 200): Promise<BatchAuditWorkspace> {
  const [auditLogs, deliveries, operations] = await Promise.all([
    fetchAuditLogs(limit), fetchAuditDeliveries(limit), fetchBatchOperationLogs('', undefined, limit),
  ])
  return { auditLogs, deliveries, operations }
}
