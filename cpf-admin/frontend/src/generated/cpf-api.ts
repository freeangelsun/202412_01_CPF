// GENERATED FROM canonical openapi/cpf-openapi.json. DO NOT EDIT.
// CPF_CANONICAL_ORVAL_DELEGATE: application-facing compatibility surface delegates every operation to the verified Orval client.
import { admApprovalDecision as orvalAdmApprovalDecision, admApprovalExecute as orvalAdmApprovalExecute, admApprovalPolicies as orvalAdmApprovalPolicies, admApprovalPolicyDetail as orvalAdmApprovalPolicyDetail, admApprovalPolicySave as orvalAdmApprovalPolicySave, admApprovalReconcile as orvalAdmApprovalReconcile, admApprovalRequest as orvalAdmApprovalRequest, admApprovalRequestDetail as orvalAdmApprovalRequestDetail, admAuditDeliveryList as orvalAdmAuditDeliveryList, admAuditDeliveryRetry as orvalAdmAuditDeliveryRetry, admAuditLogFindAuditLogs as orvalAdmAuditLogFindAuditLogs, admAuthLogin as orvalAdmAuthLogin, admAuthLogout as orvalAdmAuthLogout, admAuthMe as orvalAdmAuthMe, admBatchActGhostExecution as orvalAdmBatchActGhostExecution, admBatchDisableSchedule as orvalAdmBatchDisableSchedule, admBatchEnableSchedule as orvalAdmBatchEnableSchedule, admBatchFindExecutionDetail as orvalAdmBatchFindExecutionDetail, admBatchFindExecutionPage as orvalAdmBatchFindExecutionPage, admBatchFindExecutions as orvalAdmBatchFindExecutions, admBatchFindExecutionTargets as orvalAdmBatchFindExecutionTargets, admBatchFindGhostCandidates as orvalAdmBatchFindGhostCandidates, admBatchFindInstances as orvalAdmBatchFindInstances, admBatchFindJobDetail as orvalAdmBatchFindJobDetail, admBatchFindJobs as orvalAdmBatchFindJobs, admBatchFindLocks as orvalAdmBatchFindLocks, admBatchFindOperationLogs as orvalAdmBatchFindOperationLogs, admBatchFindRelations as orvalAdmBatchFindRelations, admBatchFindSchedules as orvalAdmBatchFindSchedules, admBatchFindStepExecutions as orvalAdmBatchFindStepExecutions, admBatchFindWorkers as orvalAdmBatchFindWorkers, admBatchJobDefinitionDetail as orvalAdmBatchJobDefinitionDetail, admBatchJobDefinitions as orvalAdmBatchJobDefinitions, admBatchJobDefinitionSave as orvalAdmBatchJobDefinitionSave, admBatchJobDefinitionTransition as orvalAdmBatchJobDefinitionTransition, admBatchJobDefinitionValidate as orvalAdmBatchJobDefinitionValidate, admBatchRegisterJob as orvalAdmBatchRegisterJob, admBatchReleaseLock as orvalAdmBatchReleaseLock, admBatchRetryExecution as orvalAdmBatchRetryExecution, admBatchRunJob as orvalAdmBatchRunJob, admBatchRunSchedulerOnce as orvalAdmBatchRunSchedulerOnce, admBatchRuntimeCommand as orvalAdmBatchRuntimeCommand, admBatchRuntimeCommandState as orvalAdmBatchRuntimeCommandState, admBatchRuntimeCreateDeploymentPlan as orvalAdmBatchRuntimeCreateDeploymentPlan, admBatchRuntimeInstances as orvalAdmBatchRuntimeInstances, admBatchRuntimeView as orvalAdmBatchRuntimeView, admBatchSimulateSchedule as orvalAdmBatchSimulateSchedule, admBatchStopExecution as orvalAdmBatchStopExecution, admBatchWorkbenchExecutionDetail as orvalAdmBatchWorkbenchExecutionDetail, admBatchWorkbenchExecutions as orvalAdmBatchWorkbenchExecutions, admBatchWorkbenchInfrastructure as orvalAdmBatchWorkbenchInfrastructure, admBatchWorkbenchJobDetail as orvalAdmBatchWorkbenchJobDetail, admBatchWorkbenchJobs as orvalAdmBatchWorkbenchJobs, admBatchWorkbenchOverview as orvalAdmBatchWorkbenchOverview, admBatchWorkbenchRecovery as orvalAdmBatchWorkbenchRecovery, admBatchWorkbenchSchedules as orvalAdmBatchWorkbenchSchedules, admBreakGlassCloseSession as orvalAdmBreakGlassCloseSession, admBreakGlassFindSessions as orvalAdmBreakGlassFindSessions, admBreakGlassOpenSession as orvalAdmBreakGlassOpenSession, admBreakGlassReviewSession as orvalAdmBreakGlassReviewSession, admCacheSummary as orvalAdmCacheSummary, admCalendarDeleteDay as orvalAdmCalendarDeleteDay, admCalendarFindDays as orvalAdmCalendarFindDays, admCalendarResolveDate as orvalAdmCalendarResolveDate, admCalendarSaveDay as orvalAdmCalendarSaveDay, admCapabilityManagementIssues as orvalAdmCapabilityManagementIssues, admCapabilityManagementOverview as orvalAdmCapabilityManagementOverview, admCenterCutFindJobDetail as orvalAdmCenterCutFindJobDetail, admCenterCutFindJobs as orvalAdmCenterCutFindJobs, admCenterCutFindParameters as orvalAdmCenterCutFindParameters, admCenterCutFindResultDetail as orvalAdmCenterCutFindResultDetail, admCenterCutFindResults as orvalAdmCenterCutFindResults, admCenterCutFindSummary as orvalAdmCenterCutFindSummary, admCenterCutFindTargets as orvalAdmCenterCutFindTargets, admCenterCutReconcileUnknownExecution as orvalAdmCenterCutReconcileUnknownExecution, admCenterCutReprocessFailedExecution as orvalAdmCenterCutReprocessFailedExecution, admChannelExportPackage as orvalAdmChannelExportPackage, admChannelFindSnapshot as orvalAdmChannelFindSnapshot, admChannelImportPackage as orvalAdmChannelImportPackage, admChannelRefreshSnapshot as orvalAdmChannelRefreshSnapshot, admChannelSave as orvalAdmChannelSave, admChannelSaveExecutionPolicy as orvalAdmChannelSaveExecutionPolicy, admCodeCreateCode as orvalAdmCodeCreateCode, admCodeDeleteCode as orvalAdmCodeDeleteCode, admCodeFindCode as orvalAdmCodeFindCode, admCodeFindCodes as orvalAdmCodeFindCodes, admCodeUpdateCode as orvalAdmCodeUpdateCode, admConfigCreateConfig as orvalAdmConfigCreateConfig, admConfigDeleteConfig as orvalAdmConfigDeleteConfig, admConfigFindConfig as orvalAdmConfigFindConfig, admConfigFindConfigs as orvalAdmConfigFindConfigs, admConfigUpdateConfig as orvalAdmConfigUpdateConfig, admDownloadDownloadCsv as orvalAdmDownloadDownloadCsv, admDownloadFindDownloadAuditLogs as orvalAdmDownloadFindDownloadAuditLogs, admDownloadFindPolicies as orvalAdmDownloadFindPolicies, admDynamicLogLevelFindRules as orvalAdmDynamicLogLevelFindRules, admFeatureFlagApproveOverride as orvalAdmFeatureFlagApproveOverride, admFeatureFlagEvaluate as orvalAdmFeatureFlagEvaluate, admFeatureFlagFind as orvalAdmFeatureFlagFind, admFeatureFlagRequestOverride as orvalAdmFeatureFlagRequestOverride, admFeatureFlagRevokeOverride as orvalAdmFeatureFlagRevokeOverride, admFeatureFlagSearch as orvalAdmFeatureFlagSearch, admFileJobArtifact as orvalAdmFileJobArtifact, admFileJobDetail as orvalAdmFileJobDetail, admFileJobList as orvalAdmFileJobList, admFileJobRows as orvalAdmFileJobRows, admFileJobUpload as orvalAdmFileJobUpload, admGatewayCancelConnectionTest as orvalAdmGatewayCancelConnectionTest, admGatewayCapability as orvalAdmGatewayCapability, admGatewayChangeBindingState as orvalAdmGatewayChangeBindingState, admGatewayDeleteBinding as orvalAdmGatewayDeleteBinding, admGatewayDeleteServerGroup as orvalAdmGatewayDeleteServerGroup, admGatewayFindApplyStatus as orvalAdmGatewayFindApplyStatus, admGatewayFindBindings as orvalAdmGatewayFindBindings, admGatewayFindConnectionTestOperation as orvalAdmGatewayFindConnectionTestOperation, admGatewayFindConnectionTests as orvalAdmGatewayFindConnectionTests, admGatewayFindGroupMembers as orvalAdmGatewayFindGroupMembers, admGatewayFindServerGroups as orvalAdmGatewayFindServerGroups, admGatewayOperationsEvents as orvalAdmGatewayOperationsEvents, admGatewayOperationsSnapshot as orvalAdmGatewayOperationsSnapshot, admGatewayOperationsStream as orvalAdmGatewayOperationsStream, admGatewayRequestConnectionTest as orvalAdmGatewayRequestConnectionTest, admGatewayRevalidateConnectionTest as orvalAdmGatewayRevalidateConnectionTest, admGatewaySaveBinding as orvalAdmGatewaySaveBinding, admGatewaySaveServerGroup as orvalAdmGatewaySaveServerGroup, admHealthInstanceDetail as orvalAdmHealthInstanceDetail, admHealthInstanceList as orvalAdmHealthInstanceList, admIncidentAcknowledge as orvalAdmIncidentAcknowledge, admIncidentCreateIncident as orvalAdmIncidentCreateIncident, admIncidentCreateMaintenance as orvalAdmIncidentCreateMaintenance, admIncidentCreatePolicy as orvalAdmIncidentCreatePolicy, admIncidentEscalate as orvalAdmIncidentEscalate, admIncidentFindIncident as orvalAdmIncidentFindIncident, admIncidentFindIncidents as orvalAdmIncidentFindIncidents, admIncidentFindMaintenance as orvalAdmIncidentFindMaintenance, admIncidentFindPolicies as orvalAdmIncidentFindPolicies, admIncidentFindTimeline as orvalAdmIncidentFindTimeline, admIncidentIngestSignal as orvalAdmIncidentIngestSignal, admIncidentRecordPostmortem as orvalAdmIncidentRecordPostmortem, admIncidentReopen as orvalAdmIncidentReopen, admIncidentResolve as orvalAdmIncidentResolve, admIncidentTransitionIncident as orvalAdmIncidentTransitionIncident, admIncidentUpdateMaintenance as orvalAdmIncidentUpdateMaintenance, admIncidentUpdatePolicy as orvalAdmIncidentUpdatePolicy, admIntegrationCryptoStatus as orvalAdmIntegrationCryptoStatus, admIntegrationDataQualityCorrectionApprovalRequest as orvalAdmIntegrationDataQualityCorrectionApprovalRequest, admIntegrationDataQualityCorrectionExecute as orvalAdmIntegrationDataQualityCorrectionExecute, admIntegrationDataQualityReplay as orvalAdmIntegrationDataQualityReplay, admIntegrationDataQualityValidate as orvalAdmIntegrationDataQualityValidate, admIntegrationTimeHealth as orvalAdmIntegrationTimeHealth, admIntegrationWebhookDlq as orvalAdmIntegrationWebhookDlq, admIntegrationWebhookReplay as orvalAdmIntegrationWebhookReplay, admLogExportCreate as orvalAdmLogExportCreate, admLogExportDownload as orvalAdmLogExportDownload, admLogFindLogs as orvalAdmLogFindLogs, admLogGetLogDetail as orvalAdmLogGetLogDetail, admLogPolicyAuditFindPolicyAudits as orvalAdmLogPolicyAuditFindPolicyAudits, admLogPolicyClearCache as orvalAdmLogPolicyClearCache, admLogPolicyCreateOverride as orvalAdmLogPolicyCreateOverride, admLogPolicyCreatePolicy as orvalAdmLogPolicyCreatePolicy, admLogPolicyCreateTraceBoost as orvalAdmLogPolicyCreateTraceBoost, admLogPolicyDisableOverride as orvalAdmLogPolicyDisableOverride, admLogPolicyDisablePolicy as orvalAdmLogPolicyDisablePolicy, admLogPolicyDistributionStatus as orvalAdmLogPolicyDistributionStatus, admLogPolicyFindPolicies as orvalAdmLogPolicyFindPolicies, admLogPolicyFindPolicy as orvalAdmLogPolicyFindPolicy, admLogPolicyFindTraceBoostHistory as orvalAdmLogPolicyFindTraceBoostHistory, admLogPolicyFindTraceBoostRuntimeState as orvalAdmLogPolicyFindTraceBoostRuntimeState, admLogPolicyRefreshCache as orvalAdmLogPolicyRefreshCache, admLogPolicyUpdatePolicy as orvalAdmLogPolicyUpdatePolicy, admMaintenanceExecuteAction as orvalAdmMaintenanceExecuteAction, admMaintenanceFindActions as orvalAdmMaintenanceFindActions, admMessageCreateMessage as orvalAdmMessageCreateMessage, admMessageDeleteMessage as orvalAdmMessageDeleteMessage, admMessageFindMessage as orvalAdmMessageFindMessage, admMessageFindMessages as orvalAdmMessageFindMessages, admMessageUpdateMessage as orvalAdmMessageUpdateMessage, admNotificationCancelDelivery as orvalAdmNotificationCancelDelivery, admNotificationDisableRule as orvalAdmNotificationDisableRule, admNotificationFindDeliveryAttempts as orvalAdmNotificationFindDeliveryAttempts, admNotificationFindDeliveryLogs as orvalAdmNotificationFindDeliveryLogs, admNotificationFindDlq as orvalAdmNotificationFindDlq, admNotificationFindRule as orvalAdmNotificationFindRule, admNotificationFindRules as orvalAdmNotificationFindRules, admNotificationRetryDelivery as orvalAdmNotificationRetryDelivery, admNotificationSaveRule as orvalAdmNotificationSaveRule, admNotificationSendTest as orvalAdmNotificationSendTest, admNotificationUpdateRule as orvalAdmNotificationUpdateRule, admOpenApiRefresh as orvalAdmOpenApiRefresh, admOpenApiStatus as orvalAdmOpenApiStatus, admOperationsGovernanceSnapshot as orvalAdmOperationsGovernanceSnapshot, admOperatorChangePassword as orvalAdmOperatorChangePassword, admOperatorCleanupExpiredSessions as orvalAdmOperatorCleanupExpiredSessions, admOperatorCreateOperator as orvalAdmOperatorCreateOperator, admOperatorFindCreateResult as orvalAdmOperatorFindCreateResult, admOperatorFindMenus as orvalAdmOperatorFindMenus, admOperatorFindOperators as orvalAdmOperatorFindOperators, admOperatorFindRoles as orvalAdmOperatorFindRoles, admOperatorFindSessions as orvalAdmOperatorFindSessions, admOperatorPasswordPolicy as orvalAdmOperatorPasswordPolicy, admOperatorRawContact as orvalAdmOperatorRawContact, admOperatorResetPassword as orvalAdmOperatorResetPassword, admOperatorRevokeSession as orvalAdmOperatorRevokeSession, admOperatorUnlockOperator as orvalAdmOperatorUnlockOperator, admOperatorUpdateContact as orvalAdmOperatorUpdateContact, admOperatorUpdateRoles as orvalAdmOperatorUpdateRoles, admOperatorUpdateStatus as orvalAdmOperatorUpdateStatus, admOperatorValidatePassword as orvalAdmOperatorValidatePassword, admParameterReferenceSearch as orvalAdmParameterReferenceSearch, admPermissionCreateApiPermission as orvalAdmPermissionCreateApiPermission, admPermissionCreateButton as orvalAdmPermissionCreateButton, admPermissionCreateMenu as orvalAdmPermissionCreateMenu, admPermissionCreateRole as orvalAdmPermissionCreateRole, admPermissionFindApiPermission as orvalAdmPermissionFindApiPermission, admPermissionFindApiPermissionMatrix as orvalAdmPermissionFindApiPermissionMatrix, admPermissionFindApiPermissions as orvalAdmPermissionFindApiPermissions, admPermissionFindButton as orvalAdmPermissionFindButton, admPermissionFindButtonMatrix as orvalAdmPermissionFindButtonMatrix, admPermissionFindButtons as orvalAdmPermissionFindButtons, admPermissionFindManagedMenu as orvalAdmPermissionFindManagedMenu, admPermissionFindManagedMenus as orvalAdmPermissionFindManagedMenus, admPermissionFindMenuMatrix as orvalAdmPermissionFindMenuMatrix, admPermissionFindRole as orvalAdmPermissionFindRole, admPermissionFindRoles as orvalAdmPermissionFindRoles, admPermissionUpdateApiPermission as orvalAdmPermissionUpdateApiPermission, admPermissionUpdateApiPermissionStatus as orvalAdmPermissionUpdateApiPermissionStatus, admPermissionUpdateButton as orvalAdmPermissionUpdateButton, admPermissionUpdateButtonPermission as orvalAdmPermissionUpdateButtonPermission, admPermissionUpdateButtonStatus as orvalAdmPermissionUpdateButtonStatus, admPermissionUpdateMenu as orvalAdmPermissionUpdateMenu, admPermissionUpdateMenuPermission as orvalAdmPermissionUpdateMenuPermission, admPermissionUpdateMenuStatus as orvalAdmPermissionUpdateMenuStatus, admPermissionUpdateRole as orvalAdmPermissionUpdateRole, admPermissionUpdateRoleApiPermission as orvalAdmPermissionUpdateRoleApiPermission, admPermissionUpdateRoleStatus as orvalAdmPermissionUpdateRoleStatus, admPlatformVersion as orvalAdmPlatformVersion, admRemoteLogBundleDownload as orvalAdmRemoteLogBundleDownload, admRemoteLogBundleDownloadTokenIssue as orvalAdmRemoteLogBundleDownloadTokenIssue, admRemoteLogBundleJobCreate as orvalAdmRemoteLogBundleJobCreate, admRemoteLogBundleJobDownload as orvalAdmRemoteLogBundleJobDownload, admRemoteLogBundleJobFind as orvalAdmRemoteLogBundleJobFind, admRemoteLogDiagnostics as orvalAdmRemoteLogDiagnostics, admRemoteLogDownload as orvalAdmRemoteLogDownload, admRemoteLogPreview as orvalAdmRemoteLogPreview, admRemoteLogSearch as orvalAdmRemoteLogSearch, admResiliencePolicyApprove as orvalAdmResiliencePolicyApprove, admResiliencePolicyFind as orvalAdmResiliencePolicyFind, admResiliencePolicyReject as orvalAdmResiliencePolicyReject, admResiliencePolicyRequest as orvalAdmResiliencePolicyRequest, admResiliencePolicySearch as orvalAdmResiliencePolicySearch, admResponseCodeCreate as orvalAdmResponseCodeCreate, admResponseCodeDelete as orvalAdmResponseCodeDelete, admResponseCodeFindAll as orvalAdmResponseCodeFindAll, admResponseCodeFindOne as orvalAdmResponseCodeFindOne, admResponseCodeUpdate as orvalAdmResponseCodeUpdate, admRuntimeControlCancelChange as orvalAdmRuntimeControlCancelChange, admRuntimeControlChangeGroupMember as orvalAdmRuntimeControlChangeGroupMember, admRuntimeControlCreateChange as orvalAdmRuntimeControlCreateChange, admRuntimeControlDeleteGroup as orvalAdmRuntimeControlDeleteGroup, admRuntimeControlFindByOperation as orvalAdmRuntimeControlFindByOperation, admRuntimeControlFindCapabilities as orvalAdmRuntimeControlFindCapabilities, admRuntimeControlFindChange as orvalAdmRuntimeControlFindChange, admRuntimeControlFindGroup as orvalAdmRuntimeControlFindGroup, admRuntimeControlFindHealth as orvalAdmRuntimeControlFindHealth, admRuntimeControlFindStateCatalog as orvalAdmRuntimeControlFindStateCatalog, admRuntimeControlFindStatus as orvalAdmRuntimeControlFindStatus, admRuntimeControlPreviewChange as orvalAdmRuntimeControlPreviewChange, admRuntimeControlPreviewTargets as orvalAdmRuntimeControlPreviewTargets, admRuntimeControlRollbackChange as orvalAdmRuntimeControlRollbackChange, admRuntimeControlSaveGroup as orvalAdmRuntimeControlSaveGroup, admRuntimeControlVerifyAudit as orvalAdmRuntimeControlVerifyAudit, admSecretFindMetadata as orvalAdmSecretFindMetadata, admSecretFindProviders as orvalAdmSecretFindProviders, admSecurityDisableMfa as orvalAdmSecurityDisableMfa, admSecurityFindIpAllowlist as orvalAdmSecurityFindIpAllowlist, admSecurityFindMfaStates as orvalAdmSecurityFindMfaStates, admSecurityRegisterMfa as orvalAdmSecurityRegisterMfa, admSecuritySaveIpAllowlist as orvalAdmSecuritySaveIpAllowlist, admSecurityVerifyMfa as orvalAdmSecurityVerifyMfa, admServiceRegistryCapabilities as orvalAdmServiceRegistryCapabilities, admServiceRegistryFindCallHistory as orvalAdmServiceRegistryFindCallHistory, admServiceRegistryFindCircuitStates as orvalAdmServiceRegistryFindCircuitStates, admServiceRegistryFindEndpoints as orvalAdmServiceRegistryFindEndpoints, admServiceRegistryFindHealth as orvalAdmServiceRegistryFindHealth, admServiceRegistryFindInstances as orvalAdmServiceRegistryFindInstances, admServiceRegistryFindRoutingPolicies as orvalAdmServiceRegistryFindRoutingPolicies, admServiceRegistryFindServices as orvalAdmServiceRegistryFindServices, admServiceRegistrySaveEndpoint as orvalAdmServiceRegistrySaveEndpoint, admServiceRegistrySaveInstance as orvalAdmServiceRegistrySaveInstance, admServiceRegistrySaveService as orvalAdmServiceRegistrySaveService, admStandardExecutionFindAll as orvalAdmStandardExecutionFindAll, admStandardExecutionFindOne as orvalAdmStandardExecutionFindOne, admTransactionGroupFindDetail as orvalAdmTransactionGroupFindDetail, admTransactionGroupFindExternalLogs as orvalAdmTransactionGroupFindExternalLogs, admTransactionGroupFindGroups as orvalAdmTransactionGroupFindGroups, admTransactionGroupFindHeaders as orvalAdmTransactionGroupFindHeaders, admTransactionGroupFindSegments as orvalAdmTransactionGroupFindSegments, admTransactionGroupFindTimeline as orvalAdmTransactionGroupFindTimeline, admTransactionMetaFindPage as orvalAdmTransactionMetaFindPage, admTransactionMetaFindTransaction as orvalAdmTransactionMetaFindTransaction, admTransactionMetaFindTransactions as orvalAdmTransactionMetaFindTransactions, admTransactionMetaInactivate as orvalAdmTransactionMetaInactivate, admTransactionMetaScan as orvalAdmTransactionMetaScan, findAdmBatchJobInstanceLogs as orvalFindAdmBatchJobInstanceLogs, findAdmBrokerDlq as orvalFindAdmBrokerDlq, findAdmBrokerInbox as orvalFindAdmBrokerInbox, findAdmBrokerOutbox as orvalFindAdmBrokerOutbox, findAdmFileTransferHistory as orvalFindAdmFileTransferHistory, findAdmIdempotencyRecords as orvalFindAdmIdempotencyRecords, findAdmUnknownResults as orvalFindAdmUnknownResults, getAdmBatchJobInstanceLog as orvalGetAdmBatchJobInstanceLog, getAdmFileLogRecoveryStatus as orvalGetAdmFileLogRecoveryStatus, getAdmLiveness as orvalGetAdmLiveness, getAdmReadiness as orvalGetAdmReadiness, getAdmSystemVersion as orvalGetAdmSystemVersion, getAdmTransactionLogRecoveryStatus as orvalGetAdmTransactionLogRecoveryStatus, requestAdmBrokerDlqReplay as orvalRequestAdmBrokerDlqReplay, resolveAdmUnknownResult as orvalResolveAdmUnknownResult, retryAdmTraceRecoveryPoison as orvalRetryAdmTraceRecoveryPoison, runAdmTransactionLogRecovery as orvalRunAdmTransactionLogRecovery, traceAdmByBusinessTransactionId as orvalTraceAdmByBusinessTransactionId, traceAdmByTraceId as orvalTraceAdmByTraceId, traceAdmByTransactionId as orvalTraceAdmByTransactionId } from "./orval/cpf-api";

export type CpfGeneratedHeaders = HeadersInit | Record<string, string>;
export interface CpfGeneratedBaseOptions { signal?: AbortSignal; headers?: CpfGeneratedHeaders; }
function headerValue(headers: CpfGeneratedHeaders | undefined, name: string): string | undefined { if (!headers) return undefined; if (headers instanceof Headers) return headers.get(name) ?? undefined; if (Array.isArray(headers)) { const found = headers.find(([key]) => String(key).toLowerCase() === name.toLowerCase()); return found ? String(found[1]) : undefined; } for (const [key,value] of Object.entries(headers)) if (key.toLowerCase() === name.toLowerCase()) return String(value); return undefined; }

