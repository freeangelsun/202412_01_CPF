/**
 * Generated from the CPF controller-source pre-runtime OpenAPI contract.
 * Runtime OpenAPI generation must replace this deterministic compatibility client.
 */
import { useMutation, useQuery } from '@tanstack/vue-query';
import type { DataTag, MutationFunction, QueryClient, QueryFunction, QueryKey, UseMutationOptions, UseMutationReturnType, UseQueryOptions, UseQueryReturnType } from '@tanstack/vue-query';
import { computed, toValue, unref } from 'vue';
import type { MaybeRefOrGetter } from 'vue';
import type {
  AdmApiPermissionRoleUpdateRequest,
  AdmApiPermissionSaveRequest,
  AdmIntegrationWebhookReplayParams,
  AdmIntegrationWebhookDlqParams,
  AdmIntegrationTimeHealthParams,
  AdmIntegrationRecord,
  AdmIntegrationDataQualityReplayParams,
  AdmIntegrationCorrectionExecutionRequest,
  AdmIntegrationCorrectionApprovalRequest,
  AdmApprovalReconcileParams,
  AdmApprovalPoliciesParams,
  AdmAuditDeliveryListParams,
  AdmAuditDeliveryRetryParams,
  AdmAuditLogFindAuditLogsParams,
  AdmAuthLogoutParams,
  AdmAuthMeParams,
  AdmBatchFindExecutionPageParams,
  AdmBatchFindExecutionTargetsParams,
  AdmBatchFindExecutionsParams,
  AdmBatchFindGhostCandidatesParams,
  AdmBatchFindLocksParams,
  AdmBatchFindOperationLogsParams,
  AdmBatchFindRelationsParams,
  AdmBatchFindStepExecutionsParams,
  AdmBatchFindWorkersParams,
  AdmBatchGhostActionRequest,
  AdmBatchJobRegisterRequest,
  AdmBatchLockReleaseRequest,
  AdmBatchOperationRequest,
  AdmBatchSimulateScheduleParams,
  AdmBatchWorkbenchExecutionsParams,
  AdmBatchWorkbenchInfrastructureParams,
  AdmBatchWorkbenchJobsParams,
  AdmBatchWorkbenchRecoveryParams,
  AdmBatchWorkbenchSchedulesParams,
  AdmBreakGlassCloseSessionRequest,
  AdmBreakGlassFindSessionsParams,
  AdmBreakGlassOpenSessionRequest,
  AdmBreakGlassReviewSessionRequest,
  AdmButtonPermissionUpdateRequest,
  AdmButtonSaveRequest,
  AdmCacheControlRequest,
  AdmCacheEvictKeyRequest,
  AdmCacheEvictNamespaceRequest,
  AdmCacheReconcileRequest,
  AdmCacheRefreshParams,
  AdmCalendarDeleteDayParams,
  AdmCalendarFindDaysParams,
  AdmCalendarResolveDateParams,
  AdmCalendarSaveDayParams,
  AdmCalendarSaveDayRequest,
  AdmCenterCutActionRequest,
  AdmCenterCutFindResultsParams,
  AdmCenterCutFindTargetsParams,
  AdmChannelPackageImportRequest,
  AdmChannelPolicySaveRequest,
  AdmChannelRefreshSnapshotParams,
  AdmChannelSaveRequest,
  AdmCodeDeleteCodeParams,
  AdmConfigDeleteConfigParams,
  AdmDownloadFindDownloadAuditLogsParams,
  AdmDynamicLogLevelRegisterParams,
  AdmDynamicLogLevelRemoveParams,
  AdmFeatureFlagApproveOverrideParams,
  AdmFeatureFlagApproveOverrideRequest,
  AdmFeatureFlagDecisionRequest,
  AdmFeatureFlagEvaluateRequest,
  AdmFeatureFlagKillSwitchRequest,
  AdmFeatureFlagOverrideRequest,
  AdmFeatureFlagRequestOverrideRequest,
  AdmFeatureFlagRevokeOverrideParams,
  AdmFeatureFlagRevokeOverrideRequest,
  AdmFeatureFlagSearchParams,
  AdmFeatureFlagSetKillSwitchParams,
  AdmFeatureFlagSetKillSwitchRequest,
  AdmFileJobApplyRequest,
  AdmFileJobCancelRequest,
  AdmFileJobListParams,
  AdmFileJobResolveUnknownRequest,
  AdmFileJobRetryRequest,
  AdmFileJobRollbackRequest,
  AdmFileJobUploadParams,
  AdmGatewayFindApplyStatusParams,
  AdmGatewayFindBindingsParams,
  AdmGatewayFindConnectionTestsParams,
  AdmGatewayFindServerGroupsParams,
  AdmGatewayOperationsEventsParams,
  AdmGatewayOperationsStreamParams,
  AdmIncidentCreateIncidentRequest,
  AdmIncidentFindIncidentsParams,
  AdmIncidentFindMaintenanceParams,
  AdmIncidentFindPoliciesParams,
  AdmIncidentTransitionIncidentRequest,
  AdmIpAllowlistRequest,
  AdmLogExportRequest,
  AdmLogFindLogsParams,
  AdmLogPolicyAuditFindPolicyAuditsParams,
  AdmLogPolicyClearCacheParams,
  AdmLogPolicyDisableOverrideParams,
  AdmLogPolicyDisablePolicyParams,
  AdmLogPolicyDistributionStatusParams,
  AdmLogPolicyFindPoliciesParams,
  AdmLogPolicyFindTraceBoostHistoryParams,
  AdmLogPolicyFindTraceBoostRuntimeStateParams,
  AdmLogPolicyOverrideRequest,
  AdmLogPolicyRefreshCacheParams,
  AdmLogPolicyRequest,
  AdmLoginRequest,
  AdmMaintenanceExecuteActionRequest,
  AdmMaintenanceFindActionsParams,
  AdmMenuPermissionUpdateRequest,
  AdmMenuSaveRequest,
  AdmMessageDeleteMessageParams,
  AdmMfaOtpRequest,
  AdmNotificationCancelDeliveryParams,
  AdmNotificationDisableRuleParams,
  AdmNotificationFindDeliveryAttemptsParams,
  AdmNotificationFindDeliveryLogsParams,
  AdmNotificationFindDlqParams,
  AdmNotificationFindRulesParams,
  AdmNotificationRetryDeliveryParams,
  AdmNotificationRuleRequest,
  AdmNotificationTestSendRequest,
  AdmOpenApiRefreshParams,
  AdmOpenApiRefreshRequest,
  AdmOperatorContactUpdateRequest,
  AdmOperatorCreateRequest,
  AdmOperatorPasswordResetRequest,
  AdmOperatorRoleUpdateRequest,
  AdmOperatorStatusUpdateRequest,
  AdmOperatorValidatePasswordParams,
  AdmParameterReferenceSearchParams,
  AdmPasswordChangeRequest,
  AdmPermissionFindButtonsParams,
  AdmReliabilityActionRequest,
  AdmRemoteLogBundleDownloadRequest,
  AdmRemoteLogBundleDownloadTokenIssueRequest,
  AdmRemoteLogBundleJobCreateRequest,
  AdmRemoteLogBundleJobDownloadParams,
  AdmRemoteLogDownloadParams,
  AdmRemoteLogPreviewParams,
  AdmRemoteLogSearchParams,
  AdmResilienceDecisionRequest,
  AdmResiliencePolicyApproveParams,
  AdmResiliencePolicyApproveRequest,
  AdmResiliencePolicyRejectParams,
  AdmResiliencePolicyRejectRequest,
  AdmResiliencePolicyRequest,
  AdmResiliencePolicyRequestRequest,
  AdmResiliencePolicySearchParams,
  AdmResponseCodeCreateParams,
  AdmResponseCodeDeleteParams,
  AdmResponseCodeUpdateParams,
  AdmRoleSaveRequest,
  AdmRuntimeControlCancelChangeRequest,
  AdmRuntimeControlChangeGroupMemberRequest,
  AdmRuntimeControlCreateChangeRequest,
  AdmRuntimeControlDeleteGroupParams,
  AdmRuntimeControlFindStatusParams,
  AdmRuntimeControlPreviewChangeRequest,
  AdmRuntimeControlPreviewTargetsRequest,
  AdmRuntimeControlRollbackChangeRequest,
  AdmRuntimeControlSaveGroupRequest,
  AdmSecretFindMetadataParams,
  AdmSecretRotateRequest,
  AdmSecurityDisableMfaParams,
  AdmServiceRegistryChangeInstanceStateRequest,
  AdmServiceRegistryFindCallHistoryParams,
  AdmServiceRegistryFindCircuitStatesParams,
  AdmServiceRegistryFindEndpointsParams,
  AdmServiceRegistryFindHealthParams,
  AdmServiceRegistryFindInstancesParams,
  AdmServiceRegistryFindRoutingPoliciesParams,
  AdmServiceRegistryFindServicesParams,
  AdmSessionRevokeRequest,
  AdmStandardExecutionFindAllParams,
  AdmStatusUpdateRequest,
  AdmTraceBoostRequest,
  AdmTransactionGroupFindGroupsParams,
  AdmTransactionMetaFindPageParams,
  AdmTransactionMetaFindTransactionsParams,
  AdmTransactionMetaInactivateParams,
  AdmTransactionMetaScanParams,
  BindingStateCommand,
  CommonCodeRequest,
  CommonConfigRequest,
  CommonMessageRequest,
  CommonResponseCodeRequest,
  ConnectionTestCancel,
  ConnectionTestRequest,
  ConnectionTestRevalidation,
  CpfControllerSourceResponse,
  CpfLogLevel,
  CpfRuntimeActualState,
  CpfRuntimePayload,
  CpfRuntimeTargetSelector,
  CpfSensitiveDataAccessRequest,
  CpfTabularFormat,
  DecisionCommand,
  DeleteCommand,
  DownloadRequest,
  EndpointDefinition,
  FindAdmBatchJobInstanceLogsParams,
  FindAdmBrokerDlqParams,
  FindAdmBrokerInboxParams,
  FindAdmBrokerOutboxParams,
  FindAdmFileTransferHistoryParams,
  FindAdmIdempotencyRecordsParams,
  FindAdmUnknownResultsParams,
  GatewayBindingCommand,
  GetAdmBatchJobInstanceLogParams,
  IncidentActionRequest,
  InstanceCommand,
  InstanceDefinition,
  MaintenanceSaveRequest,
  PolicyCommand,
  PolicySaveRequest,
  RecoveryTarget,
  RequestCommand,
  ServerGroupCommand,
  ServiceDefinition,
  SignalRequest,
  TraceAdmByBusinessTransactionIdParams,
  TraceAdmByTraceIdParams,
  TraceAdmByTransactionIdParams,
  UnknownResolution
} from './model';
import { cpfOrvalRequest } from '../../shared/orval-mutator';
import type { CpfOrvalGeneratedRequestOptions } from '../../shared/orval-mutator';
type SecondParameter<T extends (...args: never) => unknown> = CpfOrvalGeneratedRequestOptions;


// CPF PRE-RUNTIME FALLBACK START admApprovalPolicies
export type admApprovalPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalPoliciesResponseSuccess = (admApprovalPoliciesResponse200) & {
  headers: Headers;
};

export type admApprovalPoliciesResponse = (admApprovalPoliciesResponseSuccess)

export const getAdmApprovalPoliciesUrl = () => `/adm/api/approvals/policies`;

export const admApprovalPolicies = async (params?: AdmApprovalPoliciesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalPoliciesResponse> => {
  return cpfOrvalRequest<admApprovalPoliciesResponse>(getAdmApprovalPoliciesUrl(), {
    ...options,
    method: 'GET',
    params: { actionType: params?.actionType },
  });
};

export const getAdmApprovalPoliciesQueryKey = (params?: MaybeRefOrGetter<AdmApprovalPoliciesParams>) => ["adm", "api", "approvals", "policies", toValue(params)] as const;

export const getAdmApprovalPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof admApprovalPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmApprovalPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmApprovalPoliciesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admApprovalPolicies>>> = ({ signal }) => admApprovalPolicies(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicies>>, TError, TData>;
};

export type AdmApprovalPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof admApprovalPolicies>>>;
export type AdmApprovalPoliciesQueryError = unknown;

export function useAdmApprovalPolicies<TData = Awaited<ReturnType<typeof admApprovalPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmApprovalPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmApprovalPoliciesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admApprovalPolicies


// CPF PRE-RUNTIME FALLBACK START admApprovalPolicySave
export type admApprovalPolicySaveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalPolicySaveResponseSuccess = (admApprovalPolicySaveResponse200) & {
  headers: Headers;
};

export type admApprovalPolicySaveResponse = (admApprovalPolicySaveResponseSuccess)

export const getAdmApprovalPolicySaveUrl = () => `/adm/api/approvals/policies`;

export const admApprovalPolicySave = async (data: PolicyCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalPolicySaveResponse> => {
  return cpfOrvalRequest<admApprovalPolicySaveResponse>(getAdmApprovalPolicySaveUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmApprovalPolicySaveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalPolicySave>>, TError, {data: PolicyCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admApprovalPolicySave>>, TError, {data: PolicyCommand}, TContext> => {
  const mutationKey = ['admApprovalPolicySave'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admApprovalPolicySave>>, {data: PolicyCommand}> = (props) => {
    const { data } = props;
    return admApprovalPolicySave(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmApprovalPolicySaveMutationResult = NonNullable<Awaited<ReturnType<typeof admApprovalPolicySave>>>;
export type AdmApprovalPolicySaveMutationBody = PolicyCommand;
export type AdmApprovalPolicySaveMutationError = unknown;

export const useAdmApprovalPolicySave = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalPolicySave>>, TError, {data: PolicyCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admApprovalPolicySave>>, TError, {data: PolicyCommand}, TContext> => {
  return useMutation(getAdmApprovalPolicySaveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admApprovalPolicySave


// CPF PRE-RUNTIME FALLBACK START admApprovalPolicyDetail
export type admApprovalPolicyDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalPolicyDetailResponseSuccess = (admApprovalPolicyDetailResponse200) & {
  headers: Headers;
};

export type admApprovalPolicyDetailResponse = (admApprovalPolicyDetailResponseSuccess)

export const getAdmApprovalPolicyDetailUrl = (policyCode: string, version: number) => `/adm/api/approvals/policies/${encodeURIComponent(String(policyCode))}/versions/${encodeURIComponent(String(version))}`;

export const admApprovalPolicyDetail = async (policyCode: string, version: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalPolicyDetailResponse> => {
  return cpfOrvalRequest<admApprovalPolicyDetailResponse>(getAdmApprovalPolicyDetailUrl(policyCode, version), {
    ...options,
    method: 'GET',

  });
};

export const getAdmApprovalPolicyDetailQueryKey = (policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>) => ["adm", "api", "approvals", "policies", policyCode, "versions", version] as const;

export const getAdmApprovalPolicyDetailQueryOptions = <TData = Awaited<ReturnType<typeof admApprovalPolicyDetail>>, TError = unknown>(
  policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicyDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmApprovalPolicyDetailQueryKey(toValue(policyCode), toValue(version));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admApprovalPolicyDetail>>> = ({ signal }) => admApprovalPolicyDetail(toValue(policyCode), toValue(version), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(policyCode) !== null && toValue(policyCode) !== undefined && toValue(version) !== null && toValue(version) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicyDetail>>, TError, TData>;
};

export type AdmApprovalPolicyDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admApprovalPolicyDetail>>>;
export type AdmApprovalPolicyDetailQueryError = unknown;

export function useAdmApprovalPolicyDetail<TData = Awaited<ReturnType<typeof admApprovalPolicyDetail>>, TError = unknown>(
  policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalPolicyDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmApprovalPolicyDetailQueryOptions(policyCode, version, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admApprovalPolicyDetail


// CPF PRE-RUNTIME FALLBACK START admApprovalRequest
export type admApprovalRequestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalRequestResponseSuccess = (admApprovalRequestResponse200) & {
  headers: Headers;
};

export type admApprovalRequestResponse = (admApprovalRequestResponseSuccess)

export const getAdmApprovalRequestUrl = () => `/adm/api/approvals/requests`;

export const admApprovalRequest = async (data: RequestCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalRequestResponse> => {
  return cpfOrvalRequest<admApprovalRequestResponse>(getAdmApprovalRequestUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmApprovalRequestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalRequest>>, TError, {data: RequestCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admApprovalRequest>>, TError, {data: RequestCommand}, TContext> => {
  const mutationKey = ['admApprovalRequest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admApprovalRequest>>, {data: RequestCommand}> = (props) => {
    const { data } = props;
    return admApprovalRequest(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmApprovalRequestMutationResult = NonNullable<Awaited<ReturnType<typeof admApprovalRequest>>>;
export type AdmApprovalRequestMutationBody = RequestCommand;
export type AdmApprovalRequestMutationError = unknown;

export const useAdmApprovalRequest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalRequest>>, TError, {data: RequestCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admApprovalRequest>>, TError, {data: RequestCommand}, TContext> => {
  return useMutation(getAdmApprovalRequestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admApprovalRequest


// CPF PRE-RUNTIME FALLBACK START admApprovalRequestDetail
export type admApprovalRequestDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalRequestDetailResponseSuccess = (admApprovalRequestDetailResponse200) & {
  headers: Headers;
};

export type admApprovalRequestDetailResponse = (admApprovalRequestDetailResponseSuccess)

export const getAdmApprovalRequestDetailUrl = (id: number) => `/adm/api/approvals/requests/${encodeURIComponent(String(id))}`;

export const admApprovalRequestDetail = async (id: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalRequestDetailResponse> => {
  return cpfOrvalRequest<admApprovalRequestDetailResponse>(getAdmApprovalRequestDetailUrl(id), {
    ...options,
    method: 'GET',

  });
};

export const getAdmApprovalRequestDetailQueryKey = (id: MaybeRefOrGetter<number>) => ["adm", "api", "approvals", "requests", id] as const;

export const getAdmApprovalRequestDetailQueryOptions = <TData = Awaited<ReturnType<typeof admApprovalRequestDetail>>, TError = unknown>(
  id: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalRequestDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmApprovalRequestDetailQueryKey(toValue(id));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admApprovalRequestDetail>>> = ({ signal }) => admApprovalRequestDetail(toValue(id), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(id) !== null && toValue(id) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admApprovalRequestDetail>>, TError, TData>;
};

export type AdmApprovalRequestDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admApprovalRequestDetail>>>;
export type AdmApprovalRequestDetailQueryError = unknown;

export function useAdmApprovalRequestDetail<TData = Awaited<ReturnType<typeof admApprovalRequestDetail>>, TError = unknown>(
  id: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admApprovalRequestDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmApprovalRequestDetailQueryOptions(id, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admApprovalRequestDetail


// CPF PRE-RUNTIME FALLBACK START admApprovalDecision
export type admApprovalDecisionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalDecisionResponseSuccess = (admApprovalDecisionResponse200) & {
  headers: Headers;
};

export type admApprovalDecisionResponse = (admApprovalDecisionResponseSuccess)

export const getAdmApprovalDecisionUrl = (id: number) => `/adm/api/approvals/requests/${encodeURIComponent(String(id))}/decisions`;

export const admApprovalDecision = async (id: number, data: DecisionCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalDecisionResponse> => {
  return cpfOrvalRequest<admApprovalDecisionResponse>(getAdmApprovalDecisionUrl(id), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmApprovalDecisionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalDecision>>, TError, {id: number; data: DecisionCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admApprovalDecision>>, TError, {id: number; data: DecisionCommand}, TContext> => {
  const mutationKey = ['admApprovalDecision'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admApprovalDecision>>, {id: number; data: DecisionCommand}> = (props) => {
    const { id, data } = props;
    return admApprovalDecision(id, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmApprovalDecisionMutationResult = NonNullable<Awaited<ReturnType<typeof admApprovalDecision>>>;
export type AdmApprovalDecisionMutationBody = DecisionCommand;
export type AdmApprovalDecisionMutationError = unknown;

export const useAdmApprovalDecision = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalDecision>>, TError, {id: number; data: DecisionCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admApprovalDecision>>, TError, {id: number; data: DecisionCommand}, TContext> => {
  return useMutation(getAdmApprovalDecisionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admApprovalDecision


// CPF PRE-RUNTIME FALLBACK START admApprovalExecute
export type admApprovalExecuteResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admApprovalExecuteResponseSuccess = (admApprovalExecuteResponse200) & {
  headers: Headers;
};

export type admApprovalExecuteResponse = (admApprovalExecuteResponseSuccess)

export const getAdmApprovalExecuteUrl = (id: number) => `/adm/api/approvals/requests/${encodeURIComponent(String(id))}/execute`;

export const admApprovalExecute = async (id: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admApprovalExecuteResponse> => {
  return cpfOrvalRequest<admApprovalExecuteResponse>(getAdmApprovalExecuteUrl(id), {
    ...options,
    method: 'POST',

  });
};

export const getAdmApprovalExecuteMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalExecute>>, TError, {id: number}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admApprovalExecute>>, TError, {id: number}, TContext> => {
  const mutationKey = ['admApprovalExecute'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admApprovalExecute>>, {id: number}> = (props) => {
    const { id } = props;
    return admApprovalExecute(id, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmApprovalExecuteMutationResult = NonNullable<Awaited<ReturnType<typeof admApprovalExecute>>>;
export type AdmApprovalExecuteMutationBody = never;
export type AdmApprovalExecuteMutationError = unknown;

export const useAdmApprovalExecute = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admApprovalExecute>>, TError, {id: number}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admApprovalExecute>>, TError, {id: number}, TContext> => {
  return useMutation(getAdmApprovalExecuteMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admApprovalExecute


// CPF PRE-RUNTIME FALLBACK START admAuditLogFindAuditLogs
export type admAuditLogFindAuditLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuditLogFindAuditLogsResponseSuccess = (admAuditLogFindAuditLogsResponse200) & {
  headers: Headers;
};

export type admAuditLogFindAuditLogsResponse = (admAuditLogFindAuditLogsResponseSuccess)

export const getAdmAuditLogFindAuditLogsUrl = () => `/adm/api/audit-logs`;

export const admAuditLogFindAuditLogs = async (params?: AdmAuditLogFindAuditLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuditLogFindAuditLogsResponse> => {
  return cpfOrvalRequest<admAuditLogFindAuditLogsResponse>(getAdmAuditLogFindAuditLogsUrl(), {
    ...options,
    method: 'GET',
    params: { actionType: params?.actionType, targetType: params?.targetType, targetId: params?.targetId, limit: params?.limit },
  });
};

export const getAdmAuditLogFindAuditLogsQueryKey = (params?: MaybeRefOrGetter<AdmAuditLogFindAuditLogsParams>) => ["adm", "api", "audit-logs", toValue(params)] as const;

export const getAdmAuditLogFindAuditLogsQueryOptions = <TData = Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmAuditLogFindAuditLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmAuditLogFindAuditLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>> = ({ signal }) => admAuditLogFindAuditLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>, TError, TData>;
};

export type AdmAuditLogFindAuditLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>>;
export type AdmAuditLogFindAuditLogsQueryError = unknown;

export function useAdmAuditLogFindAuditLogs<TData = Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmAuditLogFindAuditLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuditLogFindAuditLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmAuditLogFindAuditLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admAuditLogFindAuditLogs


// CPF PRE-RUNTIME FALLBACK START admAuditDeliveryList
export type admAuditDeliveryListResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuditDeliveryListResponseSuccess = (admAuditDeliveryListResponse200) & {
  headers: Headers;
};

export type admAuditDeliveryListResponse = (admAuditDeliveryListResponseSuccess)

export const getAdmAuditDeliveryListUrl = () => `/adm/api/audit-logs/deliveries`;

export const admAuditDeliveryList = async (params?: AdmAuditDeliveryListParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuditDeliveryListResponse> => {
  return cpfOrvalRequest<admAuditDeliveryListResponse>(getAdmAuditDeliveryListUrl(), {
    ...options,
    method: 'GET',
    params: { deliveryStatus: params?.deliveryStatus, limit: params?.limit },
  });
};

export const getAdmAuditDeliveryListQueryKey = (params?: MaybeRefOrGetter<AdmAuditDeliveryListParams>) => ["adm", "api", "audit-logs", "deliveries", toValue(params)] as const;

export const getAdmAuditDeliveryListQueryOptions = <TData = Awaited<ReturnType<typeof admAuditDeliveryList>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmAuditDeliveryListParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuditDeliveryList>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmAuditDeliveryListQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admAuditDeliveryList>>> = ({ signal }) => admAuditDeliveryList(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admAuditDeliveryList>>, TError, TData>;
};

export type AdmAuditDeliveryListQueryResult = NonNullable<Awaited<ReturnType<typeof admAuditDeliveryList>>>;
export type AdmAuditDeliveryListQueryError = unknown;

export function useAdmAuditDeliveryList<TData = Awaited<ReturnType<typeof admAuditDeliveryList>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmAuditDeliveryListParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuditDeliveryList>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmAuditDeliveryListQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admAuditDeliveryList


// CPF PRE-RUNTIME FALLBACK START admAuditDeliveryRetry
export type admAuditDeliveryRetryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuditDeliveryRetryResponseSuccess = (admAuditDeliveryRetryResponse200) & {
  headers: Headers;
};

export type admAuditDeliveryRetryResponse = (admAuditDeliveryRetryResponseSuccess)

export const getAdmAuditDeliveryRetryUrl = (deliveryId: number) => `/adm/api/audit-logs/deliveries/${encodeURIComponent(String(deliveryId))}/retry`;

export const admAuditDeliveryRetry = async (deliveryId: number, params: AdmAuditDeliveryRetryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuditDeliveryRetryResponse> => {
  return cpfOrvalRequest<admAuditDeliveryRetryResponse>(getAdmAuditDeliveryRetryUrl(deliveryId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmAuditDeliveryRetryMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuditDeliveryRetry>>, TError, {deliveryId: number; params: AdmAuditDeliveryRetryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admAuditDeliveryRetry>>, TError, {deliveryId: number; params: AdmAuditDeliveryRetryParams}, TContext> => {
  const mutationKey = ['admAuditDeliveryRetry'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admAuditDeliveryRetry>>, {deliveryId: number; params: AdmAuditDeliveryRetryParams}> = (props) => {
    const { deliveryId, params } = props;
    return admAuditDeliveryRetry(deliveryId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmAuditDeliveryRetryMutationResult = NonNullable<Awaited<ReturnType<typeof admAuditDeliveryRetry>>>;
export type AdmAuditDeliveryRetryMutationBody = never;
export type AdmAuditDeliveryRetryMutationError = unknown;

export const useAdmAuditDeliveryRetry = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuditDeliveryRetry>>, TError, {deliveryId: number; params: AdmAuditDeliveryRetryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admAuditDeliveryRetry>>, TError, {deliveryId: number; params: AdmAuditDeliveryRetryParams}, TContext> => {
  return useMutation(getAdmAuditDeliveryRetryMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admAuditDeliveryRetry


// CPF PRE-RUNTIME FALLBACK START admAuthLogin
export type admAuthLoginResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuthLoginResponseSuccess = (admAuthLoginResponse200) & {
  headers: Headers;
};

export type admAuthLoginResponse = (admAuthLoginResponseSuccess)

export const getAdmAuthLoginUrl = () => `/adm/api/auth/login`;

export const admAuthLogin = async (data: AdmLoginRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuthLoginResponse> => {
  return cpfOrvalRequest<admAuthLoginResponse>(getAdmAuthLoginUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmAuthLoginMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuthLogin>>, TError, {data: AdmLoginRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admAuthLogin>>, TError, {data: AdmLoginRequest}, TContext> => {
  const mutationKey = ['admAuthLogin'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admAuthLogin>>, {data: AdmLoginRequest}> = (props) => {
    const { data } = props;
    return admAuthLogin(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmAuthLoginMutationResult = NonNullable<Awaited<ReturnType<typeof admAuthLogin>>>;
export type AdmAuthLoginMutationBody = AdmLoginRequest;
export type AdmAuthLoginMutationError = unknown;

export const useAdmAuthLogin = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuthLogin>>, TError, {data: AdmLoginRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admAuthLogin>>, TError, {data: AdmLoginRequest}, TContext> => {
  return useMutation(getAdmAuthLoginMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admAuthLogin


// CPF PRE-RUNTIME FALLBACK START admAuthLogout
export type admAuthLogoutResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuthLogoutResponseSuccess = (admAuthLogoutResponse200) & {
  headers: Headers;
};

export type admAuthLogoutResponse = (admAuthLogoutResponseSuccess)

export const getAdmAuthLogoutUrl = () => `/adm/api/auth/logout`;

export const admAuthLogout = async (params: AdmAuthLogoutParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuthLogoutResponse> => {
  return cpfOrvalRequest<admAuthLogoutResponse>(getAdmAuthLogoutUrl(), {
    ...options,
    method: 'POST',
    headers: { "authorization": params.authorization, ...options?.headers },
  });
};

export const getAdmAuthLogoutMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuthLogout>>, TError, {params: AdmAuthLogoutParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admAuthLogout>>, TError, {params: AdmAuthLogoutParams}, TContext> => {
  const mutationKey = ['admAuthLogout'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admAuthLogout>>, {params: AdmAuthLogoutParams}> = (props) => {
    const { params } = props;
    return admAuthLogout(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmAuthLogoutMutationResult = NonNullable<Awaited<ReturnType<typeof admAuthLogout>>>;
export type AdmAuthLogoutMutationBody = never;
export type AdmAuthLogoutMutationError = unknown;

export const useAdmAuthLogout = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admAuthLogout>>, TError, {params: AdmAuthLogoutParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admAuthLogout>>, TError, {params: AdmAuthLogoutParams}, TContext> => {
  return useMutation(getAdmAuthLogoutMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admAuthLogout


// CPF PRE-RUNTIME FALLBACK START admAuthMe
export type admAuthMeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admAuthMeResponseSuccess = (admAuthMeResponse200) & {
  headers: Headers;
};

export type admAuthMeResponse = (admAuthMeResponseSuccess)

export const getAdmAuthMeUrl = () => `/adm/api/auth/me`;

export const admAuthMe = async (params: AdmAuthMeParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admAuthMeResponse> => {
  return cpfOrvalRequest<admAuthMeResponse>(getAdmAuthMeUrl(), {
    ...options,
    method: 'GET',
    headers: { "authorization": params.authorization, ...options?.headers },
  });
};

export const getAdmAuthMeQueryKey = (params: MaybeRefOrGetter<AdmAuthMeParams>) => ["adm", "api", "auth", "me", toValue(params)] as const;

export const getAdmAuthMeQueryOptions = <TData = Awaited<ReturnType<typeof admAuthMe>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmAuthMeParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuthMe>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmAuthMeQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admAuthMe>>> = ({ signal }) => admAuthMe(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admAuthMe>>, TError, TData>;
};

export type AdmAuthMeQueryResult = NonNullable<Awaited<ReturnType<typeof admAuthMe>>>;
export type AdmAuthMeQueryError = unknown;

export function useAdmAuthMe<TData = Awaited<ReturnType<typeof admAuthMe>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmAuthMeParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admAuthMe>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmAuthMeQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admAuthMe


// CPF PRE-RUNTIME FALLBACK START admBatchRuntimeCommand
export type admBatchRuntimeCommandResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRuntimeCommandResponseSuccess = (admBatchRuntimeCommandResponse200) & {
  headers: Headers;
};

export type admBatchRuntimeCommandResponse = (admBatchRuntimeCommandResponseSuccess)

export const getAdmBatchRuntimeCommandUrl = () => `/adm/api/batch-runtime/commands`;

export const admBatchRuntimeCommand = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRuntimeCommandResponse> => {
  return cpfOrvalRequest<admBatchRuntimeCommandResponse>(getAdmBatchRuntimeCommandUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getAdmBatchRuntimeCommandMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCommand>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCommand>>, TError, void, TContext> => {
  const mutationKey = ['admBatchRuntimeCommand'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRuntimeCommand>>, void> = () => {

    return admBatchRuntimeCommand(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRuntimeCommandMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRuntimeCommand>>>;
export type AdmBatchRuntimeCommandMutationBody = never;
export type AdmBatchRuntimeCommandMutationError = unknown;

export const useAdmBatchRuntimeCommand = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCommand>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRuntimeCommand>>, TError, void, TContext> => {
  return useMutation(getAdmBatchRuntimeCommandMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRuntimeCommand


// CPF PRE-RUNTIME FALLBACK START admBatchRuntimeCommandState
export type admBatchRuntimeCommandStateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRuntimeCommandStateResponseSuccess = (admBatchRuntimeCommandStateResponse200) & {
  headers: Headers;
};

export type admBatchRuntimeCommandStateResponse = (admBatchRuntimeCommandStateResponseSuccess)

export const getAdmBatchRuntimeCommandStateUrl = (key: string) => `/adm/api/batch-runtime/commands/${encodeURIComponent(String(key))}`;

export const admBatchRuntimeCommandState = async (key: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRuntimeCommandStateResponse> => {
  return cpfOrvalRequest<admBatchRuntimeCommandStateResponse>(getAdmBatchRuntimeCommandStateUrl(key), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchRuntimeCommandStateQueryKey = (key: MaybeRefOrGetter<string>) => ["adm", "api", "batch-runtime", "commands", key] as const;

export const getAdmBatchRuntimeCommandStateQueryOptions = <TData = Awaited<ReturnType<typeof admBatchRuntimeCommandState>>, TError = unknown>(
  key: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeCommandState>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchRuntimeCommandStateQueryKey(toValue(key));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchRuntimeCommandState>>> = ({ signal }) => admBatchRuntimeCommandState(toValue(key), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(key) !== null && toValue(key) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeCommandState>>, TError, TData>;
};

export type AdmBatchRuntimeCommandStateQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchRuntimeCommandState>>>;
export type AdmBatchRuntimeCommandStateQueryError = unknown;

export function useAdmBatchRuntimeCommandState<TData = Awaited<ReturnType<typeof admBatchRuntimeCommandState>>, TError = unknown>(
  key: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeCommandState>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchRuntimeCommandStateQueryOptions(key, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchRuntimeCommandState


// CPF PRE-RUNTIME FALLBACK START admBatchRuntimeCreateDeploymentPlan
export type admBatchRuntimeCreateDeploymentPlanResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRuntimeCreateDeploymentPlanResponseSuccess = (admBatchRuntimeCreateDeploymentPlanResponse200) & {
  headers: Headers;
};

export type admBatchRuntimeCreateDeploymentPlanResponse = (admBatchRuntimeCreateDeploymentPlanResponseSuccess)

export const getAdmBatchRuntimeCreateDeploymentPlanUrl = () => `/adm/api/batch-runtime/deployment-plans`;

export const admBatchRuntimeCreateDeploymentPlan = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRuntimeCreateDeploymentPlanResponse> => {
  return cpfOrvalRequest<admBatchRuntimeCreateDeploymentPlanResponse>(getAdmBatchRuntimeCreateDeploymentPlanUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getAdmBatchRuntimeCreateDeploymentPlanMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>, TError, void, TContext> => {
  const mutationKey = ['admBatchRuntimeCreateDeploymentPlan'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>, void> = () => {

    return admBatchRuntimeCreateDeploymentPlan(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRuntimeCreateDeploymentPlanMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>>;
export type AdmBatchRuntimeCreateDeploymentPlanMutationBody = never;
export type AdmBatchRuntimeCreateDeploymentPlanMutationError = unknown;

export const useAdmBatchRuntimeCreateDeploymentPlan = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRuntimeCreateDeploymentPlan>>, TError, void, TContext> => {
  return useMutation(getAdmBatchRuntimeCreateDeploymentPlanMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRuntimeCreateDeploymentPlan


// CPF PRE-RUNTIME FALLBACK START admBatchRuntimeInstances
export type admBatchRuntimeInstancesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRuntimeInstancesResponseSuccess = (admBatchRuntimeInstancesResponse200) & {
  headers: Headers;
};

export type admBatchRuntimeInstancesResponse = (admBatchRuntimeInstancesResponseSuccess)

export const getAdmBatchRuntimeInstancesUrl = () => `/adm/api/batch-runtime/instances`;

export const admBatchRuntimeInstances = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRuntimeInstancesResponse> => {
  return cpfOrvalRequest<admBatchRuntimeInstancesResponse>(getAdmBatchRuntimeInstancesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchRuntimeInstancesQueryKey = () => ["adm", "api", "batch-runtime", "instances"] as const;

export const getAdmBatchRuntimeInstancesQueryOptions = <TData = Awaited<ReturnType<typeof admBatchRuntimeInstances>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchRuntimeInstancesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchRuntimeInstances>>> = ({ signal }) => admBatchRuntimeInstances({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeInstances>>, TError, TData>;
};

export type AdmBatchRuntimeInstancesQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchRuntimeInstances>>>;
export type AdmBatchRuntimeInstancesQueryError = unknown;

export function useAdmBatchRuntimeInstances<TData = Awaited<ReturnType<typeof admBatchRuntimeInstances>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchRuntimeInstancesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchRuntimeInstances


// CPF PRE-RUNTIME FALLBACK START admBatchJobDefinitions
export type admBatchJobDefinitionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchJobDefinitionsResponseSuccess = (admBatchJobDefinitionsResponse200) & {
  headers: Headers;
};

export type admBatchJobDefinitionsResponse = (admBatchJobDefinitionsResponseSuccess)

export const getAdmBatchJobDefinitionsUrl = () => `/adm/api/batch-runtime/job-definitions`;

export const admBatchJobDefinitions = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchJobDefinitionsResponse> => {
  return cpfOrvalRequest<admBatchJobDefinitionsResponse>(getAdmBatchJobDefinitionsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchJobDefinitionsQueryKey = () => ["adm", "api", "batch-runtime", "job-definitions"] as const;

export const getAdmBatchJobDefinitionsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchJobDefinitions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchJobDefinitionsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchJobDefinitions>>> = ({ signal }) => admBatchJobDefinitions({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitions>>, TError, TData>;
};

export type AdmBatchJobDefinitionsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchJobDefinitions>>>;
export type AdmBatchJobDefinitionsQueryError = unknown;

export function useAdmBatchJobDefinitions<TData = Awaited<ReturnType<typeof admBatchJobDefinitions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchJobDefinitionsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchJobDefinitions


// CPF PRE-RUNTIME FALLBACK START admBatchJobDefinitionSave
export type admBatchJobDefinitionSaveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchJobDefinitionSaveResponseSuccess = (admBatchJobDefinitionSaveResponse200) & {
  headers: Headers;
};

export type admBatchJobDefinitionSaveResponse = (admBatchJobDefinitionSaveResponseSuccess)

export const getAdmBatchJobDefinitionSaveUrl = () => `/adm/api/batch-runtime/job-definitions/drafts`;

export const admBatchJobDefinitionSave = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchJobDefinitionSaveResponse> => {
  return cpfOrvalRequest<admBatchJobDefinitionSaveResponse>(getAdmBatchJobDefinitionSaveUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getAdmBatchJobDefinitionSaveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>, TError, void, TContext> => {
  const mutationKey = ['admBatchJobDefinitionSave'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>, void> = () => {

    return admBatchJobDefinitionSave(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchJobDefinitionSaveMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>>;
export type AdmBatchJobDefinitionSaveMutationBody = never;
export type AdmBatchJobDefinitionSaveMutationError = unknown;

export const useAdmBatchJobDefinitionSave = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchJobDefinitionSave>>, TError, void, TContext> => {
  return useMutation(getAdmBatchJobDefinitionSaveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchJobDefinitionSave


// CPF PRE-RUNTIME FALLBACK START admBatchJobDefinitionValidate
export type admBatchJobDefinitionValidateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchJobDefinitionValidateResponseSuccess = (admBatchJobDefinitionValidateResponse200) & {
  headers: Headers;
};

export type admBatchJobDefinitionValidateResponse = (admBatchJobDefinitionValidateResponseSuccess)

export const getAdmBatchJobDefinitionValidateUrl = () => `/adm/api/batch-runtime/job-definitions/validate`;

export const admBatchJobDefinitionValidate = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchJobDefinitionValidateResponse> => {
  return cpfOrvalRequest<admBatchJobDefinitionValidateResponse>(getAdmBatchJobDefinitionValidateUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getAdmBatchJobDefinitionValidateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>, TError, void, TContext> => {
  const mutationKey = ['admBatchJobDefinitionValidate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>, void> = () => {

    return admBatchJobDefinitionValidate(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchJobDefinitionValidateMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>>;
export type AdmBatchJobDefinitionValidateMutationBody = never;
export type AdmBatchJobDefinitionValidateMutationError = unknown;

export const useAdmBatchJobDefinitionValidate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchJobDefinitionValidate>>, TError, void, TContext> => {
  return useMutation(getAdmBatchJobDefinitionValidateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchJobDefinitionValidate


// CPF PRE-RUNTIME FALLBACK START admBatchJobDefinitionDetail
export type admBatchJobDefinitionDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchJobDefinitionDetailResponseSuccess = (admBatchJobDefinitionDetailResponse200) & {
  headers: Headers;
};

export type admBatchJobDefinitionDetailResponse = (admBatchJobDefinitionDetailResponseSuccess)

export const getAdmBatchJobDefinitionDetailUrl = (jobId: string, version: string) => `/adm/api/batch-runtime/job-definitions/${encodeURIComponent(String(jobId))}/versions/${encodeURIComponent(String(version))}`;

export const admBatchJobDefinitionDetail = async (jobId: string, version: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchJobDefinitionDetailResponse> => {
  return cpfOrvalRequest<admBatchJobDefinitionDetailResponse>(getAdmBatchJobDefinitionDetailUrl(jobId, version), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchJobDefinitionDetailQueryKey = (jobId: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<string>) => ["adm", "api", "batch-runtime", "job-definitions", jobId, "versions", version] as const;

export const getAdmBatchJobDefinitionDetailQueryOptions = <TData = Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchJobDefinitionDetailQueryKey(toValue(jobId), toValue(version));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>> = ({ signal }) => admBatchJobDefinitionDetail(toValue(jobId), toValue(version), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined && toValue(version) !== null && toValue(version) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>, TError, TData>;
};

export type AdmBatchJobDefinitionDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>>;
export type AdmBatchJobDefinitionDetailQueryError = unknown;

export function useAdmBatchJobDefinitionDetail<TData = Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchJobDefinitionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchJobDefinitionDetailQueryOptions(jobId, version, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchJobDefinitionDetail


// CPF PRE-RUNTIME FALLBACK START admBatchJobDefinitionTransition
export type admBatchJobDefinitionTransitionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchJobDefinitionTransitionResponseSuccess = (admBatchJobDefinitionTransitionResponse200) & {
  headers: Headers;
};

export type admBatchJobDefinitionTransitionResponse = (admBatchJobDefinitionTransitionResponseSuccess)

export const getAdmBatchJobDefinitionTransitionUrl = (jobId: string, version: string) => `/adm/api/batch-runtime/job-definitions/${encodeURIComponent(String(jobId))}/versions/${encodeURIComponent(String(version))}/transition`;

export const admBatchJobDefinitionTransition = async (jobId: string, version: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchJobDefinitionTransitionResponse> => {
  return cpfOrvalRequest<admBatchJobDefinitionTransitionResponse>(getAdmBatchJobDefinitionTransitionUrl(jobId, version), {
    ...options,
    method: 'POST',

  });
};

export const getAdmBatchJobDefinitionTransitionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>, TError, {jobId: string; version: string}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>, TError, {jobId: string; version: string}, TContext> => {
  const mutationKey = ['admBatchJobDefinitionTransition'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>, {jobId: string; version: string}> = (props) => {
    const { jobId, version } = props;
    return admBatchJobDefinitionTransition(jobId, version, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchJobDefinitionTransitionMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>>;
export type AdmBatchJobDefinitionTransitionMutationBody = never;
export type AdmBatchJobDefinitionTransitionMutationError = unknown;

export const useAdmBatchJobDefinitionTransition = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>, TError, {jobId: string; version: string}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchJobDefinitionTransition>>, TError, {jobId: string; version: string}, TContext> => {
  return useMutation(getAdmBatchJobDefinitionTransitionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchJobDefinitionTransition


// CPF PRE-RUNTIME FALLBACK START admBatchRuntimeView
export type admBatchRuntimeViewResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRuntimeViewResponseSuccess = (admBatchRuntimeViewResponse200) & {
  headers: Headers;
};

export type admBatchRuntimeViewResponse = (admBatchRuntimeViewResponseSuccess)

export const getAdmBatchRuntimeViewUrl = (view: string) => `/adm/api/batch-runtime/views/${encodeURIComponent(String(view))}`;

export const admBatchRuntimeView = async (view: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRuntimeViewResponse> => {
  return cpfOrvalRequest<admBatchRuntimeViewResponse>(getAdmBatchRuntimeViewUrl(view), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchRuntimeViewQueryKey = (view: MaybeRefOrGetter<string>) => ["adm", "api", "batch-runtime", "views", view] as const;

export const getAdmBatchRuntimeViewQueryOptions = <TData = Awaited<ReturnType<typeof admBatchRuntimeView>>, TError = unknown>(
  view: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeView>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchRuntimeViewQueryKey(toValue(view));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchRuntimeView>>> = ({ signal }) => admBatchRuntimeView(toValue(view), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(view) !== null && toValue(view) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeView>>, TError, TData>;
};

export type AdmBatchRuntimeViewQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchRuntimeView>>>;
export type AdmBatchRuntimeViewQueryError = unknown;

export function useAdmBatchRuntimeView<TData = Awaited<ReturnType<typeof admBatchRuntimeView>>, TError = unknown>(
  view: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchRuntimeView>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchRuntimeViewQueryOptions(view, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchRuntimeView


// CPF PRE-RUNTIME FALLBACK START admBatchFindExecutionTargets
export type admBatchFindExecutionTargetsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindExecutionTargetsResponseSuccess = (admBatchFindExecutionTargetsResponse200) & {
  headers: Headers;
};

export type admBatchFindExecutionTargetsResponse = (admBatchFindExecutionTargetsResponseSuccess)

export const getAdmBatchFindExecutionTargetsUrl = () => `/adm/api/batch/execution-targets`;

export const admBatchFindExecutionTargets = async (params?: AdmBatchFindExecutionTargetsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindExecutionTargetsResponse> => {
  return cpfOrvalRequest<admBatchFindExecutionTargetsResponse>(getAdmBatchFindExecutionTargetsUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId, dispatchStatus: params?.dispatchStatus, limit: params?.limit },
  });
};

export const getAdmBatchFindExecutionTargetsQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindExecutionTargetsParams>) => ["adm", "api", "batch", "execution-targets", toValue(params)] as const;

export const getAdmBatchFindExecutionTargetsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindExecutionTargets>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionTargetsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionTargets>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindExecutionTargetsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindExecutionTargets>>> = ({ signal }) => admBatchFindExecutionTargets(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionTargets>>, TError, TData>;
};

export type AdmBatchFindExecutionTargetsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindExecutionTargets>>>;
export type AdmBatchFindExecutionTargetsQueryError = unknown;

export function useAdmBatchFindExecutionTargets<TData = Awaited<ReturnType<typeof admBatchFindExecutionTargets>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionTargetsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionTargets>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindExecutionTargetsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindExecutionTargets


// CPF PRE-RUNTIME FALLBACK START admBatchFindExecutions
export type admBatchFindExecutionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindExecutionsResponseSuccess = (admBatchFindExecutionsResponse200) & {
  headers: Headers;
};

export type admBatchFindExecutionsResponse = (admBatchFindExecutionsResponseSuccess)

export const getAdmBatchFindExecutionsUrl = () => `/adm/api/batch/executions`;

export const admBatchFindExecutions = async (params?: AdmBatchFindExecutionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindExecutionsResponse> => {
  return cpfOrvalRequest<admBatchFindExecutionsResponse>(getAdmBatchFindExecutionsUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId, transactionId: params?.transactionId, springBatchJobInstanceId: params?.springBatchJobInstanceId, workerId: params?.workerId, serverInstanceId: params?.serverInstanceId, limit: params?.limit },
  });
};

export const getAdmBatchFindExecutionsQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindExecutionsParams>) => ["adm", "api", "batch", "executions", toValue(params)] as const;

export const getAdmBatchFindExecutionsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindExecutionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindExecutions>>> = ({ signal }) => admBatchFindExecutions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutions>>, TError, TData>;
};

export type AdmBatchFindExecutionsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindExecutions>>>;
export type AdmBatchFindExecutionsQueryError = unknown;

export function useAdmBatchFindExecutions<TData = Awaited<ReturnType<typeof admBatchFindExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindExecutionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindExecutions


// CPF PRE-RUNTIME FALLBACK START admBatchFindExecutionPage
export type admBatchFindExecutionPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindExecutionPageResponseSuccess = (admBatchFindExecutionPageResponse200) & {
  headers: Headers;
};

export type admBatchFindExecutionPageResponse = (admBatchFindExecutionPageResponseSuccess)

export const getAdmBatchFindExecutionPageUrl = () => `/adm/api/batch/executions/page`;

export const admBatchFindExecutionPage = async (params?: AdmBatchFindExecutionPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindExecutionPageResponse> => {
  return cpfOrvalRequest<admBatchFindExecutionPageResponse>(getAdmBatchFindExecutionPageUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId, transactionId: params?.transactionId, springBatchJobInstanceId: params?.springBatchJobInstanceId, workerId: params?.workerId, serverInstanceId: params?.serverInstanceId, status: params?.status, fromDate: params?.fromDate, toDate: params?.toDate, page: params?.page, size: params?.size },
  });
};

export const getAdmBatchFindExecutionPageQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindExecutionPageParams>) => ["adm", "api", "batch", "executions", "page", toValue(params)] as const;

export const getAdmBatchFindExecutionPageQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindExecutionPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindExecutionPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindExecutionPage>>> = ({ signal }) => admBatchFindExecutionPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionPage>>, TError, TData>;
};

export type AdmBatchFindExecutionPageQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindExecutionPage>>>;
export type AdmBatchFindExecutionPageQueryError = unknown;

export function useAdmBatchFindExecutionPage<TData = Awaited<ReturnType<typeof admBatchFindExecutionPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindExecutionPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindExecutionPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindExecutionPage


// CPF PRE-RUNTIME FALLBACK START admBatchFindExecutionDetail
export type admBatchFindExecutionDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindExecutionDetailResponseSuccess = (admBatchFindExecutionDetailResponse200) & {
  headers: Headers;
};

export type admBatchFindExecutionDetailResponse = (admBatchFindExecutionDetailResponseSuccess)

export const getAdmBatchFindExecutionDetailUrl = (executionId: number) => `/adm/api/batch/executions/${encodeURIComponent(String(executionId))}`;

export const admBatchFindExecutionDetail = async (executionId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindExecutionDetailResponse> => {
  return cpfOrvalRequest<admBatchFindExecutionDetailResponse>(getAdmBatchFindExecutionDetailUrl(executionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchFindExecutionDetailQueryKey = (executionId: MaybeRefOrGetter<number>) => ["adm", "api", "batch", "executions", executionId] as const;

export const getAdmBatchFindExecutionDetailQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindExecutionDetail>>, TError = unknown>(
  executionId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindExecutionDetailQueryKey(toValue(executionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindExecutionDetail>>> = ({ signal }) => admBatchFindExecutionDetail(toValue(executionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(executionId) !== null && toValue(executionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionDetail>>, TError, TData>;
};

export type AdmBatchFindExecutionDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindExecutionDetail>>>;
export type AdmBatchFindExecutionDetailQueryError = unknown;

export function useAdmBatchFindExecutionDetail<TData = Awaited<ReturnType<typeof admBatchFindExecutionDetail>>, TError = unknown>(
  executionId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindExecutionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindExecutionDetailQueryOptions(executionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindExecutionDetail


// CPF PRE-RUNTIME FALLBACK START admBatchRetryExecution
export type admBatchRetryExecutionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRetryExecutionResponseSuccess = (admBatchRetryExecutionResponse200) & {
  headers: Headers;
};

export type admBatchRetryExecutionResponse = (admBatchRetryExecutionResponseSuccess)

export const getAdmBatchRetryExecutionUrl = (executionId: number) => `/adm/api/batch/executions/${encodeURIComponent(String(executionId))}/retry`;

export const admBatchRetryExecution = async (executionId: number, data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRetryExecutionResponse> => {
  return cpfOrvalRequest<admBatchRetryExecutionResponse>(getAdmBatchRetryExecutionUrl(executionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchRetryExecutionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRetryExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRetryExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchRetryExecution'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRetryExecution>>, {executionId: number; data: AdmBatchOperationRequest}> = (props) => {
    const { executionId, data } = props;
    return admBatchRetryExecution(executionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRetryExecutionMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRetryExecution>>>;
export type AdmBatchRetryExecutionMutationBody = AdmBatchOperationRequest;
export type AdmBatchRetryExecutionMutationError = unknown;

export const useAdmBatchRetryExecution = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRetryExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRetryExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchRetryExecutionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRetryExecution


// CPF PRE-RUNTIME FALLBACK START admBatchStopExecution
export type admBatchStopExecutionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchStopExecutionResponseSuccess = (admBatchStopExecutionResponse200) & {
  headers: Headers;
};

export type admBatchStopExecutionResponse = (admBatchStopExecutionResponseSuccess)

export const getAdmBatchStopExecutionUrl = (executionId: number) => `/adm/api/batch/executions/${encodeURIComponent(String(executionId))}/stop`;

export const admBatchStopExecution = async (executionId: number, data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchStopExecutionResponse> => {
  return cpfOrvalRequest<admBatchStopExecutionResponse>(getAdmBatchStopExecutionUrl(executionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchStopExecutionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchStopExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchStopExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchStopExecution'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchStopExecution>>, {executionId: number; data: AdmBatchOperationRequest}> = (props) => {
    const { executionId, data } = props;
    return admBatchStopExecution(executionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchStopExecutionMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchStopExecution>>>;
export type AdmBatchStopExecutionMutationBody = AdmBatchOperationRequest;
export type AdmBatchStopExecutionMutationError = unknown;

export const useAdmBatchStopExecution = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchStopExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchStopExecution>>, TError, {executionId: number; data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchStopExecutionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchStopExecution


// CPF PRE-RUNTIME FALLBACK START admBatchFindGhostCandidates
export type admBatchFindGhostCandidatesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindGhostCandidatesResponseSuccess = (admBatchFindGhostCandidatesResponse200) & {
  headers: Headers;
};

export type admBatchFindGhostCandidatesResponse = (admBatchFindGhostCandidatesResponseSuccess)

export const getAdmBatchFindGhostCandidatesUrl = () => `/adm/api/batch/ghost-candidates`;

export const admBatchFindGhostCandidates = async (params?: AdmBatchFindGhostCandidatesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindGhostCandidatesResponse> => {
  return cpfOrvalRequest<admBatchFindGhostCandidatesResponse>(getAdmBatchFindGhostCandidatesUrl(), {
    ...options,
    method: 'GET',
    params: { heartbeatTimeoutSeconds: params?.heartbeatTimeoutSeconds },
  });
};

export const getAdmBatchFindGhostCandidatesQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindGhostCandidatesParams>) => ["adm", "api", "batch", "ghost-candidates", toValue(params)] as const;

export const getAdmBatchFindGhostCandidatesQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindGhostCandidates>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindGhostCandidatesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindGhostCandidates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindGhostCandidatesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindGhostCandidates>>> = ({ signal }) => admBatchFindGhostCandidates(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindGhostCandidates>>, TError, TData>;
};

export type AdmBatchFindGhostCandidatesQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindGhostCandidates>>>;
export type AdmBatchFindGhostCandidatesQueryError = unknown;

export function useAdmBatchFindGhostCandidates<TData = Awaited<ReturnType<typeof admBatchFindGhostCandidates>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindGhostCandidatesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindGhostCandidates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindGhostCandidatesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindGhostCandidates


// CPF PRE-RUNTIME FALLBACK START admBatchActGhostExecution
export type admBatchActGhostExecutionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchActGhostExecutionResponseSuccess = (admBatchActGhostExecutionResponse200) & {
  headers: Headers;
};

export type admBatchActGhostExecutionResponse = (admBatchActGhostExecutionResponseSuccess)

export const getAdmBatchActGhostExecutionUrl = (executionId: number) => `/adm/api/batch/ghost-candidates/${encodeURIComponent(String(executionId))}/actions`;

export const admBatchActGhostExecution = async (executionId: number, data: AdmBatchGhostActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchActGhostExecutionResponse> => {
  return cpfOrvalRequest<admBatchActGhostExecutionResponse>(getAdmBatchActGhostExecutionUrl(executionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchActGhostExecutionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchActGhostExecution>>, TError, {executionId: number; data: AdmBatchGhostActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchActGhostExecution>>, TError, {executionId: number; data: AdmBatchGhostActionRequest}, TContext> => {
  const mutationKey = ['admBatchActGhostExecution'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchActGhostExecution>>, {executionId: number; data: AdmBatchGhostActionRequest}> = (props) => {
    const { executionId, data } = props;
    return admBatchActGhostExecution(executionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchActGhostExecutionMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchActGhostExecution>>>;
export type AdmBatchActGhostExecutionMutationBody = AdmBatchGhostActionRequest;
export type AdmBatchActGhostExecutionMutationError = unknown;

export const useAdmBatchActGhostExecution = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchActGhostExecution>>, TError, {executionId: number; data: AdmBatchGhostActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchActGhostExecution>>, TError, {executionId: number; data: AdmBatchGhostActionRequest}, TContext> => {
  return useMutation(getAdmBatchActGhostExecutionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchActGhostExecution


// CPF PRE-RUNTIME FALLBACK START admBatchFindInstances
export type admBatchFindInstancesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindInstancesResponseSuccess = (admBatchFindInstancesResponse200) & {
  headers: Headers;
};

export type admBatchFindInstancesResponse = (admBatchFindInstancesResponseSuccess)

export const getAdmBatchFindInstancesUrl = () => `/adm/api/batch/instances`;

export const admBatchFindInstances = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindInstancesResponse> => {
  return cpfOrvalRequest<admBatchFindInstancesResponse>(getAdmBatchFindInstancesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchFindInstancesQueryKey = () => ["adm", "api", "batch", "instances"] as const;

export const getAdmBatchFindInstancesQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindInstances>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindInstancesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindInstances>>> = ({ signal }) => admBatchFindInstances({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindInstances>>, TError, TData>;
};

export type AdmBatchFindInstancesQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindInstances>>>;
export type AdmBatchFindInstancesQueryError = unknown;

export function useAdmBatchFindInstances<TData = Awaited<ReturnType<typeof admBatchFindInstances>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindInstancesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindInstances


// CPF PRE-RUNTIME FALLBACK START admBatchFindJobs
export type admBatchFindJobsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindJobsResponseSuccess = (admBatchFindJobsResponse200) & {
  headers: Headers;
};

export type admBatchFindJobsResponse = (admBatchFindJobsResponseSuccess)

export const getAdmBatchFindJobsUrl = () => `/adm/api/batch/jobs`;

export const admBatchFindJobs = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindJobsResponse> => {
  return cpfOrvalRequest<admBatchFindJobsResponse>(getAdmBatchFindJobsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchFindJobsQueryKey = () => ["adm", "api", "batch", "jobs"] as const;

export const getAdmBatchFindJobsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindJobs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindJobsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindJobs>>> = ({ signal }) => admBatchFindJobs({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobs>>, TError, TData>;
};

export type AdmBatchFindJobsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindJobs>>>;
export type AdmBatchFindJobsQueryError = unknown;

export function useAdmBatchFindJobs<TData = Awaited<ReturnType<typeof admBatchFindJobs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindJobsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindJobs


// CPF PRE-RUNTIME FALLBACK START admBatchRegisterJob
export type admBatchRegisterJobResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRegisterJobResponseSuccess = (admBatchRegisterJobResponse200) & {
  headers: Headers;
};

export type admBatchRegisterJobResponse = (admBatchRegisterJobResponseSuccess)

export const getAdmBatchRegisterJobUrl = () => `/adm/api/batch/jobs`;

export const admBatchRegisterJob = async (data: AdmBatchJobRegisterRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRegisterJobResponse> => {
  return cpfOrvalRequest<admBatchRegisterJobResponse>(getAdmBatchRegisterJobUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchRegisterJobMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRegisterJob>>, TError, {data: AdmBatchJobRegisterRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRegisterJob>>, TError, {data: AdmBatchJobRegisterRequest}, TContext> => {
  const mutationKey = ['admBatchRegisterJob'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRegisterJob>>, {data: AdmBatchJobRegisterRequest}> = (props) => {
    const { data } = props;
    return admBatchRegisterJob(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRegisterJobMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRegisterJob>>>;
export type AdmBatchRegisterJobMutationBody = AdmBatchJobRegisterRequest;
export type AdmBatchRegisterJobMutationError = unknown;

export const useAdmBatchRegisterJob = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRegisterJob>>, TError, {data: AdmBatchJobRegisterRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRegisterJob>>, TError, {data: AdmBatchJobRegisterRequest}, TContext> => {
  return useMutation(getAdmBatchRegisterJobMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRegisterJob


// CPF PRE-RUNTIME FALLBACK START admBatchFindJobDetail
export type admBatchFindJobDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindJobDetailResponseSuccess = (admBatchFindJobDetailResponse200) & {
  headers: Headers;
};

export type admBatchFindJobDetailResponse = (admBatchFindJobDetailResponseSuccess)

export const getAdmBatchFindJobDetailUrl = (jobId: string) => `/adm/api/batch/jobs/${encodeURIComponent(String(jobId))}`;

export const admBatchFindJobDetail = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindJobDetailResponse> => {
  return cpfOrvalRequest<admBatchFindJobDetailResponse>(getAdmBatchFindJobDetailUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchFindJobDetailQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "batch", "jobs", jobId] as const;

export const getAdmBatchFindJobDetailQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindJobDetailQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindJobDetail>>> = ({ signal }) => admBatchFindJobDetail(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobDetail>>, TError, TData>;
};

export type AdmBatchFindJobDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindJobDetail>>>;
export type AdmBatchFindJobDetailQueryError = unknown;

export function useAdmBatchFindJobDetail<TData = Awaited<ReturnType<typeof admBatchFindJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindJobDetailQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindJobDetail


// CPF PRE-RUNTIME FALLBACK START admBatchRunJob
export type admBatchRunJobResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRunJobResponseSuccess = (admBatchRunJobResponse200) & {
  headers: Headers;
};

export type admBatchRunJobResponse = (admBatchRunJobResponseSuccess)

export const getAdmBatchRunJobUrl = (jobId: string) => `/adm/api/batch/jobs/${encodeURIComponent(String(jobId))}/run`;

export const admBatchRunJob = async (jobId: string, data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRunJobResponse> => {
  return cpfOrvalRequest<admBatchRunJobResponse>(getAdmBatchRunJobUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchRunJobMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRunJob>>, TError, {jobId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRunJob>>, TError, {jobId: string; data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchRunJob'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRunJob>>, {jobId: string; data: AdmBatchOperationRequest}> = (props) => {
    const { jobId, data } = props;
    return admBatchRunJob(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRunJobMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRunJob>>>;
export type AdmBatchRunJobMutationBody = AdmBatchOperationRequest;
export type AdmBatchRunJobMutationError = unknown;

export const useAdmBatchRunJob = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRunJob>>, TError, {jobId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRunJob>>, TError, {jobId: string; data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchRunJobMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRunJob


// CPF PRE-RUNTIME FALLBACK START admBatchFindLocks
export type admBatchFindLocksResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindLocksResponseSuccess = (admBatchFindLocksResponse200) & {
  headers: Headers;
};

export type admBatchFindLocksResponse = (admBatchFindLocksResponseSuccess)

export const getAdmBatchFindLocksUrl = () => `/adm/api/batch/locks`;

export const admBatchFindLocks = async (params?: AdmBatchFindLocksParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindLocksResponse> => {
  return cpfOrvalRequest<admBatchFindLocksResponse>(getAdmBatchFindLocksUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId },
  });
};

export const getAdmBatchFindLocksQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindLocksParams>) => ["adm", "api", "batch", "locks", toValue(params)] as const;

export const getAdmBatchFindLocksQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindLocks>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindLocksParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindLocks>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindLocksQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindLocks>>> = ({ signal }) => admBatchFindLocks(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindLocks>>, TError, TData>;
};

export type AdmBatchFindLocksQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindLocks>>>;
export type AdmBatchFindLocksQueryError = unknown;

export function useAdmBatchFindLocks<TData = Awaited<ReturnType<typeof admBatchFindLocks>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindLocksParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindLocks>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindLocksQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindLocks


// CPF PRE-RUNTIME FALLBACK START admBatchReleaseLock
export type admBatchReleaseLockResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchReleaseLockResponseSuccess = (admBatchReleaseLockResponse200) & {
  headers: Headers;
};

export type admBatchReleaseLockResponse = (admBatchReleaseLockResponseSuccess)

export const getAdmBatchReleaseLockUrl = () => `/adm/api/batch/locks/release`;

export const admBatchReleaseLock = async (data: AdmBatchLockReleaseRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchReleaseLockResponse> => {
  return cpfOrvalRequest<admBatchReleaseLockResponse>(getAdmBatchReleaseLockUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchReleaseLockMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchReleaseLock>>, TError, {data: AdmBatchLockReleaseRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchReleaseLock>>, TError, {data: AdmBatchLockReleaseRequest}, TContext> => {
  const mutationKey = ['admBatchReleaseLock'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchReleaseLock>>, {data: AdmBatchLockReleaseRequest}> = (props) => {
    const { data } = props;
    return admBatchReleaseLock(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchReleaseLockMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchReleaseLock>>>;
export type AdmBatchReleaseLockMutationBody = AdmBatchLockReleaseRequest;
export type AdmBatchReleaseLockMutationError = unknown;

export const useAdmBatchReleaseLock = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchReleaseLock>>, TError, {data: AdmBatchLockReleaseRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchReleaseLock>>, TError, {data: AdmBatchLockReleaseRequest}, TContext> => {
  return useMutation(getAdmBatchReleaseLockMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchReleaseLock


// CPF PRE-RUNTIME FALLBACK START admBatchFindOperationLogs
export type admBatchFindOperationLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindOperationLogsResponseSuccess = (admBatchFindOperationLogsResponse200) & {
  headers: Headers;
};

export type admBatchFindOperationLogsResponse = (admBatchFindOperationLogsResponseSuccess)

export const getAdmBatchFindOperationLogsUrl = () => `/adm/api/batch/operations`;

export const admBatchFindOperationLogs = async (params?: AdmBatchFindOperationLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindOperationLogsResponse> => {
  return cpfOrvalRequest<admBatchFindOperationLogsResponse>(getAdmBatchFindOperationLogsUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId, executionId: params?.executionId, limit: params?.limit },
  });
};

export const getAdmBatchFindOperationLogsQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindOperationLogsParams>) => ["adm", "api", "batch", "operations", toValue(params)] as const;

export const getAdmBatchFindOperationLogsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindOperationLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindOperationLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindOperationLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindOperationLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindOperationLogs>>> = ({ signal }) => admBatchFindOperationLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindOperationLogs>>, TError, TData>;
};

export type AdmBatchFindOperationLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindOperationLogs>>>;
export type AdmBatchFindOperationLogsQueryError = unknown;

export function useAdmBatchFindOperationLogs<TData = Awaited<ReturnType<typeof admBatchFindOperationLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindOperationLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindOperationLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindOperationLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindOperationLogs


// CPF PRE-RUNTIME FALLBACK START admBatchFindRelations
export type admBatchFindRelationsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindRelationsResponseSuccess = (admBatchFindRelationsResponse200) & {
  headers: Headers;
};

export type admBatchFindRelationsResponse = (admBatchFindRelationsResponseSuccess)

export const getAdmBatchFindRelationsUrl = () => `/adm/api/batch/relations`;

export const admBatchFindRelations = async (params?: AdmBatchFindRelationsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindRelationsResponse> => {
  return cpfOrvalRequest<admBatchFindRelationsResponse>(getAdmBatchFindRelationsUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId },
  });
};

export const getAdmBatchFindRelationsQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindRelationsParams>) => ["adm", "api", "batch", "relations", toValue(params)] as const;

export const getAdmBatchFindRelationsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindRelations>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindRelationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindRelations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindRelationsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindRelations>>> = ({ signal }) => admBatchFindRelations(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindRelations>>, TError, TData>;
};

export type AdmBatchFindRelationsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindRelations>>>;
export type AdmBatchFindRelationsQueryError = unknown;

export function useAdmBatchFindRelations<TData = Awaited<ReturnType<typeof admBatchFindRelations>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindRelationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindRelations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindRelationsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindRelations


// CPF PRE-RUNTIME FALLBACK START admBatchRunSchedulerOnce
export type admBatchRunSchedulerOnceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchRunSchedulerOnceResponseSuccess = (admBatchRunSchedulerOnceResponse200) & {
  headers: Headers;
};

export type admBatchRunSchedulerOnceResponse = (admBatchRunSchedulerOnceResponseSuccess)

export const getAdmBatchRunSchedulerOnceUrl = () => `/adm/api/batch/scheduler/run-once`;

export const admBatchRunSchedulerOnce = async (data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchRunSchedulerOnceResponse> => {
  return cpfOrvalRequest<admBatchRunSchedulerOnceResponse>(getAdmBatchRunSchedulerOnceUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchRunSchedulerOnceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>, TError, {data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>, TError, {data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchRunSchedulerOnce'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>, {data: AdmBatchOperationRequest}> = (props) => {
    const { data } = props;
    return admBatchRunSchedulerOnce(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchRunSchedulerOnceMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>>;
export type AdmBatchRunSchedulerOnceMutationBody = AdmBatchOperationRequest;
export type AdmBatchRunSchedulerOnceMutationError = unknown;

export const useAdmBatchRunSchedulerOnce = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>, TError, {data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchRunSchedulerOnce>>, TError, {data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchRunSchedulerOnceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchRunSchedulerOnce


// CPF PRE-RUNTIME FALLBACK START admBatchFindSchedules
export type admBatchFindSchedulesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindSchedulesResponseSuccess = (admBatchFindSchedulesResponse200) & {
  headers: Headers;
};

export type admBatchFindSchedulesResponse = (admBatchFindSchedulesResponseSuccess)

export const getAdmBatchFindSchedulesUrl = () => `/adm/api/batch/schedules`;

export const admBatchFindSchedules = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindSchedulesResponse> => {
  return cpfOrvalRequest<admBatchFindSchedulesResponse>(getAdmBatchFindSchedulesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchFindSchedulesQueryKey = () => ["adm", "api", "batch", "schedules"] as const;

export const getAdmBatchFindSchedulesQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindSchedules>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindSchedules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindSchedulesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindSchedules>>> = ({ signal }) => admBatchFindSchedules({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindSchedules>>, TError, TData>;
};

export type AdmBatchFindSchedulesQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindSchedules>>>;
export type AdmBatchFindSchedulesQueryError = unknown;

export function useAdmBatchFindSchedules<TData = Awaited<ReturnType<typeof admBatchFindSchedules>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindSchedules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindSchedulesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindSchedules


// CPF PRE-RUNTIME FALLBACK START admBatchDisableSchedule
export type admBatchDisableScheduleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchDisableScheduleResponseSuccess = (admBatchDisableScheduleResponse200) & {
  headers: Headers;
};

export type admBatchDisableScheduleResponse = (admBatchDisableScheduleResponseSuccess)

export const getAdmBatchDisableScheduleUrl = (scheduleId: string) => `/adm/api/batch/schedules/${encodeURIComponent(String(scheduleId))}/disable`;

export const admBatchDisableSchedule = async (scheduleId: string, data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchDisableScheduleResponse> => {
  return cpfOrvalRequest<admBatchDisableScheduleResponse>(getAdmBatchDisableScheduleUrl(scheduleId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchDisableScheduleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchDisableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchDisableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchDisableSchedule'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchDisableSchedule>>, {scheduleId: string; data: AdmBatchOperationRequest}> = (props) => {
    const { scheduleId, data } = props;
    return admBatchDisableSchedule(scheduleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchDisableScheduleMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchDisableSchedule>>>;
export type AdmBatchDisableScheduleMutationBody = AdmBatchOperationRequest;
export type AdmBatchDisableScheduleMutationError = unknown;

export const useAdmBatchDisableSchedule = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchDisableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchDisableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchDisableScheduleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchDisableSchedule


// CPF PRE-RUNTIME FALLBACK START admBatchEnableSchedule
export type admBatchEnableScheduleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchEnableScheduleResponseSuccess = (admBatchEnableScheduleResponse200) & {
  headers: Headers;
};

export type admBatchEnableScheduleResponse = (admBatchEnableScheduleResponseSuccess)

export const getAdmBatchEnableScheduleUrl = (scheduleId: string) => `/adm/api/batch/schedules/${encodeURIComponent(String(scheduleId))}/enable`;

export const admBatchEnableSchedule = async (scheduleId: string, data: AdmBatchOperationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchEnableScheduleResponse> => {
  return cpfOrvalRequest<admBatchEnableScheduleResponse>(getAdmBatchEnableScheduleUrl(scheduleId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBatchEnableScheduleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchEnableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBatchEnableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext> => {
  const mutationKey = ['admBatchEnableSchedule'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBatchEnableSchedule>>, {scheduleId: string; data: AdmBatchOperationRequest}> = (props) => {
    const { scheduleId, data } = props;
    return admBatchEnableSchedule(scheduleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBatchEnableScheduleMutationResult = NonNullable<Awaited<ReturnType<typeof admBatchEnableSchedule>>>;
export type AdmBatchEnableScheduleMutationBody = AdmBatchOperationRequest;
export type AdmBatchEnableScheduleMutationError = unknown;

export const useAdmBatchEnableSchedule = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBatchEnableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBatchEnableSchedule>>, TError, {scheduleId: string; data: AdmBatchOperationRequest}, TContext> => {
  return useMutation(getAdmBatchEnableScheduleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBatchEnableSchedule


// CPF PRE-RUNTIME FALLBACK START admBatchSimulateSchedule
export type admBatchSimulateScheduleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchSimulateScheduleResponseSuccess = (admBatchSimulateScheduleResponse200) & {
  headers: Headers;
};

export type admBatchSimulateScheduleResponse = (admBatchSimulateScheduleResponseSuccess)

export const getAdmBatchSimulateScheduleUrl = (scheduleId: string) => `/adm/api/batch/schedules/${encodeURIComponent(String(scheduleId))}/simulation`;

export const admBatchSimulateSchedule = async (scheduleId: string, params?: AdmBatchSimulateScheduleParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchSimulateScheduleResponse> => {
  return cpfOrvalRequest<admBatchSimulateScheduleResponse>(getAdmBatchSimulateScheduleUrl(scheduleId), {
    ...options,
    method: 'GET',
    params: { baseDate: params?.baseDate, days: params?.days },
  });
};

export const getAdmBatchSimulateScheduleQueryKey = (scheduleId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmBatchSimulateScheduleParams>) => ["adm", "api", "batch", "schedules", scheduleId, "simulation", toValue(params)] as const;

export const getAdmBatchSimulateScheduleQueryOptions = <TData = Awaited<ReturnType<typeof admBatchSimulateSchedule>>, TError = unknown>(
  scheduleId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmBatchSimulateScheduleParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchSimulateSchedule>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchSimulateScheduleQueryKey(toValue(scheduleId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchSimulateSchedule>>> = ({ signal }) => admBatchSimulateSchedule(toValue(scheduleId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(scheduleId) !== null && toValue(scheduleId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchSimulateSchedule>>, TError, TData>;
};

export type AdmBatchSimulateScheduleQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchSimulateSchedule>>>;
export type AdmBatchSimulateScheduleQueryError = unknown;

export function useAdmBatchSimulateSchedule<TData = Awaited<ReturnType<typeof admBatchSimulateSchedule>>, TError = unknown>(
  scheduleId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmBatchSimulateScheduleParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchSimulateSchedule>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchSimulateScheduleQueryOptions(scheduleId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchSimulateSchedule


// CPF PRE-RUNTIME FALLBACK START admBatchFindStepExecutions
export type admBatchFindStepExecutionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindStepExecutionsResponseSuccess = (admBatchFindStepExecutionsResponse200) & {
  headers: Headers;
};

export type admBatchFindStepExecutionsResponse = (admBatchFindStepExecutionsResponseSuccess)

export const getAdmBatchFindStepExecutionsUrl = () => `/adm/api/batch/steps`;

export const admBatchFindStepExecutions = async (params?: AdmBatchFindStepExecutionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindStepExecutionsResponse> => {
  return cpfOrvalRequest<admBatchFindStepExecutionsResponse>(getAdmBatchFindStepExecutionsUrl(), {
    ...options,
    method: 'GET',
    params: { executionId: params?.executionId, jobId: params?.jobId, limit: params?.limit },
  });
};

export const getAdmBatchFindStepExecutionsQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindStepExecutionsParams>) => ["adm", "api", "batch", "steps", toValue(params)] as const;

export const getAdmBatchFindStepExecutionsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindStepExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindStepExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindStepExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindStepExecutionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindStepExecutions>>> = ({ signal }) => admBatchFindStepExecutions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindStepExecutions>>, TError, TData>;
};

export type AdmBatchFindStepExecutionsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindStepExecutions>>>;
export type AdmBatchFindStepExecutionsQueryError = unknown;

export function useAdmBatchFindStepExecutions<TData = Awaited<ReturnType<typeof admBatchFindStepExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindStepExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindStepExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindStepExecutionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindStepExecutions


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchExecutions
export type admBatchWorkbenchExecutionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchExecutionsResponseSuccess = (admBatchWorkbenchExecutionsResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchExecutionsResponse = (admBatchWorkbenchExecutionsResponseSuccess)

export const getAdmBatchWorkbenchExecutionsUrl = () => `/adm/api/batch/workbench/executions`;

export const admBatchWorkbenchExecutions = async (params?: AdmBatchWorkbenchExecutionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchExecutionsResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchExecutionsResponse>(getAdmBatchWorkbenchExecutionsUrl(), {
    ...options,
    method: 'GET',
    params: { jobId: params?.jobId, transactionId: params?.transactionId, springBatchJobInstanceId: params?.springBatchJobInstanceId, status: params?.status, workerId: params?.workerId, serverInstanceId: params?.serverInstanceId, fromDate: params?.fromDate, toDate: params?.toDate, page: params?.page, size: params?.size },
  });
};

export const getAdmBatchWorkbenchExecutionsQueryKey = (params?: MaybeRefOrGetter<AdmBatchWorkbenchExecutionsParams>) => ["adm", "api", "batch", "workbench", "executions", toValue(params)] as const;

export const getAdmBatchWorkbenchExecutionsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchExecutionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>> = ({ signal }) => admBatchWorkbenchExecutions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>, TError, TData>;
};

export type AdmBatchWorkbenchExecutionsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>>;
export type AdmBatchWorkbenchExecutionsQueryError = unknown;

export function useAdmBatchWorkbenchExecutions<TData = Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchExecutionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchExecutionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchExecutions


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchExecutionDetail
export type admBatchWorkbenchExecutionDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchExecutionDetailResponseSuccess = (admBatchWorkbenchExecutionDetailResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchExecutionDetailResponse = (admBatchWorkbenchExecutionDetailResponseSuccess)

export const getAdmBatchWorkbenchExecutionDetailUrl = (executionId: number) => `/adm/api/batch/workbench/executions/${encodeURIComponent(String(executionId))}`;

export const admBatchWorkbenchExecutionDetail = async (executionId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchExecutionDetailResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchExecutionDetailResponse>(getAdmBatchWorkbenchExecutionDetailUrl(executionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchWorkbenchExecutionDetailQueryKey = (executionId: MaybeRefOrGetter<number>) => ["adm", "api", "batch", "workbench", "executions", executionId] as const;

export const getAdmBatchWorkbenchExecutionDetailQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>, TError = unknown>(
  executionId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchExecutionDetailQueryKey(toValue(executionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>> = ({ signal }) => admBatchWorkbenchExecutionDetail(toValue(executionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(executionId) !== null && toValue(executionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>, TError, TData>;
};

export type AdmBatchWorkbenchExecutionDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>>;
export type AdmBatchWorkbenchExecutionDetailQueryError = unknown;

export function useAdmBatchWorkbenchExecutionDetail<TData = Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>, TError = unknown>(
  executionId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchExecutionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchExecutionDetailQueryOptions(executionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchExecutionDetail


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchInfrastructure
export type admBatchWorkbenchInfrastructureResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchInfrastructureResponseSuccess = (admBatchWorkbenchInfrastructureResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchInfrastructureResponse = (admBatchWorkbenchInfrastructureResponseSuccess)

export const getAdmBatchWorkbenchInfrastructureUrl = () => `/adm/api/batch/workbench/infrastructure`;

export const admBatchWorkbenchInfrastructure = async (params?: AdmBatchWorkbenchInfrastructureParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchInfrastructureResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchInfrastructureResponse>(getAdmBatchWorkbenchInfrastructureUrl(), {
    ...options,
    method: 'GET',
    params: { heartbeatTimeoutSeconds: params?.heartbeatTimeoutSeconds, limit: params?.limit },
  });
};

export const getAdmBatchWorkbenchInfrastructureQueryKey = (params?: MaybeRefOrGetter<AdmBatchWorkbenchInfrastructureParams>) => ["adm", "api", "batch", "workbench", "infrastructure", toValue(params)] as const;

export const getAdmBatchWorkbenchInfrastructureQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchInfrastructureParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchInfrastructureQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>> = ({ signal }) => admBatchWorkbenchInfrastructure(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>, TError, TData>;
};

export type AdmBatchWorkbenchInfrastructureQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>>;
export type AdmBatchWorkbenchInfrastructureQueryError = unknown;

export function useAdmBatchWorkbenchInfrastructure<TData = Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchInfrastructureParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchInfrastructure>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchInfrastructureQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchInfrastructure


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchJobs
export type admBatchWorkbenchJobsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchJobsResponseSuccess = (admBatchWorkbenchJobsResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchJobsResponse = (admBatchWorkbenchJobsResponseSuccess)

export const getAdmBatchWorkbenchJobsUrl = () => `/adm/api/batch/workbench/jobs`;

export const admBatchWorkbenchJobs = async (params?: AdmBatchWorkbenchJobsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchJobsResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchJobsResponse>(getAdmBatchWorkbenchJobsUrl(), {
    ...options,
    method: 'GET',
    params: { query: params?.query, page: params?.page, size: params?.size, sort: params?.sort, direction: params?.direction },
  });
};

export const getAdmBatchWorkbenchJobsQueryKey = (params?: MaybeRefOrGetter<AdmBatchWorkbenchJobsParams>) => ["adm", "api", "batch", "workbench", "jobs", toValue(params)] as const;

export const getAdmBatchWorkbenchJobsQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchJobs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchJobsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchJobsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchJobs>>> = ({ signal }) => admBatchWorkbenchJobs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobs>>, TError, TData>;
};

export type AdmBatchWorkbenchJobsQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchJobs>>>;
export type AdmBatchWorkbenchJobsQueryError = unknown;

export function useAdmBatchWorkbenchJobs<TData = Awaited<ReturnType<typeof admBatchWorkbenchJobs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchJobsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchJobsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchJobs


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchJobDetail
export type admBatchWorkbenchJobDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchJobDetailResponseSuccess = (admBatchWorkbenchJobDetailResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchJobDetailResponse = (admBatchWorkbenchJobDetailResponseSuccess)

export const getAdmBatchWorkbenchJobDetailUrl = (jobId: string) => `/adm/api/batch/workbench/jobs/${encodeURIComponent(String(jobId))}`;

export const admBatchWorkbenchJobDetail = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchJobDetailResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchJobDetailResponse>(getAdmBatchWorkbenchJobDetailUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchWorkbenchJobDetailQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "batch", "workbench", "jobs", jobId] as const;

export const getAdmBatchWorkbenchJobDetailQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchJobDetailQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>> = ({ signal }) => admBatchWorkbenchJobDetail(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>, TError, TData>;
};

export type AdmBatchWorkbenchJobDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>>;
export type AdmBatchWorkbenchJobDetailQueryError = unknown;

export function useAdmBatchWorkbenchJobDetail<TData = Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchJobDetailQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchJobDetail


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchOverview
export type admBatchWorkbenchOverviewResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchOverviewResponseSuccess = (admBatchWorkbenchOverviewResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchOverviewResponse = (admBatchWorkbenchOverviewResponseSuccess)

export const getAdmBatchWorkbenchOverviewUrl = () => `/adm/api/batch/workbench/overview`;

export const admBatchWorkbenchOverview = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchOverviewResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchOverviewResponse>(getAdmBatchWorkbenchOverviewUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmBatchWorkbenchOverviewQueryKey = () => ["adm", "api", "batch", "workbench", "overview"] as const;

export const getAdmBatchWorkbenchOverviewQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchOverview>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchOverview>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchOverviewQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchOverview>>> = ({ signal }) => admBatchWorkbenchOverview({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchOverview>>, TError, TData>;
};

export type AdmBatchWorkbenchOverviewQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchOverview>>>;
export type AdmBatchWorkbenchOverviewQueryError = unknown;

export function useAdmBatchWorkbenchOverview<TData = Awaited<ReturnType<typeof admBatchWorkbenchOverview>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchOverview>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchOverviewQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchOverview


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchRecovery
export type admBatchWorkbenchRecoveryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchRecoveryResponseSuccess = (admBatchWorkbenchRecoveryResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchRecoveryResponse = (admBatchWorkbenchRecoveryResponseSuccess)

export const getAdmBatchWorkbenchRecoveryUrl = () => `/adm/api/batch/workbench/recovery`;

export const admBatchWorkbenchRecovery = async (params?: AdmBatchWorkbenchRecoveryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchRecoveryResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchRecoveryResponse>(getAdmBatchWorkbenchRecoveryUrl(), {
    ...options,
    method: 'GET',
    params: { heartbeatTimeoutSeconds: params?.heartbeatTimeoutSeconds, limit: params?.limit },
  });
};

export const getAdmBatchWorkbenchRecoveryQueryKey = (params?: MaybeRefOrGetter<AdmBatchWorkbenchRecoveryParams>) => ["adm", "api", "batch", "workbench", "recovery", toValue(params)] as const;

export const getAdmBatchWorkbenchRecoveryQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchRecoveryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchRecoveryQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>> = ({ signal }) => admBatchWorkbenchRecovery(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>, TError, TData>;
};

export type AdmBatchWorkbenchRecoveryQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>>;
export type AdmBatchWorkbenchRecoveryQueryError = unknown;

export function useAdmBatchWorkbenchRecovery<TData = Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchRecoveryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchRecovery>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchRecoveryQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchRecovery


// CPF PRE-RUNTIME FALLBACK START admBatchWorkbenchSchedules
export type admBatchWorkbenchSchedulesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchWorkbenchSchedulesResponseSuccess = (admBatchWorkbenchSchedulesResponse200) & {
  headers: Headers;
};

export type admBatchWorkbenchSchedulesResponse = (admBatchWorkbenchSchedulesResponseSuccess)

export const getAdmBatchWorkbenchSchedulesUrl = () => `/adm/api/batch/workbench/schedules`;

export const admBatchWorkbenchSchedules = async (params?: AdmBatchWorkbenchSchedulesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchWorkbenchSchedulesResponse> => {
  return cpfOrvalRequest<admBatchWorkbenchSchedulesResponse>(getAdmBatchWorkbenchSchedulesUrl(), {
    ...options,
    method: 'GET',
    params: { query: params?.query, page: params?.page, size: params?.size, sort: params?.sort, direction: params?.direction },
  });
};

export const getAdmBatchWorkbenchSchedulesQueryKey = (params?: MaybeRefOrGetter<AdmBatchWorkbenchSchedulesParams>) => ["adm", "api", "batch", "workbench", "schedules", toValue(params)] as const;

export const getAdmBatchWorkbenchSchedulesQueryOptions = <TData = Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchSchedulesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchWorkbenchSchedulesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>> = ({ signal }) => admBatchWorkbenchSchedules(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>, TError, TData>;
};

export type AdmBatchWorkbenchSchedulesQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>>;
export type AdmBatchWorkbenchSchedulesQueryError = unknown;

export function useAdmBatchWorkbenchSchedules<TData = Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchWorkbenchSchedulesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchWorkbenchSchedules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchWorkbenchSchedulesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchWorkbenchSchedules


// CPF PRE-RUNTIME FALLBACK START admBatchFindWorkers
export type admBatchFindWorkersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBatchFindWorkersResponseSuccess = (admBatchFindWorkersResponse200) & {
  headers: Headers;
};

export type admBatchFindWorkersResponse = (admBatchFindWorkersResponseSuccess)

export const getAdmBatchFindWorkersUrl = () => `/adm/api/batch/workers`;

export const admBatchFindWorkers = async (params?: AdmBatchFindWorkersParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBatchFindWorkersResponse> => {
  return cpfOrvalRequest<admBatchFindWorkersResponse>(getAdmBatchFindWorkersUrl(), {
    ...options,
    method: 'GET',
    params: { heartbeatTimeoutSeconds: params?.heartbeatTimeoutSeconds },
  });
};

export const getAdmBatchFindWorkersQueryKey = (params?: MaybeRefOrGetter<AdmBatchFindWorkersParams>) => ["adm", "api", "batch", "workers", toValue(params)] as const;

export const getAdmBatchFindWorkersQueryOptions = <TData = Awaited<ReturnType<typeof admBatchFindWorkers>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindWorkersParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindWorkers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBatchFindWorkersQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBatchFindWorkers>>> = ({ signal }) => admBatchFindWorkers(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBatchFindWorkers>>, TError, TData>;
};

export type AdmBatchFindWorkersQueryResult = NonNullable<Awaited<ReturnType<typeof admBatchFindWorkers>>>;
export type AdmBatchFindWorkersQueryError = unknown;

export function useAdmBatchFindWorkers<TData = Awaited<ReturnType<typeof admBatchFindWorkers>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBatchFindWorkersParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBatchFindWorkers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBatchFindWorkersQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBatchFindWorkers


// CPF PRE-RUNTIME FALLBACK START admBreakGlassFindSessions
export type admBreakGlassFindSessionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBreakGlassFindSessionsResponseSuccess = (admBreakGlassFindSessionsResponse200) & {
  headers: Headers;
};

export type admBreakGlassFindSessionsResponse = (admBreakGlassFindSessionsResponseSuccess)

export const getAdmBreakGlassFindSessionsUrl = () => `/adm/api/break-glass`;

export const admBreakGlassFindSessions = async (params?: AdmBreakGlassFindSessionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admBreakGlassFindSessionsResponse> => {
  return cpfOrvalRequest<admBreakGlassFindSessionsResponse>(getAdmBreakGlassFindSessionsUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, limit: params?.limit },
  });
};

export const getAdmBreakGlassFindSessionsQueryKey = (params?: MaybeRefOrGetter<AdmBreakGlassFindSessionsParams>) => ["adm", "api", "break-glass", toValue(params)] as const;

export const getAdmBreakGlassFindSessionsQueryOptions = <TData = Awaited<ReturnType<typeof admBreakGlassFindSessions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBreakGlassFindSessionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBreakGlassFindSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmBreakGlassFindSessionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admBreakGlassFindSessions>>> = ({ signal }) => admBreakGlassFindSessions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admBreakGlassFindSessions>>, TError, TData>;
};

export type AdmBreakGlassFindSessionsQueryResult = NonNullable<Awaited<ReturnType<typeof admBreakGlassFindSessions>>>;
export type AdmBreakGlassFindSessionsQueryError = unknown;

export function useAdmBreakGlassFindSessions<TData = Awaited<ReturnType<typeof admBreakGlassFindSessions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmBreakGlassFindSessionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admBreakGlassFindSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmBreakGlassFindSessionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admBreakGlassFindSessions


// CPF PRE-RUNTIME FALLBACK START admBreakGlassOpenSession
export type admBreakGlassOpenSessionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBreakGlassOpenSessionResponseSuccess = (admBreakGlassOpenSessionResponse200) & {
  headers: Headers;
};

export type admBreakGlassOpenSessionResponse = (admBreakGlassOpenSessionResponseSuccess)

export const getAdmBreakGlassOpenSessionUrl = () => `/adm/api/break-glass`;

export const admBreakGlassOpenSession = async (data: AdmBreakGlassOpenSessionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBreakGlassOpenSessionResponse> => {
  return cpfOrvalRequest<admBreakGlassOpenSessionResponse>(getAdmBreakGlassOpenSessionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBreakGlassOpenSessionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassOpenSession>>, TError, {data: AdmBreakGlassOpenSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassOpenSession>>, TError, {data: AdmBreakGlassOpenSessionRequest}, TContext> => {
  const mutationKey = ['admBreakGlassOpenSession'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBreakGlassOpenSession>>, {data: AdmBreakGlassOpenSessionRequest}> = (props) => {
    const { data } = props;
    return admBreakGlassOpenSession(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBreakGlassOpenSessionMutationResult = NonNullable<Awaited<ReturnType<typeof admBreakGlassOpenSession>>>;
export type AdmBreakGlassOpenSessionMutationBody = AdmBreakGlassOpenSessionRequest;
export type AdmBreakGlassOpenSessionMutationError = unknown;

export const useAdmBreakGlassOpenSession = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassOpenSession>>, TError, {data: AdmBreakGlassOpenSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBreakGlassOpenSession>>, TError, {data: AdmBreakGlassOpenSessionRequest}, TContext> => {
  return useMutation(getAdmBreakGlassOpenSessionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBreakGlassOpenSession


// CPF PRE-RUNTIME FALLBACK START admBreakGlassCloseSession
export type admBreakGlassCloseSessionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBreakGlassCloseSessionResponseSuccess = (admBreakGlassCloseSessionResponse200) & {
  headers: Headers;
};

export type admBreakGlassCloseSessionResponse = (admBreakGlassCloseSessionResponseSuccess)

export const getAdmBreakGlassCloseSessionUrl = (sessionId: string) => `/adm/api/break-glass/${encodeURIComponent(String(sessionId))}/close`;

export const admBreakGlassCloseSession = async (sessionId: string, data: AdmBreakGlassCloseSessionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBreakGlassCloseSessionResponse> => {
  return cpfOrvalRequest<admBreakGlassCloseSessionResponse>(getAdmBreakGlassCloseSessionUrl(sessionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBreakGlassCloseSessionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassCloseSession>>, TError, {sessionId: string; data: AdmBreakGlassCloseSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassCloseSession>>, TError, {sessionId: string; data: AdmBreakGlassCloseSessionRequest}, TContext> => {
  const mutationKey = ['admBreakGlassCloseSession'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBreakGlassCloseSession>>, {sessionId: string; data: AdmBreakGlassCloseSessionRequest}> = (props) => {
    const { sessionId, data } = props;
    return admBreakGlassCloseSession(sessionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBreakGlassCloseSessionMutationResult = NonNullable<Awaited<ReturnType<typeof admBreakGlassCloseSession>>>;
export type AdmBreakGlassCloseSessionMutationBody = AdmBreakGlassCloseSessionRequest;
export type AdmBreakGlassCloseSessionMutationError = unknown;

export const useAdmBreakGlassCloseSession = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassCloseSession>>, TError, {sessionId: string; data: AdmBreakGlassCloseSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBreakGlassCloseSession>>, TError, {sessionId: string; data: AdmBreakGlassCloseSessionRequest}, TContext> => {
  return useMutation(getAdmBreakGlassCloseSessionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBreakGlassCloseSession


// CPF PRE-RUNTIME FALLBACK START admBreakGlassReviewSession
export type admBreakGlassReviewSessionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admBreakGlassReviewSessionResponseSuccess = (admBreakGlassReviewSessionResponse200) & {
  headers: Headers;
};

export type admBreakGlassReviewSessionResponse = (admBreakGlassReviewSessionResponseSuccess)

export const getAdmBreakGlassReviewSessionUrl = (sessionId: string) => `/adm/api/break-glass/${encodeURIComponent(String(sessionId))}/review`;

export const admBreakGlassReviewSession = async (sessionId: string, data: AdmBreakGlassReviewSessionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admBreakGlassReviewSessionResponse> => {
  return cpfOrvalRequest<admBreakGlassReviewSessionResponse>(getAdmBreakGlassReviewSessionUrl(sessionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmBreakGlassReviewSessionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassReviewSession>>, TError, {sessionId: string; data: AdmBreakGlassReviewSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassReviewSession>>, TError, {sessionId: string; data: AdmBreakGlassReviewSessionRequest}, TContext> => {
  const mutationKey = ['admBreakGlassReviewSession'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admBreakGlassReviewSession>>, {sessionId: string; data: AdmBreakGlassReviewSessionRequest}> = (props) => {
    const { sessionId, data } = props;
    return admBreakGlassReviewSession(sessionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmBreakGlassReviewSessionMutationResult = NonNullable<Awaited<ReturnType<typeof admBreakGlassReviewSession>>>;
export type AdmBreakGlassReviewSessionMutationBody = AdmBreakGlassReviewSessionRequest;
export type AdmBreakGlassReviewSessionMutationError = unknown;

export const useAdmBreakGlassReviewSession = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admBreakGlassReviewSession>>, TError, {sessionId: string; data: AdmBreakGlassReviewSessionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admBreakGlassReviewSession>>, TError, {sessionId: string; data: AdmBreakGlassReviewSessionRequest}, TContext> => {
  return useMutation(getAdmBreakGlassReviewSessionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admBreakGlassReviewSession


// CPF PRE-RUNTIME FALLBACK START admCalendarFindDays
export type admCalendarFindDaysResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCalendarFindDaysResponseSuccess = (admCalendarFindDaysResponse200) & {
  headers: Headers;
};

export type admCalendarFindDaysResponse = (admCalendarFindDaysResponseSuccess)

export const getAdmCalendarFindDaysUrl = (calendarId: string) => `/adm/api/business-calendars/${encodeURIComponent(String(calendarId))}/days`;

export const admCalendarFindDays = async (calendarId: string, params?: AdmCalendarFindDaysParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCalendarFindDaysResponse> => {
  return cpfOrvalRequest<admCalendarFindDaysResponse>(getAdmCalendarFindDaysUrl(calendarId), {
    ...options,
    method: 'GET',
    params: { from: params?.from, to: params?.to, limit: params?.limit },
  });
};

export const getAdmCalendarFindDaysQueryKey = (calendarId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCalendarFindDaysParams>) => ["adm", "api", "business-calendars", calendarId, "days", toValue(params)] as const;

export const getAdmCalendarFindDaysQueryOptions = <TData = Awaited<ReturnType<typeof admCalendarFindDays>>, TError = unknown>(
  calendarId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCalendarFindDaysParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCalendarFindDays>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCalendarFindDaysQueryKey(toValue(calendarId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCalendarFindDays>>> = ({ signal }) => admCalendarFindDays(toValue(calendarId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(calendarId) !== null && toValue(calendarId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCalendarFindDays>>, TError, TData>;
};

export type AdmCalendarFindDaysQueryResult = NonNullable<Awaited<ReturnType<typeof admCalendarFindDays>>>;
export type AdmCalendarFindDaysQueryError = unknown;

export function useAdmCalendarFindDays<TData = Awaited<ReturnType<typeof admCalendarFindDays>>, TError = unknown>(
  calendarId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCalendarFindDaysParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCalendarFindDays>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCalendarFindDaysQueryOptions(calendarId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCalendarFindDays


// CPF PRE-RUNTIME FALLBACK START admCalendarDeleteDay
export type admCalendarDeleteDayResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCalendarDeleteDayResponseSuccess = (admCalendarDeleteDayResponse200) & {
  headers: Headers;
};

export type admCalendarDeleteDayResponse = (admCalendarDeleteDayResponseSuccess)

export const getAdmCalendarDeleteDayUrl = (calendarId: string, businessDate: string) => `/adm/api/business-calendars/${encodeURIComponent(String(calendarId))}/days/${encodeURIComponent(String(businessDate))}`;

export const admCalendarDeleteDay = async (calendarId: string, businessDate: string, params: AdmCalendarDeleteDayParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCalendarDeleteDayResponse> => {
  return cpfOrvalRequest<admCalendarDeleteDayResponse>(getAdmCalendarDeleteDayUrl(calendarId, businessDate), {
    ...options,
    method: 'DELETE',
    params: { expectedVersion: params.expectedVersion, auditReason: params.auditReason },
  });
};

export const getAdmCalendarDeleteDayMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCalendarDeleteDay>>, TError, {calendarId: string; businessDate: string; params: AdmCalendarDeleteDayParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCalendarDeleteDay>>, TError, {calendarId: string; businessDate: string; params: AdmCalendarDeleteDayParams}, TContext> => {
  const mutationKey = ['admCalendarDeleteDay'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCalendarDeleteDay>>, {calendarId: string; businessDate: string; params: AdmCalendarDeleteDayParams}> = (props) => {
    const { calendarId, businessDate, params } = props;
    return admCalendarDeleteDay(calendarId, businessDate, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCalendarDeleteDayMutationResult = NonNullable<Awaited<ReturnType<typeof admCalendarDeleteDay>>>;
export type AdmCalendarDeleteDayMutationBody = never;
export type AdmCalendarDeleteDayMutationError = unknown;

export const useAdmCalendarDeleteDay = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCalendarDeleteDay>>, TError, {calendarId: string; businessDate: string; params: AdmCalendarDeleteDayParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCalendarDeleteDay>>, TError, {calendarId: string; businessDate: string; params: AdmCalendarDeleteDayParams}, TContext> => {
  return useMutation(getAdmCalendarDeleteDayMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCalendarDeleteDay


// CPF PRE-RUNTIME FALLBACK START admCalendarSaveDay
export type admCalendarSaveDayResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCalendarSaveDayResponseSuccess = (admCalendarSaveDayResponse200) & {
  headers: Headers;
};

export type admCalendarSaveDayResponse = (admCalendarSaveDayResponseSuccess)

export const getAdmCalendarSaveDayUrl = (calendarId: string, businessDate: string) => `/adm/api/business-calendars/${encodeURIComponent(String(calendarId))}/days/${encodeURIComponent(String(businessDate))}`;

export const admCalendarSaveDay = async (calendarId: string, businessDate: string, data: AdmCalendarSaveDayRequest, params?: AdmCalendarSaveDayParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCalendarSaveDayResponse> => {
  return cpfOrvalRequest<admCalendarSaveDayResponse>(getAdmCalendarSaveDayUrl(calendarId, businessDate), {
    ...options,
    method: 'PUT',
    params: { expectedVersion: params?.expectedVersion },
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCalendarSaveDayMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCalendarSaveDay>>, TError, {calendarId: string; businessDate: string; data: AdmCalendarSaveDayRequest; params?: AdmCalendarSaveDayParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCalendarSaveDay>>, TError, {calendarId: string; businessDate: string; data: AdmCalendarSaveDayRequest; params?: AdmCalendarSaveDayParams}, TContext> => {
  const mutationKey = ['admCalendarSaveDay'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCalendarSaveDay>>, {calendarId: string; businessDate: string; data: AdmCalendarSaveDayRequest; params?: AdmCalendarSaveDayParams}> = (props) => {
    const { calendarId, businessDate, data, params } = props;
    return admCalendarSaveDay(calendarId, businessDate, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCalendarSaveDayMutationResult = NonNullable<Awaited<ReturnType<typeof admCalendarSaveDay>>>;
export type AdmCalendarSaveDayMutationBody = AdmCalendarSaveDayRequest;
export type AdmCalendarSaveDayMutationError = unknown;

export const useAdmCalendarSaveDay = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCalendarSaveDay>>, TError, {calendarId: string; businessDate: string; data: AdmCalendarSaveDayRequest; params?: AdmCalendarSaveDayParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCalendarSaveDay>>, TError, {calendarId: string; businessDate: string; data: AdmCalendarSaveDayRequest; params?: AdmCalendarSaveDayParams}, TContext> => {
  return useMutation(getAdmCalendarSaveDayMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCalendarSaveDay


// CPF PRE-RUNTIME FALLBACK START admCalendarResolveDate
export type admCalendarResolveDateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCalendarResolveDateResponseSuccess = (admCalendarResolveDateResponse200) & {
  headers: Headers;
};

export type admCalendarResolveDateResponse = (admCalendarResolveDateResponseSuccess)

export const getAdmCalendarResolveDateUrl = (calendarId: string) => `/adm/api/business-calendars/${encodeURIComponent(String(calendarId))}/resolve`;

export const admCalendarResolveDate = async (calendarId: string, params: AdmCalendarResolveDateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCalendarResolveDateResponse> => {
  return cpfOrvalRequest<admCalendarResolveDateResponse>(getAdmCalendarResolveDateUrl(calendarId), {
    ...options,
    method: 'GET',
    params: { date: params.date, offset: params.offset },
  });
};

export const getAdmCalendarResolveDateQueryKey = (calendarId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmCalendarResolveDateParams>) => ["adm", "api", "business-calendars", calendarId, "resolve", toValue(params)] as const;

export const getAdmCalendarResolveDateQueryOptions = <TData = Awaited<ReturnType<typeof admCalendarResolveDate>>, TError = unknown>(
  calendarId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmCalendarResolveDateParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCalendarResolveDate>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCalendarResolveDateQueryKey(toValue(calendarId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCalendarResolveDate>>> = ({ signal }) => admCalendarResolveDate(toValue(calendarId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(calendarId) !== null && toValue(calendarId) !== undefined && toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCalendarResolveDate>>, TError, TData>;
};

export type AdmCalendarResolveDateQueryResult = NonNullable<Awaited<ReturnType<typeof admCalendarResolveDate>>>;
export type AdmCalendarResolveDateQueryError = unknown;

export function useAdmCalendarResolveDate<TData = Awaited<ReturnType<typeof admCalendarResolveDate>>, TError = unknown>(
  calendarId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmCalendarResolveDateParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCalendarResolveDate>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCalendarResolveDateQueryOptions(calendarId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCalendarResolveDate


// CPF PRE-RUNTIME FALLBACK START admCacheEvictKey
export type admCacheEvictKeyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCacheEvictKeyResponseSuccess = (admCacheEvictKeyResponse200) & {
  headers: Headers;
};

export type admCacheEvictKeyResponse = (admCacheEvictKeyResponseSuccess)

export const getAdmCacheEvictKeyUrl = () => `/adm/api/cache/evict-key`;

export const admCacheEvictKey = async (data: AdmCacheEvictKeyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCacheEvictKeyResponse> => {
  return cpfOrvalRequest<admCacheEvictKeyResponse>(getAdmCacheEvictKeyUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCacheEvictKeyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictKey>>, TError, {data: AdmCacheEvictKeyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictKey>>, TError, {data: AdmCacheEvictKeyRequest}, TContext> => {
  const mutationKey = ['admCacheEvictKey'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCacheEvictKey>>, {data: AdmCacheEvictKeyRequest}> = (props) => {
    const { data } = props;
    return admCacheEvictKey(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCacheEvictKeyMutationResult = NonNullable<Awaited<ReturnType<typeof admCacheEvictKey>>>;
export type AdmCacheEvictKeyMutationBody = AdmCacheEvictKeyRequest;
export type AdmCacheEvictKeyMutationError = unknown;

export const useAdmCacheEvictKey = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictKey>>, TError, {data: AdmCacheEvictKeyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCacheEvictKey>>, TError, {data: AdmCacheEvictKeyRequest}, TContext> => {
  return useMutation(getAdmCacheEvictKeyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCacheEvictKey


// CPF PRE-RUNTIME FALLBACK START admCacheEvictNamespace
export type admCacheEvictNamespaceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCacheEvictNamespaceResponseSuccess = (admCacheEvictNamespaceResponse200) & {
  headers: Headers;
};

export type admCacheEvictNamespaceResponse = (admCacheEvictNamespaceResponseSuccess)

export const getAdmCacheEvictNamespaceUrl = () => `/adm/api/cache/evict-namespace`;

export const admCacheEvictNamespace = async (data: AdmCacheEvictNamespaceRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCacheEvictNamespaceResponse> => {
  return cpfOrvalRequest<admCacheEvictNamespaceResponse>(getAdmCacheEvictNamespaceUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCacheEvictNamespaceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictNamespace>>, TError, {data: AdmCacheEvictNamespaceRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictNamespace>>, TError, {data: AdmCacheEvictNamespaceRequest}, TContext> => {
  const mutationKey = ['admCacheEvictNamespace'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCacheEvictNamespace>>, {data: AdmCacheEvictNamespaceRequest}> = (props) => {
    const { data } = props;
    return admCacheEvictNamespace(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCacheEvictNamespaceMutationResult = NonNullable<Awaited<ReturnType<typeof admCacheEvictNamespace>>>;
export type AdmCacheEvictNamespaceMutationBody = AdmCacheEvictNamespaceRequest;
export type AdmCacheEvictNamespaceMutationError = unknown;

export const useAdmCacheEvictNamespace = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheEvictNamespace>>, TError, {data: AdmCacheEvictNamespaceRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCacheEvictNamespace>>, TError, {data: AdmCacheEvictNamespaceRequest}, TContext> => {
  return useMutation(getAdmCacheEvictNamespaceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCacheEvictNamespace


// CPF PRE-RUNTIME FALLBACK START admCacheReconcile
export type admCacheReconcileResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCacheReconcileResponseSuccess = (admCacheReconcileResponse200) & {
  headers: Headers;
};

export type admCacheReconcileResponse = (admCacheReconcileResponseSuccess)

export const getAdmCacheReconcileUrl = () => `/adm/api/cache/reconcile`;

export const admCacheReconcile = async (data: AdmCacheControlRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCacheReconcileResponse> => {
  return cpfOrvalRequest<admCacheReconcileResponse>(getAdmCacheReconcileUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCacheReconcileMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheReconcile>>, TError, {data: AdmCacheControlRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCacheReconcile>>, TError, {data: AdmCacheControlRequest}, TContext> => {
  const mutationKey = ['admCacheReconcile'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCacheReconcile>>, {data: AdmCacheControlRequest}> = (props) => {
    const { data } = props;
    return admCacheReconcile(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCacheReconcileMutationResult = NonNullable<Awaited<ReturnType<typeof admCacheReconcile>>>;
export type AdmCacheReconcileMutationBody = AdmCacheControlRequest;
export type AdmCacheReconcileMutationError = unknown;

export const useAdmCacheReconcile = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheReconcile>>, TError, {data: AdmCacheControlRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCacheReconcile>>, TError, {data: AdmCacheControlRequest}, TContext> => {
  return useMutation(getAdmCacheReconcileMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCacheReconcile


// CPF PRE-RUNTIME FALLBACK START admCacheRefresh
export type admCacheRefreshResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCacheRefreshResponseSuccess = (admCacheRefreshResponse200) & {
  headers: Headers;
};

export type admCacheRefreshResponse = (admCacheRefreshResponseSuccess)

export const getAdmCacheRefreshUrl = () => `/adm/api/cache/refresh`;

export const admCacheRefresh = async (params: AdmCacheRefreshParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCacheRefreshResponse> => {
  return cpfOrvalRequest<admCacheRefreshResponse>(getAdmCacheRefreshUrl(), {
    ...options,
    method: 'POST',
    params: { target: params.target, reason: params.reason },
  });
};

export const getAdmCacheRefreshMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheRefresh>>, TError, {params: AdmCacheRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCacheRefresh>>, TError, {params: AdmCacheRefreshParams}, TContext> => {
  const mutationKey = ['admCacheRefresh'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCacheRefresh>>, {params: AdmCacheRefreshParams}> = (props) => {
    const { params } = props;
    return admCacheRefresh(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCacheRefreshMutationResult = NonNullable<Awaited<ReturnType<typeof admCacheRefresh>>>;
export type AdmCacheRefreshMutationBody = never;
export type AdmCacheRefreshMutationError = unknown;

export const useAdmCacheRefresh = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCacheRefresh>>, TError, {params: AdmCacheRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCacheRefresh>>, TError, {params: AdmCacheRefreshParams}, TContext> => {
  return useMutation(getAdmCacheRefreshMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCacheRefresh


// CPF PRE-RUNTIME FALLBACK START admCacheSummary
export type admCacheSummaryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCacheSummaryResponseSuccess = (admCacheSummaryResponse200) & {
  headers: Headers;
};

export type admCacheSummaryResponse = (admCacheSummaryResponseSuccess)

export const getAdmCacheSummaryUrl = () => `/adm/api/cache/summary`;

export const admCacheSummary = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admCacheSummaryResponse> => {
  return cpfOrvalRequest<admCacheSummaryResponse>(getAdmCacheSummaryUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCacheSummaryQueryKey = () => ["adm", "api", "cache", "summary"] as const;

export const getAdmCacheSummaryQueryOptions = <TData = Awaited<ReturnType<typeof admCacheSummary>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCacheSummary>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCacheSummaryQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCacheSummary>>> = ({ signal }) => admCacheSummary({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCacheSummary>>, TError, TData>;
};

export type AdmCacheSummaryQueryResult = NonNullable<Awaited<ReturnType<typeof admCacheSummary>>>;
export type AdmCacheSummaryQueryError = unknown;

export function useAdmCacheSummary<TData = Awaited<ReturnType<typeof admCacheSummary>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCacheSummary>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCacheSummaryQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCacheSummary


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindJobs
export type admCenterCutFindJobsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindJobsResponseSuccess = (admCenterCutFindJobsResponse200) & {
  headers: Headers;
};

export type admCenterCutFindJobsResponse = (admCenterCutFindJobsResponseSuccess)

export const getAdmCenterCutFindJobsUrl = () => `/adm/api/center-cut/jobs`;

export const admCenterCutFindJobs = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindJobsResponse> => {
  return cpfOrvalRequest<admCenterCutFindJobsResponse>(getAdmCenterCutFindJobsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCenterCutFindJobsQueryKey = () => ["adm", "api", "center-cut", "jobs"] as const;

export const getAdmCenterCutFindJobsQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindJobs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindJobsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindJobs>>> = ({ signal }) => admCenterCutFindJobs({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobs>>, TError, TData>;
};

export type AdmCenterCutFindJobsQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindJobs>>>;
export type AdmCenterCutFindJobsQueryError = unknown;

export function useAdmCenterCutFindJobs<TData = Awaited<ReturnType<typeof admCenterCutFindJobs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindJobsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindJobs


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindJobDetail
export type admCenterCutFindJobDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindJobDetailResponseSuccess = (admCenterCutFindJobDetailResponse200) & {
  headers: Headers;
};

export type admCenterCutFindJobDetailResponse = (admCenterCutFindJobDetailResponseSuccess)

export const getAdmCenterCutFindJobDetailUrl = (centerCutJobId: string) => `/adm/api/center-cut/jobs/${encodeURIComponent(String(centerCutJobId))}`;

export const admCenterCutFindJobDetail = async (centerCutJobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindJobDetailResponse> => {
  return cpfOrvalRequest<admCenterCutFindJobDetailResponse>(getAdmCenterCutFindJobDetailUrl(centerCutJobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCenterCutFindJobDetailQueryKey = (centerCutJobId: MaybeRefOrGetter<string>) => ["adm", "api", "center-cut", "jobs", centerCutJobId] as const;

export const getAdmCenterCutFindJobDetailQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindJobDetail>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindJobDetailQueryKey(toValue(centerCutJobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindJobDetail>>> = ({ signal }) => admCenterCutFindJobDetail(toValue(centerCutJobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(centerCutJobId) !== null && toValue(centerCutJobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobDetail>>, TError, TData>;
};

export type AdmCenterCutFindJobDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindJobDetail>>>;
export type AdmCenterCutFindJobDetailQueryError = unknown;

export function useAdmCenterCutFindJobDetail<TData = Awaited<ReturnType<typeof admCenterCutFindJobDetail>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindJobDetailQueryOptions(centerCutJobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindJobDetail


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindParameters
export type admCenterCutFindParametersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindParametersResponseSuccess = (admCenterCutFindParametersResponse200) & {
  headers: Headers;
};

export type admCenterCutFindParametersResponse = (admCenterCutFindParametersResponseSuccess)

export const getAdmCenterCutFindParametersUrl = (centerCutJobId: string) => `/adm/api/center-cut/jobs/${encodeURIComponent(String(centerCutJobId))}/parameters`;

export const admCenterCutFindParameters = async (centerCutJobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindParametersResponse> => {
  return cpfOrvalRequest<admCenterCutFindParametersResponse>(getAdmCenterCutFindParametersUrl(centerCutJobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCenterCutFindParametersQueryKey = (centerCutJobId: MaybeRefOrGetter<string>) => ["adm", "api", "center-cut", "jobs", centerCutJobId, "parameters"] as const;

export const getAdmCenterCutFindParametersQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindParameters>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindParameters>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindParametersQueryKey(toValue(centerCutJobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindParameters>>> = ({ signal }) => admCenterCutFindParameters(toValue(centerCutJobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(centerCutJobId) !== null && toValue(centerCutJobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindParameters>>, TError, TData>;
};

export type AdmCenterCutFindParametersQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindParameters>>>;
export type AdmCenterCutFindParametersQueryError = unknown;

export function useAdmCenterCutFindParameters<TData = Awaited<ReturnType<typeof admCenterCutFindParameters>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindParameters>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindParametersQueryOptions(centerCutJobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindParameters


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindResults
export type admCenterCutFindResultsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindResultsResponseSuccess = (admCenterCutFindResultsResponse200) & {
  headers: Headers;
};

export type admCenterCutFindResultsResponse = (admCenterCutFindResultsResponseSuccess)

export const getAdmCenterCutFindResultsUrl = (centerCutJobId: string) => `/adm/api/center-cut/jobs/${encodeURIComponent(String(centerCutJobId))}/results`;

export const admCenterCutFindResults = async (centerCutJobId: string, params?: AdmCenterCutFindResultsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindResultsResponse> => {
  return cpfOrvalRequest<admCenterCutFindResultsResponse>(getAdmCenterCutFindResultsUrl(centerCutJobId), {
    ...options,
    method: 'GET',
    params: { resultStatus: params?.resultStatus, limit: params?.limit },
  });
};

export const getAdmCenterCutFindResultsQueryKey = (centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindResultsParams>) => ["adm", "api", "center-cut", "jobs", centerCutJobId, "results", toValue(params)] as const;

export const getAdmCenterCutFindResultsQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindResults>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindResultsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResults>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindResultsQueryKey(toValue(centerCutJobId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindResults>>> = ({ signal }) => admCenterCutFindResults(toValue(centerCutJobId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(centerCutJobId) !== null && toValue(centerCutJobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResults>>, TError, TData>;
};

export type AdmCenterCutFindResultsQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindResults>>>;
export type AdmCenterCutFindResultsQueryError = unknown;

export function useAdmCenterCutFindResults<TData = Awaited<ReturnType<typeof admCenterCutFindResults>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindResultsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResults>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindResultsQueryOptions(centerCutJobId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindResults


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindSummary
export type admCenterCutFindSummaryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindSummaryResponseSuccess = (admCenterCutFindSummaryResponse200) & {
  headers: Headers;
};

export type admCenterCutFindSummaryResponse = (admCenterCutFindSummaryResponseSuccess)

export const getAdmCenterCutFindSummaryUrl = (centerCutJobId: string) => `/adm/api/center-cut/jobs/${encodeURIComponent(String(centerCutJobId))}/summary`;

export const admCenterCutFindSummary = async (centerCutJobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindSummaryResponse> => {
  return cpfOrvalRequest<admCenterCutFindSummaryResponse>(getAdmCenterCutFindSummaryUrl(centerCutJobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCenterCutFindSummaryQueryKey = (centerCutJobId: MaybeRefOrGetter<string>) => ["adm", "api", "center-cut", "jobs", centerCutJobId, "summary"] as const;

export const getAdmCenterCutFindSummaryQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindSummary>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindSummary>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindSummaryQueryKey(toValue(centerCutJobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindSummary>>> = ({ signal }) => admCenterCutFindSummary(toValue(centerCutJobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(centerCutJobId) !== null && toValue(centerCutJobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindSummary>>, TError, TData>;
};

export type AdmCenterCutFindSummaryQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindSummary>>>;
export type AdmCenterCutFindSummaryQueryError = unknown;

export function useAdmCenterCutFindSummary<TData = Awaited<ReturnType<typeof admCenterCutFindSummary>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindSummary>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindSummaryQueryOptions(centerCutJobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindSummary


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindTargets
export type admCenterCutFindTargetsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindTargetsResponseSuccess = (admCenterCutFindTargetsResponse200) & {
  headers: Headers;
};

export type admCenterCutFindTargetsResponse = (admCenterCutFindTargetsResponseSuccess)

export const getAdmCenterCutFindTargetsUrl = (centerCutJobId: string) => `/adm/api/center-cut/jobs/${encodeURIComponent(String(centerCutJobId))}/targets`;

export const admCenterCutFindTargets = async (centerCutJobId: string, params?: AdmCenterCutFindTargetsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindTargetsResponse> => {
  return cpfOrvalRequest<admCenterCutFindTargetsResponse>(getAdmCenterCutFindTargetsUrl(centerCutJobId), {
    ...options,
    method: 'GET',
    params: { statusCode: params?.statusCode, limit: params?.limit },
  });
};

export const getAdmCenterCutFindTargetsQueryKey = (centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindTargetsParams>) => ["adm", "api", "center-cut", "jobs", centerCutJobId, "targets", toValue(params)] as const;

export const getAdmCenterCutFindTargetsQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindTargets>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindTargetsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindTargets>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindTargetsQueryKey(toValue(centerCutJobId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindTargets>>> = ({ signal }) => admCenterCutFindTargets(toValue(centerCutJobId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(centerCutJobId) !== null && toValue(centerCutJobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindTargets>>, TError, TData>;
};

export type AdmCenterCutFindTargetsQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindTargets>>>;
export type AdmCenterCutFindTargetsQueryError = unknown;

export function useAdmCenterCutFindTargets<TData = Awaited<ReturnType<typeof admCenterCutFindTargets>>, TError = unknown>(
  centerCutJobId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmCenterCutFindTargetsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindTargets>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindTargetsQueryOptions(centerCutJobId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindTargets


// CPF PRE-RUNTIME FALLBACK START admCenterCutFindResultDetail
export type admCenterCutFindResultDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutFindResultDetailResponseSuccess = (admCenterCutFindResultDetailResponse200) & {
  headers: Headers;
};

export type admCenterCutFindResultDetailResponse = (admCenterCutFindResultDetailResponseSuccess)

export const getAdmCenterCutFindResultDetailUrl = (resultId: string) => `/adm/api/center-cut/results/${encodeURIComponent(String(resultId))}`;

export const admCenterCutFindResultDetail = async (resultId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutFindResultDetailResponse> => {
  return cpfOrvalRequest<admCenterCutFindResultDetailResponse>(getAdmCenterCutFindResultDetailUrl(resultId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCenterCutFindResultDetailQueryKey = (resultId: MaybeRefOrGetter<string>) => ["adm", "api", "center-cut", "results", resultId] as const;

export const getAdmCenterCutFindResultDetailQueryOptions = <TData = Awaited<ReturnType<typeof admCenterCutFindResultDetail>>, TError = unknown>(
  resultId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResultDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCenterCutFindResultDetailQueryKey(toValue(resultId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCenterCutFindResultDetail>>> = ({ signal }) => admCenterCutFindResultDetail(toValue(resultId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(resultId) !== null && toValue(resultId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResultDetail>>, TError, TData>;
};

export type AdmCenterCutFindResultDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admCenterCutFindResultDetail>>>;
export type AdmCenterCutFindResultDetailQueryError = unknown;

export function useAdmCenterCutFindResultDetail<TData = Awaited<ReturnType<typeof admCenterCutFindResultDetail>>, TError = unknown>(
  resultId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCenterCutFindResultDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCenterCutFindResultDetailQueryOptions(resultId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCenterCutFindResultDetail


// CPF PRE-RUNTIME FALLBACK START admChannelFindSnapshot
export type admChannelFindSnapshotResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelFindSnapshotResponseSuccess = (admChannelFindSnapshotResponse200) & {
  headers: Headers;
};

export type admChannelFindSnapshotResponse = (admChannelFindSnapshotResponseSuccess)

export const getAdmChannelFindSnapshotUrl = () => `/adm/api/channels`;

export const admChannelFindSnapshot = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelFindSnapshotResponse> => {
  return cpfOrvalRequest<admChannelFindSnapshotResponse>(getAdmChannelFindSnapshotUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmChannelFindSnapshotQueryKey = () => ["adm", "api", "channels"] as const;

export const getAdmChannelFindSnapshotQueryOptions = <TData = Awaited<ReturnType<typeof admChannelFindSnapshot>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admChannelFindSnapshot>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmChannelFindSnapshotQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admChannelFindSnapshot>>> = ({ signal }) => admChannelFindSnapshot({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admChannelFindSnapshot>>, TError, TData>;
};

export type AdmChannelFindSnapshotQueryResult = NonNullable<Awaited<ReturnType<typeof admChannelFindSnapshot>>>;
export type AdmChannelFindSnapshotQueryError = unknown;

export function useAdmChannelFindSnapshot<TData = Awaited<ReturnType<typeof admChannelFindSnapshot>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admChannelFindSnapshot>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmChannelFindSnapshotQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admChannelFindSnapshot


// CPF PRE-RUNTIME FALLBACK START admChannelExportPackage
export type admChannelExportPackageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelExportPackageResponseSuccess = (admChannelExportPackageResponse200) & {
  headers: Headers;
};

export type admChannelExportPackageResponse = (admChannelExportPackageResponseSuccess)

export const getAdmChannelExportPackageUrl = () => `/adm/api/channels/package`;

export const admChannelExportPackage = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelExportPackageResponse> => {
  return cpfOrvalRequest<admChannelExportPackageResponse>(getAdmChannelExportPackageUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmChannelExportPackageQueryKey = () => ["adm", "api", "channels", "package"] as const;

export const getAdmChannelExportPackageQueryOptions = <TData = Awaited<ReturnType<typeof admChannelExportPackage>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admChannelExportPackage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmChannelExportPackageQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admChannelExportPackage>>> = ({ signal }) => admChannelExportPackage({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admChannelExportPackage>>, TError, TData>;
};

export type AdmChannelExportPackageQueryResult = NonNullable<Awaited<ReturnType<typeof admChannelExportPackage>>>;
export type AdmChannelExportPackageQueryError = unknown;

export function useAdmChannelExportPackage<TData = Awaited<ReturnType<typeof admChannelExportPackage>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admChannelExportPackage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmChannelExportPackageQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admChannelExportPackage


// CPF PRE-RUNTIME FALLBACK START admChannelImportPackage
export type admChannelImportPackageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelImportPackageResponseSuccess = (admChannelImportPackageResponse200) & {
  headers: Headers;
};

export type admChannelImportPackageResponse = (admChannelImportPackageResponseSuccess)

export const getAdmChannelImportPackageUrl = () => `/adm/api/channels/package/import`;

export const admChannelImportPackage = async (data: AdmChannelPackageImportRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelImportPackageResponse> => {
  return cpfOrvalRequest<admChannelImportPackageResponse>(getAdmChannelImportPackageUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmChannelImportPackageMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelImportPackage>>, TError, {data: AdmChannelPackageImportRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admChannelImportPackage>>, TError, {data: AdmChannelPackageImportRequest}, TContext> => {
  const mutationKey = ['admChannelImportPackage'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admChannelImportPackage>>, {data: AdmChannelPackageImportRequest}> = (props) => {
    const { data } = props;
    return admChannelImportPackage(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmChannelImportPackageMutationResult = NonNullable<Awaited<ReturnType<typeof admChannelImportPackage>>>;
export type AdmChannelImportPackageMutationBody = AdmChannelPackageImportRequest;
export type AdmChannelImportPackageMutationError = unknown;

export const useAdmChannelImportPackage = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelImportPackage>>, TError, {data: AdmChannelPackageImportRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admChannelImportPackage>>, TError, {data: AdmChannelPackageImportRequest}, TContext> => {
  return useMutation(getAdmChannelImportPackageMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admChannelImportPackage


// CPF PRE-RUNTIME FALLBACK START admChannelSaveExecutionPolicy
export type admChannelSaveExecutionPolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelSaveExecutionPolicyResponseSuccess = (admChannelSaveExecutionPolicyResponse200) & {
  headers: Headers;
};

export type admChannelSaveExecutionPolicyResponse = (admChannelSaveExecutionPolicyResponseSuccess)

export const getAdmChannelSaveExecutionPolicyUrl = (policyKey: string) => `/adm/api/channels/policies/${encodeURIComponent(String(policyKey))}`;

export const admChannelSaveExecutionPolicy = async (policyKey: string, data: AdmChannelPolicySaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelSaveExecutionPolicyResponse> => {
  return cpfOrvalRequest<admChannelSaveExecutionPolicyResponse>(getAdmChannelSaveExecutionPolicyUrl(policyKey), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmChannelSaveExecutionPolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>, TError, {policyKey: string; data: AdmChannelPolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>, TError, {policyKey: string; data: AdmChannelPolicySaveRequest}, TContext> => {
  const mutationKey = ['admChannelSaveExecutionPolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>, {policyKey: string; data: AdmChannelPolicySaveRequest}> = (props) => {
    const { policyKey, data } = props;
    return admChannelSaveExecutionPolicy(policyKey, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmChannelSaveExecutionPolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>>;
export type AdmChannelSaveExecutionPolicyMutationBody = AdmChannelPolicySaveRequest;
export type AdmChannelSaveExecutionPolicyMutationError = unknown;

export const useAdmChannelSaveExecutionPolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>, TError, {policyKey: string; data: AdmChannelPolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admChannelSaveExecutionPolicy>>, TError, {policyKey: string; data: AdmChannelPolicySaveRequest}, TContext> => {
  return useMutation(getAdmChannelSaveExecutionPolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admChannelSaveExecutionPolicy


// CPF PRE-RUNTIME FALLBACK START admChannelRefreshSnapshot
export type admChannelRefreshSnapshotResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelRefreshSnapshotResponseSuccess = (admChannelRefreshSnapshotResponse200) & {
  headers: Headers;
};

export type admChannelRefreshSnapshotResponse = (admChannelRefreshSnapshotResponseSuccess)

export const getAdmChannelRefreshSnapshotUrl = () => `/adm/api/channels/refresh`;

export const admChannelRefreshSnapshot = async (params: AdmChannelRefreshSnapshotParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelRefreshSnapshotResponse> => {
  return cpfOrvalRequest<admChannelRefreshSnapshotResponse>(getAdmChannelRefreshSnapshotUrl(), {
    ...options,
    method: 'POST',
    params,
  });
};

export const getAdmChannelRefreshSnapshotMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>, TError, {params: AdmChannelRefreshSnapshotParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>, TError, {params: AdmChannelRefreshSnapshotParams}, TContext> => {
  const mutationKey = ['admChannelRefreshSnapshot'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>, void> = () => {

    return admChannelRefreshSnapshot(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmChannelRefreshSnapshotMutationResult = NonNullable<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>>;
export type AdmChannelRefreshSnapshotMutationParams = AdmChannelRefreshSnapshotParams;
export type AdmChannelRefreshSnapshotMutationBody = never;
export type AdmChannelRefreshSnapshotMutationError = unknown;

export const useAdmChannelRefreshSnapshot = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admChannelRefreshSnapshot>>, TError, void, TContext> => {
  return useMutation(getAdmChannelRefreshSnapshotMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admChannelRefreshSnapshot


// CPF PRE-RUNTIME FALLBACK START admChannelSave
export type admChannelSaveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admChannelSaveResponseSuccess = (admChannelSaveResponse200) & {
  headers: Headers;
};

export type admChannelSaveResponse = (admChannelSaveResponseSuccess)

export const getAdmChannelSaveUrl = (channelCode: string) => `/adm/api/channels/${encodeURIComponent(String(channelCode))}`;

export const admChannelSave = async (channelCode: string, data: AdmChannelSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admChannelSaveResponse> => {
  return cpfOrvalRequest<admChannelSaveResponse>(getAdmChannelSaveUrl(channelCode), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmChannelSaveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelSave>>, TError, {channelCode: string; data: AdmChannelSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admChannelSave>>, TError, {channelCode: string; data: AdmChannelSaveRequest}, TContext> => {
  const mutationKey = ['admChannelSave'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admChannelSave>>, {channelCode: string; data: AdmChannelSaveRequest}> = (props) => {
    const { channelCode, data } = props;
    return admChannelSave(channelCode, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmChannelSaveMutationResult = NonNullable<Awaited<ReturnType<typeof admChannelSave>>>;
export type AdmChannelSaveMutationBody = AdmChannelSaveRequest;
export type AdmChannelSaveMutationError = unknown;

export const useAdmChannelSave = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admChannelSave>>, TError, {channelCode: string; data: AdmChannelSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admChannelSave>>, TError, {channelCode: string; data: AdmChannelSaveRequest}, TContext> => {
  return useMutation(getAdmChannelSaveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admChannelSave


// CPF PRE-RUNTIME FALLBACK START admCodeFindCodes
export type admCodeFindCodesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCodeFindCodesResponseSuccess = (admCodeFindCodesResponse200) & {
  headers: Headers;
};

export type admCodeFindCodesResponse = (admCodeFindCodesResponseSuccess)

export const getAdmCodeFindCodesUrl = () => `/adm/api/codes`;

export const admCodeFindCodes = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admCodeFindCodesResponse> => {
  return cpfOrvalRequest<admCodeFindCodesResponse>(getAdmCodeFindCodesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCodeFindCodesQueryKey = () => ["adm", "api", "codes"] as const;

export const getAdmCodeFindCodesQueryOptions = <TData = Awaited<ReturnType<typeof admCodeFindCodes>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCodes>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCodeFindCodesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCodeFindCodes>>> = ({ signal }) => admCodeFindCodes({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCodes>>, TError, TData>;
};

export type AdmCodeFindCodesQueryResult = NonNullable<Awaited<ReturnType<typeof admCodeFindCodes>>>;
export type AdmCodeFindCodesQueryError = unknown;

export function useAdmCodeFindCodes<TData = Awaited<ReturnType<typeof admCodeFindCodes>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCodes>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCodeFindCodesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCodeFindCodes


// CPF PRE-RUNTIME FALLBACK START admCodeCreateCode
export type admCodeCreateCodeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCodeCreateCodeResponseSuccess = (admCodeCreateCodeResponse200) & {
  headers: Headers;
};

export type admCodeCreateCodeResponse = (admCodeCreateCodeResponseSuccess)

export const getAdmCodeCreateCodeUrl = () => `/adm/api/codes`;

export const admCodeCreateCode = async (data: CommonCodeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCodeCreateCodeResponse> => {
  return cpfOrvalRequest<admCodeCreateCodeResponse>(getAdmCodeCreateCodeUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCodeCreateCodeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeCreateCode>>, TError, {data: CommonCodeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCodeCreateCode>>, TError, {data: CommonCodeRequest}, TContext> => {
  const mutationKey = ['admCodeCreateCode'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCodeCreateCode>>, {data: CommonCodeRequest}> = (props) => {
    const { data } = props;
    return admCodeCreateCode(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCodeCreateCodeMutationResult = NonNullable<Awaited<ReturnType<typeof admCodeCreateCode>>>;
export type AdmCodeCreateCodeMutationBody = CommonCodeRequest;
export type AdmCodeCreateCodeMutationError = unknown;

export const useAdmCodeCreateCode = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeCreateCode>>, TError, {data: CommonCodeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCodeCreateCode>>, TError, {data: CommonCodeRequest}, TContext> => {
  return useMutation(getAdmCodeCreateCodeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCodeCreateCode


// CPF PRE-RUNTIME FALLBACK START admCodeDeleteCode
export type admCodeDeleteCodeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCodeDeleteCodeResponseSuccess = (admCodeDeleteCodeResponse200) & {
  headers: Headers;
};

export type admCodeDeleteCodeResponse = (admCodeDeleteCodeResponseSuccess)

export const getAdmCodeDeleteCodeUrl = (codeId: number) => `/adm/api/codes/${encodeURIComponent(String(codeId))}`;

export const admCodeDeleteCode = async (codeId: number, params: AdmCodeDeleteCodeParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admCodeDeleteCodeResponse> => {
  return cpfOrvalRequest<admCodeDeleteCodeResponse>(getAdmCodeDeleteCodeUrl(codeId), {
    ...options,
    method: 'DELETE',
    params: { reason: params.reason },
  });
};

export const getAdmCodeDeleteCodeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeDeleteCode>>, TError, {codeId: number; params: AdmCodeDeleteCodeParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCodeDeleteCode>>, TError, {codeId: number; params: AdmCodeDeleteCodeParams}, TContext> => {
  const mutationKey = ['admCodeDeleteCode'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCodeDeleteCode>>, {codeId: number; params: AdmCodeDeleteCodeParams}> = (props) => {
    const { codeId, params } = props;
    return admCodeDeleteCode(codeId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCodeDeleteCodeMutationResult = NonNullable<Awaited<ReturnType<typeof admCodeDeleteCode>>>;
export type AdmCodeDeleteCodeMutationBody = never;
export type AdmCodeDeleteCodeMutationError = unknown;

export const useAdmCodeDeleteCode = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeDeleteCode>>, TError, {codeId: number; params: AdmCodeDeleteCodeParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCodeDeleteCode>>, TError, {codeId: number; params: AdmCodeDeleteCodeParams}, TContext> => {
  return useMutation(getAdmCodeDeleteCodeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCodeDeleteCode


// CPF PRE-RUNTIME FALLBACK START admCodeFindCode
export type admCodeFindCodeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCodeFindCodeResponseSuccess = (admCodeFindCodeResponse200) & {
  headers: Headers;
};

export type admCodeFindCodeResponse = (admCodeFindCodeResponseSuccess)

export const getAdmCodeFindCodeUrl = (codeId: number) => `/adm/api/codes/${encodeURIComponent(String(codeId))}`;

export const admCodeFindCode = async (codeId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admCodeFindCodeResponse> => {
  return cpfOrvalRequest<admCodeFindCodeResponse>(getAdmCodeFindCodeUrl(codeId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmCodeFindCodeQueryKey = (codeId: MaybeRefOrGetter<number>) => ["adm", "api", "codes", codeId] as const;

export const getAdmCodeFindCodeQueryOptions = <TData = Awaited<ReturnType<typeof admCodeFindCode>>, TError = unknown>(
  codeId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCode>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmCodeFindCodeQueryKey(toValue(codeId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admCodeFindCode>>> = ({ signal }) => admCodeFindCode(toValue(codeId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(codeId) !== null && toValue(codeId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCode>>, TError, TData>;
};

export type AdmCodeFindCodeQueryResult = NonNullable<Awaited<ReturnType<typeof admCodeFindCode>>>;
export type AdmCodeFindCodeQueryError = unknown;

export function useAdmCodeFindCode<TData = Awaited<ReturnType<typeof admCodeFindCode>>, TError = unknown>(
  codeId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admCodeFindCode>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmCodeFindCodeQueryOptions(codeId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admCodeFindCode


// CPF PRE-RUNTIME FALLBACK START admCodeUpdateCode
export type admCodeUpdateCodeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCodeUpdateCodeResponseSuccess = (admCodeUpdateCodeResponse200) & {
  headers: Headers;
};

export type admCodeUpdateCodeResponse = (admCodeUpdateCodeResponseSuccess)

export const getAdmCodeUpdateCodeUrl = (codeId: number) => `/adm/api/codes/${encodeURIComponent(String(codeId))}`;

export const admCodeUpdateCode = async (codeId: number, data: CommonCodeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCodeUpdateCodeResponse> => {
  return cpfOrvalRequest<admCodeUpdateCodeResponse>(getAdmCodeUpdateCodeUrl(codeId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCodeUpdateCodeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeUpdateCode>>, TError, {codeId: number; data: CommonCodeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCodeUpdateCode>>, TError, {codeId: number; data: CommonCodeRequest}, TContext> => {
  const mutationKey = ['admCodeUpdateCode'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCodeUpdateCode>>, {codeId: number; data: CommonCodeRequest}> = (props) => {
    const { codeId, data } = props;
    return admCodeUpdateCode(codeId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCodeUpdateCodeMutationResult = NonNullable<Awaited<ReturnType<typeof admCodeUpdateCode>>>;
export type AdmCodeUpdateCodeMutationBody = CommonCodeRequest;
export type AdmCodeUpdateCodeMutationError = unknown;

export const useAdmCodeUpdateCode = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCodeUpdateCode>>, TError, {codeId: number; data: CommonCodeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCodeUpdateCode>>, TError, {codeId: number; data: CommonCodeRequest}, TContext> => {
  return useMutation(getAdmCodeUpdateCodeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCodeUpdateCode


// CPF PRE-RUNTIME FALLBACK START admConfigFindConfigs
export type admConfigFindConfigsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admConfigFindConfigsResponseSuccess = (admConfigFindConfigsResponse200) & {
  headers: Headers;
};

export type admConfigFindConfigsResponse = (admConfigFindConfigsResponseSuccess)

export const getAdmConfigFindConfigsUrl = () => `/adm/api/configs`;

export const admConfigFindConfigs = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admConfigFindConfigsResponse> => {
  return cpfOrvalRequest<admConfigFindConfigsResponse>(getAdmConfigFindConfigsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmConfigFindConfigsQueryKey = () => ["adm", "api", "configs"] as const;

export const getAdmConfigFindConfigsQueryOptions = <TData = Awaited<ReturnType<typeof admConfigFindConfigs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfigs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmConfigFindConfigsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admConfigFindConfigs>>> = ({ signal }) => admConfigFindConfigs({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfigs>>, TError, TData>;
};

export type AdmConfigFindConfigsQueryResult = NonNullable<Awaited<ReturnType<typeof admConfigFindConfigs>>>;
export type AdmConfigFindConfigsQueryError = unknown;

export function useAdmConfigFindConfigs<TData = Awaited<ReturnType<typeof admConfigFindConfigs>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfigs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmConfigFindConfigsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admConfigFindConfigs


// CPF PRE-RUNTIME FALLBACK START admConfigCreateConfig
export type admConfigCreateConfigResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admConfigCreateConfigResponseSuccess = (admConfigCreateConfigResponse200) & {
  headers: Headers;
};

export type admConfigCreateConfigResponse = (admConfigCreateConfigResponseSuccess)

export const getAdmConfigCreateConfigUrl = () => `/adm/api/configs`;

export const admConfigCreateConfig = async (data: CommonConfigRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admConfigCreateConfigResponse> => {
  return cpfOrvalRequest<admConfigCreateConfigResponse>(getAdmConfigCreateConfigUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmConfigCreateConfigMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigCreateConfig>>, TError, {data: CommonConfigRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admConfigCreateConfig>>, TError, {data: CommonConfigRequest}, TContext> => {
  const mutationKey = ['admConfigCreateConfig'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admConfigCreateConfig>>, {data: CommonConfigRequest}> = (props) => {
    const { data } = props;
    return admConfigCreateConfig(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmConfigCreateConfigMutationResult = NonNullable<Awaited<ReturnType<typeof admConfigCreateConfig>>>;
export type AdmConfigCreateConfigMutationBody = CommonConfigRequest;
export type AdmConfigCreateConfigMutationError = unknown;

export const useAdmConfigCreateConfig = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigCreateConfig>>, TError, {data: CommonConfigRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admConfigCreateConfig>>, TError, {data: CommonConfigRequest}, TContext> => {
  return useMutation(getAdmConfigCreateConfigMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admConfigCreateConfig


// CPF PRE-RUNTIME FALLBACK START admConfigDeleteConfig
export type admConfigDeleteConfigResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admConfigDeleteConfigResponseSuccess = (admConfigDeleteConfigResponse200) & {
  headers: Headers;
};

export type admConfigDeleteConfigResponse = (admConfigDeleteConfigResponseSuccess)

export const getAdmConfigDeleteConfigUrl = (configId: number) => `/adm/api/configs/${encodeURIComponent(String(configId))}`;

export const admConfigDeleteConfig = async (configId: number, params: AdmConfigDeleteConfigParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admConfigDeleteConfigResponse> => {
  return cpfOrvalRequest<admConfigDeleteConfigResponse>(getAdmConfigDeleteConfigUrl(configId), {
    ...options,
    method: 'DELETE',
    params: { reason: params.reason },
  });
};

export const getAdmConfigDeleteConfigMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigDeleteConfig>>, TError, {configId: number; params: AdmConfigDeleteConfigParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admConfigDeleteConfig>>, TError, {configId: number; params: AdmConfigDeleteConfigParams}, TContext> => {
  const mutationKey = ['admConfigDeleteConfig'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admConfigDeleteConfig>>, {configId: number; params: AdmConfigDeleteConfigParams}> = (props) => {
    const { configId, params } = props;
    return admConfigDeleteConfig(configId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmConfigDeleteConfigMutationResult = NonNullable<Awaited<ReturnType<typeof admConfigDeleteConfig>>>;
export type AdmConfigDeleteConfigMutationBody = never;
export type AdmConfigDeleteConfigMutationError = unknown;

export const useAdmConfigDeleteConfig = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigDeleteConfig>>, TError, {configId: number; params: AdmConfigDeleteConfigParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admConfigDeleteConfig>>, TError, {configId: number; params: AdmConfigDeleteConfigParams}, TContext> => {
  return useMutation(getAdmConfigDeleteConfigMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admConfigDeleteConfig


// CPF PRE-RUNTIME FALLBACK START admConfigFindConfig
export type admConfigFindConfigResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admConfigFindConfigResponseSuccess = (admConfigFindConfigResponse200) & {
  headers: Headers;
};

export type admConfigFindConfigResponse = (admConfigFindConfigResponseSuccess)

export const getAdmConfigFindConfigUrl = (configId: number) => `/adm/api/configs/${encodeURIComponent(String(configId))}`;

export const admConfigFindConfig = async (configId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admConfigFindConfigResponse> => {
  return cpfOrvalRequest<admConfigFindConfigResponse>(getAdmConfigFindConfigUrl(configId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmConfigFindConfigQueryKey = (configId: MaybeRefOrGetter<number>) => ["adm", "api", "configs", configId] as const;

export const getAdmConfigFindConfigQueryOptions = <TData = Awaited<ReturnType<typeof admConfigFindConfig>>, TError = unknown>(
  configId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfig>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmConfigFindConfigQueryKey(toValue(configId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admConfigFindConfig>>> = ({ signal }) => admConfigFindConfig(toValue(configId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(configId) !== null && toValue(configId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfig>>, TError, TData>;
};

export type AdmConfigFindConfigQueryResult = NonNullable<Awaited<ReturnType<typeof admConfigFindConfig>>>;
export type AdmConfigFindConfigQueryError = unknown;

export function useAdmConfigFindConfig<TData = Awaited<ReturnType<typeof admConfigFindConfig>>, TError = unknown>(
  configId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admConfigFindConfig>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmConfigFindConfigQueryOptions(configId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admConfigFindConfig


// CPF PRE-RUNTIME FALLBACK START admConfigUpdateConfig
export type admConfigUpdateConfigResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admConfigUpdateConfigResponseSuccess = (admConfigUpdateConfigResponse200) & {
  headers: Headers;
};

export type admConfigUpdateConfigResponse = (admConfigUpdateConfigResponseSuccess)

export const getAdmConfigUpdateConfigUrl = (configId: number) => `/adm/api/configs/${encodeURIComponent(String(configId))}`;

export const admConfigUpdateConfig = async (configId: number, data: CommonConfigRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admConfigUpdateConfigResponse> => {
  return cpfOrvalRequest<admConfigUpdateConfigResponse>(getAdmConfigUpdateConfigUrl(configId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmConfigUpdateConfigMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigUpdateConfig>>, TError, {configId: number; data: CommonConfigRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admConfigUpdateConfig>>, TError, {configId: number; data: CommonConfigRequest}, TContext> => {
  const mutationKey = ['admConfigUpdateConfig'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admConfigUpdateConfig>>, {configId: number; data: CommonConfigRequest}> = (props) => {
    const { configId, data } = props;
    return admConfigUpdateConfig(configId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmConfigUpdateConfigMutationResult = NonNullable<Awaited<ReturnType<typeof admConfigUpdateConfig>>>;
export type AdmConfigUpdateConfigMutationBody = CommonConfigRequest;
export type AdmConfigUpdateConfigMutationError = unknown;

export const useAdmConfigUpdateConfig = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admConfigUpdateConfig>>, TError, {configId: number; data: CommonConfigRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admConfigUpdateConfig>>, TError, {configId: number; data: CommonConfigRequest}, TContext> => {
  return useMutation(getAdmConfigUpdateConfigMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admConfigUpdateConfig


// CPF PRE-RUNTIME FALLBACK START admDownloadFindDownloadAuditLogs
export type admDownloadFindDownloadAuditLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDownloadFindDownloadAuditLogsResponseSuccess = (admDownloadFindDownloadAuditLogsResponse200) & {
  headers: Headers;
};

export type admDownloadFindDownloadAuditLogsResponse = (admDownloadFindDownloadAuditLogsResponseSuccess)

export const getAdmDownloadFindDownloadAuditLogsUrl = () => `/adm/api/downloads/audit-logs`;

export const admDownloadFindDownloadAuditLogs = async (params?: AdmDownloadFindDownloadAuditLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admDownloadFindDownloadAuditLogsResponse> => {
  return cpfOrvalRequest<admDownloadFindDownloadAuditLogsResponse>(getAdmDownloadFindDownloadAuditLogsUrl(), {
    ...options,
    method: 'GET',
    params: { downloadType: params?.downloadType, adminId: params?.adminId, limit: params?.limit },
  });
};

export const getAdmDownloadFindDownloadAuditLogsQueryKey = (params?: MaybeRefOrGetter<AdmDownloadFindDownloadAuditLogsParams>) => ["adm", "api", "downloads", "audit-logs", toValue(params)] as const;

export const getAdmDownloadFindDownloadAuditLogsQueryOptions = <TData = Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmDownloadFindDownloadAuditLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmDownloadFindDownloadAuditLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>> = ({ signal }) => admDownloadFindDownloadAuditLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>, TError, TData>;
};

export type AdmDownloadFindDownloadAuditLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>>;
export type AdmDownloadFindDownloadAuditLogsQueryError = unknown;

export function useAdmDownloadFindDownloadAuditLogs<TData = Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmDownloadFindDownloadAuditLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindDownloadAuditLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmDownloadFindDownloadAuditLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admDownloadFindDownloadAuditLogs


// CPF PRE-RUNTIME FALLBACK START admDownloadDownloadCsv
export type admDownloadDownloadCsvResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDownloadDownloadCsvResponseSuccess = (admDownloadDownloadCsvResponse200) & {
  headers: Headers;
};

export type admDownloadDownloadCsvResponse = (admDownloadDownloadCsvResponseSuccess)

export const getAdmDownloadDownloadCsvUrl = () => `/adm/api/downloads/csv`;

export const admDownloadDownloadCsv = async (data: DownloadRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admDownloadDownloadCsvResponse> => {
  return cpfOrvalRequest<admDownloadDownloadCsvResponse>(getAdmDownloadDownloadCsvUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmDownloadDownloadCsvMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDownloadDownloadCsv>>, TError, {data: DownloadRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admDownloadDownloadCsv>>, TError, {data: DownloadRequest}, TContext> => {
  const mutationKey = ['admDownloadDownloadCsv'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admDownloadDownloadCsv>>, {data: DownloadRequest}> = (props) => {
    const { data } = props;
    return admDownloadDownloadCsv(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmDownloadDownloadCsvMutationResult = NonNullable<Awaited<ReturnType<typeof admDownloadDownloadCsv>>>;
export type AdmDownloadDownloadCsvMutationBody = DownloadRequest;
export type AdmDownloadDownloadCsvMutationError = unknown;

export const useAdmDownloadDownloadCsv = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDownloadDownloadCsv>>, TError, {data: DownloadRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admDownloadDownloadCsv>>, TError, {data: DownloadRequest}, TContext> => {
  return useMutation(getAdmDownloadDownloadCsvMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admDownloadDownloadCsv


// CPF PRE-RUNTIME FALLBACK START admDownloadFindPolicies
export type admDownloadFindPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDownloadFindPoliciesResponseSuccess = (admDownloadFindPoliciesResponse200) & {
  headers: Headers;
};

export type admDownloadFindPoliciesResponse = (admDownloadFindPoliciesResponseSuccess)

export const getAdmDownloadFindPoliciesUrl = () => `/adm/api/downloads/policies`;

export const admDownloadFindPolicies = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admDownloadFindPoliciesResponse> => {
  return cpfOrvalRequest<admDownloadFindPoliciesResponse>(getAdmDownloadFindPoliciesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmDownloadFindPoliciesQueryKey = () => ["adm", "api", "downloads", "policies"] as const;

export const getAdmDownloadFindPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof admDownloadFindPolicies>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmDownloadFindPoliciesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admDownloadFindPolicies>>> = ({ signal }) => admDownloadFindPolicies({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindPolicies>>, TError, TData>;
};

export type AdmDownloadFindPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof admDownloadFindPolicies>>>;
export type AdmDownloadFindPoliciesQueryError = unknown;

export function useAdmDownloadFindPolicies<TData = Awaited<ReturnType<typeof admDownloadFindPolicies>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDownloadFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmDownloadFindPoliciesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admDownloadFindPolicies


// CPF PRE-RUNTIME FALLBACK START admFileJobList
export type admFileJobListResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobListResponseSuccess = (admFileJobListResponse200) & {
  headers: Headers;
};

export type admFileJobListResponse = (admFileJobListResponseSuccess)

export const getAdmFileJobListUrl = () => `/adm/api/file-jobs`;

export const admFileJobList = async (params?: AdmFileJobListParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobListResponse> => {
  return cpfOrvalRequest<admFileJobListResponse>(getAdmFileJobListUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmFileJobListQueryKey = (params?: MaybeRefOrGetter<AdmFileJobListParams>) => ["adm", "api", "file-jobs", toValue(params)] as const;

export const getAdmFileJobListQueryOptions = <TData = Awaited<ReturnType<typeof admFileJobList>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmFileJobListParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobList>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFileJobListQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFileJobList>>> = ({ signal }) => admFileJobList(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFileJobList>>, TError, TData>;
};

export type AdmFileJobListQueryResult = NonNullable<Awaited<ReturnType<typeof admFileJobList>>>;
export type AdmFileJobListQueryError = unknown;

export function useAdmFileJobList<TData = Awaited<ReturnType<typeof admFileJobList>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmFileJobListParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobList>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFileJobListQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFileJobList


// CPF PRE-RUNTIME FALLBACK START admFileJobUpload
export type admFileJobUploadResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobUploadResponseSuccess = (admFileJobUploadResponse200) & {
  headers: Headers;
};

export type admFileJobUploadResponse = (admFileJobUploadResponseSuccess)

export const getAdmFileJobUploadUrl = () => `/adm/api/file-jobs/uploads`;

export const admFileJobUpload = async (params: AdmFileJobUploadParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobUploadResponse> => {
  return cpfOrvalRequest<admFileJobUploadResponse>(getAdmFileJobUploadUrl(), {
    ...options,
    method: 'POST',
    params: { operationId: params.operationId, templateCode: params.templateCode, templateVersion: params.templateVersion, format: params.format, dryRun: params.dryRun, reason: params.reason },
  });
};

export const getAdmFileJobUploadMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobUpload>>, TError, {params: AdmFileJobUploadParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobUpload>>, TError, {params: AdmFileJobUploadParams}, TContext> => {
  const mutationKey = ['admFileJobUpload'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobUpload>>, {params: AdmFileJobUploadParams}> = (props) => {
    const { params } = props;
    return admFileJobUpload(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobUploadMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobUpload>>>;
export type AdmFileJobUploadMutationBody = never;
export type AdmFileJobUploadMutationError = unknown;

export const useAdmFileJobUpload = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobUpload>>, TError, {params: AdmFileJobUploadParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobUpload>>, TError, {params: AdmFileJobUploadParams}, TContext> => {
  return useMutation(getAdmFileJobUploadMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobUpload


// CPF PRE-RUNTIME FALLBACK START admFileJobDetail
export type admFileJobDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobDetailResponseSuccess = (admFileJobDetailResponse200) & {
  headers: Headers;
};

export type admFileJobDetailResponse = (admFileJobDetailResponseSuccess)

export const getAdmFileJobDetailUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}`;

export const admFileJobDetail = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobDetailResponse> => {
  return cpfOrvalRequest<admFileJobDetailResponse>(getAdmFileJobDetailUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmFileJobDetailQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "file-jobs", jobId] as const;

export const getAdmFileJobDetailQueryOptions = <TData = Awaited<ReturnType<typeof admFileJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFileJobDetailQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFileJobDetail>>> = ({ signal }) => admFileJobDetail(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFileJobDetail>>, TError, TData>;
};

export type AdmFileJobDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admFileJobDetail>>>;
export type AdmFileJobDetailQueryError = unknown;

export function useAdmFileJobDetail<TData = Awaited<ReturnType<typeof admFileJobDetail>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFileJobDetailQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFileJobDetail


// CPF PRE-RUNTIME FALLBACK START admFileJobApply
export type admFileJobApplyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobApplyResponseSuccess = (admFileJobApplyResponse200) & {
  headers: Headers;
};

export type admFileJobApplyResponse = (admFileJobApplyResponseSuccess)

export const getAdmFileJobApplyUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/apply`;

export const admFileJobApply = async (jobId: string, data: AdmFileJobApplyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobApplyResponse> => {
  return cpfOrvalRequest<admFileJobApplyResponse>(getAdmFileJobApplyUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFileJobApplyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobApply>>, TError, {jobId: string; data: AdmFileJobApplyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobApply>>, TError, {jobId: string; data: AdmFileJobApplyRequest}, TContext> => {
  const mutationKey = ['admFileJobApply'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobApply>>, {jobId: string; data: AdmFileJobApplyRequest}> = (props) => {
    const { jobId, data } = props;
    return admFileJobApply(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobApplyMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobApply>>>;
export type AdmFileJobApplyMutationBody = AdmFileJobApplyRequest;
export type AdmFileJobApplyMutationError = unknown;

export const useAdmFileJobApply = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobApply>>, TError, {jobId: string; data: AdmFileJobApplyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobApply>>, TError, {jobId: string; data: AdmFileJobApplyRequest}, TContext> => {
  return useMutation(getAdmFileJobApplyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobApply


// CPF PRE-RUNTIME FALLBACK START admFileJobArtifact
export type admFileJobArtifactResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobArtifactResponseSuccess = (admFileJobArtifactResponse200) & {
  headers: Headers;
};

export type admFileJobArtifactResponse = (admFileJobArtifactResponseSuccess)

export const getAdmFileJobArtifactUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/artifact`;

export const admFileJobArtifact = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobArtifactResponse> => {
  return cpfOrvalRequest<admFileJobArtifactResponse>(getAdmFileJobArtifactUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmFileJobArtifactQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "file-jobs", jobId, "artifact"] as const;

export const getAdmFileJobArtifactQueryOptions = <TData = Awaited<ReturnType<typeof admFileJobArtifact>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobArtifact>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFileJobArtifactQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFileJobArtifact>>> = ({ signal }) => admFileJobArtifact(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFileJobArtifact>>, TError, TData>;
};

export type AdmFileJobArtifactQueryResult = NonNullable<Awaited<ReturnType<typeof admFileJobArtifact>>>;
export type AdmFileJobArtifactQueryError = unknown;

export function useAdmFileJobArtifact<TData = Awaited<ReturnType<typeof admFileJobArtifact>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobArtifact>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFileJobArtifactQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFileJobArtifact


// CPF PRE-RUNTIME FALLBACK START admFileJobCancel
export type admFileJobCancelResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobCancelResponseSuccess = (admFileJobCancelResponse200) & {
  headers: Headers;
};

export type admFileJobCancelResponse = (admFileJobCancelResponseSuccess)

export const getAdmFileJobCancelUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/cancel`;

export const admFileJobCancel = async (jobId: string, data: AdmFileJobCancelRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobCancelResponse> => {
  return cpfOrvalRequest<admFileJobCancelResponse>(getAdmFileJobCancelUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFileJobCancelMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobCancel>>, TError, {jobId: string; data: AdmFileJobCancelRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobCancel>>, TError, {jobId: string; data: AdmFileJobCancelRequest}, TContext> => {
  const mutationKey = ['admFileJobCancel'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobCancel>>, {jobId: string; data: AdmFileJobCancelRequest}> = (props) => {
    const { jobId, data } = props;
    return admFileJobCancel(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobCancelMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobCancel>>>;
export type AdmFileJobCancelMutationBody = AdmFileJobCancelRequest;
export type AdmFileJobCancelMutationError = unknown;

export const useAdmFileJobCancel = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobCancel>>, TError, {jobId: string; data: AdmFileJobCancelRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobCancel>>, TError, {jobId: string; data: AdmFileJobCancelRequest}, TContext> => {
  return useMutation(getAdmFileJobCancelMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobCancel


// CPF PRE-RUNTIME FALLBACK START admFileJobResolveUnknown
export type admFileJobResolveUnknownResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobResolveUnknownResponseSuccess = (admFileJobResolveUnknownResponse200) & {
  headers: Headers;
};

export type admFileJobResolveUnknownResponse = (admFileJobResolveUnknownResponseSuccess)

export const getAdmFileJobResolveUnknownUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/resolve-unknown`;

export const admFileJobResolveUnknown = async (jobId: string, data: AdmFileJobResolveUnknownRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobResolveUnknownResponse> => {
  return cpfOrvalRequest<admFileJobResolveUnknownResponse>(getAdmFileJobResolveUnknownUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFileJobResolveUnknownMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobResolveUnknown>>, TError, {jobId: string; data: AdmFileJobResolveUnknownRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobResolveUnknown>>, TError, {jobId: string; data: AdmFileJobResolveUnknownRequest}, TContext> => {
  const mutationKey = ['admFileJobResolveUnknown'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobResolveUnknown>>, {jobId: string; data: AdmFileJobResolveUnknownRequest}> = (props) => {
    const { jobId, data } = props;
    return admFileJobResolveUnknown(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobResolveUnknownMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobResolveUnknown>>>;
export type AdmFileJobResolveUnknownMutationBody = AdmFileJobResolveUnknownRequest;
export type AdmFileJobResolveUnknownMutationError = unknown;

export const useAdmFileJobResolveUnknown = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobResolveUnknown>>, TError, {jobId: string; data: AdmFileJobResolveUnknownRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobResolveUnknown>>, TError, {jobId: string; data: AdmFileJobResolveUnknownRequest}, TContext> => {
  return useMutation(getAdmFileJobResolveUnknownMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobResolveUnknown


// CPF PRE-RUNTIME FALLBACK START admFileJobRetry
export type admFileJobRetryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobRetryResponseSuccess = (admFileJobRetryResponse200) & {
  headers: Headers;
};

export type admFileJobRetryResponse = (admFileJobRetryResponseSuccess)

export const getAdmFileJobRetryUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/retry`;

export const admFileJobRetry = async (jobId: string, data: AdmFileJobRetryRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobRetryResponse> => {
  return cpfOrvalRequest<admFileJobRetryResponse>(getAdmFileJobRetryUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFileJobRetryMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobRetry>>, TError, {jobId: string; data: AdmFileJobRetryRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobRetry>>, TError, {jobId: string; data: AdmFileJobRetryRequest}, TContext> => {
  const mutationKey = ['admFileJobRetry'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobRetry>>, {jobId: string; data: AdmFileJobRetryRequest}> = (props) => {
    const { jobId, data } = props;
    return admFileJobRetry(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobRetryMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobRetry>>>;
export type AdmFileJobRetryMutationBody = AdmFileJobRetryRequest;
export type AdmFileJobRetryMutationError = unknown;

export const useAdmFileJobRetry = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobRetry>>, TError, {jobId: string; data: AdmFileJobRetryRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobRetry>>, TError, {jobId: string; data: AdmFileJobRetryRequest}, TContext> => {
  return useMutation(getAdmFileJobRetryMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobRetry


// CPF PRE-RUNTIME FALLBACK START admFileJobRollback
export type admFileJobRollbackResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobRollbackResponseSuccess = (admFileJobRollbackResponse200) & {
  headers: Headers;
};

export type admFileJobRollbackResponse = (admFileJobRollbackResponseSuccess)

export const getAdmFileJobRollbackUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/rollback`;

export const admFileJobRollback = async (jobId: string, data: AdmFileJobRollbackRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobRollbackResponse> => {
  return cpfOrvalRequest<admFileJobRollbackResponse>(getAdmFileJobRollbackUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFileJobRollbackMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobRollback>>, TError, {jobId: string; data: AdmFileJobRollbackRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFileJobRollback>>, TError, {jobId: string; data: AdmFileJobRollbackRequest}, TContext> => {
  const mutationKey = ['admFileJobRollback'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFileJobRollback>>, {jobId: string; data: AdmFileJobRollbackRequest}> = (props) => {
    const { jobId, data } = props;
    return admFileJobRollback(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFileJobRollbackMutationResult = NonNullable<Awaited<ReturnType<typeof admFileJobRollback>>>;
export type AdmFileJobRollbackMutationBody = AdmFileJobRollbackRequest;
export type AdmFileJobRollbackMutationError = unknown;

export const useAdmFileJobRollback = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFileJobRollback>>, TError, {jobId: string; data: AdmFileJobRollbackRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFileJobRollback>>, TError, {jobId: string; data: AdmFileJobRollbackRequest}, TContext> => {
  return useMutation(getAdmFileJobRollbackMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFileJobRollback


// CPF PRE-RUNTIME FALLBACK START admFileJobRows
export type admFileJobRowsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFileJobRowsResponseSuccess = (admFileJobRowsResponse200) & {
  headers: Headers;
};

export type admFileJobRowsResponse = (admFileJobRowsResponseSuccess)

export const getAdmFileJobRowsUrl = (jobId: string) => `/adm/api/file-jobs/${encodeURIComponent(String(jobId))}/rows`;

export const admFileJobRows = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admFileJobRowsResponse> => {
  return cpfOrvalRequest<admFileJobRowsResponse>(getAdmFileJobRowsUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmFileJobRowsQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "file-jobs", jobId, "rows"] as const;

export const getAdmFileJobRowsQueryOptions = <TData = Awaited<ReturnType<typeof admFileJobRows>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobRows>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFileJobRowsQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFileJobRows>>> = ({ signal }) => admFileJobRows(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFileJobRows>>, TError, TData>;
};

export type AdmFileJobRowsQueryResult = NonNullable<Awaited<ReturnType<typeof admFileJobRows>>>;
export type AdmFileJobRowsQueryError = unknown;

export function useAdmFileJobRows<TData = Awaited<ReturnType<typeof admFileJobRows>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFileJobRows>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFileJobRowsQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFileJobRows


// CPF PRE-RUNTIME FALLBACK START admGatewayFindBindings
export type admGatewayFindBindingsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindBindingsResponseSuccess = (admGatewayFindBindingsResponse200) & {
  headers: Headers;
};

export type admGatewayFindBindingsResponse = (admGatewayFindBindingsResponseSuccess)

export const getAdmGatewayFindBindingsUrl = () => `/adm/api/gateway-registry/bindings`;

export const admGatewayFindBindings = async (params?: AdmGatewayFindBindingsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindBindingsResponse> => {
  return cpfOrvalRequest<admGatewayFindBindingsResponse>(getAdmGatewayFindBindingsUrl(), {
    ...options,
    method: 'GET',
    params: { environmentCode: params?.environmentCode, routeId: params?.routeId, status: params?.status, limit: params?.limit },
  });
};

export const getAdmGatewayFindBindingsQueryKey = (params?: MaybeRefOrGetter<AdmGatewayFindBindingsParams>) => ["adm", "api", "gateway-registry", "bindings", toValue(params)] as const;

export const getAdmGatewayFindBindingsQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindBindings>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayFindBindingsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindBindings>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindBindingsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindBindings>>> = ({ signal }) => admGatewayFindBindings(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindBindings>>, TError, TData>;
};

export type AdmGatewayFindBindingsQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindBindings>>>;
export type AdmGatewayFindBindingsQueryError = unknown;

export function useAdmGatewayFindBindings<TData = Awaited<ReturnType<typeof admGatewayFindBindings>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayFindBindingsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindBindings>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindBindingsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindBindings


// CPF PRE-RUNTIME FALLBACK START admGatewaySaveBinding
export type admGatewaySaveBindingResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewaySaveBindingResponseSuccess = (admGatewaySaveBindingResponse200) & {
  headers: Headers;
};

export type admGatewaySaveBindingResponse = (admGatewaySaveBindingResponseSuccess)

export const getAdmGatewaySaveBindingUrl = () => `/adm/api/gateway-registry/bindings`;

export const admGatewaySaveBinding = async (data: GatewayBindingCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewaySaveBindingResponse> => {
  return cpfOrvalRequest<admGatewaySaveBindingResponse>(getAdmGatewaySaveBindingUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewaySaveBindingMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveBinding>>, TError, {data: GatewayBindingCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveBinding>>, TError, {data: GatewayBindingCommand}, TContext> => {
  const mutationKey = ['admGatewaySaveBinding'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewaySaveBinding>>, {data: GatewayBindingCommand}> = (props) => {
    const { data } = props;
    return admGatewaySaveBinding(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewaySaveBindingMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewaySaveBinding>>>;
export type AdmGatewaySaveBindingMutationBody = GatewayBindingCommand;
export type AdmGatewaySaveBindingMutationError = unknown;

export const useAdmGatewaySaveBinding = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveBinding>>, TError, {data: GatewayBindingCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewaySaveBinding>>, TError, {data: GatewayBindingCommand}, TContext> => {
  return useMutation(getAdmGatewaySaveBindingMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewaySaveBinding


// CPF PRE-RUNTIME FALLBACK START admGatewayDeleteBinding
export type admGatewayDeleteBindingResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayDeleteBindingResponseSuccess = (admGatewayDeleteBindingResponse200) & {
  headers: Headers;
};

export type admGatewayDeleteBindingResponse = (admGatewayDeleteBindingResponseSuccess)

export const getAdmGatewayDeleteBindingUrl = (id: string) => `/adm/api/gateway-registry/bindings/${encodeURIComponent(String(id))}`;

export const admGatewayDeleteBinding = async (id: string, data: DeleteCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayDeleteBindingResponse> => {
  return cpfOrvalRequest<admGatewayDeleteBindingResponse>(getAdmGatewayDeleteBindingUrl(id), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayDeleteBindingMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteBinding>>, TError, {id: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteBinding>>, TError, {id: string; data: DeleteCommand}, TContext> => {
  const mutationKey = ['admGatewayDeleteBinding'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayDeleteBinding>>, {id: string; data: DeleteCommand}> = (props) => {
    const { id, data } = props;
    return admGatewayDeleteBinding(id, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayDeleteBindingMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayDeleteBinding>>>;
export type AdmGatewayDeleteBindingMutationBody = DeleteCommand;
export type AdmGatewayDeleteBindingMutationError = unknown;

export const useAdmGatewayDeleteBinding = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteBinding>>, TError, {id: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayDeleteBinding>>, TError, {id: string; data: DeleteCommand}, TContext> => {
  return useMutation(getAdmGatewayDeleteBindingMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayDeleteBinding


// CPF PRE-RUNTIME FALLBACK START admGatewayFindApplyStatus
export type admGatewayFindApplyStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindApplyStatusResponseSuccess = (admGatewayFindApplyStatusResponse200) & {
  headers: Headers;
};

export type admGatewayFindApplyStatusResponse = (admGatewayFindApplyStatusResponseSuccess)

export const getAdmGatewayFindApplyStatusUrl = (id: string) => `/adm/api/gateway-registry/bindings/${encodeURIComponent(String(id))}/apply-status`;

export const admGatewayFindApplyStatus = async (id: string, params?: AdmGatewayFindApplyStatusParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindApplyStatusResponse> => {
  return cpfOrvalRequest<admGatewayFindApplyStatusResponse>(getAdmGatewayFindApplyStatusUrl(id), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmGatewayFindApplyStatusQueryKey = (id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindApplyStatusParams>) => ["adm", "api", "gateway-registry", "bindings", id, "apply-status", toValue(params)] as const;

export const getAdmGatewayFindApplyStatusQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindApplyStatus>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindApplyStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindApplyStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindApplyStatusQueryKey(toValue(id), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindApplyStatus>>> = ({ signal }) => admGatewayFindApplyStatus(toValue(id), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(id) !== null && toValue(id) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindApplyStatus>>, TError, TData>;
};

export type AdmGatewayFindApplyStatusQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindApplyStatus>>>;
export type AdmGatewayFindApplyStatusQueryError = unknown;

export function useAdmGatewayFindApplyStatus<TData = Awaited<ReturnType<typeof admGatewayFindApplyStatus>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindApplyStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindApplyStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindApplyStatusQueryOptions(id, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindApplyStatus


// CPF PRE-RUNTIME FALLBACK START admGatewayFindConnectionTests
export type admGatewayFindConnectionTestsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindConnectionTestsResponseSuccess = (admGatewayFindConnectionTestsResponse200) & {
  headers: Headers;
};

export type admGatewayFindConnectionTestsResponse = (admGatewayFindConnectionTestsResponseSuccess)

export const getAdmGatewayFindConnectionTestsUrl = (id: string) => `/adm/api/gateway-registry/bindings/${encodeURIComponent(String(id))}/connection-tests`;

export const admGatewayFindConnectionTests = async (id: string, params?: AdmGatewayFindConnectionTestsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindConnectionTestsResponse> => {
  return cpfOrvalRequest<admGatewayFindConnectionTestsResponse>(getAdmGatewayFindConnectionTestsUrl(id), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmGatewayFindConnectionTestsQueryKey = (id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindConnectionTestsParams>) => ["adm", "api", "gateway-registry", "bindings", id, "connection-tests", toValue(params)] as const;

export const getAdmGatewayFindConnectionTestsQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindConnectionTests>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindConnectionTestsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTests>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindConnectionTestsQueryKey(toValue(id), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindConnectionTests>>> = ({ signal }) => admGatewayFindConnectionTests(toValue(id), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(id) !== null && toValue(id) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTests>>, TError, TData>;
};

export type AdmGatewayFindConnectionTestsQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindConnectionTests>>>;
export type AdmGatewayFindConnectionTestsQueryError = unknown;

export function useAdmGatewayFindConnectionTests<TData = Awaited<ReturnType<typeof admGatewayFindConnectionTests>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmGatewayFindConnectionTestsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTests>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindConnectionTestsQueryOptions(id, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindConnectionTests


// CPF PRE-RUNTIME FALLBACK START admGatewayRequestConnectionTest
export type admGatewayRequestConnectionTestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayRequestConnectionTestResponseSuccess = (admGatewayRequestConnectionTestResponse200) & {
  headers: Headers;
};

export type admGatewayRequestConnectionTestResponse = (admGatewayRequestConnectionTestResponseSuccess)

export const getAdmGatewayRequestConnectionTestUrl = (id: string) => `/adm/api/gateway-registry/bindings/${encodeURIComponent(String(id))}/connection-tests`;

export const admGatewayRequestConnectionTest = async (id: string, data: ConnectionTestRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayRequestConnectionTestResponse> => {
  return cpfOrvalRequest<admGatewayRequestConnectionTestResponse>(getAdmGatewayRequestConnectionTestUrl(id), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayRequestConnectionTestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>, TError, {id: string; data: ConnectionTestRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>, TError, {id: string; data: ConnectionTestRequest}, TContext> => {
  const mutationKey = ['admGatewayRequestConnectionTest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>, {id: string; data: ConnectionTestRequest}> = (props) => {
    const { id, data } = props;
    return admGatewayRequestConnectionTest(id, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayRequestConnectionTestMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>>;
export type AdmGatewayRequestConnectionTestMutationBody = ConnectionTestRequest;
export type AdmGatewayRequestConnectionTestMutationError = unknown;

export const useAdmGatewayRequestConnectionTest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>, TError, {id: string; data: ConnectionTestRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayRequestConnectionTest>>, TError, {id: string; data: ConnectionTestRequest}, TContext> => {
  return useMutation(getAdmGatewayRequestConnectionTestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayRequestConnectionTest


// CPF PRE-RUNTIME FALLBACK START admGatewayChangeBindingState
export type admGatewayChangeBindingStateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayChangeBindingStateResponseSuccess = (admGatewayChangeBindingStateResponse200) & {
  headers: Headers;
};

export type admGatewayChangeBindingStateResponse = (admGatewayChangeBindingStateResponseSuccess)

export const getAdmGatewayChangeBindingStateUrl = (id: string) => `/adm/api/gateway-registry/bindings/${encodeURIComponent(String(id))}/state`;

export const admGatewayChangeBindingState = async (id: string, data: BindingStateCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayChangeBindingStateResponse> => {
  return cpfOrvalRequest<admGatewayChangeBindingStateResponse>(getAdmGatewayChangeBindingStateUrl(id), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayChangeBindingStateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayChangeBindingState>>, TError, {id: string; data: BindingStateCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayChangeBindingState>>, TError, {id: string; data: BindingStateCommand}, TContext> => {
  const mutationKey = ['admGatewayChangeBindingState'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayChangeBindingState>>, {id: string; data: BindingStateCommand}> = (props) => {
    const { id, data } = props;
    return admGatewayChangeBindingState(id, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayChangeBindingStateMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayChangeBindingState>>>;
export type AdmGatewayChangeBindingStateMutationBody = BindingStateCommand;
export type AdmGatewayChangeBindingStateMutationError = unknown;

export const useAdmGatewayChangeBindingState = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayChangeBindingState>>, TError, {id: string; data: BindingStateCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayChangeBindingState>>, TError, {id: string; data: BindingStateCommand}, TContext> => {
  return useMutation(getAdmGatewayChangeBindingStateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayChangeBindingState


// CPF PRE-RUNTIME FALLBACK START admGatewayCapability
export type admGatewayCapabilityResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayCapabilityResponseSuccess = (admGatewayCapabilityResponse200) & {
  headers: Headers;
};

export type admGatewayCapabilityResponse = (admGatewayCapabilityResponseSuccess)

export const getAdmGatewayCapabilityUrl = () => `/adm/api/gateway-registry/capability`;

export const admGatewayCapability = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayCapabilityResponse> => {
  return cpfOrvalRequest<admGatewayCapabilityResponse>(getAdmGatewayCapabilityUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmGatewayCapabilityQueryKey = () => ["adm", "api", "gateway-registry", "capability"] as const;

export const getAdmGatewayCapabilityQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayCapability>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayCapability>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayCapabilityQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayCapability>>> = ({ signal }) => admGatewayCapability({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayCapability>>, TError, TData>;
};

export type AdmGatewayCapabilityQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayCapability>>>;
export type AdmGatewayCapabilityQueryError = unknown;

export function useAdmGatewayCapability<TData = Awaited<ReturnType<typeof admGatewayCapability>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayCapability>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayCapabilityQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayCapability


// CPF PRE-RUNTIME FALLBACK START admGatewayFindConnectionTestOperation
export type admGatewayFindConnectionTestOperationResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindConnectionTestOperationResponseSuccess = (admGatewayFindConnectionTestOperationResponse200) & {
  headers: Headers;
};

export type admGatewayFindConnectionTestOperationResponse = (admGatewayFindConnectionTestOperationResponseSuccess)

export const getAdmGatewayFindConnectionTestOperationUrl = (operationId: string) => `/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(String(operationId))}`;

export const admGatewayFindConnectionTestOperation = async (operationId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindConnectionTestOperationResponse> => {
  return cpfOrvalRequest<admGatewayFindConnectionTestOperationResponse>(getAdmGatewayFindConnectionTestOperationUrl(operationId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmGatewayFindConnectionTestOperationQueryKey = (operationId: MaybeRefOrGetter<string>) => ["adm", "api", "gateway-registry", "connection-test-operations", operationId] as const;

export const getAdmGatewayFindConnectionTestOperationQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindConnectionTestOperationQueryKey(toValue(operationId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>> = ({ signal }) => admGatewayFindConnectionTestOperation(toValue(operationId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(operationId) !== null && toValue(operationId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>, TError, TData>;
};

export type AdmGatewayFindConnectionTestOperationQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>>;
export type AdmGatewayFindConnectionTestOperationQueryError = unknown;

export function useAdmGatewayFindConnectionTestOperation<TData = Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindConnectionTestOperation>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindConnectionTestOperationQueryOptions(operationId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindConnectionTestOperation


// CPF PRE-RUNTIME FALLBACK START admGatewayCancelConnectionTest
export type admGatewayCancelConnectionTestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayCancelConnectionTestResponseSuccess = (admGatewayCancelConnectionTestResponse200) & {
  headers: Headers;
};

export type admGatewayCancelConnectionTestResponse = (admGatewayCancelConnectionTestResponseSuccess)

export const getAdmGatewayCancelConnectionTestUrl = (operationId: string) => `/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(String(operationId))}/cancel`;

export const admGatewayCancelConnectionTest = async (operationId: string, data: ConnectionTestCancel, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayCancelConnectionTestResponse> => {
  return cpfOrvalRequest<admGatewayCancelConnectionTestResponse>(getAdmGatewayCancelConnectionTestUrl(operationId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayCancelConnectionTestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>, TError, {operationId: string; data: ConnectionTestCancel}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>, TError, {operationId: string; data: ConnectionTestCancel}, TContext> => {
  const mutationKey = ['admGatewayCancelConnectionTest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>, {operationId: string; data: ConnectionTestCancel}> = (props) => {
    const { operationId, data } = props;
    return admGatewayCancelConnectionTest(operationId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayCancelConnectionTestMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>>;
export type AdmGatewayCancelConnectionTestMutationBody = ConnectionTestCancel;
export type AdmGatewayCancelConnectionTestMutationError = unknown;

export const useAdmGatewayCancelConnectionTest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>, TError, {operationId: string; data: ConnectionTestCancel}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayCancelConnectionTest>>, TError, {operationId: string; data: ConnectionTestCancel}, TContext> => {
  return useMutation(getAdmGatewayCancelConnectionTestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayCancelConnectionTest


// CPF PRE-RUNTIME FALLBACK START admGatewayRevalidateConnectionTest
export type admGatewayRevalidateConnectionTestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayRevalidateConnectionTestResponseSuccess = (admGatewayRevalidateConnectionTestResponse200) & {
  headers: Headers;
};

export type admGatewayRevalidateConnectionTestResponse = (admGatewayRevalidateConnectionTestResponseSuccess)

export const getAdmGatewayRevalidateConnectionTestUrl = (operationId: string) => `/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(String(operationId))}/revalidate`;

export const admGatewayRevalidateConnectionTest = async (operationId: string, data: ConnectionTestRevalidation, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayRevalidateConnectionTestResponse> => {
  return cpfOrvalRequest<admGatewayRevalidateConnectionTestResponse>(getAdmGatewayRevalidateConnectionTestUrl(operationId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayRevalidateConnectionTestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>, TError, {operationId: string; data: ConnectionTestRevalidation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>, TError, {operationId: string; data: ConnectionTestRevalidation}, TContext> => {
  const mutationKey = ['admGatewayRevalidateConnectionTest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>, {operationId: string; data: ConnectionTestRevalidation}> = (props) => {
    const { operationId, data } = props;
    return admGatewayRevalidateConnectionTest(operationId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayRevalidateConnectionTestMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>>;
export type AdmGatewayRevalidateConnectionTestMutationBody = ConnectionTestRevalidation;
export type AdmGatewayRevalidateConnectionTestMutationError = unknown;

export const useAdmGatewayRevalidateConnectionTest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>, TError, {operationId: string; data: ConnectionTestRevalidation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayRevalidateConnectionTest>>, TError, {operationId: string; data: ConnectionTestRevalidation}, TContext> => {
  return useMutation(getAdmGatewayRevalidateConnectionTestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayRevalidateConnectionTest


// CPF PRE-RUNTIME FALLBACK START admGatewayOperationsEvents
export type admGatewayOperationsEventsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayOperationsEventsResponseSuccess = (admGatewayOperationsEventsResponse200) & {
  headers: Headers;
};

export type admGatewayOperationsEventsResponse = (admGatewayOperationsEventsResponseSuccess)

export const getAdmGatewayOperationsEventsUrl = () => `/adm/api/gateway-registry/operations/events`;

export const admGatewayOperationsEvents = async (params?: AdmGatewayOperationsEventsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayOperationsEventsResponse> => {
  return cpfOrvalRequest<admGatewayOperationsEventsResponse>(getAdmGatewayOperationsEventsUrl(), {
    ...options,
    method: 'GET',
    params: { afterEventId: params?.afterEventId, limit: params?.limit },
  });
};

export const getAdmGatewayOperationsEventsQueryKey = (params?: MaybeRefOrGetter<AdmGatewayOperationsEventsParams>) => ["adm", "api", "gateway-registry", "operations", "events", toValue(params)] as const;

export const getAdmGatewayOperationsEventsQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayOperationsEvents>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayOperationsEventsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsEvents>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayOperationsEventsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayOperationsEvents>>> = ({ signal }) => admGatewayOperationsEvents(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsEvents>>, TError, TData>;
};

export type AdmGatewayOperationsEventsQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayOperationsEvents>>>;
export type AdmGatewayOperationsEventsQueryError = unknown;

export function useAdmGatewayOperationsEvents<TData = Awaited<ReturnType<typeof admGatewayOperationsEvents>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayOperationsEventsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsEvents>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayOperationsEventsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayOperationsEvents


// CPF PRE-RUNTIME FALLBACK START admGatewayOperationsSnapshot
export type admGatewayOperationsSnapshotResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayOperationsSnapshotResponseSuccess = (admGatewayOperationsSnapshotResponse200) & {
  headers: Headers;
};

export type admGatewayOperationsSnapshotResponse = (admGatewayOperationsSnapshotResponseSuccess)

export const getAdmGatewayOperationsSnapshotUrl = () => `/adm/api/gateway-registry/operations/snapshot`;

export const admGatewayOperationsSnapshot = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayOperationsSnapshotResponse> => {
  return cpfOrvalRequest<admGatewayOperationsSnapshotResponse>(getAdmGatewayOperationsSnapshotUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmGatewayOperationsSnapshotQueryKey = () => ["adm", "api", "gateway-registry", "operations", "snapshot"] as const;

export const getAdmGatewayOperationsSnapshotQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayOperationsSnapshotQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>> = ({ signal }) => admGatewayOperationsSnapshot({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>, TError, TData>;
};

export type AdmGatewayOperationsSnapshotQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>>;
export type AdmGatewayOperationsSnapshotQueryError = unknown;

export function useAdmGatewayOperationsSnapshot<TData = Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsSnapshot>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayOperationsSnapshotQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayOperationsSnapshot


// CPF PRE-RUNTIME FALLBACK START admGatewayOperationsStream
export type admGatewayOperationsStreamResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayOperationsStreamResponseSuccess = (admGatewayOperationsStreamResponse200) & {
  headers: Headers;
};

export type admGatewayOperationsStreamResponse = (admGatewayOperationsStreamResponseSuccess)

export const getAdmGatewayOperationsStreamUrl = () => `/adm/api/gateway-registry/operations/stream`;

export const admGatewayOperationsStream = async (params?: AdmGatewayOperationsStreamParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayOperationsStreamResponse> => {
  return cpfOrvalRequest<admGatewayOperationsStreamResponse>(getAdmGatewayOperationsStreamUrl(), {
    ...options,
    method: 'GET',
    params: { afterEventId: params?.afterEventId },
    headers: { ...(params?.["Last-Event-ID"] !== undefined ? { "Last-Event-ID": params?.["Last-Event-ID"] } : {}), ...options?.headers },
  });
};

export const getAdmGatewayOperationsStreamQueryKey = (params?: MaybeRefOrGetter<AdmGatewayOperationsStreamParams>) => ["adm", "api", "gateway-registry", "operations", "stream", toValue(params)] as const;

export const getAdmGatewayOperationsStreamQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayOperationsStream>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayOperationsStreamParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsStream>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayOperationsStreamQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayOperationsStream>>> = ({ signal }) => admGatewayOperationsStream(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsStream>>, TError, TData>;
};

export type AdmGatewayOperationsStreamQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayOperationsStream>>>;
export type AdmGatewayOperationsStreamQueryError = unknown;

export function useAdmGatewayOperationsStream<TData = Awaited<ReturnType<typeof admGatewayOperationsStream>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayOperationsStreamParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayOperationsStream>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayOperationsStreamQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayOperationsStream


// CPF PRE-RUNTIME FALLBACK START admGatewayFindServerGroups
export type admGatewayFindServerGroupsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindServerGroupsResponseSuccess = (admGatewayFindServerGroupsResponse200) & {
  headers: Headers;
};

export type admGatewayFindServerGroupsResponse = (admGatewayFindServerGroupsResponseSuccess)

export const getAdmGatewayFindServerGroupsUrl = () => `/adm/api/gateway-registry/server-groups`;

export const admGatewayFindServerGroups = async (params?: AdmGatewayFindServerGroupsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindServerGroupsResponse> => {
  return cpfOrvalRequest<admGatewayFindServerGroupsResponse>(getAdmGatewayFindServerGroupsUrl(), {
    ...options,
    method: 'GET',
    params: { environmentCode: params?.environmentCode, serviceId: params?.serviceId, status: params?.status, limit: params?.limit },
  });
};

export const getAdmGatewayFindServerGroupsQueryKey = (params?: MaybeRefOrGetter<AdmGatewayFindServerGroupsParams>) => ["adm", "api", "gateway-registry", "server-groups", toValue(params)] as const;

export const getAdmGatewayFindServerGroupsQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindServerGroups>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayFindServerGroupsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindServerGroups>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindServerGroupsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindServerGroups>>> = ({ signal }) => admGatewayFindServerGroups(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindServerGroups>>, TError, TData>;
};

export type AdmGatewayFindServerGroupsQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindServerGroups>>>;
export type AdmGatewayFindServerGroupsQueryError = unknown;

export function useAdmGatewayFindServerGroups<TData = Awaited<ReturnType<typeof admGatewayFindServerGroups>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmGatewayFindServerGroupsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindServerGroups>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindServerGroupsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindServerGroups


// CPF PRE-RUNTIME FALLBACK START admGatewaySaveServerGroup
export type admGatewaySaveServerGroupResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewaySaveServerGroupResponseSuccess = (admGatewaySaveServerGroupResponse200) & {
  headers: Headers;
};

export type admGatewaySaveServerGroupResponse = (admGatewaySaveServerGroupResponseSuccess)

export const getAdmGatewaySaveServerGroupUrl = () => `/adm/api/gateway-registry/server-groups`;

export const admGatewaySaveServerGroup = async (data: ServerGroupCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewaySaveServerGroupResponse> => {
  return cpfOrvalRequest<admGatewaySaveServerGroupResponse>(getAdmGatewaySaveServerGroupUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewaySaveServerGroupMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>, TError, {data: ServerGroupCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>, TError, {data: ServerGroupCommand}, TContext> => {
  const mutationKey = ['admGatewaySaveServerGroup'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>, {data: ServerGroupCommand}> = (props) => {
    const { data } = props;
    return admGatewaySaveServerGroup(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewaySaveServerGroupMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>>;
export type AdmGatewaySaveServerGroupMutationBody = ServerGroupCommand;
export type AdmGatewaySaveServerGroupMutationError = unknown;

export const useAdmGatewaySaveServerGroup = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>, TError, {data: ServerGroupCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewaySaveServerGroup>>, TError, {data: ServerGroupCommand}, TContext> => {
  return useMutation(getAdmGatewaySaveServerGroupMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewaySaveServerGroup


// CPF PRE-RUNTIME FALLBACK START admGatewayDeleteServerGroup
export type admGatewayDeleteServerGroupResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayDeleteServerGroupResponseSuccess = (admGatewayDeleteServerGroupResponse200) & {
  headers: Headers;
};

export type admGatewayDeleteServerGroupResponse = (admGatewayDeleteServerGroupResponseSuccess)

export const getAdmGatewayDeleteServerGroupUrl = (id: string) => `/adm/api/gateway-registry/server-groups/${encodeURIComponent(String(id))}`;

export const admGatewayDeleteServerGroup = async (id: string, data: DeleteCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayDeleteServerGroupResponse> => {
  return cpfOrvalRequest<admGatewayDeleteServerGroupResponse>(getAdmGatewayDeleteServerGroupUrl(id), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmGatewayDeleteServerGroupMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>, TError, {id: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>, TError, {id: string; data: DeleteCommand}, TContext> => {
  const mutationKey = ['admGatewayDeleteServerGroup'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>, {id: string; data: DeleteCommand}> = (props) => {
    const { id, data } = props;
    return admGatewayDeleteServerGroup(id, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmGatewayDeleteServerGroupMutationResult = NonNullable<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>>;
export type AdmGatewayDeleteServerGroupMutationBody = DeleteCommand;
export type AdmGatewayDeleteServerGroupMutationError = unknown;

export const useAdmGatewayDeleteServerGroup = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>, TError, {id: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admGatewayDeleteServerGroup>>, TError, {id: string; data: DeleteCommand}, TContext> => {
  return useMutation(getAdmGatewayDeleteServerGroupMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admGatewayDeleteServerGroup


// CPF PRE-RUNTIME FALLBACK START admGatewayFindGroupMembers
export type admGatewayFindGroupMembersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admGatewayFindGroupMembersResponseSuccess = (admGatewayFindGroupMembersResponse200) & {
  headers: Headers;
};

export type admGatewayFindGroupMembersResponse = (admGatewayFindGroupMembersResponseSuccess)

export const getAdmGatewayFindGroupMembersUrl = (id: string) => `/adm/api/gateway-registry/server-groups/${encodeURIComponent(String(id))}/members`;

export const admGatewayFindGroupMembers = async (id: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admGatewayFindGroupMembersResponse> => {
  return cpfOrvalRequest<admGatewayFindGroupMembersResponse>(getAdmGatewayFindGroupMembersUrl(id), {
    ...options,
    method: 'GET',

  });
};

export const getAdmGatewayFindGroupMembersQueryKey = (id: MaybeRefOrGetter<string>) => ["adm", "api", "gateway-registry", "server-groups", id, "members"] as const;

export const getAdmGatewayFindGroupMembersQueryOptions = <TData = Awaited<ReturnType<typeof admGatewayFindGroupMembers>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindGroupMembers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmGatewayFindGroupMembersQueryKey(toValue(id));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admGatewayFindGroupMembers>>> = ({ signal }) => admGatewayFindGroupMembers(toValue(id), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(id) !== null && toValue(id) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindGroupMembers>>, TError, TData>;
};

export type AdmGatewayFindGroupMembersQueryResult = NonNullable<Awaited<ReturnType<typeof admGatewayFindGroupMembers>>>;
export type AdmGatewayFindGroupMembersQueryError = unknown;

export function useAdmGatewayFindGroupMembers<TData = Awaited<ReturnType<typeof admGatewayFindGroupMembers>>, TError = unknown>(
  id: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admGatewayFindGroupMembers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmGatewayFindGroupMembersQueryOptions(id, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admGatewayFindGroupMembers


// CPF PRE-RUNTIME FALLBACK START getAdmReadiness
export type getAdmReadinessResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type getAdmReadinessResponseSuccess = (getAdmReadinessResponse200) & {
  headers: Headers;
};

export type getAdmReadinessResponse = (getAdmReadinessResponseSuccess)

export const getGetAdmReadinessUrl = () => `/adm/api/health`;

export const getAdmReadiness = async (options?: CpfOrvalGeneratedRequestOptions): Promise<getAdmReadinessResponse> => {
  return cpfOrvalRequest<getAdmReadinessResponse>(getGetAdmReadinessUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getGetAdmReadinessQueryKey = () => ["adm", "api", "health"] as const;

export const getGetAdmReadinessQueryOptions = <TData = Awaited<ReturnType<typeof getAdmReadiness>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmReadiness>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getGetAdmReadinessQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAdmReadiness>>> = ({ signal }) => getAdmReadiness({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof getAdmReadiness>>, TError, TData>;
};

export type GetAdmReadinessQueryResult = NonNullable<Awaited<ReturnType<typeof getAdmReadiness>>>;
export type GetAdmReadinessQueryError = unknown;

export function useGetAdmReadiness<TData = Awaited<ReturnType<typeof getAdmReadiness>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmReadiness>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAdmReadinessQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END getAdmReadiness


// CPF PRE-RUNTIME FALLBACK START getAdmLiveness
export type getAdmLivenessResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type getAdmLivenessResponseSuccess = (getAdmLivenessResponse200) & {
  headers: Headers;
};

export type getAdmLivenessResponse = (getAdmLivenessResponseSuccess)

export const getGetAdmLivenessUrl = () => `/adm/api/health/liveness`;

export const getAdmLiveness = async (options?: CpfOrvalGeneratedRequestOptions): Promise<getAdmLivenessResponse> => {
  return cpfOrvalRequest<getAdmLivenessResponse>(getGetAdmLivenessUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getGetAdmLivenessQueryKey = () => ["adm", "api", "health", "liveness"] as const;

export const getGetAdmLivenessQueryOptions = <TData = Awaited<ReturnType<typeof getAdmLiveness>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmLiveness>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getGetAdmLivenessQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAdmLiveness>>> = ({ signal }) => getAdmLiveness({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof getAdmLiveness>>, TError, TData>;
};

export type GetAdmLivenessQueryResult = NonNullable<Awaited<ReturnType<typeof getAdmLiveness>>>;
export type GetAdmLivenessQueryError = unknown;

export function useGetAdmLiveness<TData = Awaited<ReturnType<typeof getAdmLiveness>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmLiveness>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAdmLivenessQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END getAdmLiveness


// CPF PRE-RUNTIME FALLBACK START admIncidentFindIncidents
export type admIncidentFindIncidentsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentFindIncidentsResponseSuccess = (admIncidentFindIncidentsResponse200) & {
  headers: Headers;
};

export type admIncidentFindIncidentsResponse = (admIncidentFindIncidentsResponseSuccess)

export const getAdmIncidentFindIncidentsUrl = () => `/adm/api/incidents`;

export const admIncidentFindIncidents = async (params?: AdmIncidentFindIncidentsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentFindIncidentsResponse> => {
  return cpfOrvalRequest<admIncidentFindIncidentsResponse>(getAdmIncidentFindIncidentsUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, page: params?.page, size: params?.size },
  });
};

export const getAdmIncidentFindIncidentsQueryKey = (params?: MaybeRefOrGetter<AdmIncidentFindIncidentsParams>) => ["adm", "api", "incidents", toValue(params)] as const;

export const getAdmIncidentFindIncidentsQueryOptions = <TData = Awaited<ReturnType<typeof admIncidentFindIncidents>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindIncidentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncidents>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmIncidentFindIncidentsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admIncidentFindIncidents>>> = ({ signal }) => admIncidentFindIncidents(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncidents>>, TError, TData>;
};

export type AdmIncidentFindIncidentsQueryResult = NonNullable<Awaited<ReturnType<typeof admIncidentFindIncidents>>>;
export type AdmIncidentFindIncidentsQueryError = unknown;

export function useAdmIncidentFindIncidents<TData = Awaited<ReturnType<typeof admIncidentFindIncidents>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindIncidentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncidents>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmIncidentFindIncidentsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admIncidentFindIncidents


// CPF PRE-RUNTIME FALLBACK START admIncidentCreateIncident
export type admIncidentCreateIncidentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentCreateIncidentResponseSuccess = (admIncidentCreateIncidentResponse200) & {
  headers: Headers;
};

export type admIncidentCreateIncidentResponse = (admIncidentCreateIncidentResponseSuccess)

export const getAdmIncidentCreateIncidentUrl = () => `/adm/api/incidents`;

export const admIncidentCreateIncident = async (data: AdmIncidentCreateIncidentRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentCreateIncidentResponse> => {
  return cpfOrvalRequest<admIncidentCreateIncidentResponse>(getAdmIncidentCreateIncidentUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentCreateIncidentMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateIncident>>, TError, {data: AdmIncidentCreateIncidentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateIncident>>, TError, {data: AdmIncidentCreateIncidentRequest}, TContext> => {
  const mutationKey = ['admIncidentCreateIncident'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentCreateIncident>>, {data: AdmIncidentCreateIncidentRequest}> = (props) => {
    const { data } = props;
    return admIncidentCreateIncident(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentCreateIncidentMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentCreateIncident>>>;
export type AdmIncidentCreateIncidentMutationBody = AdmIncidentCreateIncidentRequest;
export type AdmIncidentCreateIncidentMutationError = unknown;

export const useAdmIncidentCreateIncident = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateIncident>>, TError, {data: AdmIncidentCreateIncidentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentCreateIncident>>, TError, {data: AdmIncidentCreateIncidentRequest}, TContext> => {
  return useMutation(getAdmIncidentCreateIncidentMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentCreateIncident


// CPF PRE-RUNTIME FALLBACK START admIncidentFindMaintenance
export type admIncidentFindMaintenanceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentFindMaintenanceResponseSuccess = (admIncidentFindMaintenanceResponse200) & {
  headers: Headers;
};

export type admIncidentFindMaintenanceResponse = (admIncidentFindMaintenanceResponseSuccess)

export const getAdmIncidentFindMaintenanceUrl = () => `/adm/api/incidents/maintenance-windows`;

export const admIncidentFindMaintenance = async (params?: AdmIncidentFindMaintenanceParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentFindMaintenanceResponse> => {
  return cpfOrvalRequest<admIncidentFindMaintenanceResponse>(getAdmIncidentFindMaintenanceUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getAdmIncidentFindMaintenanceQueryKey = (params?: MaybeRefOrGetter<AdmIncidentFindMaintenanceParams>) => ["adm", "api", "incidents", "maintenance-windows", toValue(params)] as const;

export const getAdmIncidentFindMaintenanceQueryOptions = <TData = Awaited<ReturnType<typeof admIncidentFindMaintenance>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindMaintenanceParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindMaintenance>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmIncidentFindMaintenanceQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admIncidentFindMaintenance>>> = ({ signal }) => admIncidentFindMaintenance(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindMaintenance>>, TError, TData>;
};

export type AdmIncidentFindMaintenanceQueryResult = NonNullable<Awaited<ReturnType<typeof admIncidentFindMaintenance>>>;
export type AdmIncidentFindMaintenanceQueryError = unknown;

export function useAdmIncidentFindMaintenance<TData = Awaited<ReturnType<typeof admIncidentFindMaintenance>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindMaintenanceParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindMaintenance>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmIncidentFindMaintenanceQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admIncidentFindMaintenance


// CPF PRE-RUNTIME FALLBACK START admIncidentCreateMaintenance
export type admIncidentCreateMaintenanceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentCreateMaintenanceResponseSuccess = (admIncidentCreateMaintenanceResponse200) & {
  headers: Headers;
};

export type admIncidentCreateMaintenanceResponse = (admIncidentCreateMaintenanceResponseSuccess)

export const getAdmIncidentCreateMaintenanceUrl = () => `/adm/api/incidents/maintenance-windows`;

export const admIncidentCreateMaintenance = async (data: MaintenanceSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentCreateMaintenanceResponse> => {
  return cpfOrvalRequest<admIncidentCreateMaintenanceResponse>(getAdmIncidentCreateMaintenanceUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentCreateMaintenanceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>, TError, {data: MaintenanceSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>, TError, {data: MaintenanceSaveRequest}, TContext> => {
  const mutationKey = ['admIncidentCreateMaintenance'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>, {data: MaintenanceSaveRequest}> = (props) => {
    const { data } = props;
    return admIncidentCreateMaintenance(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentCreateMaintenanceMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>>;
export type AdmIncidentCreateMaintenanceMutationBody = MaintenanceSaveRequest;
export type AdmIncidentCreateMaintenanceMutationError = unknown;

export const useAdmIncidentCreateMaintenance = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>, TError, {data: MaintenanceSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentCreateMaintenance>>, TError, {data: MaintenanceSaveRequest}, TContext> => {
  return useMutation(getAdmIncidentCreateMaintenanceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentCreateMaintenance


// CPF PRE-RUNTIME FALLBACK START admIncidentUpdateMaintenance
export type admIncidentUpdateMaintenanceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentUpdateMaintenanceResponseSuccess = (admIncidentUpdateMaintenanceResponse200) & {
  headers: Headers;
};

export type admIncidentUpdateMaintenanceResponse = (admIncidentUpdateMaintenanceResponseSuccess)

export const getAdmIncidentUpdateMaintenanceUrl = (maintenanceId: number) => `/adm/api/incidents/maintenance-windows/${encodeURIComponent(String(maintenanceId))}`;

export const admIncidentUpdateMaintenance = async (maintenanceId: number, data: MaintenanceSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentUpdateMaintenanceResponse> => {
  return cpfOrvalRequest<admIncidentUpdateMaintenanceResponse>(getAdmIncidentUpdateMaintenanceUrl(maintenanceId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentUpdateMaintenanceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>, TError, {maintenanceId: number; data: MaintenanceSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>, TError, {maintenanceId: number; data: MaintenanceSaveRequest}, TContext> => {
  const mutationKey = ['admIncidentUpdateMaintenance'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>, {maintenanceId: number; data: MaintenanceSaveRequest}> = (props) => {
    const { maintenanceId, data } = props;
    return admIncidentUpdateMaintenance(maintenanceId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentUpdateMaintenanceMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>>;
export type AdmIncidentUpdateMaintenanceMutationBody = MaintenanceSaveRequest;
export type AdmIncidentUpdateMaintenanceMutationError = unknown;

export const useAdmIncidentUpdateMaintenance = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>, TError, {maintenanceId: number; data: MaintenanceSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentUpdateMaintenance>>, TError, {maintenanceId: number; data: MaintenanceSaveRequest}, TContext> => {
  return useMutation(getAdmIncidentUpdateMaintenanceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentUpdateMaintenance


// CPF PRE-RUNTIME FALLBACK START admIncidentFindPolicies
export type admIncidentFindPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentFindPoliciesResponseSuccess = (admIncidentFindPoliciesResponse200) & {
  headers: Headers;
};

export type admIncidentFindPoliciesResponse = (admIncidentFindPoliciesResponseSuccess)

export const getAdmIncidentFindPoliciesUrl = () => `/adm/api/incidents/policies`;

export const admIncidentFindPolicies = async (params?: AdmIncidentFindPoliciesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentFindPoliciesResponse> => {
  return cpfOrvalRequest<admIncidentFindPoliciesResponse>(getAdmIncidentFindPoliciesUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getAdmIncidentFindPoliciesQueryKey = (params?: MaybeRefOrGetter<AdmIncidentFindPoliciesParams>) => ["adm", "api", "incidents", "policies", toValue(params)] as const;

export const getAdmIncidentFindPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof admIncidentFindPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmIncidentFindPoliciesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admIncidentFindPolicies>>> = ({ signal }) => admIncidentFindPolicies(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindPolicies>>, TError, TData>;
};

export type AdmIncidentFindPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof admIncidentFindPolicies>>>;
export type AdmIncidentFindPoliciesQueryError = unknown;

export function useAdmIncidentFindPolicies<TData = Awaited<ReturnType<typeof admIncidentFindPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmIncidentFindPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmIncidentFindPoliciesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admIncidentFindPolicies


// CPF PRE-RUNTIME FALLBACK START admIncidentCreatePolicy
export type admIncidentCreatePolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentCreatePolicyResponseSuccess = (admIncidentCreatePolicyResponse200) & {
  headers: Headers;
};

export type admIncidentCreatePolicyResponse = (admIncidentCreatePolicyResponseSuccess)

export const getAdmIncidentCreatePolicyUrl = () => `/adm/api/incidents/policies`;

export const admIncidentCreatePolicy = async (data: PolicySaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentCreatePolicyResponse> => {
  return cpfOrvalRequest<admIncidentCreatePolicyResponse>(getAdmIncidentCreatePolicyUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentCreatePolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreatePolicy>>, TError, {data: PolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreatePolicy>>, TError, {data: PolicySaveRequest}, TContext> => {
  const mutationKey = ['admIncidentCreatePolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentCreatePolicy>>, {data: PolicySaveRequest}> = (props) => {
    const { data } = props;
    return admIncidentCreatePolicy(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentCreatePolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentCreatePolicy>>>;
export type AdmIncidentCreatePolicyMutationBody = PolicySaveRequest;
export type AdmIncidentCreatePolicyMutationError = unknown;

export const useAdmIncidentCreatePolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentCreatePolicy>>, TError, {data: PolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentCreatePolicy>>, TError, {data: PolicySaveRequest}, TContext> => {
  return useMutation(getAdmIncidentCreatePolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentCreatePolicy


// CPF PRE-RUNTIME FALLBACK START admIncidentUpdatePolicy
export type admIncidentUpdatePolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentUpdatePolicyResponseSuccess = (admIncidentUpdatePolicyResponse200) & {
  headers: Headers;
};

export type admIncidentUpdatePolicyResponse = (admIncidentUpdatePolicyResponseSuccess)

export const getAdmIncidentUpdatePolicyUrl = (policyId: number) => `/adm/api/incidents/policies/${encodeURIComponent(String(policyId))}`;

export const admIncidentUpdatePolicy = async (policyId: number, data: PolicySaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentUpdatePolicyResponse> => {
  return cpfOrvalRequest<admIncidentUpdatePolicyResponse>(getAdmIncidentUpdatePolicyUrl(policyId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentUpdatePolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>, TError, {policyId: number; data: PolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>, TError, {policyId: number; data: PolicySaveRequest}, TContext> => {
  const mutationKey = ['admIncidentUpdatePolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>, {policyId: number; data: PolicySaveRequest}> = (props) => {
    const { policyId, data } = props;
    return admIncidentUpdatePolicy(policyId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentUpdatePolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>>;
export type AdmIncidentUpdatePolicyMutationBody = PolicySaveRequest;
export type AdmIncidentUpdatePolicyMutationError = unknown;

export const useAdmIncidentUpdatePolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>, TError, {policyId: number; data: PolicySaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentUpdatePolicy>>, TError, {policyId: number; data: PolicySaveRequest}, TContext> => {
  return useMutation(getAdmIncidentUpdatePolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentUpdatePolicy


// CPF PRE-RUNTIME FALLBACK START admIncidentIngestSignal
export type admIncidentIngestSignalResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentIngestSignalResponseSuccess = (admIncidentIngestSignalResponse200) & {
  headers: Headers;
};

export type admIncidentIngestSignalResponse = (admIncidentIngestSignalResponseSuccess)

export const getAdmIncidentIngestSignalUrl = () => `/adm/api/incidents/signals`;

export const admIncidentIngestSignal = async (data: SignalRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentIngestSignalResponse> => {
  return cpfOrvalRequest<admIncidentIngestSignalResponse>(getAdmIncidentIngestSignalUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentIngestSignalMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentIngestSignal>>, TError, {data: SignalRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentIngestSignal>>, TError, {data: SignalRequest}, TContext> => {
  const mutationKey = ['admIncidentIngestSignal'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentIngestSignal>>, {data: SignalRequest}> = (props) => {
    const { data } = props;
    return admIncidentIngestSignal(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentIngestSignalMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentIngestSignal>>>;
export type AdmIncidentIngestSignalMutationBody = SignalRequest;
export type AdmIncidentIngestSignalMutationError = unknown;

export const useAdmIncidentIngestSignal = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentIngestSignal>>, TError, {data: SignalRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentIngestSignal>>, TError, {data: SignalRequest}, TContext> => {
  return useMutation(getAdmIncidentIngestSignalMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentIngestSignal


// CPF PRE-RUNTIME FALLBACK START admIncidentFindIncident
export type admIncidentFindIncidentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentFindIncidentResponseSuccess = (admIncidentFindIncidentResponse200) & {
  headers: Headers;
};

export type admIncidentFindIncidentResponse = (admIncidentFindIncidentResponseSuccess)

export const getAdmIncidentFindIncidentUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}`;

export const admIncidentFindIncident = async (incidentId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentFindIncidentResponse> => {
  return cpfOrvalRequest<admIncidentFindIncidentResponse>(getAdmIncidentFindIncidentUrl(incidentId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmIncidentFindIncidentQueryKey = (incidentId: MaybeRefOrGetter<number>) => ["adm", "api", "incidents", incidentId] as const;

export const getAdmIncidentFindIncidentQueryOptions = <TData = Awaited<ReturnType<typeof admIncidentFindIncident>>, TError = unknown>(
  incidentId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncident>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmIncidentFindIncidentQueryKey(toValue(incidentId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admIncidentFindIncident>>> = ({ signal }) => admIncidentFindIncident(toValue(incidentId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(incidentId) !== null && toValue(incidentId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncident>>, TError, TData>;
};

export type AdmIncidentFindIncidentQueryResult = NonNullable<Awaited<ReturnType<typeof admIncidentFindIncident>>>;
export type AdmIncidentFindIncidentQueryError = unknown;

export function useAdmIncidentFindIncident<TData = Awaited<ReturnType<typeof admIncidentFindIncident>>, TError = unknown>(
  incidentId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindIncident>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmIncidentFindIncidentQueryOptions(incidentId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admIncidentFindIncident


// CPF PRE-RUNTIME FALLBACK START admIncidentAcknowledge
export type admIncidentAcknowledgeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentAcknowledgeResponseSuccess = (admIncidentAcknowledgeResponse200) & {
  headers: Headers;
};

export type admIncidentAcknowledgeResponse = (admIncidentAcknowledgeResponseSuccess)

export const getAdmIncidentAcknowledgeUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/acknowledge`;

export const admIncidentAcknowledge = async (incidentId: number, data: IncidentActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentAcknowledgeResponse> => {
  return cpfOrvalRequest<admIncidentAcknowledgeResponse>(getAdmIncidentAcknowledgeUrl(incidentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentAcknowledgeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentAcknowledge>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentAcknowledge>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  const mutationKey = ['admIncidentAcknowledge'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentAcknowledge>>, {incidentId: number; data: IncidentActionRequest}> = (props) => {
    const { incidentId, data } = props;
    return admIncidentAcknowledge(incidentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentAcknowledgeMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentAcknowledge>>>;
export type AdmIncidentAcknowledgeMutationBody = IncidentActionRequest;
export type AdmIncidentAcknowledgeMutationError = unknown;

export const useAdmIncidentAcknowledge = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentAcknowledge>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentAcknowledge>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  return useMutation(getAdmIncidentAcknowledgeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentAcknowledge


// CPF PRE-RUNTIME FALLBACK START admIncidentEscalate
export type admIncidentEscalateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentEscalateResponseSuccess = (admIncidentEscalateResponse200) & {
  headers: Headers;
};

export type admIncidentEscalateResponse = (admIncidentEscalateResponseSuccess)

export const getAdmIncidentEscalateUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/escalate`;

export const admIncidentEscalate = async (incidentId: number, data: IncidentActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentEscalateResponse> => {
  return cpfOrvalRequest<admIncidentEscalateResponse>(getAdmIncidentEscalateUrl(incidentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentEscalateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentEscalate>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentEscalate>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  const mutationKey = ['admIncidentEscalate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentEscalate>>, {incidentId: number; data: IncidentActionRequest}> = (props) => {
    const { incidentId, data } = props;
    return admIncidentEscalate(incidentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentEscalateMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentEscalate>>>;
export type AdmIncidentEscalateMutationBody = IncidentActionRequest;
export type AdmIncidentEscalateMutationError = unknown;

export const useAdmIncidentEscalate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentEscalate>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentEscalate>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  return useMutation(getAdmIncidentEscalateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentEscalate


// CPF PRE-RUNTIME FALLBACK START admIncidentReopen
export type admIncidentReopenResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentReopenResponseSuccess = (admIncidentReopenResponse200) & {
  headers: Headers;
};

export type admIncidentReopenResponse = (admIncidentReopenResponseSuccess)

export const getAdmIncidentReopenUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/reopen`;

export const admIncidentReopen = async (incidentId: number, data: IncidentActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentReopenResponse> => {
  return cpfOrvalRequest<admIncidentReopenResponse>(getAdmIncidentReopenUrl(incidentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentReopenMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentReopen>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentReopen>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  const mutationKey = ['admIncidentReopen'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentReopen>>, {incidentId: number; data: IncidentActionRequest}> = (props) => {
    const { incidentId, data } = props;
    return admIncidentReopen(incidentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentReopenMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentReopen>>>;
export type AdmIncidentReopenMutationBody = IncidentActionRequest;
export type AdmIncidentReopenMutationError = unknown;

export const useAdmIncidentReopen = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentReopen>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentReopen>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  return useMutation(getAdmIncidentReopenMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentReopen


// CPF PRE-RUNTIME FALLBACK START admIncidentResolve
export type admIncidentResolveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentResolveResponseSuccess = (admIncidentResolveResponse200) & {
  headers: Headers;
};

export type admIncidentResolveResponse = (admIncidentResolveResponseSuccess)

export const getAdmIncidentResolveUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/resolve`;

export const admIncidentResolve = async (incidentId: number, data: IncidentActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentResolveResponse> => {
  return cpfOrvalRequest<admIncidentResolveResponse>(getAdmIncidentResolveUrl(incidentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentResolveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentResolve>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentResolve>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  const mutationKey = ['admIncidentResolve'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentResolve>>, {incidentId: number; data: IncidentActionRequest}> = (props) => {
    const { incidentId, data } = props;
    return admIncidentResolve(incidentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentResolveMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentResolve>>>;
export type AdmIncidentResolveMutationBody = IncidentActionRequest;
export type AdmIncidentResolveMutationError = unknown;

export const useAdmIncidentResolve = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentResolve>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentResolve>>, TError, {incidentId: number; data: IncidentActionRequest}, TContext> => {
  return useMutation(getAdmIncidentResolveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentResolve


// CPF PRE-RUNTIME FALLBACK START admIncidentTransitionIncident
export type admIncidentTransitionIncidentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentTransitionIncidentResponseSuccess = (admIncidentTransitionIncidentResponse200) & {
  headers: Headers;
};

export type admIncidentTransitionIncidentResponse = (admIncidentTransitionIncidentResponseSuccess)

export const getAdmIncidentTransitionIncidentUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/status`;

export const admIncidentTransitionIncident = async (incidentId: number, data: AdmIncidentTransitionIncidentRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentTransitionIncidentResponse> => {
  return cpfOrvalRequest<admIncidentTransitionIncidentResponse>(getAdmIncidentTransitionIncidentUrl(incidentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmIncidentTransitionIncidentMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentTransitionIncident>>, TError, {incidentId: number; data: AdmIncidentTransitionIncidentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admIncidentTransitionIncident>>, TError, {incidentId: number; data: AdmIncidentTransitionIncidentRequest}, TContext> => {
  const mutationKey = ['admIncidentTransitionIncident'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admIncidentTransitionIncident>>, {incidentId: number; data: AdmIncidentTransitionIncidentRequest}> = (props) => {
    const { incidentId, data } = props;
    return admIncidentTransitionIncident(incidentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmIncidentTransitionIncidentMutationResult = NonNullable<Awaited<ReturnType<typeof admIncidentTransitionIncident>>>;
export type AdmIncidentTransitionIncidentMutationBody = AdmIncidentTransitionIncidentRequest;
export type AdmIncidentTransitionIncidentMutationError = unknown;

export const useAdmIncidentTransitionIncident = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admIncidentTransitionIncident>>, TError, {incidentId: number; data: AdmIncidentTransitionIncidentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admIncidentTransitionIncident>>, TError, {incidentId: number; data: AdmIncidentTransitionIncidentRequest}, TContext> => {
  return useMutation(getAdmIncidentTransitionIncidentMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admIncidentTransitionIncident


// CPF PRE-RUNTIME FALLBACK START admIncidentFindTimeline
export type admIncidentFindTimelineResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admIncidentFindTimelineResponseSuccess = (admIncidentFindTimelineResponse200) & {
  headers: Headers;
};

export type admIncidentFindTimelineResponse = (admIncidentFindTimelineResponseSuccess)

export const getAdmIncidentFindTimelineUrl = (incidentId: number) => `/adm/api/incidents/${encodeURIComponent(String(incidentId))}/timeline`;

export const admIncidentFindTimeline = async (incidentId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admIncidentFindTimelineResponse> => {
  return cpfOrvalRequest<admIncidentFindTimelineResponse>(getAdmIncidentFindTimelineUrl(incidentId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmIncidentFindTimelineQueryKey = (incidentId: MaybeRefOrGetter<number>) => ["adm", "api", "incidents", incidentId, "timeline"] as const;

export const getAdmIncidentFindTimelineQueryOptions = <TData = Awaited<ReturnType<typeof admIncidentFindTimeline>>, TError = unknown>(
  incidentId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindTimeline>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmIncidentFindTimelineQueryKey(toValue(incidentId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admIncidentFindTimeline>>> = ({ signal }) => admIncidentFindTimeline(toValue(incidentId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(incidentId) !== null && toValue(incidentId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindTimeline>>, TError, TData>;
};

export type AdmIncidentFindTimelineQueryResult = NonNullable<Awaited<ReturnType<typeof admIncidentFindTimeline>>>;
export type AdmIncidentFindTimelineQueryError = unknown;

export function useAdmIncidentFindTimeline<TData = Awaited<ReturnType<typeof admIncidentFindTimeline>>, TError = unknown>(
  incidentId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admIncidentFindTimeline>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmIncidentFindTimelineQueryOptions(incidentId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admIncidentFindTimeline


// CPF PRE-RUNTIME FALLBACK START admLogExportCreate
export type admLogExportCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogExportCreateResponseSuccess = (admLogExportCreateResponse200) & {
  headers: Headers;
};

export type admLogExportCreateResponse = (admLogExportCreateResponseSuccess)

export const getAdmLogExportCreateUrl = () => `/adm/api/log-exports`;

export const admLogExportCreate = async (data: AdmLogExportRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogExportCreateResponse> => {
  return cpfOrvalRequest<admLogExportCreateResponse>(getAdmLogExportCreateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmLogExportCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogExportCreate>>, TError, {data: AdmLogExportRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogExportCreate>>, TError, {data: AdmLogExportRequest}, TContext> => {
  const mutationKey = ['admLogExportCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogExportCreate>>, {data: AdmLogExportRequest}> = (props) => {
    const { data } = props;
    return admLogExportCreate(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogExportCreateMutationResult = NonNullable<Awaited<ReturnType<typeof admLogExportCreate>>>;
export type AdmLogExportCreateMutationBody = AdmLogExportRequest;
export type AdmLogExportCreateMutationError = unknown;

export const useAdmLogExportCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogExportCreate>>, TError, {data: AdmLogExportRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogExportCreate>>, TError, {data: AdmLogExportRequest}, TContext> => {
  return useMutation(getAdmLogExportCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogExportCreate


// CPF PRE-RUNTIME FALLBACK START admLogExportDownload
export type admLogExportDownloadResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogExportDownloadResponseSuccess = (admLogExportDownloadResponse200) & {
  headers: Headers;
};

export type admLogExportDownloadResponse = (admLogExportDownloadResponseSuccess)

export const getAdmLogExportDownloadUrl = (exportId: string) => `/adm/api/log-exports/${encodeURIComponent(String(exportId))}/artifact`;

export const admLogExportDownload = async (exportId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogExportDownloadResponse> => {
  return cpfOrvalRequest<admLogExportDownloadResponse>(getAdmLogExportDownloadUrl(exportId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmLogExportDownloadQueryKey = (exportId: MaybeRefOrGetter<string>) => ["adm", "api", "log-exports", exportId, "artifact"] as const;

export const getAdmLogExportDownloadQueryOptions = <TData = Awaited<ReturnType<typeof admLogExportDownload>>, TError = unknown>(
  exportId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogExportDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogExportDownloadQueryKey(toValue(exportId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogExportDownload>>> = ({ signal }) => admLogExportDownload(toValue(exportId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(exportId) !== null && toValue(exportId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogExportDownload>>, TError, TData>;
};

export type AdmLogExportDownloadQueryResult = NonNullable<Awaited<ReturnType<typeof admLogExportDownload>>>;
export type AdmLogExportDownloadQueryError = unknown;

export function useAdmLogExportDownload<TData = Awaited<ReturnType<typeof admLogExportDownload>>, TError = unknown>(
  exportId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogExportDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogExportDownloadQueryOptions(exportId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogExportDownload


// CPF PRE-RUNTIME FALLBACK START admDynamicLogLevelFindRules
export type admDynamicLogLevelFindRulesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDynamicLogLevelFindRulesResponseSuccess = (admDynamicLogLevelFindRulesResponse200) & {
  headers: Headers;
};

export type admDynamicLogLevelFindRulesResponse = (admDynamicLogLevelFindRulesResponseSuccess)

export const getAdmDynamicLogLevelFindRulesUrl = () => `/adm/api/log-level/rules`;

export const admDynamicLogLevelFindRules = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admDynamicLogLevelFindRulesResponse> => {
  return cpfOrvalRequest<admDynamicLogLevelFindRulesResponse>(getAdmDynamicLogLevelFindRulesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmDynamicLogLevelFindRulesQueryKey = () => ["adm", "api", "log-level", "rules"] as const;

export const getAdmDynamicLogLevelFindRulesQueryOptions = <TData = Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmDynamicLogLevelFindRulesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>> = ({ signal }) => admDynamicLogLevelFindRules({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>, TError, TData>;
};

export type AdmDynamicLogLevelFindRulesQueryResult = NonNullable<Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>>;
export type AdmDynamicLogLevelFindRulesQueryError = unknown;

export function useAdmDynamicLogLevelFindRules<TData = Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admDynamicLogLevelFindRules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmDynamicLogLevelFindRulesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admDynamicLogLevelFindRules


// CPF PRE-RUNTIME FALLBACK START admDynamicLogLevelRegister
export type admDynamicLogLevelRegisterResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDynamicLogLevelRegisterResponseSuccess = (admDynamicLogLevelRegisterResponse200) & {
  headers: Headers;
};

export type admDynamicLogLevelRegisterResponse = (admDynamicLogLevelRegisterResponseSuccess)

export const getAdmDynamicLogLevelRegisterUrl = () => `/adm/api/log-level/rules`;

export const admDynamicLogLevelRegister = async (params: AdmDynamicLogLevelRegisterParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admDynamicLogLevelRegisterResponse> => {
  return cpfOrvalRequest<admDynamicLogLevelRegisterResponse>(getAdmDynamicLogLevelRegisterUrl(), {
    ...options,
    method: 'PUT',
    params: { businessTransactionId: params.businessTransactionId, transactionId: params.transactionId, logLevel: params.logLevel, ttlSeconds: params.ttlSeconds, reason: params.reason },
  });
};

export const getAdmDynamicLogLevelRegisterMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>, TError, {params: AdmDynamicLogLevelRegisterParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>, TError, {params: AdmDynamicLogLevelRegisterParams}, TContext> => {
  const mutationKey = ['admDynamicLogLevelRegister'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>, {params: AdmDynamicLogLevelRegisterParams}> = (props) => {
    const { params } = props;
    return admDynamicLogLevelRegister(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmDynamicLogLevelRegisterMutationResult = NonNullable<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>>;
export type AdmDynamicLogLevelRegisterMutationBody = never;
export type AdmDynamicLogLevelRegisterMutationError = unknown;

export const useAdmDynamicLogLevelRegister = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>, TError, {params: AdmDynamicLogLevelRegisterParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admDynamicLogLevelRegister>>, TError, {params: AdmDynamicLogLevelRegisterParams}, TContext> => {
  return useMutation(getAdmDynamicLogLevelRegisterMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admDynamicLogLevelRegister


// CPF PRE-RUNTIME FALLBACK START admDynamicLogLevelRemove
export type admDynamicLogLevelRemoveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admDynamicLogLevelRemoveResponseSuccess = (admDynamicLogLevelRemoveResponse200) & {
  headers: Headers;
};

export type admDynamicLogLevelRemoveResponse = (admDynamicLogLevelRemoveResponseSuccess)

export const getAdmDynamicLogLevelRemoveUrl = (ruleId: string) => `/adm/api/log-level/rules/${encodeURIComponent(String(ruleId))}`;

export const admDynamicLogLevelRemove = async (ruleId: string, params: AdmDynamicLogLevelRemoveParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admDynamicLogLevelRemoveResponse> => {
  return cpfOrvalRequest<admDynamicLogLevelRemoveResponse>(getAdmDynamicLogLevelRemoveUrl(ruleId), {
    ...options,
    method: 'DELETE',
    params: { reason: params.reason },
  });
};

export const getAdmDynamicLogLevelRemoveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>, TError, {ruleId: string; params: AdmDynamicLogLevelRemoveParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>, TError, {ruleId: string; params: AdmDynamicLogLevelRemoveParams}, TContext> => {
  const mutationKey = ['admDynamicLogLevelRemove'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>, {ruleId: string; params: AdmDynamicLogLevelRemoveParams}> = (props) => {
    const { ruleId, params } = props;
    return admDynamicLogLevelRemove(ruleId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmDynamicLogLevelRemoveMutationResult = NonNullable<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>>;
export type AdmDynamicLogLevelRemoveMutationBody = never;
export type AdmDynamicLogLevelRemoveMutationError = unknown;

export const useAdmDynamicLogLevelRemove = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>, TError, {ruleId: string; params: AdmDynamicLogLevelRemoveParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admDynamicLogLevelRemove>>, TError, {ruleId: string; params: AdmDynamicLogLevelRemoveParams}, TContext> => {
  return useMutation(getAdmDynamicLogLevelRemoveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admDynamicLogLevelRemove


// CPF PRE-RUNTIME FALLBACK START admLogPolicyFindPolicies
export type admLogPolicyFindPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyFindPoliciesResponseSuccess = (admLogPolicyFindPoliciesResponse200) & {
  headers: Headers;
};

export type admLogPolicyFindPoliciesResponse = (admLogPolicyFindPoliciesResponseSuccess)

export const getAdmLogPolicyFindPoliciesUrl = () => `/adm/api/log-policies`;

export const admLogPolicyFindPolicies = async (params?: AdmLogPolicyFindPoliciesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyFindPoliciesResponse> => {
  return cpfOrvalRequest<admLogPolicyFindPoliciesResponse>(getAdmLogPolicyFindPoliciesUrl(), {
    ...options,
    method: 'GET',
    params: { targetType: params?.targetType, targetId: params?.targetId, activeYn: params?.activeYn, limit: params?.limit },
  });
};

export const getAdmLogPolicyFindPoliciesQueryKey = (params?: MaybeRefOrGetter<AdmLogPolicyFindPoliciesParams>) => ["adm", "api", "log-policies", toValue(params)] as const;

export const getAdmLogPolicyFindPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyFindPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyFindPoliciesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyFindPolicies>>> = ({ signal }) => admLogPolicyFindPolicies(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicies>>, TError, TData>;
};

export type AdmLogPolicyFindPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyFindPolicies>>>;
export type AdmLogPolicyFindPoliciesQueryError = unknown;

export function useAdmLogPolicyFindPolicies<TData = Awaited<ReturnType<typeof admLogPolicyFindPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyFindPoliciesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyFindPolicies


// CPF PRE-RUNTIME FALLBACK START admLogPolicyCreatePolicy
export type admLogPolicyCreatePolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyCreatePolicyResponseSuccess = (admLogPolicyCreatePolicyResponse200) & {
  headers: Headers;
};

export type admLogPolicyCreatePolicyResponse = (admLogPolicyCreatePolicyResponseSuccess)

export const getAdmLogPolicyCreatePolicyUrl = () => `/adm/api/log-policies`;

export const admLogPolicyCreatePolicy = async (data: AdmLogPolicyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyCreatePolicyResponse> => {
  return cpfOrvalRequest<admLogPolicyCreatePolicyResponse>(getAdmLogPolicyCreatePolicyUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmLogPolicyCreatePolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>, TError, {data: AdmLogPolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>, TError, {data: AdmLogPolicyRequest}, TContext> => {
  const mutationKey = ['admLogPolicyCreatePolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>, {data: AdmLogPolicyRequest}> = (props) => {
    const { data } = props;
    return admLogPolicyCreatePolicy(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyCreatePolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>>;
export type AdmLogPolicyCreatePolicyMutationBody = AdmLogPolicyRequest;
export type AdmLogPolicyCreatePolicyMutationError = unknown;

export const useAdmLogPolicyCreatePolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>, TError, {data: AdmLogPolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyCreatePolicy>>, TError, {data: AdmLogPolicyRequest}, TContext> => {
  return useMutation(getAdmLogPolicyCreatePolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyCreatePolicy


// CPF PRE-RUNTIME FALLBACK START admLogPolicyClearCache
export type admLogPolicyClearCacheResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyClearCacheResponseSuccess = (admLogPolicyClearCacheResponse200) & {
  headers: Headers;
};

export type admLogPolicyClearCacheResponse = (admLogPolicyClearCacheResponseSuccess)

export const getAdmLogPolicyClearCacheUrl = () => `/adm/api/log-policies/cache/clear`;

export const admLogPolicyClearCache = async (params: AdmLogPolicyClearCacheParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyClearCacheResponse> => {
  return cpfOrvalRequest<admLogPolicyClearCacheResponse>(getAdmLogPolicyClearCacheUrl(), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmLogPolicyClearCacheMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyClearCache>>, TError, {params: AdmLogPolicyClearCacheParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyClearCache>>, TError, {params: AdmLogPolicyClearCacheParams}, TContext> => {
  const mutationKey = ['admLogPolicyClearCache'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyClearCache>>, {params: AdmLogPolicyClearCacheParams}> = (props) => {
    const { params } = props;
    return admLogPolicyClearCache(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyClearCacheMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyClearCache>>>;
export type AdmLogPolicyClearCacheMutationBody = never;
export type AdmLogPolicyClearCacheMutationError = unknown;

export const useAdmLogPolicyClearCache = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyClearCache>>, TError, {params: AdmLogPolicyClearCacheParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyClearCache>>, TError, {params: AdmLogPolicyClearCacheParams}, TContext> => {
  return useMutation(getAdmLogPolicyClearCacheMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyClearCache


// CPF PRE-RUNTIME FALLBACK START admLogPolicyRefreshCache
export type admLogPolicyRefreshCacheResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyRefreshCacheResponseSuccess = (admLogPolicyRefreshCacheResponse200) & {
  headers: Headers;
};

export type admLogPolicyRefreshCacheResponse = (admLogPolicyRefreshCacheResponseSuccess)

export const getAdmLogPolicyRefreshCacheUrl = () => `/adm/api/log-policies/cache/refresh`;

export const admLogPolicyRefreshCache = async (params: AdmLogPolicyRefreshCacheParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyRefreshCacheResponse> => {
  return cpfOrvalRequest<admLogPolicyRefreshCacheResponse>(getAdmLogPolicyRefreshCacheUrl(), {
    ...options,
    method: 'POST',
    params: { targetType: params.targetType, targetId: params.targetId, reason: params.reason },
  });
};

export const getAdmLogPolicyRefreshCacheMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>, TError, {params: AdmLogPolicyRefreshCacheParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>, TError, {params: AdmLogPolicyRefreshCacheParams}, TContext> => {
  const mutationKey = ['admLogPolicyRefreshCache'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>, {params: AdmLogPolicyRefreshCacheParams}> = (props) => {
    const { params } = props;
    return admLogPolicyRefreshCache(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyRefreshCacheMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>>;
export type AdmLogPolicyRefreshCacheMutationBody = never;
export type AdmLogPolicyRefreshCacheMutationError = unknown;

export const useAdmLogPolicyRefreshCache = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>, TError, {params: AdmLogPolicyRefreshCacheParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyRefreshCache>>, TError, {params: AdmLogPolicyRefreshCacheParams}, TContext> => {
  return useMutation(getAdmLogPolicyRefreshCacheMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyRefreshCache


// CPF PRE-RUNTIME FALLBACK START admLogPolicyDistributionStatus
export type admLogPolicyDistributionStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyDistributionStatusResponseSuccess = (admLogPolicyDistributionStatusResponse200) & {
  headers: Headers;
};

export type admLogPolicyDistributionStatusResponse = (admLogPolicyDistributionStatusResponseSuccess)

export const getAdmLogPolicyDistributionStatusUrl = () => `/adm/api/log-policies/distribution`;

export const admLogPolicyDistributionStatus = async (params?: AdmLogPolicyDistributionStatusParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyDistributionStatusResponse> => {
  return cpfOrvalRequest<admLogPolicyDistributionStatusResponse>(getAdmLogPolicyDistributionStatusUrl(), {
    ...options,
    method: 'GET',
    params: { targetType: params?.targetType, targetId: params?.targetId, limit: params?.limit },
  });
};

export const getAdmLogPolicyDistributionStatusQueryKey = (params?: MaybeRefOrGetter<AdmLogPolicyDistributionStatusParams>) => ["adm", "api", "log-policies", "distribution", toValue(params)] as const;

export const getAdmLogPolicyDistributionStatusQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyDistributionStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyDistributionStatusQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>> = ({ signal }) => admLogPolicyDistributionStatus(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>, TError, TData>;
};

export type AdmLogPolicyDistributionStatusQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>>;
export type AdmLogPolicyDistributionStatusQueryError = unknown;

export function useAdmLogPolicyDistributionStatus<TData = Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyDistributionStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyDistributionStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyDistributionStatusQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyDistributionStatus


// CPF PRE-RUNTIME FALLBACK START admLogPolicyFindTraceBoostHistory
export type admLogPolicyFindTraceBoostHistoryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyFindTraceBoostHistoryResponseSuccess = (admLogPolicyFindTraceBoostHistoryResponse200) & {
  headers: Headers;
};

export type admLogPolicyFindTraceBoostHistoryResponse = (admLogPolicyFindTraceBoostHistoryResponseSuccess)

export const getAdmLogPolicyFindTraceBoostHistoryUrl = () => `/adm/api/log-policies/history`;

export const admLogPolicyFindTraceBoostHistory = async (params?: AdmLogPolicyFindTraceBoostHistoryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyFindTraceBoostHistoryResponse> => {
  return cpfOrvalRequest<admLogPolicyFindTraceBoostHistoryResponse>(getAdmLogPolicyFindTraceBoostHistoryUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmLogPolicyFindTraceBoostHistoryQueryKey = (params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostHistoryParams>) => ["adm", "api", "log-policies", "history", toValue(params)] as const;

export const getAdmLogPolicyFindTraceBoostHistoryQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyFindTraceBoostHistoryQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>> = ({ signal }) => admLogPolicyFindTraceBoostHistory(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>, TError, TData>;
};

export type AdmLogPolicyFindTraceBoostHistoryQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>>;
export type AdmLogPolicyFindTraceBoostHistoryQueryError = unknown;

export function useAdmLogPolicyFindTraceBoostHistory<TData = Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyFindTraceBoostHistoryQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyFindTraceBoostHistory


// CPF PRE-RUNTIME FALLBACK START admLogPolicyCreateOverride
export type admLogPolicyCreateOverrideResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyCreateOverrideResponseSuccess = (admLogPolicyCreateOverrideResponse200) & {
  headers: Headers;
};

export type admLogPolicyCreateOverrideResponse = (admLogPolicyCreateOverrideResponseSuccess)

export const getAdmLogPolicyCreateOverrideUrl = () => `/adm/api/log-policies/overrides`;

export const admLogPolicyCreateOverride = async (data: AdmLogPolicyOverrideRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyCreateOverrideResponse> => {
  return cpfOrvalRequest<admLogPolicyCreateOverrideResponse>(getAdmLogPolicyCreateOverrideUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmLogPolicyCreateOverrideMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>, TError, {data: AdmLogPolicyOverrideRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>, TError, {data: AdmLogPolicyOverrideRequest}, TContext> => {
  const mutationKey = ['admLogPolicyCreateOverride'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>, {data: AdmLogPolicyOverrideRequest}> = (props) => {
    const { data } = props;
    return admLogPolicyCreateOverride(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyCreateOverrideMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>>;
export type AdmLogPolicyCreateOverrideMutationBody = AdmLogPolicyOverrideRequest;
export type AdmLogPolicyCreateOverrideMutationError = unknown;

export const useAdmLogPolicyCreateOverride = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>, TError, {data: AdmLogPolicyOverrideRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyCreateOverride>>, TError, {data: AdmLogPolicyOverrideRequest}, TContext> => {
  return useMutation(getAdmLogPolicyCreateOverrideMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyCreateOverride


// CPF PRE-RUNTIME FALLBACK START admLogPolicyDisableOverride
export type admLogPolicyDisableOverrideResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyDisableOverrideResponseSuccess = (admLogPolicyDisableOverrideResponse200) & {
  headers: Headers;
};

export type admLogPolicyDisableOverrideResponse = (admLogPolicyDisableOverrideResponseSuccess)

export const getAdmLogPolicyDisableOverrideUrl = (overrideId: number) => `/adm/api/log-policies/overrides/${encodeURIComponent(String(overrideId))}/disable`;

export const admLogPolicyDisableOverride = async (overrideId: number, params: AdmLogPolicyDisableOverrideParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyDisableOverrideResponse> => {
  return cpfOrvalRequest<admLogPolicyDisableOverrideResponse>(getAdmLogPolicyDisableOverrideUrl(overrideId), {
    ...options,
    method: 'PATCH',
    params: { reason: params.reason },
  });
};

export const getAdmLogPolicyDisableOverrideMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>, TError, {overrideId: number; params: AdmLogPolicyDisableOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>, TError, {overrideId: number; params: AdmLogPolicyDisableOverrideParams}, TContext> => {
  const mutationKey = ['admLogPolicyDisableOverride'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>, {overrideId: number; params: AdmLogPolicyDisableOverrideParams}> = (props) => {
    const { overrideId, params } = props;
    return admLogPolicyDisableOverride(overrideId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyDisableOverrideMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>>;
export type AdmLogPolicyDisableOverrideMutationBody = never;
export type AdmLogPolicyDisableOverrideMutationError = unknown;

export const useAdmLogPolicyDisableOverride = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>, TError, {overrideId: number; params: AdmLogPolicyDisableOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyDisableOverride>>, TError, {overrideId: number; params: AdmLogPolicyDisableOverrideParams}, TContext> => {
  return useMutation(getAdmLogPolicyDisableOverrideMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyDisableOverride


// CPF PRE-RUNTIME FALLBACK START admLogPolicyFindTraceBoostRuntimeState
export type admLogPolicyFindTraceBoostRuntimeStateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyFindTraceBoostRuntimeStateResponseSuccess = (admLogPolicyFindTraceBoostRuntimeStateResponse200) & {
  headers: Headers;
};

export type admLogPolicyFindTraceBoostRuntimeStateResponse = (admLogPolicyFindTraceBoostRuntimeStateResponseSuccess)

export const getAdmLogPolicyFindTraceBoostRuntimeStateUrl = () => `/adm/api/log-policies/runtime-state`;

export const admLogPolicyFindTraceBoostRuntimeState = async (params?: AdmLogPolicyFindTraceBoostRuntimeStateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyFindTraceBoostRuntimeStateResponse> => {
  return cpfOrvalRequest<admLogPolicyFindTraceBoostRuntimeStateResponse>(getAdmLogPolicyFindTraceBoostRuntimeStateUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmLogPolicyFindTraceBoostRuntimeStateQueryKey = (params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostRuntimeStateParams>) => ["adm", "api", "log-policies", "runtime-state", toValue(params)] as const;

export const getAdmLogPolicyFindTraceBoostRuntimeStateQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostRuntimeStateParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyFindTraceBoostRuntimeStateQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>> = ({ signal }) => admLogPolicyFindTraceBoostRuntimeState(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>, TError, TData>;
};

export type AdmLogPolicyFindTraceBoostRuntimeStateQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>>;
export type AdmLogPolicyFindTraceBoostRuntimeStateQueryError = unknown;

export function useAdmLogPolicyFindTraceBoostRuntimeState<TData = Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyFindTraceBoostRuntimeStateParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindTraceBoostRuntimeState>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyFindTraceBoostRuntimeStateQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyFindTraceBoostRuntimeState


// CPF PRE-RUNTIME FALLBACK START admLogPolicyCreateTraceBoost
export type admLogPolicyCreateTraceBoostResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyCreateTraceBoostResponseSuccess = (admLogPolicyCreateTraceBoostResponse200) & {
  headers: Headers;
};

export type admLogPolicyCreateTraceBoostResponse = (admLogPolicyCreateTraceBoostResponseSuccess)

export const getAdmLogPolicyCreateTraceBoostUrl = () => `/adm/api/log-policies/trace-boost`;

export const admLogPolicyCreateTraceBoost = async (data: AdmTraceBoostRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyCreateTraceBoostResponse> => {
  return cpfOrvalRequest<admLogPolicyCreateTraceBoostResponse>(getAdmLogPolicyCreateTraceBoostUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmLogPolicyCreateTraceBoostMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>, TError, {data: AdmTraceBoostRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>, TError, {data: AdmTraceBoostRequest}, TContext> => {
  const mutationKey = ['admLogPolicyCreateTraceBoost'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>, {data: AdmTraceBoostRequest}> = (props) => {
    const { data } = props;
    return admLogPolicyCreateTraceBoost(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyCreateTraceBoostMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>>;
export type AdmLogPolicyCreateTraceBoostMutationBody = AdmTraceBoostRequest;
export type AdmLogPolicyCreateTraceBoostMutationError = unknown;

export const useAdmLogPolicyCreateTraceBoost = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>, TError, {data: AdmTraceBoostRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyCreateTraceBoost>>, TError, {data: AdmTraceBoostRequest}, TContext> => {
  return useMutation(getAdmLogPolicyCreateTraceBoostMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyCreateTraceBoost


// CPF PRE-RUNTIME FALLBACK START admLogPolicyFindPolicy
export type admLogPolicyFindPolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyFindPolicyResponseSuccess = (admLogPolicyFindPolicyResponse200) & {
  headers: Headers;
};

export type admLogPolicyFindPolicyResponse = (admLogPolicyFindPolicyResponseSuccess)

export const getAdmLogPolicyFindPolicyUrl = (policyId: number) => `/adm/api/log-policies/${encodeURIComponent(String(policyId))}`;

export const admLogPolicyFindPolicy = async (policyId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyFindPolicyResponse> => {
  return cpfOrvalRequest<admLogPolicyFindPolicyResponse>(getAdmLogPolicyFindPolicyUrl(policyId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmLogPolicyFindPolicyQueryKey = (policyId: MaybeRefOrGetter<number>) => ["adm", "api", "log-policies", policyId] as const;

export const getAdmLogPolicyFindPolicyQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyFindPolicy>>, TError = unknown>(
  policyId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicy>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyFindPolicyQueryKey(toValue(policyId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyFindPolicy>>> = ({ signal }) => admLogPolicyFindPolicy(toValue(policyId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(policyId) !== null && toValue(policyId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicy>>, TError, TData>;
};

export type AdmLogPolicyFindPolicyQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyFindPolicy>>>;
export type AdmLogPolicyFindPolicyQueryError = unknown;

export function useAdmLogPolicyFindPolicy<TData = Awaited<ReturnType<typeof admLogPolicyFindPolicy>>, TError = unknown>(
  policyId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyFindPolicy>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyFindPolicyQueryOptions(policyId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyFindPolicy


// CPF PRE-RUNTIME FALLBACK START admLogPolicyUpdatePolicy
export type admLogPolicyUpdatePolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyUpdatePolicyResponseSuccess = (admLogPolicyUpdatePolicyResponse200) & {
  headers: Headers;
};

export type admLogPolicyUpdatePolicyResponse = (admLogPolicyUpdatePolicyResponseSuccess)

export const getAdmLogPolicyUpdatePolicyUrl = (policyId: number) => `/adm/api/log-policies/${encodeURIComponent(String(policyId))}`;

export const admLogPolicyUpdatePolicy = async (policyId: number, data: AdmLogPolicyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyUpdatePolicyResponse> => {
  return cpfOrvalRequest<admLogPolicyUpdatePolicyResponse>(getAdmLogPolicyUpdatePolicyUrl(policyId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmLogPolicyUpdatePolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>, TError, {policyId: number; data: AdmLogPolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>, TError, {policyId: number; data: AdmLogPolicyRequest}, TContext> => {
  const mutationKey = ['admLogPolicyUpdatePolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>, {policyId: number; data: AdmLogPolicyRequest}> = (props) => {
    const { policyId, data } = props;
    return admLogPolicyUpdatePolicy(policyId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyUpdatePolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>>;
export type AdmLogPolicyUpdatePolicyMutationBody = AdmLogPolicyRequest;
export type AdmLogPolicyUpdatePolicyMutationError = unknown;

export const useAdmLogPolicyUpdatePolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>, TError, {policyId: number; data: AdmLogPolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyUpdatePolicy>>, TError, {policyId: number; data: AdmLogPolicyRequest}, TContext> => {
  return useMutation(getAdmLogPolicyUpdatePolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyUpdatePolicy


// CPF PRE-RUNTIME FALLBACK START admLogPolicyDisablePolicy
export type admLogPolicyDisablePolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyDisablePolicyResponseSuccess = (admLogPolicyDisablePolicyResponse200) & {
  headers: Headers;
};

export type admLogPolicyDisablePolicyResponse = (admLogPolicyDisablePolicyResponseSuccess)

export const getAdmLogPolicyDisablePolicyUrl = (policyId: number) => `/adm/api/log-policies/${encodeURIComponent(String(policyId))}/disable`;

export const admLogPolicyDisablePolicy = async (policyId: number, params: AdmLogPolicyDisablePolicyParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyDisablePolicyResponse> => {
  return cpfOrvalRequest<admLogPolicyDisablePolicyResponse>(getAdmLogPolicyDisablePolicyUrl(policyId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmLogPolicyDisablePolicyMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>, TError, {policyId: number; params: AdmLogPolicyDisablePolicyParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>, TError, {policyId: number; params: AdmLogPolicyDisablePolicyParams}, TContext> => {
  const mutationKey = ['admLogPolicyDisablePolicy'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>, {policyId: number; params: AdmLogPolicyDisablePolicyParams}> = (props) => {
    const { policyId, params } = props;
    return admLogPolicyDisablePolicy(policyId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmLogPolicyDisablePolicyMutationResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>>;
export type AdmLogPolicyDisablePolicyMutationBody = never;
export type AdmLogPolicyDisablePolicyMutationError = unknown;

export const useAdmLogPolicyDisablePolicy = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>, TError, {policyId: number; params: AdmLogPolicyDisablePolicyParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admLogPolicyDisablePolicy>>, TError, {policyId: number; params: AdmLogPolicyDisablePolicyParams}, TContext> => {
  return useMutation(getAdmLogPolicyDisablePolicyMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admLogPolicyDisablePolicy


// CPF PRE-RUNTIME FALLBACK START admLogPolicyAuditFindPolicyAudits
export type admLogPolicyAuditFindPolicyAuditsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogPolicyAuditFindPolicyAuditsResponseSuccess = (admLogPolicyAuditFindPolicyAuditsResponse200) & {
  headers: Headers;
};

export type admLogPolicyAuditFindPolicyAuditsResponse = (admLogPolicyAuditFindPolicyAuditsResponseSuccess)

export const getAdmLogPolicyAuditFindPolicyAuditsUrl = () => `/adm/api/log-policy-audits`;

export const admLogPolicyAuditFindPolicyAudits = async (params?: AdmLogPolicyAuditFindPolicyAuditsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogPolicyAuditFindPolicyAuditsResponse> => {
  return cpfOrvalRequest<admLogPolicyAuditFindPolicyAuditsResponse>(getAdmLogPolicyAuditFindPolicyAuditsUrl(), {
    ...options,
    method: 'GET',
    params: { actionType: params?.actionType, targetType: params?.targetType, targetId: params?.targetId, policyId: params?.policyId, overrideId: params?.overrideId, limit: params?.limit },
  });
};

export const getAdmLogPolicyAuditFindPolicyAuditsQueryKey = (params?: MaybeRefOrGetter<AdmLogPolicyAuditFindPolicyAuditsParams>) => ["adm", "api", "log-policy-audits", toValue(params)] as const;

export const getAdmLogPolicyAuditFindPolicyAuditsQueryOptions = <TData = Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyAuditFindPolicyAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogPolicyAuditFindPolicyAuditsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>> = ({ signal }) => admLogPolicyAuditFindPolicyAudits(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>, TError, TData>;
};

export type AdmLogPolicyAuditFindPolicyAuditsQueryResult = NonNullable<Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>>;
export type AdmLogPolicyAuditFindPolicyAuditsQueryError = unknown;

export function useAdmLogPolicyAuditFindPolicyAudits<TData = Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogPolicyAuditFindPolicyAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogPolicyAuditFindPolicyAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogPolicyAuditFindPolicyAuditsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogPolicyAuditFindPolicyAudits


// CPF PRE-RUNTIME FALLBACK START admLogFindLogs
export type admLogFindLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogFindLogsResponseSuccess = (admLogFindLogsResponse200) & {
  headers: Headers;
};

export type admLogFindLogsResponse = (admLogFindLogsResponseSuccess)

export const getAdmLogFindLogsUrl = () => `/adm/api/logs`;

export const admLogFindLogs = async (params?: AdmLogFindLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogFindLogsResponse> => {
  return cpfOrvalRequest<admLogFindLogsResponse>(getAdmLogFindLogsUrl(), {
    ...options,
    method: 'GET',
    params: { transactionId: params?.transactionId, traceId: params?.traceId, businessTransactionId: params?.businessTransactionId, memberNo: params?.memberNo, customerNo: params?.customerNo, uri: params?.uri, responseCode: params?.responseCode, httpStatus: params?.httpStatus, channelCode: params?.channelCode, logType: params?.logType, moduleId: params?.moduleId, wasId: params?.wasId, serverInstanceId: params?.serverInstanceId, hostName: params?.hostName, limit: params?.limit },
  });
};

export const getAdmLogFindLogsQueryKey = (params?: MaybeRefOrGetter<AdmLogFindLogsParams>) => ["adm", "api", "logs", toValue(params)] as const;

export const getAdmLogFindLogsQueryOptions = <TData = Awaited<ReturnType<typeof admLogFindLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogFindLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogFindLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogFindLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogFindLogs>>> = ({ signal }) => admLogFindLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogFindLogs>>, TError, TData>;
};

export type AdmLogFindLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admLogFindLogs>>>;
export type AdmLogFindLogsQueryError = unknown;

export function useAdmLogFindLogs<TData = Awaited<ReturnType<typeof admLogFindLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmLogFindLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogFindLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogFindLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogFindLogs


// CPF PRE-RUNTIME FALLBACK START admLogGetLogDetail
export type admLogGetLogDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admLogGetLogDetailResponseSuccess = (admLogGetLogDetailResponse200) & {
  headers: Headers;
};

export type admLogGetLogDetailResponse = (admLogGetLogDetailResponseSuccess)

export const getAdmLogGetLogDetailUrl = (logIdx: number) => `/adm/api/logs/${encodeURIComponent(String(logIdx))}`;

export const admLogGetLogDetail = async (logIdx: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admLogGetLogDetailResponse> => {
  return cpfOrvalRequest<admLogGetLogDetailResponse>(getAdmLogGetLogDetailUrl(logIdx), {
    ...options,
    method: 'GET',

  });
};

export const getAdmLogGetLogDetailQueryKey = (logIdx: MaybeRefOrGetter<number>) => ["adm", "api", "logs", logIdx] as const;

export const getAdmLogGetLogDetailQueryOptions = <TData = Awaited<ReturnType<typeof admLogGetLogDetail>>, TError = unknown>(
  logIdx: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogGetLogDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmLogGetLogDetailQueryKey(toValue(logIdx));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admLogGetLogDetail>>> = ({ signal }) => admLogGetLogDetail(toValue(logIdx), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(logIdx) !== null && toValue(logIdx) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admLogGetLogDetail>>, TError, TData>;
};

export type AdmLogGetLogDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admLogGetLogDetail>>>;
export type AdmLogGetLogDetailQueryError = unknown;

export function useAdmLogGetLogDetail<TData = Awaited<ReturnType<typeof admLogGetLogDetail>>, TError = unknown>(
  logIdx: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admLogGetLogDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmLogGetLogDetailQueryOptions(logIdx, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admLogGetLogDetail


// CPF PRE-RUNTIME FALLBACK START admMaintenanceFindActions
export type admMaintenanceFindActionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMaintenanceFindActionsResponseSuccess = (admMaintenanceFindActionsResponse200) & {
  headers: Headers;
};

export type admMaintenanceFindActionsResponse = (admMaintenanceFindActionsResponseSuccess)

export const getAdmMaintenanceFindActionsUrl = () => `/adm/api/maintenance/actions`;

export const admMaintenanceFindActions = async (params?: AdmMaintenanceFindActionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admMaintenanceFindActionsResponse> => {
  return cpfOrvalRequest<admMaintenanceFindActionsResponse>(getAdmMaintenanceFindActionsUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmMaintenanceFindActionsQueryKey = (params?: MaybeRefOrGetter<AdmMaintenanceFindActionsParams>) => ["adm", "api", "maintenance", "actions", toValue(params)] as const;

export const getAdmMaintenanceFindActionsQueryOptions = <TData = Awaited<ReturnType<typeof admMaintenanceFindActions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmMaintenanceFindActionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMaintenanceFindActions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmMaintenanceFindActionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admMaintenanceFindActions>>> = ({ signal }) => admMaintenanceFindActions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admMaintenanceFindActions>>, TError, TData>;
};

export type AdmMaintenanceFindActionsQueryResult = NonNullable<Awaited<ReturnType<typeof admMaintenanceFindActions>>>;
export type AdmMaintenanceFindActionsQueryError = unknown;

export function useAdmMaintenanceFindActions<TData = Awaited<ReturnType<typeof admMaintenanceFindActions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmMaintenanceFindActionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMaintenanceFindActions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmMaintenanceFindActionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admMaintenanceFindActions


// CPF PRE-RUNTIME FALLBACK START admMaintenanceExecuteAction
export type admMaintenanceExecuteActionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMaintenanceExecuteActionResponseSuccess = (admMaintenanceExecuteActionResponse200) & {
  headers: Headers;
};

export type admMaintenanceExecuteActionResponse = (admMaintenanceExecuteActionResponseSuccess)

export const getAdmMaintenanceExecuteActionUrl = () => `/adm/api/maintenance/actions`;

export const admMaintenanceExecuteAction = async (data: AdmMaintenanceExecuteActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admMaintenanceExecuteActionResponse> => {
  return cpfOrvalRequest<admMaintenanceExecuteActionResponse>(getAdmMaintenanceExecuteActionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmMaintenanceExecuteActionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>, TError, {data: AdmMaintenanceExecuteActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>, TError, {data: AdmMaintenanceExecuteActionRequest}, TContext> => {
  const mutationKey = ['admMaintenanceExecuteAction'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>, {data: AdmMaintenanceExecuteActionRequest}> = (props) => {
    const { data } = props;
    return admMaintenanceExecuteAction(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmMaintenanceExecuteActionMutationResult = NonNullable<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>>;
export type AdmMaintenanceExecuteActionMutationBody = AdmMaintenanceExecuteActionRequest;
export type AdmMaintenanceExecuteActionMutationError = unknown;

export const useAdmMaintenanceExecuteAction = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>, TError, {data: AdmMaintenanceExecuteActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admMaintenanceExecuteAction>>, TError, {data: AdmMaintenanceExecuteActionRequest}, TContext> => {
  return useMutation(getAdmMaintenanceExecuteActionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admMaintenanceExecuteAction


// CPF PRE-RUNTIME FALLBACK START admMessageFindMessages
export type admMessageFindMessagesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMessageFindMessagesResponseSuccess = (admMessageFindMessagesResponse200) & {
  headers: Headers;
};

export type admMessageFindMessagesResponse = (admMessageFindMessagesResponseSuccess)

export const getAdmMessageFindMessagesUrl = () => `/adm/api/messages`;

export const admMessageFindMessages = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admMessageFindMessagesResponse> => {
  return cpfOrvalRequest<admMessageFindMessagesResponse>(getAdmMessageFindMessagesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmMessageFindMessagesQueryKey = () => ["adm", "api", "messages"] as const;

export const getAdmMessageFindMessagesQueryOptions = <TData = Awaited<ReturnType<typeof admMessageFindMessages>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessages>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmMessageFindMessagesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admMessageFindMessages>>> = ({ signal }) => admMessageFindMessages({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessages>>, TError, TData>;
};

export type AdmMessageFindMessagesQueryResult = NonNullable<Awaited<ReturnType<typeof admMessageFindMessages>>>;
export type AdmMessageFindMessagesQueryError = unknown;

export function useAdmMessageFindMessages<TData = Awaited<ReturnType<typeof admMessageFindMessages>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessages>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmMessageFindMessagesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admMessageFindMessages


// CPF PRE-RUNTIME FALLBACK START admMessageCreateMessage
export type admMessageCreateMessageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMessageCreateMessageResponseSuccess = (admMessageCreateMessageResponse200) & {
  headers: Headers;
};

export type admMessageCreateMessageResponse = (admMessageCreateMessageResponseSuccess)

export const getAdmMessageCreateMessageUrl = () => `/adm/api/messages`;

export const admMessageCreateMessage = async (data: CommonMessageRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admMessageCreateMessageResponse> => {
  return cpfOrvalRequest<admMessageCreateMessageResponse>(getAdmMessageCreateMessageUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmMessageCreateMessageMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageCreateMessage>>, TError, {data: CommonMessageRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admMessageCreateMessage>>, TError, {data: CommonMessageRequest}, TContext> => {
  const mutationKey = ['admMessageCreateMessage'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admMessageCreateMessage>>, {data: CommonMessageRequest}> = (props) => {
    const { data } = props;
    return admMessageCreateMessage(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmMessageCreateMessageMutationResult = NonNullable<Awaited<ReturnType<typeof admMessageCreateMessage>>>;
export type AdmMessageCreateMessageMutationBody = CommonMessageRequest;
export type AdmMessageCreateMessageMutationError = unknown;

export const useAdmMessageCreateMessage = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageCreateMessage>>, TError, {data: CommonMessageRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admMessageCreateMessage>>, TError, {data: CommonMessageRequest}, TContext> => {
  return useMutation(getAdmMessageCreateMessageMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admMessageCreateMessage


// CPF PRE-RUNTIME FALLBACK START admMessageDeleteMessage
export type admMessageDeleteMessageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMessageDeleteMessageResponseSuccess = (admMessageDeleteMessageResponse200) & {
  headers: Headers;
};

export type admMessageDeleteMessageResponse = (admMessageDeleteMessageResponseSuccess)

export const getAdmMessageDeleteMessageUrl = (messageId: number) => `/adm/api/messages/${encodeURIComponent(String(messageId))}`;

export const admMessageDeleteMessage = async (messageId: number, params: AdmMessageDeleteMessageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admMessageDeleteMessageResponse> => {
  return cpfOrvalRequest<admMessageDeleteMessageResponse>(getAdmMessageDeleteMessageUrl(messageId), {
    ...options,
    method: 'DELETE',
    params: { reason: params.reason },
  });
};

export const getAdmMessageDeleteMessageMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError, {messageId: number; params: AdmMessageDeleteMessageParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError, {messageId: number; params: AdmMessageDeleteMessageParams}, TContext> => {
  const mutationKey = ['admMessageDeleteMessage'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admMessageDeleteMessage>>, {messageId: number; params: AdmMessageDeleteMessageParams}> = (props) => {
    const { messageId, params } = props;
    return admMessageDeleteMessage(messageId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmMessageDeleteMessageMutationResult = NonNullable<Awaited<ReturnType<typeof admMessageDeleteMessage>>>;
export type AdmMessageDeleteMessageMutationBody = never;
export type AdmMessageDeleteMessageMutationError = unknown;

export const useAdmMessageDeleteMessage = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError, {messageId: number; params: AdmMessageDeleteMessageParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admMessageDeleteMessage>>, TError, {messageId: number; params: AdmMessageDeleteMessageParams}, TContext> => {
  return useMutation(getAdmMessageDeleteMessageMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admMessageDeleteMessage


// CPF PRE-RUNTIME FALLBACK START admMessageFindMessage
export type admMessageFindMessageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMessageFindMessageResponseSuccess = (admMessageFindMessageResponse200) & {
  headers: Headers;
};

export type admMessageFindMessageResponse = (admMessageFindMessageResponseSuccess)

export const getAdmMessageFindMessageUrl = (messageId: number) => `/adm/api/messages/${encodeURIComponent(String(messageId))}`;

export const admMessageFindMessage = async (messageId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admMessageFindMessageResponse> => {
  return cpfOrvalRequest<admMessageFindMessageResponse>(getAdmMessageFindMessageUrl(messageId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmMessageFindMessageQueryKey = (messageId: MaybeRefOrGetter<number>) => ["adm", "api", "messages", messageId] as const;

export const getAdmMessageFindMessageQueryOptions = <TData = Awaited<ReturnType<typeof admMessageFindMessage>>, TError = unknown>(
  messageId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmMessageFindMessageQueryKey(toValue(messageId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admMessageFindMessage>>> = ({ signal }) => admMessageFindMessage(toValue(messageId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(messageId) !== null && toValue(messageId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessage>>, TError, TData>;
};

export type AdmMessageFindMessageQueryResult = NonNullable<Awaited<ReturnType<typeof admMessageFindMessage>>>;
export type AdmMessageFindMessageQueryError = unknown;

export function useAdmMessageFindMessage<TData = Awaited<ReturnType<typeof admMessageFindMessage>>, TError = unknown>(
  messageId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admMessageFindMessage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmMessageFindMessageQueryOptions(messageId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admMessageFindMessage


// CPF PRE-RUNTIME FALLBACK START admMessageUpdateMessage
export type admMessageUpdateMessageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admMessageUpdateMessageResponseSuccess = (admMessageUpdateMessageResponse200) & {
  headers: Headers;
};

export type admMessageUpdateMessageResponse = (admMessageUpdateMessageResponseSuccess)

export const getAdmMessageUpdateMessageUrl = (messageId: number) => `/adm/api/messages/${encodeURIComponent(String(messageId))}`;

export const admMessageUpdateMessage = async (messageId: number, data: CommonMessageRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admMessageUpdateMessageResponse> => {
  return cpfOrvalRequest<admMessageUpdateMessageResponse>(getAdmMessageUpdateMessageUrl(messageId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmMessageUpdateMessageMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageUpdateMessage>>, TError, {messageId: number; data: CommonMessageRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admMessageUpdateMessage>>, TError, {messageId: number; data: CommonMessageRequest}, TContext> => {
  const mutationKey = ['admMessageUpdateMessage'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admMessageUpdateMessage>>, {messageId: number; data: CommonMessageRequest}> = (props) => {
    const { messageId, data } = props;
    return admMessageUpdateMessage(messageId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmMessageUpdateMessageMutationResult = NonNullable<Awaited<ReturnType<typeof admMessageUpdateMessage>>>;
export type AdmMessageUpdateMessageMutationBody = CommonMessageRequest;
export type AdmMessageUpdateMessageMutationError = unknown;

export const useAdmMessageUpdateMessage = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admMessageUpdateMessage>>, TError, {messageId: number; data: CommonMessageRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admMessageUpdateMessage>>, TError, {messageId: number; data: CommonMessageRequest}, TContext> => {
  return useMutation(getAdmMessageUpdateMessageMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admMessageUpdateMessage


// CPF PRE-RUNTIME FALLBACK START admNotificationFindDeliveryLogs
export type admNotificationFindDeliveryLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationFindDeliveryLogsResponseSuccess = (admNotificationFindDeliveryLogsResponse200) & {
  headers: Headers;
};

export type admNotificationFindDeliveryLogsResponse = (admNotificationFindDeliveryLogsResponseSuccess)

export const getAdmNotificationFindDeliveryLogsUrl = () => `/adm/api/notifications/delivery-logs`;

export const admNotificationFindDeliveryLogs = async (params?: AdmNotificationFindDeliveryLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationFindDeliveryLogsResponse> => {
  return cpfOrvalRequest<admNotificationFindDeliveryLogsResponse>(getAdmNotificationFindDeliveryLogsUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmNotificationFindDeliveryLogsQueryKey = (params?: MaybeRefOrGetter<AdmNotificationFindDeliveryLogsParams>) => ["adm", "api", "notifications", "delivery-logs", toValue(params)] as const;

export const getAdmNotificationFindDeliveryLogsQueryOptions = <TData = Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindDeliveryLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmNotificationFindDeliveryLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>> = ({ signal }) => admNotificationFindDeliveryLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>, TError, TData>;
};

export type AdmNotificationFindDeliveryLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>>;
export type AdmNotificationFindDeliveryLogsQueryError = unknown;

export function useAdmNotificationFindDeliveryLogs<TData = Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindDeliveryLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmNotificationFindDeliveryLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admNotificationFindDeliveryLogs


// CPF PRE-RUNTIME FALLBACK START admNotificationFindDlq
export type admNotificationFindDlqResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationFindDlqResponseSuccess = (admNotificationFindDlqResponse200) & {
  headers: Headers;
};

export type admNotificationFindDlqResponse = (admNotificationFindDlqResponseSuccess)

export const getAdmNotificationFindDlqUrl = () => `/adm/api/notifications/delivery-logs/dlq`;

export const admNotificationFindDlq = async (params?: AdmNotificationFindDlqParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationFindDlqResponse> => {
  return cpfOrvalRequest<admNotificationFindDlqResponse>(getAdmNotificationFindDlqUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmNotificationFindDlqQueryKey = (params?: MaybeRefOrGetter<AdmNotificationFindDlqParams>) => ["adm", "api", "notifications", "delivery-logs", "dlq", toValue(params)] as const;

export const getAdmNotificationFindDlqQueryOptions = <TData = Awaited<ReturnType<typeof admNotificationFindDlq>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindDlqParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDlq>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmNotificationFindDlqQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admNotificationFindDlq>>> = ({ signal }) => admNotificationFindDlq(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDlq>>, TError, TData>;
};

export type AdmNotificationFindDlqQueryResult = NonNullable<Awaited<ReturnType<typeof admNotificationFindDlq>>>;
export type AdmNotificationFindDlqQueryError = unknown;

export function useAdmNotificationFindDlq<TData = Awaited<ReturnType<typeof admNotificationFindDlq>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindDlqParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDlq>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmNotificationFindDlqQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admNotificationFindDlq


// CPF PRE-RUNTIME FALLBACK START admNotificationFindDeliveryAttempts
export type admNotificationFindDeliveryAttemptsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationFindDeliveryAttemptsResponseSuccess = (admNotificationFindDeliveryAttemptsResponse200) & {
  headers: Headers;
};

export type admNotificationFindDeliveryAttemptsResponse = (admNotificationFindDeliveryAttemptsResponseSuccess)

export const getAdmNotificationFindDeliveryAttemptsUrl = (deliveryId: number) => `/adm/api/notifications/delivery-logs/${encodeURIComponent(String(deliveryId))}/attempts`;

export const admNotificationFindDeliveryAttempts = async (deliveryId: number, params?: AdmNotificationFindDeliveryAttemptsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationFindDeliveryAttemptsResponse> => {
  return cpfOrvalRequest<admNotificationFindDeliveryAttemptsResponse>(getAdmNotificationFindDeliveryAttemptsUrl(deliveryId), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmNotificationFindDeliveryAttemptsQueryKey = (deliveryId: MaybeRefOrGetter<number>, params?: MaybeRefOrGetter<AdmNotificationFindDeliveryAttemptsParams>) => ["adm", "api", "notifications", "delivery-logs", deliveryId, "attempts", toValue(params)] as const;

export const getAdmNotificationFindDeliveryAttemptsQueryOptions = <TData = Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>, TError = unknown>(
  deliveryId: MaybeRefOrGetter<number>, params?: MaybeRefOrGetter<AdmNotificationFindDeliveryAttemptsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmNotificationFindDeliveryAttemptsQueryKey(toValue(deliveryId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>> = ({ signal }) => admNotificationFindDeliveryAttempts(toValue(deliveryId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(deliveryId) !== null && toValue(deliveryId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>, TError, TData>;
};

export type AdmNotificationFindDeliveryAttemptsQueryResult = NonNullable<Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>>;
export type AdmNotificationFindDeliveryAttemptsQueryError = unknown;

export function useAdmNotificationFindDeliveryAttempts<TData = Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>, TError = unknown>(
  deliveryId: MaybeRefOrGetter<number>, params?: MaybeRefOrGetter<AdmNotificationFindDeliveryAttemptsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindDeliveryAttempts>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmNotificationFindDeliveryAttemptsQueryOptions(deliveryId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admNotificationFindDeliveryAttempts


// CPF PRE-RUNTIME FALLBACK START admNotificationCancelDelivery
export type admNotificationCancelDeliveryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationCancelDeliveryResponseSuccess = (admNotificationCancelDeliveryResponse200) & {
  headers: Headers;
};

export type admNotificationCancelDeliveryResponse = (admNotificationCancelDeliveryResponseSuccess)

export const getAdmNotificationCancelDeliveryUrl = (deliveryId: number) => `/adm/api/notifications/delivery-logs/${encodeURIComponent(String(deliveryId))}/cancel`;

export const admNotificationCancelDelivery = async (deliveryId: number, params: AdmNotificationCancelDeliveryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationCancelDeliveryResponse> => {
  return cpfOrvalRequest<admNotificationCancelDeliveryResponse>(getAdmNotificationCancelDeliveryUrl(deliveryId), {
    ...options,
    method: 'POST',
    params: { expectedVersion: params.expectedVersion, reason: params.reason },
  });
};

export const getAdmNotificationCancelDeliveryMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationCancelDelivery>>, TError, {deliveryId: number; params: AdmNotificationCancelDeliveryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationCancelDelivery>>, TError, {deliveryId: number; params: AdmNotificationCancelDeliveryParams}, TContext> => {
  const mutationKey = ['admNotificationCancelDelivery'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationCancelDelivery>>, {deliveryId: number; params: AdmNotificationCancelDeliveryParams}> = (props) => {
    const { deliveryId, params } = props;
    return admNotificationCancelDelivery(deliveryId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationCancelDeliveryMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationCancelDelivery>>>;
export type AdmNotificationCancelDeliveryMutationBody = never;
export type AdmNotificationCancelDeliveryMutationError = unknown;

export const useAdmNotificationCancelDelivery = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationCancelDelivery>>, TError, {deliveryId: number; params: AdmNotificationCancelDeliveryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationCancelDelivery>>, TError, {deliveryId: number; params: AdmNotificationCancelDeliveryParams}, TContext> => {
  return useMutation(getAdmNotificationCancelDeliveryMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationCancelDelivery


// CPF PRE-RUNTIME FALLBACK START admNotificationRetryDelivery
export type admNotificationRetryDeliveryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationRetryDeliveryResponseSuccess = (admNotificationRetryDeliveryResponse200) & {
  headers: Headers;
};

export type admNotificationRetryDeliveryResponse = (admNotificationRetryDeliveryResponseSuccess)

export const getAdmNotificationRetryDeliveryUrl = (deliveryId: number) => `/adm/api/notifications/delivery-logs/${encodeURIComponent(String(deliveryId))}/retry`;

export const admNotificationRetryDelivery = async (deliveryId: number, params: AdmNotificationRetryDeliveryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationRetryDeliveryResponse> => {
  return cpfOrvalRequest<admNotificationRetryDeliveryResponse>(getAdmNotificationRetryDeliveryUrl(deliveryId), {
    ...options,
    method: 'POST',
    params: { expectedVersion: params.expectedVersion, reason: params.reason },
  });
};

export const getAdmNotificationRetryDeliveryMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationRetryDelivery>>, TError, {deliveryId: number; params: AdmNotificationRetryDeliveryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationRetryDelivery>>, TError, {deliveryId: number; params: AdmNotificationRetryDeliveryParams}, TContext> => {
  const mutationKey = ['admNotificationRetryDelivery'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationRetryDelivery>>, {deliveryId: number; params: AdmNotificationRetryDeliveryParams}> = (props) => {
    const { deliveryId, params } = props;
    return admNotificationRetryDelivery(deliveryId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationRetryDeliveryMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationRetryDelivery>>>;
export type AdmNotificationRetryDeliveryMutationBody = never;
export type AdmNotificationRetryDeliveryMutationError = unknown;

export const useAdmNotificationRetryDelivery = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationRetryDelivery>>, TError, {deliveryId: number; params: AdmNotificationRetryDeliveryParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationRetryDelivery>>, TError, {deliveryId: number; params: AdmNotificationRetryDeliveryParams}, TContext> => {
  return useMutation(getAdmNotificationRetryDeliveryMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationRetryDelivery


// CPF PRE-RUNTIME FALLBACK START admNotificationFindRules
export type admNotificationFindRulesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationFindRulesResponseSuccess = (admNotificationFindRulesResponse200) & {
  headers: Headers;
};

export type admNotificationFindRulesResponse = (admNotificationFindRulesResponseSuccess)

export const getAdmNotificationFindRulesUrl = () => `/adm/api/notifications/rules`;

export const admNotificationFindRules = async (params?: AdmNotificationFindRulesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationFindRulesResponse> => {
  return cpfOrvalRequest<admNotificationFindRulesResponse>(getAdmNotificationFindRulesUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getAdmNotificationFindRulesQueryKey = (params?: MaybeRefOrGetter<AdmNotificationFindRulesParams>) => ["adm", "api", "notifications", "rules", toValue(params)] as const;

export const getAdmNotificationFindRulesQueryOptions = <TData = Awaited<ReturnType<typeof admNotificationFindRules>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindRulesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmNotificationFindRulesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admNotificationFindRules>>> = ({ signal }) => admNotificationFindRules(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRules>>, TError, TData>;
};

export type AdmNotificationFindRulesQueryResult = NonNullable<Awaited<ReturnType<typeof admNotificationFindRules>>>;
export type AdmNotificationFindRulesQueryError = unknown;

export function useAdmNotificationFindRules<TData = Awaited<ReturnType<typeof admNotificationFindRules>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmNotificationFindRulesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRules>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmNotificationFindRulesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admNotificationFindRules


// CPF PRE-RUNTIME FALLBACK START admNotificationSaveRule
export type admNotificationSaveRuleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationSaveRuleResponseSuccess = (admNotificationSaveRuleResponse200) & {
  headers: Headers;
};

export type admNotificationSaveRuleResponse = (admNotificationSaveRuleResponseSuccess)

export const getAdmNotificationSaveRuleUrl = () => `/adm/api/notifications/rules`;

export const admNotificationSaveRule = async (data: AdmNotificationRuleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationSaveRuleResponse> => {
  return cpfOrvalRequest<admNotificationSaveRuleResponse>(getAdmNotificationSaveRuleUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmNotificationSaveRuleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationSaveRule>>, TError, {data: AdmNotificationRuleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationSaveRule>>, TError, {data: AdmNotificationRuleRequest}, TContext> => {
  const mutationKey = ['admNotificationSaveRule'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationSaveRule>>, {data: AdmNotificationRuleRequest}> = (props) => {
    const { data } = props;
    return admNotificationSaveRule(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationSaveRuleMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationSaveRule>>>;
export type AdmNotificationSaveRuleMutationBody = AdmNotificationRuleRequest;
export type AdmNotificationSaveRuleMutationError = unknown;

export const useAdmNotificationSaveRule = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationSaveRule>>, TError, {data: AdmNotificationRuleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationSaveRule>>, TError, {data: AdmNotificationRuleRequest}, TContext> => {
  return useMutation(getAdmNotificationSaveRuleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationSaveRule


// CPF PRE-RUNTIME FALLBACK START admNotificationFindRule
export type admNotificationFindRuleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationFindRuleResponseSuccess = (admNotificationFindRuleResponse200) & {
  headers: Headers;
};

export type admNotificationFindRuleResponse = (admNotificationFindRuleResponseSuccess)

export const getAdmNotificationFindRuleUrl = (ruleId: number) => `/adm/api/notifications/rules/${encodeURIComponent(String(ruleId))}`;

export const admNotificationFindRule = async (ruleId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationFindRuleResponse> => {
  return cpfOrvalRequest<admNotificationFindRuleResponse>(getAdmNotificationFindRuleUrl(ruleId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmNotificationFindRuleQueryKey = (ruleId: MaybeRefOrGetter<number>) => ["adm", "api", "notifications", "rules", ruleId] as const;

export const getAdmNotificationFindRuleQueryOptions = <TData = Awaited<ReturnType<typeof admNotificationFindRule>>, TError = unknown>(
  ruleId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRule>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmNotificationFindRuleQueryKey(toValue(ruleId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admNotificationFindRule>>> = ({ signal }) => admNotificationFindRule(toValue(ruleId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(ruleId) !== null && toValue(ruleId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRule>>, TError, TData>;
};

export type AdmNotificationFindRuleQueryResult = NonNullable<Awaited<ReturnType<typeof admNotificationFindRule>>>;
export type AdmNotificationFindRuleQueryError = unknown;

export function useAdmNotificationFindRule<TData = Awaited<ReturnType<typeof admNotificationFindRule>>, TError = unknown>(
  ruleId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admNotificationFindRule>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmNotificationFindRuleQueryOptions(ruleId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admNotificationFindRule


// CPF PRE-RUNTIME FALLBACK START admNotificationUpdateRule
export type admNotificationUpdateRuleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationUpdateRuleResponseSuccess = (admNotificationUpdateRuleResponse200) & {
  headers: Headers;
};

export type admNotificationUpdateRuleResponse = (admNotificationUpdateRuleResponseSuccess)

export const getAdmNotificationUpdateRuleUrl = (ruleId: number) => `/adm/api/notifications/rules/${encodeURIComponent(String(ruleId))}`;

export const admNotificationUpdateRule = async (ruleId: number, data: AdmNotificationRuleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationUpdateRuleResponse> => {
  return cpfOrvalRequest<admNotificationUpdateRuleResponse>(getAdmNotificationUpdateRuleUrl(ruleId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmNotificationUpdateRuleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationUpdateRule>>, TError, {ruleId: number; data: AdmNotificationRuleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationUpdateRule>>, TError, {ruleId: number; data: AdmNotificationRuleRequest}, TContext> => {
  const mutationKey = ['admNotificationUpdateRule'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationUpdateRule>>, {ruleId: number; data: AdmNotificationRuleRequest}> = (props) => {
    const { ruleId, data } = props;
    return admNotificationUpdateRule(ruleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationUpdateRuleMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationUpdateRule>>>;
export type AdmNotificationUpdateRuleMutationBody = AdmNotificationRuleRequest;
export type AdmNotificationUpdateRuleMutationError = unknown;

export const useAdmNotificationUpdateRule = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationUpdateRule>>, TError, {ruleId: number; data: AdmNotificationRuleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationUpdateRule>>, TError, {ruleId: number; data: AdmNotificationRuleRequest}, TContext> => {
  return useMutation(getAdmNotificationUpdateRuleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationUpdateRule


// CPF PRE-RUNTIME FALLBACK START admNotificationDisableRule
export type admNotificationDisableRuleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationDisableRuleResponseSuccess = (admNotificationDisableRuleResponse200) & {
  headers: Headers;
};

export type admNotificationDisableRuleResponse = (admNotificationDisableRuleResponseSuccess)

export const getAdmNotificationDisableRuleUrl = (ruleId: number) => `/adm/api/notifications/rules/${encodeURIComponent(String(ruleId))}/disable`;

export const admNotificationDisableRule = async (ruleId: number, params: AdmNotificationDisableRuleParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationDisableRuleResponse> => {
  return cpfOrvalRequest<admNotificationDisableRuleResponse>(getAdmNotificationDisableRuleUrl(ruleId), {
    ...options,
    method: 'PUT',
    params: { reason: params.reason },
  });
};

export const getAdmNotificationDisableRuleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationDisableRule>>, TError, {ruleId: number; params: AdmNotificationDisableRuleParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationDisableRule>>, TError, {ruleId: number; params: AdmNotificationDisableRuleParams}, TContext> => {
  const mutationKey = ['admNotificationDisableRule'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationDisableRule>>, {ruleId: number; params: AdmNotificationDisableRuleParams}> = (props) => {
    const { ruleId, params } = props;
    return admNotificationDisableRule(ruleId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationDisableRuleMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationDisableRule>>>;
export type AdmNotificationDisableRuleMutationBody = never;
export type AdmNotificationDisableRuleMutationError = unknown;

export const useAdmNotificationDisableRule = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationDisableRule>>, TError, {ruleId: number; params: AdmNotificationDisableRuleParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationDisableRule>>, TError, {ruleId: number; params: AdmNotificationDisableRuleParams}, TContext> => {
  return useMutation(getAdmNotificationDisableRuleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationDisableRule


// CPF PRE-RUNTIME FALLBACK START admNotificationSendTest
export type admNotificationSendTestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admNotificationSendTestResponseSuccess = (admNotificationSendTestResponse200) & {
  headers: Headers;
};

export type admNotificationSendTestResponse = (admNotificationSendTestResponseSuccess)

export const getAdmNotificationSendTestUrl = (ruleId: number) => `/adm/api/notifications/rules/${encodeURIComponent(String(ruleId))}/test-send`;

export const admNotificationSendTest = async (ruleId: number, data: AdmNotificationTestSendRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admNotificationSendTestResponse> => {
  return cpfOrvalRequest<admNotificationSendTestResponse>(getAdmNotificationSendTestUrl(ruleId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmNotificationSendTestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationSendTest>>, TError, {ruleId: number; data: AdmNotificationTestSendRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admNotificationSendTest>>, TError, {ruleId: number; data: AdmNotificationTestSendRequest}, TContext> => {
  const mutationKey = ['admNotificationSendTest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admNotificationSendTest>>, {ruleId: number; data: AdmNotificationTestSendRequest}> = (props) => {
    const { ruleId, data } = props;
    return admNotificationSendTest(ruleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmNotificationSendTestMutationResult = NonNullable<Awaited<ReturnType<typeof admNotificationSendTest>>>;
export type AdmNotificationSendTestMutationBody = AdmNotificationTestSendRequest;
export type AdmNotificationSendTestMutationError = unknown;

export const useAdmNotificationSendTest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admNotificationSendTest>>, TError, {ruleId: number; data: AdmNotificationTestSendRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admNotificationSendTest>>, TError, {ruleId: number; data: AdmNotificationTestSendRequest}, TContext> => {
  return useMutation(getAdmNotificationSendTestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admNotificationSendTest


// CPF PRE-RUNTIME FALLBACK START traceAdmByBusinessTransactionId
export type traceAdmByBusinessTransactionIdResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type traceAdmByBusinessTransactionIdResponseSuccess = (traceAdmByBusinessTransactionIdResponse200) & {
  headers: Headers;
};

export type traceAdmByBusinessTransactionIdResponse = (traceAdmByBusinessTransactionIdResponseSuccess)

export const getTraceAdmByBusinessTransactionIdUrl = (businessTransactionId: string) => `/adm/api/observability/business-transactions/${encodeURIComponent(String(businessTransactionId))}`;

export const traceAdmByBusinessTransactionId = async (businessTransactionId: string, params?: TraceAdmByBusinessTransactionIdParams, options?: CpfOrvalGeneratedRequestOptions): Promise<traceAdmByBusinessTransactionIdResponse> => {
  return cpfOrvalRequest<traceAdmByBusinessTransactionIdResponse>(getTraceAdmByBusinessTransactionIdUrl(businessTransactionId), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getTraceAdmByBusinessTransactionIdQueryKey = (businessTransactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByBusinessTransactionIdParams>) => ["adm", "api", "observability", "business-transactions", businessTransactionId, toValue(params)] as const;

export const getTraceAdmByBusinessTransactionIdQueryOptions = <TData = Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>, TError = unknown>(
  businessTransactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByBusinessTransactionIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getTraceAdmByBusinessTransactionIdQueryKey(toValue(businessTransactionId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>> = ({ signal }) => traceAdmByBusinessTransactionId(toValue(businessTransactionId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(businessTransactionId) !== null && toValue(businessTransactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>, TError, TData>;
};

export type TraceAdmByBusinessTransactionIdQueryResult = NonNullable<Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>>;
export type TraceAdmByBusinessTransactionIdQueryError = unknown;

export function useTraceAdmByBusinessTransactionId<TData = Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>, TError = unknown>(
  businessTransactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByBusinessTransactionIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByBusinessTransactionId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getTraceAdmByBusinessTransactionIdQueryOptions(businessTransactionId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END traceAdmByBusinessTransactionId


// CPF PRE-RUNTIME FALLBACK START traceAdmByTraceId
export type traceAdmByTraceIdResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type traceAdmByTraceIdResponseSuccess = (traceAdmByTraceIdResponse200) & {
  headers: Headers;
};

export type traceAdmByTraceIdResponse = (traceAdmByTraceIdResponseSuccess)

export const getTraceAdmByTraceIdUrl = (traceId: string) => `/adm/api/observability/traces/${encodeURIComponent(String(traceId))}`;

export const traceAdmByTraceId = async (traceId: string, params?: TraceAdmByTraceIdParams, options?: CpfOrvalGeneratedRequestOptions): Promise<traceAdmByTraceIdResponse> => {
  return cpfOrvalRequest<traceAdmByTraceIdResponse>(getTraceAdmByTraceIdUrl(traceId), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getTraceAdmByTraceIdQueryKey = (traceId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTraceIdParams>) => ["adm", "api", "observability", "traces", traceId, toValue(params)] as const;

export const getTraceAdmByTraceIdQueryOptions = <TData = Awaited<ReturnType<typeof traceAdmByTraceId>>, TError = unknown>(
  traceId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTraceIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTraceId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getTraceAdmByTraceIdQueryKey(toValue(traceId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof traceAdmByTraceId>>> = ({ signal }) => traceAdmByTraceId(toValue(traceId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(traceId) !== null && toValue(traceId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTraceId>>, TError, TData>;
};

export type TraceAdmByTraceIdQueryResult = NonNullable<Awaited<ReturnType<typeof traceAdmByTraceId>>>;
export type TraceAdmByTraceIdQueryError = unknown;

export function useTraceAdmByTraceId<TData = Awaited<ReturnType<typeof traceAdmByTraceId>>, TError = unknown>(
  traceId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTraceIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTraceId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getTraceAdmByTraceIdQueryOptions(traceId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END traceAdmByTraceId


// CPF PRE-RUNTIME FALLBACK START traceAdmByTransactionId
export type traceAdmByTransactionIdResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type traceAdmByTransactionIdResponseSuccess = (traceAdmByTransactionIdResponse200) & {
  headers: Headers;
};

export type traceAdmByTransactionIdResponse = (traceAdmByTransactionIdResponseSuccess)

export const getTraceAdmByTransactionIdUrl = (transactionId: string) => `/adm/api/observability/transactions/${encodeURIComponent(String(transactionId))}`;

export const traceAdmByTransactionId = async (transactionId: string, params?: TraceAdmByTransactionIdParams, options?: CpfOrvalGeneratedRequestOptions): Promise<traceAdmByTransactionIdResponse> => {
  return cpfOrvalRequest<traceAdmByTransactionIdResponse>(getTraceAdmByTransactionIdUrl(transactionId), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getTraceAdmByTransactionIdQueryKey = (transactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTransactionIdParams>) => ["adm", "api", "observability", "transactions", transactionId, toValue(params)] as const;

export const getTraceAdmByTransactionIdQueryOptions = <TData = Awaited<ReturnType<typeof traceAdmByTransactionId>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTransactionIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTransactionId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getTraceAdmByTransactionIdQueryKey(toValue(transactionId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof traceAdmByTransactionId>>> = ({ signal }) => traceAdmByTransactionId(toValue(transactionId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTransactionId>>, TError, TData>;
};

export type TraceAdmByTransactionIdQueryResult = NonNullable<Awaited<ReturnType<typeof traceAdmByTransactionId>>>;
export type TraceAdmByTransactionIdQueryError = unknown;

export function useTraceAdmByTransactionId<TData = Awaited<ReturnType<typeof traceAdmByTransactionId>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<TraceAdmByTransactionIdParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof traceAdmByTransactionId>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getTraceAdmByTransactionIdQueryOptions(transactionId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END traceAdmByTransactionId


// CPF PRE-RUNTIME FALLBACK START admOpenApiRefresh
export type admOpenApiRefreshResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOpenApiRefreshResponseSuccess = (admOpenApiRefreshResponse200) & {
  headers: Headers;
};

export type admOpenApiRefreshResponse = (admOpenApiRefreshResponseSuccess)

export const getAdmOpenApiRefreshUrl = () => `/adm/api/openapi/refresh`;

export const admOpenApiRefresh = async (data: AdmOpenApiRefreshRequest, params: AdmOpenApiRefreshParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admOpenApiRefreshResponse> => {
  return cpfOrvalRequest<admOpenApiRefreshResponse>(getAdmOpenApiRefreshUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmOpenApiRefreshMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOpenApiRefresh>>, TError, {data: AdmOpenApiRefreshRequest; params: AdmOpenApiRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOpenApiRefresh>>, TError, {data: AdmOpenApiRefreshRequest; params: AdmOpenApiRefreshParams}, TContext> => {
  const mutationKey = ['admOpenApiRefresh'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOpenApiRefresh>>, {data: AdmOpenApiRefreshRequest; params: AdmOpenApiRefreshParams}> = (props) => {
    const { data, params } = props;
    return admOpenApiRefresh(data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOpenApiRefreshMutationResult = NonNullable<Awaited<ReturnType<typeof admOpenApiRefresh>>>;
export type AdmOpenApiRefreshMutationBody = AdmOpenApiRefreshRequest;
export type AdmOpenApiRefreshMutationError = unknown;

export const useAdmOpenApiRefresh = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOpenApiRefresh>>, TError, {data: AdmOpenApiRefreshRequest; params: AdmOpenApiRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOpenApiRefresh>>, TError, {data: AdmOpenApiRefreshRequest; params: AdmOpenApiRefreshParams}, TContext> => {
  return useMutation(getAdmOpenApiRefreshMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOpenApiRefresh


// CPF PRE-RUNTIME FALLBACK START admOpenApiStatus
export type admOpenApiStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOpenApiStatusResponseSuccess = (admOpenApiStatusResponse200) & {
  headers: Headers;
};

export type admOpenApiStatusResponse = (admOpenApiStatusResponseSuccess)

export const getAdmOpenApiStatusUrl = () => `/adm/api/openapi/status`;

export const admOpenApiStatus = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOpenApiStatusResponse> => {
  return cpfOrvalRequest<admOpenApiStatusResponse>(getAdmOpenApiStatusUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOpenApiStatusQueryKey = () => ["adm", "api", "openapi", "status"] as const;

export const getAdmOpenApiStatusQueryOptions = <TData = Awaited<ReturnType<typeof admOpenApiStatus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOpenApiStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOpenApiStatusQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOpenApiStatus>>> = ({ signal }) => admOpenApiStatus({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOpenApiStatus>>, TError, TData>;
};

export type AdmOpenApiStatusQueryResult = NonNullable<Awaited<ReturnType<typeof admOpenApiStatus>>>;
export type AdmOpenApiStatusQueryError = unknown;

export function useAdmOpenApiStatus<TData = Awaited<ReturnType<typeof admOpenApiStatus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOpenApiStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOpenApiStatusQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOpenApiStatus


// CPF PRE-RUNTIME FALLBACK START admOperatorFindOperators
export type admOperatorFindOperatorsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorFindOperatorsResponseSuccess = (admOperatorFindOperatorsResponse200) & {
  headers: Headers;
};

export type admOperatorFindOperatorsResponse = (admOperatorFindOperatorsResponseSuccess)

export const getAdmOperatorFindOperatorsUrl = () => `/adm/api/operators`;

export const admOperatorFindOperators = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorFindOperatorsResponse> => {
  return cpfOrvalRequest<admOperatorFindOperatorsResponse>(getAdmOperatorFindOperatorsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorFindOperatorsQueryKey = () => ["adm", "api", "operators"] as const;

export const getAdmOperatorFindOperatorsQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorFindOperators>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindOperators>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorFindOperatorsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorFindOperators>>> = ({ signal }) => admOperatorFindOperators({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindOperators>>, TError, TData>;
};

export type AdmOperatorFindOperatorsQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorFindOperators>>>;
export type AdmOperatorFindOperatorsQueryError = unknown;

export function useAdmOperatorFindOperators<TData = Awaited<ReturnType<typeof admOperatorFindOperators>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindOperators>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorFindOperatorsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorFindOperators


// CPF PRE-RUNTIME FALLBACK START admOperatorCreateOperator
export type admOperatorCreateOperatorResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorCreateOperatorResponseSuccess = (admOperatorCreateOperatorResponse200) & {
  headers: Headers;
};

export type admOperatorCreateOperatorResponse = (admOperatorCreateOperatorResponseSuccess)

export const getAdmOperatorCreateOperatorUrl = () => `/adm/api/operators`;

export const admOperatorCreateOperator = async (data: AdmOperatorCreateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorCreateOperatorResponse> => {
  return cpfOrvalRequest<admOperatorCreateOperatorResponse>(getAdmOperatorCreateOperatorUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorCreateOperatorMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorCreateOperator>>, TError, {data: AdmOperatorCreateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorCreateOperator>>, TError, {data: AdmOperatorCreateRequest}, TContext> => {
  const mutationKey = ['admOperatorCreateOperator'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorCreateOperator>>, {data: AdmOperatorCreateRequest}> = (props) => {
    const { data } = props;
    return admOperatorCreateOperator(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorCreateOperatorMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorCreateOperator>>>;
export type AdmOperatorCreateOperatorMutationBody = AdmOperatorCreateRequest;
export type AdmOperatorCreateOperatorMutationError = unknown;

export const useAdmOperatorCreateOperator = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorCreateOperator>>, TError, {data: AdmOperatorCreateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorCreateOperator>>, TError, {data: AdmOperatorCreateRequest}, TContext> => {
  return useMutation(getAdmOperatorCreateOperatorMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorCreateOperator


// CPF PRE-RUNTIME FALLBACK START admOperatorFindMenus
export type admOperatorFindMenusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorFindMenusResponseSuccess = (admOperatorFindMenusResponse200) & {
  headers: Headers;
};

export type admOperatorFindMenusResponse = (admOperatorFindMenusResponseSuccess)

export const getAdmOperatorFindMenusUrl = () => `/adm/api/operators/menus`;

export const admOperatorFindMenus = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorFindMenusResponse> => {
  return cpfOrvalRequest<admOperatorFindMenusResponse>(getAdmOperatorFindMenusUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorFindMenusQueryKey = () => ["adm", "api", "operators", "menus"] as const;

export const getAdmOperatorFindMenusQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorFindMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorFindMenusQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorFindMenus>>> = ({ signal }) => admOperatorFindMenus({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindMenus>>, TError, TData>;
};

export type AdmOperatorFindMenusQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorFindMenus>>>;
export type AdmOperatorFindMenusQueryError = unknown;

export function useAdmOperatorFindMenus<TData = Awaited<ReturnType<typeof admOperatorFindMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorFindMenusQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorFindMenus


// CPF PRE-RUNTIME FALLBACK START admOperatorFindCreateResult
export type admOperatorFindCreateResultResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorFindCreateResultResponseSuccess = (admOperatorFindCreateResultResponse200) & {
  headers: Headers;
};

export type admOperatorFindCreateResultResponse = (admOperatorFindCreateResultResponseSuccess)

export const getAdmOperatorFindCreateResultUrl = (operationId: string) => `/adm/api/operators/operations/${encodeURIComponent(String(operationId))}`;

export const admOperatorFindCreateResult = async (operationId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorFindCreateResultResponse> => {
  return cpfOrvalRequest<admOperatorFindCreateResultResponse>(getAdmOperatorFindCreateResultUrl(operationId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorFindCreateResultQueryKey = (operationId: MaybeRefOrGetter<string>) => ["adm", "api", "operators", "operations", operationId] as const;

export const getAdmOperatorFindCreateResultQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorFindCreateResult>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindCreateResult>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorFindCreateResultQueryKey(toValue(operationId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorFindCreateResult>>> = ({ signal }) => admOperatorFindCreateResult(toValue(operationId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(operationId) !== null && toValue(operationId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindCreateResult>>, TError, TData>;
};

export type AdmOperatorFindCreateResultQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorFindCreateResult>>>;
export type AdmOperatorFindCreateResultQueryError = unknown;

export function useAdmOperatorFindCreateResult<TData = Awaited<ReturnType<typeof admOperatorFindCreateResult>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindCreateResult>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorFindCreateResultQueryOptions(operationId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorFindCreateResult


// CPF PRE-RUNTIME FALLBACK START admOperatorPasswordPolicy
export type admOperatorPasswordPolicyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorPasswordPolicyResponseSuccess = (admOperatorPasswordPolicyResponse200) & {
  headers: Headers;
};

export type admOperatorPasswordPolicyResponse = (admOperatorPasswordPolicyResponseSuccess)

export const getAdmOperatorPasswordPolicyUrl = () => `/adm/api/operators/password-policy`;

export const admOperatorPasswordPolicy = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorPasswordPolicyResponse> => {
  return cpfOrvalRequest<admOperatorPasswordPolicyResponse>(getAdmOperatorPasswordPolicyUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorPasswordPolicyQueryKey = () => ["adm", "api", "operators", "password-policy"] as const;

export const getAdmOperatorPasswordPolicyQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorPasswordPolicy>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorPasswordPolicy>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorPasswordPolicyQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorPasswordPolicy>>> = ({ signal }) => admOperatorPasswordPolicy({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorPasswordPolicy>>, TError, TData>;
};

export type AdmOperatorPasswordPolicyQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorPasswordPolicy>>>;
export type AdmOperatorPasswordPolicyQueryError = unknown;

export function useAdmOperatorPasswordPolicy<TData = Awaited<ReturnType<typeof admOperatorPasswordPolicy>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorPasswordPolicy>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorPasswordPolicyQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorPasswordPolicy


// CPF PRE-RUNTIME FALLBACK START admOperatorValidatePassword
export type admOperatorValidatePasswordResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorValidatePasswordResponseSuccess = (admOperatorValidatePasswordResponse200) & {
  headers: Headers;
};

export type admOperatorValidatePasswordResponse = (admOperatorValidatePasswordResponseSuccess)

export const getAdmOperatorValidatePasswordUrl = () => `/adm/api/operators/password-policy/validate`;

export const admOperatorValidatePassword = async (params: AdmOperatorValidatePasswordParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorValidatePasswordResponse> => {
  return cpfOrvalRequest<admOperatorValidatePasswordResponse>(getAdmOperatorValidatePasswordUrl(), {
    ...options,
    method: 'GET',
    params: { password: params.password },
  });
};

export const getAdmOperatorValidatePasswordQueryKey = (params: MaybeRefOrGetter<AdmOperatorValidatePasswordParams>) => ["adm", "api", "operators", "password-policy", "validate", toValue(params)] as const;

export const getAdmOperatorValidatePasswordQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorValidatePassword>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmOperatorValidatePasswordParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorValidatePassword>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorValidatePasswordQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorValidatePassword>>> = ({ signal }) => admOperatorValidatePassword(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorValidatePassword>>, TError, TData>;
};

export type AdmOperatorValidatePasswordQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorValidatePassword>>>;
export type AdmOperatorValidatePasswordQueryError = unknown;

export function useAdmOperatorValidatePassword<TData = Awaited<ReturnType<typeof admOperatorValidatePassword>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmOperatorValidatePasswordParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorValidatePassword>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorValidatePasswordQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorValidatePassword


// CPF PRE-RUNTIME FALLBACK START admOperatorFindRoles
export type admOperatorFindRolesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorFindRolesResponseSuccess = (admOperatorFindRolesResponse200) & {
  headers: Headers;
};

export type admOperatorFindRolesResponse = (admOperatorFindRolesResponseSuccess)

export const getAdmOperatorFindRolesUrl = () => `/adm/api/operators/roles`;

export const admOperatorFindRoles = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorFindRolesResponse> => {
  return cpfOrvalRequest<admOperatorFindRolesResponse>(getAdmOperatorFindRolesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorFindRolesQueryKey = () => ["adm", "api", "operators", "roles"] as const;

export const getAdmOperatorFindRolesQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorFindRolesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorFindRoles>>> = ({ signal }) => admOperatorFindRoles({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindRoles>>, TError, TData>;
};

export type AdmOperatorFindRolesQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorFindRoles>>>;
export type AdmOperatorFindRolesQueryError = unknown;

export function useAdmOperatorFindRoles<TData = Awaited<ReturnType<typeof admOperatorFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorFindRolesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorFindRoles


// CPF PRE-RUNTIME FALLBACK START admOperatorFindSessions
export type admOperatorFindSessionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorFindSessionsResponseSuccess = (admOperatorFindSessionsResponse200) & {
  headers: Headers;
};

export type admOperatorFindSessionsResponse = (admOperatorFindSessionsResponseSuccess)

export const getAdmOperatorFindSessionsUrl = () => `/adm/api/operators/sessions`;

export const admOperatorFindSessions = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorFindSessionsResponse> => {
  return cpfOrvalRequest<admOperatorFindSessionsResponse>(getAdmOperatorFindSessionsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmOperatorFindSessionsQueryKey = () => ["adm", "api", "operators", "sessions"] as const;

export const getAdmOperatorFindSessionsQueryOptions = <TData = Awaited<ReturnType<typeof admOperatorFindSessions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmOperatorFindSessionsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admOperatorFindSessions>>> = ({ signal }) => admOperatorFindSessions({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindSessions>>, TError, TData>;
};

export type AdmOperatorFindSessionsQueryResult = NonNullable<Awaited<ReturnType<typeof admOperatorFindSessions>>>;
export type AdmOperatorFindSessionsQueryError = unknown;

export function useAdmOperatorFindSessions<TData = Awaited<ReturnType<typeof admOperatorFindSessions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admOperatorFindSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmOperatorFindSessionsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admOperatorFindSessions


// CPF PRE-RUNTIME FALLBACK START admOperatorCleanupExpiredSessions
export type admOperatorCleanupExpiredSessionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorCleanupExpiredSessionsResponseSuccess = (admOperatorCleanupExpiredSessionsResponse200) & {
  headers: Headers;
};

export type admOperatorCleanupExpiredSessionsResponse = (admOperatorCleanupExpiredSessionsResponseSuccess)

export const getAdmOperatorCleanupExpiredSessionsUrl = () => `/adm/api/operators/sessions/cleanup-expired`;

export const admOperatorCleanupExpiredSessions = async (data: AdmSessionRevokeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorCleanupExpiredSessionsResponse> => {
  return cpfOrvalRequest<admOperatorCleanupExpiredSessionsResponse>(getAdmOperatorCleanupExpiredSessionsUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorCleanupExpiredSessionsMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>, TError, {data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>, TError, {data: AdmSessionRevokeRequest}, TContext> => {
  const mutationKey = ['admOperatorCleanupExpiredSessions'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>, {data: AdmSessionRevokeRequest}> = (props) => {
    const { data } = props;
    return admOperatorCleanupExpiredSessions(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorCleanupExpiredSessionsMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>>;
export type AdmOperatorCleanupExpiredSessionsMutationBody = AdmSessionRevokeRequest;
export type AdmOperatorCleanupExpiredSessionsMutationError = unknown;

export const useAdmOperatorCleanupExpiredSessions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>, TError, {data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorCleanupExpiredSessions>>, TError, {data: AdmSessionRevokeRequest}, TContext> => {
  return useMutation(getAdmOperatorCleanupExpiredSessionsMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorCleanupExpiredSessions


// CPF PRE-RUNTIME FALLBACK START admOperatorRevokeSession
export type admOperatorRevokeSessionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorRevokeSessionResponseSuccess = (admOperatorRevokeSessionResponse200) & {
  headers: Headers;
};

export type admOperatorRevokeSessionResponse = (admOperatorRevokeSessionResponseSuccess)

export const getAdmOperatorRevokeSessionUrl = (sessionId: string) => `/adm/api/operators/sessions/${encodeURIComponent(String(sessionId))}/revoke`;

export const admOperatorRevokeSession = async (sessionId: string, data: AdmSessionRevokeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorRevokeSessionResponse> => {
  return cpfOrvalRequest<admOperatorRevokeSessionResponse>(getAdmOperatorRevokeSessionUrl(sessionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorRevokeSessionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorRevokeSession>>, TError, {sessionId: string; data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorRevokeSession>>, TError, {sessionId: string; data: AdmSessionRevokeRequest}, TContext> => {
  const mutationKey = ['admOperatorRevokeSession'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorRevokeSession>>, {sessionId: string; data: AdmSessionRevokeRequest}> = (props) => {
    const { sessionId, data } = props;
    return admOperatorRevokeSession(sessionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorRevokeSessionMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorRevokeSession>>>;
export type AdmOperatorRevokeSessionMutationBody = AdmSessionRevokeRequest;
export type AdmOperatorRevokeSessionMutationError = unknown;

export const useAdmOperatorRevokeSession = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorRevokeSession>>, TError, {sessionId: string; data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorRevokeSession>>, TError, {sessionId: string; data: AdmSessionRevokeRequest}, TContext> => {
  return useMutation(getAdmOperatorRevokeSessionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorRevokeSession


// CPF PRE-RUNTIME FALLBACK START admOperatorUpdateContact
export type admOperatorUpdateContactResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorUpdateContactResponseSuccess = (admOperatorUpdateContactResponse200) & {
  headers: Headers;
};

export type admOperatorUpdateContactResponse = (admOperatorUpdateContactResponseSuccess)

export const getAdmOperatorUpdateContactUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/contacts`;

export const admOperatorUpdateContact = async (operatorId: string, data: AdmOperatorContactUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorUpdateContactResponse> => {
  return cpfOrvalRequest<admOperatorUpdateContactResponse>(getAdmOperatorUpdateContactUrl(operatorId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorUpdateContactMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateContact>>, TError, {operatorId: string; data: AdmOperatorContactUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateContact>>, TError, {operatorId: string; data: AdmOperatorContactUpdateRequest}, TContext> => {
  const mutationKey = ['admOperatorUpdateContact'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorUpdateContact>>, {operatorId: string; data: AdmOperatorContactUpdateRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorUpdateContact(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorUpdateContactMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorUpdateContact>>>;
export type AdmOperatorUpdateContactMutationBody = AdmOperatorContactUpdateRequest;
export type AdmOperatorUpdateContactMutationError = unknown;

export const useAdmOperatorUpdateContact = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateContact>>, TError, {operatorId: string; data: AdmOperatorContactUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorUpdateContact>>, TError, {operatorId: string; data: AdmOperatorContactUpdateRequest}, TContext> => {
  return useMutation(getAdmOperatorUpdateContactMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorUpdateContact


// CPF PRE-RUNTIME FALLBACK START admOperatorRawContact
export type admOperatorRawContactResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorRawContactResponseSuccess = (admOperatorRawContactResponse200) & {
  headers: Headers;
};

export type admOperatorRawContactResponse = (admOperatorRawContactResponseSuccess)

export const getAdmOperatorRawContactUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/contacts/raw`;

export const admOperatorRawContact = async (operatorId: string, data: CpfSensitiveDataAccessRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorRawContactResponse> => {
  return cpfOrvalRequest<admOperatorRawContactResponse>(getAdmOperatorRawContactUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorRawContactMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorRawContact>>, TError, {operatorId: string; data: CpfSensitiveDataAccessRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorRawContact>>, TError, {operatorId: string; data: CpfSensitiveDataAccessRequest}, TContext> => {
  const mutationKey = ['admOperatorRawContact'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorRawContact>>, {operatorId: string; data: CpfSensitiveDataAccessRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorRawContact(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorRawContactMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorRawContact>>>;
export type AdmOperatorRawContactMutationBody = CpfSensitiveDataAccessRequest;
export type AdmOperatorRawContactMutationError = unknown;

export const useAdmOperatorRawContact = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorRawContact>>, TError, {operatorId: string; data: CpfSensitiveDataAccessRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorRawContact>>, TError, {operatorId: string; data: CpfSensitiveDataAccessRequest}, TContext> => {
  return useMutation(getAdmOperatorRawContactMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorRawContact


// CPF PRE-RUNTIME FALLBACK START admOperatorChangePassword
export type admOperatorChangePasswordResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorChangePasswordResponseSuccess = (admOperatorChangePasswordResponse200) & {
  headers: Headers;
};

export type admOperatorChangePasswordResponse = (admOperatorChangePasswordResponseSuccess)

export const getAdmOperatorChangePasswordUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/password`;

export const admOperatorChangePassword = async (operatorId: string, data: AdmPasswordChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorChangePasswordResponse> => {
  return cpfOrvalRequest<admOperatorChangePasswordResponse>(getAdmOperatorChangePasswordUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorChangePasswordMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorChangePassword>>, TError, {operatorId: string; data: AdmPasswordChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorChangePassword>>, TError, {operatorId: string; data: AdmPasswordChangeRequest}, TContext> => {
  const mutationKey = ['admOperatorChangePassword'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorChangePassword>>, {operatorId: string; data: AdmPasswordChangeRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorChangePassword(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorChangePasswordMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorChangePassword>>>;
export type AdmOperatorChangePasswordMutationBody = AdmPasswordChangeRequest;
export type AdmOperatorChangePasswordMutationError = unknown;

export const useAdmOperatorChangePassword = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorChangePassword>>, TError, {operatorId: string; data: AdmPasswordChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorChangePassword>>, TError, {operatorId: string; data: AdmPasswordChangeRequest}, TContext> => {
  return useMutation(getAdmOperatorChangePasswordMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorChangePassword


// CPF PRE-RUNTIME FALLBACK START admOperatorResetPassword
export type admOperatorResetPasswordResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorResetPasswordResponseSuccess = (admOperatorResetPasswordResponse200) & {
  headers: Headers;
};

export type admOperatorResetPasswordResponse = (admOperatorResetPasswordResponseSuccess)

export const getAdmOperatorResetPasswordUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/password/reset`;

export const admOperatorResetPassword = async (operatorId: string, data: AdmOperatorPasswordResetRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorResetPasswordResponse> => {
  return cpfOrvalRequest<admOperatorResetPasswordResponse>(getAdmOperatorResetPasswordUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorResetPasswordMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorResetPassword>>, TError, {operatorId: string; data: AdmOperatorPasswordResetRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorResetPassword>>, TError, {operatorId: string; data: AdmOperatorPasswordResetRequest}, TContext> => {
  const mutationKey = ['admOperatorResetPassword'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorResetPassword>>, {operatorId: string; data: AdmOperatorPasswordResetRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorResetPassword(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorResetPasswordMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorResetPassword>>>;
export type AdmOperatorResetPasswordMutationBody = AdmOperatorPasswordResetRequest;
export type AdmOperatorResetPasswordMutationError = unknown;

export const useAdmOperatorResetPassword = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorResetPassword>>, TError, {operatorId: string; data: AdmOperatorPasswordResetRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorResetPassword>>, TError, {operatorId: string; data: AdmOperatorPasswordResetRequest}, TContext> => {
  return useMutation(getAdmOperatorResetPasswordMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorResetPassword


// CPF PRE-RUNTIME FALLBACK START admOperatorUpdateRoles
export type admOperatorUpdateRolesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorUpdateRolesResponseSuccess = (admOperatorUpdateRolesResponse200) & {
  headers: Headers;
};

export type admOperatorUpdateRolesResponse = (admOperatorUpdateRolesResponseSuccess)

export const getAdmOperatorUpdateRolesUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/roles`;

export const admOperatorUpdateRoles = async (operatorId: string, data: AdmOperatorRoleUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorUpdateRolesResponse> => {
  return cpfOrvalRequest<admOperatorUpdateRolesResponse>(getAdmOperatorUpdateRolesUrl(operatorId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorUpdateRolesMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateRoles>>, TError, {operatorId: string; data: AdmOperatorRoleUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateRoles>>, TError, {operatorId: string; data: AdmOperatorRoleUpdateRequest}, TContext> => {
  const mutationKey = ['admOperatorUpdateRoles'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorUpdateRoles>>, {operatorId: string; data: AdmOperatorRoleUpdateRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorUpdateRoles(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorUpdateRolesMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorUpdateRoles>>>;
export type AdmOperatorUpdateRolesMutationBody = AdmOperatorRoleUpdateRequest;
export type AdmOperatorUpdateRolesMutationError = unknown;

export const useAdmOperatorUpdateRoles = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateRoles>>, TError, {operatorId: string; data: AdmOperatorRoleUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorUpdateRoles>>, TError, {operatorId: string; data: AdmOperatorRoleUpdateRequest}, TContext> => {
  return useMutation(getAdmOperatorUpdateRolesMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorUpdateRoles


// CPF PRE-RUNTIME FALLBACK START admOperatorUpdateStatus
export type admOperatorUpdateStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorUpdateStatusResponseSuccess = (admOperatorUpdateStatusResponse200) & {
  headers: Headers;
};

export type admOperatorUpdateStatusResponse = (admOperatorUpdateStatusResponseSuccess)

export const getAdmOperatorUpdateStatusUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/status`;

export const admOperatorUpdateStatus = async (operatorId: string, data: AdmOperatorStatusUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorUpdateStatusResponse> => {
  return cpfOrvalRequest<admOperatorUpdateStatusResponse>(getAdmOperatorUpdateStatusUrl(operatorId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorUpdateStatusMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateStatus>>, TError, {operatorId: string; data: AdmOperatorStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateStatus>>, TError, {operatorId: string; data: AdmOperatorStatusUpdateRequest}, TContext> => {
  const mutationKey = ['admOperatorUpdateStatus'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorUpdateStatus>>, {operatorId: string; data: AdmOperatorStatusUpdateRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorUpdateStatus(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorUpdateStatusMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorUpdateStatus>>>;
export type AdmOperatorUpdateStatusMutationBody = AdmOperatorStatusUpdateRequest;
export type AdmOperatorUpdateStatusMutationError = unknown;

export const useAdmOperatorUpdateStatus = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUpdateStatus>>, TError, {operatorId: string; data: AdmOperatorStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorUpdateStatus>>, TError, {operatorId: string; data: AdmOperatorStatusUpdateRequest}, TContext> => {
  return useMutation(getAdmOperatorUpdateStatusMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorUpdateStatus


// CPF PRE-RUNTIME FALLBACK START admOperatorUnlockOperator
export type admOperatorUnlockOperatorResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admOperatorUnlockOperatorResponseSuccess = (admOperatorUnlockOperatorResponse200) & {
  headers: Headers;
};

export type admOperatorUnlockOperatorResponse = (admOperatorUnlockOperatorResponseSuccess)

export const getAdmOperatorUnlockOperatorUrl = (operatorId: string) => `/adm/api/operators/${encodeURIComponent(String(operatorId))}/unlock`;

export const admOperatorUnlockOperator = async (operatorId: string, data: AdmSessionRevokeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admOperatorUnlockOperatorResponse> => {
  return cpfOrvalRequest<admOperatorUnlockOperatorResponse>(getAdmOperatorUnlockOperatorUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmOperatorUnlockOperatorMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUnlockOperator>>, TError, {operatorId: string; data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admOperatorUnlockOperator>>, TError, {operatorId: string; data: AdmSessionRevokeRequest}, TContext> => {
  const mutationKey = ['admOperatorUnlockOperator'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admOperatorUnlockOperator>>, {operatorId: string; data: AdmSessionRevokeRequest}> = (props) => {
    const { operatorId, data } = props;
    return admOperatorUnlockOperator(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmOperatorUnlockOperatorMutationResult = NonNullable<Awaited<ReturnType<typeof admOperatorUnlockOperator>>>;
export type AdmOperatorUnlockOperatorMutationBody = AdmSessionRevokeRequest;
export type AdmOperatorUnlockOperatorMutationError = unknown;

export const useAdmOperatorUnlockOperator = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admOperatorUnlockOperator>>, TError, {operatorId: string; data: AdmSessionRevokeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admOperatorUnlockOperator>>, TError, {operatorId: string; data: AdmSessionRevokeRequest}, TContext> => {
  return useMutation(getAdmOperatorUnlockOperatorMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admOperatorUnlockOperator


// CPF PRE-RUNTIME FALLBACK START admParameterReferenceSearch
export type admParameterReferenceSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admParameterReferenceSearchResponseSuccess = (admParameterReferenceSearchResponse200) & {
  headers: Headers;
};

export type admParameterReferenceSearchResponse = (admParameterReferenceSearchResponseSuccess)

export const getAdmParameterReferenceSearchUrl = () => `/adm/api/parameter-references`;

export const admParameterReferenceSearch = async (params: AdmParameterReferenceSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admParameterReferenceSearchResponse> => {
  return cpfOrvalRequest<admParameterReferenceSearchResponse>(getAdmParameterReferenceSearchUrl(), {
    ...options,
    method: 'GET',
    params: { referenceType: params.referenceType, parentType: params.parentType, parentId: params.parentId, q: params.q, offset: params.offset, limit: params.limit },
  });
};

export const getAdmParameterReferenceSearchQueryKey = (params: MaybeRefOrGetter<AdmParameterReferenceSearchParams>) => ["adm", "api", "parameter-references", toValue(params)] as const;

export const getAdmParameterReferenceSearchQueryOptions = <TData = Awaited<ReturnType<typeof admParameterReferenceSearch>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmParameterReferenceSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admParameterReferenceSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmParameterReferenceSearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admParameterReferenceSearch>>> = ({ signal }) => admParameterReferenceSearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admParameterReferenceSearch>>, TError, TData>;
};

export type AdmParameterReferenceSearchQueryResult = NonNullable<Awaited<ReturnType<typeof admParameterReferenceSearch>>>;
export type AdmParameterReferenceSearchQueryError = unknown;

export function useAdmParameterReferenceSearch<TData = Awaited<ReturnType<typeof admParameterReferenceSearch>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmParameterReferenceSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admParameterReferenceSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmParameterReferenceSearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admParameterReferenceSearch


// CPF PRE-RUNTIME FALLBACK START admPermissionFindApiPermissionMatrix
export type admPermissionFindApiPermissionMatrixResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindApiPermissionMatrixResponseSuccess = (admPermissionFindApiPermissionMatrixResponse200) & {
  headers: Headers;
};

export type admPermissionFindApiPermissionMatrixResponse = (admPermissionFindApiPermissionMatrixResponseSuccess)

export const getAdmPermissionFindApiPermissionMatrixUrl = () => `/adm/api/permissions/api-matrix`;

export const admPermissionFindApiPermissionMatrix = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindApiPermissionMatrixResponse> => {
  return cpfOrvalRequest<admPermissionFindApiPermissionMatrixResponse>(getAdmPermissionFindApiPermissionMatrixUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindApiPermissionMatrixQueryKey = () => ["adm", "api", "permissions", "api-matrix"] as const;

export const getAdmPermissionFindApiPermissionMatrixQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindApiPermissionMatrixQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>> = ({ signal }) => admPermissionFindApiPermissionMatrix({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>, TError, TData>;
};

export type AdmPermissionFindApiPermissionMatrixQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>>;
export type AdmPermissionFindApiPermissionMatrixQueryError = unknown;

export function useAdmPermissionFindApiPermissionMatrix<TData = Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissionMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindApiPermissionMatrixQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindApiPermissionMatrix


// CPF PRE-RUNTIME FALLBACK START admPermissionFindApiPermissions
export type admPermissionFindApiPermissionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindApiPermissionsResponseSuccess = (admPermissionFindApiPermissionsResponse200) & {
  headers: Headers;
};

export type admPermissionFindApiPermissionsResponse = (admPermissionFindApiPermissionsResponseSuccess)

export const getAdmPermissionFindApiPermissionsUrl = () => `/adm/api/permissions/api-permissions`;

export const admPermissionFindApiPermissions = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindApiPermissionsResponse> => {
  return cpfOrvalRequest<admPermissionFindApiPermissionsResponse>(getAdmPermissionFindApiPermissionsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindApiPermissionsQueryKey = () => ["adm", "api", "permissions", "api-permissions"] as const;

export const getAdmPermissionFindApiPermissionsQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindApiPermissions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindApiPermissionsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindApiPermissions>>> = ({ signal }) => admPermissionFindApiPermissions({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissions>>, TError, TData>;
};

export type AdmPermissionFindApiPermissionsQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindApiPermissions>>>;
export type AdmPermissionFindApiPermissionsQueryError = unknown;

export function useAdmPermissionFindApiPermissions<TData = Awaited<ReturnType<typeof admPermissionFindApiPermissions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindApiPermissionsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindApiPermissions


// CPF PRE-RUNTIME FALLBACK START admPermissionCreateApiPermission
export type admPermissionCreateApiPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionCreateApiPermissionResponseSuccess = (admPermissionCreateApiPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionCreateApiPermissionResponse = (admPermissionCreateApiPermissionResponseSuccess)

export const getAdmPermissionCreateApiPermissionUrl = () => `/adm/api/permissions/api-permissions`;

export const admPermissionCreateApiPermission = async (data: AdmApiPermissionSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionCreateApiPermissionResponse> => {
  return cpfOrvalRequest<admPermissionCreateApiPermissionResponse>(getAdmPermissionCreateApiPermissionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionCreateApiPermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>, TError, {data: AdmApiPermissionSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>, TError, {data: AdmApiPermissionSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionCreateApiPermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>, {data: AdmApiPermissionSaveRequest}> = (props) => {
    const { data } = props;
    return admPermissionCreateApiPermission(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionCreateApiPermissionMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>>;
export type AdmPermissionCreateApiPermissionMutationBody = AdmApiPermissionSaveRequest;
export type AdmPermissionCreateApiPermissionMutationError = unknown;

export const useAdmPermissionCreateApiPermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>, TError, {data: AdmApiPermissionSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionCreateApiPermission>>, TError, {data: AdmApiPermissionSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionCreateApiPermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionCreateApiPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionFindApiPermission
export type admPermissionFindApiPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindApiPermissionResponseSuccess = (admPermissionFindApiPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionFindApiPermissionResponse = (admPermissionFindApiPermissionResponseSuccess)

export const getAdmPermissionFindApiPermissionUrl = (apiPermissionId: string) => `/adm/api/permissions/api-permissions/${encodeURIComponent(String(apiPermissionId))}`;

export const admPermissionFindApiPermission = async (apiPermissionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindApiPermissionResponse> => {
  return cpfOrvalRequest<admPermissionFindApiPermissionResponse>(getAdmPermissionFindApiPermissionUrl(apiPermissionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindApiPermissionQueryKey = (apiPermissionId: MaybeRefOrGetter<string>) => ["adm", "api", "permissions", "api-permissions", apiPermissionId] as const;

export const getAdmPermissionFindApiPermissionQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindApiPermission>>, TError = unknown>(
  apiPermissionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermission>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindApiPermissionQueryKey(toValue(apiPermissionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindApiPermission>>> = ({ signal }) => admPermissionFindApiPermission(toValue(apiPermissionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(apiPermissionId) !== null && toValue(apiPermissionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermission>>, TError, TData>;
};

export type AdmPermissionFindApiPermissionQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindApiPermission>>>;
export type AdmPermissionFindApiPermissionQueryError = unknown;

export function useAdmPermissionFindApiPermission<TData = Awaited<ReturnType<typeof admPermissionFindApiPermission>>, TError = unknown>(
  apiPermissionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindApiPermission>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindApiPermissionQueryOptions(apiPermissionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindApiPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateApiPermission
export type admPermissionUpdateApiPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateApiPermissionResponseSuccess = (admPermissionUpdateApiPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateApiPermissionResponse = (admPermissionUpdateApiPermissionResponseSuccess)

export const getAdmPermissionUpdateApiPermissionUrl = (apiPermissionId: string) => `/adm/api/permissions/api-permissions/${encodeURIComponent(String(apiPermissionId))}`;

export const admPermissionUpdateApiPermission = async (apiPermissionId: string, data: AdmApiPermissionSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateApiPermissionResponse> => {
  return cpfOrvalRequest<admPermissionUpdateApiPermissionResponse>(getAdmPermissionUpdateApiPermissionUrl(apiPermissionId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateApiPermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>, TError, {apiPermissionId: string; data: AdmApiPermissionSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>, TError, {apiPermissionId: string; data: AdmApiPermissionSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateApiPermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>, {apiPermissionId: string; data: AdmApiPermissionSaveRequest}> = (props) => {
    const { apiPermissionId, data } = props;
    return admPermissionUpdateApiPermission(apiPermissionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateApiPermissionMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>>;
export type AdmPermissionUpdateApiPermissionMutationBody = AdmApiPermissionSaveRequest;
export type AdmPermissionUpdateApiPermissionMutationError = unknown;

export const useAdmPermissionUpdateApiPermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>, TError, {apiPermissionId: string; data: AdmApiPermissionSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateApiPermission>>, TError, {apiPermissionId: string; data: AdmApiPermissionSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateApiPermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateApiPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateApiPermissionStatus
export type admPermissionUpdateApiPermissionStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateApiPermissionStatusResponseSuccess = (admPermissionUpdateApiPermissionStatusResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateApiPermissionStatusResponse = (admPermissionUpdateApiPermissionStatusResponseSuccess)

export const getAdmPermissionUpdateApiPermissionStatusUrl = (apiPermissionId: string) => `/adm/api/permissions/api-permissions/${encodeURIComponent(String(apiPermissionId))}/status`;

export const admPermissionUpdateApiPermissionStatus = async (apiPermissionId: string, data: AdmStatusUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateApiPermissionStatusResponse> => {
  return cpfOrvalRequest<admPermissionUpdateApiPermissionStatusResponse>(getAdmPermissionUpdateApiPermissionStatusUrl(apiPermissionId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateApiPermissionStatusMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>, TError, {apiPermissionId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>, TError, {apiPermissionId: string; data: AdmStatusUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateApiPermissionStatus'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>, {apiPermissionId: string; data: AdmStatusUpdateRequest}> = (props) => {
    const { apiPermissionId, data } = props;
    return admPermissionUpdateApiPermissionStatus(apiPermissionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateApiPermissionStatusMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>>;
export type AdmPermissionUpdateApiPermissionStatusMutationBody = AdmStatusUpdateRequest;
export type AdmPermissionUpdateApiPermissionStatusMutationError = unknown;

export const useAdmPermissionUpdateApiPermissionStatus = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>, TError, {apiPermissionId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateApiPermissionStatus>>, TError, {apiPermissionId: string; data: AdmStatusUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateApiPermissionStatusMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateApiPermissionStatus


// CPF PRE-RUNTIME FALLBACK START admPermissionFindButtonMatrix
export type admPermissionFindButtonMatrixResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindButtonMatrixResponseSuccess = (admPermissionFindButtonMatrixResponse200) & {
  headers: Headers;
};

export type admPermissionFindButtonMatrixResponse = (admPermissionFindButtonMatrixResponseSuccess)

export const getAdmPermissionFindButtonMatrixUrl = () => `/adm/api/permissions/button-matrix`;

export const admPermissionFindButtonMatrix = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindButtonMatrixResponse> => {
  return cpfOrvalRequest<admPermissionFindButtonMatrixResponse>(getAdmPermissionFindButtonMatrixUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindButtonMatrixQueryKey = () => ["adm", "api", "permissions", "button-matrix"] as const;

export const getAdmPermissionFindButtonMatrixQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindButtonMatrixQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>> = ({ signal }) => admPermissionFindButtonMatrix({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>, TError, TData>;
};

export type AdmPermissionFindButtonMatrixQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>>;
export type AdmPermissionFindButtonMatrixQueryError = unknown;

export function useAdmPermissionFindButtonMatrix<TData = Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtonMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindButtonMatrixQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindButtonMatrix


// CPF PRE-RUNTIME FALLBACK START admPermissionFindButtons
export type admPermissionFindButtonsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindButtonsResponseSuccess = (admPermissionFindButtonsResponse200) & {
  headers: Headers;
};

export type admPermissionFindButtonsResponse = (admPermissionFindButtonsResponseSuccess)

export const getAdmPermissionFindButtonsUrl = () => `/adm/api/permissions/buttons`;

export const admPermissionFindButtons = async (params?: AdmPermissionFindButtonsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindButtonsResponse> => {
  return cpfOrvalRequest<admPermissionFindButtonsResponse>(getAdmPermissionFindButtonsUrl(), {
    ...options,
    method: 'GET',
    params: { menuId: params?.menuId },
  });
};

export const getAdmPermissionFindButtonsQueryKey = (params?: MaybeRefOrGetter<AdmPermissionFindButtonsParams>) => ["adm", "api", "permissions", "buttons", toValue(params)] as const;

export const getAdmPermissionFindButtonsQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindButtons>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmPermissionFindButtonsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtons>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindButtonsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindButtons>>> = ({ signal }) => admPermissionFindButtons(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtons>>, TError, TData>;
};

export type AdmPermissionFindButtonsQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindButtons>>>;
export type AdmPermissionFindButtonsQueryError = unknown;

export function useAdmPermissionFindButtons<TData = Awaited<ReturnType<typeof admPermissionFindButtons>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmPermissionFindButtonsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButtons>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindButtonsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindButtons


// CPF PRE-RUNTIME FALLBACK START admPermissionCreateButton
export type admPermissionCreateButtonResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionCreateButtonResponseSuccess = (admPermissionCreateButtonResponse200) & {
  headers: Headers;
};

export type admPermissionCreateButtonResponse = (admPermissionCreateButtonResponseSuccess)

export const getAdmPermissionCreateButtonUrl = () => `/adm/api/permissions/buttons`;

export const admPermissionCreateButton = async (data: AdmButtonSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionCreateButtonResponse> => {
  return cpfOrvalRequest<admPermissionCreateButtonResponse>(getAdmPermissionCreateButtonUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionCreateButtonMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateButton>>, TError, {data: AdmButtonSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateButton>>, TError, {data: AdmButtonSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionCreateButton'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionCreateButton>>, {data: AdmButtonSaveRequest}> = (props) => {
    const { data } = props;
    return admPermissionCreateButton(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionCreateButtonMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionCreateButton>>>;
export type AdmPermissionCreateButtonMutationBody = AdmButtonSaveRequest;
export type AdmPermissionCreateButtonMutationError = unknown;

export const useAdmPermissionCreateButton = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateButton>>, TError, {data: AdmButtonSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionCreateButton>>, TError, {data: AdmButtonSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionCreateButtonMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionCreateButton


// CPF PRE-RUNTIME FALLBACK START admPermissionFindButton
export type admPermissionFindButtonResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindButtonResponseSuccess = (admPermissionFindButtonResponse200) & {
  headers: Headers;
};

export type admPermissionFindButtonResponse = (admPermissionFindButtonResponseSuccess)

export const getAdmPermissionFindButtonUrl = (buttonId: string) => `/adm/api/permissions/buttons/${encodeURIComponent(String(buttonId))}`;

export const admPermissionFindButton = async (buttonId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindButtonResponse> => {
  return cpfOrvalRequest<admPermissionFindButtonResponse>(getAdmPermissionFindButtonUrl(buttonId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindButtonQueryKey = (buttonId: MaybeRefOrGetter<string>) => ["adm", "api", "permissions", "buttons", buttonId] as const;

export const getAdmPermissionFindButtonQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindButton>>, TError = unknown>(
  buttonId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButton>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindButtonQueryKey(toValue(buttonId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindButton>>> = ({ signal }) => admPermissionFindButton(toValue(buttonId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(buttonId) !== null && toValue(buttonId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButton>>, TError, TData>;
};

export type AdmPermissionFindButtonQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindButton>>>;
export type AdmPermissionFindButtonQueryError = unknown;

export function useAdmPermissionFindButton<TData = Awaited<ReturnType<typeof admPermissionFindButton>>, TError = unknown>(
  buttonId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindButton>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindButtonQueryOptions(buttonId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindButton


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateButton
export type admPermissionUpdateButtonResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateButtonResponseSuccess = (admPermissionUpdateButtonResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateButtonResponse = (admPermissionUpdateButtonResponseSuccess)

export const getAdmPermissionUpdateButtonUrl = (buttonId: string) => `/adm/api/permissions/buttons/${encodeURIComponent(String(buttonId))}`;

export const admPermissionUpdateButton = async (buttonId: string, data: AdmButtonSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateButtonResponse> => {
  return cpfOrvalRequest<admPermissionUpdateButtonResponse>(getAdmPermissionUpdateButtonUrl(buttonId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateButtonMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButton>>, TError, {buttonId: string; data: AdmButtonSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButton>>, TError, {buttonId: string; data: AdmButtonSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateButton'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateButton>>, {buttonId: string; data: AdmButtonSaveRequest}> = (props) => {
    const { buttonId, data } = props;
    return admPermissionUpdateButton(buttonId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateButtonMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateButton>>>;
export type AdmPermissionUpdateButtonMutationBody = AdmButtonSaveRequest;
export type AdmPermissionUpdateButtonMutationError = unknown;

export const useAdmPermissionUpdateButton = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButton>>, TError, {buttonId: string; data: AdmButtonSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateButton>>, TError, {buttonId: string; data: AdmButtonSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateButtonMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateButton


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateButtonStatus
export type admPermissionUpdateButtonStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateButtonStatusResponseSuccess = (admPermissionUpdateButtonStatusResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateButtonStatusResponse = (admPermissionUpdateButtonStatusResponseSuccess)

export const getAdmPermissionUpdateButtonStatusUrl = (buttonId: string) => `/adm/api/permissions/buttons/${encodeURIComponent(String(buttonId))}/status`;

export const admPermissionUpdateButtonStatus = async (buttonId: string, data: AdmStatusUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateButtonStatusResponse> => {
  return cpfOrvalRequest<admPermissionUpdateButtonStatusResponse>(getAdmPermissionUpdateButtonStatusUrl(buttonId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateButtonStatusMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>, TError, {buttonId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>, TError, {buttonId: string; data: AdmStatusUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateButtonStatus'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>, {buttonId: string; data: AdmStatusUpdateRequest}> = (props) => {
    const { buttonId, data } = props;
    return admPermissionUpdateButtonStatus(buttonId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateButtonStatusMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>>;
export type AdmPermissionUpdateButtonStatusMutationBody = AdmStatusUpdateRequest;
export type AdmPermissionUpdateButtonStatusMutationError = unknown;

export const useAdmPermissionUpdateButtonStatus = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>, TError, {buttonId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateButtonStatus>>, TError, {buttonId: string; data: AdmStatusUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateButtonStatusMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateButtonStatus


// CPF PRE-RUNTIME FALLBACK START admPermissionFindMenuMatrix
export type admPermissionFindMenuMatrixResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindMenuMatrixResponseSuccess = (admPermissionFindMenuMatrixResponse200) & {
  headers: Headers;
};

export type admPermissionFindMenuMatrixResponse = (admPermissionFindMenuMatrixResponseSuccess)

export const getAdmPermissionFindMenuMatrixUrl = () => `/adm/api/permissions/menu-matrix`;

export const admPermissionFindMenuMatrix = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindMenuMatrixResponse> => {
  return cpfOrvalRequest<admPermissionFindMenuMatrixResponse>(getAdmPermissionFindMenuMatrixUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindMenuMatrixQueryKey = () => ["adm", "api", "permissions", "menu-matrix"] as const;

export const getAdmPermissionFindMenuMatrixQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindMenuMatrixQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>> = ({ signal }) => admPermissionFindMenuMatrix({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>, TError, TData>;
};

export type AdmPermissionFindMenuMatrixQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>>;
export type AdmPermissionFindMenuMatrixQueryError = unknown;

export function useAdmPermissionFindMenuMatrix<TData = Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindMenuMatrix>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindMenuMatrixQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindMenuMatrix


// CPF PRE-RUNTIME FALLBACK START admPermissionFindManagedMenus
export type admPermissionFindManagedMenusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindManagedMenusResponseSuccess = (admPermissionFindManagedMenusResponse200) & {
  headers: Headers;
};

export type admPermissionFindManagedMenusResponse = (admPermissionFindManagedMenusResponseSuccess)

export const getAdmPermissionFindManagedMenusUrl = () => `/adm/api/permissions/menus`;

export const admPermissionFindManagedMenus = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindManagedMenusResponse> => {
  return cpfOrvalRequest<admPermissionFindManagedMenusResponse>(getAdmPermissionFindManagedMenusUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindManagedMenusQueryKey = () => ["adm", "api", "permissions", "menus"] as const;

export const getAdmPermissionFindManagedMenusQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindManagedMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindManagedMenusQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindManagedMenus>>> = ({ signal }) => admPermissionFindManagedMenus({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenus>>, TError, TData>;
};

export type AdmPermissionFindManagedMenusQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindManagedMenus>>>;
export type AdmPermissionFindManagedMenusQueryError = unknown;

export function useAdmPermissionFindManagedMenus<TData = Awaited<ReturnType<typeof admPermissionFindManagedMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindManagedMenusQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindManagedMenus


// CPF PRE-RUNTIME FALLBACK START admPermissionCreateMenu
export type admPermissionCreateMenuResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionCreateMenuResponseSuccess = (admPermissionCreateMenuResponse200) & {
  headers: Headers;
};

export type admPermissionCreateMenuResponse = (admPermissionCreateMenuResponseSuccess)

export const getAdmPermissionCreateMenuUrl = () => `/adm/api/permissions/menus`;

export const admPermissionCreateMenu = async (data: AdmMenuSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionCreateMenuResponse> => {
  return cpfOrvalRequest<admPermissionCreateMenuResponse>(getAdmPermissionCreateMenuUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionCreateMenuMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateMenu>>, TError, {data: AdmMenuSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateMenu>>, TError, {data: AdmMenuSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionCreateMenu'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionCreateMenu>>, {data: AdmMenuSaveRequest}> = (props) => {
    const { data } = props;
    return admPermissionCreateMenu(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionCreateMenuMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionCreateMenu>>>;
export type AdmPermissionCreateMenuMutationBody = AdmMenuSaveRequest;
export type AdmPermissionCreateMenuMutationError = unknown;

export const useAdmPermissionCreateMenu = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateMenu>>, TError, {data: AdmMenuSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionCreateMenu>>, TError, {data: AdmMenuSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionCreateMenuMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionCreateMenu


// CPF PRE-RUNTIME FALLBACK START admPermissionFindManagedMenu
export type admPermissionFindManagedMenuResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindManagedMenuResponseSuccess = (admPermissionFindManagedMenuResponse200) & {
  headers: Headers;
};

export type admPermissionFindManagedMenuResponse = (admPermissionFindManagedMenuResponseSuccess)

export const getAdmPermissionFindManagedMenuUrl = (menuId: string) => `/adm/api/permissions/menus/${encodeURIComponent(String(menuId))}`;

export const admPermissionFindManagedMenu = async (menuId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindManagedMenuResponse> => {
  return cpfOrvalRequest<admPermissionFindManagedMenuResponse>(getAdmPermissionFindManagedMenuUrl(menuId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindManagedMenuQueryKey = (menuId: MaybeRefOrGetter<string>) => ["adm", "api", "permissions", "menus", menuId] as const;

export const getAdmPermissionFindManagedMenuQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindManagedMenu>>, TError = unknown>(
  menuId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenu>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindManagedMenuQueryKey(toValue(menuId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindManagedMenu>>> = ({ signal }) => admPermissionFindManagedMenu(toValue(menuId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(menuId) !== null && toValue(menuId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenu>>, TError, TData>;
};

export type AdmPermissionFindManagedMenuQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindManagedMenu>>>;
export type AdmPermissionFindManagedMenuQueryError = unknown;

export function useAdmPermissionFindManagedMenu<TData = Awaited<ReturnType<typeof admPermissionFindManagedMenu>>, TError = unknown>(
  menuId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindManagedMenu>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindManagedMenuQueryOptions(menuId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindManagedMenu


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateMenu
export type admPermissionUpdateMenuResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateMenuResponseSuccess = (admPermissionUpdateMenuResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateMenuResponse = (admPermissionUpdateMenuResponseSuccess)

export const getAdmPermissionUpdateMenuUrl = (menuId: string) => `/adm/api/permissions/menus/${encodeURIComponent(String(menuId))}`;

export const admPermissionUpdateMenu = async (menuId: string, data: AdmMenuSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateMenuResponse> => {
  return cpfOrvalRequest<admPermissionUpdateMenuResponse>(getAdmPermissionUpdateMenuUrl(menuId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateMenuMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenu>>, TError, {menuId: string; data: AdmMenuSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenu>>, TError, {menuId: string; data: AdmMenuSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateMenu'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateMenu>>, {menuId: string; data: AdmMenuSaveRequest}> = (props) => {
    const { menuId, data } = props;
    return admPermissionUpdateMenu(menuId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateMenuMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateMenu>>>;
export type AdmPermissionUpdateMenuMutationBody = AdmMenuSaveRequest;
export type AdmPermissionUpdateMenuMutationError = unknown;

export const useAdmPermissionUpdateMenu = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenu>>, TError, {menuId: string; data: AdmMenuSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateMenu>>, TError, {menuId: string; data: AdmMenuSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateMenuMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateMenu


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateMenuStatus
export type admPermissionUpdateMenuStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateMenuStatusResponseSuccess = (admPermissionUpdateMenuStatusResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateMenuStatusResponse = (admPermissionUpdateMenuStatusResponseSuccess)

export const getAdmPermissionUpdateMenuStatusUrl = (menuId: string) => `/adm/api/permissions/menus/${encodeURIComponent(String(menuId))}/status`;

export const admPermissionUpdateMenuStatus = async (menuId: string, data: AdmStatusUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateMenuStatusResponse> => {
  return cpfOrvalRequest<admPermissionUpdateMenuStatusResponse>(getAdmPermissionUpdateMenuStatusUrl(menuId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateMenuStatusMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>, TError, {menuId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>, TError, {menuId: string; data: AdmStatusUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateMenuStatus'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>, {menuId: string; data: AdmStatusUpdateRequest}> = (props) => {
    const { menuId, data } = props;
    return admPermissionUpdateMenuStatus(menuId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateMenuStatusMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>>;
export type AdmPermissionUpdateMenuStatusMutationBody = AdmStatusUpdateRequest;
export type AdmPermissionUpdateMenuStatusMutationError = unknown;

export const useAdmPermissionUpdateMenuStatus = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>, TError, {menuId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateMenuStatus>>, TError, {menuId: string; data: AdmStatusUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateMenuStatusMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateMenuStatus


// CPF PRE-RUNTIME FALLBACK START admPermissionFindRoles
export type admPermissionFindRolesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindRolesResponseSuccess = (admPermissionFindRolesResponse200) & {
  headers: Headers;
};

export type admPermissionFindRolesResponse = (admPermissionFindRolesResponseSuccess)

export const getAdmPermissionFindRolesUrl = () => `/adm/api/permissions/roles`;

export const admPermissionFindRoles = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindRolesResponse> => {
  return cpfOrvalRequest<admPermissionFindRolesResponse>(getAdmPermissionFindRolesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindRolesQueryKey = () => ["adm", "api", "permissions", "roles"] as const;

export const getAdmPermissionFindRolesQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindRolesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindRoles>>> = ({ signal }) => admPermissionFindRoles({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRoles>>, TError, TData>;
};

export type AdmPermissionFindRolesQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindRoles>>>;
export type AdmPermissionFindRolesQueryError = unknown;

export function useAdmPermissionFindRoles<TData = Awaited<ReturnType<typeof admPermissionFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindRolesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindRoles


// CPF PRE-RUNTIME FALLBACK START admPermissionCreateRole
export type admPermissionCreateRoleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionCreateRoleResponseSuccess = (admPermissionCreateRoleResponse200) & {
  headers: Headers;
};

export type admPermissionCreateRoleResponse = (admPermissionCreateRoleResponseSuccess)

export const getAdmPermissionCreateRoleUrl = () => `/adm/api/permissions/roles`;

export const admPermissionCreateRole = async (data: AdmRoleSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionCreateRoleResponse> => {
  return cpfOrvalRequest<admPermissionCreateRoleResponse>(getAdmPermissionCreateRoleUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionCreateRoleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateRole>>, TError, {data: AdmRoleSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateRole>>, TError, {data: AdmRoleSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionCreateRole'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionCreateRole>>, {data: AdmRoleSaveRequest}> = (props) => {
    const { data } = props;
    return admPermissionCreateRole(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionCreateRoleMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionCreateRole>>>;
export type AdmPermissionCreateRoleMutationBody = AdmRoleSaveRequest;
export type AdmPermissionCreateRoleMutationError = unknown;

export const useAdmPermissionCreateRole = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionCreateRole>>, TError, {data: AdmRoleSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionCreateRole>>, TError, {data: AdmRoleSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionCreateRoleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionCreateRole


// CPF PRE-RUNTIME FALLBACK START admPermissionFindRole
export type admPermissionFindRoleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionFindRoleResponseSuccess = (admPermissionFindRoleResponse200) & {
  headers: Headers;
};

export type admPermissionFindRoleResponse = (admPermissionFindRoleResponseSuccess)

export const getAdmPermissionFindRoleUrl = (roleId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}`;

export const admPermissionFindRole = async (roleId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionFindRoleResponse> => {
  return cpfOrvalRequest<admPermissionFindRoleResponse>(getAdmPermissionFindRoleUrl(roleId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmPermissionFindRoleQueryKey = (roleId: MaybeRefOrGetter<string>) => ["adm", "api", "permissions", "roles", roleId] as const;

export const getAdmPermissionFindRoleQueryOptions = <TData = Awaited<ReturnType<typeof admPermissionFindRole>>, TError = unknown>(
  roleId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRole>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmPermissionFindRoleQueryKey(toValue(roleId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admPermissionFindRole>>> = ({ signal }) => admPermissionFindRole(toValue(roleId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(roleId) !== null && toValue(roleId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRole>>, TError, TData>;
};

export type AdmPermissionFindRoleQueryResult = NonNullable<Awaited<ReturnType<typeof admPermissionFindRole>>>;
export type AdmPermissionFindRoleQueryError = unknown;

export function useAdmPermissionFindRole<TData = Awaited<ReturnType<typeof admPermissionFindRole>>, TError = unknown>(
  roleId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admPermissionFindRole>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmPermissionFindRoleQueryOptions(roleId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admPermissionFindRole


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateRole
export type admPermissionUpdateRoleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateRoleResponseSuccess = (admPermissionUpdateRoleResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateRoleResponse = (admPermissionUpdateRoleResponseSuccess)

export const getAdmPermissionUpdateRoleUrl = (roleId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}`;

export const admPermissionUpdateRole = async (roleId: string, data: AdmRoleSaveRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateRoleResponse> => {
  return cpfOrvalRequest<admPermissionUpdateRoleResponse>(getAdmPermissionUpdateRoleUrl(roleId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateRoleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRole>>, TError, {roleId: string; data: AdmRoleSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRole>>, TError, {roleId: string; data: AdmRoleSaveRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateRole'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateRole>>, {roleId: string; data: AdmRoleSaveRequest}> = (props) => {
    const { roleId, data } = props;
    return admPermissionUpdateRole(roleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateRoleMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateRole>>>;
export type AdmPermissionUpdateRoleMutationBody = AdmRoleSaveRequest;
export type AdmPermissionUpdateRoleMutationError = unknown;

export const useAdmPermissionUpdateRole = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRole>>, TError, {roleId: string; data: AdmRoleSaveRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateRole>>, TError, {roleId: string; data: AdmRoleSaveRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateRoleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateRole


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateRoleApiPermission
export type admPermissionUpdateRoleApiPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateRoleApiPermissionResponseSuccess = (admPermissionUpdateRoleApiPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateRoleApiPermissionResponse = (admPermissionUpdateRoleApiPermissionResponseSuccess)

export const getAdmPermissionUpdateRoleApiPermissionUrl = (roleId: string, apiPermissionId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}/api-permissions/${encodeURIComponent(String(apiPermissionId))}`;

export const admPermissionUpdateRoleApiPermission = async (roleId: string, apiPermissionId: string, data: AdmApiPermissionRoleUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateRoleApiPermissionResponse> => {
  return cpfOrvalRequest<admPermissionUpdateRoleApiPermissionResponse>(getAdmPermissionUpdateRoleApiPermissionUrl(roleId, apiPermissionId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateRoleApiPermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>, TError, {roleId: string; apiPermissionId: string; data: AdmApiPermissionRoleUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>, TError, {roleId: string; apiPermissionId: string; data: AdmApiPermissionRoleUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateRoleApiPermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>, {roleId: string; apiPermissionId: string; data: AdmApiPermissionRoleUpdateRequest}> = (props) => {
    const { roleId, apiPermissionId, data } = props;
    return admPermissionUpdateRoleApiPermission(roleId, apiPermissionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateRoleApiPermissionMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>>;
export type AdmPermissionUpdateRoleApiPermissionMutationBody = AdmApiPermissionRoleUpdateRequest;
export type AdmPermissionUpdateRoleApiPermissionMutationError = unknown;

export const useAdmPermissionUpdateRoleApiPermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>, TError, {roleId: string; apiPermissionId: string; data: AdmApiPermissionRoleUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateRoleApiPermission>>, TError, {roleId: string; apiPermissionId: string; data: AdmApiPermissionRoleUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateRoleApiPermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateRoleApiPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateButtonPermission
export type admPermissionUpdateButtonPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateButtonPermissionResponseSuccess = (admPermissionUpdateButtonPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateButtonPermissionResponse = (admPermissionUpdateButtonPermissionResponseSuccess)

export const getAdmPermissionUpdateButtonPermissionUrl = (roleId: string, buttonId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}/buttons/${encodeURIComponent(String(buttonId))}`;

export const admPermissionUpdateButtonPermission = async (roleId: string, buttonId: string, data: AdmButtonPermissionUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateButtonPermissionResponse> => {
  return cpfOrvalRequest<admPermissionUpdateButtonPermissionResponse>(getAdmPermissionUpdateButtonPermissionUrl(roleId, buttonId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateButtonPermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>, TError, {roleId: string; buttonId: string; data: AdmButtonPermissionUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>, TError, {roleId: string; buttonId: string; data: AdmButtonPermissionUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateButtonPermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>, {roleId: string; buttonId: string; data: AdmButtonPermissionUpdateRequest}> = (props) => {
    const { roleId, buttonId, data } = props;
    return admPermissionUpdateButtonPermission(roleId, buttonId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateButtonPermissionMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>>;
export type AdmPermissionUpdateButtonPermissionMutationBody = AdmButtonPermissionUpdateRequest;
export type AdmPermissionUpdateButtonPermissionMutationError = unknown;

export const useAdmPermissionUpdateButtonPermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>, TError, {roleId: string; buttonId: string; data: AdmButtonPermissionUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateButtonPermission>>, TError, {roleId: string; buttonId: string; data: AdmButtonPermissionUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateButtonPermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateButtonPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateMenuPermission
export type admPermissionUpdateMenuPermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateMenuPermissionResponseSuccess = (admPermissionUpdateMenuPermissionResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateMenuPermissionResponse = (admPermissionUpdateMenuPermissionResponseSuccess)

export const getAdmPermissionUpdateMenuPermissionUrl = (roleId: string, menuId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}/menus/${encodeURIComponent(String(menuId))}`;

export const admPermissionUpdateMenuPermission = async (roleId: string, menuId: string, data: AdmMenuPermissionUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateMenuPermissionResponse> => {
  return cpfOrvalRequest<admPermissionUpdateMenuPermissionResponse>(getAdmPermissionUpdateMenuPermissionUrl(roleId, menuId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateMenuPermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>, TError, {roleId: string; menuId: string; data: AdmMenuPermissionUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>, TError, {roleId: string; menuId: string; data: AdmMenuPermissionUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateMenuPermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>, {roleId: string; menuId: string; data: AdmMenuPermissionUpdateRequest}> = (props) => {
    const { roleId, menuId, data } = props;
    return admPermissionUpdateMenuPermission(roleId, menuId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateMenuPermissionMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>>;
export type AdmPermissionUpdateMenuPermissionMutationBody = AdmMenuPermissionUpdateRequest;
export type AdmPermissionUpdateMenuPermissionMutationError = unknown;

export const useAdmPermissionUpdateMenuPermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>, TError, {roleId: string; menuId: string; data: AdmMenuPermissionUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateMenuPermission>>, TError, {roleId: string; menuId: string; data: AdmMenuPermissionUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateMenuPermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateMenuPermission


// CPF PRE-RUNTIME FALLBACK START admPermissionUpdateRoleStatus
export type admPermissionUpdateRoleStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admPermissionUpdateRoleStatusResponseSuccess = (admPermissionUpdateRoleStatusResponse200) & {
  headers: Headers;
};

export type admPermissionUpdateRoleStatusResponse = (admPermissionUpdateRoleStatusResponseSuccess)

export const getAdmPermissionUpdateRoleStatusUrl = (roleId: string) => `/adm/api/permissions/roles/${encodeURIComponent(String(roleId))}/status`;

export const admPermissionUpdateRoleStatus = async (roleId: string, data: AdmStatusUpdateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admPermissionUpdateRoleStatusResponse> => {
  return cpfOrvalRequest<admPermissionUpdateRoleStatusResponse>(getAdmPermissionUpdateRoleStatusUrl(roleId), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmPermissionUpdateRoleStatusMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>, TError, {roleId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>, TError, {roleId: string; data: AdmStatusUpdateRequest}, TContext> => {
  const mutationKey = ['admPermissionUpdateRoleStatus'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>, {roleId: string; data: AdmStatusUpdateRequest}> = (props) => {
    const { roleId, data } = props;
    return admPermissionUpdateRoleStatus(roleId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmPermissionUpdateRoleStatusMutationResult = NonNullable<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>>;
export type AdmPermissionUpdateRoleStatusMutationBody = AdmStatusUpdateRequest;
export type AdmPermissionUpdateRoleStatusMutationError = unknown;

export const useAdmPermissionUpdateRoleStatus = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>, TError, {roleId: string; data: AdmStatusUpdateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admPermissionUpdateRoleStatus>>, TError, {roleId: string; data: AdmStatusUpdateRequest}, TContext> => {
  return useMutation(getAdmPermissionUpdateRoleStatusMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admPermissionUpdateRoleStatus


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagSearch
export type admFeatureFlagSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagSearchResponseSuccess = (admFeatureFlagSearchResponse200) & {
  headers: Headers;
};

export type admFeatureFlagSearchResponse = (admFeatureFlagSearchResponseSuccess)

export const getAdmFeatureFlagSearchUrl = () => `/adm/api/platform/feature-flags`;

export const admFeatureFlagSearch = async (params?: AdmFeatureFlagSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagSearchResponse> => {
  return cpfOrvalRequest<admFeatureFlagSearchResponse>(getAdmFeatureFlagSearchUrl(), {
    ...options,
    method: 'GET',
    params: { query: params?.query, page: params?.page, size: params?.size },
  });
};

export const getAdmFeatureFlagSearchQueryKey = (params?: MaybeRefOrGetter<AdmFeatureFlagSearchParams>) => ["adm", "api", "platform", "feature-flags", toValue(params)] as const;

export const getAdmFeatureFlagSearchQueryOptions = <TData = Awaited<ReturnType<typeof admFeatureFlagSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmFeatureFlagSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFeatureFlagSearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFeatureFlagSearch>>> = ({ signal }) => admFeatureFlagSearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagSearch>>, TError, TData>;
};

export type AdmFeatureFlagSearchQueryResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagSearch>>>;
export type AdmFeatureFlagSearchQueryError = unknown;

export function useAdmFeatureFlagSearch<TData = Awaited<ReturnType<typeof admFeatureFlagSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmFeatureFlagSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFeatureFlagSearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagSearch


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagEvaluate
export type admFeatureFlagEvaluateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagEvaluateResponseSuccess = (admFeatureFlagEvaluateResponse200) & {
  headers: Headers;
};

export type admFeatureFlagEvaluateResponse = (admFeatureFlagEvaluateResponseSuccess)

export const getAdmFeatureFlagEvaluateUrl = () => `/adm/api/platform/feature-flags/evaluate`;

export const admFeatureFlagEvaluate = async (data: AdmFeatureFlagEvaluateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagEvaluateResponse> => {
  return cpfOrvalRequest<admFeatureFlagEvaluateResponse>(getAdmFeatureFlagEvaluateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFeatureFlagEvaluateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>, TError, {data: AdmFeatureFlagEvaluateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>, TError, {data: AdmFeatureFlagEvaluateRequest}, TContext> => {
  const mutationKey = ['admFeatureFlagEvaluate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>, {data: AdmFeatureFlagEvaluateRequest}> = (props) => {
    const { data } = props;
    return admFeatureFlagEvaluate(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFeatureFlagEvaluateMutationResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>>;
export type AdmFeatureFlagEvaluateMutationBody = AdmFeatureFlagEvaluateRequest;
export type AdmFeatureFlagEvaluateMutationError = unknown;

export const useAdmFeatureFlagEvaluate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>, TError, {data: AdmFeatureFlagEvaluateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFeatureFlagEvaluate>>, TError, {data: AdmFeatureFlagEvaluateRequest}, TContext> => {
  return useMutation(getAdmFeatureFlagEvaluateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagEvaluate


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagRequestOverride
export type admFeatureFlagRequestOverrideResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagRequestOverrideResponseSuccess = (admFeatureFlagRequestOverrideResponse200) & {
  headers: Headers;
};

export type admFeatureFlagRequestOverrideResponse = (admFeatureFlagRequestOverrideResponseSuccess)

export const getAdmFeatureFlagRequestOverrideUrl = () => `/adm/api/platform/feature-flags/override-requests`;

export const admFeatureFlagRequestOverride = async (data: AdmFeatureFlagOverrideRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagRequestOverrideResponse> => {
  return cpfOrvalRequest<admFeatureFlagRequestOverrideResponse>(getAdmFeatureFlagRequestOverrideUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmFeatureFlagRequestOverrideMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>, TError, {data: AdmFeatureFlagOverrideRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>, TError, {data: AdmFeatureFlagOverrideRequest}, TContext> => {
  const mutationKey = ['admFeatureFlagRequestOverride'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>, {data: AdmFeatureFlagOverrideRequest}> = (props) => {
    const { data } = props;
    return admFeatureFlagRequestOverride(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFeatureFlagRequestOverrideMutationResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>>;
export type AdmFeatureFlagRequestOverrideMutationBody = AdmFeatureFlagOverrideRequest;
export type AdmFeatureFlagRequestOverrideMutationError = unknown;

export const useAdmFeatureFlagRequestOverride = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>, TError, {data: AdmFeatureFlagOverrideRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFeatureFlagRequestOverride>>, TError, {data: AdmFeatureFlagOverrideRequest}, TContext> => {
  return useMutation(getAdmFeatureFlagRequestOverrideMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagRequestOverride


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagApproveOverride
export type admFeatureFlagApproveOverrideResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagApproveOverrideResponseSuccess = (admFeatureFlagApproveOverrideResponse200) & {
  headers: Headers;
};

export type admFeatureFlagApproveOverrideResponse = (admFeatureFlagApproveOverrideResponseSuccess)

export const getAdmFeatureFlagApproveOverrideUrl = (requestId: string) => `/adm/api/platform/feature-flags/override-requests/${encodeURIComponent(String(requestId))}/approve`;

export const admFeatureFlagApproveOverride = async (requestId: string, data: AdmFeatureFlagDecisionRequest, params: AdmFeatureFlagApproveOverrideParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagApproveOverrideResponse> => {
  return cpfOrvalRequest<admFeatureFlagApproveOverrideResponse>(getAdmFeatureFlagApproveOverrideUrl(requestId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmFeatureFlagApproveOverrideMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagApproveOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagApproveOverrideParams}, TContext> => {
  const mutationKey = ['admFeatureFlagApproveOverride'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagApproveOverrideParams}> = (props) => {
    const { requestId, data, params } = props;
    return admFeatureFlagApproveOverride(requestId, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFeatureFlagApproveOverrideMutationResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>>;
export type AdmFeatureFlagApproveOverrideMutationBody = AdmFeatureFlagDecisionRequest;
export type AdmFeatureFlagApproveOverrideMutationError = unknown;

export const useAdmFeatureFlagApproveOverride = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagApproveOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFeatureFlagApproveOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagApproveOverrideParams}, TContext> => {
  return useMutation(getAdmFeatureFlagApproveOverrideMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagApproveOverride


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagRevokeOverride
export type admFeatureFlagRevokeOverrideResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagRevokeOverrideResponseSuccess = (admFeatureFlagRevokeOverrideResponse200) & {
  headers: Headers;
};

export type admFeatureFlagRevokeOverrideResponse = (admFeatureFlagRevokeOverrideResponseSuccess)

export const getAdmFeatureFlagRevokeOverrideUrl = (requestId: string) => `/adm/api/platform/feature-flags/override-requests/${encodeURIComponent(String(requestId))}/revoke`;

export const admFeatureFlagRevokeOverride = async (requestId: string, data: AdmFeatureFlagDecisionRequest, params: AdmFeatureFlagRevokeOverrideParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagRevokeOverrideResponse> => {
  return cpfOrvalRequest<admFeatureFlagRevokeOverrideResponse>(getAdmFeatureFlagRevokeOverrideUrl(requestId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmFeatureFlagRevokeOverrideMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagRevokeOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagRevokeOverrideParams}, TContext> => {
  const mutationKey = ['admFeatureFlagRevokeOverride'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagRevokeOverrideParams}> = (props) => {
    const { requestId, data, params } = props;
    return admFeatureFlagRevokeOverride(requestId, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFeatureFlagRevokeOverrideMutationResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>>;
export type AdmFeatureFlagRevokeOverrideMutationBody = AdmFeatureFlagDecisionRequest;
export type AdmFeatureFlagRevokeOverrideMutationError = unknown;

export const useAdmFeatureFlagRevokeOverride = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagRevokeOverrideParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFeatureFlagRevokeOverride>>, TError, {requestId: string; data: AdmFeatureFlagDecisionRequest; params: AdmFeatureFlagRevokeOverrideParams}, TContext> => {
  return useMutation(getAdmFeatureFlagRevokeOverrideMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagRevokeOverride


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagFind
export type admFeatureFlagFindResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagFindResponseSuccess = (admFeatureFlagFindResponse200) & {
  headers: Headers;
};

export type admFeatureFlagFindResponse = (admFeatureFlagFindResponseSuccess)

export const getAdmFeatureFlagFindUrl = (flagKey: string) => `/adm/api/platform/feature-flags/${encodeURIComponent(String(flagKey))}`;

export const admFeatureFlagFind = async (flagKey: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagFindResponse> => {
  return cpfOrvalRequest<admFeatureFlagFindResponse>(getAdmFeatureFlagFindUrl(flagKey), {
    ...options,
    method: 'GET',

  });
};

export const getAdmFeatureFlagFindQueryKey = (flagKey: MaybeRefOrGetter<string>) => ["adm", "api", "platform", "feature-flags", flagKey] as const;

export const getAdmFeatureFlagFindQueryOptions = <TData = Awaited<ReturnType<typeof admFeatureFlagFind>>, TError = unknown>(
  flagKey: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmFeatureFlagFindQueryKey(toValue(flagKey));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admFeatureFlagFind>>> = ({ signal }) => admFeatureFlagFind(toValue(flagKey), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(flagKey) !== null && toValue(flagKey) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagFind>>, TError, TData>;
};

export type AdmFeatureFlagFindQueryResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagFind>>>;
export type AdmFeatureFlagFindQueryError = unknown;

export function useAdmFeatureFlagFind<TData = Awaited<ReturnType<typeof admFeatureFlagFind>>, TError = unknown>(
  flagKey: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admFeatureFlagFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmFeatureFlagFindQueryOptions(flagKey, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagFind


// CPF PRE-RUNTIME FALLBACK START admFeatureFlagSetKillSwitch
export type admFeatureFlagSetKillSwitchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admFeatureFlagSetKillSwitchResponseSuccess = (admFeatureFlagSetKillSwitchResponse200) & {
  headers: Headers;
};

export type admFeatureFlagSetKillSwitchResponse = (admFeatureFlagSetKillSwitchResponseSuccess)

export const getAdmFeatureFlagSetKillSwitchUrl = (flagKey: string) => `/adm/api/platform/feature-flags/${encodeURIComponent(String(flagKey))}/kill-switch`;

export const admFeatureFlagSetKillSwitch = async (flagKey: string, data: AdmFeatureFlagKillSwitchRequest, params: AdmFeatureFlagSetKillSwitchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admFeatureFlagSetKillSwitchResponse> => {
  return cpfOrvalRequest<admFeatureFlagSetKillSwitchResponse>(getAdmFeatureFlagSetKillSwitchUrl(flagKey), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmFeatureFlagSetKillSwitchMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>, TError, {flagKey: string; data: AdmFeatureFlagKillSwitchRequest; params: AdmFeatureFlagSetKillSwitchParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>, TError, {flagKey: string; data: AdmFeatureFlagKillSwitchRequest; params: AdmFeatureFlagSetKillSwitchParams}, TContext> => {
  const mutationKey = ['admFeatureFlagSetKillSwitch'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>, {flagKey: string; data: AdmFeatureFlagKillSwitchRequest; params: AdmFeatureFlagSetKillSwitchParams}> = (props) => {
    const { flagKey, data, params } = props;
    return admFeatureFlagSetKillSwitch(flagKey, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmFeatureFlagSetKillSwitchMutationResult = NonNullable<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>>;
export type AdmFeatureFlagSetKillSwitchMutationBody = AdmFeatureFlagKillSwitchRequest;
export type AdmFeatureFlagSetKillSwitchMutationError = unknown;

export const useAdmFeatureFlagSetKillSwitch = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>, TError, {flagKey: string; data: AdmFeatureFlagKillSwitchRequest; params: AdmFeatureFlagSetKillSwitchParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admFeatureFlagSetKillSwitch>>, TError, {flagKey: string; data: AdmFeatureFlagKillSwitchRequest; params: AdmFeatureFlagSetKillSwitchParams}, TContext> => {
  return useMutation(getAdmFeatureFlagSetKillSwitchMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admFeatureFlagSetKillSwitch


// CPF PRE-RUNTIME FALLBACK START admResiliencePolicySearch
export type admResiliencePolicySearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResiliencePolicySearchResponseSuccess = (admResiliencePolicySearchResponse200) & {
  headers: Headers;
};

export type admResiliencePolicySearchResponse = (admResiliencePolicySearchResponseSuccess)

export const getAdmResiliencePolicySearchUrl = () => `/adm/api/platform/resilience-policies`;

export const admResiliencePolicySearch = async (params?: AdmResiliencePolicySearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResiliencePolicySearchResponse> => {
  return cpfOrvalRequest<admResiliencePolicySearchResponse>(getAdmResiliencePolicySearchUrl(), {
    ...options,
    method: 'GET',
    params: { query: params?.query, page: params?.page, size: params?.size },
  });
};

export const getAdmResiliencePolicySearchQueryKey = (params?: MaybeRefOrGetter<AdmResiliencePolicySearchParams>) => ["adm", "api", "platform", "resilience-policies", toValue(params)] as const;

export const getAdmResiliencePolicySearchQueryOptions = <TData = Awaited<ReturnType<typeof admResiliencePolicySearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmResiliencePolicySearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicySearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmResiliencePolicySearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admResiliencePolicySearch>>> = ({ signal }) => admResiliencePolicySearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicySearch>>, TError, TData>;
};

export type AdmResiliencePolicySearchQueryResult = NonNullable<Awaited<ReturnType<typeof admResiliencePolicySearch>>>;
export type AdmResiliencePolicySearchQueryError = unknown;

export function useAdmResiliencePolicySearch<TData = Awaited<ReturnType<typeof admResiliencePolicySearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmResiliencePolicySearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicySearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmResiliencePolicySearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admResiliencePolicySearch


// CPF PRE-RUNTIME FALLBACK START admResiliencePolicyRequest
export type admResiliencePolicyRequestResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResiliencePolicyRequestResponseSuccess = (admResiliencePolicyRequestResponse200) & {
  headers: Headers;
};

export type admResiliencePolicyRequestResponse = (admResiliencePolicyRequestResponseSuccess)

export const getAdmResiliencePolicyRequestUrl = () => `/adm/api/platform/resilience-policies/requests`;

export const admResiliencePolicyRequest = async (data: AdmResiliencePolicyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admResiliencePolicyRequestResponse> => {
  return cpfOrvalRequest<admResiliencePolicyRequestResponse>(getAdmResiliencePolicyRequestUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmResiliencePolicyRequestMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyRequest>>, TError, {data: AdmResiliencePolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyRequest>>, TError, {data: AdmResiliencePolicyRequest}, TContext> => {
  const mutationKey = ['admResiliencePolicyRequest'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResiliencePolicyRequest>>, {data: AdmResiliencePolicyRequest}> = (props) => {
    const { data } = props;
    return admResiliencePolicyRequest(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResiliencePolicyRequestMutationResult = NonNullable<Awaited<ReturnType<typeof admResiliencePolicyRequest>>>;
export type AdmResiliencePolicyRequestMutationBody = AdmResiliencePolicyRequest;
export type AdmResiliencePolicyRequestMutationError = unknown;

export const useAdmResiliencePolicyRequest = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyRequest>>, TError, {data: AdmResiliencePolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResiliencePolicyRequest>>, TError, {data: AdmResiliencePolicyRequest}, TContext> => {
  return useMutation(getAdmResiliencePolicyRequestMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResiliencePolicyRequest


// CPF PRE-RUNTIME FALLBACK START admResiliencePolicyApprove
export type admResiliencePolicyApproveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResiliencePolicyApproveResponseSuccess = (admResiliencePolicyApproveResponse200) & {
  headers: Headers;
};

export type admResiliencePolicyApproveResponse = (admResiliencePolicyApproveResponseSuccess)

export const getAdmResiliencePolicyApproveUrl = (requestId: string) => `/adm/api/platform/resilience-policies/requests/${encodeURIComponent(String(requestId))}/approve`;

export const admResiliencePolicyApprove = async (requestId: string, data: AdmResilienceDecisionRequest, params: AdmResiliencePolicyApproveParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResiliencePolicyApproveResponse> => {
  return cpfOrvalRequest<admResiliencePolicyApproveResponse>(getAdmResiliencePolicyApproveUrl(requestId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmResiliencePolicyApproveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyApprove>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyApproveParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyApprove>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyApproveParams}, TContext> => {
  const mutationKey = ['admResiliencePolicyApprove'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResiliencePolicyApprove>>, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyApproveParams}> = (props) => {
    const { requestId, data, params } = props;
    return admResiliencePolicyApprove(requestId, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResiliencePolicyApproveMutationResult = NonNullable<Awaited<ReturnType<typeof admResiliencePolicyApprove>>>;
export type AdmResiliencePolicyApproveMutationBody = AdmResilienceDecisionRequest;
export type AdmResiliencePolicyApproveMutationError = unknown;

export const useAdmResiliencePolicyApprove = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyApprove>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyApproveParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResiliencePolicyApprove>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyApproveParams}, TContext> => {
  return useMutation(getAdmResiliencePolicyApproveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResiliencePolicyApprove


// CPF PRE-RUNTIME FALLBACK START admResiliencePolicyReject
export type admResiliencePolicyRejectResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResiliencePolicyRejectResponseSuccess = (admResiliencePolicyRejectResponse200) & {
  headers: Headers;
};

export type admResiliencePolicyRejectResponse = (admResiliencePolicyRejectResponseSuccess)

export const getAdmResiliencePolicyRejectUrl = (requestId: string) => `/adm/api/platform/resilience-policies/requests/${encodeURIComponent(String(requestId))}/reject`;

export const admResiliencePolicyReject = async (requestId: string, data: AdmResilienceDecisionRequest, params: AdmResiliencePolicyRejectParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResiliencePolicyRejectResponse> => {
  return cpfOrvalRequest<admResiliencePolicyRejectResponse>(getAdmResiliencePolicyRejectUrl(requestId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Risk-Confirmed": params["X-CPF-Risk-Confirmed"], ...options?.headers },
    data,
  });
};

export const getAdmResiliencePolicyRejectMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyReject>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyRejectParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyReject>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyRejectParams}, TContext> => {
  const mutationKey = ['admResiliencePolicyReject'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResiliencePolicyReject>>, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyRejectParams}> = (props) => {
    const { requestId, data, params } = props;
    return admResiliencePolicyReject(requestId, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResiliencePolicyRejectMutationResult = NonNullable<Awaited<ReturnType<typeof admResiliencePolicyReject>>>;
export type AdmResiliencePolicyRejectMutationBody = AdmResilienceDecisionRequest;
export type AdmResiliencePolicyRejectMutationError = unknown;

export const useAdmResiliencePolicyReject = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResiliencePolicyReject>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyRejectParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResiliencePolicyReject>>, TError, {requestId: string; data: AdmResilienceDecisionRequest; params: AdmResiliencePolicyRejectParams}, TContext> => {
  return useMutation(getAdmResiliencePolicyRejectMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResiliencePolicyReject


// CPF PRE-RUNTIME FALLBACK START admResiliencePolicyFind
export type admResiliencePolicyFindResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResiliencePolicyFindResponseSuccess = (admResiliencePolicyFindResponse200) & {
  headers: Headers;
};

export type admResiliencePolicyFindResponse = (admResiliencePolicyFindResponseSuccess)

export const getAdmResiliencePolicyFindUrl = (operationId: string) => `/adm/api/platform/resilience-policies/${encodeURIComponent(String(operationId))}`;

export const admResiliencePolicyFind = async (operationId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admResiliencePolicyFindResponse> => {
  return cpfOrvalRequest<admResiliencePolicyFindResponse>(getAdmResiliencePolicyFindUrl(operationId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmResiliencePolicyFindQueryKey = (operationId: MaybeRefOrGetter<string>) => ["adm", "api", "platform", "resilience-policies", operationId] as const;

export const getAdmResiliencePolicyFindQueryOptions = <TData = Awaited<ReturnType<typeof admResiliencePolicyFind>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicyFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmResiliencePolicyFindQueryKey(toValue(operationId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admResiliencePolicyFind>>> = ({ signal }) => admResiliencePolicyFind(toValue(operationId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(operationId) !== null && toValue(operationId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicyFind>>, TError, TData>;
};

export type AdmResiliencePolicyFindQueryResult = NonNullable<Awaited<ReturnType<typeof admResiliencePolicyFind>>>;
export type AdmResiliencePolicyFindQueryError = unknown;

export function useAdmResiliencePolicyFind<TData = Awaited<ReturnType<typeof admResiliencePolicyFind>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResiliencePolicyFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmResiliencePolicyFindQueryOptions(operationId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admResiliencePolicyFind


// CPF PRE-RUNTIME FALLBACK START findAdmBatchJobInstanceLogs
export type findAdmBatchJobInstanceLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmBatchJobInstanceLogsResponseSuccess = (findAdmBatchJobInstanceLogsResponse200) & {
  headers: Headers;
};

export type findAdmBatchJobInstanceLogsResponse = (findAdmBatchJobInstanceLogsResponseSuccess)

export const getFindAdmBatchJobInstanceLogsUrl = () => `/adm/api/reliability/batch-job-logs`;

export const findAdmBatchJobInstanceLogs = async (params?: FindAdmBatchJobInstanceLogsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmBatchJobInstanceLogsResponse> => {
  return cpfOrvalRequest<findAdmBatchJobInstanceLogsResponse>(getFindAdmBatchJobInstanceLogsUrl(), {
    ...options,
    method: 'GET',
    params: { businessDate: params?.businessDate, jobName: params?.jobName, jobInstanceId: params?.jobInstanceId, serverInstanceId: params?.serverInstanceId, limit: params?.limit },
  });
};

export const getFindAdmBatchJobInstanceLogsQueryKey = (params?: MaybeRefOrGetter<FindAdmBatchJobInstanceLogsParams>) => ["adm", "api", "reliability", "batch-job-logs", toValue(params)] as const;

export const getFindAdmBatchJobInstanceLogsQueryOptions = <TData = Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBatchJobInstanceLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmBatchJobInstanceLogsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>> = ({ signal }) => findAdmBatchJobInstanceLogs(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>, TError, TData>;
};

export type FindAdmBatchJobInstanceLogsQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>>;
export type FindAdmBatchJobInstanceLogsQueryError = unknown;

export function useFindAdmBatchJobInstanceLogs<TData = Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBatchJobInstanceLogsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBatchJobInstanceLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmBatchJobInstanceLogsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmBatchJobInstanceLogs


// CPF PRE-RUNTIME FALLBACK START getAdmBatchJobInstanceLog
export type getAdmBatchJobInstanceLogResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type getAdmBatchJobInstanceLogResponseSuccess = (getAdmBatchJobInstanceLogResponse200) & {
  headers: Headers;
};

export type getAdmBatchJobInstanceLogResponse = (getAdmBatchJobInstanceLogResponseSuccess)

export const getGetAdmBatchJobInstanceLogUrl = (businessDate: string, jobName: string, jobInstanceId: number) => `/adm/api/reliability/batch-job-logs/${encodeURIComponent(String(businessDate))}/${encodeURIComponent(String(jobName))}/${encodeURIComponent(String(jobInstanceId))}`;

export const getAdmBatchJobInstanceLog = async (businessDate: string, jobName: string, jobInstanceId: number, params: GetAdmBatchJobInstanceLogParams, options?: CpfOrvalGeneratedRequestOptions): Promise<getAdmBatchJobInstanceLogResponse> => {
  return cpfOrvalRequest<getAdmBatchJobInstanceLogResponse>(getGetAdmBatchJobInstanceLogUrl(businessDate, jobName, jobInstanceId), {
    ...options,
    method: 'GET',
    params: { serverInstanceId: params.serverInstanceId, maxRecords: params.maxRecords },
  });
};

export const getGetAdmBatchJobInstanceLogQueryKey = (businessDate: MaybeRefOrGetter<string>, jobName: MaybeRefOrGetter<string>, jobInstanceId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<GetAdmBatchJobInstanceLogParams>) => ["adm", "api", "reliability", "batch-job-logs", businessDate, jobName, jobInstanceId, toValue(params)] as const;

export const getGetAdmBatchJobInstanceLogQueryOptions = <TData = Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>, TError = unknown>(
  businessDate: MaybeRefOrGetter<string>, jobName: MaybeRefOrGetter<string>, jobInstanceId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<GetAdmBatchJobInstanceLogParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getGetAdmBatchJobInstanceLogQueryKey(toValue(businessDate), toValue(jobName), toValue(jobInstanceId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>> = ({ signal }) => getAdmBatchJobInstanceLog(toValue(businessDate), toValue(jobName), toValue(jobInstanceId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(businessDate) !== null && toValue(businessDate) !== undefined && toValue(jobName) !== null && toValue(jobName) !== undefined && toValue(jobInstanceId) !== null && toValue(jobInstanceId) !== undefined && toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>, TError, TData>;
};

export type GetAdmBatchJobInstanceLogQueryResult = NonNullable<Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>>;
export type GetAdmBatchJobInstanceLogQueryError = unknown;

export function useGetAdmBatchJobInstanceLog<TData = Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>, TError = unknown>(
  businessDate: MaybeRefOrGetter<string>, jobName: MaybeRefOrGetter<string>, jobInstanceId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<GetAdmBatchJobInstanceLogParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmBatchJobInstanceLog>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAdmBatchJobInstanceLogQueryOptions(businessDate, jobName, jobInstanceId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END getAdmBatchJobInstanceLog


// CPF PRE-RUNTIME FALLBACK START findAdmBrokerDlq
export type findAdmBrokerDlqResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmBrokerDlqResponseSuccess = (findAdmBrokerDlqResponse200) & {
  headers: Headers;
};

export type findAdmBrokerDlqResponse = (findAdmBrokerDlqResponseSuccess)

export const getFindAdmBrokerDlqUrl = () => `/adm/api/reliability/broker/dlq`;

export const findAdmBrokerDlq = async (params?: FindAdmBrokerDlqParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmBrokerDlqResponse> => {
  return cpfOrvalRequest<findAdmBrokerDlqResponse>(getFindAdmBrokerDlqUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, transactionId: params?.transactionId, topic: params?.topic, limit: params?.limit },
  });
};

export const getFindAdmBrokerDlqQueryKey = (params?: MaybeRefOrGetter<FindAdmBrokerDlqParams>) => ["adm", "api", "reliability", "broker", "dlq", toValue(params)] as const;

export const getFindAdmBrokerDlqQueryOptions = <TData = Awaited<ReturnType<typeof findAdmBrokerDlq>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerDlqParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerDlq>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmBrokerDlqQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmBrokerDlq>>> = ({ signal }) => findAdmBrokerDlq(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerDlq>>, TError, TData>;
};

export type FindAdmBrokerDlqQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmBrokerDlq>>>;
export type FindAdmBrokerDlqQueryError = unknown;

export function useFindAdmBrokerDlq<TData = Awaited<ReturnType<typeof findAdmBrokerDlq>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerDlqParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerDlq>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmBrokerDlqQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmBrokerDlq


// CPF PRE-RUNTIME FALLBACK START requestAdmBrokerDlqReplay
export type requestAdmBrokerDlqReplayResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type requestAdmBrokerDlqReplayResponseSuccess = (requestAdmBrokerDlqReplayResponse200) & {
  headers: Headers;
};

export type requestAdmBrokerDlqReplayResponse = (requestAdmBrokerDlqReplayResponseSuccess)

export const getRequestAdmBrokerDlqReplayUrl = (messageId: string) => `/adm/api/reliability/broker/dlq/${encodeURIComponent(String(messageId))}/replay`;

export const requestAdmBrokerDlqReplay = async (messageId: string, data: AdmReliabilityActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<requestAdmBrokerDlqReplayResponse> => {
  return cpfOrvalRequest<requestAdmBrokerDlqReplayResponse>(getRequestAdmBrokerDlqReplayUrl(messageId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getRequestAdmBrokerDlqReplayMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError, {messageId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError, {messageId: string; data: AdmReliabilityActionRequest}, TContext> => {
  const mutationKey = ['requestAdmBrokerDlqReplay'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, {messageId: string; data: AdmReliabilityActionRequest}> = (props) => {
    const { messageId, data } = props;
    return requestAdmBrokerDlqReplay(messageId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type RequestAdmBrokerDlqReplayMutationResult = NonNullable<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>>;
export type RequestAdmBrokerDlqReplayMutationBody = AdmReliabilityActionRequest;
export type RequestAdmBrokerDlqReplayMutationError = unknown;

export const useRequestAdmBrokerDlqReplay = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError, {messageId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof requestAdmBrokerDlqReplay>>, TError, {messageId: string; data: AdmReliabilityActionRequest}, TContext> => {
  return useMutation(getRequestAdmBrokerDlqReplayMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END requestAdmBrokerDlqReplay


// CPF PRE-RUNTIME FALLBACK START findAdmBrokerInbox
export type findAdmBrokerInboxResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmBrokerInboxResponseSuccess = (findAdmBrokerInboxResponse200) & {
  headers: Headers;
};

export type findAdmBrokerInboxResponse = (findAdmBrokerInboxResponseSuccess)

export const getFindAdmBrokerInboxUrl = () => `/adm/api/reliability/broker/inbox`;

export const findAdmBrokerInbox = async (params?: FindAdmBrokerInboxParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmBrokerInboxResponse> => {
  return cpfOrvalRequest<findAdmBrokerInboxResponse>(getFindAdmBrokerInboxUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, key: params?.key, limit: params?.limit },
  });
};

export const getFindAdmBrokerInboxQueryKey = (params?: MaybeRefOrGetter<FindAdmBrokerInboxParams>) => ["adm", "api", "reliability", "broker", "inbox", toValue(params)] as const;

export const getFindAdmBrokerInboxQueryOptions = <TData = Awaited<ReturnType<typeof findAdmBrokerInbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerInboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerInbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmBrokerInboxQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmBrokerInbox>>> = ({ signal }) => findAdmBrokerInbox(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerInbox>>, TError, TData>;
};

export type FindAdmBrokerInboxQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmBrokerInbox>>>;
export type FindAdmBrokerInboxQueryError = unknown;

export function useFindAdmBrokerInbox<TData = Awaited<ReturnType<typeof findAdmBrokerInbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerInboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerInbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmBrokerInboxQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmBrokerInbox


// CPF PRE-RUNTIME FALLBACK START findAdmBrokerOutbox
export type findAdmBrokerOutboxResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmBrokerOutboxResponseSuccess = (findAdmBrokerOutboxResponse200) & {
  headers: Headers;
};

export type findAdmBrokerOutboxResponse = (findAdmBrokerOutboxResponseSuccess)

export const getFindAdmBrokerOutboxUrl = () => `/adm/api/reliability/broker/outbox`;

export const findAdmBrokerOutbox = async (params?: FindAdmBrokerOutboxParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmBrokerOutboxResponse> => {
  return cpfOrvalRequest<findAdmBrokerOutboxResponse>(getFindAdmBrokerOutboxUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, transactionId: params?.transactionId, topic: params?.topic, limit: params?.limit },
  });
};

export const getFindAdmBrokerOutboxQueryKey = (params?: MaybeRefOrGetter<FindAdmBrokerOutboxParams>) => ["adm", "api", "reliability", "broker", "outbox", toValue(params)] as const;

export const getFindAdmBrokerOutboxQueryOptions = <TData = Awaited<ReturnType<typeof findAdmBrokerOutbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerOutboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerOutbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmBrokerOutboxQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmBrokerOutbox>>> = ({ signal }) => findAdmBrokerOutbox(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerOutbox>>, TError, TData>;
};

export type FindAdmBrokerOutboxQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmBrokerOutbox>>>;
export type FindAdmBrokerOutboxQueryError = unknown;

export function useFindAdmBrokerOutbox<TData = Awaited<ReturnType<typeof findAdmBrokerOutbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmBrokerOutboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmBrokerOutbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmBrokerOutboxQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmBrokerOutbox


// CPF PRE-RUNTIME FALLBACK START findAdmFileTransferHistory
export type findAdmFileTransferHistoryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmFileTransferHistoryResponseSuccess = (findAdmFileTransferHistoryResponse200) & {
  headers: Headers;
};

export type findAdmFileTransferHistoryResponse = (findAdmFileTransferHistoryResponseSuccess)

export const getFindAdmFileTransferHistoryUrl = () => `/adm/api/reliability/file-transfers`;

export const findAdmFileTransferHistory = async (params?: FindAdmFileTransferHistoryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmFileTransferHistoryResponse> => {
  return cpfOrvalRequest<findAdmFileTransferHistoryResponse>(getFindAdmFileTransferHistoryUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, transactionId: params?.transactionId, endpointCode: params?.endpointCode, limit: params?.limit },
  });
};

export const getFindAdmFileTransferHistoryQueryKey = (params?: MaybeRefOrGetter<FindAdmFileTransferHistoryParams>) => ["adm", "api", "reliability", "file-transfers", toValue(params)] as const;

export const getFindAdmFileTransferHistoryQueryOptions = <TData = Awaited<ReturnType<typeof findAdmFileTransferHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmFileTransferHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmFileTransferHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmFileTransferHistoryQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmFileTransferHistory>>> = ({ signal }) => findAdmFileTransferHistory(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmFileTransferHistory>>, TError, TData>;
};

export type FindAdmFileTransferHistoryQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmFileTransferHistory>>>;
export type FindAdmFileTransferHistoryQueryError = unknown;

export function useFindAdmFileTransferHistory<TData = Awaited<ReturnType<typeof findAdmFileTransferHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmFileTransferHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmFileTransferHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmFileTransferHistoryQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmFileTransferHistory


// CPF PRE-RUNTIME FALLBACK START findAdmIdempotencyRecords
export type findAdmIdempotencyRecordsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmIdempotencyRecordsResponseSuccess = (findAdmIdempotencyRecordsResponse200) & {
  headers: Headers;
};

export type findAdmIdempotencyRecordsResponse = (findAdmIdempotencyRecordsResponseSuccess)

export const getFindAdmIdempotencyRecordsUrl = () => `/adm/api/reliability/idempotency`;

export const findAdmIdempotencyRecords = async (params?: FindAdmIdempotencyRecordsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmIdempotencyRecordsResponse> => {
  return cpfOrvalRequest<findAdmIdempotencyRecordsResponse>(getFindAdmIdempotencyRecordsUrl(), {
    ...options,
    method: 'GET',
    params: { scope: params?.scope, status: params?.status, key: params?.key, limit: params?.limit },
  });
};

export const getFindAdmIdempotencyRecordsQueryKey = (params?: MaybeRefOrGetter<FindAdmIdempotencyRecordsParams>) => ["adm", "api", "reliability", "idempotency", toValue(params)] as const;

export const getFindAdmIdempotencyRecordsQueryOptions = <TData = Awaited<ReturnType<typeof findAdmIdempotencyRecords>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmIdempotencyRecordsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmIdempotencyRecords>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmIdempotencyRecordsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmIdempotencyRecords>>> = ({ signal }) => findAdmIdempotencyRecords(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmIdempotencyRecords>>, TError, TData>;
};

export type FindAdmIdempotencyRecordsQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmIdempotencyRecords>>>;
export type FindAdmIdempotencyRecordsQueryError = unknown;

export function useFindAdmIdempotencyRecords<TData = Awaited<ReturnType<typeof findAdmIdempotencyRecords>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmIdempotencyRecordsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmIdempotencyRecords>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmIdempotencyRecordsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmIdempotencyRecords


// CPF PRE-RUNTIME FALLBACK START getAdmTransactionLogRecoveryStatus
export type getAdmTransactionLogRecoveryStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type getAdmTransactionLogRecoveryStatusResponseSuccess = (getAdmTransactionLogRecoveryStatusResponse200) & {
  headers: Headers;
};

export type getAdmTransactionLogRecoveryStatusResponse = (getAdmTransactionLogRecoveryStatusResponseSuccess)

export const getGetAdmTransactionLogRecoveryStatusUrl = () => `/adm/api/reliability/transaction-log-recovery`;

export const getAdmTransactionLogRecoveryStatus = async (options?: CpfOrvalGeneratedRequestOptions): Promise<getAdmTransactionLogRecoveryStatusResponse> => {
  return cpfOrvalRequest<getAdmTransactionLogRecoveryStatusResponse>(getGetAdmTransactionLogRecoveryStatusUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getGetAdmTransactionLogRecoveryStatusQueryKey = () => ["adm", "api", "reliability", "transaction-log-recovery"] as const;

export const getGetAdmTransactionLogRecoveryStatusQueryOptions = <TData = Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getGetAdmTransactionLogRecoveryStatusQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>> = ({ signal }) => getAdmTransactionLogRecoveryStatus({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>, TError, TData>;
};

export type GetAdmTransactionLogRecoveryStatusQueryResult = NonNullable<Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>>;
export type GetAdmTransactionLogRecoveryStatusQueryError = unknown;

export function useGetAdmTransactionLogRecoveryStatus<TData = Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmTransactionLogRecoveryStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAdmTransactionLogRecoveryStatusQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END getAdmTransactionLogRecoveryStatus


// CPF PRE-RUNTIME FALLBACK START retryAdmTraceRecoveryPoison
export type retryAdmTraceRecoveryPoisonResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type retryAdmTraceRecoveryPoisonResponseSuccess = (retryAdmTraceRecoveryPoisonResponse200) & {
  headers: Headers;
};

export type retryAdmTraceRecoveryPoisonResponse = (retryAdmTraceRecoveryPoisonResponseSuccess)

export const getRetryAdmTraceRecoveryPoisonUrl = (target: string, recoveryEventId: string) => `/adm/api/reliability/transaction-log-recovery/poison/${encodeURIComponent(String(target))}/${encodeURIComponent(String(recoveryEventId))}/retry`;

export const retryAdmTraceRecoveryPoison = async (target: string, recoveryEventId: string, data: AdmReliabilityActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<retryAdmTraceRecoveryPoisonResponse> => {
  return cpfOrvalRequest<retryAdmTraceRecoveryPoisonResponse>(getRetryAdmTraceRecoveryPoisonUrl(target, recoveryEventId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getRetryAdmTraceRecoveryPoisonMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>, TError, {target: string; recoveryEventId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>, TError, {target: string; recoveryEventId: string; data: AdmReliabilityActionRequest}, TContext> => {
  const mutationKey = ['retryAdmTraceRecoveryPoison'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>, {target: string; recoveryEventId: string; data: AdmReliabilityActionRequest}> = (props) => {
    const { target, recoveryEventId, data } = props;
    return retryAdmTraceRecoveryPoison(target, recoveryEventId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type RetryAdmTraceRecoveryPoisonMutationResult = NonNullable<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>>;
export type RetryAdmTraceRecoveryPoisonMutationBody = AdmReliabilityActionRequest;
export type RetryAdmTraceRecoveryPoisonMutationError = unknown;

export const useRetryAdmTraceRecoveryPoison = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>, TError, {target: string; recoveryEventId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof retryAdmTraceRecoveryPoison>>, TError, {target: string; recoveryEventId: string; data: AdmReliabilityActionRequest}, TContext> => {
  return useMutation(getRetryAdmTraceRecoveryPoisonMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END retryAdmTraceRecoveryPoison


// CPF PRE-RUNTIME FALLBACK START runAdmTransactionLogRecovery
export type runAdmTransactionLogRecoveryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type runAdmTransactionLogRecoveryResponseSuccess = (runAdmTransactionLogRecoveryResponse200) & {
  headers: Headers;
};

export type runAdmTransactionLogRecoveryResponse = (runAdmTransactionLogRecoveryResponseSuccess)

export const getRunAdmTransactionLogRecoveryUrl = () => `/adm/api/reliability/transaction-log-recovery/run`;

export const runAdmTransactionLogRecovery = async (data: AdmReliabilityActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<runAdmTransactionLogRecoveryResponse> => {
  return cpfOrvalRequest<runAdmTransactionLogRecoveryResponse>(getRunAdmTransactionLogRecoveryUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getRunAdmTransactionLogRecoveryMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>, TError, {data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>, TError, {data: AdmReliabilityActionRequest}, TContext> => {
  const mutationKey = ['runAdmTransactionLogRecovery'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>, {data: AdmReliabilityActionRequest}> = (props) => {
    const { data } = props;
    return runAdmTransactionLogRecovery(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type RunAdmTransactionLogRecoveryMutationResult = NonNullable<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>>;
export type RunAdmTransactionLogRecoveryMutationBody = AdmReliabilityActionRequest;
export type RunAdmTransactionLogRecoveryMutationError = unknown;

export const useRunAdmTransactionLogRecovery = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>, TError, {data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof runAdmTransactionLogRecovery>>, TError, {data: AdmReliabilityActionRequest}, TContext> => {
  return useMutation(getRunAdmTransactionLogRecoveryMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END runAdmTransactionLogRecovery


// CPF PRE-RUNTIME FALLBACK START findAdmUnknownResults
export type findAdmUnknownResultsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type findAdmUnknownResultsResponseSuccess = (findAdmUnknownResultsResponse200) & {
  headers: Headers;
};

export type findAdmUnknownResultsResponse = (findAdmUnknownResultsResponseSuccess)

export const getFindAdmUnknownResultsUrl = () => `/adm/api/reliability/unknown-results`;

export const findAdmUnknownResults = async (params?: FindAdmUnknownResultsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<findAdmUnknownResultsResponse> => {
  return cpfOrvalRequest<findAdmUnknownResultsResponse>(getFindAdmUnknownResultsUrl(), {
    ...options,
    method: 'GET',
    params: { type: params?.type, status: params?.status, transactionId: params?.transactionId, limit: params?.limit },
  });
};

export const getFindAdmUnknownResultsQueryKey = (params?: MaybeRefOrGetter<FindAdmUnknownResultsParams>) => ["adm", "api", "reliability", "unknown-results", toValue(params)] as const;

export const getFindAdmUnknownResultsQueryOptions = <TData = Awaited<ReturnType<typeof findAdmUnknownResults>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmUnknownResultsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmUnknownResults>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getFindAdmUnknownResultsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof findAdmUnknownResults>>> = ({ signal }) => findAdmUnknownResults(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof findAdmUnknownResults>>, TError, TData>;
};

export type FindAdmUnknownResultsQueryResult = NonNullable<Awaited<ReturnType<typeof findAdmUnknownResults>>>;
export type FindAdmUnknownResultsQueryError = unknown;

export function useFindAdmUnknownResults<TData = Awaited<ReturnType<typeof findAdmUnknownResults>>, TError = unknown>(
  params?: MaybeRefOrGetter<FindAdmUnknownResultsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof findAdmUnknownResults>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getFindAdmUnknownResultsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END findAdmUnknownResults


// CPF PRE-RUNTIME FALLBACK START resolveAdmUnknownResult
export type resolveAdmUnknownResultResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type resolveAdmUnknownResultResponseSuccess = (resolveAdmUnknownResultResponse200) & {
  headers: Headers;
};

export type resolveAdmUnknownResultResponse = (resolveAdmUnknownResultResponseSuccess)

export const getResolveAdmUnknownResultUrl = (unknownId: string) => `/adm/api/reliability/unknown-results/${encodeURIComponent(String(unknownId))}/resolve`;

export const resolveAdmUnknownResult = async (unknownId: string, data: AdmReliabilityActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<resolveAdmUnknownResultResponse> => {
  return cpfOrvalRequest<resolveAdmUnknownResultResponse>(getResolveAdmUnknownResultUrl(unknownId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getResolveAdmUnknownResultMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof resolveAdmUnknownResult>>, TError, {unknownId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof resolveAdmUnknownResult>>, TError, {unknownId: string; data: AdmReliabilityActionRequest}, TContext> => {
  const mutationKey = ['resolveAdmUnknownResult'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof resolveAdmUnknownResult>>, {unknownId: string; data: AdmReliabilityActionRequest}> = (props) => {
    const { unknownId, data } = props;
    return resolveAdmUnknownResult(unknownId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type ResolveAdmUnknownResultMutationResult = NonNullable<Awaited<ReturnType<typeof resolveAdmUnknownResult>>>;
export type ResolveAdmUnknownResultMutationBody = AdmReliabilityActionRequest;
export type ResolveAdmUnknownResultMutationError = unknown;

export const useResolveAdmUnknownResult = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof resolveAdmUnknownResult>>, TError, {unknownId: string; data: AdmReliabilityActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof resolveAdmUnknownResult>>, TError, {unknownId: string; data: AdmReliabilityActionRequest}, TContext> => {
  return useMutation(getResolveAdmUnknownResultMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END resolveAdmUnknownResult


// CPF PRE-RUNTIME FALLBACK START admRemoteLogSearch
export type admRemoteLogSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogSearchResponseSuccess = (admRemoteLogSearchResponse200) & {
  headers: Headers;
};

export type admRemoteLogSearchResponse = (admRemoteLogSearchResponseSuccess)

export const getAdmRemoteLogSearchUrl = () => `/adm/api/remote-logs`;

export const admRemoteLogSearch = async (params?: AdmRemoteLogSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogSearchResponse> => {
  return cpfOrvalRequest<admRemoteLogSearchResponse>(getAdmRemoteLogSearchUrl(), {
    ...options,
    method: 'GET',
    params: { environment: params?.environment, module: params?.module, service: params?.service, instance: params?.instance, logType: params?.logType, fileName: params?.fileName, standardTransactionId: params?.standardTransactionId, standardBatchId: params?.standardBatchId, transactionId: params?.transactionId, segmentId: params?.segmentId, jobInstanceId: params?.jobInstanceId, jobExecutionId: params?.jobExecutionId, stepExecutionId: params?.stepExecutionId, schedulerId: params?.schedulerId, modifiedFrom: params?.modifiedFrom, modifiedTo: params?.modifiedTo, minSize: params?.minSize, maxSize: params?.maxSize, compressed: params?.compressed, active: params?.active, limit: params?.limit },
  });
};

export const getAdmRemoteLogSearchQueryKey = (params?: MaybeRefOrGetter<AdmRemoteLogSearchParams>) => ["adm", "api", "remote-logs", toValue(params)] as const;

export const getAdmRemoteLogSearchQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmRemoteLogSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogSearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogSearch>>> = ({ signal }) => admRemoteLogSearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogSearch>>, TError, TData>;
};

export type AdmRemoteLogSearchQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogSearch>>>;
export type AdmRemoteLogSearchQueryError = unknown;

export function useAdmRemoteLogSearch<TData = Awaited<ReturnType<typeof admRemoteLogSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmRemoteLogSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogSearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogSearch


// CPF PRE-RUNTIME FALLBACK START admRemoteLogBundleJobCreate
export type admRemoteLogBundleJobCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogBundleJobCreateResponseSuccess = (admRemoteLogBundleJobCreateResponse200) & {
  headers: Headers;
};

export type admRemoteLogBundleJobCreateResponse = (admRemoteLogBundleJobCreateResponseSuccess)

export const getAdmRemoteLogBundleJobCreateUrl = () => `/adm/api/remote-logs/bundle-jobs`;

export const admRemoteLogBundleJobCreate = async (data: AdmRemoteLogBundleJobCreateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogBundleJobCreateResponse> => {
  return cpfOrvalRequest<admRemoteLogBundleJobCreateResponse>(getAdmRemoteLogBundleJobCreateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRemoteLogBundleJobCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>, TError, {data: AdmRemoteLogBundleJobCreateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>, TError, {data: AdmRemoteLogBundleJobCreateRequest}, TContext> => {
  const mutationKey = ['admRemoteLogBundleJobCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>, {data: AdmRemoteLogBundleJobCreateRequest}> = (props) => {
    const { data } = props;
    return admRemoteLogBundleJobCreate(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRemoteLogBundleJobCreateMutationResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>>;
export type AdmRemoteLogBundleJobCreateMutationBody = AdmRemoteLogBundleJobCreateRequest;
export type AdmRemoteLogBundleJobCreateMutationError = unknown;

export const useAdmRemoteLogBundleJobCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>, TError, {data: AdmRemoteLogBundleJobCreateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRemoteLogBundleJobCreate>>, TError, {data: AdmRemoteLogBundleJobCreateRequest}, TContext> => {
  return useMutation(getAdmRemoteLogBundleJobCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRemoteLogBundleJobCreate


// CPF PRE-RUNTIME FALLBACK START admRemoteLogBundleJobFind
export type admRemoteLogBundleJobFindResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogBundleJobFindResponseSuccess = (admRemoteLogBundleJobFindResponse200) & {
  headers: Headers;
};

export type admRemoteLogBundleJobFindResponse = (admRemoteLogBundleJobFindResponseSuccess)

export const getAdmRemoteLogBundleJobFindUrl = (jobId: string) => `/adm/api/remote-logs/bundle-jobs/${encodeURIComponent(String(jobId))}`;

export const admRemoteLogBundleJobFind = async (jobId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogBundleJobFindResponse> => {
  return cpfOrvalRequest<admRemoteLogBundleJobFindResponse>(getAdmRemoteLogBundleJobFindUrl(jobId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRemoteLogBundleJobFindQueryKey = (jobId: MaybeRefOrGetter<string>) => ["adm", "api", "remote-logs", "bundle-jobs", jobId] as const;

export const getAdmRemoteLogBundleJobFindQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogBundleJobFindQueryKey(toValue(jobId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>> = ({ signal }) => admRemoteLogBundleJobFind(toValue(jobId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>, TError, TData>;
};

export type AdmRemoteLogBundleJobFindQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>>;
export type AdmRemoteLogBundleJobFindQueryError = unknown;

export function useAdmRemoteLogBundleJobFind<TData = Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobFind>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogBundleJobFindQueryOptions(jobId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogBundleJobFind


// CPF PRE-RUNTIME FALLBACK START admRemoteLogBundleJobDownload
export type admRemoteLogBundleJobDownloadResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogBundleJobDownloadResponseSuccess = (admRemoteLogBundleJobDownloadResponse200) & {
  headers: Headers;
};

export type admRemoteLogBundleJobDownloadResponse = (admRemoteLogBundleJobDownloadResponseSuccess)

export const getAdmRemoteLogBundleJobDownloadUrl = (jobId: string) => `/adm/api/remote-logs/bundle-jobs/${encodeURIComponent(String(jobId))}/download`;

export const admRemoteLogBundleJobDownload = async (jobId: string, params: AdmRemoteLogBundleJobDownloadParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogBundleJobDownloadResponse> => {
  return cpfOrvalRequest<admRemoteLogBundleJobDownloadResponse>(getAdmRemoteLogBundleJobDownloadUrl(jobId), {
    ...options,
    method: 'GET',
    params: { token: params.token, reason: params.reason },
  });
};

export const getAdmRemoteLogBundleJobDownloadQueryKey = (jobId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogBundleJobDownloadParams>) => ["adm", "api", "remote-logs", "bundle-jobs", jobId, "download", toValue(params)] as const;

export const getAdmRemoteLogBundleJobDownloadQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogBundleJobDownloadParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogBundleJobDownloadQueryKey(toValue(jobId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>> = ({ signal }) => admRemoteLogBundleJobDownload(toValue(jobId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(jobId) !== null && toValue(jobId) !== undefined && toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>, TError, TData>;
};

export type AdmRemoteLogBundleJobDownloadQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>>;
export type AdmRemoteLogBundleJobDownloadQueryError = unknown;

export function useAdmRemoteLogBundleJobDownload<TData = Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>, TError = unknown>(
  jobId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogBundleJobDownloadParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogBundleJobDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogBundleJobDownloadQueryOptions(jobId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogBundleJobDownload


// CPF PRE-RUNTIME FALLBACK START admRemoteLogBundleDownloadTokenIssue
export type admRemoteLogBundleDownloadTokenIssueResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogBundleDownloadTokenIssueResponseSuccess = (admRemoteLogBundleDownloadTokenIssueResponse200) & {
  headers: Headers;
};

export type admRemoteLogBundleDownloadTokenIssueResponse = (admRemoteLogBundleDownloadTokenIssueResponseSuccess)

export const getAdmRemoteLogBundleDownloadTokenIssueUrl = (jobId: string) => `/adm/api/remote-logs/bundle-jobs/${encodeURIComponent(String(jobId))}/download-tokens`;

export const admRemoteLogBundleDownloadTokenIssue = async (jobId: string, data: AdmRemoteLogBundleDownloadTokenIssueRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogBundleDownloadTokenIssueResponse> => {
  return cpfOrvalRequest<admRemoteLogBundleDownloadTokenIssueResponse>(getAdmRemoteLogBundleDownloadTokenIssueUrl(jobId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRemoteLogBundleDownloadTokenIssueMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>, TError, {jobId: string; data: AdmRemoteLogBundleDownloadTokenIssueRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>, TError, {jobId: string; data: AdmRemoteLogBundleDownloadTokenIssueRequest}, TContext> => {
  const mutationKey = ['admRemoteLogBundleDownloadTokenIssue'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>, {jobId: string; data: AdmRemoteLogBundleDownloadTokenIssueRequest}> = (props) => {
    const { jobId, data } = props;
    return admRemoteLogBundleDownloadTokenIssue(jobId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRemoteLogBundleDownloadTokenIssueMutationResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>>;
export type AdmRemoteLogBundleDownloadTokenIssueMutationBody = AdmRemoteLogBundleDownloadTokenIssueRequest;
export type AdmRemoteLogBundleDownloadTokenIssueMutationError = unknown;

export const useAdmRemoteLogBundleDownloadTokenIssue = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>, TError, {jobId: string; data: AdmRemoteLogBundleDownloadTokenIssueRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRemoteLogBundleDownloadTokenIssue>>, TError, {jobId: string; data: AdmRemoteLogBundleDownloadTokenIssueRequest}, TContext> => {
  return useMutation(getAdmRemoteLogBundleDownloadTokenIssueMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRemoteLogBundleDownloadTokenIssue


// CPF PRE-RUNTIME FALLBACK START admRemoteLogBundleDownload
export type admRemoteLogBundleDownloadResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogBundleDownloadResponseSuccess = (admRemoteLogBundleDownloadResponse200) & {
  headers: Headers;
};

export type admRemoteLogBundleDownloadResponse = (admRemoteLogBundleDownloadResponseSuccess)

export const getAdmRemoteLogBundleDownloadUrl = () => `/adm/api/remote-logs/bundles`;

export const admRemoteLogBundleDownload = async (data: AdmRemoteLogBundleDownloadRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogBundleDownloadResponse> => {
  return cpfOrvalRequest<admRemoteLogBundleDownloadResponse>(getAdmRemoteLogBundleDownloadUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRemoteLogBundleDownloadMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>, TError, {data: AdmRemoteLogBundleDownloadRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>, TError, {data: AdmRemoteLogBundleDownloadRequest}, TContext> => {
  const mutationKey = ['admRemoteLogBundleDownload'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>, {data: AdmRemoteLogBundleDownloadRequest}> = (props) => {
    const { data } = props;
    return admRemoteLogBundleDownload(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRemoteLogBundleDownloadMutationResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>>;
export type AdmRemoteLogBundleDownloadMutationBody = AdmRemoteLogBundleDownloadRequest;
export type AdmRemoteLogBundleDownloadMutationError = unknown;

export const useAdmRemoteLogBundleDownload = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>, TError, {data: AdmRemoteLogBundleDownloadRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRemoteLogBundleDownload>>, TError, {data: AdmRemoteLogBundleDownloadRequest}, TContext> => {
  return useMutation(getAdmRemoteLogBundleDownloadMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRemoteLogBundleDownload


// CPF PRE-RUNTIME FALLBACK START admRemoteLogDiagnostics
export type admRemoteLogDiagnosticsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogDiagnosticsResponseSuccess = (admRemoteLogDiagnosticsResponse200) & {
  headers: Headers;
};

export type admRemoteLogDiagnosticsResponse = (admRemoteLogDiagnosticsResponseSuccess)

export const getAdmRemoteLogDiagnosticsUrl = () => `/adm/api/remote-logs/diagnostics`;

export const admRemoteLogDiagnostics = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogDiagnosticsResponse> => {
  return cpfOrvalRequest<admRemoteLogDiagnosticsResponse>(getAdmRemoteLogDiagnosticsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRemoteLogDiagnosticsQueryKey = () => ["adm", "api", "remote-logs", "diagnostics"] as const;

export const getAdmRemoteLogDiagnosticsQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogDiagnostics>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDiagnostics>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogDiagnosticsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogDiagnostics>>> = ({ signal }) => admRemoteLogDiagnostics({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDiagnostics>>, TError, TData>;
};

export type AdmRemoteLogDiagnosticsQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogDiagnostics>>>;
export type AdmRemoteLogDiagnosticsQueryError = unknown;

export function useAdmRemoteLogDiagnostics<TData = Awaited<ReturnType<typeof admRemoteLogDiagnostics>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDiagnostics>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogDiagnosticsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogDiagnostics


// CPF PRE-RUNTIME FALLBACK START admRemoteLogDownload
export type admRemoteLogDownloadResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogDownloadResponseSuccess = (admRemoteLogDownloadResponse200) & {
  headers: Headers;
};

export type admRemoteLogDownloadResponse = (admRemoteLogDownloadResponseSuccess)

export const getAdmRemoteLogDownloadUrl = (artifactId: string) => `/adm/api/remote-logs/${encodeURIComponent(String(artifactId))}/download`;

export const admRemoteLogDownload = async (artifactId: string, params: AdmRemoteLogDownloadParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogDownloadResponse> => {
  return cpfOrvalRequest<admRemoteLogDownloadResponse>(getAdmRemoteLogDownloadUrl(artifactId), {
    ...options,
    method: 'GET',
    params: { reason: params.reason },
  });
};

export const getAdmRemoteLogDownloadQueryKey = (artifactId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogDownloadParams>) => ["adm", "api", "remote-logs", artifactId, "download", toValue(params)] as const;

export const getAdmRemoteLogDownloadQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogDownload>>, TError = unknown>(
  artifactId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogDownloadParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogDownloadQueryKey(toValue(artifactId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogDownload>>> = ({ signal }) => admRemoteLogDownload(toValue(artifactId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(artifactId) !== null && toValue(artifactId) !== undefined && toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDownload>>, TError, TData>;
};

export type AdmRemoteLogDownloadQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogDownload>>>;
export type AdmRemoteLogDownloadQueryError = unknown;

export function useAdmRemoteLogDownload<TData = Awaited<ReturnType<typeof admRemoteLogDownload>>, TError = unknown>(
  artifactId: MaybeRefOrGetter<string>, params: MaybeRefOrGetter<AdmRemoteLogDownloadParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogDownload>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogDownloadQueryOptions(artifactId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogDownload


// CPF PRE-RUNTIME FALLBACK START admRemoteLogPreview
export type admRemoteLogPreviewResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRemoteLogPreviewResponseSuccess = (admRemoteLogPreviewResponse200) & {
  headers: Headers;
};

export type admRemoteLogPreviewResponse = (admRemoteLogPreviewResponseSuccess)

export const getAdmRemoteLogPreviewUrl = (artifactId: string) => `/adm/api/remote-logs/${encodeURIComponent(String(artifactId))}/preview`;

export const admRemoteLogPreview = async (artifactId: string, params?: AdmRemoteLogPreviewParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRemoteLogPreviewResponse> => {
  return cpfOrvalRequest<admRemoteLogPreviewResponse>(getAdmRemoteLogPreviewUrl(artifactId), {
    ...options,
    method: 'GET',
    params: { lastLines: params?.lastLines, keyword: params?.keyword },
  });
};

export const getAdmRemoteLogPreviewQueryKey = (artifactId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmRemoteLogPreviewParams>) => ["adm", "api", "remote-logs", artifactId, "preview", toValue(params)] as const;

export const getAdmRemoteLogPreviewQueryOptions = <TData = Awaited<ReturnType<typeof admRemoteLogPreview>>, TError = unknown>(
  artifactId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmRemoteLogPreviewParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogPreview>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRemoteLogPreviewQueryKey(toValue(artifactId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRemoteLogPreview>>> = ({ signal }) => admRemoteLogPreview(toValue(artifactId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(artifactId) !== null && toValue(artifactId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogPreview>>, TError, TData>;
};

export type AdmRemoteLogPreviewQueryResult = NonNullable<Awaited<ReturnType<typeof admRemoteLogPreview>>>;
export type AdmRemoteLogPreviewQueryError = unknown;

export function useAdmRemoteLogPreview<TData = Awaited<ReturnType<typeof admRemoteLogPreview>>, TError = unknown>(
  artifactId: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<AdmRemoteLogPreviewParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRemoteLogPreview>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRemoteLogPreviewQueryOptions(artifactId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRemoteLogPreview


// CPF PRE-RUNTIME FALLBACK START admResponseCodeFindAll
export type admResponseCodeFindAllResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResponseCodeFindAllResponseSuccess = (admResponseCodeFindAllResponse200) & {
  headers: Headers;
};

export type admResponseCodeFindAllResponse = (admResponseCodeFindAllResponseSuccess)

export const getAdmResponseCodeFindAllUrl = () => `/adm/api/response-codes`;

export const admResponseCodeFindAll = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admResponseCodeFindAllResponse> => {
  return cpfOrvalRequest<admResponseCodeFindAllResponse>(getAdmResponseCodeFindAllUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmResponseCodeFindAllQueryKey = () => ["adm", "api", "response-codes"] as const;

export const getAdmResponseCodeFindAllQueryOptions = <TData = Awaited<ReturnType<typeof admResponseCodeFindAll>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindAll>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmResponseCodeFindAllQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admResponseCodeFindAll>>> = ({ signal }) => admResponseCodeFindAll({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindAll>>, TError, TData>;
};

export type AdmResponseCodeFindAllQueryResult = NonNullable<Awaited<ReturnType<typeof admResponseCodeFindAll>>>;
export type AdmResponseCodeFindAllQueryError = unknown;

export function useAdmResponseCodeFindAll<TData = Awaited<ReturnType<typeof admResponseCodeFindAll>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindAll>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmResponseCodeFindAllQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admResponseCodeFindAll


// CPF PRE-RUNTIME FALLBACK START admResponseCodeCreate
export type admResponseCodeCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResponseCodeCreateResponseSuccess = (admResponseCodeCreateResponse200) & {
  headers: Headers;
};

export type admResponseCodeCreateResponse = (admResponseCodeCreateResponseSuccess)

export const getAdmResponseCodeCreateUrl = () => `/adm/api/response-codes`;

export const admResponseCodeCreate = async (data: CommonResponseCodeRequest, params: AdmResponseCodeCreateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResponseCodeCreateResponse> => {
  return cpfOrvalRequest<admResponseCodeCreateResponse>(getAdmResponseCodeCreateUrl(), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmResponseCodeCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: AdmResponseCodeCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: AdmResponseCodeCreateParams}, TContext> => {
  const mutationKey = ['admResponseCodeCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResponseCodeCreate>>, {data: CommonResponseCodeRequest; params: AdmResponseCodeCreateParams}> = (props) => {
    const { data, params } = props;
    return admResponseCodeCreate(data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResponseCodeCreateMutationResult = NonNullable<Awaited<ReturnType<typeof admResponseCodeCreate>>>;
export type AdmResponseCodeCreateMutationBody = CommonResponseCodeRequest;
export type AdmResponseCodeCreateMutationError = unknown;

export const useAdmResponseCodeCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: AdmResponseCodeCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: AdmResponseCodeCreateParams}, TContext> => {
  return useMutation(getAdmResponseCodeCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResponseCodeCreate


// CPF PRE-RUNTIME FALLBACK START admResponseCodeDelete
export type admResponseCodeDeleteResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResponseCodeDeleteResponseSuccess = (admResponseCodeDeleteResponse200) & {
  headers: Headers;
};

export type admResponseCodeDeleteResponse = (admResponseCodeDeleteResponseSuccess)

export const getAdmResponseCodeDeleteUrl = (responseCode: string) => `/adm/api/response-codes/${encodeURIComponent(String(responseCode))}`;

export const admResponseCodeDelete = async (responseCode: string, params: AdmResponseCodeDeleteParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResponseCodeDeleteResponse> => {
  return cpfOrvalRequest<admResponseCodeDeleteResponse>(getAdmResponseCodeDeleteUrl(responseCode), {
    ...options,
    method: 'DELETE',
    params: { reason: params.reason },
  });
};

export const getAdmResponseCodeDeleteMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeDelete>>, TError, {responseCode: string; params: AdmResponseCodeDeleteParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeDelete>>, TError, {responseCode: string; params: AdmResponseCodeDeleteParams}, TContext> => {
  const mutationKey = ['admResponseCodeDelete'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResponseCodeDelete>>, {responseCode: string; params: AdmResponseCodeDeleteParams}> = (props) => {
    const { responseCode, params } = props;
    return admResponseCodeDelete(responseCode, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResponseCodeDeleteMutationResult = NonNullable<Awaited<ReturnType<typeof admResponseCodeDelete>>>;
export type AdmResponseCodeDeleteMutationBody = never;
export type AdmResponseCodeDeleteMutationError = unknown;

export const useAdmResponseCodeDelete = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeDelete>>, TError, {responseCode: string; params: AdmResponseCodeDeleteParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResponseCodeDelete>>, TError, {responseCode: string; params: AdmResponseCodeDeleteParams}, TContext> => {
  return useMutation(getAdmResponseCodeDeleteMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResponseCodeDelete


// CPF PRE-RUNTIME FALLBACK START admResponseCodeFindOne
export type admResponseCodeFindOneResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResponseCodeFindOneResponseSuccess = (admResponseCodeFindOneResponse200) & {
  headers: Headers;
};

export type admResponseCodeFindOneResponse = (admResponseCodeFindOneResponseSuccess)

export const getAdmResponseCodeFindOneUrl = (responseCode: string) => `/adm/api/response-codes/${encodeURIComponent(String(responseCode))}`;

export const admResponseCodeFindOne = async (responseCode: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admResponseCodeFindOneResponse> => {
  return cpfOrvalRequest<admResponseCodeFindOneResponse>(getAdmResponseCodeFindOneUrl(responseCode), {
    ...options,
    method: 'GET',

  });
};

export const getAdmResponseCodeFindOneQueryKey = (responseCode: MaybeRefOrGetter<string>) => ["adm", "api", "response-codes", responseCode] as const;

export const getAdmResponseCodeFindOneQueryOptions = <TData = Awaited<ReturnType<typeof admResponseCodeFindOne>>, TError = unknown>(
  responseCode: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindOne>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmResponseCodeFindOneQueryKey(toValue(responseCode));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admResponseCodeFindOne>>> = ({ signal }) => admResponseCodeFindOne(toValue(responseCode), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(responseCode) !== null && toValue(responseCode) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindOne>>, TError, TData>;
};

export type AdmResponseCodeFindOneQueryResult = NonNullable<Awaited<ReturnType<typeof admResponseCodeFindOne>>>;
export type AdmResponseCodeFindOneQueryError = unknown;

export function useAdmResponseCodeFindOne<TData = Awaited<ReturnType<typeof admResponseCodeFindOne>>, TError = unknown>(
  responseCode: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admResponseCodeFindOne>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmResponseCodeFindOneQueryOptions(responseCode, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admResponseCodeFindOne


// CPF PRE-RUNTIME FALLBACK START admResponseCodeUpdate
export type admResponseCodeUpdateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admResponseCodeUpdateResponseSuccess = (admResponseCodeUpdateResponse200) & {
  headers: Headers;
};

export type admResponseCodeUpdateResponse = (admResponseCodeUpdateResponseSuccess)

export const getAdmResponseCodeUpdateUrl = (responseCode: string) => `/adm/api/response-codes/${encodeURIComponent(String(responseCode))}`;

export const admResponseCodeUpdate = async (responseCode: string, data: CommonResponseCodeRequest, params: AdmResponseCodeUpdateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admResponseCodeUpdateResponse> => {
  return cpfOrvalRequest<admResponseCodeUpdateResponse>(getAdmResponseCodeUpdateUrl(responseCode), {
    ...options,
    method: 'PUT',
    params: { reason: params.reason },
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmResponseCodeUpdateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeUpdate>>, TError, {responseCode: string; data: CommonResponseCodeRequest; params: AdmResponseCodeUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeUpdate>>, TError, {responseCode: string; data: CommonResponseCodeRequest; params: AdmResponseCodeUpdateParams}, TContext> => {
  const mutationKey = ['admResponseCodeUpdate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admResponseCodeUpdate>>, {responseCode: string; data: CommonResponseCodeRequest; params: AdmResponseCodeUpdateParams}> = (props) => {
    const { responseCode, data, params } = props;
    return admResponseCodeUpdate(responseCode, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmResponseCodeUpdateMutationResult = NonNullable<Awaited<ReturnType<typeof admResponseCodeUpdate>>>;
export type AdmResponseCodeUpdateMutationBody = CommonResponseCodeRequest;
export type AdmResponseCodeUpdateMutationError = unknown;

export const useAdmResponseCodeUpdate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admResponseCodeUpdate>>, TError, {responseCode: string; data: CommonResponseCodeRequest; params: AdmResponseCodeUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admResponseCodeUpdate>>, TError, {responseCode: string; data: CommonResponseCodeRequest; params: AdmResponseCodeUpdateParams}, TContext> => {
  return useMutation(getAdmResponseCodeUpdateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admResponseCodeUpdate


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindCapabilities
export type admRuntimeControlFindCapabilitiesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindCapabilitiesResponseSuccess = (admRuntimeControlFindCapabilitiesResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindCapabilitiesResponse = (admRuntimeControlFindCapabilitiesResponseSuccess)

export const getAdmRuntimeControlFindCapabilitiesUrl = () => `/adm/api/runtime-control/capabilities`;

export const admRuntimeControlFindCapabilities = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindCapabilitiesResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindCapabilitiesResponse>(getAdmRuntimeControlFindCapabilitiesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindCapabilitiesQueryKey = () => ["adm", "api", "runtime-control", "capabilities"] as const;

export const getAdmRuntimeControlFindCapabilitiesQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindCapabilitiesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>> = ({ signal }) => admRuntimeControlFindCapabilities({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>, TError, TData>;
};

export type AdmRuntimeControlFindCapabilitiesQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>>;
export type AdmRuntimeControlFindCapabilitiesQueryError = unknown;

export function useAdmRuntimeControlFindCapabilities<TData = Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindCapabilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindCapabilitiesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindCapabilities


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlCreateChange
export type admRuntimeControlCreateChangeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlCreateChangeResponseSuccess = (admRuntimeControlCreateChangeResponse200) & {
  headers: Headers;
};

export type admRuntimeControlCreateChangeResponse = (admRuntimeControlCreateChangeResponseSuccess)

export const getAdmRuntimeControlCreateChangeUrl = () => `/adm/api/runtime-control/changes`;

export const admRuntimeControlCreateChange = async (data: AdmRuntimeControlCreateChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlCreateChangeResponse> => {
  return cpfOrvalRequest<admRuntimeControlCreateChangeResponse>(getAdmRuntimeControlCreateChangeUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlCreateChangeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>, TError, {data: AdmRuntimeControlCreateChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>, TError, {data: AdmRuntimeControlCreateChangeRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlCreateChange'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>, {data: AdmRuntimeControlCreateChangeRequest}> = (props) => {
    const { data } = props;
    return admRuntimeControlCreateChange(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlCreateChangeMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>>;
export type AdmRuntimeControlCreateChangeMutationBody = AdmRuntimeControlCreateChangeRequest;
export type AdmRuntimeControlCreateChangeMutationError = unknown;

export const useAdmRuntimeControlCreateChange = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>, TError, {data: AdmRuntimeControlCreateChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlCreateChange>>, TError, {data: AdmRuntimeControlCreateChangeRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlCreateChangeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlCreateChange


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindChange
export type admRuntimeControlFindChangeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindChangeResponseSuccess = (admRuntimeControlFindChangeResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindChangeResponse = (admRuntimeControlFindChangeResponseSuccess)

export const getAdmRuntimeControlFindChangeUrl = (changeId: string) => `/adm/api/runtime-control/changes/${encodeURIComponent(String(changeId))}`;

export const admRuntimeControlFindChange = async (changeId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindChangeResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindChangeResponse>(getAdmRuntimeControlFindChangeUrl(changeId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindChangeQueryKey = (changeId: MaybeRefOrGetter<string>) => ["adm", "api", "runtime-control", "changes", changeId] as const;

export const getAdmRuntimeControlFindChangeQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindChange>>, TError = unknown>(
  changeId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindChange>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindChangeQueryKey(toValue(changeId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindChange>>> = ({ signal }) => admRuntimeControlFindChange(toValue(changeId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(changeId) !== null && toValue(changeId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindChange>>, TError, TData>;
};

export type AdmRuntimeControlFindChangeQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindChange>>>;
export type AdmRuntimeControlFindChangeQueryError = unknown;

export function useAdmRuntimeControlFindChange<TData = Awaited<ReturnType<typeof admRuntimeControlFindChange>>, TError = unknown>(
  changeId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindChange>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindChangeQueryOptions(changeId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindChange


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlVerifyAudit
export type admRuntimeControlVerifyAuditResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlVerifyAuditResponseSuccess = (admRuntimeControlVerifyAuditResponse200) & {
  headers: Headers;
};

export type admRuntimeControlVerifyAuditResponse = (admRuntimeControlVerifyAuditResponseSuccess)

export const getAdmRuntimeControlVerifyAuditUrl = (changeId: string) => `/adm/api/runtime-control/changes/${encodeURIComponent(String(changeId))}/audit/verify`;

export const admRuntimeControlVerifyAudit = async (changeId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlVerifyAuditResponse> => {
  return cpfOrvalRequest<admRuntimeControlVerifyAuditResponse>(getAdmRuntimeControlVerifyAuditUrl(changeId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlVerifyAuditQueryKey = (changeId: MaybeRefOrGetter<string>) => ["adm", "api", "runtime-control", "changes", changeId, "audit", "verify"] as const;

export const getAdmRuntimeControlVerifyAuditQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>, TError = unknown>(
  changeId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlVerifyAuditQueryKey(toValue(changeId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>> = ({ signal }) => admRuntimeControlVerifyAudit(toValue(changeId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(changeId) !== null && toValue(changeId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>, TError, TData>;
};

export type AdmRuntimeControlVerifyAuditQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>>;
export type AdmRuntimeControlVerifyAuditQueryError = unknown;

export function useAdmRuntimeControlVerifyAudit<TData = Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>, TError = unknown>(
  changeId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlVerifyAudit>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlVerifyAuditQueryOptions(changeId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlVerifyAudit


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlCancelChange
export type admRuntimeControlCancelChangeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlCancelChangeResponseSuccess = (admRuntimeControlCancelChangeResponse200) & {
  headers: Headers;
};

export type admRuntimeControlCancelChangeResponse = (admRuntimeControlCancelChangeResponseSuccess)

export const getAdmRuntimeControlCancelChangeUrl = (changeId: string) => `/adm/api/runtime-control/changes/${encodeURIComponent(String(changeId))}/cancel`;

export const admRuntimeControlCancelChange = async (changeId: string, data: AdmRuntimeControlCancelChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlCancelChangeResponse> => {
  return cpfOrvalRequest<admRuntimeControlCancelChangeResponse>(getAdmRuntimeControlCancelChangeUrl(changeId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlCancelChangeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>, TError, {changeId: string; data: AdmRuntimeControlCancelChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>, TError, {changeId: string; data: AdmRuntimeControlCancelChangeRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlCancelChange'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>, {changeId: string; data: AdmRuntimeControlCancelChangeRequest}> = (props) => {
    const { changeId, data } = props;
    return admRuntimeControlCancelChange(changeId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlCancelChangeMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>>;
export type AdmRuntimeControlCancelChangeMutationBody = AdmRuntimeControlCancelChangeRequest;
export type AdmRuntimeControlCancelChangeMutationError = unknown;

export const useAdmRuntimeControlCancelChange = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>, TError, {changeId: string; data: AdmRuntimeControlCancelChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlCancelChange>>, TError, {changeId: string; data: AdmRuntimeControlCancelChangeRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlCancelChangeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlCancelChange


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlRollbackChange
export type admRuntimeControlRollbackChangeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlRollbackChangeResponseSuccess = (admRuntimeControlRollbackChangeResponse200) & {
  headers: Headers;
};

export type admRuntimeControlRollbackChangeResponse = (admRuntimeControlRollbackChangeResponseSuccess)

export const getAdmRuntimeControlRollbackChangeUrl = (changeId: string) => `/adm/api/runtime-control/changes/${encodeURIComponent(String(changeId))}/rollback`;

export const admRuntimeControlRollbackChange = async (changeId: string, data: AdmRuntimeControlRollbackChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlRollbackChangeResponse> => {
  return cpfOrvalRequest<admRuntimeControlRollbackChangeResponse>(getAdmRuntimeControlRollbackChangeUrl(changeId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlRollbackChangeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>, TError, {changeId: string; data: AdmRuntimeControlRollbackChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>, TError, {changeId: string; data: AdmRuntimeControlRollbackChangeRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlRollbackChange'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>, {changeId: string; data: AdmRuntimeControlRollbackChangeRequest}> = (props) => {
    const { changeId, data } = props;
    return admRuntimeControlRollbackChange(changeId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlRollbackChangeMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>>;
export type AdmRuntimeControlRollbackChangeMutationBody = AdmRuntimeControlRollbackChangeRequest;
export type AdmRuntimeControlRollbackChangeMutationError = unknown;

export const useAdmRuntimeControlRollbackChange = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>, TError, {changeId: string; data: AdmRuntimeControlRollbackChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlRollbackChange>>, TError, {changeId: string; data: AdmRuntimeControlRollbackChangeRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlRollbackChangeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlRollbackChange


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlSaveGroup
export type admRuntimeControlSaveGroupResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlSaveGroupResponseSuccess = (admRuntimeControlSaveGroupResponse200) & {
  headers: Headers;
};

export type admRuntimeControlSaveGroupResponse = (admRuntimeControlSaveGroupResponseSuccess)

export const getAdmRuntimeControlSaveGroupUrl = () => `/adm/api/runtime-control/groups`;

export const admRuntimeControlSaveGroup = async (data: AdmRuntimeControlSaveGroupRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlSaveGroupResponse> => {
  return cpfOrvalRequest<admRuntimeControlSaveGroupResponse>(getAdmRuntimeControlSaveGroupUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlSaveGroupMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>, TError, {data: AdmRuntimeControlSaveGroupRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>, TError, {data: AdmRuntimeControlSaveGroupRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlSaveGroup'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>, {data: AdmRuntimeControlSaveGroupRequest}> = (props) => {
    const { data } = props;
    return admRuntimeControlSaveGroup(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlSaveGroupMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>>;
export type AdmRuntimeControlSaveGroupMutationBody = AdmRuntimeControlSaveGroupRequest;
export type AdmRuntimeControlSaveGroupMutationError = unknown;

export const useAdmRuntimeControlSaveGroup = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>, TError, {data: AdmRuntimeControlSaveGroupRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlSaveGroup>>, TError, {data: AdmRuntimeControlSaveGroupRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlSaveGroupMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlSaveGroup


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlDeleteGroup
export type admRuntimeControlDeleteGroupResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlDeleteGroupResponseSuccess = (admRuntimeControlDeleteGroupResponse200) & {
  headers: Headers;
};

export type admRuntimeControlDeleteGroupResponse = (admRuntimeControlDeleteGroupResponseSuccess)

export const getAdmRuntimeControlDeleteGroupUrl = (groupId: string) => `/adm/api/runtime-control/groups/${encodeURIComponent(String(groupId))}`;

export const admRuntimeControlDeleteGroup = async (groupId: string, params: AdmRuntimeControlDeleteGroupParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlDeleteGroupResponse> => {
  return cpfOrvalRequest<admRuntimeControlDeleteGroupResponse>(getAdmRuntimeControlDeleteGroupUrl(groupId), {
    ...options,
    method: 'DELETE',
    params: { operationId: params.operationId, expectedVersion: params.expectedVersion, reason: params.reason },
  });
};

export const getAdmRuntimeControlDeleteGroupMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>, TError, {groupId: string; params: AdmRuntimeControlDeleteGroupParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>, TError, {groupId: string; params: AdmRuntimeControlDeleteGroupParams}, TContext> => {
  const mutationKey = ['admRuntimeControlDeleteGroup'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>, {groupId: string; params: AdmRuntimeControlDeleteGroupParams}> = (props) => {
    const { groupId, params } = props;
    return admRuntimeControlDeleteGroup(groupId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlDeleteGroupMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>>;
export type AdmRuntimeControlDeleteGroupMutationBody = never;
export type AdmRuntimeControlDeleteGroupMutationError = unknown;

export const useAdmRuntimeControlDeleteGroup = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>, TError, {groupId: string; params: AdmRuntimeControlDeleteGroupParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlDeleteGroup>>, TError, {groupId: string; params: AdmRuntimeControlDeleteGroupParams}, TContext> => {
  return useMutation(getAdmRuntimeControlDeleteGroupMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlDeleteGroup


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindGroup
export type admRuntimeControlFindGroupResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindGroupResponseSuccess = (admRuntimeControlFindGroupResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindGroupResponse = (admRuntimeControlFindGroupResponseSuccess)

export const getAdmRuntimeControlFindGroupUrl = (groupId: string) => `/adm/api/runtime-control/groups/${encodeURIComponent(String(groupId))}`;

export const admRuntimeControlFindGroup = async (groupId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindGroupResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindGroupResponse>(getAdmRuntimeControlFindGroupUrl(groupId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindGroupQueryKey = (groupId: MaybeRefOrGetter<string>) => ["adm", "api", "runtime-control", "groups", groupId] as const;

export const getAdmRuntimeControlFindGroupQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindGroup>>, TError = unknown>(
  groupId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindGroup>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindGroupQueryKey(toValue(groupId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindGroup>>> = ({ signal }) => admRuntimeControlFindGroup(toValue(groupId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(groupId) !== null && toValue(groupId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindGroup>>, TError, TData>;
};

export type AdmRuntimeControlFindGroupQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindGroup>>>;
export type AdmRuntimeControlFindGroupQueryError = unknown;

export function useAdmRuntimeControlFindGroup<TData = Awaited<ReturnType<typeof admRuntimeControlFindGroup>>, TError = unknown>(
  groupId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindGroup>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindGroupQueryOptions(groupId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindGroup


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlChangeGroupMember
export type admRuntimeControlChangeGroupMemberResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlChangeGroupMemberResponseSuccess = (admRuntimeControlChangeGroupMemberResponse200) & {
  headers: Headers;
};

export type admRuntimeControlChangeGroupMemberResponse = (admRuntimeControlChangeGroupMemberResponseSuccess)

export const getAdmRuntimeControlChangeGroupMemberUrl = (groupId: string) => `/adm/api/runtime-control/groups/${encodeURIComponent(String(groupId))}/members`;

export const admRuntimeControlChangeGroupMember = async (groupId: string, data: AdmRuntimeControlChangeGroupMemberRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlChangeGroupMemberResponse> => {
  return cpfOrvalRequest<admRuntimeControlChangeGroupMemberResponse>(getAdmRuntimeControlChangeGroupMemberUrl(groupId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlChangeGroupMemberMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>, TError, {groupId: string; data: AdmRuntimeControlChangeGroupMemberRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>, TError, {groupId: string; data: AdmRuntimeControlChangeGroupMemberRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlChangeGroupMember'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>, {groupId: string; data: AdmRuntimeControlChangeGroupMemberRequest}> = (props) => {
    const { groupId, data } = props;
    return admRuntimeControlChangeGroupMember(groupId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlChangeGroupMemberMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>>;
export type AdmRuntimeControlChangeGroupMemberMutationBody = AdmRuntimeControlChangeGroupMemberRequest;
export type AdmRuntimeControlChangeGroupMemberMutationError = unknown;

export const useAdmRuntimeControlChangeGroupMember = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>, TError, {groupId: string; data: AdmRuntimeControlChangeGroupMemberRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlChangeGroupMember>>, TError, {groupId: string; data: AdmRuntimeControlChangeGroupMemberRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlChangeGroupMemberMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlChangeGroupMember


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindHealth
export type admRuntimeControlFindHealthResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindHealthResponseSuccess = (admRuntimeControlFindHealthResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindHealthResponse = (admRuntimeControlFindHealthResponseSuccess)

export const getAdmRuntimeControlFindHealthUrl = () => `/adm/api/runtime-control/health`;

export const admRuntimeControlFindHealth = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindHealthResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindHealthResponse>(getAdmRuntimeControlFindHealthUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindHealthQueryKey = () => ["adm", "api", "runtime-control", "health"] as const;

export const getAdmRuntimeControlFindHealthQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindHealth>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindHealth>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindHealthQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindHealth>>> = ({ signal }) => admRuntimeControlFindHealth({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindHealth>>, TError, TData>;
};

export type AdmRuntimeControlFindHealthQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindHealth>>>;
export type AdmRuntimeControlFindHealthQueryError = unknown;

export function useAdmRuntimeControlFindHealth<TData = Awaited<ReturnType<typeof admRuntimeControlFindHealth>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindHealth>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindHealthQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindHealth


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindByOperation
export type admRuntimeControlFindByOperationResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindByOperationResponseSuccess = (admRuntimeControlFindByOperationResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindByOperationResponse = (admRuntimeControlFindByOperationResponseSuccess)

export const getAdmRuntimeControlFindByOperationUrl = (operationId: string) => `/adm/api/runtime-control/operations/${encodeURIComponent(String(operationId))}`;

export const admRuntimeControlFindByOperation = async (operationId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindByOperationResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindByOperationResponse>(getAdmRuntimeControlFindByOperationUrl(operationId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindByOperationQueryKey = (operationId: MaybeRefOrGetter<string>) => ["adm", "api", "runtime-control", "operations", operationId] as const;

export const getAdmRuntimeControlFindByOperationQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindByOperationQueryKey(toValue(operationId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>> = ({ signal }) => admRuntimeControlFindByOperation(toValue(operationId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(operationId) !== null && toValue(operationId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>, TError, TData>;
};

export type AdmRuntimeControlFindByOperationQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>>;
export type AdmRuntimeControlFindByOperationQueryError = unknown;

export function useAdmRuntimeControlFindByOperation<TData = Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>, TError = unknown>(
  operationId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindByOperation>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindByOperationQueryOptions(operationId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindByOperation


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlPreviewChange
export type admRuntimeControlPreviewChangeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlPreviewChangeResponseSuccess = (admRuntimeControlPreviewChangeResponse200) & {
  headers: Headers;
};

export type admRuntimeControlPreviewChangeResponse = (admRuntimeControlPreviewChangeResponseSuccess)

export const getAdmRuntimeControlPreviewChangeUrl = () => `/adm/api/runtime-control/preview-change`;

export const admRuntimeControlPreviewChange = async (data: AdmRuntimeControlPreviewChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlPreviewChangeResponse> => {
  return cpfOrvalRequest<admRuntimeControlPreviewChangeResponse>(getAdmRuntimeControlPreviewChangeUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlPreviewChangeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>, TError, {data: AdmRuntimeControlPreviewChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>, TError, {data: AdmRuntimeControlPreviewChangeRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlPreviewChange'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>, {data: AdmRuntimeControlPreviewChangeRequest}> = (props) => {
    const { data } = props;
    return admRuntimeControlPreviewChange(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlPreviewChangeMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>>;
export type AdmRuntimeControlPreviewChangeMutationBody = AdmRuntimeControlPreviewChangeRequest;
export type AdmRuntimeControlPreviewChangeMutationError = unknown;

export const useAdmRuntimeControlPreviewChange = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>, TError, {data: AdmRuntimeControlPreviewChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlPreviewChange>>, TError, {data: AdmRuntimeControlPreviewChangeRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlPreviewChangeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlPreviewChange


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlPreviewTargets
export type admRuntimeControlPreviewTargetsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlPreviewTargetsResponseSuccess = (admRuntimeControlPreviewTargetsResponse200) & {
  headers: Headers;
};

export type admRuntimeControlPreviewTargetsResponse = (admRuntimeControlPreviewTargetsResponseSuccess)

export const getAdmRuntimeControlPreviewTargetsUrl = () => `/adm/api/runtime-control/preview-targets`;

export const admRuntimeControlPreviewTargets = async (data: AdmRuntimeControlPreviewTargetsRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlPreviewTargetsResponse> => {
  return cpfOrvalRequest<admRuntimeControlPreviewTargetsResponse>(getAdmRuntimeControlPreviewTargetsUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmRuntimeControlPreviewTargetsMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>, TError, {data: AdmRuntimeControlPreviewTargetsRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>, TError, {data: AdmRuntimeControlPreviewTargetsRequest}, TContext> => {
  const mutationKey = ['admRuntimeControlPreviewTargets'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>, {data: AdmRuntimeControlPreviewTargetsRequest}> = (props) => {
    const { data } = props;
    return admRuntimeControlPreviewTargets(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmRuntimeControlPreviewTargetsMutationResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>>;
export type AdmRuntimeControlPreviewTargetsMutationBody = AdmRuntimeControlPreviewTargetsRequest;
export type AdmRuntimeControlPreviewTargetsMutationError = unknown;

export const useAdmRuntimeControlPreviewTargets = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>, TError, {data: AdmRuntimeControlPreviewTargetsRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admRuntimeControlPreviewTargets>>, TError, {data: AdmRuntimeControlPreviewTargetsRequest}, TContext> => {
  return useMutation(getAdmRuntimeControlPreviewTargetsMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlPreviewTargets


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindStateCatalog
export type admRuntimeControlFindStateCatalogResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindStateCatalogResponseSuccess = (admRuntimeControlFindStateCatalogResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindStateCatalogResponse = (admRuntimeControlFindStateCatalogResponseSuccess)

export const getAdmRuntimeControlFindStateCatalogUrl = () => `/adm/api/runtime-control/states`;

export const admRuntimeControlFindStateCatalog = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindStateCatalogResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindStateCatalogResponse>(getAdmRuntimeControlFindStateCatalogUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmRuntimeControlFindStateCatalogQueryKey = () => ["adm", "api", "runtime-control", "states"] as const;

export const getAdmRuntimeControlFindStateCatalogQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindStateCatalogQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>> = ({ signal }) => admRuntimeControlFindStateCatalog({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>, TError, TData>;
};

export type AdmRuntimeControlFindStateCatalogQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>>;
export type AdmRuntimeControlFindStateCatalogQueryError = unknown;

export function useAdmRuntimeControlFindStateCatalog<TData = Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStateCatalog>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindStateCatalogQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindStateCatalog


// CPF PRE-RUNTIME FALLBACK START admRuntimeControlFindStatus
export type admRuntimeControlFindStatusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admRuntimeControlFindStatusResponseSuccess = (admRuntimeControlFindStatusResponse200) & {
  headers: Headers;
};

export type admRuntimeControlFindStatusResponse = (admRuntimeControlFindStatusResponseSuccess)

export const getAdmRuntimeControlFindStatusUrl = () => `/adm/api/runtime-control/status`;

export const admRuntimeControlFindStatus = async (params?: AdmRuntimeControlFindStatusParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admRuntimeControlFindStatusResponse> => {
  return cpfOrvalRequest<admRuntimeControlFindStatusResponse>(getAdmRuntimeControlFindStatusUrl(), {
    ...options,
    method: 'GET',
    params: { environment: params?.environment, serviceId: params?.serviceId },
  });
};

export const getAdmRuntimeControlFindStatusQueryKey = (params?: MaybeRefOrGetter<AdmRuntimeControlFindStatusParams>) => ["adm", "api", "runtime-control", "status", toValue(params)] as const;

export const getAdmRuntimeControlFindStatusQueryOptions = <TData = Awaited<ReturnType<typeof admRuntimeControlFindStatus>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmRuntimeControlFindStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmRuntimeControlFindStatusQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admRuntimeControlFindStatus>>> = ({ signal }) => admRuntimeControlFindStatus(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStatus>>, TError, TData>;
};

export type AdmRuntimeControlFindStatusQueryResult = NonNullable<Awaited<ReturnType<typeof admRuntimeControlFindStatus>>>;
export type AdmRuntimeControlFindStatusQueryError = unknown;

export function useAdmRuntimeControlFindStatus<TData = Awaited<ReturnType<typeof admRuntimeControlFindStatus>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmRuntimeControlFindStatusParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admRuntimeControlFindStatus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmRuntimeControlFindStatusQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admRuntimeControlFindStatus


// CPF PRE-RUNTIME FALLBACK START admSecretFindMetadata
export type admSecretFindMetadataResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecretFindMetadataResponseSuccess = (admSecretFindMetadataResponse200) & {
  headers: Headers;
};

export type admSecretFindMetadataResponse = (admSecretFindMetadataResponseSuccess)

export const getAdmSecretFindMetadataUrl = () => `/adm/api/secrets/metadata`;

export const admSecretFindMetadata = async (params: AdmSecretFindMetadataParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecretFindMetadataResponse> => {
  return cpfOrvalRequest<admSecretFindMetadataResponse>(getAdmSecretFindMetadataUrl(), {
    ...options,
    method: 'GET',
    params: { provider: params.provider, key: params.key },
  });
};

export const getAdmSecretFindMetadataQueryKey = (params: MaybeRefOrGetter<AdmSecretFindMetadataParams>) => ["adm", "api", "secrets", "metadata", toValue(params)] as const;

export const getAdmSecretFindMetadataQueryOptions = <TData = Awaited<ReturnType<typeof admSecretFindMetadata>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmSecretFindMetadataParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecretFindMetadata>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmSecretFindMetadataQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admSecretFindMetadata>>> = ({ signal }) => admSecretFindMetadata(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admSecretFindMetadata>>, TError, TData>;
};

export type AdmSecretFindMetadataQueryResult = NonNullable<Awaited<ReturnType<typeof admSecretFindMetadata>>>;
export type AdmSecretFindMetadataQueryError = unknown;

export function useAdmSecretFindMetadata<TData = Awaited<ReturnType<typeof admSecretFindMetadata>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmSecretFindMetadataParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecretFindMetadata>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmSecretFindMetadataQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admSecretFindMetadata


// CPF PRE-RUNTIME FALLBACK START admSecretFindProviders
export type admSecretFindProvidersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecretFindProvidersResponseSuccess = (admSecretFindProvidersResponse200) & {
  headers: Headers;
};

export type admSecretFindProvidersResponse = (admSecretFindProvidersResponseSuccess)

export const getAdmSecretFindProvidersUrl = () => `/adm/api/secrets/providers`;

export const admSecretFindProviders = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admSecretFindProvidersResponse> => {
  return cpfOrvalRequest<admSecretFindProvidersResponse>(getAdmSecretFindProvidersUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmSecretFindProvidersQueryKey = () => ["adm", "api", "secrets", "providers"] as const;

export const getAdmSecretFindProvidersQueryOptions = <TData = Awaited<ReturnType<typeof admSecretFindProviders>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecretFindProviders>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmSecretFindProvidersQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admSecretFindProviders>>> = ({ signal }) => admSecretFindProviders({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admSecretFindProviders>>, TError, TData>;
};

export type AdmSecretFindProvidersQueryResult = NonNullable<Awaited<ReturnType<typeof admSecretFindProviders>>>;
export type AdmSecretFindProvidersQueryError = unknown;

export function useAdmSecretFindProviders<TData = Awaited<ReturnType<typeof admSecretFindProviders>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecretFindProviders>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmSecretFindProvidersQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admSecretFindProviders


// CPF PRE-RUNTIME FALLBACK START admSecretRotate
export type admSecretRotateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecretRotateResponseSuccess = (admSecretRotateResponse200) & {
  headers: Headers;
};

export type admSecretRotateResponse = (admSecretRotateResponseSuccess)

export const getAdmSecretRotateUrl = () => `/adm/api/secrets/rotate`;

export const admSecretRotate = async (data: AdmSecretRotateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecretRotateResponse> => {
  return cpfOrvalRequest<admSecretRotateResponse>(getAdmSecretRotateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmSecretRotateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecretRotate>>, TError, {data: AdmSecretRotateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admSecretRotate>>, TError, {data: AdmSecretRotateRequest}, TContext> => {
  const mutationKey = ['admSecretRotate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admSecretRotate>>, {data: AdmSecretRotateRequest}> = (props) => {
    const { data } = props;
    return admSecretRotate(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmSecretRotateMutationResult = NonNullable<Awaited<ReturnType<typeof admSecretRotate>>>;
export type AdmSecretRotateMutationBody = AdmSecretRotateRequest;
export type AdmSecretRotateMutationError = unknown;

export const useAdmSecretRotate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecretRotate>>, TError, {data: AdmSecretRotateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admSecretRotate>>, TError, {data: AdmSecretRotateRequest}, TContext> => {
  return useMutation(getAdmSecretRotateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admSecretRotate


// CPF PRE-RUNTIME FALLBACK START admSecurityFindIpAllowlist
export type admSecurityFindIpAllowlistResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecurityFindIpAllowlistResponseSuccess = (admSecurityFindIpAllowlistResponse200) & {
  headers: Headers;
};

export type admSecurityFindIpAllowlistResponse = (admSecurityFindIpAllowlistResponseSuccess)

export const getAdmSecurityFindIpAllowlistUrl = () => `/adm/api/security/ip-allowlist`;

export const admSecurityFindIpAllowlist = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admSecurityFindIpAllowlistResponse> => {
  return cpfOrvalRequest<admSecurityFindIpAllowlistResponse>(getAdmSecurityFindIpAllowlistUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmSecurityFindIpAllowlistQueryKey = () => ["adm", "api", "security", "ip-allowlist"] as const;

export const getAdmSecurityFindIpAllowlistQueryOptions = <TData = Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmSecurityFindIpAllowlistQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>> = ({ signal }) => admSecurityFindIpAllowlist({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>, TError, TData>;
};

export type AdmSecurityFindIpAllowlistQueryResult = NonNullable<Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>>;
export type AdmSecurityFindIpAllowlistQueryError = unknown;

export function useAdmSecurityFindIpAllowlist<TData = Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindIpAllowlist>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmSecurityFindIpAllowlistQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admSecurityFindIpAllowlist


// CPF PRE-RUNTIME FALLBACK START admSecuritySaveIpAllowlist
export type admSecuritySaveIpAllowlistResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecuritySaveIpAllowlistResponseSuccess = (admSecuritySaveIpAllowlistResponse200) & {
  headers: Headers;
};

export type admSecuritySaveIpAllowlistResponse = (admSecuritySaveIpAllowlistResponseSuccess)

export const getAdmSecuritySaveIpAllowlistUrl = () => `/adm/api/security/ip-allowlist`;

export const admSecuritySaveIpAllowlist = async (data: AdmIpAllowlistRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecuritySaveIpAllowlistResponse> => {
  return cpfOrvalRequest<admSecuritySaveIpAllowlistResponse>(getAdmSecuritySaveIpAllowlistUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmSecuritySaveIpAllowlistMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>, TError, {data: AdmIpAllowlistRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>, TError, {data: AdmIpAllowlistRequest}, TContext> => {
  const mutationKey = ['admSecuritySaveIpAllowlist'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>, {data: AdmIpAllowlistRequest}> = (props) => {
    const { data } = props;
    return admSecuritySaveIpAllowlist(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmSecuritySaveIpAllowlistMutationResult = NonNullable<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>>;
export type AdmSecuritySaveIpAllowlistMutationBody = AdmIpAllowlistRequest;
export type AdmSecuritySaveIpAllowlistMutationError = unknown;

export const useAdmSecuritySaveIpAllowlist = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>, TError, {data: AdmIpAllowlistRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admSecuritySaveIpAllowlist>>, TError, {data: AdmIpAllowlistRequest}, TContext> => {
  return useMutation(getAdmSecuritySaveIpAllowlistMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admSecuritySaveIpAllowlist


// CPF PRE-RUNTIME FALLBACK START admSecurityFindMfaStates
export type admSecurityFindMfaStatesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecurityFindMfaStatesResponseSuccess = (admSecurityFindMfaStatesResponse200) & {
  headers: Headers;
};

export type admSecurityFindMfaStatesResponse = (admSecurityFindMfaStatesResponseSuccess)

export const getAdmSecurityFindMfaStatesUrl = () => `/adm/api/security/mfa`;

export const admSecurityFindMfaStates = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admSecurityFindMfaStatesResponse> => {
  return cpfOrvalRequest<admSecurityFindMfaStatesResponse>(getAdmSecurityFindMfaStatesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmSecurityFindMfaStatesQueryKey = () => ["adm", "api", "security", "mfa"] as const;

export const getAdmSecurityFindMfaStatesQueryOptions = <TData = Awaited<ReturnType<typeof admSecurityFindMfaStates>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindMfaStates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmSecurityFindMfaStatesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admSecurityFindMfaStates>>> = ({ signal }) => admSecurityFindMfaStates({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindMfaStates>>, TError, TData>;
};

export type AdmSecurityFindMfaStatesQueryResult = NonNullable<Awaited<ReturnType<typeof admSecurityFindMfaStates>>>;
export type AdmSecurityFindMfaStatesQueryError = unknown;

export function useAdmSecurityFindMfaStates<TData = Awaited<ReturnType<typeof admSecurityFindMfaStates>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admSecurityFindMfaStates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmSecurityFindMfaStatesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admSecurityFindMfaStates


// CPF PRE-RUNTIME FALLBACK START admSecurityDisableMfa
export type admSecurityDisableMfaResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecurityDisableMfaResponseSuccess = (admSecurityDisableMfaResponse200) & {
  headers: Headers;
};

export type admSecurityDisableMfaResponse = (admSecurityDisableMfaResponseSuccess)

export const getAdmSecurityDisableMfaUrl = (operatorId: string) => `/adm/api/security/mfa/${encodeURIComponent(String(operatorId))}/disable`;

export const admSecurityDisableMfa = async (operatorId: string, params: AdmSecurityDisableMfaParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecurityDisableMfaResponse> => {
  return cpfOrvalRequest<admSecurityDisableMfaResponse>(getAdmSecurityDisableMfaUrl(operatorId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmSecurityDisableMfaMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityDisableMfa>>, TError, {operatorId: string; params: AdmSecurityDisableMfaParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admSecurityDisableMfa>>, TError, {operatorId: string; params: AdmSecurityDisableMfaParams}, TContext> => {
  const mutationKey = ['admSecurityDisableMfa'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admSecurityDisableMfa>>, {operatorId: string; params: AdmSecurityDisableMfaParams}> = (props) => {
    const { operatorId, params } = props;
    return admSecurityDisableMfa(operatorId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmSecurityDisableMfaMutationResult = NonNullable<Awaited<ReturnType<typeof admSecurityDisableMfa>>>;
export type AdmSecurityDisableMfaMutationBody = never;
export type AdmSecurityDisableMfaMutationError = unknown;

export const useAdmSecurityDisableMfa = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityDisableMfa>>, TError, {operatorId: string; params: AdmSecurityDisableMfaParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admSecurityDisableMfa>>, TError, {operatorId: string; params: AdmSecurityDisableMfaParams}, TContext> => {
  return useMutation(getAdmSecurityDisableMfaMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admSecurityDisableMfa


// CPF PRE-RUNTIME FALLBACK START admSecurityRegisterMfa
export type admSecurityRegisterMfaResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecurityRegisterMfaResponseSuccess = (admSecurityRegisterMfaResponse200) & {
  headers: Headers;
};

export type admSecurityRegisterMfaResponse = (admSecurityRegisterMfaResponseSuccess)

export const getAdmSecurityRegisterMfaUrl = (operatorId: string) => `/adm/api/security/mfa/${encodeURIComponent(String(operatorId))}/register`;

export const admSecurityRegisterMfa = async (operatorId: string, data: AdmMfaOtpRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecurityRegisterMfaResponse> => {
  return cpfOrvalRequest<admSecurityRegisterMfaResponse>(getAdmSecurityRegisterMfaUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmSecurityRegisterMfaMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityRegisterMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admSecurityRegisterMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext> => {
  const mutationKey = ['admSecurityRegisterMfa'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admSecurityRegisterMfa>>, {operatorId: string; data: AdmMfaOtpRequest}> = (props) => {
    const { operatorId, data } = props;
    return admSecurityRegisterMfa(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmSecurityRegisterMfaMutationResult = NonNullable<Awaited<ReturnType<typeof admSecurityRegisterMfa>>>;
export type AdmSecurityRegisterMfaMutationBody = AdmMfaOtpRequest;
export type AdmSecurityRegisterMfaMutationError = unknown;

export const useAdmSecurityRegisterMfa = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityRegisterMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admSecurityRegisterMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext> => {
  return useMutation(getAdmSecurityRegisterMfaMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admSecurityRegisterMfa


// CPF PRE-RUNTIME FALLBACK START admSecurityVerifyMfa
export type admSecurityVerifyMfaResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admSecurityVerifyMfaResponseSuccess = (admSecurityVerifyMfaResponse200) & {
  headers: Headers;
};

export type admSecurityVerifyMfaResponse = (admSecurityVerifyMfaResponseSuccess)

export const getAdmSecurityVerifyMfaUrl = (operatorId: string) => `/adm/api/security/mfa/${encodeURIComponent(String(operatorId))}/verify`;

export const admSecurityVerifyMfa = async (operatorId: string, data: AdmMfaOtpRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admSecurityVerifyMfaResponse> => {
  return cpfOrvalRequest<admSecurityVerifyMfaResponse>(getAdmSecurityVerifyMfaUrl(operatorId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmSecurityVerifyMfaMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityVerifyMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admSecurityVerifyMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext> => {
  const mutationKey = ['admSecurityVerifyMfa'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admSecurityVerifyMfa>>, {operatorId: string; data: AdmMfaOtpRequest}> = (props) => {
    const { operatorId, data } = props;
    return admSecurityVerifyMfa(operatorId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmSecurityVerifyMfaMutationResult = NonNullable<Awaited<ReturnType<typeof admSecurityVerifyMfa>>>;
export type AdmSecurityVerifyMfaMutationBody = AdmMfaOtpRequest;
export type AdmSecurityVerifyMfaMutationError = unknown;

export const useAdmSecurityVerifyMfa = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admSecurityVerifyMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admSecurityVerifyMfa>>, TError, {operatorId: string; data: AdmMfaOtpRequest}, TContext> => {
  return useMutation(getAdmSecurityVerifyMfaMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admSecurityVerifyMfa


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindCallHistory
export type admServiceRegistryFindCallHistoryResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindCallHistoryResponseSuccess = (admServiceRegistryFindCallHistoryResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindCallHistoryResponse = (admServiceRegistryFindCallHistoryResponseSuccess)

export const getAdmServiceRegistryFindCallHistoryUrl = () => `/adm/api/service-registry/call-history`;

export const admServiceRegistryFindCallHistory = async (params?: AdmServiceRegistryFindCallHistoryParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindCallHistoryResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindCallHistoryResponse>(getAdmServiceRegistryFindCallHistoryUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, transactionId: params?.transactionId, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindCallHistoryQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindCallHistoryParams>) => ["adm", "api", "service-registry", "call-history", toValue(params)] as const;

export const getAdmServiceRegistryFindCallHistoryQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindCallHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindCallHistoryQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>> = ({ signal }) => admServiceRegistryFindCallHistory(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>, TError, TData>;
};

export type AdmServiceRegistryFindCallHistoryQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>>;
export type AdmServiceRegistryFindCallHistoryQueryError = unknown;

export function useAdmServiceRegistryFindCallHistory<TData = Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindCallHistoryParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCallHistory>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindCallHistoryQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindCallHistory


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryCapabilities
export type admServiceRegistryCapabilitiesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryCapabilitiesResponseSuccess = (admServiceRegistryCapabilitiesResponse200) & {
  headers: Headers;
};

export type admServiceRegistryCapabilitiesResponse = (admServiceRegistryCapabilitiesResponseSuccess)

export const getAdmServiceRegistryCapabilitiesUrl = () => `/adm/api/service-registry/capabilities`;

export const admServiceRegistryCapabilities = async (options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryCapabilitiesResponse> => {
  return cpfOrvalRequest<admServiceRegistryCapabilitiesResponse>(getAdmServiceRegistryCapabilitiesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getAdmServiceRegistryCapabilitiesQueryKey = () => ["adm", "api", "service-registry", "capabilities"] as const;

export const getAdmServiceRegistryCapabilitiesQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryCapabilities>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryCapabilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryCapabilitiesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryCapabilities>>> = ({ signal }) => admServiceRegistryCapabilities({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryCapabilities>>, TError, TData>;
};

export type AdmServiceRegistryCapabilitiesQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryCapabilities>>>;
export type AdmServiceRegistryCapabilitiesQueryError = unknown;

export function useAdmServiceRegistryCapabilities<TData = Awaited<ReturnType<typeof admServiceRegistryCapabilities>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryCapabilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryCapabilitiesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryCapabilities


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindCircuitStates
export type admServiceRegistryFindCircuitStatesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindCircuitStatesResponseSuccess = (admServiceRegistryFindCircuitStatesResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindCircuitStatesResponse = (admServiceRegistryFindCircuitStatesResponseSuccess)

export const getAdmServiceRegistryFindCircuitStatesUrl = () => `/adm/api/service-registry/circuit-states`;

export const admServiceRegistryFindCircuitStates = async (params?: AdmServiceRegistryFindCircuitStatesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindCircuitStatesResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindCircuitStatesResponse>(getAdmServiceRegistryFindCircuitStatesUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, endpointCode: params?.endpointCode, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindCircuitStatesQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindCircuitStatesParams>) => ["adm", "api", "service-registry", "circuit-states", toValue(params)] as const;

export const getAdmServiceRegistryFindCircuitStatesQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindCircuitStatesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindCircuitStatesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>> = ({ signal }) => admServiceRegistryFindCircuitStates(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>, TError, TData>;
};

export type AdmServiceRegistryFindCircuitStatesQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>>;
export type AdmServiceRegistryFindCircuitStatesQueryError = unknown;

export function useAdmServiceRegistryFindCircuitStates<TData = Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindCircuitStatesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindCircuitStates>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindCircuitStatesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindCircuitStates


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindEndpoints
export type admServiceRegistryFindEndpointsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindEndpointsResponseSuccess = (admServiceRegistryFindEndpointsResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindEndpointsResponse = (admServiceRegistryFindEndpointsResponseSuccess)

export const getAdmServiceRegistryFindEndpointsUrl = () => `/adm/api/service-registry/endpoints`;

export const admServiceRegistryFindEndpoints = async (params?: AdmServiceRegistryFindEndpointsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindEndpointsResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindEndpointsResponse>(getAdmServiceRegistryFindEndpointsUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, endpointCode: params?.endpointCode, useYn: params?.useYn, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindEndpointsQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindEndpointsParams>) => ["adm", "api", "service-registry", "endpoints", toValue(params)] as const;

export const getAdmServiceRegistryFindEndpointsQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindEndpointsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindEndpointsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>> = ({ signal }) => admServiceRegistryFindEndpoints(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>, TError, TData>;
};

export type AdmServiceRegistryFindEndpointsQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>>;
export type AdmServiceRegistryFindEndpointsQueryError = unknown;

export function useAdmServiceRegistryFindEndpoints<TData = Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindEndpointsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindEndpoints>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindEndpointsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindEndpoints


// CPF PRE-RUNTIME FALLBACK START admServiceRegistrySaveEndpoint
export type admServiceRegistrySaveEndpointResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistrySaveEndpointResponseSuccess = (admServiceRegistrySaveEndpointResponse200) & {
  headers: Headers;
};

export type admServiceRegistrySaveEndpointResponse = (admServiceRegistrySaveEndpointResponseSuccess)

export const getAdmServiceRegistrySaveEndpointUrl = () => `/adm/api/service-registry/endpoints`;

export const admServiceRegistrySaveEndpoint = async (data: EndpointDefinition, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistrySaveEndpointResponse> => {
  return cpfOrvalRequest<admServiceRegistrySaveEndpointResponse>(getAdmServiceRegistrySaveEndpointUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistrySaveEndpointMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>, TError, {data: EndpointDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>, TError, {data: EndpointDefinition}, TContext> => {
  const mutationKey = ['admServiceRegistrySaveEndpoint'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>, {data: EndpointDefinition}> = (props) => {
    const { data } = props;
    return admServiceRegistrySaveEndpoint(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistrySaveEndpointMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>>;
export type AdmServiceRegistrySaveEndpointMutationBody = EndpointDefinition;
export type AdmServiceRegistrySaveEndpointMutationError = unknown;

export const useAdmServiceRegistrySaveEndpoint = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>, TError, {data: EndpointDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistrySaveEndpoint>>, TError, {data: EndpointDefinition}, TContext> => {
  return useMutation(getAdmServiceRegistrySaveEndpointMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistrySaveEndpoint


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryDeleteEndpoint
export type admServiceRegistryDeleteEndpointResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryDeleteEndpointResponseSuccess = (admServiceRegistryDeleteEndpointResponse200) & {
  headers: Headers;
};

export type admServiceRegistryDeleteEndpointResponse = (admServiceRegistryDeleteEndpointResponseSuccess)

export const getAdmServiceRegistryDeleteEndpointUrl = (endpointCode: string) => `/adm/api/service-registry/endpoints/${encodeURIComponent(String(endpointCode))}`;

export const admServiceRegistryDeleteEndpoint = async (endpointCode: string, data: DeleteCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryDeleteEndpointResponse> => {
  return cpfOrvalRequest<admServiceRegistryDeleteEndpointResponse>(getAdmServiceRegistryDeleteEndpointUrl(endpointCode), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistryDeleteEndpointMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>, TError, {endpointCode: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>, TError, {endpointCode: string; data: DeleteCommand}, TContext> => {
  const mutationKey = ['admServiceRegistryDeleteEndpoint'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>, {endpointCode: string; data: DeleteCommand}> = (props) => {
    const { endpointCode, data } = props;
    return admServiceRegistryDeleteEndpoint(endpointCode, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistryDeleteEndpointMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>>;
export type AdmServiceRegistryDeleteEndpointMutationBody = DeleteCommand;
export type AdmServiceRegistryDeleteEndpointMutationError = unknown;

export const useAdmServiceRegistryDeleteEndpoint = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>, TError, {endpointCode: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistryDeleteEndpoint>>, TError, {endpointCode: string; data: DeleteCommand}, TContext> => {
  return useMutation(getAdmServiceRegistryDeleteEndpointMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryDeleteEndpoint


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindHealth
export type admServiceRegistryFindHealthResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindHealthResponseSuccess = (admServiceRegistryFindHealthResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindHealthResponse = (admServiceRegistryFindHealthResponseSuccess)

export const getAdmServiceRegistryFindHealthUrl = () => `/adm/api/service-registry/health`;

export const admServiceRegistryFindHealth = async (params?: AdmServiceRegistryFindHealthParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindHealthResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindHealthResponse>(getAdmServiceRegistryFindHealthUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, endpointCode: params?.endpointCode, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindHealthQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindHealthParams>) => ["adm", "api", "service-registry", "health", toValue(params)] as const;

export const getAdmServiceRegistryFindHealthQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindHealth>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindHealthParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindHealth>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindHealthQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindHealth>>> = ({ signal }) => admServiceRegistryFindHealth(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindHealth>>, TError, TData>;
};

export type AdmServiceRegistryFindHealthQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindHealth>>>;
export type AdmServiceRegistryFindHealthQueryError = unknown;

export function useAdmServiceRegistryFindHealth<TData = Awaited<ReturnType<typeof admServiceRegistryFindHealth>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindHealthParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindHealth>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindHealthQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindHealth


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindInstances
export type admServiceRegistryFindInstancesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindInstancesResponseSuccess = (admServiceRegistryFindInstancesResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindInstancesResponse = (admServiceRegistryFindInstancesResponseSuccess)

export const getAdmServiceRegistryFindInstancesUrl = () => `/adm/api/service-registry/instances`;

export const admServiceRegistryFindInstances = async (params?: AdmServiceRegistryFindInstancesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindInstancesResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindInstancesResponse>(getAdmServiceRegistryFindInstancesUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, endpointCode: params?.endpointCode, status: params?.status, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindInstancesQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindInstancesParams>) => ["adm", "api", "service-registry", "instances", toValue(params)] as const;

export const getAdmServiceRegistryFindInstancesQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindInstances>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindInstancesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindInstancesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindInstances>>> = ({ signal }) => admServiceRegistryFindInstances(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindInstances>>, TError, TData>;
};

export type AdmServiceRegistryFindInstancesQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindInstances>>>;
export type AdmServiceRegistryFindInstancesQueryError = unknown;

export function useAdmServiceRegistryFindInstances<TData = Awaited<ReturnType<typeof admServiceRegistryFindInstances>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindInstancesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindInstances>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindInstancesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindInstances


// CPF PRE-RUNTIME FALLBACK START admServiceRegistrySaveInstance
export type admServiceRegistrySaveInstanceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistrySaveInstanceResponseSuccess = (admServiceRegistrySaveInstanceResponse200) & {
  headers: Headers;
};

export type admServiceRegistrySaveInstanceResponse = (admServiceRegistrySaveInstanceResponseSuccess)

export const getAdmServiceRegistrySaveInstanceUrl = () => `/adm/api/service-registry/instances`;

export const admServiceRegistrySaveInstance = async (data: InstanceDefinition, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistrySaveInstanceResponse> => {
  return cpfOrvalRequest<admServiceRegistrySaveInstanceResponse>(getAdmServiceRegistrySaveInstanceUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistrySaveInstanceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>, TError, {data: InstanceDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>, TError, {data: InstanceDefinition}, TContext> => {
  const mutationKey = ['admServiceRegistrySaveInstance'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>, {data: InstanceDefinition}> = (props) => {
    const { data } = props;
    return admServiceRegistrySaveInstance(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistrySaveInstanceMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>>;
export type AdmServiceRegistrySaveInstanceMutationBody = InstanceDefinition;
export type AdmServiceRegistrySaveInstanceMutationError = unknown;

export const useAdmServiceRegistrySaveInstance = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>, TError, {data: InstanceDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistrySaveInstance>>, TError, {data: InstanceDefinition}, TContext> => {
  return useMutation(getAdmServiceRegistrySaveInstanceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistrySaveInstance


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryDeleteInstance
export type admServiceRegistryDeleteInstanceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryDeleteInstanceResponseSuccess = (admServiceRegistryDeleteInstanceResponse200) & {
  headers: Headers;
};

export type admServiceRegistryDeleteInstanceResponse = (admServiceRegistryDeleteInstanceResponseSuccess)

export const getAdmServiceRegistryDeleteInstanceUrl = (instanceId: string) => `/adm/api/service-registry/instances/${encodeURIComponent(String(instanceId))}`;

export const admServiceRegistryDeleteInstance = async (instanceId: string, data: DeleteCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryDeleteInstanceResponse> => {
  return cpfOrvalRequest<admServiceRegistryDeleteInstanceResponse>(getAdmServiceRegistryDeleteInstanceUrl(instanceId), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistryDeleteInstanceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>, TError, {instanceId: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>, TError, {instanceId: string; data: DeleteCommand}, TContext> => {
  const mutationKey = ['admServiceRegistryDeleteInstance'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>, {instanceId: string; data: DeleteCommand}> = (props) => {
    const { instanceId, data } = props;
    return admServiceRegistryDeleteInstance(instanceId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistryDeleteInstanceMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>>;
export type AdmServiceRegistryDeleteInstanceMutationBody = DeleteCommand;
export type AdmServiceRegistryDeleteInstanceMutationError = unknown;

export const useAdmServiceRegistryDeleteInstance = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>, TError, {instanceId: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistryDeleteInstance>>, TError, {instanceId: string; data: DeleteCommand}, TContext> => {
  return useMutation(getAdmServiceRegistryDeleteInstanceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryDeleteInstance


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindRoutingPolicies
export type admServiceRegistryFindRoutingPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindRoutingPoliciesResponseSuccess = (admServiceRegistryFindRoutingPoliciesResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindRoutingPoliciesResponse = (admServiceRegistryFindRoutingPoliciesResponseSuccess)

export const getAdmServiceRegistryFindRoutingPoliciesUrl = () => `/adm/api/service-registry/routing-policies`;

export const admServiceRegistryFindRoutingPolicies = async (params?: AdmServiceRegistryFindRoutingPoliciesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindRoutingPoliciesResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindRoutingPoliciesResponse>(getAdmServiceRegistryFindRoutingPoliciesUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, endpointCode: params?.endpointCode, activeYn: params?.activeYn, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindRoutingPoliciesQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindRoutingPoliciesParams>) => ["adm", "api", "service-registry", "routing-policies", toValue(params)] as const;

export const getAdmServiceRegistryFindRoutingPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindRoutingPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindRoutingPoliciesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>> = ({ signal }) => admServiceRegistryFindRoutingPolicies(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>, TError, TData>;
};

export type AdmServiceRegistryFindRoutingPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>>;
export type AdmServiceRegistryFindRoutingPoliciesQueryError = unknown;

export function useAdmServiceRegistryFindRoutingPolicies<TData = Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindRoutingPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindRoutingPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindRoutingPoliciesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindRoutingPolicies


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryFindServices
export type admServiceRegistryFindServicesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryFindServicesResponseSuccess = (admServiceRegistryFindServicesResponse200) & {
  headers: Headers;
};

export type admServiceRegistryFindServicesResponse = (admServiceRegistryFindServicesResponseSuccess)

export const getAdmServiceRegistryFindServicesUrl = () => `/adm/api/service-registry/services`;

export const admServiceRegistryFindServices = async (params?: AdmServiceRegistryFindServicesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryFindServicesResponse> => {
  return cpfOrvalRequest<admServiceRegistryFindServicesResponse>(getAdmServiceRegistryFindServicesUrl(), {
    ...options,
    method: 'GET',
    params: { serviceId: params?.serviceId, useYn: params?.useYn, limit: params?.limit },
  });
};

export const getAdmServiceRegistryFindServicesQueryKey = (params?: MaybeRefOrGetter<AdmServiceRegistryFindServicesParams>) => ["adm", "api", "service-registry", "services", toValue(params)] as const;

export const getAdmServiceRegistryFindServicesQueryOptions = <TData = Awaited<ReturnType<typeof admServiceRegistryFindServices>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindServicesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindServices>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmServiceRegistryFindServicesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admServiceRegistryFindServices>>> = ({ signal }) => admServiceRegistryFindServices(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindServices>>, TError, TData>;
};

export type AdmServiceRegistryFindServicesQueryResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryFindServices>>>;
export type AdmServiceRegistryFindServicesQueryError = unknown;

export function useAdmServiceRegistryFindServices<TData = Awaited<ReturnType<typeof admServiceRegistryFindServices>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmServiceRegistryFindServicesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admServiceRegistryFindServices>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmServiceRegistryFindServicesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryFindServices


// CPF PRE-RUNTIME FALLBACK START admServiceRegistrySaveService
export type admServiceRegistrySaveServiceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistrySaveServiceResponseSuccess = (admServiceRegistrySaveServiceResponse200) & {
  headers: Headers;
};

export type admServiceRegistrySaveServiceResponse = (admServiceRegistrySaveServiceResponseSuccess)

export const getAdmServiceRegistrySaveServiceUrl = () => `/adm/api/service-registry/services`;

export const admServiceRegistrySaveService = async (data: ServiceDefinition, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistrySaveServiceResponse> => {
  return cpfOrvalRequest<admServiceRegistrySaveServiceResponse>(getAdmServiceRegistrySaveServiceUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistrySaveServiceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveService>>, TError, {data: ServiceDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveService>>, TError, {data: ServiceDefinition}, TContext> => {
  const mutationKey = ['admServiceRegistrySaveService'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistrySaveService>>, {data: ServiceDefinition}> = (props) => {
    const { data } = props;
    return admServiceRegistrySaveService(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistrySaveServiceMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistrySaveService>>>;
export type AdmServiceRegistrySaveServiceMutationBody = ServiceDefinition;
export type AdmServiceRegistrySaveServiceMutationError = unknown;

export const useAdmServiceRegistrySaveService = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistrySaveService>>, TError, {data: ServiceDefinition}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistrySaveService>>, TError, {data: ServiceDefinition}, TContext> => {
  return useMutation(getAdmServiceRegistrySaveServiceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistrySaveService


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryDeleteService
export type admServiceRegistryDeleteServiceResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryDeleteServiceResponseSuccess = (admServiceRegistryDeleteServiceResponse200) & {
  headers: Headers;
};

export type admServiceRegistryDeleteServiceResponse = (admServiceRegistryDeleteServiceResponseSuccess)

export const getAdmServiceRegistryDeleteServiceUrl = (serviceId: string) => `/adm/api/service-registry/services/${encodeURIComponent(String(serviceId))}`;

export const admServiceRegistryDeleteService = async (serviceId: string, data: DeleteCommand, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryDeleteServiceResponse> => {
  return cpfOrvalRequest<admServiceRegistryDeleteServiceResponse>(getAdmServiceRegistryDeleteServiceUrl(serviceId), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistryDeleteServiceMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>, TError, {serviceId: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>, TError, {serviceId: string; data: DeleteCommand}, TContext> => {
  const mutationKey = ['admServiceRegistryDeleteService'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>, {serviceId: string; data: DeleteCommand}> = (props) => {
    const { serviceId, data } = props;
    return admServiceRegistryDeleteService(serviceId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistryDeleteServiceMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>>;
export type AdmServiceRegistryDeleteServiceMutationBody = DeleteCommand;
export type AdmServiceRegistryDeleteServiceMutationError = unknown;

export const useAdmServiceRegistryDeleteService = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>, TError, {serviceId: string; data: DeleteCommand}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistryDeleteService>>, TError, {serviceId: string; data: DeleteCommand}, TContext> => {
  return useMutation(getAdmServiceRegistryDeleteServiceMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryDeleteService


// CPF PRE-RUNTIME FALLBACK START admServiceRegistryChangeInstanceState
export type admServiceRegistryChangeInstanceStateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admServiceRegistryChangeInstanceStateResponseSuccess = (admServiceRegistryChangeInstanceStateResponse200) & {
  headers: Headers;
};

export type admServiceRegistryChangeInstanceStateResponse = (admServiceRegistryChangeInstanceStateResponseSuccess)

export const getAdmServiceRegistryChangeInstanceStateUrl = (serviceId: string, endpointCode: string, instanceId: string) => `/adm/api/service-registry/services/${encodeURIComponent(String(serviceId))}/endpoints/${encodeURIComponent(String(endpointCode))}/instances/${encodeURIComponent(String(instanceId))}/state`;

export const admServiceRegistryChangeInstanceState = async (serviceId: string, endpointCode: string, instanceId: string, data: AdmServiceRegistryChangeInstanceStateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admServiceRegistryChangeInstanceStateResponse> => {
  return cpfOrvalRequest<admServiceRegistryChangeInstanceStateResponse>(getAdmServiceRegistryChangeInstanceStateUrl(serviceId, endpointCode, instanceId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmServiceRegistryChangeInstanceStateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>, TError, {serviceId: string; endpointCode: string; instanceId: string; data: AdmServiceRegistryChangeInstanceStateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>, TError, {serviceId: string; endpointCode: string; instanceId: string; data: AdmServiceRegistryChangeInstanceStateRequest}, TContext> => {
  const mutationKey = ['admServiceRegistryChangeInstanceState'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>, {serviceId: string; endpointCode: string; instanceId: string; data: AdmServiceRegistryChangeInstanceStateRequest}> = (props) => {
    const { serviceId, endpointCode, instanceId, data } = props;
    return admServiceRegistryChangeInstanceState(serviceId, endpointCode, instanceId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmServiceRegistryChangeInstanceStateMutationResult = NonNullable<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>>;
export type AdmServiceRegistryChangeInstanceStateMutationBody = AdmServiceRegistryChangeInstanceStateRequest;
export type AdmServiceRegistryChangeInstanceStateMutationError = unknown;

export const useAdmServiceRegistryChangeInstanceState = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>, TError, {serviceId: string; endpointCode: string; instanceId: string; data: AdmServiceRegistryChangeInstanceStateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admServiceRegistryChangeInstanceState>>, TError, {serviceId: string; endpointCode: string; instanceId: string; data: AdmServiceRegistryChangeInstanceStateRequest}, TContext> => {
  return useMutation(getAdmServiceRegistryChangeInstanceStateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admServiceRegistryChangeInstanceState


// CPF PRE-RUNTIME FALLBACK START admStandardExecutionFindAll
export type admStandardExecutionFindAllResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admStandardExecutionFindAllResponseSuccess = (admStandardExecutionFindAllResponse200) & {
  headers: Headers;
};

export type admStandardExecutionFindAllResponse = (admStandardExecutionFindAllResponseSuccess)

export const getAdmStandardExecutionFindAllUrl = () => `/adm/api/standard-executions`;

export const admStandardExecutionFindAll = async (params?: AdmStandardExecutionFindAllParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admStandardExecutionFindAllResponse> => {
  return cpfOrvalRequest<admStandardExecutionFindAllResponse>(getAdmStandardExecutionFindAllUrl(), {
    ...options,
    method: 'GET',
    params: { type: params?.type, ownerDomain: params?.ownerDomain, keyword: params?.keyword },
  });
};

export const getAdmStandardExecutionFindAllQueryKey = (params?: MaybeRefOrGetter<AdmStandardExecutionFindAllParams>) => ["adm", "api", "standard-executions", toValue(params)] as const;

export const getAdmStandardExecutionFindAllQueryOptions = <TData = Awaited<ReturnType<typeof admStandardExecutionFindAll>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmStandardExecutionFindAllParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindAll>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmStandardExecutionFindAllQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admStandardExecutionFindAll>>> = ({ signal }) => admStandardExecutionFindAll(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindAll>>, TError, TData>;
};

export type AdmStandardExecutionFindAllQueryResult = NonNullable<Awaited<ReturnType<typeof admStandardExecutionFindAll>>>;
export type AdmStandardExecutionFindAllQueryError = unknown;

export function useAdmStandardExecutionFindAll<TData = Awaited<ReturnType<typeof admStandardExecutionFindAll>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmStandardExecutionFindAllParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindAll>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmStandardExecutionFindAllQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admStandardExecutionFindAll


// CPF PRE-RUNTIME FALLBACK START admStandardExecutionFindOne
export type admStandardExecutionFindOneResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admStandardExecutionFindOneResponseSuccess = (admStandardExecutionFindOneResponse200) & {
  headers: Headers;
};

export type admStandardExecutionFindOneResponse = (admStandardExecutionFindOneResponseSuccess)

export const getAdmStandardExecutionFindOneUrl = (standardExecutionId: string) => `/adm/api/standard-executions/${encodeURIComponent(String(standardExecutionId))}`;

export const admStandardExecutionFindOne = async (standardExecutionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admStandardExecutionFindOneResponse> => {
  return cpfOrvalRequest<admStandardExecutionFindOneResponse>(getAdmStandardExecutionFindOneUrl(standardExecutionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmStandardExecutionFindOneQueryKey = (standardExecutionId: MaybeRefOrGetter<string>) => ["adm", "api", "standard-executions", standardExecutionId] as const;

export const getAdmStandardExecutionFindOneQueryOptions = <TData = Awaited<ReturnType<typeof admStandardExecutionFindOne>>, TError = unknown>(
  standardExecutionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindOne>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmStandardExecutionFindOneQueryKey(toValue(standardExecutionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admStandardExecutionFindOne>>> = ({ signal }) => admStandardExecutionFindOne(toValue(standardExecutionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(standardExecutionId) !== null && toValue(standardExecutionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindOne>>, TError, TData>;
};

export type AdmStandardExecutionFindOneQueryResult = NonNullable<Awaited<ReturnType<typeof admStandardExecutionFindOne>>>;
export type AdmStandardExecutionFindOneQueryError = unknown;

export function useAdmStandardExecutionFindOne<TData = Awaited<ReturnType<typeof admStandardExecutionFindOne>>, TError = unknown>(
  standardExecutionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admStandardExecutionFindOne>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmStandardExecutionFindOneQueryOptions(standardExecutionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admStandardExecutionFindOne


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindGroups
export type admTransactionGroupFindGroupsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindGroupsResponseSuccess = (admTransactionGroupFindGroupsResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindGroupsResponse = (admTransactionGroupFindGroupsResponseSuccess)

export const getAdmTransactionGroupFindGroupsUrl = () => `/adm/api/transaction-groups`;

export const admTransactionGroupFindGroups = async (params: AdmTransactionGroupFindGroupsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindGroupsResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindGroupsResponse>(getAdmTransactionGroupFindGroupsUrl(), {
    ...options,
    method: 'GET',
    params: { criteria: params.criteria },
  });
};

export const getAdmTransactionGroupFindGroupsQueryKey = (params: MaybeRefOrGetter<AdmTransactionGroupFindGroupsParams>) => ["adm", "api", "transaction-groups", toValue(params)] as const;

export const getAdmTransactionGroupFindGroupsQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindGroups>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmTransactionGroupFindGroupsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindGroups>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindGroupsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindGroups>>> = ({ signal }) => admTransactionGroupFindGroups(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindGroups>>, TError, TData>;
};

export type AdmTransactionGroupFindGroupsQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindGroups>>>;
export type AdmTransactionGroupFindGroupsQueryError = unknown;

export function useAdmTransactionGroupFindGroups<TData = Awaited<ReturnType<typeof admTransactionGroupFindGroups>>, TError = unknown>(
  params: MaybeRefOrGetter<AdmTransactionGroupFindGroupsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindGroups>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindGroupsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindGroups


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindDetail
export type admTransactionGroupFindDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindDetailResponseSuccess = (admTransactionGroupFindDetailResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindDetailResponse = (admTransactionGroupFindDetailResponseSuccess)

export const getAdmTransactionGroupFindDetailUrl = (transactionId: string) => `/adm/api/transaction-groups/${encodeURIComponent(String(transactionId))}`;

export const admTransactionGroupFindDetail = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindDetailResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindDetailResponse>(getAdmTransactionGroupFindDetailUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionGroupFindDetailQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transaction-groups", transactionId] as const;

export const getAdmTransactionGroupFindDetailQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindDetail>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindDetailQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindDetail>>> = ({ signal }) => admTransactionGroupFindDetail(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindDetail>>, TError, TData>;
};

export type AdmTransactionGroupFindDetailQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindDetail>>>;
export type AdmTransactionGroupFindDetailQueryError = unknown;

export function useAdmTransactionGroupFindDetail<TData = Awaited<ReturnType<typeof admTransactionGroupFindDetail>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindDetailQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindDetail


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindExternalLogs
export type admTransactionGroupFindExternalLogsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindExternalLogsResponseSuccess = (admTransactionGroupFindExternalLogsResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindExternalLogsResponse = (admTransactionGroupFindExternalLogsResponseSuccess)

export const getAdmTransactionGroupFindExternalLogsUrl = (transactionId: string) => `/adm/api/transaction-groups/${encodeURIComponent(String(transactionId))}/external-logs`;

export const admTransactionGroupFindExternalLogs = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindExternalLogsResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindExternalLogsResponse>(getAdmTransactionGroupFindExternalLogsUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionGroupFindExternalLogsQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transaction-groups", transactionId, "external-logs"] as const;

export const getAdmTransactionGroupFindExternalLogsQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindExternalLogsQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>> = ({ signal }) => admTransactionGroupFindExternalLogs(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>, TError, TData>;
};

export type AdmTransactionGroupFindExternalLogsQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>>;
export type AdmTransactionGroupFindExternalLogsQueryError = unknown;

export function useAdmTransactionGroupFindExternalLogs<TData = Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindExternalLogs>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindExternalLogsQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindExternalLogs


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindHeaders
export type admTransactionGroupFindHeadersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindHeadersResponseSuccess = (admTransactionGroupFindHeadersResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindHeadersResponse = (admTransactionGroupFindHeadersResponseSuccess)

export const getAdmTransactionGroupFindHeadersUrl = (transactionId: string) => `/adm/api/transaction-groups/${encodeURIComponent(String(transactionId))}/headers`;

export const admTransactionGroupFindHeaders = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindHeadersResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindHeadersResponse>(getAdmTransactionGroupFindHeadersUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionGroupFindHeadersQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transaction-groups", transactionId, "headers"] as const;

export const getAdmTransactionGroupFindHeadersQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindHeadersQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>> = ({ signal }) => admTransactionGroupFindHeaders(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>, TError, TData>;
};

export type AdmTransactionGroupFindHeadersQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>>;
export type AdmTransactionGroupFindHeadersQueryError = unknown;

export function useAdmTransactionGroupFindHeaders<TData = Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindHeaders>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindHeadersQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindHeaders


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindSegments
export type admTransactionGroupFindSegmentsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindSegmentsResponseSuccess = (admTransactionGroupFindSegmentsResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindSegmentsResponse = (admTransactionGroupFindSegmentsResponseSuccess)

export const getAdmTransactionGroupFindSegmentsUrl = (transactionId: string) => `/adm/api/transaction-groups/${encodeURIComponent(String(transactionId))}/segments`;

export const admTransactionGroupFindSegments = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindSegmentsResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindSegmentsResponse>(getAdmTransactionGroupFindSegmentsUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionGroupFindSegmentsQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transaction-groups", transactionId, "segments"] as const;

export const getAdmTransactionGroupFindSegmentsQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindSegments>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindSegments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindSegmentsQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindSegments>>> = ({ signal }) => admTransactionGroupFindSegments(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindSegments>>, TError, TData>;
};

export type AdmTransactionGroupFindSegmentsQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindSegments>>>;
export type AdmTransactionGroupFindSegmentsQueryError = unknown;

export function useAdmTransactionGroupFindSegments<TData = Awaited<ReturnType<typeof admTransactionGroupFindSegments>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindSegments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindSegmentsQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindSegments


// CPF PRE-RUNTIME FALLBACK START admTransactionGroupFindTimeline
export type admTransactionGroupFindTimelineResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionGroupFindTimelineResponseSuccess = (admTransactionGroupFindTimelineResponse200) & {
  headers: Headers;
};

export type admTransactionGroupFindTimelineResponse = (admTransactionGroupFindTimelineResponseSuccess)

export const getAdmTransactionGroupFindTimelineUrl = (transactionId: string) => `/adm/api/transaction-groups/${encodeURIComponent(String(transactionId))}/timeline`;

export const admTransactionGroupFindTimeline = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionGroupFindTimelineResponse> => {
  return cpfOrvalRequest<admTransactionGroupFindTimelineResponse>(getAdmTransactionGroupFindTimelineUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionGroupFindTimelineQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transaction-groups", transactionId, "timeline"] as const;

export const getAdmTransactionGroupFindTimelineQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionGroupFindTimelineQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>> = ({ signal }) => admTransactionGroupFindTimeline(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>, TError, TData>;
};

export type AdmTransactionGroupFindTimelineQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>>;
export type AdmTransactionGroupFindTimelineQueryError = unknown;

export function useAdmTransactionGroupFindTimeline<TData = Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionGroupFindTimeline>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionGroupFindTimelineQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionGroupFindTimeline


// CPF PRE-RUNTIME FALLBACK START admTransactionMetaFindTransactions
export type admTransactionMetaFindTransactionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionMetaFindTransactionsResponseSuccess = (admTransactionMetaFindTransactionsResponse200) & {
  headers: Headers;
};

export type admTransactionMetaFindTransactionsResponse = (admTransactionMetaFindTransactionsResponseSuccess)

export const getAdmTransactionMetaFindTransactionsUrl = () => `/adm/api/transactions`;

export const admTransactionMetaFindTransactions = async (params?: AdmTransactionMetaFindTransactionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionMetaFindTransactionsResponse> => {
  return cpfOrvalRequest<admTransactionMetaFindTransactionsResponse>(getAdmTransactionMetaFindTransactionsUrl(), {
    ...options,
    method: 'GET',
    params: { moduleCode: params?.moduleCode, activeYn: params?.activeYn, transactionId: params?.transactionId, limit: params?.limit },
  });
};

export const getAdmTransactionMetaFindTransactionsQueryKey = (params?: MaybeRefOrGetter<AdmTransactionMetaFindTransactionsParams>) => ["adm", "api", "transactions", toValue(params)] as const;

export const getAdmTransactionMetaFindTransactionsQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmTransactionMetaFindTransactionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionMetaFindTransactionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>> = ({ signal }) => admTransactionMetaFindTransactions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>, TError, TData>;
};

export type AdmTransactionMetaFindTransactionsQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>>;
export type AdmTransactionMetaFindTransactionsQueryError = unknown;

export function useAdmTransactionMetaFindTransactions<TData = Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmTransactionMetaFindTransactionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransactions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionMetaFindTransactionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionMetaFindTransactions


// CPF PRE-RUNTIME FALLBACK START admTransactionMetaFindPage
export type admTransactionMetaFindPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionMetaFindPageResponseSuccess = (admTransactionMetaFindPageResponse200) & {
  headers: Headers;
};

export type admTransactionMetaFindPageResponse = (admTransactionMetaFindPageResponseSuccess)

export const getAdmTransactionMetaFindPageUrl = () => `/adm/api/transactions/page`;

export const admTransactionMetaFindPage = async (params?: AdmTransactionMetaFindPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionMetaFindPageResponse> => {
  return cpfOrvalRequest<admTransactionMetaFindPageResponse>(getAdmTransactionMetaFindPageUrl(), {
    ...options,
    method: 'GET',
    params: { moduleCode: params?.moduleCode, activeYn: params?.activeYn, transactionId: params?.transactionId, page: params?.page, size: params?.size },
  });
};

export const getAdmTransactionMetaFindPageQueryKey = (params?: MaybeRefOrGetter<AdmTransactionMetaFindPageParams>) => ["adm", "api", "transactions", "page", toValue(params)] as const;

export const getAdmTransactionMetaFindPageQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionMetaFindPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmTransactionMetaFindPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionMetaFindPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionMetaFindPage>>> = ({ signal }) => admTransactionMetaFindPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindPage>>, TError, TData>;
};

export type AdmTransactionMetaFindPageQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionMetaFindPage>>>;
export type AdmTransactionMetaFindPageQueryError = unknown;

export function useAdmTransactionMetaFindPage<TData = Awaited<ReturnType<typeof admTransactionMetaFindPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<AdmTransactionMetaFindPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionMetaFindPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionMetaFindPage


// CPF PRE-RUNTIME FALLBACK START admTransactionMetaScan
export type admTransactionMetaScanResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionMetaScanResponseSuccess = (admTransactionMetaScanResponse200) & {
  headers: Headers;
};

export type admTransactionMetaScanResponse = (admTransactionMetaScanResponseSuccess)

export const getAdmTransactionMetaScanUrl = () => `/adm/api/transactions/scan`;

export const admTransactionMetaScan = async (params: AdmTransactionMetaScanParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionMetaScanResponse> => {
  return cpfOrvalRequest<admTransactionMetaScanResponse>(getAdmTransactionMetaScanUrl(), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmTransactionMetaScanMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaScan>>, TError, {params: AdmTransactionMetaScanParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaScan>>, TError, {params: AdmTransactionMetaScanParams}, TContext> => {
  const mutationKey = ['admTransactionMetaScan'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admTransactionMetaScan>>, {params: AdmTransactionMetaScanParams}> = (props) => {
    const { params } = props;
    return admTransactionMetaScan(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmTransactionMetaScanMutationResult = NonNullable<Awaited<ReturnType<typeof admTransactionMetaScan>>>;
export type AdmTransactionMetaScanMutationBody = never;
export type AdmTransactionMetaScanMutationError = unknown;

export const useAdmTransactionMetaScan = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaScan>>, TError, {params: AdmTransactionMetaScanParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admTransactionMetaScan>>, TError, {params: AdmTransactionMetaScanParams}, TContext> => {
  return useMutation(getAdmTransactionMetaScanMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admTransactionMetaScan


// CPF PRE-RUNTIME FALLBACK START admTransactionMetaFindTransaction
export type admTransactionMetaFindTransactionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionMetaFindTransactionResponseSuccess = (admTransactionMetaFindTransactionResponse200) & {
  headers: Headers;
};

export type admTransactionMetaFindTransactionResponse = (admTransactionMetaFindTransactionResponseSuccess)

export const getAdmTransactionMetaFindTransactionUrl = (transactionId: string) => `/adm/api/transactions/${encodeURIComponent(String(transactionId))}`;

export const admTransactionMetaFindTransaction = async (transactionId: string, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionMetaFindTransactionResponse> => {
  return cpfOrvalRequest<admTransactionMetaFindTransactionResponse>(getAdmTransactionMetaFindTransactionUrl(transactionId), {
    ...options,
    method: 'GET',

  });
};

export const getAdmTransactionMetaFindTransactionQueryKey = (transactionId: MaybeRefOrGetter<string>) => ["adm", "api", "transactions", transactionId] as const;

export const getAdmTransactionMetaFindTransactionQueryOptions = <TData = Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getAdmTransactionMetaFindTransactionQueryKey(toValue(transactionId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>> = ({ signal }) => admTransactionMetaFindTransaction(toValue(transactionId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(transactionId) !== null && toValue(transactionId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>, TError, TData>;
};

export type AdmTransactionMetaFindTransactionQueryResult = NonNullable<Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>>;
export type AdmTransactionMetaFindTransactionQueryError = unknown;

export function useAdmTransactionMetaFindTransaction<TData = Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>, TError = unknown>(
  transactionId: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof admTransactionMetaFindTransaction>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getAdmTransactionMetaFindTransactionQueryOptions(transactionId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END admTransactionMetaFindTransaction


// CPF PRE-RUNTIME FALLBACK START admTransactionMetaInactivate
export type admTransactionMetaInactivateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admTransactionMetaInactivateResponseSuccess = (admTransactionMetaInactivateResponse200) & {
  headers: Headers;
};

export type admTransactionMetaInactivateResponse = (admTransactionMetaInactivateResponseSuccess)

export const getAdmTransactionMetaInactivateUrl = (transactionId: string) => `/adm/api/transactions/${encodeURIComponent(String(transactionId))}/inactive`;

export const admTransactionMetaInactivate = async (transactionId: string, params: AdmTransactionMetaInactivateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<admTransactionMetaInactivateResponse> => {
  return cpfOrvalRequest<admTransactionMetaInactivateResponse>(getAdmTransactionMetaInactivateUrl(transactionId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getAdmTransactionMetaInactivateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaInactivate>>, TError, {transactionId: string; params: AdmTransactionMetaInactivateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaInactivate>>, TError, {transactionId: string; params: AdmTransactionMetaInactivateParams}, TContext> => {
  const mutationKey = ['admTransactionMetaInactivate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admTransactionMetaInactivate>>, {transactionId: string; params: AdmTransactionMetaInactivateParams}> = (props) => {
    const { transactionId, params } = props;
    return admTransactionMetaInactivate(transactionId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmTransactionMetaInactivateMutationResult = NonNullable<Awaited<ReturnType<typeof admTransactionMetaInactivate>>>;
export type AdmTransactionMetaInactivateMutationBody = never;
export type AdmTransactionMetaInactivateMutationError = unknown;

export const useAdmTransactionMetaInactivate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admTransactionMetaInactivate>>, TError, {transactionId: string; params: AdmTransactionMetaInactivateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admTransactionMetaInactivate>>, TError, {transactionId: string; params: AdmTransactionMetaInactivateParams}, TContext> => {
  return useMutation(getAdmTransactionMetaInactivateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admTransactionMetaInactivate


// CPF PRE-RUNTIME FALLBACK START getAdmSystemVersion
export type getAdmSystemVersionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type getAdmSystemVersionResponseSuccess = (getAdmSystemVersionResponse200) & {
  headers: Headers;
};

export type getAdmSystemVersionResponse = (getAdmSystemVersionResponseSuccess)

export const getGetAdmSystemVersionUrl = () => `/adm/api/v1/system/version`;

export const getAdmSystemVersion = async (options?: CpfOrvalGeneratedRequestOptions): Promise<getAdmSystemVersionResponse> => {
  return cpfOrvalRequest<getAdmSystemVersionResponse>(getGetAdmSystemVersionUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getGetAdmSystemVersionQueryKey = () => ["adm", "api", "v1", "system", "version"] as const;

export const getGetAdmSystemVersionQueryOptions = <TData = Awaited<ReturnType<typeof getAdmSystemVersion>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmSystemVersion>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getGetAdmSystemVersionQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof getAdmSystemVersion>>> = ({ signal }) => getAdmSystemVersion({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof getAdmSystemVersion>>, TError, TData>;
};

export type GetAdmSystemVersionQueryResult = NonNullable<Awaited<ReturnType<typeof getAdmSystemVersion>>>;
export type GetAdmSystemVersionQueryError = unknown;

export function useGetAdmSystemVersion<TData = Awaited<ReturnType<typeof getAdmSystemVersion>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getAdmSystemVersion>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetAdmSystemVersionQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END getAdmSystemVersion


// CPF PRE-RUNTIME FALLBACK START admCenterCutReprocessFailedExecution
export type admCenterCutReprocessFailedExecutionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutReprocessFailedExecutionResponseSuccess = (admCenterCutReprocessFailedExecutionResponse200) & {
  headers: Headers;
};

export type admCenterCutReprocessFailedExecutionResponse = (admCenterCutReprocessFailedExecutionResponseSuccess)

export const getAdmCenterCutReprocessFailedExecutionUrl = (executionId: string) => `/adm/api/center-cut/executions/${encodeURIComponent(String(executionId))}/reprocess-failed`;

export const admCenterCutReprocessFailedExecution = async (executionId: string, data: AdmCenterCutActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutReprocessFailedExecutionResponse> => {
  return cpfOrvalRequest<admCenterCutReprocessFailedExecutionResponse>(getAdmCenterCutReprocessFailedExecutionUrl(executionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCenterCutReprocessFailedExecutionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext> => {
  const mutationKey = ['admCenterCutReprocessFailedExecution'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>, {executionId: string; data: AdmCenterCutActionRequest}> = (props) => {
    const { executionId, data } = props;
    return admCenterCutReprocessFailedExecution(executionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCenterCutReprocessFailedExecutionMutationResult = NonNullable<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>>;
export type AdmCenterCutReprocessFailedExecutionMutationBody = AdmCenterCutActionRequest;
export type AdmCenterCutReprocessFailedExecutionMutationError = unknown;

export const useAdmCenterCutReprocessFailedExecution = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCenterCutReprocessFailedExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext> => {
  return useMutation(getAdmCenterCutReprocessFailedExecutionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCenterCutReprocessFailedExecution


// CPF PRE-RUNTIME FALLBACK START admCenterCutReconcileUnknownExecution
export type admCenterCutReconcileUnknownExecutionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type admCenterCutReconcileUnknownExecutionResponseSuccess = (admCenterCutReconcileUnknownExecutionResponse200) & {
  headers: Headers;
};

export type admCenterCutReconcileUnknownExecutionResponse = (admCenterCutReconcileUnknownExecutionResponseSuccess)

export const getAdmCenterCutReconcileUnknownExecutionUrl = (executionId: string) => `/adm/api/center-cut/executions/${encodeURIComponent(String(executionId))}/reconcile-unknown`;

export const admCenterCutReconcileUnknownExecution = async (executionId: string, data: AdmCenterCutActionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<admCenterCutReconcileUnknownExecutionResponse> => {
  return cpfOrvalRequest<admCenterCutReconcileUnknownExecutionResponse>(getAdmCenterCutReconcileUnknownExecutionUrl(executionId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getAdmCenterCutReconcileUnknownExecutionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext> => {
  const mutationKey = ['admCenterCutReconcileUnknownExecution'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>, {executionId: string; data: AdmCenterCutActionRequest}> = (props) => {
    const { executionId, data } = props;
    return admCenterCutReconcileUnknownExecution(executionId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type AdmCenterCutReconcileUnknownExecutionMutationResult = NonNullable<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>>;
export type AdmCenterCutReconcileUnknownExecutionMutationBody = AdmCenterCutActionRequest;
export type AdmCenterCutReconcileUnknownExecutionMutationError = unknown;

export const useAdmCenterCutReconcileUnknownExecution = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof admCenterCutReconcileUnknownExecution>>, TError, {executionId: string; data: AdmCenterCutActionRequest}, TContext> => {
  return useMutation(getAdmCenterCutReconcileUnknownExecutionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END admCenterCutReconcileUnknownExecution

// CPF PRE-RUNTIME FALLBACK START REV-004
export const admApprovalReconcile = async (
  id: number,
  params: AdmApprovalReconcileParams,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/approvals/requests/${encodeURIComponent(String(id))}/reconcile`,
  { ...options, method: 'POST', params: { reason: params.reason } },
);

export const admIntegrationCryptoStatus = async (
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  '/adm/api/integration-closure/crypto/status',
  { ...options, method: 'GET' },
);

export const admIntegrationTimeHealth = async (
  params?: AdmIntegrationTimeHealthParams,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  '/adm/api/integration-closure/time/health',
  { ...options, method: 'GET', params: { zone: params?.zone, maxSkewMillis: params?.maxSkewMillis } },
);

export const admIntegrationDataQualityValidate = async (
  recordId: string,
  data: AdmIntegrationRecord,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/integration-closure/data-quality/validate/${encodeURIComponent(String(recordId))}`,
  { ...options, method: 'POST', headers: { 'Content-Type': 'application/json', ...options?.headers }, data },
);

export const admIntegrationDataQualityCorrectionApprovalRequest = async (
  id: string,
  data: AdmIntegrationCorrectionApprovalRequest,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/integration-closure/data-quality/quarantine/${encodeURIComponent(String(id))}/correction-approvals`,
  { ...options, method: 'POST', headers: { 'Content-Type': 'application/json', ...options?.headers }, data },
);

export const admIntegrationDataQualityCorrectionExecute = async (
  approvalRequestId: number,
  data: AdmIntegrationCorrectionExecutionRequest,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/integration-closure/data-quality/correction-approvals/${encodeURIComponent(String(approvalRequestId))}/execute`,
  { ...options, method: 'POST', headers: { 'Content-Type': 'application/json', ...options?.headers }, data },
);

export const admIntegrationDataQualityReplay = async (
  id: string,
  params: AdmIntegrationDataQualityReplayParams,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/integration-closure/data-quality/quarantine/${encodeURIComponent(String(id))}/replay`,
  { ...options, method: 'POST', params: { reason: params.reason } },
);

export const admIntegrationWebhookDlq = async (
  params?: AdmIntegrationWebhookDlqParams,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  '/adm/api/integration-closure/webhooks/dlq',
  { ...options, method: 'GET', params: { limit: params?.limit } },
);

export const admIntegrationWebhookReplay = async (
  id: string,
  params: AdmIntegrationWebhookReplayParams,
  options?: CpfOrvalGeneratedRequestOptions,
): Promise<CpfControllerSourceResponse> => cpfOrvalRequest<CpfControllerSourceResponse>(
  `/adm/api/integration-closure/webhooks/${encodeURIComponent(String(id))}/replay`,
  { ...options, method: 'POST', params: { expectedVersion: params.expectedVersion, reason: params.reason } },
);
// CPF PRE-RUNTIME FALLBACK END REV-004
