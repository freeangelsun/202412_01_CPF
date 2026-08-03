// Generated compatibility adapter backed by the canonical full OpenAPI.
import { cpfGeneratedRequest } from "../shared/cpfApi";
export interface CpfGeneratedRequestOptions { data?: unknown; signal?: AbortSignal; headers?: HeadersInit; path?: Record<string, string | number>; query?: Record<string, unknown>; }
function renderPath(template: string, values: Record<string, string | number> = {}): string { return template.replace(/\{([^}]+)\}/g, (_, name) => { const value = values[name]; if (value === undefined || value === null || String(value).trim() === "") throw new Error(`Missing path parameter: ${name}`); return encodeURIComponent(String(value)); }); }
export async function admCalendarDeleteDay<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/business-calendars/{calendarId}/days/{businessDate}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCodeDeleteCode<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/codes/{codeId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admConfigDeleteConfig<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/configs/{configId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayDeleteBinding<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings/{id}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayDeleteServerGroup<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/server-groups/{id}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDynamicLogLevelRemove<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-level/rules/{ruleId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMessageDeleteMessage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/messages/{messageId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResponseCodeDelete<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/response-codes/{responseCode}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlDeleteGroup<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/groups/{groupId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryDeleteEndpoint<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/endpoints/{endpointCode}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryDeleteInstance<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/instances/{instanceId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryDeleteService<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/services/{serviceId}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalPolicyDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/policies/{policyCode}/{version}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalRequestDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/requests/{id}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuditLogFindAuditLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/audit-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuditDeliveryList<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/audit-logs/deliveries", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuthMe<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/auth/me", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRuntimeCommandState<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/commands/{key}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRuntimeInstances<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/instances", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchJobDefinitions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/job-definitions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchJobDefinitionDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/job-definitions/{jobId}/versions/{version}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRuntimeView<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/views/{view}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindExecutionTargets<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/execution-targets", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindExecutions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/executions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindExecutionPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/executions/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindExecutionDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/executions/{executionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindGhostCandidates<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/ghost-candidates", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindInstances<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/instances", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindJobs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/jobs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindJobDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/jobs/{jobId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindLocks<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/locks", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindOperationLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/operations", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindRelations<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/relations", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindSchedules<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/schedules", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchSimulateSchedule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/schedules/{scheduleId}/simulation", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindStepExecutions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/steps", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchExecutions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/executions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchExecutionDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/executions/{executionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchInfrastructure<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/infrastructure", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchJobs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/jobs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchJobDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/jobs/{jobId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchOverview<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/overview", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchRecovery<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/recovery", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchWorkbenchSchedules<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workbench/schedules", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchFindWorkers<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/workers", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBreakGlassFindSessions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/break-glass", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCalendarFindDays<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/business-calendars/{calendarId}/days", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCalendarResolveDate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/business-calendars/{calendarId}/resolve", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCacheSummary<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/cache/summary", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindJobs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindJobDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs/{centerCutJobId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindParameters<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs/{centerCutJobId}/parameters", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindResults<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs/{centerCutJobId}/results", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindSummary<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs/{centerCutJobId}/summary", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindTargets<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/jobs/{centerCutJobId}/targets", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCenterCutFindResultDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/center-cut/results/{resultId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelFindSnapshot<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelExportPackage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels/package", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCodeFindCodes<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/codes", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCodeFindCode<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/codes/{codeId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admConfigFindConfigs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/configs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admConfigFindConfig<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/configs/{configId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDownloadFindDownloadAuditLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/downloads/audit-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDownloadFindPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/downloads/policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobList<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobArtifact<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/artifact", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobRows<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/rows", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindBindings<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindApplyStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings/{id}/apply-status", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindConnectionTests<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings/{id}/connection-tests", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayCapability<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/capability", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindConnectionTestOperation<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/connection-test-operations/{operationId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayOperationsEvents<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/operations/events", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayOperationsSnapshot<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/operations/snapshot", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayOperationsStream<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/operations/stream", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindServerGroups<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/server-groups", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayFindGroupMembers<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/server-groups/{id}/members", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function getAdmReadiness<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/health", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function getAdmLiveness<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/health/liveness", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentFindIncidents<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentFindMaintenance<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/maintenance-windows", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentFindPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentFindIncident<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentFindTimeline<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/timeline", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogExportDownload<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-exports/{exportId}/artifact", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDynamicLogLevelFindRules<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-level/rules", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyFindPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyDistributionStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/distribution", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyFindTraceBoostHistory<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/history", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyFindTraceBoostRuntimeState<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/runtime-state", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyFindPolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/{policyId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyAuditFindPolicyAudits<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policy-audits", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogFindLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogGetLogDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/logs/{logIdx}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMaintenanceFindActions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/maintenance/actions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMessageFindMessages<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/messages", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMessageFindMessage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/messages/{messageId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationFindDeliveryLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/delivery-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationFindDlq<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/delivery-logs/dlq", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationFindDeliveryAttempts<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/delivery-logs/{deliveryId}/attempts", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationFindRules<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationFindRule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules/{ruleId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function traceAdmByBusinessTransactionId<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/observability/business-transactions/{businessTransactionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function traceAdmByTraceId<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/observability/traces/{traceId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function traceAdmByTransactionId<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/observability/transactions/{transactionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorFindOperators<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorFindMenus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/menus", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorFindCreateResult<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/operations/{operationId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorPasswordPolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/password-policy", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorValidatePassword<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/password-policy/validate", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorFindRoles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/roles", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorFindSessions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/sessions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admParameterReferenceSearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/parameter-references", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindApiPermissionMatrix<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-matrix", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindApiPermissions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-permissions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindApiPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-permissions/{apiPermissionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindButtonMatrix<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/button-matrix", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindButtons<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/buttons", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindButton<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/buttons/{buttonId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindMenuMatrix<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menu-matrix", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindManagedMenus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menus", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindManagedMenu<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menus/{menuId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindRoles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionFindRole<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagSearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagFind<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/{flagKey}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResiliencePolicySearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/resilience-policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResiliencePolicyFind<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/resilience-policies/{operationId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmBatchJobInstanceLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/batch-job-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function getAdmBatchJobInstanceLog<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/batch-job-logs/{businessDate}/{jobName}/{jobInstanceId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmBrokerDlq<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/broker/dlq", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmBrokerInbox<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/broker/inbox", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmBrokerOutbox<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/broker/outbox", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmFileTransferHistory<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/file-transfers", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmIdempotencyRecords<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/idempotency", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function getAdmTransactionLogRecoveryStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/transaction-log-recovery", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function findAdmUnknownResults<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/unknown-results", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogSearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogBundleJobFind<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/bundle-jobs/{jobId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogBundleJobDownload<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/bundle-jobs/{jobId}/download", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogDiagnostics<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/diagnostics", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogDownload<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/{artifactId}/download", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogPreview<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/{artifactId}/preview", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResponseCodeFindAll<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/response-codes", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResponseCodeFindOne<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/response-codes/{responseCode}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindCapabilities<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/capabilities", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindChange<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/changes/{changeId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlVerifyAudit<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/changes/{changeId}/audit/verify", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindGroup<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/groups/{groupId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindHealth<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/health", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindByOperation<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/operations/{operationId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindStateCatalog<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/states", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlFindStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/status", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecretFindMetadata<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/secrets/metadata", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecretFindProviders<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/secrets/providers", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecurityFindIpAllowlist<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/ip-allowlist", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecurityFindMfaStates<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/mfa", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindCallHistory<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/call-history", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryCapabilities<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/capabilities", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindCircuitStates<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/circuit-states", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindEndpoints<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/endpoints", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindHealth<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/health", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindInstances<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/instances", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindRoutingPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/routing-policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryFindServices<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/services", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admStandardExecutionFindAll<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/standard-executions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admStandardExecutionFindOne<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/standard-executions/{standardExecutionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindGroups<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups/{transactionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindExternalLogs<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups/{transactionId}/external-logs", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindHeaders<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups/{transactionId}/headers", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindSegments<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups/{transactionId}/segments", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionGroupFindTimeline<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transaction-groups/{transactionId}/timeline", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionMetaFindTransactions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transactions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionMetaFindPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transactions/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionMetaFindTransaction<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transactions/{transactionId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function getAdmSystemVersion<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/v1/system/version", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyDisableOverride<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/overrides/{overrideId}/disable", options.path), method: "PATCH", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalPolicySave<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/policies", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalRequest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/requests", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalDecision<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/requests/{id}/decisions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admApprovalExecute<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/approvals/requests/{id}/execute", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuditDeliveryRetry<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/audit-logs/deliveries/{deliveryId}/retry", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuthLogin<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/auth/login", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admAuthLogout<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/auth/logout", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRuntimeCommand<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/commands", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRuntimeCreateDeploymentPlan<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/deployment-plans", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchJobDefinitionSave<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/job-definitions/drafts", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchJobDefinitionValidate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/job-definitions/validate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchJobDefinitionTransition<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch-runtime/job-definitions/{jobId}/versions/{version}/transition", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRetryExecution<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/executions/{executionId}/retry", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchStopExecution<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/executions/{executionId}/stop", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchActGhostExecution<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/ghost-candidates/{executionId}/actions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRegisterJob<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/jobs", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRunJob<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/jobs/{jobId}/run", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchReleaseLock<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/locks/release", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchRunSchedulerOnce<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/scheduler/run-once", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchDisableSchedule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/schedules/{scheduleId}/disable", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBatchEnableSchedule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/batch/schedules/{scheduleId}/enable", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBreakGlassOpenSession<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/break-glass", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBreakGlassCloseSession<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/break-glass/{sessionId}/close", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admBreakGlassReviewSession<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/break-glass/{sessionId}/review", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCacheEvictKey<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/cache/evict-key", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCacheEvictNamespace<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/cache/evict-namespace", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCacheReconcile<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/cache/reconcile", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCacheRefresh<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/cache/refresh", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelImportPackage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels/package/import", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelRefreshSnapshot<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels/refresh", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCodeCreateCode<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/codes", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admConfigCreateConfig<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/configs", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDownloadDownloadCsv<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/downloads/csv", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobUpload<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/uploads", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobApply<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/apply", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobCancel<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/cancel", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobResolveUnknown<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/resolve-unknown", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobRetry<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/retry", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFileJobRollback<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/file-jobs/{jobId}/rollback", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewaySaveBinding<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayRequestConnectionTest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings/{id}/connection-tests", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayChangeBindingState<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/bindings/{id}/state", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayCancelConnectionTest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/connection-test-operations/{operationId}/cancel", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewayRevalidateConnectionTest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/connection-test-operations/{operationId}/revalidate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admGatewaySaveServerGroup<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/gateway-registry/server-groups", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentCreateIncident<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentCreateMaintenance<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/maintenance-windows", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentCreatePolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/policies", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentIngestSignal<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/signals", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentAcknowledge<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/acknowledge", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentEscalate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/escalate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentReopen<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/reopen", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentResolve<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/resolve", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentTransitionIncident<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/{incidentId}/status", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogExportCreate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-exports", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyCreatePolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyClearCache<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/cache/clear", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyRefreshCache<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/cache/refresh", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyCreateOverride<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/overrides", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyCreateTraceBoost<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/trace-boost", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyDisablePolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/{policyId}/disable", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMaintenanceExecuteAction<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/maintenance/actions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMessageCreateMessage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/messages", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationCancelDelivery<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/delivery-logs/{deliveryId}/cancel", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationRetryDelivery<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/delivery-logs/{deliveryId}/retry", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationSaveRule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationSendTest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules/{ruleId}/test-send", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorCreateOperator<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorCleanupExpiredSessions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/sessions/cleanup-expired", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorRevokeSession<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/sessions/{sessionId}/revoke", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorRawContact<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/contacts/raw", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorChangePassword<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/password", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorResetPassword<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/password/reset", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorUnlockOperator<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/unlock", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionCreateApiPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-permissions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionCreateButton<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/buttons", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionCreateMenu<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menus", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionCreateRole<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagEvaluate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/evaluate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagRequestOverride<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/override-requests", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagApproveOverride<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/override-requests/{requestId}/approve", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagRevokeOverride<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/override-requests/{requestId}/revoke", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admFeatureFlagSetKillSwitch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/feature-flags/{flagKey}/kill-switch", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResiliencePolicyRequest<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/resilience-policies/requests", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResiliencePolicyApprove<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/resilience-policies/requests/{requestId}/approve", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResiliencePolicyReject<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/platform/resilience-policies/requests/{requestId}/reject", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function requestAdmBrokerDlqReplay<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/broker/dlq/{messageId}/replay", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function retryAdmTraceRecoveryPoison<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/transaction-log-recovery/poison/{target}/{recoveryEventId}/retry", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function runAdmTransactionLogRecovery<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/transaction-log-recovery/run", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function resolveAdmUnknownResult<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/reliability/unknown-results/{unknownId}/resolve", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogBundleJobCreate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/bundle-jobs", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogBundleDownloadTokenIssue<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/bundle-jobs/{jobId}/download-tokens", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRemoteLogBundleDownload<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/remote-logs/bundles", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResponseCodeCreate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/response-codes", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlCreateChange<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/changes", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlCancelChange<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/changes/{changeId}/cancel", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlRollbackChange<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/changes/{changeId}/rollback", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlSaveGroup<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/groups", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlChangeGroupMember<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/groups/{groupId}/members", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlPreviewChange<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/preview-change", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admRuntimeControlPreviewTargets<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/runtime-control/preview-targets", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecretRotate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/secrets/rotate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecuritySaveIpAllowlist<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/ip-allowlist", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecurityDisableMfa<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/mfa/{operatorId}/disable", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecurityRegisterMfa<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/mfa/{operatorId}/register", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admSecurityVerifyMfa<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/security/mfa/{operatorId}/verify", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistrySaveEndpoint<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/endpoints", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistrySaveInstance<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/instances", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistrySaveService<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/services", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admServiceRegistryChangeInstanceState<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/service-registry/services/{serviceId}/endpoints/{endpointCode}/instances/{instanceId}/state", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionMetaScan<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transactions/scan", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admTransactionMetaInactivate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/transactions/{transactionId}/inactive", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCalendarSaveDay<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/business-calendars/{calendarId}/days/{businessDate}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelSaveExecutionPolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels/policies/{policyKey}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admChannelSave<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/channels/{channelCode}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admCodeUpdateCode<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/codes/{codeId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admConfigUpdateConfig<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/configs/{configId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentUpdateMaintenance<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/maintenance-windows/{maintenanceId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admIncidentUpdatePolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/incidents/policies/{policyId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admDynamicLogLevelRegister<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-level/rules", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admLogPolicyUpdatePolicy<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/log-policies/{policyId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admMessageUpdateMessage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/messages/{messageId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationUpdateRule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules/{ruleId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admNotificationDisableRule<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/notifications/rules/{ruleId}/disable", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorUpdateContact<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/contacts", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorUpdateRoles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/roles", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admOperatorUpdateStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/operators/{operatorId}/status", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateApiPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-permissions/{apiPermissionId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateApiPermissionStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/api-permissions/{apiPermissionId}/status", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateButton<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/buttons/{buttonId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateButtonStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/buttons/{buttonId}/status", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateMenu<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menus/{menuId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateMenuStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/menus/{menuId}/status", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateRole<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateRoleApiPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}/api-permissions/{apiPermissionId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateButtonPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}/buttons/{buttonId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateMenuPermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}/menus/{menuId}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admPermissionUpdateRoleStatus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/permissions/roles/{roleId}/status", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function admResponseCodeUpdate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/adm/api/response-codes/{responseCode}", options.path), method: "PUT", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