export type AdmApprovalDecisionBody = { action: string; breakGlass?: boolean; idempotencyKey: string; reason: string };
export type AdmApprovalDecisionPath = { id: number };
export type AdmApprovalDecisionQuery = Record<string, never>;
export type AdmApprovalDecisionHeaders = Record<string, never>;
export type AdmApprovalDecisionResponse = Record<string, unknown>;
export type AdmApprovalDecisionOptions = CpfGeneratedBaseOptions & { data: AdmApprovalDecisionBody; path: AdmApprovalDecisionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admApprovalDecision<T = AdmApprovalDecisionResponse>(options: AdmApprovalDecisionOptions): Promise<T> {
  const response = await orvalAdmApprovalDecision(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalExecuteBody = never;
export type AdmApprovalExecutePath = { id: number };
export type AdmApprovalExecuteQuery = { reason: string };
export type AdmApprovalExecuteHeaders = Record<string, never>;
export type AdmApprovalExecuteResponse = Record<string, unknown>;
export type AdmApprovalExecuteOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmApprovalExecutePath; query?: AdmApprovalExecuteQuery; headers?: CpfGeneratedHeaders; };
export async function admApprovalExecute<T = AdmApprovalExecuteResponse>(options: AdmApprovalExecuteOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmApprovalExecute(options.path["id"], contractParams as Parameters<typeof orvalAdmApprovalExecute>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalPoliciesBody = never;
export type AdmApprovalPoliciesPath = Record<string, never>;
export type AdmApprovalPoliciesQuery = { actionType?: string };
export type AdmApprovalPoliciesHeaders = Record<string, never>;
export type AdmApprovalPoliciesResponse = Record<string, unknown>;
export type AdmApprovalPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmApprovalPoliciesQuery; headers?: CpfGeneratedHeaders; };
export async function admApprovalPolicies<T = AdmApprovalPoliciesResponse>(options: AdmApprovalPoliciesOptions = {} as AdmApprovalPoliciesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmApprovalPolicies(contractParams as Parameters<typeof orvalAdmApprovalPolicies>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalPolicyDetailBody = never;
export type AdmApprovalPolicyDetailPath = { policyCode: string; version: number };
export type AdmApprovalPolicyDetailQuery = Record<string, never>;
export type AdmApprovalPolicyDetailHeaders = Record<string, never>;
export type AdmApprovalPolicyDetailResponse = Record<string, unknown>;
export type AdmApprovalPolicyDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmApprovalPolicyDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admApprovalPolicyDetail<T = AdmApprovalPolicyDetailResponse>(options: AdmApprovalPolicyDetailOptions): Promise<T> {
  const response = await orvalAdmApprovalPolicyDetail(options.path["policyCode"], options.path["version"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalPolicySaveBody = { actionType: string; breakGlassAllowedYn?: string; description?: string; effectiveFrom?: string; effectiveTo?: string; enabledYn?: string; policyCode: string; policyName: string; policyVersion?: number; reason: string; selfApprovalAllowedYn?: string; steps: Array<{ decisionRule?: string; requiredCount?: number; requiredYn?: string; stepNo?: number; stepType?: string; targetCode: string; targetType: string }> };
export type AdmApprovalPolicySavePath = Record<string, never>;
export type AdmApprovalPolicySaveQuery = Record<string, never>;
export type AdmApprovalPolicySaveHeaders = Record<string, never>;
export type AdmApprovalPolicySaveResponse = Record<string, unknown>;
export type AdmApprovalPolicySaveOptions = CpfGeneratedBaseOptions & { data: AdmApprovalPolicySaveBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admApprovalPolicySave<T = AdmApprovalPolicySaveResponse>(options: AdmApprovalPolicySaveOptions): Promise<T> {
  const response = await orvalAdmApprovalPolicySave(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalReconcileBody = never;
export type AdmApprovalReconcilePath = { id: number };
export type AdmApprovalReconcileQuery = { reason: string };
export type AdmApprovalReconcileHeaders = Record<string, never>;
export type AdmApprovalReconcileResponse = Record<string, unknown>;
export type AdmApprovalReconcileOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmApprovalReconcilePath; query?: AdmApprovalReconcileQuery; headers?: CpfGeneratedHeaders; };
export async function admApprovalReconcile<T = AdmApprovalReconcileResponse>(options: AdmApprovalReconcileOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmApprovalReconcile(options.path["id"], contractParams as Parameters<typeof orvalAdmApprovalReconcile>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalRequestBody = { actionType: string; expireAt?: string; ownerCommand: string; ownerModule: string; payloadSnapshot: string; policyCode?: string; policyVersion?: number; reason: string; requestKey: string; targetId: string; targetType: string };
export type AdmApprovalRequestPath = Record<string, never>;
export type AdmApprovalRequestQuery = Record<string, never>;
export type AdmApprovalRequestHeaders = Record<string, never>;
export type AdmApprovalRequestResponse = Record<string, unknown>;
export type AdmApprovalRequestOptions = CpfGeneratedBaseOptions & { data: AdmApprovalRequestBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admApprovalRequest<T = AdmApprovalRequestResponse>(options: AdmApprovalRequestOptions): Promise<T> {
  const response = await orvalAdmApprovalRequest(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmApprovalRequestDetailBody = never;
export type AdmApprovalRequestDetailPath = { id: number };
export type AdmApprovalRequestDetailQuery = Record<string, never>;
export type AdmApprovalRequestDetailHeaders = Record<string, never>;
export type AdmApprovalRequestDetailResponse = Record<string, unknown>;
export type AdmApprovalRequestDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmApprovalRequestDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admApprovalRequestDetail<T = AdmApprovalRequestDetailResponse>(options: AdmApprovalRequestDetailOptions): Promise<T> {
  const response = await orvalAdmApprovalRequestDetail(options.path["id"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuditDeliveryListBody = never;
export type AdmAuditDeliveryListPath = Record<string, never>;
export type AdmAuditDeliveryListQuery = { deliveryStatus?: string; limit?: number };
export type AdmAuditDeliveryListHeaders = Record<string, never>;
export type AdmAuditDeliveryListResponse = Record<string, unknown>;
export type AdmAuditDeliveryListOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmAuditDeliveryListQuery; headers?: CpfGeneratedHeaders; };
export async function admAuditDeliveryList<T = AdmAuditDeliveryListResponse>(options: AdmAuditDeliveryListOptions = {} as AdmAuditDeliveryListOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmAuditDeliveryList(contractParams as Parameters<typeof orvalAdmAuditDeliveryList>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuditDeliveryRetryBody = never;
export type AdmAuditDeliveryRetryPath = { deliveryId: number };
export type AdmAuditDeliveryRetryQuery = { reason: string };
export type AdmAuditDeliveryRetryHeaders = Record<string, never>;
export type AdmAuditDeliveryRetryResponse = Record<string, unknown>;
export type AdmAuditDeliveryRetryOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmAuditDeliveryRetryPath; query?: AdmAuditDeliveryRetryQuery; headers?: CpfGeneratedHeaders; };
export async function admAuditDeliveryRetry<T = AdmAuditDeliveryRetryResponse>(options: AdmAuditDeliveryRetryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmAuditDeliveryRetry(options.path["deliveryId"], contractParams as Parameters<typeof orvalAdmAuditDeliveryRetry>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuditLogFindAuditLogsBody = never;
export type AdmAuditLogFindAuditLogsPath = Record<string, never>;
export type AdmAuditLogFindAuditLogsQuery = { operatorId?: string; actionType?: string; targetType?: string; targetId?: string; limit?: number };
export type AdmAuditLogFindAuditLogsHeaders = Record<string, never>;
export type AdmAuditLogFindAuditLogsResponse = Record<string, unknown>;
export type AdmAuditLogFindAuditLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmAuditLogFindAuditLogsQuery; headers?: CpfGeneratedHeaders; };
export async function admAuditLogFindAuditLogs<T = AdmAuditLogFindAuditLogsResponse>(options: AdmAuditLogFindAuditLogsOptions = {} as AdmAuditLogFindAuditLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmAuditLogFindAuditLogs(contractParams as Parameters<typeof orvalAdmAuditLogFindAuditLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuthLoginBody = { operatorId?: string; otpCode?: string; password?: string };
export type AdmAuthLoginPath = Record<string, never>;
export type AdmAuthLoginQuery = Record<string, never>;
export type AdmAuthLoginHeaders = Record<string, never>;
export type AdmAuthLoginResponse = Record<string, unknown>;
export type AdmAuthLoginOptions = CpfGeneratedBaseOptions & { data: AdmAuthLoginBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admAuthLogin<T = AdmAuthLoginResponse>(options: AdmAuthLoginOptions): Promise<T> {
  const response = await orvalAdmAuthLogin(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuthLogoutBody = never;
export type AdmAuthLogoutPath = Record<string, never>;
export type AdmAuthLogoutQuery = Record<string, never>;
export type AdmAuthLogoutHeaders = Record<string, never>;
export type AdmAuthLogoutResponse = Record<string, unknown>;
export type AdmAuthLogoutOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admAuthLogout<T = AdmAuthLogoutResponse>(options: AdmAuthLogoutOptions = {} as AdmAuthLogoutOptions): Promise<T> {
  const response = await orvalAdmAuthLogout({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmAuthMeBody = never;
export type AdmAuthMePath = Record<string, never>;
export type AdmAuthMeQuery = Record<string, never>;
export type AdmAuthMeHeaders = Record<string, never>;
export type AdmAuthMeResponse = Record<string, unknown>;
export type AdmAuthMeOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admAuthMe<T = AdmAuthMeResponse>(options: AdmAuthMeOptions = {} as AdmAuthMeOptions): Promise<T> {
  const response = await orvalAdmAuthMe({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchActGhostExecutionBody = { actionType?: string; approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; reason?: string };
export type AdmBatchActGhostExecutionPath = { executionId: number };
export type AdmBatchActGhostExecutionQuery = Record<string, never>;
export type AdmBatchActGhostExecutionHeaders = Record<string, never>;
export type AdmBatchActGhostExecutionResponse = Record<string, unknown>;
export type AdmBatchActGhostExecutionOptions = CpfGeneratedBaseOptions & { data: AdmBatchActGhostExecutionBody; path: AdmBatchActGhostExecutionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchActGhostExecution<T = AdmBatchActGhostExecutionResponse>(options: AdmBatchActGhostExecutionOptions): Promise<T> {
  const response = await orvalAdmBatchActGhostExecution(options.path["executionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchDisableScheduleBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchDisableSchedulePath = { scheduleId: string };
export type AdmBatchDisableScheduleQuery = Record<string, never>;
export type AdmBatchDisableScheduleHeaders = Record<string, never>;
export type AdmBatchDisableScheduleResponse = Record<string, unknown>;
export type AdmBatchDisableScheduleOptions = CpfGeneratedBaseOptions & { data: AdmBatchDisableScheduleBody; path: AdmBatchDisableSchedulePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchDisableSchedule<T = AdmBatchDisableScheduleResponse>(options: AdmBatchDisableScheduleOptions): Promise<T> {
  const response = await orvalAdmBatchDisableSchedule(options.path["scheduleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchEnableScheduleBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchEnableSchedulePath = { scheduleId: string };
export type AdmBatchEnableScheduleQuery = Record<string, never>;
export type AdmBatchEnableScheduleHeaders = Record<string, never>;
export type AdmBatchEnableScheduleResponse = Record<string, unknown>;
export type AdmBatchEnableScheduleOptions = CpfGeneratedBaseOptions & { data: AdmBatchEnableScheduleBody; path: AdmBatchEnableSchedulePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchEnableSchedule<T = AdmBatchEnableScheduleResponse>(options: AdmBatchEnableScheduleOptions): Promise<T> {
  const response = await orvalAdmBatchEnableSchedule(options.path["scheduleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindExecutionDetailBody = never;
export type AdmBatchFindExecutionDetailPath = { executionId: number };
export type AdmBatchFindExecutionDetailQuery = Record<string, never>;
export type AdmBatchFindExecutionDetailHeaders = Record<string, never>;
export type AdmBatchFindExecutionDetailResponse = Record<string, unknown>;
export type AdmBatchFindExecutionDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchFindExecutionDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchFindExecutionDetail<T = AdmBatchFindExecutionDetailResponse>(options: AdmBatchFindExecutionDetailOptions): Promise<T> {
  const response = await orvalAdmBatchFindExecutionDetail(options.path["executionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindExecutionPageBody = never;
export type AdmBatchFindExecutionPagePath = Record<string, never>;
export type AdmBatchFindExecutionPageQuery = { jobId?: string; transactionId?: string; springBatchJobInstanceId?: number; workerId?: string; serverInstanceId?: string; status?: string; fromDate?: string; toDate?: string; page?: number; size?: number };
export type AdmBatchFindExecutionPageHeaders = Record<string, never>;
export type AdmBatchFindExecutionPageResponse = Record<string, unknown>;
export type AdmBatchFindExecutionPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindExecutionPageQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindExecutionPage<T = AdmBatchFindExecutionPageResponse>(options: AdmBatchFindExecutionPageOptions = {} as AdmBatchFindExecutionPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindExecutionPage(contractParams as Parameters<typeof orvalAdmBatchFindExecutionPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindExecutionsBody = never;
export type AdmBatchFindExecutionsPath = Record<string, never>;
export type AdmBatchFindExecutionsQuery = { jobId?: string; transactionId?: string; springBatchJobInstanceId?: number; workerId?: string; serverInstanceId?: string; limit?: number };
export type AdmBatchFindExecutionsHeaders = Record<string, never>;
export type AdmBatchFindExecutionsResponse = Record<string, unknown>;
export type AdmBatchFindExecutionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindExecutionsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindExecutions<T = AdmBatchFindExecutionsResponse>(options: AdmBatchFindExecutionsOptions = {} as AdmBatchFindExecutionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindExecutions(contractParams as Parameters<typeof orvalAdmBatchFindExecutions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindExecutionTargetsBody = never;
export type AdmBatchFindExecutionTargetsPath = Record<string, never>;
export type AdmBatchFindExecutionTargetsQuery = { jobId?: string; dispatchStatus?: string; limit?: number };
export type AdmBatchFindExecutionTargetsHeaders = Record<string, never>;
export type AdmBatchFindExecutionTargetsResponse = Record<string, unknown>;
export type AdmBatchFindExecutionTargetsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindExecutionTargetsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindExecutionTargets<T = AdmBatchFindExecutionTargetsResponse>(options: AdmBatchFindExecutionTargetsOptions = {} as AdmBatchFindExecutionTargetsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindExecutionTargets(contractParams as Parameters<typeof orvalAdmBatchFindExecutionTargets>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindGhostCandidatesBody = never;
export type AdmBatchFindGhostCandidatesPath = Record<string, never>;
export type AdmBatchFindGhostCandidatesQuery = { heartbeatTimeoutSeconds?: number };
export type AdmBatchFindGhostCandidatesHeaders = Record<string, never>;
export type AdmBatchFindGhostCandidatesResponse = Record<string, unknown>;
export type AdmBatchFindGhostCandidatesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindGhostCandidatesQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindGhostCandidates<T = AdmBatchFindGhostCandidatesResponse>(options: AdmBatchFindGhostCandidatesOptions = {} as AdmBatchFindGhostCandidatesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindGhostCandidates(contractParams as Parameters<typeof orvalAdmBatchFindGhostCandidates>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindInstancesBody = never;
export type AdmBatchFindInstancesPath = Record<string, never>;
export type AdmBatchFindInstancesQuery = Record<string, never>;
export type AdmBatchFindInstancesHeaders = Record<string, never>;
export type AdmBatchFindInstancesResponse = Record<string, unknown>;
export type AdmBatchFindInstancesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchFindInstances<T = AdmBatchFindInstancesResponse>(options: AdmBatchFindInstancesOptions = {} as AdmBatchFindInstancesOptions): Promise<T> {
  const response = await orvalAdmBatchFindInstances({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindJobDetailBody = never;
export type AdmBatchFindJobDetailPath = { jobId: string };
export type AdmBatchFindJobDetailQuery = Record<string, never>;
export type AdmBatchFindJobDetailHeaders = Record<string, never>;
export type AdmBatchFindJobDetailResponse = Record<string, unknown>;
export type AdmBatchFindJobDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchFindJobDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchFindJobDetail<T = AdmBatchFindJobDetailResponse>(options: AdmBatchFindJobDetailOptions): Promise<T> {
  const response = await orvalAdmBatchFindJobDetail(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindJobsBody = never;
export type AdmBatchFindJobsPath = Record<string, never>;
export type AdmBatchFindJobsQuery = Record<string, never>;
export type AdmBatchFindJobsHeaders = Record<string, never>;
export type AdmBatchFindJobsResponse = Record<string, unknown>;
export type AdmBatchFindJobsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchFindJobs<T = AdmBatchFindJobsResponse>(options: AdmBatchFindJobsOptions = {} as AdmBatchFindJobsOptions): Promise<T> {
  const response = await orvalAdmBatchFindJobs({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindLocksBody = never;
export type AdmBatchFindLocksPath = Record<string, never>;
export type AdmBatchFindLocksQuery = { jobId?: string };
export type AdmBatchFindLocksHeaders = Record<string, never>;
export type AdmBatchFindLocksResponse = Record<string, unknown>;
export type AdmBatchFindLocksOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindLocksQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindLocks<T = AdmBatchFindLocksResponse>(options: AdmBatchFindLocksOptions = {} as AdmBatchFindLocksOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindLocks(contractParams as Parameters<typeof orvalAdmBatchFindLocks>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindOperationLogsBody = never;
export type AdmBatchFindOperationLogsPath = Record<string, never>;
export type AdmBatchFindOperationLogsQuery = { jobId?: string; executionId?: number; limit?: number };
export type AdmBatchFindOperationLogsHeaders = Record<string, never>;
export type AdmBatchFindOperationLogsResponse = Record<string, unknown>;
export type AdmBatchFindOperationLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindOperationLogsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindOperationLogs<T = AdmBatchFindOperationLogsResponse>(options: AdmBatchFindOperationLogsOptions = {} as AdmBatchFindOperationLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindOperationLogs(contractParams as Parameters<typeof orvalAdmBatchFindOperationLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindRelationsBody = never;
export type AdmBatchFindRelationsPath = Record<string, never>;
export type AdmBatchFindRelationsQuery = { jobId?: string };
export type AdmBatchFindRelationsHeaders = Record<string, never>;
export type AdmBatchFindRelationsResponse = Record<string, unknown>;
export type AdmBatchFindRelationsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindRelationsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindRelations<T = AdmBatchFindRelationsResponse>(options: AdmBatchFindRelationsOptions = {} as AdmBatchFindRelationsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindRelations(contractParams as Parameters<typeof orvalAdmBatchFindRelations>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindSchedulesBody = never;
export type AdmBatchFindSchedulesPath = Record<string, never>;
export type AdmBatchFindSchedulesQuery = Record<string, never>;
export type AdmBatchFindSchedulesHeaders = Record<string, never>;
export type AdmBatchFindSchedulesResponse = Record<string, unknown>;
export type AdmBatchFindSchedulesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchFindSchedules<T = AdmBatchFindSchedulesResponse>(options: AdmBatchFindSchedulesOptions = {} as AdmBatchFindSchedulesOptions): Promise<T> {
  const response = await orvalAdmBatchFindSchedules({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindStepExecutionsBody = never;
export type AdmBatchFindStepExecutionsPath = Record<string, never>;
export type AdmBatchFindStepExecutionsQuery = { executionId?: number; jobId?: string; limit?: number };
export type AdmBatchFindStepExecutionsHeaders = Record<string, never>;
export type AdmBatchFindStepExecutionsResponse = Record<string, unknown>;
export type AdmBatchFindStepExecutionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindStepExecutionsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindStepExecutions<T = AdmBatchFindStepExecutionsResponse>(options: AdmBatchFindStepExecutionsOptions = {} as AdmBatchFindStepExecutionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindStepExecutions(contractParams as Parameters<typeof orvalAdmBatchFindStepExecutions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchFindWorkersBody = never;
export type AdmBatchFindWorkersPath = Record<string, never>;
export type AdmBatchFindWorkersQuery = { heartbeatTimeoutSeconds?: number };
export type AdmBatchFindWorkersHeaders = Record<string, never>;
export type AdmBatchFindWorkersResponse = Record<string, unknown>;
export type AdmBatchFindWorkersOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchFindWorkersQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchFindWorkers<T = AdmBatchFindWorkersResponse>(options: AdmBatchFindWorkersOptions = {} as AdmBatchFindWorkersOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchFindWorkers(contractParams as Parameters<typeof orvalAdmBatchFindWorkers>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchJobDefinitionDetailBody = never;
export type AdmBatchJobDefinitionDetailPath = { jobId: string; version: number };
export type AdmBatchJobDefinitionDetailQuery = Record<string, never>;
export type AdmBatchJobDefinitionDetailHeaders = Record<string, never>;
export type AdmBatchJobDefinitionDetailResponse = Record<string, unknown>;
export type AdmBatchJobDefinitionDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchJobDefinitionDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchJobDefinitionDetail<T = AdmBatchJobDefinitionDetailResponse>(options: AdmBatchJobDefinitionDetailOptions): Promise<T> {
  const response = await orvalAdmBatchJobDefinitionDetail(options.path["jobId"], options.path["version"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchJobDefinitionsBody = never;
export type AdmBatchJobDefinitionsPath = Record<string, never>;
export type AdmBatchJobDefinitionsQuery = { jobId?: string; state?: string; limit?: number };
export type AdmBatchJobDefinitionsHeaders = Record<string, never>;
export type AdmBatchJobDefinitionsResponse = Record<string, unknown>;
export type AdmBatchJobDefinitionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchJobDefinitionsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchJobDefinitions<T = AdmBatchJobDefinitionsResponse>(options: AdmBatchJobDefinitionsOptions = {} as AdmBatchJobDefinitionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchJobDefinitions(contractParams as Parameters<typeof orvalAdmBatchJobDefinitions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchJobDefinitionSaveBody = Record<string, unknown>;
export type AdmBatchJobDefinitionSavePath = Record<string, never>;
export type AdmBatchJobDefinitionSaveQuery = Record<string, never>;
export type AdmBatchJobDefinitionSaveHeaders = Record<string, never>;
export type AdmBatchJobDefinitionSaveResponse = Record<string, unknown>;
export type AdmBatchJobDefinitionSaveOptions = CpfGeneratedBaseOptions & { data: AdmBatchJobDefinitionSaveBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchJobDefinitionSave<T = AdmBatchJobDefinitionSaveResponse>(options: AdmBatchJobDefinitionSaveOptions): Promise<T> {
  const response = await orvalAdmBatchJobDefinitionSave(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchJobDefinitionTransitionBody = Record<string, unknown>;
export type AdmBatchJobDefinitionTransitionPath = { jobId: string; version: number };
export type AdmBatchJobDefinitionTransitionQuery = Record<string, never>;
export type AdmBatchJobDefinitionTransitionHeaders = Record<string, never>;
export type AdmBatchJobDefinitionTransitionResponse = Record<string, unknown>;
export type AdmBatchJobDefinitionTransitionOptions = CpfGeneratedBaseOptions & { data: AdmBatchJobDefinitionTransitionBody; path: AdmBatchJobDefinitionTransitionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchJobDefinitionTransition<T = AdmBatchJobDefinitionTransitionResponse>(options: AdmBatchJobDefinitionTransitionOptions): Promise<T> {
  const response = await orvalAdmBatchJobDefinitionTransition(options.path["jobId"], options.path["version"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchJobDefinitionValidateBody = Record<string, unknown>;
export type AdmBatchJobDefinitionValidatePath = Record<string, never>;
export type AdmBatchJobDefinitionValidateQuery = Record<string, never>;
export type AdmBatchJobDefinitionValidateHeaders = Record<string, never>;
export type AdmBatchJobDefinitionValidateResponse = Record<string, unknown>;
export type AdmBatchJobDefinitionValidateOptions = CpfGeneratedBaseOptions & { data: AdmBatchJobDefinitionValidateBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchJobDefinitionValidate<T = AdmBatchJobDefinitionValidateResponse>(options: AdmBatchJobDefinitionValidateOptions): Promise<T> {
  const response = await orvalAdmBatchJobDefinitionValidate(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRegisterJobBody = { description?: string; jobId?: string; jobName?: string; jobType?: string; reason?: string };
export type AdmBatchRegisterJobPath = Record<string, never>;
export type AdmBatchRegisterJobQuery = Record<string, never>;
export type AdmBatchRegisterJobHeaders = Record<string, never>;
export type AdmBatchRegisterJobResponse = Record<string, unknown>;
export type AdmBatchRegisterJobOptions = CpfGeneratedBaseOptions & { data: AdmBatchRegisterJobBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRegisterJob<T = AdmBatchRegisterJobResponse>(options: AdmBatchRegisterJobOptions): Promise<T> {
  const response = await orvalAdmBatchRegisterJob(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchReleaseLockBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; lockKey?: string; reason?: string };
export type AdmBatchReleaseLockPath = Record<string, never>;
export type AdmBatchReleaseLockQuery = Record<string, never>;
export type AdmBatchReleaseLockHeaders = Record<string, never>;
export type AdmBatchReleaseLockResponse = Record<string, unknown>;
export type AdmBatchReleaseLockOptions = CpfGeneratedBaseOptions & { data: AdmBatchReleaseLockBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchReleaseLock<T = AdmBatchReleaseLockResponse>(options: AdmBatchReleaseLockOptions): Promise<T> {
  const response = await orvalAdmBatchReleaseLock(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRetryExecutionBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchRetryExecutionPath = { executionId: number };
export type AdmBatchRetryExecutionQuery = Record<string, never>;
export type AdmBatchRetryExecutionHeaders = Record<string, never>;
export type AdmBatchRetryExecutionResponse = Record<string, unknown>;
export type AdmBatchRetryExecutionOptions = CpfGeneratedBaseOptions & { data: AdmBatchRetryExecutionBody; path: AdmBatchRetryExecutionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRetryExecution<T = AdmBatchRetryExecutionResponse>(options: AdmBatchRetryExecutionOptions): Promise<T> {
  const response = await orvalAdmBatchRetryExecution(options.path["executionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRunJobBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchRunJobPath = { jobId: string };
export type AdmBatchRunJobQuery = Record<string, never>;
export type AdmBatchRunJobHeaders = Record<string, never>;
export type AdmBatchRunJobResponse = Record<string, unknown>;
export type AdmBatchRunJobOptions = CpfGeneratedBaseOptions & { data: AdmBatchRunJobBody; path: AdmBatchRunJobPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRunJob<T = AdmBatchRunJobResponse>(options: AdmBatchRunJobOptions): Promise<T> {
  const response = await orvalAdmBatchRunJob(options.path["jobId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRunSchedulerOnceBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchRunSchedulerOncePath = Record<string, never>;
export type AdmBatchRunSchedulerOnceQuery = Record<string, never>;
export type AdmBatchRunSchedulerOnceHeaders = Record<string, never>;
export type AdmBatchRunSchedulerOnceResponse = Record<string, unknown>;
export type AdmBatchRunSchedulerOnceOptions = CpfGeneratedBaseOptions & { data: AdmBatchRunSchedulerOnceBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRunSchedulerOnce<T = AdmBatchRunSchedulerOnceResponse>(options: AdmBatchRunSchedulerOnceOptions): Promise<T> {
  const response = await orvalAdmBatchRunSchedulerOnce(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRuntimeCommandBody = Record<string, unknown>;
export type AdmBatchRuntimeCommandPath = Record<string, never>;
export type AdmBatchRuntimeCommandQuery = Record<string, never>;
export type AdmBatchRuntimeCommandHeaders = Record<string, never>;
export type AdmBatchRuntimeCommandResponse = Record<string, unknown>;
export type AdmBatchRuntimeCommandOptions = CpfGeneratedBaseOptions & { data: AdmBatchRuntimeCommandBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRuntimeCommand<T = AdmBatchRuntimeCommandResponse>(options: AdmBatchRuntimeCommandOptions): Promise<T> {
  const response = await orvalAdmBatchRuntimeCommand(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRuntimeCommandStateBody = never;
export type AdmBatchRuntimeCommandStatePath = { key: string };
export type AdmBatchRuntimeCommandStateQuery = Record<string, never>;
export type AdmBatchRuntimeCommandStateHeaders = Record<string, never>;
export type AdmBatchRuntimeCommandStateResponse = Record<string, unknown>;
export type AdmBatchRuntimeCommandStateOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchRuntimeCommandStatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRuntimeCommandState<T = AdmBatchRuntimeCommandStateResponse>(options: AdmBatchRuntimeCommandStateOptions): Promise<T> {
  const response = await orvalAdmBatchRuntimeCommandState(options.path["key"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRuntimeCreateDeploymentPlanBody = Record<string, unknown>;
export type AdmBatchRuntimeCreateDeploymentPlanPath = Record<string, never>;
export type AdmBatchRuntimeCreateDeploymentPlanQuery = Record<string, never>;
export type AdmBatchRuntimeCreateDeploymentPlanHeaders = Record<string, never>;
export type AdmBatchRuntimeCreateDeploymentPlanResponse = Record<string, unknown>;
export type AdmBatchRuntimeCreateDeploymentPlanOptions = CpfGeneratedBaseOptions & { data: AdmBatchRuntimeCreateDeploymentPlanBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRuntimeCreateDeploymentPlan<T = AdmBatchRuntimeCreateDeploymentPlanResponse>(options: AdmBatchRuntimeCreateDeploymentPlanOptions): Promise<T> {
  const response = await orvalAdmBatchRuntimeCreateDeploymentPlan(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRuntimeInstancesBody = never;
export type AdmBatchRuntimeInstancesPath = Record<string, never>;
export type AdmBatchRuntimeInstancesQuery = { staleAfterSeconds?: number };
export type AdmBatchRuntimeInstancesHeaders = Record<string, never>;
export type AdmBatchRuntimeInstancesResponse = Record<string, unknown>;
export type AdmBatchRuntimeInstancesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchRuntimeInstancesQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchRuntimeInstances<T = AdmBatchRuntimeInstancesResponse>(options: AdmBatchRuntimeInstancesOptions = {} as AdmBatchRuntimeInstancesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchRuntimeInstances(contractParams as Parameters<typeof orvalAdmBatchRuntimeInstances>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchRuntimeViewBody = never;
export type AdmBatchRuntimeViewPath = { view: string };
export type AdmBatchRuntimeViewQuery = Record<string, never>;
export type AdmBatchRuntimeViewHeaders = Record<string, never>;
export type AdmBatchRuntimeViewResponse = Record<string, unknown>;
export type AdmBatchRuntimeViewOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchRuntimeViewPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchRuntimeView<T = AdmBatchRuntimeViewResponse>(options: AdmBatchRuntimeViewOptions): Promise<T> {
  const response = await orvalAdmBatchRuntimeView(options.path["view"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchSimulateScheduleBody = never;
export type AdmBatchSimulateSchedulePath = { scheduleId: string };
export type AdmBatchSimulateScheduleQuery = { baseDate?: string; days?: number };
export type AdmBatchSimulateScheduleHeaders = Record<string, never>;
export type AdmBatchSimulateScheduleResponse = Record<string, unknown>;
export type AdmBatchSimulateScheduleOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchSimulateSchedulePath; query?: AdmBatchSimulateScheduleQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchSimulateSchedule<T = AdmBatchSimulateScheduleResponse>(options: AdmBatchSimulateScheduleOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchSimulateSchedule(options.path["scheduleId"], contractParams as Parameters<typeof orvalAdmBatchSimulateSchedule>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchStopExecutionBody = { approvalRequestId?: string; expectedVersion?: number; idempotencyKey?: string; jobParameters?: string; reason?: string };
export type AdmBatchStopExecutionPath = { executionId: number };
export type AdmBatchStopExecutionQuery = Record<string, never>;
export type AdmBatchStopExecutionHeaders = Record<string, never>;
export type AdmBatchStopExecutionResponse = Record<string, unknown>;
export type AdmBatchStopExecutionOptions = CpfGeneratedBaseOptions & { data: AdmBatchStopExecutionBody; path: AdmBatchStopExecutionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchStopExecution<T = AdmBatchStopExecutionResponse>(options: AdmBatchStopExecutionOptions): Promise<T> {
  const response = await orvalAdmBatchStopExecution(options.path["executionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchExecutionDetailBody = never;
export type AdmBatchWorkbenchExecutionDetailPath = { executionId: number };
export type AdmBatchWorkbenchExecutionDetailQuery = Record<string, never>;
export type AdmBatchWorkbenchExecutionDetailHeaders = Record<string, never>;
export type AdmBatchWorkbenchExecutionDetailResponse = Record<string, unknown>;
export type AdmBatchWorkbenchExecutionDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchWorkbenchExecutionDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchExecutionDetail<T = AdmBatchWorkbenchExecutionDetailResponse>(options: AdmBatchWorkbenchExecutionDetailOptions): Promise<T> {
  const response = await orvalAdmBatchWorkbenchExecutionDetail(options.path["executionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchExecutionsBody = never;
export type AdmBatchWorkbenchExecutionsPath = Record<string, never>;
export type AdmBatchWorkbenchExecutionsQuery = { jobId?: string; transactionId?: string; springBatchJobInstanceId?: number; status?: string; workerId?: string; serverInstanceId?: string; fromDate?: string; toDate?: string; page?: number; size?: number };
export type AdmBatchWorkbenchExecutionsHeaders = Record<string, never>;
export type AdmBatchWorkbenchExecutionsResponse = Record<string, unknown>;
export type AdmBatchWorkbenchExecutionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchWorkbenchExecutionsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchExecutions<T = AdmBatchWorkbenchExecutionsResponse>(options: AdmBatchWorkbenchExecutionsOptions = {} as AdmBatchWorkbenchExecutionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchWorkbenchExecutions(contractParams as Parameters<typeof orvalAdmBatchWorkbenchExecutions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchInfrastructureBody = never;
export type AdmBatchWorkbenchInfrastructurePath = Record<string, never>;
export type AdmBatchWorkbenchInfrastructureQuery = { heartbeatTimeoutSeconds?: number; limit?: number };
export type AdmBatchWorkbenchInfrastructureHeaders = Record<string, never>;
export type AdmBatchWorkbenchInfrastructureResponse = Record<string, unknown>;
export type AdmBatchWorkbenchInfrastructureOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchWorkbenchInfrastructureQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchInfrastructure<T = AdmBatchWorkbenchInfrastructureResponse>(options: AdmBatchWorkbenchInfrastructureOptions = {} as AdmBatchWorkbenchInfrastructureOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchWorkbenchInfrastructure(contractParams as Parameters<typeof orvalAdmBatchWorkbenchInfrastructure>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchJobDetailBody = never;
export type AdmBatchWorkbenchJobDetailPath = { jobId: string };
export type AdmBatchWorkbenchJobDetailQuery = Record<string, never>;
export type AdmBatchWorkbenchJobDetailHeaders = Record<string, never>;
export type AdmBatchWorkbenchJobDetailResponse = Record<string, unknown>;
export type AdmBatchWorkbenchJobDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmBatchWorkbenchJobDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchJobDetail<T = AdmBatchWorkbenchJobDetailResponse>(options: AdmBatchWorkbenchJobDetailOptions): Promise<T> {
  const response = await orvalAdmBatchWorkbenchJobDetail(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchJobsBody = never;
export type AdmBatchWorkbenchJobsPath = Record<string, never>;
export type AdmBatchWorkbenchJobsQuery = { query?: string; page?: number; size?: number; sort?: string; direction?: string };
export type AdmBatchWorkbenchJobsHeaders = Record<string, never>;
export type AdmBatchWorkbenchJobsResponse = Record<string, unknown>;
export type AdmBatchWorkbenchJobsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchWorkbenchJobsQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchJobs<T = AdmBatchWorkbenchJobsResponse>(options: AdmBatchWorkbenchJobsOptions = {} as AdmBatchWorkbenchJobsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchWorkbenchJobs(contractParams as Parameters<typeof orvalAdmBatchWorkbenchJobs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchOverviewBody = never;
export type AdmBatchWorkbenchOverviewPath = Record<string, never>;
export type AdmBatchWorkbenchOverviewQuery = Record<string, never>;
export type AdmBatchWorkbenchOverviewHeaders = Record<string, never>;
export type AdmBatchWorkbenchOverviewResponse = Record<string, unknown>;
export type AdmBatchWorkbenchOverviewOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchOverview<T = AdmBatchWorkbenchOverviewResponse>(options: AdmBatchWorkbenchOverviewOptions = {} as AdmBatchWorkbenchOverviewOptions): Promise<T> {
  const response = await orvalAdmBatchWorkbenchOverview({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchRecoveryBody = never;
export type AdmBatchWorkbenchRecoveryPath = Record<string, never>;
export type AdmBatchWorkbenchRecoveryQuery = { heartbeatTimeoutSeconds?: number; limit?: number };
export type AdmBatchWorkbenchRecoveryHeaders = Record<string, never>;
export type AdmBatchWorkbenchRecoveryResponse = Record<string, unknown>;
export type AdmBatchWorkbenchRecoveryOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchWorkbenchRecoveryQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchRecovery<T = AdmBatchWorkbenchRecoveryResponse>(options: AdmBatchWorkbenchRecoveryOptions = {} as AdmBatchWorkbenchRecoveryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchWorkbenchRecovery(contractParams as Parameters<typeof orvalAdmBatchWorkbenchRecovery>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBatchWorkbenchSchedulesBody = never;
export type AdmBatchWorkbenchSchedulesPath = Record<string, never>;
export type AdmBatchWorkbenchSchedulesQuery = { query?: string; page?: number; size?: number; sort?: string; direction?: string };
export type AdmBatchWorkbenchSchedulesHeaders = Record<string, never>;
export type AdmBatchWorkbenchSchedulesResponse = Record<string, unknown>;
export type AdmBatchWorkbenchSchedulesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBatchWorkbenchSchedulesQuery; headers?: CpfGeneratedHeaders; };
export async function admBatchWorkbenchSchedules<T = AdmBatchWorkbenchSchedulesResponse>(options: AdmBatchWorkbenchSchedulesOptions = {} as AdmBatchWorkbenchSchedulesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBatchWorkbenchSchedules(contractParams as Parameters<typeof orvalAdmBatchWorkbenchSchedules>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBreakGlassCloseSessionBody = Record<string, unknown>;
export type AdmBreakGlassCloseSessionPath = { sessionId: string };
export type AdmBreakGlassCloseSessionQuery = Record<string, never>;
export type AdmBreakGlassCloseSessionHeaders = Record<string, never>;
export type AdmBreakGlassCloseSessionResponse = Record<string, unknown>;
export type AdmBreakGlassCloseSessionOptions = CpfGeneratedBaseOptions & { data: AdmBreakGlassCloseSessionBody; path: AdmBreakGlassCloseSessionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBreakGlassCloseSession<T = AdmBreakGlassCloseSessionResponse>(options: AdmBreakGlassCloseSessionOptions): Promise<T> {
  const response = await orvalAdmBreakGlassCloseSession(options.path["sessionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBreakGlassFindSessionsBody = never;
export type AdmBreakGlassFindSessionsPath = Record<string, never>;
export type AdmBreakGlassFindSessionsQuery = { status?: string; limit?: number };
export type AdmBreakGlassFindSessionsHeaders = Record<string, never>;
export type AdmBreakGlassFindSessionsResponse = Record<string, unknown>;
export type AdmBreakGlassFindSessionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmBreakGlassFindSessionsQuery; headers?: CpfGeneratedHeaders; };
export async function admBreakGlassFindSessions<T = AdmBreakGlassFindSessionsResponse>(options: AdmBreakGlassFindSessionsOptions = {} as AdmBreakGlassFindSessionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmBreakGlassFindSessions(contractParams as Parameters<typeof orvalAdmBreakGlassFindSessions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBreakGlassOpenSessionBody = Record<string, unknown>;
export type AdmBreakGlassOpenSessionPath = Record<string, never>;
export type AdmBreakGlassOpenSessionQuery = Record<string, never>;
export type AdmBreakGlassOpenSessionHeaders = Record<string, never>;
export type AdmBreakGlassOpenSessionResponse = Record<string, unknown>;
export type AdmBreakGlassOpenSessionOptions = CpfGeneratedBaseOptions & { data: AdmBreakGlassOpenSessionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBreakGlassOpenSession<T = AdmBreakGlassOpenSessionResponse>(options: AdmBreakGlassOpenSessionOptions): Promise<T> {
  const response = await orvalAdmBreakGlassOpenSession(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmBreakGlassReviewSessionBody = Record<string, unknown>;
export type AdmBreakGlassReviewSessionPath = { sessionId: string };
export type AdmBreakGlassReviewSessionQuery = Record<string, never>;
export type AdmBreakGlassReviewSessionHeaders = Record<string, never>;
export type AdmBreakGlassReviewSessionResponse = Record<string, unknown>;
export type AdmBreakGlassReviewSessionOptions = CpfGeneratedBaseOptions & { data: AdmBreakGlassReviewSessionBody; path: AdmBreakGlassReviewSessionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admBreakGlassReviewSession<T = AdmBreakGlassReviewSessionResponse>(options: AdmBreakGlassReviewSessionOptions): Promise<T> {
  const response = await orvalAdmBreakGlassReviewSession(options.path["sessionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCacheSummaryBody = never;
export type AdmCacheSummaryPath = Record<string, never>;
export type AdmCacheSummaryQuery = Record<string, never>;
export type AdmCacheSummaryHeaders = Record<string, never>;
export type AdmCacheSummaryResponse = Record<string, unknown>;
export type AdmCacheSummaryOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCacheSummary<T = AdmCacheSummaryResponse>(options: AdmCacheSummaryOptions = {} as AdmCacheSummaryOptions): Promise<T> {
  const response = await orvalAdmCacheSummary({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCalendarDeleteDayBody = never;
export type AdmCalendarDeleteDayPath = { calendarId: string; businessDate: string };
export type AdmCalendarDeleteDayQuery = { expectedVersion: number; auditReason: string };
export type AdmCalendarDeleteDayHeaders = Record<string, never>;
export type AdmCalendarDeleteDayResponse = Record<string, unknown>;
export type AdmCalendarDeleteDayOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCalendarDeleteDayPath; query?: AdmCalendarDeleteDayQuery; headers?: CpfGeneratedHeaders; };
export async function admCalendarDeleteDay<T = AdmCalendarDeleteDayResponse>(options: AdmCalendarDeleteDayOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCalendarDeleteDay(options.path["calendarId"], options.path["businessDate"], contractParams as Parameters<typeof orvalAdmCalendarDeleteDay>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCalendarFindDaysBody = never;
export type AdmCalendarFindDaysPath = { calendarId: string };
export type AdmCalendarFindDaysQuery = { from?: string; to?: string; limit?: number };
export type AdmCalendarFindDaysHeaders = Record<string, never>;
export type AdmCalendarFindDaysResponse = Record<string, unknown>;
export type AdmCalendarFindDaysOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCalendarFindDaysPath; query?: AdmCalendarFindDaysQuery; headers?: CpfGeneratedHeaders; };
export async function admCalendarFindDays<T = AdmCalendarFindDaysResponse>(options: AdmCalendarFindDaysOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCalendarFindDays(options.path["calendarId"], contractParams as Parameters<typeof orvalAdmCalendarFindDays>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCalendarResolveDateBody = never;
export type AdmCalendarResolveDatePath = { calendarId: string };
export type AdmCalendarResolveDateQuery = { date: string; offset?: number };
export type AdmCalendarResolveDateHeaders = Record<string, never>;
export type AdmCalendarResolveDateResponse = Record<string, unknown>;
export type AdmCalendarResolveDateOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCalendarResolveDatePath; query?: AdmCalendarResolveDateQuery; headers?: CpfGeneratedHeaders; };
export async function admCalendarResolveDate<T = AdmCalendarResolveDateResponse>(options: AdmCalendarResolveDateOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCalendarResolveDate(options.path["calendarId"], contractParams as Parameters<typeof orvalAdmCalendarResolveDate>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCalendarSaveDayBody = { auditReason?: string; businessDay: boolean; dayType?: string; institutionCode?: string; reason?: string };
export type AdmCalendarSaveDayPath = { calendarId: string; businessDate: string };
export type AdmCalendarSaveDayQuery = { expectedVersion?: number };
export type AdmCalendarSaveDayHeaders = Record<string, never>;
export type AdmCalendarSaveDayResponse = Record<string, unknown>;
export type AdmCalendarSaveDayOptions = CpfGeneratedBaseOptions & { data: AdmCalendarSaveDayBody; path: AdmCalendarSaveDayPath; query?: AdmCalendarSaveDayQuery; headers?: CpfGeneratedHeaders; };
export async function admCalendarSaveDay<T = AdmCalendarSaveDayResponse>(options: AdmCalendarSaveDayOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCalendarSaveDay(options.path["calendarId"], options.path["businessDate"], options.data, contractParams as Parameters<typeof orvalAdmCalendarSaveDay>[3], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCapabilityManagementIssuesBody = never;
export type AdmCapabilityManagementIssuesPath = Record<string, never>;
export type AdmCapabilityManagementIssuesQuery = { systemCode?: string; systemId?: string; starterId?: string; capabilityId?: string; provider?: string; includeStale?: boolean };
export type AdmCapabilityManagementIssuesHeaders = Record<string, never>;
export type AdmCapabilityManagementIssuesResponse = Record<string, unknown>;
export type AdmCapabilityManagementIssuesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmCapabilityManagementIssuesQuery; headers?: CpfGeneratedHeaders; };
export async function admCapabilityManagementIssues<T = AdmCapabilityManagementIssuesResponse>(options: AdmCapabilityManagementIssuesOptions = {} as AdmCapabilityManagementIssuesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCapabilityManagementIssues(contractParams as Parameters<typeof orvalAdmCapabilityManagementIssues>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCapabilityManagementOverviewBody = never;
export type AdmCapabilityManagementOverviewPath = Record<string, never>;
export type AdmCapabilityManagementOverviewQuery = { environment?: string; systemCode?: string; systemId?: string; domainCode?: string; domainId?: string; application?: string; module?: string; host?: string; instanceId?: string; starterId?: string; capabilityId?: string; provider?: string; version?: string; status?: string; includeStale?: boolean; page?: number; size?: number };
export type AdmCapabilityManagementOverviewHeaders = Record<string, never>;
export type AdmCapabilityManagementOverviewResponse = Record<string, unknown>;
export type AdmCapabilityManagementOverviewOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmCapabilityManagementOverviewQuery; headers?: CpfGeneratedHeaders; };
export async function admCapabilityManagementOverview<T = AdmCapabilityManagementOverviewResponse>(options: AdmCapabilityManagementOverviewOptions = {} as AdmCapabilityManagementOverviewOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCapabilityManagementOverview(contractParams as Parameters<typeof orvalAdmCapabilityManagementOverview>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindJobDetailBody = never;
export type AdmCenterCutFindJobDetailPath = { centerCutJobId: string };
export type AdmCenterCutFindJobDetailQuery = Record<string, never>;
export type AdmCenterCutFindJobDetailHeaders = Record<string, never>;
export type AdmCenterCutFindJobDetailResponse = Record<string, unknown>;
export type AdmCenterCutFindJobDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindJobDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindJobDetail<T = AdmCenterCutFindJobDetailResponse>(options: AdmCenterCutFindJobDetailOptions): Promise<T> {
  const response = await orvalAdmCenterCutFindJobDetail(options.path["centerCutJobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindJobsBody = never;
export type AdmCenterCutFindJobsPath = Record<string, never>;
export type AdmCenterCutFindJobsQuery = Record<string, never>;
export type AdmCenterCutFindJobsHeaders = Record<string, never>;
export type AdmCenterCutFindJobsResponse = Record<string, unknown>;
export type AdmCenterCutFindJobsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindJobs<T = AdmCenterCutFindJobsResponse>(options: AdmCenterCutFindJobsOptions = {} as AdmCenterCutFindJobsOptions): Promise<T> {
  const response = await orvalAdmCenterCutFindJobs({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindParametersBody = never;
export type AdmCenterCutFindParametersPath = { centerCutJobId: string };
export type AdmCenterCutFindParametersQuery = Record<string, never>;
export type AdmCenterCutFindParametersHeaders = Record<string, never>;
export type AdmCenterCutFindParametersResponse = Record<string, unknown>;
export type AdmCenterCutFindParametersOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindParametersPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindParameters<T = AdmCenterCutFindParametersResponse>(options: AdmCenterCutFindParametersOptions): Promise<T> {
  const response = await orvalAdmCenterCutFindParameters(options.path["centerCutJobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindResultDetailBody = never;
export type AdmCenterCutFindResultDetailPath = { resultId: string };
export type AdmCenterCutFindResultDetailQuery = Record<string, never>;
export type AdmCenterCutFindResultDetailHeaders = Record<string, never>;
export type AdmCenterCutFindResultDetailResponse = Record<string, unknown>;
export type AdmCenterCutFindResultDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindResultDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindResultDetail<T = AdmCenterCutFindResultDetailResponse>(options: AdmCenterCutFindResultDetailOptions): Promise<T> {
  const response = await orvalAdmCenterCutFindResultDetail(options.path["resultId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindResultsBody = never;
export type AdmCenterCutFindResultsPath = { centerCutJobId: string };
export type AdmCenterCutFindResultsQuery = { resultStatus?: string; limit?: number };
export type AdmCenterCutFindResultsHeaders = Record<string, never>;
export type AdmCenterCutFindResultsResponse = Record<string, unknown>;
export type AdmCenterCutFindResultsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindResultsPath; query?: AdmCenterCutFindResultsQuery; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindResults<T = AdmCenterCutFindResultsResponse>(options: AdmCenterCutFindResultsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCenterCutFindResults(options.path["centerCutJobId"], contractParams as Parameters<typeof orvalAdmCenterCutFindResults>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindSummaryBody = never;
export type AdmCenterCutFindSummaryPath = { centerCutJobId: string };
export type AdmCenterCutFindSummaryQuery = Record<string, never>;
export type AdmCenterCutFindSummaryHeaders = Record<string, never>;
export type AdmCenterCutFindSummaryResponse = Record<string, unknown>;
export type AdmCenterCutFindSummaryOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindSummaryPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindSummary<T = AdmCenterCutFindSummaryResponse>(options: AdmCenterCutFindSummaryOptions): Promise<T> {
  const response = await orvalAdmCenterCutFindSummary(options.path["centerCutJobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutFindTargetsBody = never;
export type AdmCenterCutFindTargetsPath = { centerCutJobId: string };
export type AdmCenterCutFindTargetsQuery = { statusCode?: string; limit?: number };
export type AdmCenterCutFindTargetsHeaders = Record<string, never>;
export type AdmCenterCutFindTargetsResponse = Record<string, unknown>;
export type AdmCenterCutFindTargetsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCenterCutFindTargetsPath; query?: AdmCenterCutFindTargetsQuery; headers?: CpfGeneratedHeaders; };
export async function admCenterCutFindTargets<T = AdmCenterCutFindTargetsResponse>(options: AdmCenterCutFindTargetsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCenterCutFindTargets(options.path["centerCutJobId"], contractParams as Parameters<typeof orvalAdmCenterCutFindTargets>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutReconcileUnknownExecutionBody = { approvalRequestId?: string; idempotencyKey?: string; reason?: string };
export type AdmCenterCutReconcileUnknownExecutionPath = { executionId: string };
export type AdmCenterCutReconcileUnknownExecutionQuery = Record<string, never>;
export type AdmCenterCutReconcileUnknownExecutionHeaders = Record<string, never>;
export type AdmCenterCutReconcileUnknownExecutionResponse = Record<string, unknown>;
export type AdmCenterCutReconcileUnknownExecutionOptions = CpfGeneratedBaseOptions & { data: AdmCenterCutReconcileUnknownExecutionBody; path: AdmCenterCutReconcileUnknownExecutionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutReconcileUnknownExecution<T = AdmCenterCutReconcileUnknownExecutionResponse>(options: AdmCenterCutReconcileUnknownExecutionOptions): Promise<T> {
  const response = await orvalAdmCenterCutReconcileUnknownExecution(options.path["executionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCenterCutReprocessFailedExecutionBody = { approvalRequestId?: string; idempotencyKey?: string; reason?: string };
export type AdmCenterCutReprocessFailedExecutionPath = { executionId: string };
export type AdmCenterCutReprocessFailedExecutionQuery = Record<string, never>;
export type AdmCenterCutReprocessFailedExecutionHeaders = Record<string, never>;
export type AdmCenterCutReprocessFailedExecutionResponse = Record<string, unknown>;
export type AdmCenterCutReprocessFailedExecutionOptions = CpfGeneratedBaseOptions & { data: AdmCenterCutReprocessFailedExecutionBody; path: AdmCenterCutReprocessFailedExecutionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCenterCutReprocessFailedExecution<T = AdmCenterCutReprocessFailedExecutionResponse>(options: AdmCenterCutReprocessFailedExecutionOptions): Promise<T> {
  const response = await orvalAdmCenterCutReprocessFailedExecution(options.path["executionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelExportPackageBody = never;
export type AdmChannelExportPackagePath = Record<string, never>;
export type AdmChannelExportPackageQuery = Record<string, never>;
export type AdmChannelExportPackageHeaders = Record<string, never>;
export type AdmChannelExportPackageResponse = Record<string, unknown>;
export type AdmChannelExportPackageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelExportPackage<T = AdmChannelExportPackageResponse>(options: AdmChannelExportPackageOptions = {} as AdmChannelExportPackageOptions): Promise<T> {
  const response = await orvalAdmChannelExportPackage({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelFindSnapshotBody = never;
export type AdmChannelFindSnapshotPath = Record<string, never>;
export type AdmChannelFindSnapshotQuery = Record<string, never>;
export type AdmChannelFindSnapshotHeaders = Record<string, never>;
export type AdmChannelFindSnapshotResponse = Record<string, unknown>;
export type AdmChannelFindSnapshotOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelFindSnapshot<T = AdmChannelFindSnapshotResponse>(options: AdmChannelFindSnapshotOptions = {} as AdmChannelFindSnapshotOptions): Promise<T> {
  const response = await orvalAdmChannelFindSnapshot({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelImportPackageBody = { dryRun: boolean; policyPackage: Record<string, unknown>; reason: string };
export type AdmChannelImportPackagePath = Record<string, never>;
export type AdmChannelImportPackageQuery = Record<string, never>;
export type AdmChannelImportPackageHeaders = Record<string, never>;
export type AdmChannelImportPackageResponse = Record<string, unknown>;
export type AdmChannelImportPackageOptions = CpfGeneratedBaseOptions & { data: AdmChannelImportPackageBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelImportPackage<T = AdmChannelImportPackageResponse>(options: AdmChannelImportPackageOptions): Promise<T> {
  const response = await orvalAdmChannelImportPackage(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelRefreshSnapshotBody = never;
export type AdmChannelRefreshSnapshotPath = Record<string, never>;
export type AdmChannelRefreshSnapshotQuery = Record<string, never>;
export type AdmChannelRefreshSnapshotHeaders = Record<string, never>;
export type AdmChannelRefreshSnapshotResponse = Record<string, unknown>;
export type AdmChannelRefreshSnapshotOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelRefreshSnapshot<T = AdmChannelRefreshSnapshotResponse>(options: AdmChannelRefreshSnapshotOptions = {} as AdmChannelRefreshSnapshotOptions): Promise<T> {
  const response = await orvalAdmChannelRefreshSnapshot({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelSaveBody = { active: boolean; authenticationRequired: boolean; channelName: string; channelType: string; clientChannel: boolean; description?: string; internalChannel: boolean; reason: string; signatureRequired: boolean; trustLevel: string };
export type AdmChannelSavePath = { channelCode: string };
export type AdmChannelSaveQuery = Record<string, never>;
export type AdmChannelSaveHeaders = Record<string, never>;
export type AdmChannelSaveResponse = Record<string, unknown>;
export type AdmChannelSaveOptions = CpfGeneratedBaseOptions & { data: AdmChannelSaveBody; path: AdmChannelSavePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelSave<T = AdmChannelSaveResponse>(options: AdmChannelSaveOptions): Promise<T> {
  const response = await orvalAdmChannelSave(options.path["channelCode"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmChannelSaveExecutionPolicyBody = { active: boolean; allowed: boolean; authenticationRequired: boolean; callerChannelCode: string; effectiveFrom?: string; effectiveTo?: string; maxTps: number; originalChannelCode: string; reason: string; requestType: string; signatureRequired: boolean; standardExecutionId: string };
export type AdmChannelSaveExecutionPolicyPath = { policyKey: string };
export type AdmChannelSaveExecutionPolicyQuery = Record<string, never>;
export type AdmChannelSaveExecutionPolicyHeaders = Record<string, never>;
export type AdmChannelSaveExecutionPolicyResponse = Record<string, unknown>;
export type AdmChannelSaveExecutionPolicyOptions = CpfGeneratedBaseOptions & { data: AdmChannelSaveExecutionPolicyBody; path: AdmChannelSaveExecutionPolicyPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admChannelSaveExecutionPolicy<T = AdmChannelSaveExecutionPolicyResponse>(options: AdmChannelSaveExecutionPolicyOptions): Promise<T> {
  const response = await orvalAdmChannelSaveExecutionPolicy(options.path["policyKey"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCodeCreateCodeBody = Record<string, unknown>;
export type AdmCodeCreateCodePath = Record<string, never>;
export type AdmCodeCreateCodeQuery = Record<string, never>;
export type AdmCodeCreateCodeHeaders = Record<string, never>;
export type AdmCodeCreateCodeResponse = Record<string, unknown>;
export type AdmCodeCreateCodeOptions = CpfGeneratedBaseOptions & { data: AdmCodeCreateCodeBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCodeCreateCode<T = AdmCodeCreateCodeResponse>(options: AdmCodeCreateCodeOptions): Promise<T> {
  const response = await orvalAdmCodeCreateCode(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCodeDeleteCodeBody = never;
export type AdmCodeDeleteCodePath = { codeId: number };
export type AdmCodeDeleteCodeQuery = { reason: string };
export type AdmCodeDeleteCodeHeaders = Record<string, never>;
export type AdmCodeDeleteCodeResponse = Record<string, unknown>;
export type AdmCodeDeleteCodeOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCodeDeleteCodePath; query?: AdmCodeDeleteCodeQuery; headers?: CpfGeneratedHeaders; };
export async function admCodeDeleteCode<T = AdmCodeDeleteCodeResponse>(options: AdmCodeDeleteCodeOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmCodeDeleteCode(options.path["codeId"], contractParams as Parameters<typeof orvalAdmCodeDeleteCode>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCodeFindCodeBody = never;
export type AdmCodeFindCodePath = { codeId: number };
export type AdmCodeFindCodeQuery = Record<string, never>;
export type AdmCodeFindCodeHeaders = Record<string, never>;
export type AdmCodeFindCodeResponse = Record<string, unknown>;
export type AdmCodeFindCodeOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmCodeFindCodePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCodeFindCode<T = AdmCodeFindCodeResponse>(options: AdmCodeFindCodeOptions): Promise<T> {
  const response = await orvalAdmCodeFindCode(options.path["codeId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCodeFindCodesBody = never;
export type AdmCodeFindCodesPath = Record<string, never>;
export type AdmCodeFindCodesQuery = Record<string, never>;
export type AdmCodeFindCodesHeaders = Record<string, never>;
export type AdmCodeFindCodesResponse = Record<string, unknown>;
export type AdmCodeFindCodesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCodeFindCodes<T = AdmCodeFindCodesResponse>(options: AdmCodeFindCodesOptions = {} as AdmCodeFindCodesOptions): Promise<T> {
  const response = await orvalAdmCodeFindCodes({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmCodeUpdateCodeBody = Record<string, unknown>;
export type AdmCodeUpdateCodePath = { codeId: number };
export type AdmCodeUpdateCodeQuery = Record<string, never>;
export type AdmCodeUpdateCodeHeaders = Record<string, never>;
export type AdmCodeUpdateCodeResponse = Record<string, unknown>;
export type AdmCodeUpdateCodeOptions = CpfGeneratedBaseOptions & { data: AdmCodeUpdateCodeBody; path: AdmCodeUpdateCodePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admCodeUpdateCode<T = AdmCodeUpdateCodeResponse>(options: AdmCodeUpdateCodeOptions): Promise<T> {
  const response = await orvalAdmCodeUpdateCode(options.path["codeId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmConfigCreateConfigBody = Record<string, unknown>;
export type AdmConfigCreateConfigPath = Record<string, never>;
export type AdmConfigCreateConfigQuery = Record<string, never>;
export type AdmConfigCreateConfigHeaders = Record<string, never>;
export type AdmConfigCreateConfigResponse = Record<string, unknown>;
export type AdmConfigCreateConfigOptions = CpfGeneratedBaseOptions & { data: AdmConfigCreateConfigBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admConfigCreateConfig<T = AdmConfigCreateConfigResponse>(options: AdmConfigCreateConfigOptions): Promise<T> {
  const response = await orvalAdmConfigCreateConfig(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmConfigDeleteConfigBody = never;
export type AdmConfigDeleteConfigPath = { configId: number };
export type AdmConfigDeleteConfigQuery = { reason: string };
export type AdmConfigDeleteConfigHeaders = Record<string, never>;
export type AdmConfigDeleteConfigResponse = Record<string, unknown>;
export type AdmConfigDeleteConfigOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmConfigDeleteConfigPath; query?: AdmConfigDeleteConfigQuery; headers?: CpfGeneratedHeaders; };
export async function admConfigDeleteConfig<T = AdmConfigDeleteConfigResponse>(options: AdmConfigDeleteConfigOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmConfigDeleteConfig(options.path["configId"], contractParams as Parameters<typeof orvalAdmConfigDeleteConfig>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmConfigFindConfigBody = never;
export type AdmConfigFindConfigPath = { configId: number };
export type AdmConfigFindConfigQuery = Record<string, never>;
export type AdmConfigFindConfigHeaders = Record<string, never>;
export type AdmConfigFindConfigResponse = Record<string, unknown>;
export type AdmConfigFindConfigOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmConfigFindConfigPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admConfigFindConfig<T = AdmConfigFindConfigResponse>(options: AdmConfigFindConfigOptions): Promise<T> {
  const response = await orvalAdmConfigFindConfig(options.path["configId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmConfigFindConfigsBody = never;
export type AdmConfigFindConfigsPath = Record<string, never>;
export type AdmConfigFindConfigsQuery = Record<string, never>;
export type AdmConfigFindConfigsHeaders = Record<string, never>;
export type AdmConfigFindConfigsResponse = Record<string, unknown>;
export type AdmConfigFindConfigsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admConfigFindConfigs<T = AdmConfigFindConfigsResponse>(options: AdmConfigFindConfigsOptions = {} as AdmConfigFindConfigsOptions): Promise<T> {
  const response = await orvalAdmConfigFindConfigs({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmConfigUpdateConfigBody = Record<string, unknown>;
export type AdmConfigUpdateConfigPath = { configId: number };
export type AdmConfigUpdateConfigQuery = Record<string, never>;
export type AdmConfigUpdateConfigHeaders = Record<string, never>;
export type AdmConfigUpdateConfigResponse = Record<string, unknown>;
export type AdmConfigUpdateConfigOptions = CpfGeneratedBaseOptions & { data: AdmConfigUpdateConfigBody; path: AdmConfigUpdateConfigPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admConfigUpdateConfig<T = AdmConfigUpdateConfigResponse>(options: AdmConfigUpdateConfigOptions): Promise<T> {
  const response = await orvalAdmConfigUpdateConfig(options.path["configId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmDownloadDownloadCsvBody = { downloadType?: string; fromDate?: string; includeSensitive?: boolean; jobId?: string; limit?: number; reason?: string; targetType?: string; toDate?: string; traceId?: string; transactionId?: string };
export type AdmDownloadDownloadCsvPath = Record<string, never>;
export type AdmDownloadDownloadCsvQuery = Record<string, never>;
export type AdmDownloadDownloadCsvHeaders = Record<string, never>;
export type AdmDownloadDownloadCsvResponse = Record<string, unknown>;
export type AdmDownloadDownloadCsvOptions = CpfGeneratedBaseOptions & { data: AdmDownloadDownloadCsvBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admDownloadDownloadCsv<T = AdmDownloadDownloadCsvResponse>(options: AdmDownloadDownloadCsvOptions): Promise<T> {
  const response = await orvalAdmDownloadDownloadCsv(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmDownloadFindDownloadAuditLogsBody = never;
export type AdmDownloadFindDownloadAuditLogsPath = Record<string, never>;
export type AdmDownloadFindDownloadAuditLogsQuery = { downloadType?: string; adminId?: string; limit?: number };
export type AdmDownloadFindDownloadAuditLogsHeaders = Record<string, never>;
export type AdmDownloadFindDownloadAuditLogsResponse = Record<string, unknown>;
export type AdmDownloadFindDownloadAuditLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmDownloadFindDownloadAuditLogsQuery; headers?: CpfGeneratedHeaders; };
export async function admDownloadFindDownloadAuditLogs<T = AdmDownloadFindDownloadAuditLogsResponse>(options: AdmDownloadFindDownloadAuditLogsOptions = {} as AdmDownloadFindDownloadAuditLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmDownloadFindDownloadAuditLogs(contractParams as Parameters<typeof orvalAdmDownloadFindDownloadAuditLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmDownloadFindPoliciesBody = never;
export type AdmDownloadFindPoliciesPath = Record<string, never>;
export type AdmDownloadFindPoliciesQuery = Record<string, never>;
export type AdmDownloadFindPoliciesHeaders = Record<string, never>;
export type AdmDownloadFindPoliciesResponse = Record<string, unknown>;
export type AdmDownloadFindPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admDownloadFindPolicies<T = AdmDownloadFindPoliciesResponse>(options: AdmDownloadFindPoliciesOptions = {} as AdmDownloadFindPoliciesOptions): Promise<T> {
  const response = await orvalAdmDownloadFindPolicies({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmDynamicLogLevelFindRulesBody = never;
export type AdmDynamicLogLevelFindRulesPath = Record<string, never>;
export type AdmDynamicLogLevelFindRulesQuery = Record<string, never>;
export type AdmDynamicLogLevelFindRulesHeaders = Record<string, never>;
export type AdmDynamicLogLevelFindRulesResponse = Record<string, unknown>;
export type AdmDynamicLogLevelFindRulesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admDynamicLogLevelFindRules<T = AdmDynamicLogLevelFindRulesResponse>(options: AdmDynamicLogLevelFindRulesOptions = {} as AdmDynamicLogLevelFindRulesOptions): Promise<T> {
  const response = await orvalAdmDynamicLogLevelFindRules({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagApproveOverrideBody = { reason?: string };
export type AdmFeatureFlagApproveOverridePath = { requestId: string };
export type AdmFeatureFlagApproveOverrideQuery = Record<string, never>;
export type AdmFeatureFlagApproveOverrideHeaders = { "X-CPF-Risk-Confirmed": string };
export type AdmFeatureFlagApproveOverrideResponse = Record<string, unknown>;
export type AdmFeatureFlagApproveOverrideOptions = CpfGeneratedBaseOptions & { data: AdmFeatureFlagApproveOverrideBody; path: AdmFeatureFlagApproveOverridePath; query?: never; headers: CpfGeneratedHeaders & AdmFeatureFlagApproveOverrideHeaders; };
export async function admFeatureFlagApproveOverride<T = AdmFeatureFlagApproveOverrideResponse>(options: AdmFeatureFlagApproveOverrideOptions): Promise<T> {
  const contractParams = { "X-CPF-Risk-Confirmed": headerValue(options.headers, "X-CPF-Risk-Confirmed") };
  const response = await orvalAdmFeatureFlagApproveOverride(options.path["requestId"], options.data, contractParams as Parameters<typeof orvalAdmFeatureFlagApproveOverride>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagEvaluateBody = { attributes?: Record<string, string>; flagKey?: string; targetingKey?: string; value?: string; valueType?: string };
export type AdmFeatureFlagEvaluatePath = Record<string, never>;
export type AdmFeatureFlagEvaluateQuery = Record<string, never>;
export type AdmFeatureFlagEvaluateHeaders = Record<string, never>;
export type AdmFeatureFlagEvaluateResponse = Record<string, unknown>;
export type AdmFeatureFlagEvaluateOptions = CpfGeneratedBaseOptions & { data: AdmFeatureFlagEvaluateBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFeatureFlagEvaluate<T = AdmFeatureFlagEvaluateResponse>(options: AdmFeatureFlagEvaluateOptions): Promise<T> {
  const response = await orvalAdmFeatureFlagEvaluate(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagFindBody = never;
export type AdmFeatureFlagFindPath = { flagKey: string };
export type AdmFeatureFlagFindQuery = Record<string, never>;
export type AdmFeatureFlagFindHeaders = Record<string, never>;
export type AdmFeatureFlagFindResponse = Record<string, unknown>;
export type AdmFeatureFlagFindOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmFeatureFlagFindPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFeatureFlagFind<T = AdmFeatureFlagFindResponse>(options: AdmFeatureFlagFindOptions): Promise<T> {
  const response = await orvalAdmFeatureFlagFind(options.path["flagKey"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagRequestOverrideBody = { expiresAt?: string; flagKey?: string; reason?: string; value?: string; valueType?: string };
export type AdmFeatureFlagRequestOverridePath = Record<string, never>;
export type AdmFeatureFlagRequestOverrideQuery = Record<string, never>;
export type AdmFeatureFlagRequestOverrideHeaders = Record<string, never>;
export type AdmFeatureFlagRequestOverrideResponse = Record<string, unknown>;
export type AdmFeatureFlagRequestOverrideOptions = CpfGeneratedBaseOptions & { data: AdmFeatureFlagRequestOverrideBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFeatureFlagRequestOverride<T = AdmFeatureFlagRequestOverrideResponse>(options: AdmFeatureFlagRequestOverrideOptions): Promise<T> {
  const response = await orvalAdmFeatureFlagRequestOverride(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagRevokeOverrideBody = { reason?: string };
export type AdmFeatureFlagRevokeOverridePath = { requestId: string };
export type AdmFeatureFlagRevokeOverrideQuery = Record<string, never>;
export type AdmFeatureFlagRevokeOverrideHeaders = { "X-CPF-Risk-Confirmed": string };
export type AdmFeatureFlagRevokeOverrideResponse = Record<string, unknown>;
export type AdmFeatureFlagRevokeOverrideOptions = CpfGeneratedBaseOptions & { data: AdmFeatureFlagRevokeOverrideBody; path: AdmFeatureFlagRevokeOverridePath; query?: never; headers: CpfGeneratedHeaders & AdmFeatureFlagRevokeOverrideHeaders; };
export async function admFeatureFlagRevokeOverride<T = AdmFeatureFlagRevokeOverrideResponse>(options: AdmFeatureFlagRevokeOverrideOptions): Promise<T> {
  const contractParams = { "X-CPF-Risk-Confirmed": headerValue(options.headers, "X-CPF-Risk-Confirmed") };
  const response = await orvalAdmFeatureFlagRevokeOverride(options.path["requestId"], options.data, contractParams as Parameters<typeof orvalAdmFeatureFlagRevokeOverride>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFeatureFlagSearchBody = never;
export type AdmFeatureFlagSearchPath = Record<string, never>;
export type AdmFeatureFlagSearchQuery = { query?: string; page?: number; size?: number };
export type AdmFeatureFlagSearchHeaders = Record<string, never>;
export type AdmFeatureFlagSearchResponse = Record<string, unknown>;
export type AdmFeatureFlagSearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmFeatureFlagSearchQuery; headers?: CpfGeneratedHeaders; };
export async function admFeatureFlagSearch<T = AdmFeatureFlagSearchResponse>(options: AdmFeatureFlagSearchOptions = {} as AdmFeatureFlagSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmFeatureFlagSearch(contractParams as Parameters<typeof orvalAdmFeatureFlagSearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFileJobArtifactBody = never;
export type AdmFileJobArtifactPath = { jobId: string };
export type AdmFileJobArtifactQuery = Record<string, never>;
export type AdmFileJobArtifactHeaders = Record<string, never>;
export type AdmFileJobArtifactResponse = Record<string, unknown>;
export type AdmFileJobArtifactOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmFileJobArtifactPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFileJobArtifact<T = AdmFileJobArtifactResponse>(options: AdmFileJobArtifactOptions): Promise<T> {
  const response = await orvalAdmFileJobArtifact(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFileJobDetailBody = never;
export type AdmFileJobDetailPath = { jobId: string };
export type AdmFileJobDetailQuery = Record<string, never>;
export type AdmFileJobDetailHeaders = Record<string, never>;
export type AdmFileJobDetailResponse = Record<string, unknown>;
export type AdmFileJobDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmFileJobDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFileJobDetail<T = AdmFileJobDetailResponse>(options: AdmFileJobDetailOptions): Promise<T> {
  const response = await orvalAdmFileJobDetail(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFileJobListBody = never;
export type AdmFileJobListPath = Record<string, never>;
export type AdmFileJobListQuery = { limit?: number };
export type AdmFileJobListHeaders = Record<string, never>;
export type AdmFileJobListResponse = Record<string, unknown>;
export type AdmFileJobListOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmFileJobListQuery; headers?: CpfGeneratedHeaders; };
export async function admFileJobList<T = AdmFileJobListResponse>(options: AdmFileJobListOptions = {} as AdmFileJobListOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmFileJobList(contractParams as Parameters<typeof orvalAdmFileJobList>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFileJobRowsBody = never;
export type AdmFileJobRowsPath = { jobId: string };
export type AdmFileJobRowsQuery = Record<string, never>;
export type AdmFileJobRowsHeaders = Record<string, never>;
export type AdmFileJobRowsResponse = Record<string, unknown>;
export type AdmFileJobRowsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmFileJobRowsPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admFileJobRows<T = AdmFileJobRowsResponse>(options: AdmFileJobRowsOptions): Promise<T> {
  const response = await orvalAdmFileJobRows(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmFileJobUploadBody = FormData;
export type AdmFileJobUploadPath = Record<string, never>;
export type AdmFileJobUploadQuery = { operationId: string; templateCode: string; templateVersion: number; format: string; dryRun?: boolean; reason: string };
export type AdmFileJobUploadHeaders = Record<string, never>;
export type AdmFileJobUploadResponse = Record<string, unknown>;
export type AdmFileJobUploadOptions = CpfGeneratedBaseOptions & { data: AdmFileJobUploadBody; path?: never; query?: AdmFileJobUploadQuery; headers?: CpfGeneratedHeaders; };
export async function admFileJobUpload<T = AdmFileJobUploadResponse>(options: AdmFileJobUploadOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmFileJobUpload(options.data, contractParams as Parameters<typeof orvalAdmFileJobUpload>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayCancelConnectionTestBody = Record<string, unknown>;
export type AdmGatewayCancelConnectionTestPath = { operationId: string };
export type AdmGatewayCancelConnectionTestQuery = Record<string, never>;
export type AdmGatewayCancelConnectionTestHeaders = Record<string, never>;
export type AdmGatewayCancelConnectionTestResponse = Record<string, unknown>;
export type AdmGatewayCancelConnectionTestOptions = CpfGeneratedBaseOptions & { data: AdmGatewayCancelConnectionTestBody; path: AdmGatewayCancelConnectionTestPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayCancelConnectionTest<T = AdmGatewayCancelConnectionTestResponse>(options: AdmGatewayCancelConnectionTestOptions): Promise<T> {
  const response = await orvalAdmGatewayCancelConnectionTest(options.path["operationId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayCapabilityBody = never;
export type AdmGatewayCapabilityPath = Record<string, never>;
export type AdmGatewayCapabilityQuery = Record<string, never>;
export type AdmGatewayCapabilityHeaders = Record<string, never>;
export type AdmGatewayCapabilityResponse = Record<string, unknown>;
export type AdmGatewayCapabilityOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayCapability<T = AdmGatewayCapabilityResponse>(options: AdmGatewayCapabilityOptions = {} as AdmGatewayCapabilityOptions): Promise<T> {
  const response = await orvalAdmGatewayCapability({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayChangeBindingStateBody = Record<string, unknown>;
export type AdmGatewayChangeBindingStatePath = { id: string };
export type AdmGatewayChangeBindingStateQuery = Record<string, never>;
export type AdmGatewayChangeBindingStateHeaders = Record<string, never>;
export type AdmGatewayChangeBindingStateResponse = Record<string, unknown>;
export type AdmGatewayChangeBindingStateOptions = CpfGeneratedBaseOptions & { data: AdmGatewayChangeBindingStateBody; path: AdmGatewayChangeBindingStatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayChangeBindingState<T = AdmGatewayChangeBindingStateResponse>(options: AdmGatewayChangeBindingStateOptions): Promise<T> {
  const response = await orvalAdmGatewayChangeBindingState(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayDeleteBindingBody = Record<string, unknown>;
export type AdmGatewayDeleteBindingPath = { id: string };
export type AdmGatewayDeleteBindingQuery = Record<string, never>;
export type AdmGatewayDeleteBindingHeaders = Record<string, never>;
export type AdmGatewayDeleteBindingResponse = Record<string, unknown>;
export type AdmGatewayDeleteBindingOptions = CpfGeneratedBaseOptions & { data: AdmGatewayDeleteBindingBody; path: AdmGatewayDeleteBindingPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayDeleteBinding<T = AdmGatewayDeleteBindingResponse>(options: AdmGatewayDeleteBindingOptions): Promise<T> {
  const response = await orvalAdmGatewayDeleteBinding(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayDeleteServerGroupBody = Record<string, unknown>;
export type AdmGatewayDeleteServerGroupPath = { id: string };
export type AdmGatewayDeleteServerGroupQuery = Record<string, never>;
export type AdmGatewayDeleteServerGroupHeaders = Record<string, never>;
export type AdmGatewayDeleteServerGroupResponse = Record<string, unknown>;
export type AdmGatewayDeleteServerGroupOptions = CpfGeneratedBaseOptions & { data: AdmGatewayDeleteServerGroupBody; path: AdmGatewayDeleteServerGroupPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayDeleteServerGroup<T = AdmGatewayDeleteServerGroupResponse>(options: AdmGatewayDeleteServerGroupOptions): Promise<T> {
  const response = await orvalAdmGatewayDeleteServerGroup(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindApplyStatusBody = never;
export type AdmGatewayFindApplyStatusPath = { id: string };
export type AdmGatewayFindApplyStatusQuery = { limit?: number };
export type AdmGatewayFindApplyStatusHeaders = Record<string, never>;
export type AdmGatewayFindApplyStatusResponse = Record<string, unknown>;
export type AdmGatewayFindApplyStatusOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmGatewayFindApplyStatusPath; query?: AdmGatewayFindApplyStatusQuery; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindApplyStatus<T = AdmGatewayFindApplyStatusResponse>(options: AdmGatewayFindApplyStatusOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmGatewayFindApplyStatus(options.path["id"], contractParams as Parameters<typeof orvalAdmGatewayFindApplyStatus>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindBindingsBody = never;
export type AdmGatewayFindBindingsPath = Record<string, never>;
export type AdmGatewayFindBindingsQuery = { environmentCode?: string; routeId?: string; status?: string; limit?: number };
export type AdmGatewayFindBindingsHeaders = Record<string, never>;
export type AdmGatewayFindBindingsResponse = Record<string, unknown>;
export type AdmGatewayFindBindingsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmGatewayFindBindingsQuery; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindBindings<T = AdmGatewayFindBindingsResponse>(options: AdmGatewayFindBindingsOptions = {} as AdmGatewayFindBindingsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmGatewayFindBindings(contractParams as Parameters<typeof orvalAdmGatewayFindBindings>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindConnectionTestOperationBody = never;
export type AdmGatewayFindConnectionTestOperationPath = { operationId: string };
export type AdmGatewayFindConnectionTestOperationQuery = Record<string, never>;
export type AdmGatewayFindConnectionTestOperationHeaders = Record<string, never>;
export type AdmGatewayFindConnectionTestOperationResponse = Record<string, unknown>;
export type AdmGatewayFindConnectionTestOperationOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmGatewayFindConnectionTestOperationPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindConnectionTestOperation<T = AdmGatewayFindConnectionTestOperationResponse>(options: AdmGatewayFindConnectionTestOperationOptions): Promise<T> {
  const response = await orvalAdmGatewayFindConnectionTestOperation(options.path["operationId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindConnectionTestsBody = never;
export type AdmGatewayFindConnectionTestsPath = { id: string };
export type AdmGatewayFindConnectionTestsQuery = { limit?: number };
export type AdmGatewayFindConnectionTestsHeaders = Record<string, never>;
export type AdmGatewayFindConnectionTestsResponse = Record<string, unknown>;
export type AdmGatewayFindConnectionTestsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmGatewayFindConnectionTestsPath; query?: AdmGatewayFindConnectionTestsQuery; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindConnectionTests<T = AdmGatewayFindConnectionTestsResponse>(options: AdmGatewayFindConnectionTestsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmGatewayFindConnectionTests(options.path["id"], contractParams as Parameters<typeof orvalAdmGatewayFindConnectionTests>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindGroupMembersBody = never;
export type AdmGatewayFindGroupMembersPath = { id: string };
export type AdmGatewayFindGroupMembersQuery = Record<string, never>;
export type AdmGatewayFindGroupMembersHeaders = Record<string, never>;
export type AdmGatewayFindGroupMembersResponse = Record<string, unknown>;
export type AdmGatewayFindGroupMembersOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmGatewayFindGroupMembersPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindGroupMembers<T = AdmGatewayFindGroupMembersResponse>(options: AdmGatewayFindGroupMembersOptions): Promise<T> {
  const response = await orvalAdmGatewayFindGroupMembers(options.path["id"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayFindServerGroupsBody = never;
export type AdmGatewayFindServerGroupsPath = Record<string, never>;
export type AdmGatewayFindServerGroupsQuery = { environmentCode?: string; serviceId?: string; status?: string; limit?: number };
export type AdmGatewayFindServerGroupsHeaders = Record<string, never>;
export type AdmGatewayFindServerGroupsResponse = Record<string, unknown>;
export type AdmGatewayFindServerGroupsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmGatewayFindServerGroupsQuery; headers?: CpfGeneratedHeaders; };
export async function admGatewayFindServerGroups<T = AdmGatewayFindServerGroupsResponse>(options: AdmGatewayFindServerGroupsOptions = {} as AdmGatewayFindServerGroupsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmGatewayFindServerGroups(contractParams as Parameters<typeof orvalAdmGatewayFindServerGroups>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayOperationsEventsBody = never;
export type AdmGatewayOperationsEventsPath = Record<string, never>;
export type AdmGatewayOperationsEventsQuery = { afterEventId?: string; limit?: number };
export type AdmGatewayOperationsEventsHeaders = Record<string, never>;
export type AdmGatewayOperationsEventsResponse = Record<string, unknown>;
export type AdmGatewayOperationsEventsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmGatewayOperationsEventsQuery; headers?: CpfGeneratedHeaders; };
export async function admGatewayOperationsEvents<T = AdmGatewayOperationsEventsResponse>(options: AdmGatewayOperationsEventsOptions = {} as AdmGatewayOperationsEventsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmGatewayOperationsEvents(contractParams as Parameters<typeof orvalAdmGatewayOperationsEvents>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayOperationsSnapshotBody = never;
export type AdmGatewayOperationsSnapshotPath = Record<string, never>;
export type AdmGatewayOperationsSnapshotQuery = Record<string, never>;
export type AdmGatewayOperationsSnapshotHeaders = Record<string, never>;
export type AdmGatewayOperationsSnapshotResponse = Record<string, unknown>;
export type AdmGatewayOperationsSnapshotOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayOperationsSnapshot<T = AdmGatewayOperationsSnapshotResponse>(options: AdmGatewayOperationsSnapshotOptions = {} as AdmGatewayOperationsSnapshotOptions): Promise<T> {
  const response = await orvalAdmGatewayOperationsSnapshot({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayOperationsStreamBody = never;
export type AdmGatewayOperationsStreamPath = Record<string, never>;
export type AdmGatewayOperationsStreamQuery = { afterEventId?: string };
export type AdmGatewayOperationsStreamHeaders = { "Last-Event-ID"?: string };
export type AdmGatewayOperationsStreamResponse = Record<string, unknown>;
export type AdmGatewayOperationsStreamOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmGatewayOperationsStreamQuery; headers?: CpfGeneratedHeaders & AdmGatewayOperationsStreamHeaders; };
export async function admGatewayOperationsStream<T = AdmGatewayOperationsStreamResponse>(options: AdmGatewayOperationsStreamOptions = {} as AdmGatewayOperationsStreamOptions): Promise<T> {
  const contractParams = { ...(options.query || {}), "Last-Event-ID": headerValue(options.headers, "Last-Event-ID") };
  const response = await orvalAdmGatewayOperationsStream(contractParams as Parameters<typeof orvalAdmGatewayOperationsStream>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayRequestConnectionTestBody = Record<string, unknown>;
export type AdmGatewayRequestConnectionTestPath = { id: string };
export type AdmGatewayRequestConnectionTestQuery = Record<string, never>;
export type AdmGatewayRequestConnectionTestHeaders = Record<string, never>;
export type AdmGatewayRequestConnectionTestResponse = Record<string, unknown>;
export type AdmGatewayRequestConnectionTestOptions = CpfGeneratedBaseOptions & { data: AdmGatewayRequestConnectionTestBody; path: AdmGatewayRequestConnectionTestPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayRequestConnectionTest<T = AdmGatewayRequestConnectionTestResponse>(options: AdmGatewayRequestConnectionTestOptions): Promise<T> {
  const response = await orvalAdmGatewayRequestConnectionTest(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewayRevalidateConnectionTestBody = Record<string, unknown>;
export type AdmGatewayRevalidateConnectionTestPath = { operationId: string };
export type AdmGatewayRevalidateConnectionTestQuery = Record<string, never>;
export type AdmGatewayRevalidateConnectionTestHeaders = Record<string, never>;
export type AdmGatewayRevalidateConnectionTestResponse = Record<string, unknown>;
export type AdmGatewayRevalidateConnectionTestOptions = CpfGeneratedBaseOptions & { data: AdmGatewayRevalidateConnectionTestBody; path: AdmGatewayRevalidateConnectionTestPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewayRevalidateConnectionTest<T = AdmGatewayRevalidateConnectionTestResponse>(options: AdmGatewayRevalidateConnectionTestOptions): Promise<T> {
  const response = await orvalAdmGatewayRevalidateConnectionTest(options.path["operationId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewaySaveBindingBody = Record<string, unknown>;
export type AdmGatewaySaveBindingPath = Record<string, never>;
export type AdmGatewaySaveBindingQuery = Record<string, never>;
export type AdmGatewaySaveBindingHeaders = Record<string, never>;
export type AdmGatewaySaveBindingResponse = Record<string, unknown>;
export type AdmGatewaySaveBindingOptions = CpfGeneratedBaseOptions & { data: AdmGatewaySaveBindingBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewaySaveBinding<T = AdmGatewaySaveBindingResponse>(options: AdmGatewaySaveBindingOptions): Promise<T> {
  const response = await orvalAdmGatewaySaveBinding(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmGatewaySaveServerGroupBody = Record<string, unknown>;
export type AdmGatewaySaveServerGroupPath = Record<string, never>;
export type AdmGatewaySaveServerGroupQuery = Record<string, never>;
export type AdmGatewaySaveServerGroupHeaders = Record<string, never>;
export type AdmGatewaySaveServerGroupResponse = Record<string, unknown>;
export type AdmGatewaySaveServerGroupOptions = CpfGeneratedBaseOptions & { data: AdmGatewaySaveServerGroupBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admGatewaySaveServerGroup<T = AdmGatewaySaveServerGroupResponse>(options: AdmGatewaySaveServerGroupOptions): Promise<T> {
  const response = await orvalAdmGatewaySaveServerGroup(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmHealthInstanceDetailBody = never;
export type AdmHealthInstanceDetailPath = { systemId: string; instanceId: string };
export type AdmHealthInstanceDetailQuery = Record<string, never>;
export type AdmHealthInstanceDetailHeaders = Record<string, never>;
export type AdmHealthInstanceDetailResponse = Record<string, unknown>;
export type AdmHealthInstanceDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmHealthInstanceDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admHealthInstanceDetail<T = AdmHealthInstanceDetailResponse>(options: AdmHealthInstanceDetailOptions): Promise<T> {
  const response = await orvalAdmHealthInstanceDetail(options.path["systemId"], options.path["instanceId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmHealthInstanceListBody = never;
export type AdmHealthInstanceListPath = Record<string, never>;
export type AdmHealthInstanceListQuery = { systemId?: string; readiness?: string; includeStale?: boolean; page?: number; size?: number };
export type AdmHealthInstanceListHeaders = Record<string, never>;
export type AdmHealthInstanceListResponse = Record<string, unknown>;
export type AdmHealthInstanceListOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmHealthInstanceListQuery; headers?: CpfGeneratedHeaders; };
export async function admHealthInstanceList<T = AdmHealthInstanceListResponse>(options: AdmHealthInstanceListOptions = {} as AdmHealthInstanceListOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmHealthInstanceList(contractParams as Parameters<typeof orvalAdmHealthInstanceList>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentAcknowledgeBody = { approvalRequestId?: string; expectedVersion: number; idempotencyKey?: string; reason?: string };
export type AdmIncidentAcknowledgePath = { incidentId: number };
export type AdmIncidentAcknowledgeQuery = Record<string, never>;
export type AdmIncidentAcknowledgeHeaders = Record<string, never>;
export type AdmIncidentAcknowledgeResponse = Record<string, unknown>;
export type AdmIncidentAcknowledgeOptions = CpfGeneratedBaseOptions & { data: AdmIncidentAcknowledgeBody; path: AdmIncidentAcknowledgePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentAcknowledge<T = AdmIncidentAcknowledgeResponse>(options: AdmIncidentAcknowledgeOptions): Promise<T> {
  const response = await orvalAdmIncidentAcknowledge(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentCreateIncidentBody = Record<string, unknown>;
export type AdmIncidentCreateIncidentPath = Record<string, never>;
export type AdmIncidentCreateIncidentQuery = Record<string, never>;
export type AdmIncidentCreateIncidentHeaders = Record<string, never>;
export type AdmIncidentCreateIncidentResponse = Record<string, unknown>;
export type AdmIncidentCreateIncidentOptions = CpfGeneratedBaseOptions & { data: AdmIncidentCreateIncidentBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentCreateIncident<T = AdmIncidentCreateIncidentResponse>(options: AdmIncidentCreateIncidentOptions): Promise<T> {
  const response = await orvalAdmIncidentCreateIncident(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentCreateMaintenanceBody = { approvalRequestId?: string; endsAt?: string; expectedVersion: number; idempotencyKey?: string; maintenanceCode?: string; reason?: string; startsAt?: string; targetId?: string; targetType?: string; useYn?: string };
export type AdmIncidentCreateMaintenancePath = Record<string, never>;
export type AdmIncidentCreateMaintenanceQuery = Record<string, never>;
export type AdmIncidentCreateMaintenanceHeaders = Record<string, never>;
export type AdmIncidentCreateMaintenanceResponse = Record<string, unknown>;
export type AdmIncidentCreateMaintenanceOptions = CpfGeneratedBaseOptions & { data: AdmIncidentCreateMaintenanceBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentCreateMaintenance<T = AdmIncidentCreateMaintenanceResponse>(options: AdmIncidentCreateMaintenanceOptions): Promise<T> {
  const response = await orvalAdmIncidentCreateMaintenance(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentCreatePolicyBody = { approvalRequestId?: string; escalationMinutes: number; eventSubType?: string; eventType?: string; expectedVersion: number; idempotencyKey?: string; policyCode?: string; reason?: string; receiverGroup?: string; severity?: string; thresholdCount: number; useYn?: string; windowSeconds: number };
export type AdmIncidentCreatePolicyPath = Record<string, never>;
export type AdmIncidentCreatePolicyQuery = Record<string, never>;
export type AdmIncidentCreatePolicyHeaders = Record<string, never>;
export type AdmIncidentCreatePolicyResponse = Record<string, unknown>;
export type AdmIncidentCreatePolicyOptions = CpfGeneratedBaseOptions & { data: AdmIncidentCreatePolicyBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentCreatePolicy<T = AdmIncidentCreatePolicyResponse>(options: AdmIncidentCreatePolicyOptions): Promise<T> {
  const response = await orvalAdmIncidentCreatePolicy(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentEscalateBody = { approvalRequestId?: string; expectedVersion: number; idempotencyKey?: string; reason?: string };
export type AdmIncidentEscalatePath = { incidentId: number };
export type AdmIncidentEscalateQuery = Record<string, never>;
export type AdmIncidentEscalateHeaders = Record<string, never>;
export type AdmIncidentEscalateResponse = Record<string, unknown>;
export type AdmIncidentEscalateOptions = CpfGeneratedBaseOptions & { data: AdmIncidentEscalateBody; path: AdmIncidentEscalatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentEscalate<T = AdmIncidentEscalateResponse>(options: AdmIncidentEscalateOptions): Promise<T> {
  const response = await orvalAdmIncidentEscalate(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentFindIncidentBody = never;
export type AdmIncidentFindIncidentPath = { incidentId: number };
export type AdmIncidentFindIncidentQuery = Record<string, never>;
export type AdmIncidentFindIncidentHeaders = Record<string, never>;
export type AdmIncidentFindIncidentResponse = Record<string, unknown>;
export type AdmIncidentFindIncidentOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmIncidentFindIncidentPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentFindIncident<T = AdmIncidentFindIncidentResponse>(options: AdmIncidentFindIncidentOptions): Promise<T> {
  const response = await orvalAdmIncidentFindIncident(options.path["incidentId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentFindIncidentsBody = never;
export type AdmIncidentFindIncidentsPath = Record<string, never>;
export type AdmIncidentFindIncidentsQuery = { status?: string; page?: number; size?: number };
export type AdmIncidentFindIncidentsHeaders = Record<string, never>;
export type AdmIncidentFindIncidentsResponse = Record<string, unknown>;
export type AdmIncidentFindIncidentsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmIncidentFindIncidentsQuery; headers?: CpfGeneratedHeaders; };
export async function admIncidentFindIncidents<T = AdmIncidentFindIncidentsResponse>(options: AdmIncidentFindIncidentsOptions = {} as AdmIncidentFindIncidentsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIncidentFindIncidents(contractParams as Parameters<typeof orvalAdmIncidentFindIncidents>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentFindMaintenanceBody = never;
export type AdmIncidentFindMaintenancePath = Record<string, never>;
export type AdmIncidentFindMaintenanceQuery = { page?: number; size?: number };
export type AdmIncidentFindMaintenanceHeaders = Record<string, never>;
export type AdmIncidentFindMaintenanceResponse = Record<string, unknown>;
export type AdmIncidentFindMaintenanceOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmIncidentFindMaintenanceQuery; headers?: CpfGeneratedHeaders; };
export async function admIncidentFindMaintenance<T = AdmIncidentFindMaintenanceResponse>(options: AdmIncidentFindMaintenanceOptions = {} as AdmIncidentFindMaintenanceOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIncidentFindMaintenance(contractParams as Parameters<typeof orvalAdmIncidentFindMaintenance>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentFindPoliciesBody = never;
export type AdmIncidentFindPoliciesPath = Record<string, never>;
export type AdmIncidentFindPoliciesQuery = { page?: number; size?: number };
export type AdmIncidentFindPoliciesHeaders = Record<string, never>;
export type AdmIncidentFindPoliciesResponse = Record<string, unknown>;
export type AdmIncidentFindPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmIncidentFindPoliciesQuery; headers?: CpfGeneratedHeaders; };
export async function admIncidentFindPolicies<T = AdmIncidentFindPoliciesResponse>(options: AdmIncidentFindPoliciesOptions = {} as AdmIncidentFindPoliciesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIncidentFindPolicies(contractParams as Parameters<typeof orvalAdmIncidentFindPolicies>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentFindTimelineBody = never;
export type AdmIncidentFindTimelinePath = { incidentId: number };
export type AdmIncidentFindTimelineQuery = Record<string, never>;
export type AdmIncidentFindTimelineHeaders = Record<string, never>;
export type AdmIncidentFindTimelineResponse = Record<string, unknown>;
export type AdmIncidentFindTimelineOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmIncidentFindTimelinePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentFindTimeline<T = AdmIncidentFindTimelineResponse>(options: AdmIncidentFindTimelineOptions): Promise<T> {
  const response = await orvalAdmIncidentFindTimeline(options.path["incidentId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentIngestSignalBody = { correlationId?: string; idempotencyKey?: string; occurredAt?: string; policyCode?: string; sourceId?: string; sourceType?: string; summary?: string; title?: string; transactionId?: string };
export type AdmIncidentIngestSignalPath = Record<string, never>;
export type AdmIncidentIngestSignalQuery = Record<string, never>;
export type AdmIncidentIngestSignalHeaders = Record<string, never>;
export type AdmIncidentIngestSignalResponse = Record<string, unknown>;
export type AdmIncidentIngestSignalOptions = CpfGeneratedBaseOptions & { data: AdmIncidentIngestSignalBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentIngestSignal<T = AdmIncidentIngestSignalResponse>(options: AdmIncidentIngestSignalOptions): Promise<T> {
  const response = await orvalAdmIncidentIngestSignal(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentRecordPostmortemBody = { approvalRequestId?: string; expectedVersion: number; idempotencyKey?: string; reason?: string };
export type AdmIncidentRecordPostmortemPath = { incidentId: number };
export type AdmIncidentRecordPostmortemQuery = Record<string, never>;
export type AdmIncidentRecordPostmortemHeaders = Record<string, never>;
export type AdmIncidentRecordPostmortemResponse = Record<string, unknown>;
export type AdmIncidentRecordPostmortemOptions = CpfGeneratedBaseOptions & { data: AdmIncidentRecordPostmortemBody; path: AdmIncidentRecordPostmortemPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentRecordPostmortem<T = AdmIncidentRecordPostmortemResponse>(options: AdmIncidentRecordPostmortemOptions): Promise<T> {
  const response = await orvalAdmIncidentRecordPostmortem(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentReopenBody = { approvalRequestId?: string; expectedVersion: number; idempotencyKey?: string; reason?: string };
export type AdmIncidentReopenPath = { incidentId: number };
export type AdmIncidentReopenQuery = Record<string, never>;
export type AdmIncidentReopenHeaders = Record<string, never>;
export type AdmIncidentReopenResponse = Record<string, unknown>;
export type AdmIncidentReopenOptions = CpfGeneratedBaseOptions & { data: AdmIncidentReopenBody; path: AdmIncidentReopenPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentReopen<T = AdmIncidentReopenResponse>(options: AdmIncidentReopenOptions): Promise<T> {
  const response = await orvalAdmIncidentReopen(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentResolveBody = { approvalRequestId?: string; expectedVersion: number; idempotencyKey?: string; reason?: string };
export type AdmIncidentResolvePath = { incidentId: number };
export type AdmIncidentResolveQuery = Record<string, never>;
export type AdmIncidentResolveHeaders = Record<string, never>;
export type AdmIncidentResolveResponse = Record<string, unknown>;
export type AdmIncidentResolveOptions = CpfGeneratedBaseOptions & { data: AdmIncidentResolveBody; path: AdmIncidentResolvePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentResolve<T = AdmIncidentResolveResponse>(options: AdmIncidentResolveOptions): Promise<T> {
  const response = await orvalAdmIncidentResolve(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentTransitionIncidentBody = Record<string, unknown>;
export type AdmIncidentTransitionIncidentPath = { incidentId: number };
export type AdmIncidentTransitionIncidentQuery = Record<string, never>;
export type AdmIncidentTransitionIncidentHeaders = Record<string, never>;
export type AdmIncidentTransitionIncidentResponse = Record<string, unknown>;
export type AdmIncidentTransitionIncidentOptions = CpfGeneratedBaseOptions & { data: AdmIncidentTransitionIncidentBody; path: AdmIncidentTransitionIncidentPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentTransitionIncident<T = AdmIncidentTransitionIncidentResponse>(options: AdmIncidentTransitionIncidentOptions): Promise<T> {
  const response = await orvalAdmIncidentTransitionIncident(options.path["incidentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentUpdateMaintenanceBody = { approvalRequestId?: string; endsAt?: string; expectedVersion: number; idempotencyKey?: string; maintenanceCode?: string; reason?: string; startsAt?: string; targetId?: string; targetType?: string; useYn?: string };
export type AdmIncidentUpdateMaintenancePath = { maintenanceId: number };
export type AdmIncidentUpdateMaintenanceQuery = Record<string, never>;
export type AdmIncidentUpdateMaintenanceHeaders = Record<string, never>;
export type AdmIncidentUpdateMaintenanceResponse = Record<string, unknown>;
export type AdmIncidentUpdateMaintenanceOptions = CpfGeneratedBaseOptions & { data: AdmIncidentUpdateMaintenanceBody; path: AdmIncidentUpdateMaintenancePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentUpdateMaintenance<T = AdmIncidentUpdateMaintenanceResponse>(options: AdmIncidentUpdateMaintenanceOptions): Promise<T> {
  const response = await orvalAdmIncidentUpdateMaintenance(options.path["maintenanceId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIncidentUpdatePolicyBody = { approvalRequestId?: string; escalationMinutes: number; eventSubType?: string; eventType?: string; expectedVersion: number; idempotencyKey?: string; policyCode?: string; reason?: string; receiverGroup?: string; severity?: string; thresholdCount: number; useYn?: string; windowSeconds: number };
export type AdmIncidentUpdatePolicyPath = { policyId: number };
export type AdmIncidentUpdatePolicyQuery = Record<string, never>;
export type AdmIncidentUpdatePolicyHeaders = Record<string, never>;
export type AdmIncidentUpdatePolicyResponse = Record<string, unknown>;
export type AdmIncidentUpdatePolicyOptions = CpfGeneratedBaseOptions & { data: AdmIncidentUpdatePolicyBody; path: AdmIncidentUpdatePolicyPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIncidentUpdatePolicy<T = AdmIncidentUpdatePolicyResponse>(options: AdmIncidentUpdatePolicyOptions): Promise<T> {
  const response = await orvalAdmIncidentUpdatePolicy(options.path["policyId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationCryptoStatusBody = never;
export type AdmIntegrationCryptoStatusPath = Record<string, never>;
export type AdmIntegrationCryptoStatusQuery = Record<string, never>;
export type AdmIntegrationCryptoStatusHeaders = Record<string, never>;
export type AdmIntegrationCryptoStatusResponse = Record<string, unknown>;
export type AdmIntegrationCryptoStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIntegrationCryptoStatus<T = AdmIntegrationCryptoStatusResponse>(options: AdmIntegrationCryptoStatusOptions = {} as AdmIntegrationCryptoStatusOptions): Promise<T> {
  const response = await orvalAdmIntegrationCryptoStatus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationDataQualityCorrectionApprovalRequestBody = { corrected: Record<string, unknown>; expectedVersion: number; idempotencyKey: string; reason: string };
export type AdmIntegrationDataQualityCorrectionApprovalRequestPath = { id: string };
export type AdmIntegrationDataQualityCorrectionApprovalRequestQuery = Record<string, never>;
export type AdmIntegrationDataQualityCorrectionApprovalRequestHeaders = Record<string, never>;
export type AdmIntegrationDataQualityCorrectionApprovalRequestResponse = Record<string, unknown>;
export type AdmIntegrationDataQualityCorrectionApprovalRequestOptions = CpfGeneratedBaseOptions & { data: AdmIntegrationDataQualityCorrectionApprovalRequestBody; path: AdmIntegrationDataQualityCorrectionApprovalRequestPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIntegrationDataQualityCorrectionApprovalRequest<T = AdmIntegrationDataQualityCorrectionApprovalRequestResponse>(options: AdmIntegrationDataQualityCorrectionApprovalRequestOptions): Promise<T> {
  const response = await orvalAdmIntegrationDataQualityCorrectionApprovalRequest(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationDataQualityCorrectionExecuteBody = { reason: string };
export type AdmIntegrationDataQualityCorrectionExecutePath = { approvalRequestId: number };
export type AdmIntegrationDataQualityCorrectionExecuteQuery = Record<string, never>;
export type AdmIntegrationDataQualityCorrectionExecuteHeaders = Record<string, never>;
export type AdmIntegrationDataQualityCorrectionExecuteResponse = Record<string, unknown>;
export type AdmIntegrationDataQualityCorrectionExecuteOptions = CpfGeneratedBaseOptions & { data: AdmIntegrationDataQualityCorrectionExecuteBody; path: AdmIntegrationDataQualityCorrectionExecutePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIntegrationDataQualityCorrectionExecute<T = AdmIntegrationDataQualityCorrectionExecuteResponse>(options: AdmIntegrationDataQualityCorrectionExecuteOptions): Promise<T> {
  const response = await orvalAdmIntegrationDataQualityCorrectionExecute(options.path["approvalRequestId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationDataQualityReplayBody = { expectedVersion: number; idempotencyKey: string; reason: string };
export type AdmIntegrationDataQualityReplayPath = { id: string };
export type AdmIntegrationDataQualityReplayQuery = Record<string, never>;
export type AdmIntegrationDataQualityReplayHeaders = Record<string, never>;
export type AdmIntegrationDataQualityReplayResponse = Record<string, unknown>;
export type AdmIntegrationDataQualityReplayOptions = CpfGeneratedBaseOptions & { data: AdmIntegrationDataQualityReplayBody; path: AdmIntegrationDataQualityReplayPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIntegrationDataQualityReplay<T = AdmIntegrationDataQualityReplayResponse>(options: AdmIntegrationDataQualityReplayOptions): Promise<T> {
  const response = await orvalAdmIntegrationDataQualityReplay(options.path["id"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationDataQualityValidateBody = Record<string, unknown>;
export type AdmIntegrationDataQualityValidatePath = { recordId: string };
export type AdmIntegrationDataQualityValidateQuery = Record<string, never>;
export type AdmIntegrationDataQualityValidateHeaders = Record<string, never>;
export type AdmIntegrationDataQualityValidateResponse = Record<string, unknown>;
export type AdmIntegrationDataQualityValidateOptions = CpfGeneratedBaseOptions & { data: AdmIntegrationDataQualityValidateBody; path: AdmIntegrationDataQualityValidatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admIntegrationDataQualityValidate<T = AdmIntegrationDataQualityValidateResponse>(options: AdmIntegrationDataQualityValidateOptions): Promise<T> {
  const response = await orvalAdmIntegrationDataQualityValidate(options.path["recordId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationTimeHealthBody = never;
export type AdmIntegrationTimeHealthPath = Record<string, never>;
export type AdmIntegrationTimeHealthQuery = { zone?: string; maxSkewMillis?: number };
export type AdmIntegrationTimeHealthHeaders = Record<string, never>;
export type AdmIntegrationTimeHealthResponse = Record<string, unknown>;
export type AdmIntegrationTimeHealthOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmIntegrationTimeHealthQuery; headers?: CpfGeneratedHeaders; };
export async function admIntegrationTimeHealth<T = AdmIntegrationTimeHealthResponse>(options: AdmIntegrationTimeHealthOptions = {} as AdmIntegrationTimeHealthOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIntegrationTimeHealth(contractParams as Parameters<typeof orvalAdmIntegrationTimeHealth>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationWebhookDlqBody = never;
export type AdmIntegrationWebhookDlqPath = Record<string, never>;
export type AdmIntegrationWebhookDlqQuery = { limit?: number };
export type AdmIntegrationWebhookDlqHeaders = Record<string, never>;
export type AdmIntegrationWebhookDlqResponse = Record<string, unknown>;
export type AdmIntegrationWebhookDlqOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmIntegrationWebhookDlqQuery; headers?: CpfGeneratedHeaders; };
export async function admIntegrationWebhookDlq<T = AdmIntegrationWebhookDlqResponse>(options: AdmIntegrationWebhookDlqOptions = {} as AdmIntegrationWebhookDlqOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIntegrationWebhookDlq(contractParams as Parameters<typeof orvalAdmIntegrationWebhookDlq>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmIntegrationWebhookReplayBody = never;
export type AdmIntegrationWebhookReplayPath = { id: string };
export type AdmIntegrationWebhookReplayQuery = { expectedVersion: number; reason: string };
export type AdmIntegrationWebhookReplayHeaders = Record<string, never>;
export type AdmIntegrationWebhookReplayResponse = Record<string, unknown>;
export type AdmIntegrationWebhookReplayOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmIntegrationWebhookReplayPath; query?: AdmIntegrationWebhookReplayQuery; headers?: CpfGeneratedHeaders; };
export async function admIntegrationWebhookReplay<T = AdmIntegrationWebhookReplayResponse>(options: AdmIntegrationWebhookReplayOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmIntegrationWebhookReplay(options.path["id"], contractParams as Parameters<typeof orvalAdmIntegrationWebhookReplay>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogExportCreateBody = { action?: string; format?: string; logId?: string; reason?: string; requestedBy?: string };
export type AdmLogExportCreatePath = Record<string, never>;
export type AdmLogExportCreateQuery = Record<string, never>;
export type AdmLogExportCreateHeaders = Record<string, never>;
export type AdmLogExportCreateResponse = Record<string, unknown>;
export type AdmLogExportCreateOptions = CpfGeneratedBaseOptions & { data: AdmLogExportCreateBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogExportCreate<T = AdmLogExportCreateResponse>(options: AdmLogExportCreateOptions): Promise<T> {
  const response = await orvalAdmLogExportCreate(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogExportDownloadBody = never;
export type AdmLogExportDownloadPath = { exportId: string };
export type AdmLogExportDownloadQuery = Record<string, never>;
export type AdmLogExportDownloadHeaders = Record<string, never>;
export type AdmLogExportDownloadResponse = Record<string, unknown>;
export type AdmLogExportDownloadOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmLogExportDownloadPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogExportDownload<T = AdmLogExportDownloadResponse>(options: AdmLogExportDownloadOptions): Promise<T> {
  const response = await orvalAdmLogExportDownload(options.path["exportId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogFindLogsBody = never;
export type AdmLogFindLogsPath = Record<string, never>;
export type AdmLogFindLogsQuery = { transactionId?: string; traceId?: string; businessTransactionId?: string; memberNo?: string; customerNo?: string; uri?: string; responseCode?: string; httpStatus?: number; channelCode?: string; logType?: string; moduleId?: string; wasId?: string; serverInstanceId?: string; hostName?: string; systemCode?: string; domainCode?: string; application?: string; starterId?: string; capabilityId?: string; provider?: string; operation?: string; limit?: number };
export type AdmLogFindLogsHeaders = Record<string, never>;
export type AdmLogFindLogsResponse = Record<string, unknown>;
export type AdmLogFindLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogFindLogsQuery; headers?: CpfGeneratedHeaders; };
export async function admLogFindLogs<T = AdmLogFindLogsResponse>(options: AdmLogFindLogsOptions = {} as AdmLogFindLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogFindLogs(contractParams as Parameters<typeof orvalAdmLogFindLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogGetLogDetailBody = never;
export type AdmLogGetLogDetailPath = { logIdx: number };
export type AdmLogGetLogDetailQuery = Record<string, never>;
export type AdmLogGetLogDetailHeaders = Record<string, never>;
export type AdmLogGetLogDetailResponse = Record<string, unknown>;
export type AdmLogGetLogDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmLogGetLogDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogGetLogDetail<T = AdmLogGetLogDetailResponse>(options: AdmLogGetLogDetailOptions): Promise<T> {
  const response = await orvalAdmLogGetLogDetail(options.path["logIdx"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyAuditFindPolicyAuditsBody = never;
export type AdmLogPolicyAuditFindPolicyAuditsPath = Record<string, never>;
export type AdmLogPolicyAuditFindPolicyAuditsQuery = { operatorId?: string; actionType?: string; targetType?: string; targetId?: string; policyId?: number; overrideId?: number; limit?: number };
export type AdmLogPolicyAuditFindPolicyAuditsHeaders = Record<string, never>;
export type AdmLogPolicyAuditFindPolicyAuditsResponse = Record<string, unknown>;
export type AdmLogPolicyAuditFindPolicyAuditsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyAuditFindPolicyAuditsQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyAuditFindPolicyAudits<T = AdmLogPolicyAuditFindPolicyAuditsResponse>(options: AdmLogPolicyAuditFindPolicyAuditsOptions = {} as AdmLogPolicyAuditFindPolicyAuditsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyAuditFindPolicyAudits(contractParams as Parameters<typeof orvalAdmLogPolicyAuditFindPolicyAudits>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyClearCacheBody = never;
export type AdmLogPolicyClearCachePath = Record<string, never>;
export type AdmLogPolicyClearCacheQuery = { reason: string };
export type AdmLogPolicyClearCacheHeaders = Record<string, never>;
export type AdmLogPolicyClearCacheResponse = Record<string, unknown>;
export type AdmLogPolicyClearCacheOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyClearCacheQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyClearCache<T = AdmLogPolicyClearCacheResponse>(options: AdmLogPolicyClearCacheOptions = {} as AdmLogPolicyClearCacheOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyClearCache(contractParams as Parameters<typeof orvalAdmLogPolicyClearCache>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyCreateOverrideBody = { approvedBy?: string; dbLogEnabledYn?: string; effectiveEndAt?: string; effectiveStartAt?: string; errorStackCaptureMode?: string; fieldAllowlist?: string; fileLogEnabledYn?: string; headerAllowlist?: string; logLevel?: string; maskingPolicyKey?: string; maxHeaderBytes?: number; maxQueryBytes?: number; maxRequestBodyBytes?: number; maxResponseBodyBytes?: number; maxStackBytes?: number; policyId?: number; queryAllowlist?: string; queryCaptureMode?: string; reason?: string; requestBodyCaptureMode?: string; requestHeaderCaptureMode?: string; responseBodyCaptureMode?: string; responseHeaderCaptureMode?: string; targetId?: string; targetType?: string };
export type AdmLogPolicyCreateOverridePath = Record<string, never>;
export type AdmLogPolicyCreateOverrideQuery = Record<string, never>;
export type AdmLogPolicyCreateOverrideHeaders = Record<string, never>;
export type AdmLogPolicyCreateOverrideResponse = Record<string, unknown>;
export type AdmLogPolicyCreateOverrideOptions = CpfGeneratedBaseOptions & { data: AdmLogPolicyCreateOverrideBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyCreateOverride<T = AdmLogPolicyCreateOverrideResponse>(options: AdmLogPolicyCreateOverrideOptions): Promise<T> {
  const response = await orvalAdmLogPolicyCreateOverride(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyCreatePolicyBody = { activeYn?: string; dbLogEnabledYn?: string; description?: string; errorStackCaptureMode?: string; fieldAllowlist?: string; fileLogEnabledYn?: string; headerAllowlist?: string; logLevel?: string; maskingPolicyKey?: string; maxHeaderBytes?: number; maxQueryBytes?: number; maxRequestBodyBytes?: number; maxResponseBodyBytes?: number; maxStackBytes?: number; policyKey?: string; policyName?: string; priority?: number; queryAllowlist?: string; queryCaptureMode?: string; reason?: string; requestBodyCaptureMode?: string; requestHeaderCaptureMode?: string; responseBodyCaptureMode?: string; responseHeaderCaptureMode?: string; retentionDays?: number; samplingRate?: number; targetId?: string; targetType?: string };
export type AdmLogPolicyCreatePolicyPath = Record<string, never>;
export type AdmLogPolicyCreatePolicyQuery = Record<string, never>;
export type AdmLogPolicyCreatePolicyHeaders = Record<string, never>;
export type AdmLogPolicyCreatePolicyResponse = Record<string, unknown>;
export type AdmLogPolicyCreatePolicyOptions = CpfGeneratedBaseOptions & { data: AdmLogPolicyCreatePolicyBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyCreatePolicy<T = AdmLogPolicyCreatePolicyResponse>(options: AdmLogPolicyCreatePolicyOptions): Promise<T> {
  const response = await orvalAdmLogPolicyCreatePolicy(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyCreateTraceBoostBody = { apiPath?: string; businessTransactionId?: string; durationMsGreaterThan?: number; failureCode?: string; logLevel?: string; policyId?: number; reason?: string; status?: string; transactionId?: string; ttlSeconds?: number };
export type AdmLogPolicyCreateTraceBoostPath = Record<string, never>;
export type AdmLogPolicyCreateTraceBoostQuery = Record<string, never>;
export type AdmLogPolicyCreateTraceBoostHeaders = Record<string, never>;
export type AdmLogPolicyCreateTraceBoostResponse = Record<string, unknown>;
export type AdmLogPolicyCreateTraceBoostOptions = CpfGeneratedBaseOptions & { data: AdmLogPolicyCreateTraceBoostBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyCreateTraceBoost<T = AdmLogPolicyCreateTraceBoostResponse>(options: AdmLogPolicyCreateTraceBoostOptions): Promise<T> {
  const response = await orvalAdmLogPolicyCreateTraceBoost(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyDisableOverrideBody = never;
export type AdmLogPolicyDisableOverridePath = { overrideId: number };
export type AdmLogPolicyDisableOverrideQuery = { reason: string };
export type AdmLogPolicyDisableOverrideHeaders = Record<string, never>;
export type AdmLogPolicyDisableOverrideResponse = Record<string, unknown>;
export type AdmLogPolicyDisableOverrideOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmLogPolicyDisableOverridePath; query?: AdmLogPolicyDisableOverrideQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyDisableOverride<T = AdmLogPolicyDisableOverrideResponse>(options: AdmLogPolicyDisableOverrideOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyDisableOverride(options.path["overrideId"], contractParams as Parameters<typeof orvalAdmLogPolicyDisableOverride>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyDisablePolicyBody = never;
export type AdmLogPolicyDisablePolicyPath = { policyId: number };
export type AdmLogPolicyDisablePolicyQuery = { reason: string };
export type AdmLogPolicyDisablePolicyHeaders = Record<string, never>;
export type AdmLogPolicyDisablePolicyResponse = Record<string, unknown>;
export type AdmLogPolicyDisablePolicyOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmLogPolicyDisablePolicyPath; query?: AdmLogPolicyDisablePolicyQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyDisablePolicy<T = AdmLogPolicyDisablePolicyResponse>(options: AdmLogPolicyDisablePolicyOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyDisablePolicy(options.path["policyId"], contractParams as Parameters<typeof orvalAdmLogPolicyDisablePolicy>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyDistributionStatusBody = never;
export type AdmLogPolicyDistributionStatusPath = Record<string, never>;
export type AdmLogPolicyDistributionStatusQuery = { targetType?: string; targetId?: string; limit?: number };
export type AdmLogPolicyDistributionStatusHeaders = Record<string, never>;
export type AdmLogPolicyDistributionStatusResponse = Record<string, unknown>;
export type AdmLogPolicyDistributionStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyDistributionStatusQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyDistributionStatus<T = AdmLogPolicyDistributionStatusResponse>(options: AdmLogPolicyDistributionStatusOptions = {} as AdmLogPolicyDistributionStatusOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyDistributionStatus(contractParams as Parameters<typeof orvalAdmLogPolicyDistributionStatus>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyFindPoliciesBody = never;
export type AdmLogPolicyFindPoliciesPath = Record<string, never>;
export type AdmLogPolicyFindPoliciesQuery = { targetType?: string; targetId?: string; activeYn?: string; limit?: number };
export type AdmLogPolicyFindPoliciesHeaders = Record<string, never>;
export type AdmLogPolicyFindPoliciesResponse = Record<string, unknown>;
export type AdmLogPolicyFindPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyFindPoliciesQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyFindPolicies<T = AdmLogPolicyFindPoliciesResponse>(options: AdmLogPolicyFindPoliciesOptions = {} as AdmLogPolicyFindPoliciesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyFindPolicies(contractParams as Parameters<typeof orvalAdmLogPolicyFindPolicies>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyFindPolicyBody = never;
export type AdmLogPolicyFindPolicyPath = { policyId: number };
export type AdmLogPolicyFindPolicyQuery = Record<string, never>;
export type AdmLogPolicyFindPolicyHeaders = Record<string, never>;
export type AdmLogPolicyFindPolicyResponse = Record<string, unknown>;
export type AdmLogPolicyFindPolicyOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmLogPolicyFindPolicyPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyFindPolicy<T = AdmLogPolicyFindPolicyResponse>(options: AdmLogPolicyFindPolicyOptions): Promise<T> {
  const response = await orvalAdmLogPolicyFindPolicy(options.path["policyId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyFindTraceBoostHistoryBody = never;
export type AdmLogPolicyFindTraceBoostHistoryPath = Record<string, never>;
export type AdmLogPolicyFindTraceBoostHistoryQuery = { limit?: number };
export type AdmLogPolicyFindTraceBoostHistoryHeaders = Record<string, never>;
export type AdmLogPolicyFindTraceBoostHistoryResponse = Record<string, unknown>;
export type AdmLogPolicyFindTraceBoostHistoryOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyFindTraceBoostHistoryQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyFindTraceBoostHistory<T = AdmLogPolicyFindTraceBoostHistoryResponse>(options: AdmLogPolicyFindTraceBoostHistoryOptions = {} as AdmLogPolicyFindTraceBoostHistoryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyFindTraceBoostHistory(contractParams as Parameters<typeof orvalAdmLogPolicyFindTraceBoostHistory>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyFindTraceBoostRuntimeStateBody = never;
export type AdmLogPolicyFindTraceBoostRuntimeStatePath = Record<string, never>;
export type AdmLogPolicyFindTraceBoostRuntimeStateQuery = { limit?: number };
export type AdmLogPolicyFindTraceBoostRuntimeStateHeaders = Record<string, never>;
export type AdmLogPolicyFindTraceBoostRuntimeStateResponse = Record<string, unknown>;
export type AdmLogPolicyFindTraceBoostRuntimeStateOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyFindTraceBoostRuntimeStateQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyFindTraceBoostRuntimeState<T = AdmLogPolicyFindTraceBoostRuntimeStateResponse>(options: AdmLogPolicyFindTraceBoostRuntimeStateOptions = {} as AdmLogPolicyFindTraceBoostRuntimeStateOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyFindTraceBoostRuntimeState(contractParams as Parameters<typeof orvalAdmLogPolicyFindTraceBoostRuntimeState>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyRefreshCacheBody = never;
export type AdmLogPolicyRefreshCachePath = Record<string, never>;
export type AdmLogPolicyRefreshCacheQuery = { targetType: string; targetId: string; reason: string };
export type AdmLogPolicyRefreshCacheHeaders = Record<string, never>;
export type AdmLogPolicyRefreshCacheResponse = Record<string, unknown>;
export type AdmLogPolicyRefreshCacheOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmLogPolicyRefreshCacheQuery; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyRefreshCache<T = AdmLogPolicyRefreshCacheResponse>(options: AdmLogPolicyRefreshCacheOptions = {} as AdmLogPolicyRefreshCacheOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmLogPolicyRefreshCache(contractParams as Parameters<typeof orvalAdmLogPolicyRefreshCache>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmLogPolicyUpdatePolicyBody = { activeYn?: string; dbLogEnabledYn?: string; description?: string; errorStackCaptureMode?: string; fieldAllowlist?: string; fileLogEnabledYn?: string; headerAllowlist?: string; logLevel?: string; maskingPolicyKey?: string; maxHeaderBytes?: number; maxQueryBytes?: number; maxRequestBodyBytes?: number; maxResponseBodyBytes?: number; maxStackBytes?: number; policyKey?: string; policyName?: string; priority?: number; queryAllowlist?: string; queryCaptureMode?: string; reason?: string; requestBodyCaptureMode?: string; requestHeaderCaptureMode?: string; responseBodyCaptureMode?: string; responseHeaderCaptureMode?: string; retentionDays?: number; samplingRate?: number; targetId?: string; targetType?: string };
export type AdmLogPolicyUpdatePolicyPath = { policyId: number };
export type AdmLogPolicyUpdatePolicyQuery = Record<string, never>;
export type AdmLogPolicyUpdatePolicyHeaders = Record<string, never>;
export type AdmLogPolicyUpdatePolicyResponse = Record<string, unknown>;
export type AdmLogPolicyUpdatePolicyOptions = CpfGeneratedBaseOptions & { data: AdmLogPolicyUpdatePolicyBody; path: AdmLogPolicyUpdatePolicyPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admLogPolicyUpdatePolicy<T = AdmLogPolicyUpdatePolicyResponse>(options: AdmLogPolicyUpdatePolicyOptions): Promise<T> {
  const response = await orvalAdmLogPolicyUpdatePolicy(options.path["policyId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMaintenanceExecuteActionBody = Record<string, unknown>;
export type AdmMaintenanceExecuteActionPath = Record<string, never>;
export type AdmMaintenanceExecuteActionQuery = Record<string, never>;
export type AdmMaintenanceExecuteActionHeaders = Record<string, never>;
export type AdmMaintenanceExecuteActionResponse = Record<string, unknown>;
export type AdmMaintenanceExecuteActionOptions = CpfGeneratedBaseOptions & { data: AdmMaintenanceExecuteActionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admMaintenanceExecuteAction<T = AdmMaintenanceExecuteActionResponse>(options: AdmMaintenanceExecuteActionOptions): Promise<T> {
  const response = await orvalAdmMaintenanceExecuteAction(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMaintenanceFindActionsBody = never;
export type AdmMaintenanceFindActionsPath = Record<string, never>;
export type AdmMaintenanceFindActionsQuery = { limit?: number };
export type AdmMaintenanceFindActionsHeaders = Record<string, never>;
export type AdmMaintenanceFindActionsResponse = Record<string, unknown>;
export type AdmMaintenanceFindActionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmMaintenanceFindActionsQuery; headers?: CpfGeneratedHeaders; };
export async function admMaintenanceFindActions<T = AdmMaintenanceFindActionsResponse>(options: AdmMaintenanceFindActionsOptions = {} as AdmMaintenanceFindActionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmMaintenanceFindActions(contractParams as Parameters<typeof orvalAdmMaintenanceFindActions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMessageCreateMessageBody = Record<string, unknown>;
export type AdmMessageCreateMessagePath = Record<string, never>;
export type AdmMessageCreateMessageQuery = Record<string, never>;
export type AdmMessageCreateMessageHeaders = Record<string, never>;
export type AdmMessageCreateMessageResponse = Record<string, unknown>;
export type AdmMessageCreateMessageOptions = CpfGeneratedBaseOptions & { data: AdmMessageCreateMessageBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admMessageCreateMessage<T = AdmMessageCreateMessageResponse>(options: AdmMessageCreateMessageOptions): Promise<T> {
  const response = await orvalAdmMessageCreateMessage(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMessageDeleteMessageBody = never;
export type AdmMessageDeleteMessagePath = { messageId: number };
export type AdmMessageDeleteMessageQuery = { reason: string };
export type AdmMessageDeleteMessageHeaders = Record<string, never>;
export type AdmMessageDeleteMessageResponse = Record<string, unknown>;
export type AdmMessageDeleteMessageOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmMessageDeleteMessagePath; query?: AdmMessageDeleteMessageQuery; headers?: CpfGeneratedHeaders; };
export async function admMessageDeleteMessage<T = AdmMessageDeleteMessageResponse>(options: AdmMessageDeleteMessageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmMessageDeleteMessage(options.path["messageId"], contractParams as Parameters<typeof orvalAdmMessageDeleteMessage>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMessageFindMessageBody = never;
export type AdmMessageFindMessagePath = { messageId: number };
export type AdmMessageFindMessageQuery = Record<string, never>;
export type AdmMessageFindMessageHeaders = Record<string, never>;
export type AdmMessageFindMessageResponse = Record<string, unknown>;
export type AdmMessageFindMessageOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmMessageFindMessagePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admMessageFindMessage<T = AdmMessageFindMessageResponse>(options: AdmMessageFindMessageOptions): Promise<T> {
  const response = await orvalAdmMessageFindMessage(options.path["messageId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMessageFindMessagesBody = never;
export type AdmMessageFindMessagesPath = Record<string, never>;
export type AdmMessageFindMessagesQuery = Record<string, never>;
export type AdmMessageFindMessagesHeaders = Record<string, never>;
export type AdmMessageFindMessagesResponse = Record<string, unknown>;
export type AdmMessageFindMessagesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admMessageFindMessages<T = AdmMessageFindMessagesResponse>(options: AdmMessageFindMessagesOptions = {} as AdmMessageFindMessagesOptions): Promise<T> {
  const response = await orvalAdmMessageFindMessages({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmMessageUpdateMessageBody = Record<string, unknown>;
export type AdmMessageUpdateMessagePath = { messageId: number };
export type AdmMessageUpdateMessageQuery = Record<string, never>;
export type AdmMessageUpdateMessageHeaders = Record<string, never>;
export type AdmMessageUpdateMessageResponse = Record<string, unknown>;
export type AdmMessageUpdateMessageOptions = CpfGeneratedBaseOptions & { data: AdmMessageUpdateMessageBody; path: AdmMessageUpdateMessagePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admMessageUpdateMessage<T = AdmMessageUpdateMessageResponse>(options: AdmMessageUpdateMessageOptions): Promise<T> {
  const response = await orvalAdmMessageUpdateMessage(options.path["messageId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationCancelDeliveryBody = never;
export type AdmNotificationCancelDeliveryPath = { deliveryId: number };
export type AdmNotificationCancelDeliveryQuery = { expectedVersion: number; reason: string };
export type AdmNotificationCancelDeliveryHeaders = Record<string, never>;
export type AdmNotificationCancelDeliveryResponse = Record<string, unknown>;
export type AdmNotificationCancelDeliveryOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmNotificationCancelDeliveryPath; query?: AdmNotificationCancelDeliveryQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationCancelDelivery<T = AdmNotificationCancelDeliveryResponse>(options: AdmNotificationCancelDeliveryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationCancelDelivery(options.path["deliveryId"], contractParams as Parameters<typeof orvalAdmNotificationCancelDelivery>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationDisableRuleBody = never;
export type AdmNotificationDisableRulePath = { ruleId: number };
export type AdmNotificationDisableRuleQuery = { reason: string };
export type AdmNotificationDisableRuleHeaders = Record<string, never>;
export type AdmNotificationDisableRuleResponse = Record<string, unknown>;
export type AdmNotificationDisableRuleOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmNotificationDisableRulePath; query?: AdmNotificationDisableRuleQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationDisableRule<T = AdmNotificationDisableRuleResponse>(options: AdmNotificationDisableRuleOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationDisableRule(options.path["ruleId"], contractParams as Parameters<typeof orvalAdmNotificationDisableRule>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationFindDeliveryAttemptsBody = never;
export type AdmNotificationFindDeliveryAttemptsPath = { deliveryId: number };
export type AdmNotificationFindDeliveryAttemptsQuery = { limit?: number };
export type AdmNotificationFindDeliveryAttemptsHeaders = Record<string, never>;
export type AdmNotificationFindDeliveryAttemptsResponse = Record<string, unknown>;
export type AdmNotificationFindDeliveryAttemptsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmNotificationFindDeliveryAttemptsPath; query?: AdmNotificationFindDeliveryAttemptsQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationFindDeliveryAttempts<T = AdmNotificationFindDeliveryAttemptsResponse>(options: AdmNotificationFindDeliveryAttemptsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationFindDeliveryAttempts(options.path["deliveryId"], contractParams as Parameters<typeof orvalAdmNotificationFindDeliveryAttempts>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationFindDeliveryLogsBody = never;
export type AdmNotificationFindDeliveryLogsPath = Record<string, never>;
export type AdmNotificationFindDeliveryLogsQuery = { limit?: number };
export type AdmNotificationFindDeliveryLogsHeaders = Record<string, never>;
export type AdmNotificationFindDeliveryLogsResponse = Record<string, unknown>;
export type AdmNotificationFindDeliveryLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmNotificationFindDeliveryLogsQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationFindDeliveryLogs<T = AdmNotificationFindDeliveryLogsResponse>(options: AdmNotificationFindDeliveryLogsOptions = {} as AdmNotificationFindDeliveryLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationFindDeliveryLogs(contractParams as Parameters<typeof orvalAdmNotificationFindDeliveryLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationFindDlqBody = never;
export type AdmNotificationFindDlqPath = Record<string, never>;
export type AdmNotificationFindDlqQuery = { limit?: number };
export type AdmNotificationFindDlqHeaders = Record<string, never>;
export type AdmNotificationFindDlqResponse = Record<string, unknown>;
export type AdmNotificationFindDlqOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmNotificationFindDlqQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationFindDlq<T = AdmNotificationFindDlqResponse>(options: AdmNotificationFindDlqOptions = {} as AdmNotificationFindDlqOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationFindDlq(contractParams as Parameters<typeof orvalAdmNotificationFindDlq>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationFindRuleBody = never;
export type AdmNotificationFindRulePath = { ruleId: number };
export type AdmNotificationFindRuleQuery = Record<string, never>;
export type AdmNotificationFindRuleHeaders = Record<string, never>;
export type AdmNotificationFindRuleResponse = Record<string, unknown>;
export type AdmNotificationFindRuleOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmNotificationFindRulePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admNotificationFindRule<T = AdmNotificationFindRuleResponse>(options: AdmNotificationFindRuleOptions): Promise<T> {
  const response = await orvalAdmNotificationFindRule(options.path["ruleId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationFindRulesBody = never;
export type AdmNotificationFindRulesPath = Record<string, never>;
export type AdmNotificationFindRulesQuery = { limit?: number };
export type AdmNotificationFindRulesHeaders = Record<string, never>;
export type AdmNotificationFindRulesResponse = Record<string, unknown>;
export type AdmNotificationFindRulesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmNotificationFindRulesQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationFindRules<T = AdmNotificationFindRulesResponse>(options: AdmNotificationFindRulesOptions = {} as AdmNotificationFindRulesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationFindRules(contractParams as Parameters<typeof orvalAdmNotificationFindRules>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationRetryDeliveryBody = never;
export type AdmNotificationRetryDeliveryPath = { deliveryId: number };
export type AdmNotificationRetryDeliveryQuery = { expectedVersion: number; reason: string };
export type AdmNotificationRetryDeliveryHeaders = Record<string, never>;
export type AdmNotificationRetryDeliveryResponse = Record<string, unknown>;
export type AdmNotificationRetryDeliveryOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmNotificationRetryDeliveryPath; query?: AdmNotificationRetryDeliveryQuery; headers?: CpfGeneratedHeaders; };
export async function admNotificationRetryDelivery<T = AdmNotificationRetryDeliveryResponse>(options: AdmNotificationRetryDeliveryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmNotificationRetryDelivery(options.path["deliveryId"], contractParams as Parameters<typeof orvalAdmNotificationRetryDelivery>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationSaveRuleBody = { channelCode?: string; eventSubType?: string; eventType?: string; reason?: string; receiverGroup?: string; severity?: string; templateCode?: string; useYn?: string };
export type AdmNotificationSaveRulePath = Record<string, never>;
export type AdmNotificationSaveRuleQuery = Record<string, never>;
export type AdmNotificationSaveRuleHeaders = Record<string, never>;
export type AdmNotificationSaveRuleResponse = Record<string, unknown>;
export type AdmNotificationSaveRuleOptions = CpfGeneratedBaseOptions & { data: AdmNotificationSaveRuleBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admNotificationSaveRule<T = AdmNotificationSaveRuleResponse>(options: AdmNotificationSaveRuleOptions): Promise<T> {
  const response = await orvalAdmNotificationSaveRule(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationSendTestBody = { message?: string; reason?: string; receiver?: string; targetId?: string; targetType?: string };
export type AdmNotificationSendTestPath = { ruleId: number };
export type AdmNotificationSendTestQuery = Record<string, never>;
export type AdmNotificationSendTestHeaders = Record<string, never>;
export type AdmNotificationSendTestResponse = Record<string, unknown>;
export type AdmNotificationSendTestOptions = CpfGeneratedBaseOptions & { data: AdmNotificationSendTestBody; path: AdmNotificationSendTestPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admNotificationSendTest<T = AdmNotificationSendTestResponse>(options: AdmNotificationSendTestOptions): Promise<T> {
  const response = await orvalAdmNotificationSendTest(options.path["ruleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmNotificationUpdateRuleBody = { channelCode?: string; eventSubType?: string; eventType?: string; reason?: string; receiverGroup?: string; severity?: string; templateCode?: string; useYn?: string };
export type AdmNotificationUpdateRulePath = { ruleId: number };
export type AdmNotificationUpdateRuleQuery = Record<string, never>;
export type AdmNotificationUpdateRuleHeaders = Record<string, never>;
export type AdmNotificationUpdateRuleResponse = Record<string, unknown>;
export type AdmNotificationUpdateRuleOptions = CpfGeneratedBaseOptions & { data: AdmNotificationUpdateRuleBody; path: AdmNotificationUpdateRulePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admNotificationUpdateRule<T = AdmNotificationUpdateRuleResponse>(options: AdmNotificationUpdateRuleOptions): Promise<T> {
  const response = await orvalAdmNotificationUpdateRule(options.path["ruleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOpenApiRefreshBody = { reason?: string };
export type AdmOpenApiRefreshPath = Record<string, never>;
export type AdmOpenApiRefreshQuery = Record<string, never>;
export type AdmOpenApiRefreshHeaders = { "X-CPF-Risk-Confirmed": string };
export type AdmOpenApiRefreshResponse = Record<string, unknown>;
export type AdmOpenApiRefreshOptions = CpfGeneratedBaseOptions & { data: AdmOpenApiRefreshBody; path?: never; query?: never; headers: CpfGeneratedHeaders & AdmOpenApiRefreshHeaders; };
export async function admOpenApiRefresh<T = AdmOpenApiRefreshResponse>(options: AdmOpenApiRefreshOptions): Promise<T> {
  const contractParams = { "X-CPF-Risk-Confirmed": headerValue(options.headers, "X-CPF-Risk-Confirmed") };
  const response = await orvalAdmOpenApiRefresh(options.data, contractParams as Parameters<typeof orvalAdmOpenApiRefresh>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOpenApiStatusBody = never;
export type AdmOpenApiStatusPath = Record<string, never>;
export type AdmOpenApiStatusQuery = Record<string, never>;
export type AdmOpenApiStatusHeaders = Record<string, never>;
export type AdmOpenApiStatusResponse = Record<string, unknown>;
export type AdmOpenApiStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOpenApiStatus<T = AdmOpenApiStatusResponse>(options: AdmOpenApiStatusOptions = {} as AdmOpenApiStatusOptions): Promise<T> {
  const response = await orvalAdmOpenApiStatus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperationsGovernanceSnapshotBody = never;
export type AdmOperationsGovernanceSnapshotPath = Record<string, never>;
export type AdmOperationsGovernanceSnapshotQuery = Record<string, never>;
export type AdmOperationsGovernanceSnapshotHeaders = Record<string, never>;
export type AdmOperationsGovernanceSnapshotResponse = Record<string, unknown>;
export type AdmOperationsGovernanceSnapshotOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperationsGovernanceSnapshot<T = AdmOperationsGovernanceSnapshotResponse>(options: AdmOperationsGovernanceSnapshotOptions = {} as AdmOperationsGovernanceSnapshotOptions): Promise<T> {
  const response = await orvalAdmOperationsGovernanceSnapshot({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorChangePasswordBody = { currentPassword?: string; newPassword?: string; newPasswordConfirm?: string; reason?: string };
export type AdmOperatorChangePasswordPath = { operatorId: string };
export type AdmOperatorChangePasswordQuery = Record<string, never>;
export type AdmOperatorChangePasswordHeaders = Record<string, never>;
export type AdmOperatorChangePasswordResponse = Record<string, unknown>;
export type AdmOperatorChangePasswordOptions = CpfGeneratedBaseOptions & { data: AdmOperatorChangePasswordBody; path: AdmOperatorChangePasswordPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorChangePassword<T = AdmOperatorChangePasswordResponse>(options: AdmOperatorChangePasswordOptions): Promise<T> {
  const response = await orvalAdmOperatorChangePassword(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorCleanupExpiredSessionsBody = { reason?: string };
export type AdmOperatorCleanupExpiredSessionsPath = Record<string, never>;
export type AdmOperatorCleanupExpiredSessionsQuery = Record<string, never>;
export type AdmOperatorCleanupExpiredSessionsHeaders = Record<string, never>;
export type AdmOperatorCleanupExpiredSessionsResponse = Record<string, unknown>;
export type AdmOperatorCleanupExpiredSessionsOptions = CpfGeneratedBaseOptions & { data: AdmOperatorCleanupExpiredSessionsBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorCleanupExpiredSessions<T = AdmOperatorCleanupExpiredSessionsResponse>(options: AdmOperatorCleanupExpiredSessionsOptions): Promise<T> {
  const response = await orvalAdmOperatorCleanupExpiredSessions(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorCreateOperatorBody = { mobileNo?: string; officePhoneNo?: string; operationId?: string; operatorId?: string; operatorName?: string; password?: string; reason?: string; roleIds?: Array<string> };
export type AdmOperatorCreateOperatorPath = Record<string, never>;
export type AdmOperatorCreateOperatorQuery = Record<string, never>;
export type AdmOperatorCreateOperatorHeaders = Record<string, never>;
export type AdmOperatorCreateOperatorResponse = Record<string, unknown>;
export type AdmOperatorCreateOperatorOptions = CpfGeneratedBaseOptions & { data: AdmOperatorCreateOperatorBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorCreateOperator<T = AdmOperatorCreateOperatorResponse>(options: AdmOperatorCreateOperatorOptions): Promise<T> {
  const response = await orvalAdmOperatorCreateOperator(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorFindCreateResultBody = never;
export type AdmOperatorFindCreateResultPath = { operationId: string };
export type AdmOperatorFindCreateResultQuery = Record<string, never>;
export type AdmOperatorFindCreateResultHeaders = Record<string, never>;
export type AdmOperatorFindCreateResultResponse = Record<string, unknown>;
export type AdmOperatorFindCreateResultOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmOperatorFindCreateResultPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorFindCreateResult<T = AdmOperatorFindCreateResultResponse>(options: AdmOperatorFindCreateResultOptions): Promise<T> {
  const response = await orvalAdmOperatorFindCreateResult(options.path["operationId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorFindMenusBody = never;
export type AdmOperatorFindMenusPath = Record<string, never>;
export type AdmOperatorFindMenusQuery = Record<string, never>;
export type AdmOperatorFindMenusHeaders = Record<string, never>;
export type AdmOperatorFindMenusResponse = Record<string, unknown>;
export type AdmOperatorFindMenusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorFindMenus<T = AdmOperatorFindMenusResponse>(options: AdmOperatorFindMenusOptions = {} as AdmOperatorFindMenusOptions): Promise<T> {
  const response = await orvalAdmOperatorFindMenus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorFindOperatorsBody = never;
export type AdmOperatorFindOperatorsPath = Record<string, never>;
export type AdmOperatorFindOperatorsQuery = Record<string, never>;
export type AdmOperatorFindOperatorsHeaders = Record<string, never>;
export type AdmOperatorFindOperatorsResponse = Record<string, unknown>;
export type AdmOperatorFindOperatorsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorFindOperators<T = AdmOperatorFindOperatorsResponse>(options: AdmOperatorFindOperatorsOptions = {} as AdmOperatorFindOperatorsOptions): Promise<T> {
  const response = await orvalAdmOperatorFindOperators({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorFindRolesBody = never;
export type AdmOperatorFindRolesPath = Record<string, never>;
export type AdmOperatorFindRolesQuery = Record<string, never>;
export type AdmOperatorFindRolesHeaders = Record<string, never>;
export type AdmOperatorFindRolesResponse = Record<string, unknown>;
export type AdmOperatorFindRolesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorFindRoles<T = AdmOperatorFindRolesResponse>(options: AdmOperatorFindRolesOptions = {} as AdmOperatorFindRolesOptions): Promise<T> {
  const response = await orvalAdmOperatorFindRoles({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorFindSessionsBody = never;
export type AdmOperatorFindSessionsPath = Record<string, never>;
export type AdmOperatorFindSessionsQuery = { operatorId?: string };
export type AdmOperatorFindSessionsHeaders = Record<string, never>;
export type AdmOperatorFindSessionsResponse = Record<string, unknown>;
export type AdmOperatorFindSessionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmOperatorFindSessionsQuery; headers?: CpfGeneratedHeaders; };
export async function admOperatorFindSessions<T = AdmOperatorFindSessionsResponse>(options: AdmOperatorFindSessionsOptions = {} as AdmOperatorFindSessionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmOperatorFindSessions(contractParams as Parameters<typeof orvalAdmOperatorFindSessions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorPasswordPolicyBody = never;
export type AdmOperatorPasswordPolicyPath = Record<string, never>;
export type AdmOperatorPasswordPolicyQuery = Record<string, never>;
export type AdmOperatorPasswordPolicyHeaders = Record<string, never>;
export type AdmOperatorPasswordPolicyResponse = Record<string, unknown>;
export type AdmOperatorPasswordPolicyOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorPasswordPolicy<T = AdmOperatorPasswordPolicyResponse>(options: AdmOperatorPasswordPolicyOptions = {} as AdmOperatorPasswordPolicyOptions): Promise<T> {
  const response = await orvalAdmOperatorPasswordPolicy({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorRawContactBody = Record<string, unknown>;
export type AdmOperatorRawContactPath = { operatorId: string };
export type AdmOperatorRawContactQuery = Record<string, never>;
export type AdmOperatorRawContactHeaders = Record<string, never>;
export type AdmOperatorRawContactResponse = Record<string, unknown>;
export type AdmOperatorRawContactOptions = CpfGeneratedBaseOptions & { data: AdmOperatorRawContactBody; path: AdmOperatorRawContactPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorRawContact<T = AdmOperatorRawContactResponse>(options: AdmOperatorRawContactOptions): Promise<T> {
  const response = await orvalAdmOperatorRawContact(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorResetPasswordBody = { forceChange: boolean; newPassword?: string; reason?: string };
export type AdmOperatorResetPasswordPath = { operatorId: string };
export type AdmOperatorResetPasswordQuery = Record<string, never>;
export type AdmOperatorResetPasswordHeaders = Record<string, never>;
export type AdmOperatorResetPasswordResponse = Record<string, unknown>;
export type AdmOperatorResetPasswordOptions = CpfGeneratedBaseOptions & { data: AdmOperatorResetPasswordBody; path: AdmOperatorResetPasswordPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorResetPassword<T = AdmOperatorResetPasswordResponse>(options: AdmOperatorResetPasswordOptions): Promise<T> {
  const response = await orvalAdmOperatorResetPassword(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorRevokeSessionBody = { reason?: string };
export type AdmOperatorRevokeSessionPath = { sessionId: string };
export type AdmOperatorRevokeSessionQuery = Record<string, never>;
export type AdmOperatorRevokeSessionHeaders = Record<string, never>;
export type AdmOperatorRevokeSessionResponse = Record<string, unknown>;
export type AdmOperatorRevokeSessionOptions = CpfGeneratedBaseOptions & { data: AdmOperatorRevokeSessionBody; path: AdmOperatorRevokeSessionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorRevokeSession<T = AdmOperatorRevokeSessionResponse>(options: AdmOperatorRevokeSessionOptions): Promise<T> {
  const response = await orvalAdmOperatorRevokeSession(options.path["sessionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorUnlockOperatorBody = { reason?: string };
export type AdmOperatorUnlockOperatorPath = { operatorId: string };
export type AdmOperatorUnlockOperatorQuery = Record<string, never>;
export type AdmOperatorUnlockOperatorHeaders = Record<string, never>;
export type AdmOperatorUnlockOperatorResponse = Record<string, unknown>;
export type AdmOperatorUnlockOperatorOptions = CpfGeneratedBaseOptions & { data: AdmOperatorUnlockOperatorBody; path: AdmOperatorUnlockOperatorPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorUnlockOperator<T = AdmOperatorUnlockOperatorResponse>(options: AdmOperatorUnlockOperatorOptions): Promise<T> {
  const response = await orvalAdmOperatorUnlockOperator(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorUpdateContactBody = { clearMobileNo: boolean; clearOfficePhoneNo: boolean; expectedVersion?: number; mobileNo?: string; officePhoneNo?: string; reason?: string };
export type AdmOperatorUpdateContactPath = { operatorId: string };
export type AdmOperatorUpdateContactQuery = Record<string, never>;
export type AdmOperatorUpdateContactHeaders = Record<string, never>;
export type AdmOperatorUpdateContactResponse = Record<string, unknown>;
export type AdmOperatorUpdateContactOptions = CpfGeneratedBaseOptions & { data: AdmOperatorUpdateContactBody; path: AdmOperatorUpdateContactPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorUpdateContact<T = AdmOperatorUpdateContactResponse>(options: AdmOperatorUpdateContactOptions): Promise<T> {
  const response = await orvalAdmOperatorUpdateContact(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorUpdateRolesBody = { reason?: string; roleIds?: Array<string> };
export type AdmOperatorUpdateRolesPath = { operatorId: string };
export type AdmOperatorUpdateRolesQuery = Record<string, never>;
export type AdmOperatorUpdateRolesHeaders = Record<string, never>;
export type AdmOperatorUpdateRolesResponse = Record<string, unknown>;
export type AdmOperatorUpdateRolesOptions = CpfGeneratedBaseOptions & { data: AdmOperatorUpdateRolesBody; path: AdmOperatorUpdateRolesPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorUpdateRoles<T = AdmOperatorUpdateRolesResponse>(options: AdmOperatorUpdateRolesOptions): Promise<T> {
  const response = await orvalAdmOperatorUpdateRoles(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorUpdateStatusBody = { accountStatus?: string; expectedVersion?: number; reason?: string };
export type AdmOperatorUpdateStatusPath = { operatorId: string };
export type AdmOperatorUpdateStatusQuery = Record<string, never>;
export type AdmOperatorUpdateStatusHeaders = Record<string, never>;
export type AdmOperatorUpdateStatusResponse = Record<string, unknown>;
export type AdmOperatorUpdateStatusOptions = CpfGeneratedBaseOptions & { data: AdmOperatorUpdateStatusBody; path: AdmOperatorUpdateStatusPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admOperatorUpdateStatus<T = AdmOperatorUpdateStatusResponse>(options: AdmOperatorUpdateStatusOptions): Promise<T> {
  const response = await orvalAdmOperatorUpdateStatus(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmOperatorValidatePasswordBody = never;
export type AdmOperatorValidatePasswordPath = Record<string, never>;
export type AdmOperatorValidatePasswordQuery = { operatorId: string; password: string };
export type AdmOperatorValidatePasswordHeaders = Record<string, never>;
export type AdmOperatorValidatePasswordResponse = Record<string, unknown>;
export type AdmOperatorValidatePasswordOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmOperatorValidatePasswordQuery; headers?: CpfGeneratedHeaders; };
export async function admOperatorValidatePassword<T = AdmOperatorValidatePasswordResponse>(options: AdmOperatorValidatePasswordOptions = {} as AdmOperatorValidatePasswordOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmOperatorValidatePassword(contractParams as Parameters<typeof orvalAdmOperatorValidatePassword>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmParameterReferenceSearchBody = never;
export type AdmParameterReferenceSearchPath = Record<string, never>;
export type AdmParameterReferenceSearchQuery = { referenceType: string; parentType?: string; parentId?: string; q?: string; offset?: number; limit?: number };
export type AdmParameterReferenceSearchHeaders = Record<string, never>;
export type AdmParameterReferenceSearchResponse = Record<string, unknown>;
export type AdmParameterReferenceSearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmParameterReferenceSearchQuery; headers?: CpfGeneratedHeaders; };
export async function admParameterReferenceSearch<T = AdmParameterReferenceSearchResponse>(options: AdmParameterReferenceSearchOptions = {} as AdmParameterReferenceSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmParameterReferenceSearch(contractParams as Parameters<typeof orvalAdmParameterReferenceSearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionCreateApiPermissionBody = { apiGroupCode?: string; apiName?: string; apiPath?: string; apiPermissionId?: string; buttonId?: string; httpMethod?: string; menuId?: string; permissionCode?: string; reason?: string; useYn?: string };
export type AdmPermissionCreateApiPermissionPath = Record<string, never>;
export type AdmPermissionCreateApiPermissionQuery = Record<string, never>;
export type AdmPermissionCreateApiPermissionHeaders = Record<string, never>;
export type AdmPermissionCreateApiPermissionResponse = Record<string, unknown>;
export type AdmPermissionCreateApiPermissionOptions = CpfGeneratedBaseOptions & { data: AdmPermissionCreateApiPermissionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionCreateApiPermission<T = AdmPermissionCreateApiPermissionResponse>(options: AdmPermissionCreateApiPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionCreateApiPermission(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionCreateButtonBody = { actionCode?: string; apiPattern?: string; buttonId?: string; buttonName?: string; httpMethod?: string; menuId?: string; reason?: string; sortOrder?: number; useYn?: string };
export type AdmPermissionCreateButtonPath = Record<string, never>;
export type AdmPermissionCreateButtonQuery = Record<string, never>;
export type AdmPermissionCreateButtonHeaders = Record<string, never>;
export type AdmPermissionCreateButtonResponse = Record<string, unknown>;
export type AdmPermissionCreateButtonOptions = CpfGeneratedBaseOptions & { data: AdmPermissionCreateButtonBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionCreateButton<T = AdmPermissionCreateButtonResponse>(options: AdmPermissionCreateButtonOptions): Promise<T> {
  const response = await orvalAdmPermissionCreateButton(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionCreateMenuBody = { menuId?: string; menuName?: string; menuPath?: string; parentMenuId?: string; reason?: string; sortOrder?: number; useYn?: string };
export type AdmPermissionCreateMenuPath = Record<string, never>;
export type AdmPermissionCreateMenuQuery = Record<string, never>;
export type AdmPermissionCreateMenuHeaders = Record<string, never>;
export type AdmPermissionCreateMenuResponse = Record<string, unknown>;
export type AdmPermissionCreateMenuOptions = CpfGeneratedBaseOptions & { data: AdmPermissionCreateMenuBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionCreateMenu<T = AdmPermissionCreateMenuResponse>(options: AdmPermissionCreateMenuOptions): Promise<T> {
  const response = await orvalAdmPermissionCreateMenu(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionCreateRoleBody = { description?: string; reason?: string; roleId?: string; roleName?: string; roleType?: string; useYn?: string };
export type AdmPermissionCreateRolePath = Record<string, never>;
export type AdmPermissionCreateRoleQuery = Record<string, never>;
export type AdmPermissionCreateRoleHeaders = Record<string, never>;
export type AdmPermissionCreateRoleResponse = Record<string, unknown>;
export type AdmPermissionCreateRoleOptions = CpfGeneratedBaseOptions & { data: AdmPermissionCreateRoleBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionCreateRole<T = AdmPermissionCreateRoleResponse>(options: AdmPermissionCreateRoleOptions): Promise<T> {
  const response = await orvalAdmPermissionCreateRole(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindApiPermissionBody = never;
export type AdmPermissionFindApiPermissionPath = { apiPermissionId: string };
export type AdmPermissionFindApiPermissionQuery = Record<string, never>;
export type AdmPermissionFindApiPermissionHeaders = Record<string, never>;
export type AdmPermissionFindApiPermissionResponse = Record<string, unknown>;
export type AdmPermissionFindApiPermissionOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmPermissionFindApiPermissionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindApiPermission<T = AdmPermissionFindApiPermissionResponse>(options: AdmPermissionFindApiPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionFindApiPermission(options.path["apiPermissionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindApiPermissionMatrixBody = never;
export type AdmPermissionFindApiPermissionMatrixPath = Record<string, never>;
export type AdmPermissionFindApiPermissionMatrixQuery = Record<string, never>;
export type AdmPermissionFindApiPermissionMatrixHeaders = Record<string, never>;
export type AdmPermissionFindApiPermissionMatrixResponse = Record<string, unknown>;
export type AdmPermissionFindApiPermissionMatrixOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindApiPermissionMatrix<T = AdmPermissionFindApiPermissionMatrixResponse>(options: AdmPermissionFindApiPermissionMatrixOptions = {} as AdmPermissionFindApiPermissionMatrixOptions): Promise<T> {
  const response = await orvalAdmPermissionFindApiPermissionMatrix({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindApiPermissionsBody = never;
export type AdmPermissionFindApiPermissionsPath = Record<string, never>;
export type AdmPermissionFindApiPermissionsQuery = Record<string, never>;
export type AdmPermissionFindApiPermissionsHeaders = Record<string, never>;
export type AdmPermissionFindApiPermissionsResponse = Record<string, unknown>;
export type AdmPermissionFindApiPermissionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindApiPermissions<T = AdmPermissionFindApiPermissionsResponse>(options: AdmPermissionFindApiPermissionsOptions = {} as AdmPermissionFindApiPermissionsOptions): Promise<T> {
  const response = await orvalAdmPermissionFindApiPermissions({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindButtonBody = never;
export type AdmPermissionFindButtonPath = { buttonId: string };
export type AdmPermissionFindButtonQuery = Record<string, never>;
export type AdmPermissionFindButtonHeaders = Record<string, never>;
export type AdmPermissionFindButtonResponse = Record<string, unknown>;
export type AdmPermissionFindButtonOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmPermissionFindButtonPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindButton<T = AdmPermissionFindButtonResponse>(options: AdmPermissionFindButtonOptions): Promise<T> {
  const response = await orvalAdmPermissionFindButton(options.path["buttonId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindButtonMatrixBody = never;
export type AdmPermissionFindButtonMatrixPath = Record<string, never>;
export type AdmPermissionFindButtonMatrixQuery = Record<string, never>;
export type AdmPermissionFindButtonMatrixHeaders = Record<string, never>;
export type AdmPermissionFindButtonMatrixResponse = Record<string, unknown>;
export type AdmPermissionFindButtonMatrixOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindButtonMatrix<T = AdmPermissionFindButtonMatrixResponse>(options: AdmPermissionFindButtonMatrixOptions = {} as AdmPermissionFindButtonMatrixOptions): Promise<T> {
  const response = await orvalAdmPermissionFindButtonMatrix({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindButtonsBody = never;
export type AdmPermissionFindButtonsPath = Record<string, never>;
export type AdmPermissionFindButtonsQuery = { menuId?: string };
export type AdmPermissionFindButtonsHeaders = Record<string, never>;
export type AdmPermissionFindButtonsResponse = Record<string, unknown>;
export type AdmPermissionFindButtonsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmPermissionFindButtonsQuery; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindButtons<T = AdmPermissionFindButtonsResponse>(options: AdmPermissionFindButtonsOptions = {} as AdmPermissionFindButtonsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmPermissionFindButtons(contractParams as Parameters<typeof orvalAdmPermissionFindButtons>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindManagedMenuBody = never;
export type AdmPermissionFindManagedMenuPath = { menuId: string };
export type AdmPermissionFindManagedMenuQuery = Record<string, never>;
export type AdmPermissionFindManagedMenuHeaders = Record<string, never>;
export type AdmPermissionFindManagedMenuResponse = Record<string, unknown>;
export type AdmPermissionFindManagedMenuOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmPermissionFindManagedMenuPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindManagedMenu<T = AdmPermissionFindManagedMenuResponse>(options: AdmPermissionFindManagedMenuOptions): Promise<T> {
  const response = await orvalAdmPermissionFindManagedMenu(options.path["menuId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindManagedMenusBody = never;
export type AdmPermissionFindManagedMenusPath = Record<string, never>;
export type AdmPermissionFindManagedMenusQuery = Record<string, never>;
export type AdmPermissionFindManagedMenusHeaders = Record<string, never>;
export type AdmPermissionFindManagedMenusResponse = Record<string, unknown>;
export type AdmPermissionFindManagedMenusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindManagedMenus<T = AdmPermissionFindManagedMenusResponse>(options: AdmPermissionFindManagedMenusOptions = {} as AdmPermissionFindManagedMenusOptions): Promise<T> {
  const response = await orvalAdmPermissionFindManagedMenus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindMenuMatrixBody = never;
export type AdmPermissionFindMenuMatrixPath = Record<string, never>;
export type AdmPermissionFindMenuMatrixQuery = Record<string, never>;
export type AdmPermissionFindMenuMatrixHeaders = Record<string, never>;
export type AdmPermissionFindMenuMatrixResponse = Record<string, unknown>;
export type AdmPermissionFindMenuMatrixOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindMenuMatrix<T = AdmPermissionFindMenuMatrixResponse>(options: AdmPermissionFindMenuMatrixOptions = {} as AdmPermissionFindMenuMatrixOptions): Promise<T> {
  const response = await orvalAdmPermissionFindMenuMatrix({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindRoleBody = never;
export type AdmPermissionFindRolePath = { roleId: string };
export type AdmPermissionFindRoleQuery = Record<string, never>;
export type AdmPermissionFindRoleHeaders = Record<string, never>;
export type AdmPermissionFindRoleResponse = Record<string, unknown>;
export type AdmPermissionFindRoleOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmPermissionFindRolePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindRole<T = AdmPermissionFindRoleResponse>(options: AdmPermissionFindRoleOptions): Promise<T> {
  const response = await orvalAdmPermissionFindRole(options.path["roleId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionFindRolesBody = never;
export type AdmPermissionFindRolesPath = Record<string, never>;
export type AdmPermissionFindRolesQuery = Record<string, never>;
export type AdmPermissionFindRolesHeaders = Record<string, never>;
export type AdmPermissionFindRolesResponse = Record<string, unknown>;
export type AdmPermissionFindRolesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionFindRoles<T = AdmPermissionFindRolesResponse>(options: AdmPermissionFindRolesOptions = {} as AdmPermissionFindRolesOptions): Promise<T> {
  const response = await orvalAdmPermissionFindRoles({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateApiPermissionBody = { apiGroupCode?: string; apiName?: string; apiPath?: string; apiPermissionId?: string; buttonId?: string; httpMethod?: string; menuId?: string; permissionCode?: string; reason?: string; useYn?: string };
export type AdmPermissionUpdateApiPermissionPath = { apiPermissionId: string };
export type AdmPermissionUpdateApiPermissionQuery = Record<string, never>;
export type AdmPermissionUpdateApiPermissionHeaders = Record<string, never>;
export type AdmPermissionUpdateApiPermissionResponse = Record<string, unknown>;
export type AdmPermissionUpdateApiPermissionOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateApiPermissionBody; path: AdmPermissionUpdateApiPermissionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateApiPermission<T = AdmPermissionUpdateApiPermissionResponse>(options: AdmPermissionUpdateApiPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateApiPermission(options.path["apiPermissionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateApiPermissionStatusBody = { reason?: string; useYn?: string };
export type AdmPermissionUpdateApiPermissionStatusPath = { apiPermissionId: string };
export type AdmPermissionUpdateApiPermissionStatusQuery = Record<string, never>;
export type AdmPermissionUpdateApiPermissionStatusHeaders = Record<string, never>;
export type AdmPermissionUpdateApiPermissionStatusResponse = Record<string, unknown>;
export type AdmPermissionUpdateApiPermissionStatusOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateApiPermissionStatusBody; path: AdmPermissionUpdateApiPermissionStatusPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateApiPermissionStatus<T = AdmPermissionUpdateApiPermissionStatusResponse>(options: AdmPermissionUpdateApiPermissionStatusOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateApiPermissionStatus(options.path["apiPermissionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateButtonBody = { actionCode?: string; apiPattern?: string; buttonId?: string; buttonName?: string; httpMethod?: string; menuId?: string; reason?: string; sortOrder?: number; useYn?: string };
export type AdmPermissionUpdateButtonPath = { buttonId: string };
export type AdmPermissionUpdateButtonQuery = Record<string, never>;
export type AdmPermissionUpdateButtonHeaders = Record<string, never>;
export type AdmPermissionUpdateButtonResponse = Record<string, unknown>;
export type AdmPermissionUpdateButtonOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateButtonBody; path: AdmPermissionUpdateButtonPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateButton<T = AdmPermissionUpdateButtonResponse>(options: AdmPermissionUpdateButtonOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateButton(options.path["buttonId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateButtonPermissionBody = { allowYn?: string; reason?: string };
export type AdmPermissionUpdateButtonPermissionPath = { roleId: string; buttonId: string };
export type AdmPermissionUpdateButtonPermissionQuery = Record<string, never>;
export type AdmPermissionUpdateButtonPermissionHeaders = Record<string, never>;
export type AdmPermissionUpdateButtonPermissionResponse = Record<string, unknown>;
export type AdmPermissionUpdateButtonPermissionOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateButtonPermissionBody; path: AdmPermissionUpdateButtonPermissionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateButtonPermission<T = AdmPermissionUpdateButtonPermissionResponse>(options: AdmPermissionUpdateButtonPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateButtonPermission(options.path["roleId"], options.path["buttonId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateButtonStatusBody = { reason?: string; useYn?: string };
export type AdmPermissionUpdateButtonStatusPath = { buttonId: string };
export type AdmPermissionUpdateButtonStatusQuery = Record<string, never>;
export type AdmPermissionUpdateButtonStatusHeaders = Record<string, never>;
export type AdmPermissionUpdateButtonStatusResponse = Record<string, unknown>;
export type AdmPermissionUpdateButtonStatusOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateButtonStatusBody; path: AdmPermissionUpdateButtonStatusPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateButtonStatus<T = AdmPermissionUpdateButtonStatusResponse>(options: AdmPermissionUpdateButtonStatusOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateButtonStatus(options.path["buttonId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateMenuBody = { menuId?: string; menuName?: string; menuPath?: string; parentMenuId?: string; reason?: string; sortOrder?: number; useYn?: string };
export type AdmPermissionUpdateMenuPath = { menuId: string };
export type AdmPermissionUpdateMenuQuery = Record<string, never>;
export type AdmPermissionUpdateMenuHeaders = Record<string, never>;
export type AdmPermissionUpdateMenuResponse = Record<string, unknown>;
export type AdmPermissionUpdateMenuOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateMenuBody; path: AdmPermissionUpdateMenuPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateMenu<T = AdmPermissionUpdateMenuResponse>(options: AdmPermissionUpdateMenuOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateMenu(options.path["menuId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateMenuPermissionBody = { deleteYn?: string; readYn?: string; reason?: string; writeYn?: string };
export type AdmPermissionUpdateMenuPermissionPath = { roleId: string; menuId: string };
export type AdmPermissionUpdateMenuPermissionQuery = Record<string, never>;
export type AdmPermissionUpdateMenuPermissionHeaders = Record<string, never>;
export type AdmPermissionUpdateMenuPermissionResponse = Record<string, unknown>;
export type AdmPermissionUpdateMenuPermissionOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateMenuPermissionBody; path: AdmPermissionUpdateMenuPermissionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateMenuPermission<T = AdmPermissionUpdateMenuPermissionResponse>(options: AdmPermissionUpdateMenuPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateMenuPermission(options.path["roleId"], options.path["menuId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateMenuStatusBody = { reason?: string; useYn?: string };
export type AdmPermissionUpdateMenuStatusPath = { menuId: string };
export type AdmPermissionUpdateMenuStatusQuery = Record<string, never>;
export type AdmPermissionUpdateMenuStatusHeaders = Record<string, never>;
export type AdmPermissionUpdateMenuStatusResponse = Record<string, unknown>;
export type AdmPermissionUpdateMenuStatusOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateMenuStatusBody; path: AdmPermissionUpdateMenuStatusPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateMenuStatus<T = AdmPermissionUpdateMenuStatusResponse>(options: AdmPermissionUpdateMenuStatusOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateMenuStatus(options.path["menuId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateRoleBody = { description?: string; reason?: string; roleId?: string; roleName?: string; roleType?: string; useYn?: string };
export type AdmPermissionUpdateRolePath = { roleId: string };
export type AdmPermissionUpdateRoleQuery = Record<string, never>;
export type AdmPermissionUpdateRoleHeaders = Record<string, never>;
export type AdmPermissionUpdateRoleResponse = Record<string, unknown>;
export type AdmPermissionUpdateRoleOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateRoleBody; path: AdmPermissionUpdateRolePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateRole<T = AdmPermissionUpdateRoleResponse>(options: AdmPermissionUpdateRoleOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateRole(options.path["roleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateRoleApiPermissionBody = { allowYn?: string; reason?: string };
export type AdmPermissionUpdateRoleApiPermissionPath = { roleId: string; apiPermissionId: string };
export type AdmPermissionUpdateRoleApiPermissionQuery = Record<string, never>;
export type AdmPermissionUpdateRoleApiPermissionHeaders = Record<string, never>;
export type AdmPermissionUpdateRoleApiPermissionResponse = Record<string, unknown>;
export type AdmPermissionUpdateRoleApiPermissionOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateRoleApiPermissionBody; path: AdmPermissionUpdateRoleApiPermissionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateRoleApiPermission<T = AdmPermissionUpdateRoleApiPermissionResponse>(options: AdmPermissionUpdateRoleApiPermissionOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateRoleApiPermission(options.path["roleId"], options.path["apiPermissionId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPermissionUpdateRoleStatusBody = { reason?: string; useYn?: string };
export type AdmPermissionUpdateRoleStatusPath = { roleId: string };
export type AdmPermissionUpdateRoleStatusQuery = Record<string, never>;
export type AdmPermissionUpdateRoleStatusHeaders = Record<string, never>;
export type AdmPermissionUpdateRoleStatusResponse = Record<string, unknown>;
export type AdmPermissionUpdateRoleStatusOptions = CpfGeneratedBaseOptions & { data: AdmPermissionUpdateRoleStatusBody; path: AdmPermissionUpdateRoleStatusPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPermissionUpdateRoleStatus<T = AdmPermissionUpdateRoleStatusResponse>(options: AdmPermissionUpdateRoleStatusOptions): Promise<T> {
  const response = await orvalAdmPermissionUpdateRoleStatus(options.path["roleId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmPlatformVersionBody = never;
export type AdmPlatformVersionPath = Record<string, never>;
export type AdmPlatformVersionQuery = Record<string, never>;
export type AdmPlatformVersionHeaders = Record<string, never>;
export type AdmPlatformVersionResponse = Record<string, unknown>;
export type AdmPlatformVersionOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admPlatformVersion<T = AdmPlatformVersionResponse>(options: AdmPlatformVersionOptions = {} as AdmPlatformVersionOptions): Promise<T> {
  const response = await orvalAdmPlatformVersion({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogBundleDownloadBody = { artifactIds?: Array<string>; reason?: string };
export type AdmRemoteLogBundleDownloadPath = Record<string, never>;
export type AdmRemoteLogBundleDownloadQuery = Record<string, never>;
export type AdmRemoteLogBundleDownloadHeaders = Record<string, never>;
export type AdmRemoteLogBundleDownloadResponse = Record<string, unknown>;
export type AdmRemoteLogBundleDownloadOptions = CpfGeneratedBaseOptions & { data: AdmRemoteLogBundleDownloadBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogBundleDownload<T = AdmRemoteLogBundleDownloadResponse>(options: AdmRemoteLogBundleDownloadOptions): Promise<T> {
  const response = await orvalAdmRemoteLogBundleDownload(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogBundleDownloadTokenIssueBody = { reason?: string };
export type AdmRemoteLogBundleDownloadTokenIssuePath = { jobId: string };
export type AdmRemoteLogBundleDownloadTokenIssueQuery = Record<string, never>;
export type AdmRemoteLogBundleDownloadTokenIssueHeaders = Record<string, never>;
export type AdmRemoteLogBundleDownloadTokenIssueResponse = Record<string, unknown>;
export type AdmRemoteLogBundleDownloadTokenIssueOptions = CpfGeneratedBaseOptions & { data: AdmRemoteLogBundleDownloadTokenIssueBody; path: AdmRemoteLogBundleDownloadTokenIssuePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogBundleDownloadTokenIssue<T = AdmRemoteLogBundleDownloadTokenIssueResponse>(options: AdmRemoteLogBundleDownloadTokenIssueOptions): Promise<T> {
  const response = await orvalAdmRemoteLogBundleDownloadTokenIssue(options.path["jobId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogBundleJobCreateBody = { artifactIds?: Array<string>; reason?: string };
export type AdmRemoteLogBundleJobCreatePath = Record<string, never>;
export type AdmRemoteLogBundleJobCreateQuery = Record<string, never>;
export type AdmRemoteLogBundleJobCreateHeaders = Record<string, never>;
export type AdmRemoteLogBundleJobCreateResponse = Record<string, unknown>;
export type AdmRemoteLogBundleJobCreateOptions = CpfGeneratedBaseOptions & { data: AdmRemoteLogBundleJobCreateBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogBundleJobCreate<T = AdmRemoteLogBundleJobCreateResponse>(options: AdmRemoteLogBundleJobCreateOptions): Promise<T> {
  const response = await orvalAdmRemoteLogBundleJobCreate(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogBundleJobDownloadBody = never;
export type AdmRemoteLogBundleJobDownloadPath = { jobId: string };
export type AdmRemoteLogBundleJobDownloadQuery = { token: string; reason: string };
export type AdmRemoteLogBundleJobDownloadHeaders = Record<string, never>;
export type AdmRemoteLogBundleJobDownloadResponse = Record<string, unknown>;
export type AdmRemoteLogBundleJobDownloadOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRemoteLogBundleJobDownloadPath; query?: AdmRemoteLogBundleJobDownloadQuery; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogBundleJobDownload<T = AdmRemoteLogBundleJobDownloadResponse>(options: AdmRemoteLogBundleJobDownloadOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRemoteLogBundleJobDownload(options.path["jobId"], contractParams as Parameters<typeof orvalAdmRemoteLogBundleJobDownload>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogBundleJobFindBody = never;
export type AdmRemoteLogBundleJobFindPath = { jobId: string };
export type AdmRemoteLogBundleJobFindQuery = Record<string, never>;
export type AdmRemoteLogBundleJobFindHeaders = Record<string, never>;
export type AdmRemoteLogBundleJobFindResponse = Record<string, unknown>;
export type AdmRemoteLogBundleJobFindOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRemoteLogBundleJobFindPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogBundleJobFind<T = AdmRemoteLogBundleJobFindResponse>(options: AdmRemoteLogBundleJobFindOptions): Promise<T> {
  const response = await orvalAdmRemoteLogBundleJobFind(options.path["jobId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogDiagnosticsBody = never;
export type AdmRemoteLogDiagnosticsPath = Record<string, never>;
export type AdmRemoteLogDiagnosticsQuery = Record<string, never>;
export type AdmRemoteLogDiagnosticsHeaders = Record<string, never>;
export type AdmRemoteLogDiagnosticsResponse = Record<string, unknown>;
export type AdmRemoteLogDiagnosticsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogDiagnostics<T = AdmRemoteLogDiagnosticsResponse>(options: AdmRemoteLogDiagnosticsOptions = {} as AdmRemoteLogDiagnosticsOptions): Promise<T> {
  const response = await orvalAdmRemoteLogDiagnostics({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogDownloadBody = never;
export type AdmRemoteLogDownloadPath = { artifactId: string };
export type AdmRemoteLogDownloadQuery = { reason: string };
export type AdmRemoteLogDownloadHeaders = Record<string, never>;
export type AdmRemoteLogDownloadResponse = Record<string, unknown>;
export type AdmRemoteLogDownloadOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRemoteLogDownloadPath; query?: AdmRemoteLogDownloadQuery; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogDownload<T = AdmRemoteLogDownloadResponse>(options: AdmRemoteLogDownloadOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRemoteLogDownload(options.path["artifactId"], contractParams as Parameters<typeof orvalAdmRemoteLogDownload>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogPreviewBody = never;
export type AdmRemoteLogPreviewPath = { artifactId: string };
export type AdmRemoteLogPreviewQuery = { lastLines?: number; keyword?: string };
export type AdmRemoteLogPreviewHeaders = Record<string, never>;
export type AdmRemoteLogPreviewResponse = Record<string, unknown>;
export type AdmRemoteLogPreviewOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRemoteLogPreviewPath; query?: AdmRemoteLogPreviewQuery; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogPreview<T = AdmRemoteLogPreviewResponse>(options: AdmRemoteLogPreviewOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRemoteLogPreview(options.path["artifactId"], contractParams as Parameters<typeof orvalAdmRemoteLogPreview>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRemoteLogSearchBody = never;
export type AdmRemoteLogSearchPath = Record<string, never>;
export type AdmRemoteLogSearchQuery = { environment?: string; module?: string; service?: string; instance?: string; logType?: string; fileName?: string; standardTransactionId?: string; standardBatchId?: string; transactionId?: string; segmentId?: string; jobInstanceId?: string; jobExecutionId?: string; stepExecutionId?: string; schedulerId?: string; modifiedFrom?: string; modifiedTo?: string; minSize?: number; maxSize?: number; compressed?: boolean; active?: boolean; limit?: number };
export type AdmRemoteLogSearchHeaders = Record<string, never>;
export type AdmRemoteLogSearchResponse = Record<string, unknown>;
export type AdmRemoteLogSearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmRemoteLogSearchQuery; headers?: CpfGeneratedHeaders; };
export async function admRemoteLogSearch<T = AdmRemoteLogSearchResponse>(options: AdmRemoteLogSearchOptions = {} as AdmRemoteLogSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRemoteLogSearch(contractParams as Parameters<typeof orvalAdmRemoteLogSearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResiliencePolicyApproveBody = { reason?: string };
export type AdmResiliencePolicyApprovePath = { requestId: string };
export type AdmResiliencePolicyApproveQuery = Record<string, never>;
export type AdmResiliencePolicyApproveHeaders = { "X-CPF-Risk-Confirmed": string };
export type AdmResiliencePolicyApproveResponse = Record<string, unknown>;
export type AdmResiliencePolicyApproveOptions = CpfGeneratedBaseOptions & { data: AdmResiliencePolicyApproveBody; path: AdmResiliencePolicyApprovePath; query?: never; headers: CpfGeneratedHeaders & AdmResiliencePolicyApproveHeaders; };
export async function admResiliencePolicyApprove<T = AdmResiliencePolicyApproveResponse>(options: AdmResiliencePolicyApproveOptions): Promise<T> {
  const contractParams = { "X-CPF-Risk-Confirmed": headerValue(options.headers, "X-CPF-Risk-Confirmed") };
  const response = await orvalAdmResiliencePolicyApprove(options.path["requestId"], options.data, contractParams as Parameters<typeof orvalAdmResiliencePolicyApprove>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResiliencePolicyFindBody = never;
export type AdmResiliencePolicyFindPath = { operationId: string };
export type AdmResiliencePolicyFindQuery = Record<string, never>;
export type AdmResiliencePolicyFindHeaders = Record<string, never>;
export type AdmResiliencePolicyFindResponse = Record<string, unknown>;
export type AdmResiliencePolicyFindOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmResiliencePolicyFindPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admResiliencePolicyFind<T = AdmResiliencePolicyFindResponse>(options: AdmResiliencePolicyFindOptions): Promise<T> {
  const response = await orvalAdmResiliencePolicyFind(options.path["operationId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResiliencePolicyRejectBody = { reason?: string };
export type AdmResiliencePolicyRejectPath = { requestId: string };
export type AdmResiliencePolicyRejectQuery = Record<string, never>;
export type AdmResiliencePolicyRejectHeaders = { "X-CPF-Risk-Confirmed": string };
export type AdmResiliencePolicyRejectResponse = Record<string, unknown>;
export type AdmResiliencePolicyRejectOptions = CpfGeneratedBaseOptions & { data: AdmResiliencePolicyRejectBody; path: AdmResiliencePolicyRejectPath; query?: never; headers: CpfGeneratedHeaders & AdmResiliencePolicyRejectHeaders; };
export async function admResiliencePolicyReject<T = AdmResiliencePolicyRejectResponse>(options: AdmResiliencePolicyRejectOptions): Promise<T> {
  const contractParams = { "X-CPF-Risk-Confirmed": headerValue(options.headers, "X-CPF-Risk-Confirmed") };
  const response = await orvalAdmResiliencePolicyReject(options.path["requestId"], options.data, contractParams as Parameters<typeof orvalAdmResiliencePolicyReject>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResiliencePolicyRequestBody = { bulkheadMaxConcurrent: number; circuitFailureThreshold: number; circuitOpenMs: number; idempotent: boolean; maxAttempts: number; operationId?: string; rateLimitPermits: number; rateLimitWindowMs: number; reason?: string; retryBackoffMs: number; timeoutMs: number; unknownResultReconcileEnabled: boolean };
export type AdmResiliencePolicyRequestPath = Record<string, never>;
export type AdmResiliencePolicyRequestQuery = Record<string, never>;
export type AdmResiliencePolicyRequestHeaders = Record<string, never>;
export type AdmResiliencePolicyRequestResponse = Record<string, unknown>;
export type AdmResiliencePolicyRequestOptions = CpfGeneratedBaseOptions & { data: AdmResiliencePolicyRequestBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admResiliencePolicyRequest<T = AdmResiliencePolicyRequestResponse>(options: AdmResiliencePolicyRequestOptions): Promise<T> {
  const response = await orvalAdmResiliencePolicyRequest(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResiliencePolicySearchBody = never;
export type AdmResiliencePolicySearchPath = Record<string, never>;
export type AdmResiliencePolicySearchQuery = { query?: string; page?: number; size?: number };
export type AdmResiliencePolicySearchHeaders = Record<string, never>;
export type AdmResiliencePolicySearchResponse = Record<string, unknown>;
export type AdmResiliencePolicySearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmResiliencePolicySearchQuery; headers?: CpfGeneratedHeaders; };
export async function admResiliencePolicySearch<T = AdmResiliencePolicySearchResponse>(options: AdmResiliencePolicySearchOptions = {} as AdmResiliencePolicySearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmResiliencePolicySearch(contractParams as Parameters<typeof orvalAdmResiliencePolicySearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResponseCodeCreateBody = Record<string, unknown>;
export type AdmResponseCodeCreatePath = Record<string, never>;
export type AdmResponseCodeCreateQuery = { reason: string };
export type AdmResponseCodeCreateHeaders = Record<string, never>;
export type AdmResponseCodeCreateResponse = Record<string, unknown>;
export type AdmResponseCodeCreateOptions = CpfGeneratedBaseOptions & { data: AdmResponseCodeCreateBody; path?: never; query?: AdmResponseCodeCreateQuery; headers?: CpfGeneratedHeaders; };
export async function admResponseCodeCreate<T = AdmResponseCodeCreateResponse>(options: AdmResponseCodeCreateOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmResponseCodeCreate(options.data, contractParams as Parameters<typeof orvalAdmResponseCodeCreate>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResponseCodeDeleteBody = never;
export type AdmResponseCodeDeletePath = { responseCode: string };
export type AdmResponseCodeDeleteQuery = { reason: string };
export type AdmResponseCodeDeleteHeaders = Record<string, never>;
export type AdmResponseCodeDeleteResponse = Record<string, unknown>;
export type AdmResponseCodeDeleteOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmResponseCodeDeletePath; query?: AdmResponseCodeDeleteQuery; headers?: CpfGeneratedHeaders; };
export async function admResponseCodeDelete<T = AdmResponseCodeDeleteResponse>(options: AdmResponseCodeDeleteOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmResponseCodeDelete(options.path["responseCode"], contractParams as Parameters<typeof orvalAdmResponseCodeDelete>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResponseCodeFindAllBody = never;
export type AdmResponseCodeFindAllPath = Record<string, never>;
export type AdmResponseCodeFindAllQuery = Record<string, never>;
export type AdmResponseCodeFindAllHeaders = Record<string, never>;
export type AdmResponseCodeFindAllResponse = Record<string, unknown>;
export type AdmResponseCodeFindAllOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admResponseCodeFindAll<T = AdmResponseCodeFindAllResponse>(options: AdmResponseCodeFindAllOptions = {} as AdmResponseCodeFindAllOptions): Promise<T> {
  const response = await orvalAdmResponseCodeFindAll({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResponseCodeFindOneBody = never;
export type AdmResponseCodeFindOnePath = { responseCode: string };
export type AdmResponseCodeFindOneQuery = Record<string, never>;
export type AdmResponseCodeFindOneHeaders = Record<string, never>;
export type AdmResponseCodeFindOneResponse = Record<string, unknown>;
export type AdmResponseCodeFindOneOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmResponseCodeFindOnePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admResponseCodeFindOne<T = AdmResponseCodeFindOneResponse>(options: AdmResponseCodeFindOneOptions): Promise<T> {
  const response = await orvalAdmResponseCodeFindOne(options.path["responseCode"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmResponseCodeUpdateBody = Record<string, unknown>;
export type AdmResponseCodeUpdatePath = { responseCode: string };
export type AdmResponseCodeUpdateQuery = { reason: string };
export type AdmResponseCodeUpdateHeaders = Record<string, never>;
export type AdmResponseCodeUpdateResponse = Record<string, unknown>;
export type AdmResponseCodeUpdateOptions = CpfGeneratedBaseOptions & { data: AdmResponseCodeUpdateBody; path: AdmResponseCodeUpdatePath; query?: AdmResponseCodeUpdateQuery; headers?: CpfGeneratedHeaders; };
export async function admResponseCodeUpdate<T = AdmResponseCodeUpdateResponse>(options: AdmResponseCodeUpdateOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmResponseCodeUpdate(options.path["responseCode"], options.data, contractParams as Parameters<typeof orvalAdmResponseCodeUpdate>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlCancelChangeBody = { operationId?: string; reason?: string };
export type AdmRuntimeControlCancelChangePath = { changeId: string };
export type AdmRuntimeControlCancelChangeQuery = Record<string, never>;
export type AdmRuntimeControlCancelChangeHeaders = Record<string, never>;
export type AdmRuntimeControlCancelChangeResponse = Record<string, unknown>;
export type AdmRuntimeControlCancelChangeOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlCancelChangeBody; path: AdmRuntimeControlCancelChangePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlCancelChange<T = AdmRuntimeControlCancelChangeResponse>(options: AdmRuntimeControlCancelChangeOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlCancelChange(options.path["changeId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlChangeGroupMemberBody = { active: boolean; groupId?: string; instanceId?: string; operationId?: string; reason?: string };
export type AdmRuntimeControlChangeGroupMemberPath = { groupId: string };
export type AdmRuntimeControlChangeGroupMemberQuery = Record<string, never>;
export type AdmRuntimeControlChangeGroupMemberHeaders = Record<string, never>;
export type AdmRuntimeControlChangeGroupMemberResponse = Record<string, unknown>;
export type AdmRuntimeControlChangeGroupMemberOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlChangeGroupMemberBody; path: AdmRuntimeControlChangeGroupMemberPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlChangeGroupMember<T = AdmRuntimeControlChangeGroupMemberResponse>(options: AdmRuntimeControlChangeGroupMemberOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlChangeGroupMember(options.path["groupId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlCreateChangeBody = { approvalId?: string; breakGlassId?: string; changeType?: string; expectedVersion?: number; expiresAt?: string; operationId?: string; payload?: Record<string, unknown>; payloadSchemaVersion: number; quorumPercent?: number; reason?: string; rolloutMode?: string; scheduledAt?: string; target?: Record<string, unknown>; waveSize?: number };
export type AdmRuntimeControlCreateChangePath = Record<string, never>;
export type AdmRuntimeControlCreateChangeQuery = Record<string, never>;
export type AdmRuntimeControlCreateChangeHeaders = Record<string, never>;
export type AdmRuntimeControlCreateChangeResponse = Record<string, unknown>;
export type AdmRuntimeControlCreateChangeOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlCreateChangeBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlCreateChange<T = AdmRuntimeControlCreateChangeResponse>(options: AdmRuntimeControlCreateChangeOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlCreateChange(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlDeleteGroupBody = never;
export type AdmRuntimeControlDeleteGroupPath = { groupId: string };
export type AdmRuntimeControlDeleteGroupQuery = { operationId: string; expectedVersion: number; reason: string };
export type AdmRuntimeControlDeleteGroupHeaders = Record<string, never>;
export type AdmRuntimeControlDeleteGroupResponse = Record<string, unknown>;
export type AdmRuntimeControlDeleteGroupOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRuntimeControlDeleteGroupPath; query?: AdmRuntimeControlDeleteGroupQuery; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlDeleteGroup<T = AdmRuntimeControlDeleteGroupResponse>(options: AdmRuntimeControlDeleteGroupOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRuntimeControlDeleteGroup(options.path["groupId"], contractParams as Parameters<typeof orvalAdmRuntimeControlDeleteGroup>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindByOperationBody = never;
export type AdmRuntimeControlFindByOperationPath = { operationId: string };
export type AdmRuntimeControlFindByOperationQuery = Record<string, never>;
export type AdmRuntimeControlFindByOperationHeaders = Record<string, never>;
export type AdmRuntimeControlFindByOperationResponse = Record<string, unknown>;
export type AdmRuntimeControlFindByOperationOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRuntimeControlFindByOperationPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindByOperation<T = AdmRuntimeControlFindByOperationResponse>(options: AdmRuntimeControlFindByOperationOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindByOperation(options.path["operationId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindCapabilitiesBody = never;
export type AdmRuntimeControlFindCapabilitiesPath = Record<string, never>;
export type AdmRuntimeControlFindCapabilitiesQuery = Record<string, never>;
export type AdmRuntimeControlFindCapabilitiesHeaders = Record<string, never>;
export type AdmRuntimeControlFindCapabilitiesResponse = Record<string, unknown>;
export type AdmRuntimeControlFindCapabilitiesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindCapabilities<T = AdmRuntimeControlFindCapabilitiesResponse>(options: AdmRuntimeControlFindCapabilitiesOptions = {} as AdmRuntimeControlFindCapabilitiesOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindCapabilities({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindChangeBody = never;
export type AdmRuntimeControlFindChangePath = { changeId: string };
export type AdmRuntimeControlFindChangeQuery = Record<string, never>;
export type AdmRuntimeControlFindChangeHeaders = Record<string, never>;
export type AdmRuntimeControlFindChangeResponse = Record<string, unknown>;
export type AdmRuntimeControlFindChangeOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRuntimeControlFindChangePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindChange<T = AdmRuntimeControlFindChangeResponse>(options: AdmRuntimeControlFindChangeOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindChange(options.path["changeId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindGroupBody = never;
export type AdmRuntimeControlFindGroupPath = { groupId: string };
export type AdmRuntimeControlFindGroupQuery = Record<string, never>;
export type AdmRuntimeControlFindGroupHeaders = Record<string, never>;
export type AdmRuntimeControlFindGroupResponse = Record<string, unknown>;
export type AdmRuntimeControlFindGroupOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRuntimeControlFindGroupPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindGroup<T = AdmRuntimeControlFindGroupResponse>(options: AdmRuntimeControlFindGroupOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindGroup(options.path["groupId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindHealthBody = never;
export type AdmRuntimeControlFindHealthPath = Record<string, never>;
export type AdmRuntimeControlFindHealthQuery = Record<string, never>;
export type AdmRuntimeControlFindHealthHeaders = Record<string, never>;
export type AdmRuntimeControlFindHealthResponse = Record<string, unknown>;
export type AdmRuntimeControlFindHealthOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindHealth<T = AdmRuntimeControlFindHealthResponse>(options: AdmRuntimeControlFindHealthOptions = {} as AdmRuntimeControlFindHealthOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindHealth({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindStateCatalogBody = never;
export type AdmRuntimeControlFindStateCatalogPath = Record<string, never>;
export type AdmRuntimeControlFindStateCatalogQuery = Record<string, never>;
export type AdmRuntimeControlFindStateCatalogHeaders = Record<string, never>;
export type AdmRuntimeControlFindStateCatalogResponse = Record<string, unknown>;
export type AdmRuntimeControlFindStateCatalogOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindStateCatalog<T = AdmRuntimeControlFindStateCatalogResponse>(options: AdmRuntimeControlFindStateCatalogOptions = {} as AdmRuntimeControlFindStateCatalogOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlFindStateCatalog({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlFindStatusBody = never;
export type AdmRuntimeControlFindStatusPath = Record<string, never>;
export type AdmRuntimeControlFindStatusQuery = { environment?: string; serviceId?: string };
export type AdmRuntimeControlFindStatusHeaders = Record<string, never>;
export type AdmRuntimeControlFindStatusResponse = Record<string, unknown>;
export type AdmRuntimeControlFindStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmRuntimeControlFindStatusQuery; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlFindStatus<T = AdmRuntimeControlFindStatusResponse>(options: AdmRuntimeControlFindStatusOptions = {} as AdmRuntimeControlFindStatusOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmRuntimeControlFindStatus(contractParams as Parameters<typeof orvalAdmRuntimeControlFindStatus>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlPreviewChangeBody = { approvalId?: string; breakGlassId?: string; changeType?: string; expectedVersion?: number; expiresAt?: string; operationId?: string; payload?: Record<string, unknown>; payloadSchemaVersion: number; quorumPercent?: number; reason?: string; rolloutMode?: string; scheduledAt?: string; target?: Record<string, unknown>; waveSize?: number };
export type AdmRuntimeControlPreviewChangePath = Record<string, never>;
export type AdmRuntimeControlPreviewChangeQuery = Record<string, never>;
export type AdmRuntimeControlPreviewChangeHeaders = Record<string, never>;
export type AdmRuntimeControlPreviewChangeResponse = Record<string, unknown>;
export type AdmRuntimeControlPreviewChangeOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlPreviewChangeBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlPreviewChange<T = AdmRuntimeControlPreviewChangeResponse>(options: AdmRuntimeControlPreviewChangeOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlPreviewChange(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlPreviewTargetsBody = { changeType?: string; payloadSchemaVersion: number; target?: Record<string, unknown> };
export type AdmRuntimeControlPreviewTargetsPath = Record<string, never>;
export type AdmRuntimeControlPreviewTargetsQuery = Record<string, never>;
export type AdmRuntimeControlPreviewTargetsHeaders = Record<string, never>;
export type AdmRuntimeControlPreviewTargetsResponse = Record<string, unknown>;
export type AdmRuntimeControlPreviewTargetsOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlPreviewTargetsBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlPreviewTargets<T = AdmRuntimeControlPreviewTargetsResponse>(options: AdmRuntimeControlPreviewTargetsOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlPreviewTargets(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlRollbackChangeBody = { operationId?: string; reason?: string };
export type AdmRuntimeControlRollbackChangePath = { changeId: string };
export type AdmRuntimeControlRollbackChangeQuery = Record<string, never>;
export type AdmRuntimeControlRollbackChangeHeaders = Record<string, never>;
export type AdmRuntimeControlRollbackChangeResponse = Record<string, unknown>;
export type AdmRuntimeControlRollbackChangeOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlRollbackChangeBody; path: AdmRuntimeControlRollbackChangePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlRollbackChange<T = AdmRuntimeControlRollbackChangeResponse>(options: AdmRuntimeControlRollbackChangeOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlRollbackChange(options.path["changeId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlSaveGroupBody = { active: boolean; description?: string; environment?: string; expectedVersion?: number; groupId?: string; groupName?: string; operationId?: string; parentGroupId?: string; reason?: string };
export type AdmRuntimeControlSaveGroupPath = Record<string, never>;
export type AdmRuntimeControlSaveGroupQuery = Record<string, never>;
export type AdmRuntimeControlSaveGroupHeaders = Record<string, never>;
export type AdmRuntimeControlSaveGroupResponse = Record<string, unknown>;
export type AdmRuntimeControlSaveGroupOptions = CpfGeneratedBaseOptions & { data: AdmRuntimeControlSaveGroupBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlSaveGroup<T = AdmRuntimeControlSaveGroupResponse>(options: AdmRuntimeControlSaveGroupOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlSaveGroup(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmRuntimeControlVerifyAuditBody = never;
export type AdmRuntimeControlVerifyAuditPath = { changeId: string };
export type AdmRuntimeControlVerifyAuditQuery = Record<string, never>;
export type AdmRuntimeControlVerifyAuditHeaders = Record<string, never>;
export type AdmRuntimeControlVerifyAuditResponse = Record<string, unknown>;
export type AdmRuntimeControlVerifyAuditOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmRuntimeControlVerifyAuditPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admRuntimeControlVerifyAudit<T = AdmRuntimeControlVerifyAuditResponse>(options: AdmRuntimeControlVerifyAuditOptions): Promise<T> {
  const response = await orvalAdmRuntimeControlVerifyAudit(options.path["changeId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecretFindMetadataBody = never;
export type AdmSecretFindMetadataPath = Record<string, never>;
export type AdmSecretFindMetadataQuery = { provider: string; key: string };
export type AdmSecretFindMetadataHeaders = Record<string, never>;
export type AdmSecretFindMetadataResponse = Record<string, unknown>;
export type AdmSecretFindMetadataOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmSecretFindMetadataQuery; headers?: CpfGeneratedHeaders; };
export async function admSecretFindMetadata<T = AdmSecretFindMetadataResponse>(options: AdmSecretFindMetadataOptions = {} as AdmSecretFindMetadataOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmSecretFindMetadata(contractParams as Parameters<typeof orvalAdmSecretFindMetadata>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecretFindProvidersBody = never;
export type AdmSecretFindProvidersPath = Record<string, never>;
export type AdmSecretFindProvidersQuery = Record<string, never>;
export type AdmSecretFindProvidersHeaders = Record<string, never>;
export type AdmSecretFindProvidersResponse = Record<string, unknown>;
export type AdmSecretFindProvidersOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecretFindProviders<T = AdmSecretFindProvidersResponse>(options: AdmSecretFindProvidersOptions = {} as AdmSecretFindProvidersOptions): Promise<T> {
  const response = await orvalAdmSecretFindProviders({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecurityDisableMfaBody = never;
export type AdmSecurityDisableMfaPath = { operatorId: string };
export type AdmSecurityDisableMfaQuery = { reason: string };
export type AdmSecurityDisableMfaHeaders = Record<string, never>;
export type AdmSecurityDisableMfaResponse = Record<string, unknown>;
export type AdmSecurityDisableMfaOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmSecurityDisableMfaPath; query?: AdmSecurityDisableMfaQuery; headers?: CpfGeneratedHeaders; };
export async function admSecurityDisableMfa<T = AdmSecurityDisableMfaResponse>(options: AdmSecurityDisableMfaOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmSecurityDisableMfa(options.path["operatorId"], contractParams as Parameters<typeof orvalAdmSecurityDisableMfa>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecurityFindIpAllowlistBody = never;
export type AdmSecurityFindIpAllowlistPath = Record<string, never>;
export type AdmSecurityFindIpAllowlistQuery = Record<string, never>;
export type AdmSecurityFindIpAllowlistHeaders = Record<string, never>;
export type AdmSecurityFindIpAllowlistResponse = Record<string, unknown>;
export type AdmSecurityFindIpAllowlistOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecurityFindIpAllowlist<T = AdmSecurityFindIpAllowlistResponse>(options: AdmSecurityFindIpAllowlistOptions = {} as AdmSecurityFindIpAllowlistOptions): Promise<T> {
  const response = await orvalAdmSecurityFindIpAllowlist({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecurityFindMfaStatesBody = never;
export type AdmSecurityFindMfaStatesPath = Record<string, never>;
export type AdmSecurityFindMfaStatesQuery = Record<string, never>;
export type AdmSecurityFindMfaStatesHeaders = Record<string, never>;
export type AdmSecurityFindMfaStatesResponse = Record<string, unknown>;
export type AdmSecurityFindMfaStatesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecurityFindMfaStates<T = AdmSecurityFindMfaStatesResponse>(options: AdmSecurityFindMfaStatesOptions = {} as AdmSecurityFindMfaStatesOptions): Promise<T> {
  const response = await orvalAdmSecurityFindMfaStates({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecurityRegisterMfaBody = { otpCode?: string; reason?: string; secretRef?: string };
export type AdmSecurityRegisterMfaPath = { operatorId: string };
export type AdmSecurityRegisterMfaQuery = Record<string, never>;
export type AdmSecurityRegisterMfaHeaders = Record<string, never>;
export type AdmSecurityRegisterMfaResponse = Record<string, unknown>;
export type AdmSecurityRegisterMfaOptions = CpfGeneratedBaseOptions & { data: AdmSecurityRegisterMfaBody; path: AdmSecurityRegisterMfaPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecurityRegisterMfa<T = AdmSecurityRegisterMfaResponse>(options: AdmSecurityRegisterMfaOptions): Promise<T> {
  const response = await orvalAdmSecurityRegisterMfa(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecuritySaveIpAllowlistBody = { description?: string; ipPattern?: string; reason?: string; useYn?: string };
export type AdmSecuritySaveIpAllowlistPath = Record<string, never>;
export type AdmSecuritySaveIpAllowlistQuery = Record<string, never>;
export type AdmSecuritySaveIpAllowlistHeaders = Record<string, never>;
export type AdmSecuritySaveIpAllowlistResponse = Record<string, unknown>;
export type AdmSecuritySaveIpAllowlistOptions = CpfGeneratedBaseOptions & { data: AdmSecuritySaveIpAllowlistBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecuritySaveIpAllowlist<T = AdmSecuritySaveIpAllowlistResponse>(options: AdmSecuritySaveIpAllowlistOptions): Promise<T> {
  const response = await orvalAdmSecuritySaveIpAllowlist(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmSecurityVerifyMfaBody = { otpCode?: string; reason?: string; secretRef?: string };
export type AdmSecurityVerifyMfaPath = { operatorId: string };
export type AdmSecurityVerifyMfaQuery = Record<string, never>;
export type AdmSecurityVerifyMfaHeaders = Record<string, never>;
export type AdmSecurityVerifyMfaResponse = Record<string, unknown>;
export type AdmSecurityVerifyMfaOptions = CpfGeneratedBaseOptions & { data: AdmSecurityVerifyMfaBody; path: AdmSecurityVerifyMfaPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admSecurityVerifyMfa<T = AdmSecurityVerifyMfaResponse>(options: AdmSecurityVerifyMfaOptions): Promise<T> {
  const response = await orvalAdmSecurityVerifyMfa(options.path["operatorId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryCapabilitiesBody = never;
export type AdmServiceRegistryCapabilitiesPath = Record<string, never>;
export type AdmServiceRegistryCapabilitiesQuery = Record<string, never>;
export type AdmServiceRegistryCapabilitiesHeaders = Record<string, never>;
export type AdmServiceRegistryCapabilitiesResponse = Record<string, unknown>;
export type AdmServiceRegistryCapabilitiesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryCapabilities<T = AdmServiceRegistryCapabilitiesResponse>(options: AdmServiceRegistryCapabilitiesOptions = {} as AdmServiceRegistryCapabilitiesOptions): Promise<T> {
  const response = await orvalAdmServiceRegistryCapabilities({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindCallHistoryBody = never;
export type AdmServiceRegistryFindCallHistoryPath = Record<string, never>;
export type AdmServiceRegistryFindCallHistoryQuery = { serviceId?: string; transactionId?: string; limit?: number };
export type AdmServiceRegistryFindCallHistoryHeaders = Record<string, never>;
export type AdmServiceRegistryFindCallHistoryResponse = Record<string, unknown>;
export type AdmServiceRegistryFindCallHistoryOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindCallHistoryQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindCallHistory<T = AdmServiceRegistryFindCallHistoryResponse>(options: AdmServiceRegistryFindCallHistoryOptions = {} as AdmServiceRegistryFindCallHistoryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindCallHistory(contractParams as Parameters<typeof orvalAdmServiceRegistryFindCallHistory>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindCircuitStatesBody = never;
export type AdmServiceRegistryFindCircuitStatesPath = Record<string, never>;
export type AdmServiceRegistryFindCircuitStatesQuery = { serviceId?: string; endpointCode?: string; limit?: number };
export type AdmServiceRegistryFindCircuitStatesHeaders = Record<string, never>;
export type AdmServiceRegistryFindCircuitStatesResponse = Record<string, unknown>;
export type AdmServiceRegistryFindCircuitStatesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindCircuitStatesQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindCircuitStates<T = AdmServiceRegistryFindCircuitStatesResponse>(options: AdmServiceRegistryFindCircuitStatesOptions = {} as AdmServiceRegistryFindCircuitStatesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindCircuitStates(contractParams as Parameters<typeof orvalAdmServiceRegistryFindCircuitStates>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindEndpointsBody = never;
export type AdmServiceRegistryFindEndpointsPath = Record<string, never>;
export type AdmServiceRegistryFindEndpointsQuery = { serviceId?: string; endpointCode?: string; useYn?: string; limit?: number };
export type AdmServiceRegistryFindEndpointsHeaders = Record<string, never>;
export type AdmServiceRegistryFindEndpointsResponse = Record<string, unknown>;
export type AdmServiceRegistryFindEndpointsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindEndpointsQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindEndpoints<T = AdmServiceRegistryFindEndpointsResponse>(options: AdmServiceRegistryFindEndpointsOptions = {} as AdmServiceRegistryFindEndpointsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindEndpoints(contractParams as Parameters<typeof orvalAdmServiceRegistryFindEndpoints>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindHealthBody = never;
export type AdmServiceRegistryFindHealthPath = Record<string, never>;
export type AdmServiceRegistryFindHealthQuery = { serviceId?: string; endpointCode?: string; limit?: number };
export type AdmServiceRegistryFindHealthHeaders = Record<string, never>;
export type AdmServiceRegistryFindHealthResponse = Record<string, unknown>;
export type AdmServiceRegistryFindHealthOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindHealthQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindHealth<T = AdmServiceRegistryFindHealthResponse>(options: AdmServiceRegistryFindHealthOptions = {} as AdmServiceRegistryFindHealthOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindHealth(contractParams as Parameters<typeof orvalAdmServiceRegistryFindHealth>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindInstancesBody = never;
export type AdmServiceRegistryFindInstancesPath = Record<string, never>;
export type AdmServiceRegistryFindInstancesQuery = { serviceId?: string; endpointCode?: string; status?: string; limit?: number };
export type AdmServiceRegistryFindInstancesHeaders = Record<string, never>;
export type AdmServiceRegistryFindInstancesResponse = Record<string, unknown>;
export type AdmServiceRegistryFindInstancesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindInstancesQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindInstances<T = AdmServiceRegistryFindInstancesResponse>(options: AdmServiceRegistryFindInstancesOptions = {} as AdmServiceRegistryFindInstancesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindInstances(contractParams as Parameters<typeof orvalAdmServiceRegistryFindInstances>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindRoutingPoliciesBody = never;
export type AdmServiceRegistryFindRoutingPoliciesPath = Record<string, never>;
export type AdmServiceRegistryFindRoutingPoliciesQuery = { serviceId?: string; endpointCode?: string; activeYn?: string; limit?: number };
export type AdmServiceRegistryFindRoutingPoliciesHeaders = Record<string, never>;
export type AdmServiceRegistryFindRoutingPoliciesResponse = Record<string, unknown>;
export type AdmServiceRegistryFindRoutingPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindRoutingPoliciesQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindRoutingPolicies<T = AdmServiceRegistryFindRoutingPoliciesResponse>(options: AdmServiceRegistryFindRoutingPoliciesOptions = {} as AdmServiceRegistryFindRoutingPoliciesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindRoutingPolicies(contractParams as Parameters<typeof orvalAdmServiceRegistryFindRoutingPolicies>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistryFindServicesBody = never;
export type AdmServiceRegistryFindServicesPath = Record<string, never>;
export type AdmServiceRegistryFindServicesQuery = { serviceId?: string; useYn?: string; limit?: number };
export type AdmServiceRegistryFindServicesHeaders = Record<string, never>;
export type AdmServiceRegistryFindServicesResponse = Record<string, unknown>;
export type AdmServiceRegistryFindServicesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmServiceRegistryFindServicesQuery; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistryFindServices<T = AdmServiceRegistryFindServicesResponse>(options: AdmServiceRegistryFindServicesOptions = {} as AdmServiceRegistryFindServicesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmServiceRegistryFindServices(contractParams as Parameters<typeof orvalAdmServiceRegistryFindServices>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistrySaveEndpointBody = Record<string, unknown>;
export type AdmServiceRegistrySaveEndpointPath = Record<string, never>;
export type AdmServiceRegistrySaveEndpointQuery = Record<string, never>;
export type AdmServiceRegistrySaveEndpointHeaders = Record<string, never>;
export type AdmServiceRegistrySaveEndpointResponse = Record<string, unknown>;
export type AdmServiceRegistrySaveEndpointOptions = CpfGeneratedBaseOptions & { data: AdmServiceRegistrySaveEndpointBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistrySaveEndpoint<T = AdmServiceRegistrySaveEndpointResponse>(options: AdmServiceRegistrySaveEndpointOptions): Promise<T> {
  const response = await orvalAdmServiceRegistrySaveEndpoint(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistrySaveInstanceBody = Record<string, unknown>;
export type AdmServiceRegistrySaveInstancePath = Record<string, never>;
export type AdmServiceRegistrySaveInstanceQuery = Record<string, never>;
export type AdmServiceRegistrySaveInstanceHeaders = Record<string, never>;
export type AdmServiceRegistrySaveInstanceResponse = Record<string, unknown>;
export type AdmServiceRegistrySaveInstanceOptions = CpfGeneratedBaseOptions & { data: AdmServiceRegistrySaveInstanceBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistrySaveInstance<T = AdmServiceRegistrySaveInstanceResponse>(options: AdmServiceRegistrySaveInstanceOptions): Promise<T> {
  const response = await orvalAdmServiceRegistrySaveInstance(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmServiceRegistrySaveServiceBody = Record<string, unknown>;
export type AdmServiceRegistrySaveServicePath = Record<string, never>;
export type AdmServiceRegistrySaveServiceQuery = Record<string, never>;
export type AdmServiceRegistrySaveServiceHeaders = Record<string, never>;
export type AdmServiceRegistrySaveServiceResponse = Record<string, unknown>;
export type AdmServiceRegistrySaveServiceOptions = CpfGeneratedBaseOptions & { data: AdmServiceRegistrySaveServiceBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function admServiceRegistrySaveService<T = AdmServiceRegistrySaveServiceResponse>(options: AdmServiceRegistrySaveServiceOptions): Promise<T> {
  const response = await orvalAdmServiceRegistrySaveService(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmStandardExecutionFindAllBody = never;
export type AdmStandardExecutionFindAllPath = Record<string, never>;
export type AdmStandardExecutionFindAllQuery = { type?: string; ownerDomain?: string; keyword?: string };
export type AdmStandardExecutionFindAllHeaders = Record<string, never>;
export type AdmStandardExecutionFindAllResponse = Record<string, unknown>;
export type AdmStandardExecutionFindAllOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmStandardExecutionFindAllQuery; headers?: CpfGeneratedHeaders; };
export async function admStandardExecutionFindAll<T = AdmStandardExecutionFindAllResponse>(options: AdmStandardExecutionFindAllOptions = {} as AdmStandardExecutionFindAllOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmStandardExecutionFindAll(contractParams as Parameters<typeof orvalAdmStandardExecutionFindAll>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmStandardExecutionFindOneBody = never;
export type AdmStandardExecutionFindOnePath = { standardExecutionId: string };
export type AdmStandardExecutionFindOneQuery = Record<string, never>;
export type AdmStandardExecutionFindOneHeaders = Record<string, never>;
export type AdmStandardExecutionFindOneResponse = Record<string, unknown>;
export type AdmStandardExecutionFindOneOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmStandardExecutionFindOnePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admStandardExecutionFindOne<T = AdmStandardExecutionFindOneResponse>(options: AdmStandardExecutionFindOneOptions): Promise<T> {
  const response = await orvalAdmStandardExecutionFindOne(options.path["standardExecutionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindDetailBody = never;
export type AdmTransactionGroupFindDetailPath = { transactionId: string };
export type AdmTransactionGroupFindDetailQuery = Record<string, never>;
export type AdmTransactionGroupFindDetailHeaders = Record<string, never>;
export type AdmTransactionGroupFindDetailResponse = Record<string, unknown>;
export type AdmTransactionGroupFindDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionGroupFindDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindDetail<T = AdmTransactionGroupFindDetailResponse>(options: AdmTransactionGroupFindDetailOptions): Promise<T> {
  const response = await orvalAdmTransactionGroupFindDetail(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindExternalLogsBody = never;
export type AdmTransactionGroupFindExternalLogsPath = { transactionId: string };
export type AdmTransactionGroupFindExternalLogsQuery = Record<string, never>;
export type AdmTransactionGroupFindExternalLogsHeaders = Record<string, never>;
export type AdmTransactionGroupFindExternalLogsResponse = Record<string, unknown>;
export type AdmTransactionGroupFindExternalLogsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionGroupFindExternalLogsPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindExternalLogs<T = AdmTransactionGroupFindExternalLogsResponse>(options: AdmTransactionGroupFindExternalLogsOptions): Promise<T> {
  const response = await orvalAdmTransactionGroupFindExternalLogs(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindGroupsBody = never;
export type AdmTransactionGroupFindGroupsPath = Record<string, never>;
export type AdmTransactionGroupFindGroupsQuery = { criteria: Record<string, string> };
export type AdmTransactionGroupFindGroupsHeaders = Record<string, never>;
export type AdmTransactionGroupFindGroupsResponse = Record<string, unknown>;
export type AdmTransactionGroupFindGroupsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmTransactionGroupFindGroupsQuery; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindGroups<T = AdmTransactionGroupFindGroupsResponse>(options: AdmTransactionGroupFindGroupsOptions = {} as AdmTransactionGroupFindGroupsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmTransactionGroupFindGroups(contractParams as Parameters<typeof orvalAdmTransactionGroupFindGroups>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindHeadersBody = never;
export type AdmTransactionGroupFindHeadersPath = { transactionId: string };
export type AdmTransactionGroupFindHeadersQuery = Record<string, never>;
export type AdmTransactionGroupFindHeadersHeaders = Record<string, never>;
export type AdmTransactionGroupFindHeadersResponse = Record<string, unknown>;
export type AdmTransactionGroupFindHeadersOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionGroupFindHeadersPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindHeaders<T = AdmTransactionGroupFindHeadersResponse>(options: AdmTransactionGroupFindHeadersOptions): Promise<T> {
  const response = await orvalAdmTransactionGroupFindHeaders(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindSegmentsBody = never;
export type AdmTransactionGroupFindSegmentsPath = { transactionId: string };
export type AdmTransactionGroupFindSegmentsQuery = Record<string, never>;
export type AdmTransactionGroupFindSegmentsHeaders = Record<string, never>;
export type AdmTransactionGroupFindSegmentsResponse = Record<string, unknown>;
export type AdmTransactionGroupFindSegmentsOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionGroupFindSegmentsPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindSegments<T = AdmTransactionGroupFindSegmentsResponse>(options: AdmTransactionGroupFindSegmentsOptions): Promise<T> {
  const response = await orvalAdmTransactionGroupFindSegments(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionGroupFindTimelineBody = never;
export type AdmTransactionGroupFindTimelinePath = { transactionId: string };
export type AdmTransactionGroupFindTimelineQuery = Record<string, never>;
export type AdmTransactionGroupFindTimelineHeaders = Record<string, never>;
export type AdmTransactionGroupFindTimelineResponse = Record<string, unknown>;
export type AdmTransactionGroupFindTimelineOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionGroupFindTimelinePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionGroupFindTimeline<T = AdmTransactionGroupFindTimelineResponse>(options: AdmTransactionGroupFindTimelineOptions): Promise<T> {
  const response = await orvalAdmTransactionGroupFindTimeline(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionMetaFindPageBody = never;
export type AdmTransactionMetaFindPagePath = Record<string, never>;
export type AdmTransactionMetaFindPageQuery = { moduleCode?: string; activeYn?: string; transactionId?: string; page?: number; size?: number };
export type AdmTransactionMetaFindPageHeaders = Record<string, never>;
export type AdmTransactionMetaFindPageResponse = Record<string, unknown>;
export type AdmTransactionMetaFindPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmTransactionMetaFindPageQuery; headers?: CpfGeneratedHeaders; };
export async function admTransactionMetaFindPage<T = AdmTransactionMetaFindPageResponse>(options: AdmTransactionMetaFindPageOptions = {} as AdmTransactionMetaFindPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmTransactionMetaFindPage(contractParams as Parameters<typeof orvalAdmTransactionMetaFindPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionMetaFindTransactionBody = never;
export type AdmTransactionMetaFindTransactionPath = { transactionId: string };
export type AdmTransactionMetaFindTransactionQuery = Record<string, never>;
export type AdmTransactionMetaFindTransactionHeaders = Record<string, never>;
export type AdmTransactionMetaFindTransactionResponse = Record<string, unknown>;
export type AdmTransactionMetaFindTransactionOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionMetaFindTransactionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function admTransactionMetaFindTransaction<T = AdmTransactionMetaFindTransactionResponse>(options: AdmTransactionMetaFindTransactionOptions): Promise<T> {
  const response = await orvalAdmTransactionMetaFindTransaction(options.path["transactionId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionMetaFindTransactionsBody = never;
export type AdmTransactionMetaFindTransactionsPath = Record<string, never>;
export type AdmTransactionMetaFindTransactionsQuery = { moduleCode?: string; activeYn?: string; transactionId?: string; limit?: number };
export type AdmTransactionMetaFindTransactionsHeaders = Record<string, never>;
export type AdmTransactionMetaFindTransactionsResponse = Record<string, unknown>;
export type AdmTransactionMetaFindTransactionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmTransactionMetaFindTransactionsQuery; headers?: CpfGeneratedHeaders; };
export async function admTransactionMetaFindTransactions<T = AdmTransactionMetaFindTransactionsResponse>(options: AdmTransactionMetaFindTransactionsOptions = {} as AdmTransactionMetaFindTransactionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmTransactionMetaFindTransactions(contractParams as Parameters<typeof orvalAdmTransactionMetaFindTransactions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionMetaInactivateBody = never;
export type AdmTransactionMetaInactivatePath = { transactionId: string };
export type AdmTransactionMetaInactivateQuery = { reason: string };
export type AdmTransactionMetaInactivateHeaders = Record<string, never>;
export type AdmTransactionMetaInactivateResponse = Record<string, unknown>;
export type AdmTransactionMetaInactivateOptions = CpfGeneratedBaseOptions & { data?: never; path: AdmTransactionMetaInactivatePath; query?: AdmTransactionMetaInactivateQuery; headers?: CpfGeneratedHeaders; };
export async function admTransactionMetaInactivate<T = AdmTransactionMetaInactivateResponse>(options: AdmTransactionMetaInactivateOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmTransactionMetaInactivate(options.path["transactionId"], contractParams as Parameters<typeof orvalAdmTransactionMetaInactivate>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type AdmTransactionMetaScanBody = never;
export type AdmTransactionMetaScanPath = Record<string, never>;
export type AdmTransactionMetaScanQuery = { reason: string };
export type AdmTransactionMetaScanHeaders = Record<string, never>;
export type AdmTransactionMetaScanResponse = Record<string, unknown>;
export type AdmTransactionMetaScanOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: AdmTransactionMetaScanQuery; headers?: CpfGeneratedHeaders; };
export async function admTransactionMetaScan<T = AdmTransactionMetaScanResponse>(options: AdmTransactionMetaScanOptions = {} as AdmTransactionMetaScanOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalAdmTransactionMetaScan(contractParams as Parameters<typeof orvalAdmTransactionMetaScan>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmBatchJobInstanceLogsBody = never;
export type FindAdmBatchJobInstanceLogsPath = Record<string, never>;
export type FindAdmBatchJobInstanceLogsQuery = { businessDate?: string; jobName?: string; jobInstanceId?: number; serverInstanceId?: string; limit?: number };
export type FindAdmBatchJobInstanceLogsHeaders = Record<string, never>;
export type FindAdmBatchJobInstanceLogsResponse = Record<string, unknown>;
export type FindAdmBatchJobInstanceLogsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmBatchJobInstanceLogsQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmBatchJobInstanceLogs<T = FindAdmBatchJobInstanceLogsResponse>(options: FindAdmBatchJobInstanceLogsOptions = {} as FindAdmBatchJobInstanceLogsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmBatchJobInstanceLogs(contractParams as Parameters<typeof orvalFindAdmBatchJobInstanceLogs>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmBrokerDlqBody = never;
export type FindAdmBrokerDlqPath = Record<string, never>;
export type FindAdmBrokerDlqQuery = { status?: string; transactionId?: string; topic?: string; limit?: number };
export type FindAdmBrokerDlqHeaders = Record<string, never>;
export type FindAdmBrokerDlqResponse = Record<string, unknown>;
export type FindAdmBrokerDlqOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmBrokerDlqQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmBrokerDlq<T = FindAdmBrokerDlqResponse>(options: FindAdmBrokerDlqOptions = {} as FindAdmBrokerDlqOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmBrokerDlq(contractParams as Parameters<typeof orvalFindAdmBrokerDlq>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmBrokerInboxBody = never;
export type FindAdmBrokerInboxPath = Record<string, never>;
export type FindAdmBrokerInboxQuery = { status?: string; key?: string; limit?: number };
export type FindAdmBrokerInboxHeaders = Record<string, never>;
export type FindAdmBrokerInboxResponse = Record<string, unknown>;
export type FindAdmBrokerInboxOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmBrokerInboxQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmBrokerInbox<T = FindAdmBrokerInboxResponse>(options: FindAdmBrokerInboxOptions = {} as FindAdmBrokerInboxOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmBrokerInbox(contractParams as Parameters<typeof orvalFindAdmBrokerInbox>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmBrokerOutboxBody = never;
export type FindAdmBrokerOutboxPath = Record<string, never>;
export type FindAdmBrokerOutboxQuery = { status?: string; transactionId?: string; topic?: string; limit?: number };
export type FindAdmBrokerOutboxHeaders = Record<string, never>;
export type FindAdmBrokerOutboxResponse = Record<string, unknown>;
export type FindAdmBrokerOutboxOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmBrokerOutboxQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmBrokerOutbox<T = FindAdmBrokerOutboxResponse>(options: FindAdmBrokerOutboxOptions = {} as FindAdmBrokerOutboxOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmBrokerOutbox(contractParams as Parameters<typeof orvalFindAdmBrokerOutbox>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmFileTransferHistoryBody = never;
export type FindAdmFileTransferHistoryPath = Record<string, never>;
export type FindAdmFileTransferHistoryQuery = { status?: string; transactionId?: string; endpointCode?: string; limit?: number };
export type FindAdmFileTransferHistoryHeaders = Record<string, never>;
export type FindAdmFileTransferHistoryResponse = Record<string, unknown>;
export type FindAdmFileTransferHistoryOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmFileTransferHistoryQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmFileTransferHistory<T = FindAdmFileTransferHistoryResponse>(options: FindAdmFileTransferHistoryOptions = {} as FindAdmFileTransferHistoryOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmFileTransferHistory(contractParams as Parameters<typeof orvalFindAdmFileTransferHistory>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmIdempotencyRecordsBody = never;
export type FindAdmIdempotencyRecordsPath = Record<string, never>;
export type FindAdmIdempotencyRecordsQuery = { scope?: string; status?: string; key?: string; limit?: number };
export type FindAdmIdempotencyRecordsHeaders = Record<string, never>;
export type FindAdmIdempotencyRecordsResponse = Record<string, unknown>;
export type FindAdmIdempotencyRecordsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmIdempotencyRecordsQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmIdempotencyRecords<T = FindAdmIdempotencyRecordsResponse>(options: FindAdmIdempotencyRecordsOptions = {} as FindAdmIdempotencyRecordsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmIdempotencyRecords(contractParams as Parameters<typeof orvalFindAdmIdempotencyRecords>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type FindAdmUnknownResultsBody = never;
export type FindAdmUnknownResultsPath = Record<string, never>;
export type FindAdmUnknownResultsQuery = { type?: string; status?: string; transactionId?: string; limit?: number };
export type FindAdmUnknownResultsHeaders = Record<string, never>;
export type FindAdmUnknownResultsResponse = Record<string, unknown>;
export type FindAdmUnknownResultsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: FindAdmUnknownResultsQuery; headers?: CpfGeneratedHeaders; };
export async function findAdmUnknownResults<T = FindAdmUnknownResultsResponse>(options: FindAdmUnknownResultsOptions = {} as FindAdmUnknownResultsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalFindAdmUnknownResults(contractParams as Parameters<typeof orvalFindAdmUnknownResults>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmBatchJobInstanceLogBody = never;
export type GetAdmBatchJobInstanceLogPath = { businessDate: string; jobName: string; jobInstanceId: number };
export type GetAdmBatchJobInstanceLogQuery = { serverInstanceId: string; maxRecords?: number };
export type GetAdmBatchJobInstanceLogHeaders = Record<string, never>;
export type GetAdmBatchJobInstanceLogResponse = Record<string, unknown>;
export type GetAdmBatchJobInstanceLogOptions = CpfGeneratedBaseOptions & { data?: never; path: GetAdmBatchJobInstanceLogPath; query?: GetAdmBatchJobInstanceLogQuery; headers?: CpfGeneratedHeaders; };
export async function getAdmBatchJobInstanceLog<T = GetAdmBatchJobInstanceLogResponse>(options: GetAdmBatchJobInstanceLogOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalGetAdmBatchJobInstanceLog(options.path["businessDate"], options.path["jobName"], options.path["jobInstanceId"], contractParams as Parameters<typeof orvalGetAdmBatchJobInstanceLog>[3], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmFileLogRecoveryStatusBody = never;
export type GetAdmFileLogRecoveryStatusPath = Record<string, never>;
export type GetAdmFileLogRecoveryStatusQuery = Record<string, never>;
export type GetAdmFileLogRecoveryStatusHeaders = Record<string, never>;
export type GetAdmFileLogRecoveryStatusResponse = Record<string, unknown>;
export type GetAdmFileLogRecoveryStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function getAdmFileLogRecoveryStatus<T = GetAdmFileLogRecoveryStatusResponse>(options: GetAdmFileLogRecoveryStatusOptions = {} as GetAdmFileLogRecoveryStatusOptions): Promise<T> {
  const response = await orvalGetAdmFileLogRecoveryStatus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmLivenessBody = never;
export type GetAdmLivenessPath = Record<string, never>;
export type GetAdmLivenessQuery = Record<string, never>;
export type GetAdmLivenessHeaders = Record<string, never>;
export type GetAdmLivenessResponse = Record<string, unknown>;
export type GetAdmLivenessOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function getAdmLiveness<T = GetAdmLivenessResponse>(options: GetAdmLivenessOptions = {} as GetAdmLivenessOptions): Promise<T> {
  const response = await orvalGetAdmLiveness({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmReadinessBody = never;
export type GetAdmReadinessPath = Record<string, never>;
export type GetAdmReadinessQuery = Record<string, never>;
export type GetAdmReadinessHeaders = Record<string, never>;
export type GetAdmReadinessResponse = Record<string, unknown>;
export type GetAdmReadinessOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function getAdmReadiness<T = GetAdmReadinessResponse>(options: GetAdmReadinessOptions = {} as GetAdmReadinessOptions): Promise<T> {
  const response = await orvalGetAdmReadiness({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmSystemVersionBody = never;
export type GetAdmSystemVersionPath = Record<string, never>;
export type GetAdmSystemVersionQuery = Record<string, never>;
export type GetAdmSystemVersionHeaders = Record<string, never>;
export type GetAdmSystemVersionResponse = Record<string, unknown>;
export type GetAdmSystemVersionOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function getAdmSystemVersion<T = GetAdmSystemVersionResponse>(options: GetAdmSystemVersionOptions = {} as GetAdmSystemVersionOptions): Promise<T> {
  const response = await orvalGetAdmSystemVersion({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type GetAdmTransactionLogRecoveryStatusBody = never;
export type GetAdmTransactionLogRecoveryStatusPath = Record<string, never>;
export type GetAdmTransactionLogRecoveryStatusQuery = Record<string, never>;
export type GetAdmTransactionLogRecoveryStatusHeaders = Record<string, never>;
export type GetAdmTransactionLogRecoveryStatusResponse = Record<string, unknown>;
export type GetAdmTransactionLogRecoveryStatusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function getAdmTransactionLogRecoveryStatus<T = GetAdmTransactionLogRecoveryStatusResponse>(options: GetAdmTransactionLogRecoveryStatusOptions = {} as GetAdmTransactionLogRecoveryStatusOptions): Promise<T> {
  const response = await orvalGetAdmTransactionLogRecoveryStatus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type RequestAdmBrokerDlqReplayBody = { expectedVersion?: number; reason: string; targetStatus?: string };
export type RequestAdmBrokerDlqReplayPath = { messageId: string };
export type RequestAdmBrokerDlqReplayQuery = Record<string, never>;
export type RequestAdmBrokerDlqReplayHeaders = Record<string, never>;
export type RequestAdmBrokerDlqReplayResponse = Record<string, unknown>;
export type RequestAdmBrokerDlqReplayOptions = CpfGeneratedBaseOptions & { data: RequestAdmBrokerDlqReplayBody; path: RequestAdmBrokerDlqReplayPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function requestAdmBrokerDlqReplay<T = RequestAdmBrokerDlqReplayResponse>(options: RequestAdmBrokerDlqReplayOptions): Promise<T> {
  const response = await orvalRequestAdmBrokerDlqReplay(options.path["messageId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type ResolveAdmUnknownResultBody = { expectedVersion?: number; reason: string; targetStatus?: string };
export type ResolveAdmUnknownResultPath = { unknownId: string };
export type ResolveAdmUnknownResultQuery = Record<string, never>;
export type ResolveAdmUnknownResultHeaders = Record<string, never>;
export type ResolveAdmUnknownResultResponse = Record<string, unknown>;
export type ResolveAdmUnknownResultOptions = CpfGeneratedBaseOptions & { data: ResolveAdmUnknownResultBody; path: ResolveAdmUnknownResultPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function resolveAdmUnknownResult<T = ResolveAdmUnknownResultResponse>(options: ResolveAdmUnknownResultOptions): Promise<T> {
  const response = await orvalResolveAdmUnknownResult(options.path["unknownId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type RetryAdmTraceRecoveryPoisonBody = { expectedVersion?: number; reason: string; targetStatus?: string };
export type RetryAdmTraceRecoveryPoisonPath = { target: string; recoveryEventId: string };
export type RetryAdmTraceRecoveryPoisonQuery = Record<string, never>;
export type RetryAdmTraceRecoveryPoisonHeaders = Record<string, never>;
export type RetryAdmTraceRecoveryPoisonResponse = Record<string, unknown>;
export type RetryAdmTraceRecoveryPoisonOptions = CpfGeneratedBaseOptions & { data: RetryAdmTraceRecoveryPoisonBody; path: RetryAdmTraceRecoveryPoisonPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function retryAdmTraceRecoveryPoison<T = RetryAdmTraceRecoveryPoisonResponse>(options: RetryAdmTraceRecoveryPoisonOptions): Promise<T> {
  const response = await orvalRetryAdmTraceRecoveryPoison(options.path["target"], options.path["recoveryEventId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type RunAdmTransactionLogRecoveryBody = { expectedVersion?: number; reason: string; targetStatus?: string };
export type RunAdmTransactionLogRecoveryPath = Record<string, never>;
export type RunAdmTransactionLogRecoveryQuery = Record<string, never>;
export type RunAdmTransactionLogRecoveryHeaders = Record<string, never>;
export type RunAdmTransactionLogRecoveryResponse = Record<string, unknown>;
export type RunAdmTransactionLogRecoveryOptions = CpfGeneratedBaseOptions & { data: RunAdmTransactionLogRecoveryBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function runAdmTransactionLogRecovery<T = RunAdmTransactionLogRecoveryResponse>(options: RunAdmTransactionLogRecoveryOptions): Promise<T> {
  const response = await orvalRunAdmTransactionLogRecovery(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type TraceAdmByBusinessTransactionIdBody = never;
export type TraceAdmByBusinessTransactionIdPath = { businessTransactionId: string };
export type TraceAdmByBusinessTransactionIdQuery = { limit?: number };
export type TraceAdmByBusinessTransactionIdHeaders = Record<string, never>;
export type TraceAdmByBusinessTransactionIdResponse = Record<string, unknown>;
export type TraceAdmByBusinessTransactionIdOptions = CpfGeneratedBaseOptions & { data?: never; path: TraceAdmByBusinessTransactionIdPath; query?: TraceAdmByBusinessTransactionIdQuery; headers?: CpfGeneratedHeaders; };
export async function traceAdmByBusinessTransactionId<T = TraceAdmByBusinessTransactionIdResponse>(options: TraceAdmByBusinessTransactionIdOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalTraceAdmByBusinessTransactionId(options.path["businessTransactionId"], contractParams as Parameters<typeof orvalTraceAdmByBusinessTransactionId>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type TraceAdmByTraceIdBody = never;
export type TraceAdmByTraceIdPath = { traceId: string };
export type TraceAdmByTraceIdQuery = { limit?: number };
export type TraceAdmByTraceIdHeaders = Record<string, never>;
export type TraceAdmByTraceIdResponse = Record<string, unknown>;
export type TraceAdmByTraceIdOptions = CpfGeneratedBaseOptions & { data?: never; path: TraceAdmByTraceIdPath; query?: TraceAdmByTraceIdQuery; headers?: CpfGeneratedHeaders; };
export async function traceAdmByTraceId<T = TraceAdmByTraceIdResponse>(options: TraceAdmByTraceIdOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalTraceAdmByTraceId(options.path["traceId"], contractParams as Parameters<typeof orvalTraceAdmByTraceId>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type TraceAdmByTransactionIdBody = never;
export type TraceAdmByTransactionIdPath = { transactionId: string };
export type TraceAdmByTransactionIdQuery = { limit?: number };
export type TraceAdmByTransactionIdHeaders = Record<string, never>;
export type TraceAdmByTransactionIdResponse = Record<string, unknown>;
export type TraceAdmByTransactionIdOptions = CpfGeneratedBaseOptions & { data?: never; path: TraceAdmByTransactionIdPath; query?: TraceAdmByTransactionIdQuery; headers?: CpfGeneratedHeaders; };
export async function traceAdmByTransactionId<T = TraceAdmByTransactionIdResponse>(options: TraceAdmByTransactionIdOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalTraceAdmByTransactionId(options.path["transactionId"], contractParams as Parameters<typeof orvalTraceAdmByTransactionId>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}
