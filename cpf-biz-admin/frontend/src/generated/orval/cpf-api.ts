/**
 * Generated from the CPF controller-source pre-runtime OpenAPI contract.
 * Runtime OpenAPI generation must replace this deterministic compatibility client.
 */
import { useMutation, useQuery } from '@tanstack/vue-query';
import type { DataTag, MutationFunction, QueryClient, QueryFunction, QueryKey, UseMutationOptions, UseMutationReturnType, UseQueryOptions, UseQueryReturnType } from '@tanstack/vue-query';
import { computed, toValue, unref } from 'vue';
import type { MaybeRefOrGetter } from 'vue';
import type {
  ActionRule,
  AdminUserRequest,
  ApiPermission,
  ApprovalState,
  AssignmentRequest,
  AttachmentDownload,
  AttachmentSecurityRequest,
  BootstrapResult,
  BzaApprovalDelegationsParams,
  BzaApprovalDirectoryEntry,
  BzaApprovalExpireDueParams,
  BzaApprovalInboxParams,
  BzaApprovalPoliciesParams,
  BzaApprovalPolicySimulateRequest,
  BzaApprovalSubmissionsParams,
  BzaAuthLoginHistoriesParams,
  BzaAuthRevokeSessionParams,
  BzaAuthSessionsParams,
  BzaAuthorizationResult,
  BzaBackofficeFindBusinessAuditsParams,
  BzaBackofficeFindEffectivePermissionsParams,
  BzaBackofficeFindEmployeesPageParams,
  BzaBackofficeFindEmployeesParams,
  BzaBackofficeFindOrganizationsPageParams,
  BzaCommonCatalogRefreshParams,
  BzaCommonDetailRequest,
  BzaCommonMessageCreateParams,
  BzaCommonMessageDisableParams,
  BzaCommonMessageSearchParams,
  BzaCommonMessageUpdateParams,
  BzaCommonResponseCodeCreateParams,
  BzaCommonResponseCodeDisableParams,
  BzaCommonResponseCodeSearchParams,
  BzaCommonResponseCodeUpdateParams,
  BzaCommonSearchParams,
  BzaCurrentOperatorResponse,
  BzaDirectoryFindAssignmentsPageParams,
  BzaDirectoryFindAssignmentsParams,
  BzaDirectoryFindJobTitlesPageParams,
  BzaDirectoryFindPositionsPageParams,
  BzaDirectoryFindResponsibilitiesPageParams,
  BzaDirectoryFindResponsibilitiesParams,
  BzaDirectoryFindUserRolesPageParams,
  BzaDirectoryFindUserRolesParams,
  BzaEmployeeRawContactResponse,
  BzaLoginHistoryResponse,
  BzaLogoutResponse,
  BzaOperationFindAdminUsersPageParams,
  BzaOperationFindMenusPageParams,
  BzaOperationFindPermissionsPageParams,
  BzaOperationFindRolesPageParams,
  BzaOperatorResponse,
  BzaOperatorRow,
  BzaPasswordChangeResponse,
  BzaSessionResponse,
  BzaSessionRevokeResponse,
  BzaSupportCompareRolePermissionsParams,
  BzaSupportDisableSavedSearchParams,
  BzaSupportDownloadAttachmentParams,
  BzaSupportFindAttachmentsParams,
  BzaSupportFindDownloadAuditsParams,
  BzaSupportFindNotificationsParams,
  BzaSupportFindSavedSearchesParams,
  BzaSupportReadAllNotificationsParams,
  BzaSupportReadNotificationParams,
  BzaSupportRecheckAttachmentParams,
  BzaSupportUploadAttachmentParams,
  BzaSupportUploadAttachmentRequest,
  CommonMessageRequest,
  CommonResponseCodeRequest,
  CpfApiError,
  CpfCommonMutation,
  CpfControllerSourceResponse,
  CpfSensitiveDataAccessRequest,
  DecisionRequest,
  Definition,
  DelegationRequest,
  EmployeeRequest,
  JobTitleRequest,
  LifecycleRequest,
  LoginCommitResult,
  LoginFailureCommand,
  LoginHistoryWrite,
  LoginOperationState,
  LoginRequest,
  LoginResult,
  LoginSuccessCommand,
  MenuDeleteRequest,
  MenuDeleteResult,
  MenuImpact,
  MenuRequest,
  NotificationRequest,
  OrganizationRequest,
  PasswordChangeRequest,
  PermissionRequest,
  PermissionSimulationRequest,
  PolicyRequest,
  PolicyStepRequest,
  PositionRequest,
  RawActionRule,
  RawDefinition,
  RefreshRequest,
  RefreshTokenRow,
  RefreshTokenWrite,
  ResponsibilityRequest,
  RoleRequest,
  SavedSearchRequest,
  SequenceAudit,
  SequenceRequest,
  SequenceResult,
  SequenceRule,
  SequenceState,
  SubmitRequest,
  UserRoleRequest
} from './model';
import { cpfOrvalRequest } from '../../shared/orval-mutator';
import type { CpfOrvalGeneratedRequestOptions } from '../../shared/orval-mutator';
type SecondParameter<T extends (...args: never) => unknown> = CpfOrvalGeneratedRequestOptions;


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindAdminUsers
export type bzaOperationFindAdminUsersResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindAdminUsersResponseSuccess = (bzaOperationFindAdminUsersResponse200) & {
  headers: Headers;
};

export type bzaOperationFindAdminUsersResponse = (bzaOperationFindAdminUsersResponseSuccess)

export const getBzaOperationFindAdminUsersUrl = () => `/api/bza/admin-users`;

export const bzaOperationFindAdminUsers = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindAdminUsersResponse> => {
  return cpfOrvalRequest<bzaOperationFindAdminUsersResponse>(getBzaOperationFindAdminUsersUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindAdminUsersQueryKey = () => ["api", "bza", "admin-users"] as const;

export const getBzaOperationFindAdminUsersQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindAdminUsersQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>> = ({ signal }) => bzaOperationFindAdminUsers({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>, TError, TData>;
};

export type BzaOperationFindAdminUsersQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>>;
export type BzaOperationFindAdminUsersQueryError = unknown;

export function useBzaOperationFindAdminUsers<TData = Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsers>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindAdminUsersQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindAdminUsers


// CPF PRE-RUNTIME FALLBACK START bzaOperationSaveAdminUser
export type bzaOperationSaveAdminUserResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationSaveAdminUserResponseSuccess = (bzaOperationSaveAdminUserResponse200) & {
  headers: Headers;
};

export type bzaOperationSaveAdminUserResponse = (bzaOperationSaveAdminUserResponseSuccess)

export const getBzaOperationSaveAdminUserUrl = () => `/api/bza/admin-users`;

export const bzaOperationSaveAdminUser = async (data: AdminUserRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationSaveAdminUserResponse> => {
  return cpfOrvalRequest<bzaOperationSaveAdminUserResponse>(getBzaOperationSaveAdminUserUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaOperationSaveAdminUserMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>, TError, {data: AdminUserRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>, TError, {data: AdminUserRequest}, TContext> => {
  const mutationKey = ['bzaOperationSaveAdminUser'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>, {data: AdminUserRequest}> = (props) => {
    const { data } = props;
    return bzaOperationSaveAdminUser(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaOperationSaveAdminUserMutationResult = NonNullable<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>>;
export type BzaOperationSaveAdminUserMutationBody = AdminUserRequest;
export type BzaOperationSaveAdminUserMutationError = unknown;

export const useBzaOperationSaveAdminUser = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>, TError, {data: AdminUserRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaOperationSaveAdminUser>>, TError, {data: AdminUserRequest}, TContext> => {
  return useMutation(getBzaOperationSaveAdminUserMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaOperationSaveAdminUser


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindAdminUsersPage
export type bzaOperationFindAdminUsersPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindAdminUsersPageResponseSuccess = (bzaOperationFindAdminUsersPageResponse200) & {
  headers: Headers;
};

export type bzaOperationFindAdminUsersPageResponse = (bzaOperationFindAdminUsersPageResponseSuccess)

export const getBzaOperationFindAdminUsersPageUrl = () => `/api/bza/admin-users/page`;

export const bzaOperationFindAdminUsersPage = async (params?: BzaOperationFindAdminUsersPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindAdminUsersPageResponse> => {
  return cpfOrvalRequest<bzaOperationFindAdminUsersPageResponse>(getBzaOperationFindAdminUsersPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaOperationFindAdminUsersPageQueryKey = (params?: MaybeRefOrGetter<BzaOperationFindAdminUsersPageParams>) => ["api", "bza", "admin-users", "page", toValue(params)] as const;

export const getBzaOperationFindAdminUsersPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindAdminUsersPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindAdminUsersPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>> = ({ signal }) => bzaOperationFindAdminUsersPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>, TError, TData>;
};

export type BzaOperationFindAdminUsersPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>>;
export type BzaOperationFindAdminUsersPageQueryError = unknown;

export function useBzaOperationFindAdminUsersPage<TData = Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindAdminUsersPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindAdminUsersPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindAdminUsersPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindAdminUsersPage


// CPF PRE-RUNTIME FALLBACK START bzaApprovalDelegations
export type bzaApprovalDelegationsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalDelegationsResponseSuccess = (bzaApprovalDelegationsResponse200) & {
  headers: Headers;
};

export type bzaApprovalDelegationsResponse = (bzaApprovalDelegationsResponseSuccess)

export const getBzaApprovalDelegationsUrl = () => `/api/bza/approvals/delegations`;

export const bzaApprovalDelegations = async (params?: BzaApprovalDelegationsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalDelegationsResponse> => {
  return cpfOrvalRequest<bzaApprovalDelegationsResponse>(getBzaApprovalDelegationsUrl(), {
    ...options,
    method: 'GET',
    params: { employeeNo: params?.employeeNo, effectiveAt: params?.effectiveAt },
  });
};

export const getBzaApprovalDelegationsQueryKey = (params?: MaybeRefOrGetter<BzaApprovalDelegationsParams>) => ["api", "bza", "approvals", "delegations", toValue(params)] as const;

export const getBzaApprovalDelegationsQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalDelegations>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalDelegationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalDelegations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalDelegationsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalDelegations>>> = ({ signal }) => bzaApprovalDelegations(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalDelegations>>, TError, TData>;
};

export type BzaApprovalDelegationsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalDelegations>>>;
export type BzaApprovalDelegationsQueryError = unknown;

export function useBzaApprovalDelegations<TData = Awaited<ReturnType<typeof bzaApprovalDelegations>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalDelegationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalDelegations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalDelegationsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalDelegations


// CPF PRE-RUNTIME FALLBACK START bzaApprovalDelegationSave
export type bzaApprovalDelegationSaveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalDelegationSaveResponseSuccess = (bzaApprovalDelegationSaveResponse200) & {
  headers: Headers;
};

export type bzaApprovalDelegationSaveResponse = (bzaApprovalDelegationSaveResponseSuccess)

export const getBzaApprovalDelegationSaveUrl = () => `/api/bza/approvals/delegations`;

export const bzaApprovalDelegationSave = async (data: DelegationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalDelegationSaveResponse> => {
  return cpfOrvalRequest<bzaApprovalDelegationSaveResponse>(getBzaApprovalDelegationSaveUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalDelegationSaveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>, TError, {data: DelegationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>, TError, {data: DelegationRequest}, TContext> => {
  const mutationKey = ['bzaApprovalDelegationSave'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>, {data: DelegationRequest}> = (props) => {
    const { data } = props;
    return bzaApprovalDelegationSave(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalDelegationSaveMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>>;
export type BzaApprovalDelegationSaveMutationBody = DelegationRequest;
export type BzaApprovalDelegationSaveMutationError = unknown;

export const useBzaApprovalDelegationSave = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>, TError, {data: DelegationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalDelegationSave>>, TError, {data: DelegationRequest}, TContext> => {
  return useMutation(getBzaApprovalDelegationSaveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalDelegationSave


// CPF PRE-RUNTIME FALLBACK START bzaApprovalInbox
export type bzaApprovalInboxResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalInboxResponseSuccess = (bzaApprovalInboxResponse200) & {
  headers: Headers;
};

export type bzaApprovalInboxResponse = (bzaApprovalInboxResponseSuccess)

export const getBzaApprovalInboxUrl = () => `/api/bza/approvals/inbox`;

export const bzaApprovalInbox = async (params?: BzaApprovalInboxParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalInboxResponse> => {
  return cpfOrvalRequest<bzaApprovalInboxResponse>(getBzaApprovalInboxUrl(), {
    ...options,
    method: 'GET',
    params: { decisionStatus: params?.decisionStatus, limit: params?.limit },
  });
};

export const getBzaApprovalInboxQueryKey = (params?: MaybeRefOrGetter<BzaApprovalInboxParams>) => ["api", "bza", "approvals", "inbox", toValue(params)] as const;

export const getBzaApprovalInboxQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalInbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalInboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalInbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalInboxQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalInbox>>> = ({ signal }) => bzaApprovalInbox(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalInbox>>, TError, TData>;
};

export type BzaApprovalInboxQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalInbox>>>;
export type BzaApprovalInboxQueryError = unknown;

export function useBzaApprovalInbox<TData = Awaited<ReturnType<typeof bzaApprovalInbox>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalInboxParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalInbox>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalInboxQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalInbox


// CPF PRE-RUNTIME FALLBACK START bzaApprovalPolicies
export type bzaApprovalPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalPoliciesResponseSuccess = (bzaApprovalPoliciesResponse200) & {
  headers: Headers;
};

export type bzaApprovalPoliciesResponse = (bzaApprovalPoliciesResponseSuccess)

export const getBzaApprovalPoliciesUrl = () => `/api/bza/approvals/policies`;

export const bzaApprovalPolicies = async (params?: BzaApprovalPoliciesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalPoliciesResponse> => {
  return cpfOrvalRequest<bzaApprovalPoliciesResponse>(getBzaApprovalPoliciesUrl(), {
    ...options,
    method: 'GET',
    params: { businessDomain: params?.businessDomain, approvalType: params?.approvalType },
  });
};

export const getBzaApprovalPoliciesQueryKey = (params?: MaybeRefOrGetter<BzaApprovalPoliciesParams>) => ["api", "bza", "approvals", "policies", toValue(params)] as const;

export const getBzaApprovalPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalPoliciesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalPolicies>>> = ({ signal }) => bzaApprovalPolicies(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicies>>, TError, TData>;
};

export type BzaApprovalPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalPolicies>>>;
export type BzaApprovalPoliciesQueryError = unknown;

export function useBzaApprovalPolicies<TData = Awaited<ReturnType<typeof bzaApprovalPolicies>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalPoliciesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalPoliciesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalPolicies


// CPF PRE-RUNTIME FALLBACK START bzaApprovalPolicySave
export type bzaApprovalPolicySaveResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalPolicySaveResponseSuccess = (bzaApprovalPolicySaveResponse200) & {
  headers: Headers;
};

export type bzaApprovalPolicySaveResponse = (bzaApprovalPolicySaveResponseSuccess)

export const getBzaApprovalPolicySaveUrl = () => `/api/bza/approvals/policies`;

export const bzaApprovalPolicySave = async (data: PolicyRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalPolicySaveResponse> => {
  return cpfOrvalRequest<bzaApprovalPolicySaveResponse>(getBzaApprovalPolicySaveUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalPolicySaveMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySave>>, TError, {data: PolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySave>>, TError, {data: PolicyRequest}, TContext> => {
  const mutationKey = ['bzaApprovalPolicySave'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalPolicySave>>, {data: PolicyRequest}> = (props) => {
    const { data } = props;
    return bzaApprovalPolicySave(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalPolicySaveMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalPolicySave>>>;
export type BzaApprovalPolicySaveMutationBody = PolicyRequest;
export type BzaApprovalPolicySaveMutationError = unknown;

export const useBzaApprovalPolicySave = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySave>>, TError, {data: PolicyRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalPolicySave>>, TError, {data: PolicyRequest}, TContext> => {
  return useMutation(getBzaApprovalPolicySaveMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalPolicySave


// CPF PRE-RUNTIME FALLBACK START bzaApprovalPolicyDetail
export type bzaApprovalPolicyDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalPolicyDetailResponseSuccess = (bzaApprovalPolicyDetailResponse200) & {
  headers: Headers;
};

export type bzaApprovalPolicyDetailResponse = (bzaApprovalPolicyDetailResponseSuccess)

export const getBzaApprovalPolicyDetailUrl = (policyCode: string, version: number) => `/api/bza/approvals/policies/${encodeURIComponent(String(policyCode))}/${encodeURIComponent(String(version))}`;

export const bzaApprovalPolicyDetail = async (policyCode: string, version: number, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalPolicyDetailResponse> => {
  return cpfOrvalRequest<bzaApprovalPolicyDetailResponse>(getBzaApprovalPolicyDetailUrl(policyCode, version), {
    ...options,
    method: 'GET',

  });
};

export const getBzaApprovalPolicyDetailQueryKey = (policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>) => ["api", "bza", "approvals", "policies", policyCode, version] as const;

export const getBzaApprovalPolicyDetailQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>, TError = unknown>(
  policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalPolicyDetailQueryKey(toValue(policyCode), toValue(version));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>> = ({ signal }) => bzaApprovalPolicyDetail(toValue(policyCode), toValue(version), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(policyCode) !== null && toValue(policyCode) !== undefined && toValue(version) !== null && toValue(version) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>, TError, TData>;
};

export type BzaApprovalPolicyDetailQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>>;
export type BzaApprovalPolicyDetailQueryError = unknown;

export function useBzaApprovalPolicyDetail<TData = Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>, TError = unknown>(
  policyCode: MaybeRefOrGetter<string>, version: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalPolicyDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalPolicyDetailQueryOptions(policyCode, version, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalPolicyDetail


// CPF PRE-RUNTIME FALLBACK START bzaApprovalPolicySimulate
export type bzaApprovalPolicySimulateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalPolicySimulateResponseSuccess = (bzaApprovalPolicySimulateResponse200) & {
  headers: Headers;
};

export type bzaApprovalPolicySimulateResponse = (bzaApprovalPolicySimulateResponseSuccess)

export const getBzaApprovalPolicySimulateUrl = () => `/api/bza/approvals/simulate`;

export const bzaApprovalPolicySimulate = async (data: BzaApprovalPolicySimulateRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalPolicySimulateResponse> => {
  return cpfOrvalRequest<bzaApprovalPolicySimulateResponse>(getBzaApprovalPolicySimulateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalPolicySimulateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>, TError, {data: BzaApprovalPolicySimulateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>, TError, {data: BzaApprovalPolicySimulateRequest}, TContext> => {
  const mutationKey = ['bzaApprovalPolicySimulate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>, {data: BzaApprovalPolicySimulateRequest}> = (props) => {
    const { data } = props;
    return bzaApprovalPolicySimulate(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalPolicySimulateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>>;
export type BzaApprovalPolicySimulateMutationBody = BzaApprovalPolicySimulateRequest;
export type BzaApprovalPolicySimulateMutationError = unknown;

export const useBzaApprovalPolicySimulate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>, TError, {data: BzaApprovalPolicySimulateRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalPolicySimulate>>, TError, {data: BzaApprovalPolicySimulateRequest}, TContext> => {
  return useMutation(getBzaApprovalPolicySimulateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalPolicySimulate


// CPF PRE-RUNTIME FALLBACK START bzaApprovalSubmissions
export type bzaApprovalSubmissionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalSubmissionsResponseSuccess = (bzaApprovalSubmissionsResponse200) & {
  headers: Headers;
};

export type bzaApprovalSubmissionsResponse = (bzaApprovalSubmissionsResponseSuccess)

export const getBzaApprovalSubmissionsUrl = () => `/api/bza/approvals/submissions`;

export const bzaApprovalSubmissions = async (params?: BzaApprovalSubmissionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalSubmissionsResponse> => {
  return cpfOrvalRequest<bzaApprovalSubmissionsResponse>(getBzaApprovalSubmissionsUrl(), {
    ...options,
    method: 'GET',
    params: { status: params?.status, limit: params?.limit },
  });
};

export const getBzaApprovalSubmissionsQueryKey = (params?: MaybeRefOrGetter<BzaApprovalSubmissionsParams>) => ["api", "bza", "approvals", "submissions", toValue(params)] as const;

export const getBzaApprovalSubmissionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalSubmissions>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalSubmissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalSubmissionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalSubmissions>>> = ({ signal }) => bzaApprovalSubmissions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissions>>, TError, TData>;
};

export type BzaApprovalSubmissionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalSubmissions>>>;
export type BzaApprovalSubmissionsQueryError = unknown;

export function useBzaApprovalSubmissions<TData = Awaited<ReturnType<typeof bzaApprovalSubmissions>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaApprovalSubmissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalSubmissionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalSubmissions


// CPF PRE-RUNTIME FALLBACK START bzaApprovalPolicySubmit
export type bzaApprovalPolicySubmitResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalPolicySubmitResponseSuccess = (bzaApprovalPolicySubmitResponse200) & {
  headers: Headers;
};

export type bzaApprovalPolicySubmitResponse = (bzaApprovalPolicySubmitResponseSuccess)

export const getBzaApprovalPolicySubmitUrl = () => `/api/bza/approvals/submissions`;

export const bzaApprovalPolicySubmit = async (data: SubmitRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalPolicySubmitResponse> => {
  return cpfOrvalRequest<bzaApprovalPolicySubmitResponse>(getBzaApprovalPolicySubmitUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalPolicySubmitMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>, TError, {data: SubmitRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>, TError, {data: SubmitRequest}, TContext> => {
  const mutationKey = ['bzaApprovalPolicySubmit'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>, {data: SubmitRequest}> = (props) => {
    const { data } = props;
    return bzaApprovalPolicySubmit(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalPolicySubmitMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>>;
export type BzaApprovalPolicySubmitMutationBody = SubmitRequest;
export type BzaApprovalPolicySubmitMutationError = unknown;

export const useBzaApprovalPolicySubmit = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>, TError, {data: SubmitRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalPolicySubmit>>, TError, {data: SubmitRequest}, TContext> => {
  return useMutation(getBzaApprovalPolicySubmitMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalPolicySubmit


// CPF PRE-RUNTIME FALLBACK START bzaApprovalExpireDue
export type bzaApprovalExpireDueResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalExpireDueResponseSuccess = (bzaApprovalExpireDueResponse200) & {
  headers: Headers;
};

export type bzaApprovalExpireDueResponse = (bzaApprovalExpireDueResponseSuccess)

export const getBzaApprovalExpireDueUrl = () => `/api/bza/approvals/submissions/expire-due`;

export const bzaApprovalExpireDue = async (params?: BzaApprovalExpireDueParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalExpireDueResponse> => {
  return cpfOrvalRequest<bzaApprovalExpireDueResponse>(getBzaApprovalExpireDueUrl(), {
    ...options,
    method: 'POST',
    params: { limit: params?.limit },
  });
};

export const getBzaApprovalExpireDueMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalExpireDue>>, TError, {params?: BzaApprovalExpireDueParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalExpireDue>>, TError, {params?: BzaApprovalExpireDueParams}, TContext> => {
  const mutationKey = ['bzaApprovalExpireDue'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalExpireDue>>, {params?: BzaApprovalExpireDueParams}> = (props) => {
    const { params } = props;
    return bzaApprovalExpireDue(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalExpireDueMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalExpireDue>>>;
export type BzaApprovalExpireDueMutationBody = never;
export type BzaApprovalExpireDueMutationError = unknown;

export const useBzaApprovalExpireDue = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalExpireDue>>, TError, {params?: BzaApprovalExpireDueParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalExpireDue>>, TError, {params?: BzaApprovalExpireDueParams}, TContext> => {
  return useMutation(getBzaApprovalExpireDueMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalExpireDue


// CPF PRE-RUNTIME FALLBACK START bzaApprovalSubmissionDetail
export type bzaApprovalSubmissionDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalSubmissionDetailResponseSuccess = (bzaApprovalSubmissionDetailResponse200) & {
  headers: Headers;
};

export type bzaApprovalSubmissionDetailResponse = (bzaApprovalSubmissionDetailResponseSuccess)

export const getBzaApprovalSubmissionDetailUrl = (approvalId: number) => `/api/bza/approvals/submissions/${encodeURIComponent(String(approvalId))}`;

export const bzaApprovalSubmissionDetail = async (approvalId: number, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalSubmissionDetailResponse> => {
  return cpfOrvalRequest<bzaApprovalSubmissionDetailResponse>(getBzaApprovalSubmissionDetailUrl(approvalId), {
    ...options,
    method: 'GET',

  });
};

export const getBzaApprovalSubmissionDetailQueryKey = (approvalId: MaybeRefOrGetter<number>) => ["api", "bza", "approvals", "submissions", approvalId] as const;

export const getBzaApprovalSubmissionDetailQueryOptions = <TData = Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>, TError = unknown>(
  approvalId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaApprovalSubmissionDetailQueryKey(toValue(approvalId));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>> = ({ signal }) => bzaApprovalSubmissionDetail(toValue(approvalId), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(approvalId) !== null && toValue(approvalId) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>, TError, TData>;
};

export type BzaApprovalSubmissionDetailQueryResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>>;
export type BzaApprovalSubmissionDetailQueryError = unknown;

export function useBzaApprovalSubmissionDetail<TData = Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>, TError = unknown>(
  approvalId: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaApprovalSubmissionDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaApprovalSubmissionDetailQueryOptions(approvalId, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaApprovalSubmissionDetail


// CPF PRE-RUNTIME FALLBACK START bzaApprovalCancel
export type bzaApprovalCancelResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalCancelResponseSuccess = (bzaApprovalCancelResponse200) & {
  headers: Headers;
};

export type bzaApprovalCancelResponse = (bzaApprovalCancelResponseSuccess)

export const getBzaApprovalCancelUrl = (approvalId: number) => `/api/bza/approvals/submissions/${encodeURIComponent(String(approvalId))}/cancel`;

export const bzaApprovalCancel = async (approvalId: number, data: LifecycleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalCancelResponse> => {
  return cpfOrvalRequest<bzaApprovalCancelResponse>(getBzaApprovalCancelUrl(approvalId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalCancelMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalCancel>>, TError, {approvalId: number; data: LifecycleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalCancel>>, TError, {approvalId: number; data: LifecycleRequest}, TContext> => {
  const mutationKey = ['bzaApprovalCancel'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalCancel>>, {approvalId: number; data: LifecycleRequest}> = (props) => {
    const { approvalId, data } = props;
    return bzaApprovalCancel(approvalId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalCancelMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalCancel>>>;
export type BzaApprovalCancelMutationBody = LifecycleRequest;
export type BzaApprovalCancelMutationError = unknown;

export const useBzaApprovalCancel = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalCancel>>, TError, {approvalId: number; data: LifecycleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalCancel>>, TError, {approvalId: number; data: LifecycleRequest}, TContext> => {
  return useMutation(getBzaApprovalCancelMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalCancel


// CPF PRE-RUNTIME FALLBACK START bzaApprovalResubmit
export type bzaApprovalResubmitResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalResubmitResponseSuccess = (bzaApprovalResubmitResponse200) & {
  headers: Headers;
};

export type bzaApprovalResubmitResponse = (bzaApprovalResubmitResponseSuccess)

export const getBzaApprovalResubmitUrl = (approvalId: number) => `/api/bza/approvals/submissions/${encodeURIComponent(String(approvalId))}/resubmit`;

export const bzaApprovalResubmit = async (approvalId: number, data: SubmitRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalResubmitResponse> => {
  return cpfOrvalRequest<bzaApprovalResubmitResponse>(getBzaApprovalResubmitUrl(approvalId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalResubmitMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalResubmit>>, TError, {approvalId: number; data: SubmitRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalResubmit>>, TError, {approvalId: number; data: SubmitRequest}, TContext> => {
  const mutationKey = ['bzaApprovalResubmit'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalResubmit>>, {approvalId: number; data: SubmitRequest}> = (props) => {
    const { approvalId, data } = props;
    return bzaApprovalResubmit(approvalId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalResubmitMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalResubmit>>>;
export type BzaApprovalResubmitMutationBody = SubmitRequest;
export type BzaApprovalResubmitMutationError = unknown;

export const useBzaApprovalResubmit = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalResubmit>>, TError, {approvalId: number; data: SubmitRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalResubmit>>, TError, {approvalId: number; data: SubmitRequest}, TContext> => {
  return useMutation(getBzaApprovalResubmitMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalResubmit


// CPF PRE-RUNTIME FALLBACK START bzaApprovalWithdraw
export type bzaApprovalWithdrawResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalWithdrawResponseSuccess = (bzaApprovalWithdrawResponse200) & {
  headers: Headers;
};

export type bzaApprovalWithdrawResponse = (bzaApprovalWithdrawResponseSuccess)

export const getBzaApprovalWithdrawUrl = (approvalId: number) => `/api/bza/approvals/submissions/${encodeURIComponent(String(approvalId))}/withdraw`;

export const bzaApprovalWithdraw = async (approvalId: number, data: LifecycleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalWithdrawResponse> => {
  return cpfOrvalRequest<bzaApprovalWithdrawResponse>(getBzaApprovalWithdrawUrl(approvalId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalWithdrawMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalWithdraw>>, TError, {approvalId: number; data: LifecycleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalWithdraw>>, TError, {approvalId: number; data: LifecycleRequest}, TContext> => {
  const mutationKey = ['bzaApprovalWithdraw'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalWithdraw>>, {approvalId: number; data: LifecycleRequest}> = (props) => {
    const { approvalId, data } = props;
    return bzaApprovalWithdraw(approvalId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalWithdrawMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalWithdraw>>>;
export type BzaApprovalWithdrawMutationBody = LifecycleRequest;
export type BzaApprovalWithdrawMutationError = unknown;

export const useBzaApprovalWithdraw = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalWithdraw>>, TError, {approvalId: number; data: LifecycleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalWithdraw>>, TError, {approvalId: number; data: LifecycleRequest}, TContext> => {
  return useMutation(getBzaApprovalWithdrawMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalWithdraw


// CPF PRE-RUNTIME FALLBACK START bzaApprovalParticipantDecision
export type bzaApprovalParticipantDecisionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaApprovalParticipantDecisionResponseSuccess = (bzaApprovalParticipantDecisionResponse200) & {
  headers: Headers;
};

export type bzaApprovalParticipantDecisionResponse = (bzaApprovalParticipantDecisionResponseSuccess)

export const getBzaApprovalParticipantDecisionUrl = (approvalId: number) => `/api/bza/approvals/${encodeURIComponent(String(approvalId))}/decisions`;

export const bzaApprovalParticipantDecision = async (approvalId: number, data: DecisionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaApprovalParticipantDecisionResponse> => {
  return cpfOrvalRequest<bzaApprovalParticipantDecisionResponse>(getBzaApprovalParticipantDecisionUrl(approvalId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaApprovalParticipantDecisionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>, TError, {approvalId: number; data: DecisionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>, TError, {approvalId: number; data: DecisionRequest}, TContext> => {
  const mutationKey = ['bzaApprovalParticipantDecision'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>, {approvalId: number; data: DecisionRequest}> = (props) => {
    const { approvalId, data } = props;
    return bzaApprovalParticipantDecision(approvalId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaApprovalParticipantDecisionMutationResult = NonNullable<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>>;
export type BzaApprovalParticipantDecisionMutationBody = DecisionRequest;
export type BzaApprovalParticipantDecisionMutationError = unknown;

export const useBzaApprovalParticipantDecision = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>, TError, {approvalId: number; data: DecisionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaApprovalParticipantDecision>>, TError, {approvalId: number; data: DecisionRequest}, TContext> => {
  return useMutation(getBzaApprovalParticipantDecisionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaApprovalParticipantDecision


// CPF PRE-RUNTIME FALLBACK START bzaSupportFindAttachments
export type bzaSupportFindAttachmentsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportFindAttachmentsResponseSuccess = (bzaSupportFindAttachmentsResponse200) & {
  headers: Headers;
};

export type bzaSupportFindAttachmentsResponse = (bzaSupportFindAttachmentsResponseSuccess)

export const getBzaSupportFindAttachmentsUrl = () => `/api/bza/attachments`;

export const bzaSupportFindAttachments = async (params: BzaSupportFindAttachmentsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportFindAttachmentsResponse> => {
  return cpfOrvalRequest<bzaSupportFindAttachmentsResponse>(getBzaSupportFindAttachmentsUrl(), {
    ...options,
    method: 'GET',
    params: { groupId: params.groupId },
  });
};

export const getBzaSupportFindAttachmentsQueryKey = (params: MaybeRefOrGetter<BzaSupportFindAttachmentsParams>) => ["api", "bza", "attachments", toValue(params)] as const;

export const getBzaSupportFindAttachmentsQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportFindAttachments>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaSupportFindAttachmentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindAttachments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportFindAttachmentsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportFindAttachments>>> = ({ signal }) => bzaSupportFindAttachments(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindAttachments>>, TError, TData>;
};

export type BzaSupportFindAttachmentsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportFindAttachments>>>;
export type BzaSupportFindAttachmentsQueryError = unknown;

export function useBzaSupportFindAttachments<TData = Awaited<ReturnType<typeof bzaSupportFindAttachments>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaSupportFindAttachmentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindAttachments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportFindAttachmentsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportFindAttachments


// CPF PRE-RUNTIME FALLBACK START bzaSupportUploadAttachment
export type bzaSupportUploadAttachmentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportUploadAttachmentResponseSuccess = (bzaSupportUploadAttachmentResponse200) & {
  headers: Headers;
};

export type bzaSupportUploadAttachmentResponse = (bzaSupportUploadAttachmentResponseSuccess)

export const getBzaSupportUploadAttachmentUrl = () => `/api/bza/attachments`;

export const bzaSupportUploadAttachment = async (data: BzaSupportUploadAttachmentRequest, params: BzaSupportUploadAttachmentParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportUploadAttachmentResponse> => {
  return cpfOrvalRequest<bzaSupportUploadAttachmentResponse>(getBzaSupportUploadAttachmentUrl(), {
    ...options,
    method: 'POST',
    params: { groupId: params.groupId, reason: params.reason },
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaSupportUploadAttachmentMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>, TError, {data: BzaSupportUploadAttachmentRequest; params: BzaSupportUploadAttachmentParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>, TError, {data: BzaSupportUploadAttachmentRequest; params: BzaSupportUploadAttachmentParams}, TContext> => {
  const mutationKey = ['bzaSupportUploadAttachment'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>, {data: BzaSupportUploadAttachmentRequest; params: BzaSupportUploadAttachmentParams}> = (props) => {
    const { data, params } = props;
    return bzaSupportUploadAttachment(data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportUploadAttachmentMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>>;
export type BzaSupportUploadAttachmentMutationBody = BzaSupportUploadAttachmentRequest;
export type BzaSupportUploadAttachmentMutationError = unknown;

export const useBzaSupportUploadAttachment = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>, TError, {data: BzaSupportUploadAttachmentRequest; params: BzaSupportUploadAttachmentParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportUploadAttachment>>, TError, {data: BzaSupportUploadAttachmentRequest; params: BzaSupportUploadAttachmentParams}, TContext> => {
  return useMutation(getBzaSupportUploadAttachmentMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportUploadAttachment


// CPF PRE-RUNTIME FALLBACK START bzaSupportDownloadAttachment
export type bzaSupportDownloadAttachmentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportDownloadAttachmentResponseSuccess = (bzaSupportDownloadAttachmentResponse200) & {
  headers: Headers;
};

export type bzaSupportDownloadAttachmentResponse = (bzaSupportDownloadAttachmentResponseSuccess)

export const getBzaSupportDownloadAttachmentUrl = (attachmentId: number) => `/api/bza/attachments/${encodeURIComponent(String(attachmentId))}/download`;

export const bzaSupportDownloadAttachment = async (attachmentId: number, params: BzaSupportDownloadAttachmentParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportDownloadAttachmentResponse> => {
  return cpfOrvalRequest<bzaSupportDownloadAttachmentResponse>(getBzaSupportDownloadAttachmentUrl(attachmentId), {
    ...options,
    method: 'GET',
    params: { reason: params.reason },
  });
};

export const getBzaSupportDownloadAttachmentQueryKey = (attachmentId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<BzaSupportDownloadAttachmentParams>) => ["api", "bza", "attachments", attachmentId, "download", toValue(params)] as const;

export const getBzaSupportDownloadAttachmentQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>, TError = unknown>(
  attachmentId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<BzaSupportDownloadAttachmentParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportDownloadAttachmentQueryKey(toValue(attachmentId), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>> = ({ signal }) => bzaSupportDownloadAttachment(toValue(attachmentId), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(attachmentId) !== null && toValue(attachmentId) !== undefined && toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>, TError, TData>;
};

export type BzaSupportDownloadAttachmentQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>>;
export type BzaSupportDownloadAttachmentQueryError = unknown;

export function useBzaSupportDownloadAttachment<TData = Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>, TError = unknown>(
  attachmentId: MaybeRefOrGetter<number>, params: MaybeRefOrGetter<BzaSupportDownloadAttachmentParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDownloadAttachment>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportDownloadAttachmentQueryOptions(attachmentId, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportDownloadAttachment


// CPF PRE-RUNTIME FALLBACK START bzaSupportRecheckAttachment
export type bzaSupportRecheckAttachmentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportRecheckAttachmentResponseSuccess = (bzaSupportRecheckAttachmentResponse200) & {
  headers: Headers;
};

export type bzaSupportRecheckAttachmentResponse = (bzaSupportRecheckAttachmentResponseSuccess)

export const getBzaSupportRecheckAttachmentUrl = (attachmentId: number) => `/api/bza/attachments/${encodeURIComponent(String(attachmentId))}/recheck`;

export const bzaSupportRecheckAttachment = async (attachmentId: number, params: BzaSupportRecheckAttachmentParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportRecheckAttachmentResponse> => {
  return cpfOrvalRequest<bzaSupportRecheckAttachmentResponse>(getBzaSupportRecheckAttachmentUrl(attachmentId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getBzaSupportRecheckAttachmentMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>, TError, {attachmentId: number; params: BzaSupportRecheckAttachmentParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>, TError, {attachmentId: number; params: BzaSupportRecheckAttachmentParams}, TContext> => {
  const mutationKey = ['bzaSupportRecheckAttachment'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>, {attachmentId: number; params: BzaSupportRecheckAttachmentParams}> = (props) => {
    const { attachmentId, params } = props;
    return bzaSupportRecheckAttachment(attachmentId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportRecheckAttachmentMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>>;
export type BzaSupportRecheckAttachmentMutationBody = never;
export type BzaSupportRecheckAttachmentMutationError = unknown;

export const useBzaSupportRecheckAttachment = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>, TError, {attachmentId: number; params: BzaSupportRecheckAttachmentParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportRecheckAttachment>>, TError, {attachmentId: number; params: BzaSupportRecheckAttachmentParams}, TContext> => {
  return useMutation(getBzaSupportRecheckAttachmentMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportRecheckAttachment


// CPF PRE-RUNTIME FALLBACK START bzaSupportUpdateAttachmentSecurity
export type bzaSupportUpdateAttachmentSecurityResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportUpdateAttachmentSecurityResponseSuccess = (bzaSupportUpdateAttachmentSecurityResponse200) & {
  headers: Headers;
};

export type bzaSupportUpdateAttachmentSecurityResponse = (bzaSupportUpdateAttachmentSecurityResponseSuccess)

export const getBzaSupportUpdateAttachmentSecurityUrl = (attachmentId: number) => `/api/bza/attachments/${encodeURIComponent(String(attachmentId))}/security`;

export const bzaSupportUpdateAttachmentSecurity = async (attachmentId: number, data: AttachmentSecurityRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportUpdateAttachmentSecurityResponse> => {
  return cpfOrvalRequest<bzaSupportUpdateAttachmentSecurityResponse>(getBzaSupportUpdateAttachmentSecurityUrl(attachmentId), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaSupportUpdateAttachmentSecurityMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>, TError, {attachmentId: number; data: AttachmentSecurityRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>, TError, {attachmentId: number; data: AttachmentSecurityRequest}, TContext> => {
  const mutationKey = ['bzaSupportUpdateAttachmentSecurity'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>, {attachmentId: number; data: AttachmentSecurityRequest}> = (props) => {
    const { attachmentId, data } = props;
    return bzaSupportUpdateAttachmentSecurity(attachmentId, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportUpdateAttachmentSecurityMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>>;
export type BzaSupportUpdateAttachmentSecurityMutationBody = AttachmentSecurityRequest;
export type BzaSupportUpdateAttachmentSecurityMutationError = unknown;

export const useBzaSupportUpdateAttachmentSecurity = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>, TError, {attachmentId: number; data: AttachmentSecurityRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportUpdateAttachmentSecurity>>, TError, {attachmentId: number; data: AttachmentSecurityRequest}, TContext> => {
  return useMutation(getBzaSupportUpdateAttachmentSecurityMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportUpdateAttachmentSecurity


// CPF PRE-RUNTIME FALLBACK START bzaBusinessAuditVerify
export type bzaBusinessAuditVerifyResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBusinessAuditVerifyResponseSuccess = (bzaBusinessAuditVerifyResponse200) & {
  headers: Headers;
};

export type bzaBusinessAuditVerifyResponse = (bzaBusinessAuditVerifyResponseSuccess)

export const getBzaBusinessAuditVerifyUrl = () => `/api/bza/audits/verify`;

export const bzaBusinessAuditVerify = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBusinessAuditVerifyResponse> => {
  return cpfOrvalRequest<bzaBusinessAuditVerifyResponse>(getBzaBusinessAuditVerifyUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaBusinessAuditVerifyQueryKey = () => ["api", "bza", "audits", "verify"] as const;

export const getBzaBusinessAuditVerifyQueryOptions = <TData = Awaited<ReturnType<typeof bzaBusinessAuditVerify>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBusinessAuditVerify>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBusinessAuditVerifyQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBusinessAuditVerify>>> = ({ signal }) => bzaBusinessAuditVerify({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBusinessAuditVerify>>, TError, TData>;
};

export type BzaBusinessAuditVerifyQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBusinessAuditVerify>>>;
export type BzaBusinessAuditVerifyQueryError = unknown;

export function useBzaBusinessAuditVerify<TData = Awaited<ReturnType<typeof bzaBusinessAuditVerify>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBusinessAuditVerify>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBusinessAuditVerifyQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBusinessAuditVerify


// CPF PRE-RUNTIME FALLBACK START bzaAuthLogin
export type bzaAuthLoginResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthLoginResponseSuccess = (bzaAuthLoginResponse200) & {
  headers: Headers;
};

export type bzaAuthLoginResponse = (bzaAuthLoginResponseSuccess)

export const getBzaAuthLoginUrl = () => `/api/bza/auth/login`;

export const bzaAuthLogin = async (data: LoginRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthLoginResponse> => {
  return cpfOrvalRequest<bzaAuthLoginResponse>(getBzaAuthLoginUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaAuthLoginMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogin>>, TError, {data: LoginRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogin>>, TError, {data: LoginRequest}, TContext> => {
  const mutationKey = ['bzaAuthLogin'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaAuthLogin>>, {data: LoginRequest}> = (props) => {
    const { data } = props;
    return bzaAuthLogin(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaAuthLoginMutationResult = NonNullable<Awaited<ReturnType<typeof bzaAuthLogin>>>;
export type BzaAuthLoginMutationBody = LoginRequest;
export type BzaAuthLoginMutationError = unknown;

export const useBzaAuthLogin = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogin>>, TError, {data: LoginRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaAuthLogin>>, TError, {data: LoginRequest}, TContext> => {
  return useMutation(getBzaAuthLoginMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaAuthLogin


// CPF PRE-RUNTIME FALLBACK START bzaAuthLoginHistories
export type bzaAuthLoginHistoriesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthLoginHistoriesResponseSuccess = (bzaAuthLoginHistoriesResponse200) & {
  headers: Headers;
};

export type bzaAuthLoginHistoriesResponse = (bzaAuthLoginHistoriesResponseSuccess)

export const getBzaAuthLoginHistoriesUrl = () => `/api/bza/auth/login-history`;

export const bzaAuthLoginHistories = async (params?: BzaAuthLoginHistoriesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthLoginHistoriesResponse> => {
  return cpfOrvalRequest<bzaAuthLoginHistoriesResponse>(getBzaAuthLoginHistoriesUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getBzaAuthLoginHistoriesQueryKey = (params?: MaybeRefOrGetter<BzaAuthLoginHistoriesParams>) => ["api", "bza", "auth", "login-history", toValue(params)] as const;

export const getBzaAuthLoginHistoriesQueryOptions = <TData = Awaited<ReturnType<typeof bzaAuthLoginHistories>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaAuthLoginHistoriesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthLoginHistories>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaAuthLoginHistoriesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaAuthLoginHistories>>> = ({ signal }) => bzaAuthLoginHistories(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaAuthLoginHistories>>, TError, TData>;
};

export type BzaAuthLoginHistoriesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaAuthLoginHistories>>>;
export type BzaAuthLoginHistoriesQueryError = unknown;

export function useBzaAuthLoginHistories<TData = Awaited<ReturnType<typeof bzaAuthLoginHistories>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaAuthLoginHistoriesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthLoginHistories>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaAuthLoginHistoriesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaAuthLoginHistories


// CPF PRE-RUNTIME FALLBACK START bzaAuthLogout
export type bzaAuthLogoutResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthLogoutResponseSuccess = (bzaAuthLogoutResponse200) & {
  headers: Headers;
};

export type bzaAuthLogoutResponse = (bzaAuthLogoutResponseSuccess)

export const getBzaAuthLogoutUrl = () => `/api/bza/auth/logout`;

export const bzaAuthLogout = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthLogoutResponse> => {
  return cpfOrvalRequest<bzaAuthLogoutResponse>(getBzaAuthLogoutUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getBzaAuthLogoutMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogout>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogout>>, TError, void, TContext> => {
  const mutationKey = ['bzaAuthLogout'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaAuthLogout>>, void> = () => {

    return bzaAuthLogout(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaAuthLogoutMutationResult = NonNullable<Awaited<ReturnType<typeof bzaAuthLogout>>>;
export type BzaAuthLogoutMutationBody = never;
export type BzaAuthLogoutMutationError = unknown;

export const useBzaAuthLogout = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthLogout>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaAuthLogout>>, TError, void, TContext> => {
  return useMutation(getBzaAuthLogoutMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaAuthLogout


// CPF PRE-RUNTIME FALLBACK START bzaAuthMe
export type bzaAuthMeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthMeResponseSuccess = (bzaAuthMeResponse200) & {
  headers: Headers;
};

export type bzaAuthMeResponse = (bzaAuthMeResponseSuccess)

export const getBzaAuthMeUrl = () => `/api/bza/auth/me`;

export const bzaAuthMe = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthMeResponse> => {
  return cpfOrvalRequest<bzaAuthMeResponse>(getBzaAuthMeUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaAuthMeQueryKey = () => ["api", "bza", "auth", "me"] as const;

export const getBzaAuthMeQueryOptions = <TData = Awaited<ReturnType<typeof bzaAuthMe>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthMe>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaAuthMeQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaAuthMe>>> = ({ signal }) => bzaAuthMe({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaAuthMe>>, TError, TData>;
};

export type BzaAuthMeQueryResult = NonNullable<Awaited<ReturnType<typeof bzaAuthMe>>>;
export type BzaAuthMeQueryError = unknown;

export function useBzaAuthMe<TData = Awaited<ReturnType<typeof bzaAuthMe>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthMe>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaAuthMeQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaAuthMe


// CPF PRE-RUNTIME FALLBACK START bzaAuthChangePassword
export type bzaAuthChangePasswordResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthChangePasswordResponseSuccess = (bzaAuthChangePasswordResponse200) & {
  headers: Headers;
};

export type bzaAuthChangePasswordResponse = (bzaAuthChangePasswordResponseSuccess)

export const getBzaAuthChangePasswordUrl = () => `/api/bza/auth/password/change`;

export const bzaAuthChangePassword = async (data: PasswordChangeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthChangePasswordResponse> => {
  return cpfOrvalRequest<bzaAuthChangePasswordResponse>(getBzaAuthChangePasswordUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaAuthChangePasswordMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthChangePassword>>, TError, {data: PasswordChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaAuthChangePassword>>, TError, {data: PasswordChangeRequest}, TContext> => {
  const mutationKey = ['bzaAuthChangePassword'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaAuthChangePassword>>, {data: PasswordChangeRequest}> = (props) => {
    const { data } = props;
    return bzaAuthChangePassword(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaAuthChangePasswordMutationResult = NonNullable<Awaited<ReturnType<typeof bzaAuthChangePassword>>>;
export type BzaAuthChangePasswordMutationBody = PasswordChangeRequest;
export type BzaAuthChangePasswordMutationError = unknown;

export const useBzaAuthChangePassword = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthChangePassword>>, TError, {data: PasswordChangeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaAuthChangePassword>>, TError, {data: PasswordChangeRequest}, TContext> => {
  return useMutation(getBzaAuthChangePasswordMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaAuthChangePassword


// CPF PRE-RUNTIME FALLBACK START bzaAuthRefresh
export type bzaAuthRefreshResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthRefreshResponseSuccess = (bzaAuthRefreshResponse200) & {
  headers: Headers;
};

export type bzaAuthRefreshResponse = (bzaAuthRefreshResponseSuccess)

export const getBzaAuthRefreshUrl = () => `/api/bza/auth/refresh`;

export const bzaAuthRefresh = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthRefreshResponse> => {
  return cpfOrvalRequest<bzaAuthRefreshResponse>(getBzaAuthRefreshUrl(), {
    ...options,
    method: 'POST',

  });
};

export const getBzaAuthRefreshMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRefresh>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRefresh>>, TError, void, TContext> => {
  const mutationKey = ['bzaAuthRefresh'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaAuthRefresh>>, void> = () => {

    return bzaAuthRefresh(requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaAuthRefreshMutationResult = NonNullable<Awaited<ReturnType<typeof bzaAuthRefresh>>>;
export type BzaAuthRefreshMutationBody = never;
export type BzaAuthRefreshMutationError = unknown;

export const useBzaAuthRefresh = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRefresh>>, TError, void, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaAuthRefresh>>, TError, void, TContext> => {
  return useMutation(getBzaAuthRefreshMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaAuthRefresh


// CPF PRE-RUNTIME FALLBACK START bzaAuthSessions
export type bzaAuthSessionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthSessionsResponseSuccess = (bzaAuthSessionsResponse200) & {
  headers: Headers;
};

export type bzaAuthSessionsResponse = (bzaAuthSessionsResponseSuccess)

export const getBzaAuthSessionsUrl = () => `/api/bza/auth/sessions`;

export const bzaAuthSessions = async (params?: BzaAuthSessionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthSessionsResponse> => {
  return cpfOrvalRequest<bzaAuthSessionsResponse>(getBzaAuthSessionsUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getBzaAuthSessionsQueryKey = (params?: MaybeRefOrGetter<BzaAuthSessionsParams>) => ["api", "bza", "auth", "sessions", toValue(params)] as const;

export const getBzaAuthSessionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaAuthSessions>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaAuthSessionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaAuthSessionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaAuthSessions>>> = ({ signal }) => bzaAuthSessions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaAuthSessions>>, TError, TData>;
};

export type BzaAuthSessionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaAuthSessions>>>;
export type BzaAuthSessionsQueryError = unknown;

export function useBzaAuthSessions<TData = Awaited<ReturnType<typeof bzaAuthSessions>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaAuthSessionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaAuthSessions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaAuthSessionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaAuthSessions


// CPF PRE-RUNTIME FALLBACK START bzaAuthRevokeSession
export type bzaAuthRevokeSessionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaAuthRevokeSessionResponseSuccess = (bzaAuthRevokeSessionResponse200) & {
  headers: Headers;
};

export type bzaAuthRevokeSessionResponse = (bzaAuthRevokeSessionResponseSuccess)

export const getBzaAuthRevokeSessionUrl = (sessionId: string) => `/api/bza/auth/sessions/${encodeURIComponent(String(sessionId))}/revoke`;

export const bzaAuthRevokeSession = async (sessionId: string, params: BzaAuthRevokeSessionParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaAuthRevokeSessionResponse> => {
  return cpfOrvalRequest<bzaAuthRevokeSessionResponse>(getBzaAuthRevokeSessionUrl(sessionId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getBzaAuthRevokeSessionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRevokeSession>>, TError, {sessionId: string; params: BzaAuthRevokeSessionParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRevokeSession>>, TError, {sessionId: string; params: BzaAuthRevokeSessionParams}, TContext> => {
  const mutationKey = ['bzaAuthRevokeSession'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaAuthRevokeSession>>, {sessionId: string; params: BzaAuthRevokeSessionParams}> = (props) => {
    const { sessionId, params } = props;
    return bzaAuthRevokeSession(sessionId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaAuthRevokeSessionMutationResult = NonNullable<Awaited<ReturnType<typeof bzaAuthRevokeSession>>>;
export type BzaAuthRevokeSessionMutationBody = never;
export type BzaAuthRevokeSessionMutationError = unknown;

export const useBzaAuthRevokeSession = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaAuthRevokeSession>>, TError, {sessionId: string; params: BzaAuthRevokeSessionParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaAuthRevokeSession>>, TError, {sessionId: string; params: BzaAuthRevokeSessionParams}, TContext> => {
  return useMutation(getBzaAuthRevokeSessionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaAuthRevokeSession


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindBusinessAudits
export type bzaBackofficeFindBusinessAuditsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindBusinessAuditsResponseSuccess = (bzaBackofficeFindBusinessAuditsResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindBusinessAuditsResponse = (bzaBackofficeFindBusinessAuditsResponseSuccess)

export const getBzaBackofficeFindBusinessAuditsUrl = () => `/api/bza/backoffice/audits`;

export const bzaBackofficeFindBusinessAudits = async (params?: BzaBackofficeFindBusinessAuditsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindBusinessAuditsResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindBusinessAuditsResponse>(getBzaBackofficeFindBusinessAuditsUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getBzaBackofficeFindBusinessAuditsQueryKey = (params?: MaybeRefOrGetter<BzaBackofficeFindBusinessAuditsParams>) => ["api", "bza", "backoffice", "audits", toValue(params)] as const;

export const getBzaBackofficeFindBusinessAuditsQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindBusinessAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindBusinessAuditsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>> = ({ signal }) => bzaBackofficeFindBusinessAudits(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>, TError, TData>;
};

export type BzaBackofficeFindBusinessAuditsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>>;
export type BzaBackofficeFindBusinessAuditsQueryError = unknown;

export function useBzaBackofficeFindBusinessAudits<TData = Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindBusinessAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindBusinessAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindBusinessAuditsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindBusinessAudits


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindEmployees
export type bzaBackofficeFindEmployeesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindEmployeesResponseSuccess = (bzaBackofficeFindEmployeesResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindEmployeesResponse = (bzaBackofficeFindEmployeesResponseSuccess)

export const getBzaBackofficeFindEmployeesUrl = () => `/api/bza/backoffice/employees`;

export const bzaBackofficeFindEmployees = async (params?: BzaBackofficeFindEmployeesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindEmployeesResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindEmployeesResponse>(getBzaBackofficeFindEmployeesUrl(), {
    ...options,
    method: 'GET',
    params: { organizationCode: params?.organizationCode, status: params?.status },
  });
};

export const getBzaBackofficeFindEmployeesQueryKey = (params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesParams>) => ["api", "bza", "backoffice", "employees", toValue(params)] as const;

export const getBzaBackofficeFindEmployeesQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindEmployeesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>> = ({ signal }) => bzaBackofficeFindEmployees(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>, TError, TData>;
};

export type BzaBackofficeFindEmployeesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>>;
export type BzaBackofficeFindEmployeesQueryError = unknown;

export function useBzaBackofficeFindEmployees<TData = Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployees>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindEmployeesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindEmployees


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeSaveEmployee
export type bzaBackofficeSaveEmployeeResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeSaveEmployeeResponseSuccess = (bzaBackofficeSaveEmployeeResponse200) & {
  headers: Headers;
};

export type bzaBackofficeSaveEmployeeResponse = (bzaBackofficeSaveEmployeeResponseSuccess)

export const getBzaBackofficeSaveEmployeeUrl = () => `/api/bza/backoffice/employees`;

export const bzaBackofficeSaveEmployee = async (data: EmployeeRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeSaveEmployeeResponse> => {
  return cpfOrvalRequest<bzaBackofficeSaveEmployeeResponse>(getBzaBackofficeSaveEmployeeUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaBackofficeSaveEmployeeMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>, TError, {data: EmployeeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>, TError, {data: EmployeeRequest}, TContext> => {
  const mutationKey = ['bzaBackofficeSaveEmployee'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>, {data: EmployeeRequest}> = (props) => {
    const { data } = props;
    return bzaBackofficeSaveEmployee(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaBackofficeSaveEmployeeMutationResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>>;
export type BzaBackofficeSaveEmployeeMutationBody = EmployeeRequest;
export type BzaBackofficeSaveEmployeeMutationError = unknown;

export const useBzaBackofficeSaveEmployee = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>, TError, {data: EmployeeRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaBackofficeSaveEmployee>>, TError, {data: EmployeeRequest}, TContext> => {
  return useMutation(getBzaBackofficeSaveEmployeeMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeSaveEmployee


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindEmployeesPage
export type bzaBackofficeFindEmployeesPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindEmployeesPageResponseSuccess = (bzaBackofficeFindEmployeesPageResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindEmployeesPageResponse = (bzaBackofficeFindEmployeesPageResponseSuccess)

export const getBzaBackofficeFindEmployeesPageUrl = () => `/api/bza/backoffice/employees/page`;

export const bzaBackofficeFindEmployeesPage = async (params?: BzaBackofficeFindEmployeesPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindEmployeesPageResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindEmployeesPageResponse>(getBzaBackofficeFindEmployeesPageUrl(), {
    ...options,
    method: 'GET',
    params: { organizationCode: params?.organizationCode, status: params?.status, page: params?.page, size: params?.size },
  });
};

export const getBzaBackofficeFindEmployeesPageQueryKey = (params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesPageParams>) => ["api", "bza", "backoffice", "employees", "page", toValue(params)] as const;

export const getBzaBackofficeFindEmployeesPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindEmployeesPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>> = ({ signal }) => bzaBackofficeFindEmployeesPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>, TError, TData>;
};

export type BzaBackofficeFindEmployeesPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>>;
export type BzaBackofficeFindEmployeesPageQueryError = unknown;

export function useBzaBackofficeFindEmployeesPage<TData = Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindEmployeesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEmployeesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindEmployeesPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindEmployeesPage


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeEmployeeRawContact
export type bzaBackofficeEmployeeRawContactResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeEmployeeRawContactResponseSuccess = (bzaBackofficeEmployeeRawContactResponse200) & {
  headers: Headers;
};

export type bzaBackofficeEmployeeRawContactResponse = (bzaBackofficeEmployeeRawContactResponseSuccess)

export const getBzaBackofficeEmployeeRawContactUrl = (employeeNo: string) => `/api/bza/backoffice/employees/${encodeURIComponent(String(employeeNo))}/contacts/raw`;

export const bzaBackofficeEmployeeRawContact = async (employeeNo: string, data: CpfSensitiveDataAccessRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeEmployeeRawContactResponse> => {
  return cpfOrvalRequest<bzaBackofficeEmployeeRawContactResponse>(getBzaBackofficeEmployeeRawContactUrl(employeeNo), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaBackofficeEmployeeRawContactMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>, TError, {employeeNo: string; data: CpfSensitiveDataAccessRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>, TError, {employeeNo: string; data: CpfSensitiveDataAccessRequest}, TContext> => {
  const mutationKey = ['bzaBackofficeEmployeeRawContact'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>, {employeeNo: string; data: CpfSensitiveDataAccessRequest}> = (props) => {
    const { employeeNo, data } = props;
    return bzaBackofficeEmployeeRawContact(employeeNo, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaBackofficeEmployeeRawContactMutationResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>>;
export type BzaBackofficeEmployeeRawContactMutationBody = CpfSensitiveDataAccessRequest;
export type BzaBackofficeEmployeeRawContactMutationError = unknown;

export const useBzaBackofficeEmployeeRawContact = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>, TError, {employeeNo: string; data: CpfSensitiveDataAccessRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaBackofficeEmployeeRawContact>>, TError, {employeeNo: string; data: CpfSensitiveDataAccessRequest}, TContext> => {
  return useMutation(getBzaBackofficeEmployeeRawContactMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeEmployeeRawContact


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindOrganizations
export type bzaBackofficeFindOrganizationsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindOrganizationsResponseSuccess = (bzaBackofficeFindOrganizationsResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindOrganizationsResponse = (bzaBackofficeFindOrganizationsResponseSuccess)

export const getBzaBackofficeFindOrganizationsUrl = () => `/api/bza/backoffice/organizations`;

export const bzaBackofficeFindOrganizations = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindOrganizationsResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindOrganizationsResponse>(getBzaBackofficeFindOrganizationsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaBackofficeFindOrganizationsQueryKey = () => ["api", "bza", "backoffice", "organizations"] as const;

export const getBzaBackofficeFindOrganizationsQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindOrganizationsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>> = ({ signal }) => bzaBackofficeFindOrganizations({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>, TError, TData>;
};

export type BzaBackofficeFindOrganizationsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>>;
export type BzaBackofficeFindOrganizationsQueryError = unknown;

export function useBzaBackofficeFindOrganizations<TData = Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizations>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindOrganizationsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindOrganizations


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeSaveOrganization
export type bzaBackofficeSaveOrganizationResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeSaveOrganizationResponseSuccess = (bzaBackofficeSaveOrganizationResponse200) & {
  headers: Headers;
};

export type bzaBackofficeSaveOrganizationResponse = (bzaBackofficeSaveOrganizationResponseSuccess)

export const getBzaBackofficeSaveOrganizationUrl = () => `/api/bza/backoffice/organizations`;

export const bzaBackofficeSaveOrganization = async (data: OrganizationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeSaveOrganizationResponse> => {
  return cpfOrvalRequest<bzaBackofficeSaveOrganizationResponse>(getBzaBackofficeSaveOrganizationUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaBackofficeSaveOrganizationMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>, TError, {data: OrganizationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>, TError, {data: OrganizationRequest}, TContext> => {
  const mutationKey = ['bzaBackofficeSaveOrganization'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>, {data: OrganizationRequest}> = (props) => {
    const { data } = props;
    return bzaBackofficeSaveOrganization(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaBackofficeSaveOrganizationMutationResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>>;
export type BzaBackofficeSaveOrganizationMutationBody = OrganizationRequest;
export type BzaBackofficeSaveOrganizationMutationError = unknown;

export const useBzaBackofficeSaveOrganization = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>, TError, {data: OrganizationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaBackofficeSaveOrganization>>, TError, {data: OrganizationRequest}, TContext> => {
  return useMutation(getBzaBackofficeSaveOrganizationMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeSaveOrganization


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindOrganizationsPage
export type bzaBackofficeFindOrganizationsPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindOrganizationsPageResponseSuccess = (bzaBackofficeFindOrganizationsPageResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindOrganizationsPageResponse = (bzaBackofficeFindOrganizationsPageResponseSuccess)

export const getBzaBackofficeFindOrganizationsPageUrl = () => `/api/bza/backoffice/organizations/page`;

export const bzaBackofficeFindOrganizationsPage = async (params?: BzaBackofficeFindOrganizationsPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindOrganizationsPageResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindOrganizationsPageResponse>(getBzaBackofficeFindOrganizationsPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaBackofficeFindOrganizationsPageQueryKey = (params?: MaybeRefOrGetter<BzaBackofficeFindOrganizationsPageParams>) => ["api", "bza", "backoffice", "organizations", "page", toValue(params)] as const;

export const getBzaBackofficeFindOrganizationsPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindOrganizationsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindOrganizationsPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>> = ({ signal }) => bzaBackofficeFindOrganizationsPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>, TError, TData>;
};

export type BzaBackofficeFindOrganizationsPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>>;
export type BzaBackofficeFindOrganizationsPageQueryError = unknown;

export function useBzaBackofficeFindOrganizationsPage<TData = Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaBackofficeFindOrganizationsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindOrganizationsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindOrganizationsPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindOrganizationsPage


// CPF PRE-RUNTIME FALLBACK START bzaBackofficeFindEffectivePermissions
export type bzaBackofficeFindEffectivePermissionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaBackofficeFindEffectivePermissionsResponseSuccess = (bzaBackofficeFindEffectivePermissionsResponse200) & {
  headers: Headers;
};

export type bzaBackofficeFindEffectivePermissionsResponse = (bzaBackofficeFindEffectivePermissionsResponseSuccess)

export const getBzaBackofficeFindEffectivePermissionsUrl = () => `/api/bza/backoffice/permissions/effective`;

export const bzaBackofficeFindEffectivePermissions = async (params: BzaBackofficeFindEffectivePermissionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaBackofficeFindEffectivePermissionsResponse> => {
  return cpfOrvalRequest<bzaBackofficeFindEffectivePermissionsResponse>(getBzaBackofficeFindEffectivePermissionsUrl(), {
    ...options,
    method: 'GET',
    params: { loginId: params.loginId },
  });
};

export const getBzaBackofficeFindEffectivePermissionsQueryKey = (params: MaybeRefOrGetter<BzaBackofficeFindEffectivePermissionsParams>) => ["api", "bza", "backoffice", "permissions", "effective", toValue(params)] as const;

export const getBzaBackofficeFindEffectivePermissionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaBackofficeFindEffectivePermissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaBackofficeFindEffectivePermissionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>> = ({ signal }) => bzaBackofficeFindEffectivePermissions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>, TError, TData>;
};

export type BzaBackofficeFindEffectivePermissionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>>;
export type BzaBackofficeFindEffectivePermissionsQueryError = unknown;

export function useBzaBackofficeFindEffectivePermissions<TData = Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaBackofficeFindEffectivePermissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaBackofficeFindEffectivePermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaBackofficeFindEffectivePermissionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaBackofficeFindEffectivePermissions


// CPF PRE-RUNTIME FALLBACK START bzaCommonCatalogRefresh
export type bzaCommonCatalogRefreshResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonCatalogRefreshResponseSuccess = (bzaCommonCatalogRefreshResponse200) & {
  headers: Headers;
};

export type bzaCommonCatalogRefreshResponse = (bzaCommonCatalogRefreshResponseSuccess)

export const getBzaCommonCatalogRefreshUrl = () => `/api/bza/common-catalog/cache/refresh`;

export const bzaCommonCatalogRefresh = async (params: BzaCommonCatalogRefreshParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonCatalogRefreshResponse> => {
  return cpfOrvalRequest<bzaCommonCatalogRefreshResponse>(getBzaCommonCatalogRefreshUrl(), {
    ...options,
    method: 'POST',
    headers: { "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
  });
};

export const getBzaCommonCatalogRefreshMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>, TError, {params: BzaCommonCatalogRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>, TError, {params: BzaCommonCatalogRefreshParams}, TContext> => {
  const mutationKey = ['bzaCommonCatalogRefresh'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>, {params: BzaCommonCatalogRefreshParams}> = (props) => {
    const { params } = props;
    return bzaCommonCatalogRefresh(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonCatalogRefreshMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>>;
export type BzaCommonCatalogRefreshMutationBody = never;
export type BzaCommonCatalogRefreshMutationError = unknown;

export const useBzaCommonCatalogRefresh = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>, TError, {params: BzaCommonCatalogRefreshParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonCatalogRefresh>>, TError, {params: BzaCommonCatalogRefreshParams}, TContext> => {
  return useMutation(getBzaCommonCatalogRefreshMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonCatalogRefresh


// CPF PRE-RUNTIME FALLBACK START bzaCommonMessageSearch
export type bzaCommonMessageSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonMessageSearchResponseSuccess = (bzaCommonMessageSearchResponse200) & {
  headers: Headers;
};

export type bzaCommonMessageSearchResponse = (bzaCommonMessageSearchResponseSuccess)

export const getBzaCommonMessageSearchUrl = () => `/api/bza/common-catalog/messages`;

export const bzaCommonMessageSearch = async (params?: BzaCommonMessageSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonMessageSearchResponse> => {
  return cpfOrvalRequest<bzaCommonMessageSearchResponse>(getBzaCommonMessageSearchUrl(), {
    ...options,
    method: 'GET',
    params: { keyword: params?.keyword, locale: params?.locale, active: params?.active, page: params?.page, size: params?.size },
  });
};

export const getBzaCommonMessageSearchQueryKey = (params?: MaybeRefOrGetter<BzaCommonMessageSearchParams>) => ["api", "bza", "common-catalog", "messages", toValue(params)] as const;

export const getBzaCommonMessageSearchQueryOptions = <TData = Awaited<ReturnType<typeof bzaCommonMessageSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaCommonMessageSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaCommonMessageSearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaCommonMessageSearch>>> = ({ signal }) => bzaCommonMessageSearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageSearch>>, TError, TData>;
};

export type BzaCommonMessageSearchQueryResult = NonNullable<Awaited<ReturnType<typeof bzaCommonMessageSearch>>>;
export type BzaCommonMessageSearchQueryError = unknown;

export function useBzaCommonMessageSearch<TData = Awaited<ReturnType<typeof bzaCommonMessageSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaCommonMessageSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaCommonMessageSearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaCommonMessageSearch


// CPF PRE-RUNTIME FALLBACK START bzaCommonMessageCreate
export type bzaCommonMessageCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonMessageCreateResponseSuccess = (bzaCommonMessageCreateResponse200) & {
  headers: Headers;
};

export type bzaCommonMessageCreateResponse = (bzaCommonMessageCreateResponseSuccess)

export const getBzaCommonMessageCreateUrl = () => `/api/bza/common-catalog/messages`;

export const bzaCommonMessageCreate = async (data: CommonMessageRequest, params: BzaCommonMessageCreateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonMessageCreateResponse> => {
  return cpfOrvalRequest<bzaCommonMessageCreateResponse>(getBzaCommonMessageCreateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
    data,
  });
};

export const getBzaCommonMessageCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageCreate>>, TError, {data: CommonMessageRequest; params: BzaCommonMessageCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageCreate>>, TError, {data: CommonMessageRequest; params: BzaCommonMessageCreateParams}, TContext> => {
  const mutationKey = ['bzaCommonMessageCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonMessageCreate>>, {data: CommonMessageRequest; params: BzaCommonMessageCreateParams}> = (props) => {
    const { data, params } = props;
    return bzaCommonMessageCreate(data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonMessageCreateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonMessageCreate>>>;
export type BzaCommonMessageCreateMutationBody = CommonMessageRequest;
export type BzaCommonMessageCreateMutationError = unknown;

export const useBzaCommonMessageCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageCreate>>, TError, {data: CommonMessageRequest; params: BzaCommonMessageCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonMessageCreate>>, TError, {data: CommonMessageRequest; params: BzaCommonMessageCreateParams}, TContext> => {
  return useMutation(getBzaCommonMessageCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonMessageCreate


// CPF PRE-RUNTIME FALLBACK START bzaCommonMessageDisable
export type bzaCommonMessageDisableResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonMessageDisableResponseSuccess = (bzaCommonMessageDisableResponse200) & {
  headers: Headers;
};

export type bzaCommonMessageDisableResponse = (bzaCommonMessageDisableResponseSuccess)

export const getBzaCommonMessageDisableUrl = (id: number) => `/api/bza/common-catalog/messages/${encodeURIComponent(String(id))}`;

export const bzaCommonMessageDisable = async (id: number, params: BzaCommonMessageDisableParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonMessageDisableResponse> => {
  return cpfOrvalRequest<bzaCommonMessageDisableResponse>(getBzaCommonMessageDisableUrl(id), {
    ...options,
    method: 'DELETE',
    params: { expectedVersion: params.expectedVersion },
    headers: { "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
  });
};

export const getBzaCommonMessageDisableMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageDisable>>, TError, {id: number; params: BzaCommonMessageDisableParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageDisable>>, TError, {id: number; params: BzaCommonMessageDisableParams}, TContext> => {
  const mutationKey = ['bzaCommonMessageDisable'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonMessageDisable>>, {id: number; params: BzaCommonMessageDisableParams}> = (props) => {
    const { id, params } = props;
    return bzaCommonMessageDisable(id, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonMessageDisableMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonMessageDisable>>>;
export type BzaCommonMessageDisableMutationBody = never;
export type BzaCommonMessageDisableMutationError = unknown;

export const useBzaCommonMessageDisable = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageDisable>>, TError, {id: number; params: BzaCommonMessageDisableParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonMessageDisable>>, TError, {id: number; params: BzaCommonMessageDisableParams}, TContext> => {
  return useMutation(getBzaCommonMessageDisableMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonMessageDisable


// CPF PRE-RUNTIME FALLBACK START bzaCommonMessageDetail
export type bzaCommonMessageDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonMessageDetailResponseSuccess = (bzaCommonMessageDetailResponse200) & {
  headers: Headers;
};

export type bzaCommonMessageDetailResponse = (bzaCommonMessageDetailResponseSuccess)

export const getBzaCommonMessageDetailUrl = (id: number) => `/api/bza/common-catalog/messages/${encodeURIComponent(String(id))}`;

export const bzaCommonMessageDetail = async (id: number, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonMessageDetailResponse> => {
  return cpfOrvalRequest<bzaCommonMessageDetailResponse>(getBzaCommonMessageDetailUrl(id), {
    ...options,
    method: 'GET',

  });
};

export const getBzaCommonMessageDetailQueryKey = (id: MaybeRefOrGetter<number>) => ["api", "bza", "common-catalog", "messages", id] as const;

export const getBzaCommonMessageDetailQueryOptions = <TData = Awaited<ReturnType<typeof bzaCommonMessageDetail>>, TError = unknown>(
  id: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaCommonMessageDetailQueryKey(toValue(id));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaCommonMessageDetail>>> = ({ signal }) => bzaCommonMessageDetail(toValue(id), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(id) !== null && toValue(id) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageDetail>>, TError, TData>;
};

export type BzaCommonMessageDetailQueryResult = NonNullable<Awaited<ReturnType<typeof bzaCommonMessageDetail>>>;
export type BzaCommonMessageDetailQueryError = unknown;

export function useBzaCommonMessageDetail<TData = Awaited<ReturnType<typeof bzaCommonMessageDetail>>, TError = unknown>(
  id: MaybeRefOrGetter<number>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonMessageDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaCommonMessageDetailQueryOptions(id, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaCommonMessageDetail


// CPF PRE-RUNTIME FALLBACK START bzaCommonMessageUpdate
export type bzaCommonMessageUpdateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonMessageUpdateResponseSuccess = (bzaCommonMessageUpdateResponse200) & {
  headers: Headers;
};

export type bzaCommonMessageUpdateResponse = (bzaCommonMessageUpdateResponseSuccess)

export const getBzaCommonMessageUpdateUrl = (id: number) => `/api/bza/common-catalog/messages/${encodeURIComponent(String(id))}`;

export const bzaCommonMessageUpdate = async (id: number, data: CommonMessageRequest, params: BzaCommonMessageUpdateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonMessageUpdateResponse> => {
  return cpfOrvalRequest<bzaCommonMessageUpdateResponse>(getBzaCommonMessageUpdateUrl(id), {
    ...options,
    method: 'PUT',
    params: { expectedVersion: params.expectedVersion },
    headers: { 'Content-Type': 'application/json', "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
    data,
  });
};

export const getBzaCommonMessageUpdateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>, TError, {id: number; data: CommonMessageRequest; params: BzaCommonMessageUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>, TError, {id: number; data: CommonMessageRequest; params: BzaCommonMessageUpdateParams}, TContext> => {
  const mutationKey = ['bzaCommonMessageUpdate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>, {id: number; data: CommonMessageRequest; params: BzaCommonMessageUpdateParams}> = (props) => {
    const { id, data, params } = props;
    return bzaCommonMessageUpdate(id, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonMessageUpdateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>>;
export type BzaCommonMessageUpdateMutationBody = CommonMessageRequest;
export type BzaCommonMessageUpdateMutationError = unknown;

export const useBzaCommonMessageUpdate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>, TError, {id: number; data: CommonMessageRequest; params: BzaCommonMessageUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonMessageUpdate>>, TError, {id: number; data: CommonMessageRequest; params: BzaCommonMessageUpdateParams}, TContext> => {
  return useMutation(getBzaCommonMessageUpdateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonMessageUpdate


// CPF PRE-RUNTIME FALLBACK START bzaCommonResponseCodeSearch
export type bzaCommonResponseCodeSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonResponseCodeSearchResponseSuccess = (bzaCommonResponseCodeSearchResponse200) & {
  headers: Headers;
};

export type bzaCommonResponseCodeSearchResponse = (bzaCommonResponseCodeSearchResponseSuccess)

export const getBzaCommonResponseCodeSearchUrl = () => `/api/bza/common-catalog/response-codes`;

export const bzaCommonResponseCodeSearch = async (params?: BzaCommonResponseCodeSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonResponseCodeSearchResponse> => {
  return cpfOrvalRequest<bzaCommonResponseCodeSearchResponse>(getBzaCommonResponseCodeSearchUrl(), {
    ...options,
    method: 'GET',
    params: { keyword: params?.keyword, active: params?.active, page: params?.page, size: params?.size },
  });
};

export const getBzaCommonResponseCodeSearchQueryKey = (params?: MaybeRefOrGetter<BzaCommonResponseCodeSearchParams>) => ["api", "bza", "common-catalog", "response-codes", toValue(params)] as const;

export const getBzaCommonResponseCodeSearchQueryOptions = <TData = Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaCommonResponseCodeSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaCommonResponseCodeSearchQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>> = ({ signal }) => bzaCommonResponseCodeSearch(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>, TError, TData>;
};

export type BzaCommonResponseCodeSearchQueryResult = NonNullable<Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>>;
export type BzaCommonResponseCodeSearchQueryError = unknown;

export function useBzaCommonResponseCodeSearch<TData = Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaCommonResponseCodeSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaCommonResponseCodeSearchQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaCommonResponseCodeSearch


// CPF PRE-RUNTIME FALLBACK START bzaCommonResponseCodeCreate
export type bzaCommonResponseCodeCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonResponseCodeCreateResponseSuccess = (bzaCommonResponseCodeCreateResponse200) & {
  headers: Headers;
};

export type bzaCommonResponseCodeCreateResponse = (bzaCommonResponseCodeCreateResponseSuccess)

export const getBzaCommonResponseCodeCreateUrl = () => `/api/bza/common-catalog/response-codes`;

export const bzaCommonResponseCodeCreate = async (data: CommonResponseCodeRequest, params: BzaCommonResponseCodeCreateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonResponseCodeCreateResponse> => {
  return cpfOrvalRequest<bzaCommonResponseCodeCreateResponse>(getBzaCommonResponseCodeCreateUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
    data,
  });
};

export const getBzaCommonResponseCodeCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: BzaCommonResponseCodeCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: BzaCommonResponseCodeCreateParams}, TContext> => {
  const mutationKey = ['bzaCommonResponseCodeCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>, {data: CommonResponseCodeRequest; params: BzaCommonResponseCodeCreateParams}> = (props) => {
    const { data, params } = props;
    return bzaCommonResponseCodeCreate(data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonResponseCodeCreateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>>;
export type BzaCommonResponseCodeCreateMutationBody = CommonResponseCodeRequest;
export type BzaCommonResponseCodeCreateMutationError = unknown;

export const useBzaCommonResponseCodeCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: BzaCommonResponseCodeCreateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonResponseCodeCreate>>, TError, {data: CommonResponseCodeRequest; params: BzaCommonResponseCodeCreateParams}, TContext> => {
  return useMutation(getBzaCommonResponseCodeCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonResponseCodeCreate


// CPF PRE-RUNTIME FALLBACK START bzaCommonResponseCodeDisable
export type bzaCommonResponseCodeDisableResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonResponseCodeDisableResponseSuccess = (bzaCommonResponseCodeDisableResponse200) & {
  headers: Headers;
};

export type bzaCommonResponseCodeDisableResponse = (bzaCommonResponseCodeDisableResponseSuccess)

export const getBzaCommonResponseCodeDisableUrl = (code: string) => `/api/bza/common-catalog/response-codes/${encodeURIComponent(String(code))}`;

export const bzaCommonResponseCodeDisable = async (code: string, params: BzaCommonResponseCodeDisableParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonResponseCodeDisableResponse> => {
  return cpfOrvalRequest<bzaCommonResponseCodeDisableResponse>(getBzaCommonResponseCodeDisableUrl(code), {
    ...options,
    method: 'DELETE',
    params: { expectedVersion: params.expectedVersion },
    headers: { "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
  });
};

export const getBzaCommonResponseCodeDisableMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>, TError, {code: string; params: BzaCommonResponseCodeDisableParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>, TError, {code: string; params: BzaCommonResponseCodeDisableParams}, TContext> => {
  const mutationKey = ['bzaCommonResponseCodeDisable'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>, {code: string; params: BzaCommonResponseCodeDisableParams}> = (props) => {
    const { code, params } = props;
    return bzaCommonResponseCodeDisable(code, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonResponseCodeDisableMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>>;
export type BzaCommonResponseCodeDisableMutationBody = never;
export type BzaCommonResponseCodeDisableMutationError = unknown;

export const useBzaCommonResponseCodeDisable = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>, TError, {code: string; params: BzaCommonResponseCodeDisableParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonResponseCodeDisable>>, TError, {code: string; params: BzaCommonResponseCodeDisableParams}, TContext> => {
  return useMutation(getBzaCommonResponseCodeDisableMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonResponseCodeDisable


// CPF PRE-RUNTIME FALLBACK START bzaCommonResponseCodeDetail
export type bzaCommonResponseCodeDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonResponseCodeDetailResponseSuccess = (bzaCommonResponseCodeDetailResponse200) & {
  headers: Headers;
};

export type bzaCommonResponseCodeDetailResponse = (bzaCommonResponseCodeDetailResponseSuccess)

export const getBzaCommonResponseCodeDetailUrl = (code: string) => `/api/bza/common-catalog/response-codes/${encodeURIComponent(String(code))}`;

export const bzaCommonResponseCodeDetail = async (code: string, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonResponseCodeDetailResponse> => {
  return cpfOrvalRequest<bzaCommonResponseCodeDetailResponse>(getBzaCommonResponseCodeDetailUrl(code), {
    ...options,
    method: 'GET',

  });
};

export const getBzaCommonResponseCodeDetailQueryKey = (code: MaybeRefOrGetter<string>) => ["api", "bza", "common-catalog", "response-codes", code] as const;

export const getBzaCommonResponseCodeDetailQueryOptions = <TData = Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>, TError = unknown>(
  code: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaCommonResponseCodeDetailQueryKey(toValue(code));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>> = ({ signal }) => bzaCommonResponseCodeDetail(toValue(code), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(code) !== null && toValue(code) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>, TError, TData>;
};

export type BzaCommonResponseCodeDetailQueryResult = NonNullable<Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>>;
export type BzaCommonResponseCodeDetailQueryError = unknown;

export function useBzaCommonResponseCodeDetail<TData = Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>, TError = unknown>(
  code: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeDetail>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaCommonResponseCodeDetailQueryOptions(code, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaCommonResponseCodeDetail


// CPF PRE-RUNTIME FALLBACK START bzaCommonResponseCodeUpdate
export type bzaCommonResponseCodeUpdateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonResponseCodeUpdateResponseSuccess = (bzaCommonResponseCodeUpdateResponse200) & {
  headers: Headers;
};

export type bzaCommonResponseCodeUpdateResponse = (bzaCommonResponseCodeUpdateResponseSuccess)

export const getBzaCommonResponseCodeUpdateUrl = (code: string) => `/api/bza/common-catalog/response-codes/${encodeURIComponent(String(code))}`;

export const bzaCommonResponseCodeUpdate = async (code: string, data: CommonResponseCodeRequest, params: BzaCommonResponseCodeUpdateParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonResponseCodeUpdateResponse> => {
  return cpfOrvalRequest<bzaCommonResponseCodeUpdateResponse>(getBzaCommonResponseCodeUpdateUrl(code), {
    ...options,
    method: 'PUT',
    params: { expectedVersion: params.expectedVersion },
    headers: { 'Content-Type': 'application/json', "X-CPF-Reason": params["X-CPF-Reason"], ...options?.headers },
    data,
  });
};

export const getBzaCommonResponseCodeUpdateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>, TError, {code: string; data: CommonResponseCodeRequest; params: BzaCommonResponseCodeUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>, TError, {code: string; data: CommonResponseCodeRequest; params: BzaCommonResponseCodeUpdateParams}, TContext> => {
  const mutationKey = ['bzaCommonResponseCodeUpdate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>, {code: string; data: CommonResponseCodeRequest; params: BzaCommonResponseCodeUpdateParams}> = (props) => {
    const { code, data, params } = props;
    return bzaCommonResponseCodeUpdate(code, data, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonResponseCodeUpdateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>>;
export type BzaCommonResponseCodeUpdateMutationBody = CommonResponseCodeRequest;
export type BzaCommonResponseCodeUpdateMutationError = unknown;

export const useBzaCommonResponseCodeUpdate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>, TError, {code: string; data: CommonResponseCodeRequest; params: BzaCommonResponseCodeUpdateParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonResponseCodeUpdate>>, TError, {code: string; data: CommonResponseCodeRequest; params: BzaCommonResponseCodeUpdateParams}, TContext> => {
  return useMutation(getBzaCommonResponseCodeUpdateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonResponseCodeUpdate


// CPF PRE-RUNTIME FALLBACK START bzaCommonDelete
export type bzaCommonDeleteResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonDeleteResponseSuccess = (bzaCommonDeleteResponse200) & {
  headers: Headers;
};

export type bzaCommonDeleteResponse = (bzaCommonDeleteResponseSuccess)

export const getBzaCommonDeleteUrl = (resource: string) => `/api/bza/common/${encodeURIComponent(String(resource))}`;

export const bzaCommonDelete = async (resource: string, data: CpfCommonMutation, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonDeleteResponse> => {
  return cpfOrvalRequest<bzaCommonDeleteResponse>(getBzaCommonDeleteUrl(resource), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaCommonDeleteMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDelete>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDelete>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  const mutationKey = ['bzaCommonDelete'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonDelete>>, {resource: string; data: CpfCommonMutation}> = (props) => {
    const { resource, data } = props;
    return bzaCommonDelete(resource, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonDeleteMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonDelete>>>;
export type BzaCommonDeleteMutationBody = CpfCommonMutation;
export type BzaCommonDeleteMutationError = unknown;

export const useBzaCommonDelete = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDelete>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonDelete>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  return useMutation(getBzaCommonDeleteMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonDelete


// CPF PRE-RUNTIME FALLBACK START bzaCommonSearch
export type bzaCommonSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonSearchResponseSuccess = (bzaCommonSearchResponse200) & {
  headers: Headers;
};

export type bzaCommonSearchResponse = (bzaCommonSearchResponseSuccess)

export const getBzaCommonSearchUrl = (resource: string) => `/api/bza/common/${encodeURIComponent(String(resource))}`;

export const bzaCommonSearch = async (resource: string, params?: BzaCommonSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonSearchResponse> => {
  return cpfOrvalRequest<bzaCommonSearchResponse>(getBzaCommonSearchUrl(resource), {
    ...options,
    method: 'GET',
    params: { query: params?.query, page: params?.page, size: params?.size, includeDisabled: params?.includeDisabled, effectiveAt: params?.effectiveAt },
  });
};

export const getBzaCommonSearchQueryKey = (resource: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<BzaCommonSearchParams>) => ["api", "bza", "common", resource, toValue(params)] as const;

export const getBzaCommonSearchQueryOptions = <TData = Awaited<ReturnType<typeof bzaCommonSearch>>, TError = unknown>(
  resource: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<BzaCommonSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaCommonSearchQueryKey(toValue(resource), toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaCommonSearch>>> = ({ signal }) => bzaCommonSearch(toValue(resource), toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(resource) !== null && toValue(resource) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaCommonSearch>>, TError, TData>;
};

export type BzaCommonSearchQueryResult = NonNullable<Awaited<ReturnType<typeof bzaCommonSearch>>>;
export type BzaCommonSearchQueryError = unknown;

export function useBzaCommonSearch<TData = Awaited<ReturnType<typeof bzaCommonSearch>>, TError = unknown>(
  resource: MaybeRefOrGetter<string>, params?: MaybeRefOrGetter<BzaCommonSearchParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaCommonSearch>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaCommonSearchQueryOptions(resource, params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaCommonSearch


// CPF PRE-RUNTIME FALLBACK START bzaCommonCreate
export type bzaCommonCreateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonCreateResponseSuccess = (bzaCommonCreateResponse200) & {
  headers: Headers;
};

export type bzaCommonCreateResponse = (bzaCommonCreateResponseSuccess)

export const getBzaCommonCreateUrl = (resource: string) => `/api/bza/common/${encodeURIComponent(String(resource))}`;

export const bzaCommonCreate = async (resource: string, data: CpfCommonMutation, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonCreateResponse> => {
  return cpfOrvalRequest<bzaCommonCreateResponse>(getBzaCommonCreateUrl(resource), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaCommonCreateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCreate>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCreate>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  const mutationKey = ['bzaCommonCreate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonCreate>>, {resource: string; data: CpfCommonMutation}> = (props) => {
    const { resource, data } = props;
    return bzaCommonCreate(resource, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonCreateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonCreate>>>;
export type BzaCommonCreateMutationBody = CpfCommonMutation;
export type BzaCommonCreateMutationError = unknown;

export const useBzaCommonCreate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonCreate>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonCreate>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  return useMutation(getBzaCommonCreateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonCreate


// CPF PRE-RUNTIME FALLBACK START bzaCommonUpdate
export type bzaCommonUpdateResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonUpdateResponseSuccess = (bzaCommonUpdateResponse200) & {
  headers: Headers;
};

export type bzaCommonUpdateResponse = (bzaCommonUpdateResponseSuccess)

export const getBzaCommonUpdateUrl = (resource: string) => `/api/bza/common/${encodeURIComponent(String(resource))}`;

export const bzaCommonUpdate = async (resource: string, data: CpfCommonMutation, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonUpdateResponse> => {
  return cpfOrvalRequest<bzaCommonUpdateResponse>(getBzaCommonUpdateUrl(resource), {
    ...options,
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaCommonUpdateMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonUpdate>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonUpdate>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  const mutationKey = ['bzaCommonUpdate'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonUpdate>>, {resource: string; data: CpfCommonMutation}> = (props) => {
    const { resource, data } = props;
    return bzaCommonUpdate(resource, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonUpdateMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonUpdate>>>;
export type BzaCommonUpdateMutationBody = CpfCommonMutation;
export type BzaCommonUpdateMutationError = unknown;

export const useBzaCommonUpdate = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonUpdate>>, TError, {resource: string; data: CpfCommonMutation}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonUpdate>>, TError, {resource: string; data: CpfCommonMutation}, TContext> => {
  return useMutation(getBzaCommonUpdateMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonUpdate


// CPF PRE-RUNTIME FALLBACK START bzaCommonDetail
export type bzaCommonDetailResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaCommonDetailResponseSuccess = (bzaCommonDetailResponse200) & {
  headers: Headers;
};

export type bzaCommonDetailResponse = (bzaCommonDetailResponseSuccess)

export const getBzaCommonDetailUrl = (resource: string) => `/api/bza/common/${encodeURIComponent(String(resource))}/detail`;

export const bzaCommonDetail = async (resource: string, data: BzaCommonDetailRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaCommonDetailResponse> => {
  return cpfOrvalRequest<bzaCommonDetailResponse>(getBzaCommonDetailUrl(resource), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaCommonDetailMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDetail>>, TError, {resource: string; data: BzaCommonDetailRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDetail>>, TError, {resource: string; data: BzaCommonDetailRequest}, TContext> => {
  const mutationKey = ['bzaCommonDetail'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaCommonDetail>>, {resource: string; data: BzaCommonDetailRequest}> = (props) => {
    const { resource, data } = props;
    return bzaCommonDetail(resource, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaCommonDetailMutationResult = NonNullable<Awaited<ReturnType<typeof bzaCommonDetail>>>;
export type BzaCommonDetailMutationBody = BzaCommonDetailRequest;
export type BzaCommonDetailMutationError = unknown;

export const useBzaCommonDetail = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaCommonDetail>>, TError, {resource: string; data: BzaCommonDetailRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaCommonDetail>>, TError, {resource: string; data: BzaCommonDetailRequest}, TContext> => {
  return useMutation(getBzaCommonDetailMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaCommonDetail


// CPF PRE-RUNTIME FALLBACK START bzaSupportDashboard
export type bzaSupportDashboardResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportDashboardResponseSuccess = (bzaSupportDashboardResponse200) & {
  headers: Headers;
};

export type bzaSupportDashboardResponse = (bzaSupportDashboardResponseSuccess)

export const getBzaSupportDashboardUrl = () => `/api/bza/dashboard`;

export const bzaSupportDashboard = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportDashboardResponse> => {
  return cpfOrvalRequest<bzaSupportDashboardResponse>(getBzaSupportDashboardUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaSupportDashboardQueryKey = () => ["api", "bza", "dashboard"] as const;

export const getBzaSupportDashboardQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportDashboard>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDashboard>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportDashboardQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportDashboard>>> = ({ signal }) => bzaSupportDashboard({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDashboard>>, TError, TData>;
};

export type BzaSupportDashboardQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportDashboard>>>;
export type BzaSupportDashboardQueryError = unknown;

export function useBzaSupportDashboard<TData = Awaited<ReturnType<typeof bzaSupportDashboard>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportDashboard>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportDashboardQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportDashboard


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindAssignments
export type bzaDirectoryFindAssignmentsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindAssignmentsResponseSuccess = (bzaDirectoryFindAssignmentsResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindAssignmentsResponse = (bzaDirectoryFindAssignmentsResponseSuccess)

export const getBzaDirectoryFindAssignmentsUrl = () => `/api/bza/directory/assignments`;

export const bzaDirectoryFindAssignments = async (params?: BzaDirectoryFindAssignmentsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindAssignmentsResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindAssignmentsResponse>(getBzaDirectoryFindAssignmentsUrl(), {
    ...options,
    method: 'GET',
    params: { employeeNo: params?.employeeNo, organizationCode: params?.organizationCode, effectiveAt: params?.effectiveAt },
  });
};

export const getBzaDirectoryFindAssignmentsQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsParams>) => ["api", "bza", "directory", "assignments", toValue(params)] as const;

export const getBzaDirectoryFindAssignmentsQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindAssignmentsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>> = ({ signal }) => bzaDirectoryFindAssignments(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>, TError, TData>;
};

export type BzaDirectoryFindAssignmentsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>>;
export type BzaDirectoryFindAssignmentsQueryError = unknown;

export function useBzaDirectoryFindAssignments<TData = Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignments>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindAssignmentsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindAssignments


// CPF PRE-RUNTIME FALLBACK START bzaDirectorySaveAssignment
export type bzaDirectorySaveAssignmentResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectorySaveAssignmentResponseSuccess = (bzaDirectorySaveAssignmentResponse200) & {
  headers: Headers;
};

export type bzaDirectorySaveAssignmentResponse = (bzaDirectorySaveAssignmentResponseSuccess)

export const getBzaDirectorySaveAssignmentUrl = () => `/api/bza/directory/assignments`;

export const bzaDirectorySaveAssignment = async (data: AssignmentRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectorySaveAssignmentResponse> => {
  return cpfOrvalRequest<bzaDirectorySaveAssignmentResponse>(getBzaDirectorySaveAssignmentUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaDirectorySaveAssignmentMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>, TError, {data: AssignmentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>, TError, {data: AssignmentRequest}, TContext> => {
  const mutationKey = ['bzaDirectorySaveAssignment'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>, {data: AssignmentRequest}> = (props) => {
    const { data } = props;
    return bzaDirectorySaveAssignment(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaDirectorySaveAssignmentMutationResult = NonNullable<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>>;
export type BzaDirectorySaveAssignmentMutationBody = AssignmentRequest;
export type BzaDirectorySaveAssignmentMutationError = unknown;

export const useBzaDirectorySaveAssignment = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>, TError, {data: AssignmentRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaDirectorySaveAssignment>>, TError, {data: AssignmentRequest}, TContext> => {
  return useMutation(getBzaDirectorySaveAssignmentMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaDirectorySaveAssignment


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindAssignmentsPage
export type bzaDirectoryFindAssignmentsPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindAssignmentsPageResponseSuccess = (bzaDirectoryFindAssignmentsPageResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindAssignmentsPageResponse = (bzaDirectoryFindAssignmentsPageResponseSuccess)

export const getBzaDirectoryFindAssignmentsPageUrl = () => `/api/bza/directory/assignments/page`;

export const bzaDirectoryFindAssignmentsPage = async (params?: BzaDirectoryFindAssignmentsPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindAssignmentsPageResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindAssignmentsPageResponse>(getBzaDirectoryFindAssignmentsPageUrl(), {
    ...options,
    method: 'GET',
    params: { employeeNo: params?.employeeNo, organizationCode: params?.organizationCode, effectiveAt: params?.effectiveAt, page: params?.page, size: params?.size },
  });
};

export const getBzaDirectoryFindAssignmentsPageQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsPageParams>) => ["api", "bza", "directory", "assignments", "page", toValue(params)] as const;

export const getBzaDirectoryFindAssignmentsPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindAssignmentsPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>> = ({ signal }) => bzaDirectoryFindAssignmentsPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>, TError, TData>;
};

export type BzaDirectoryFindAssignmentsPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>>;
export type BzaDirectoryFindAssignmentsPageQueryError = unknown;

export function useBzaDirectoryFindAssignmentsPage<TData = Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindAssignmentsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindAssignmentsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindAssignmentsPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindAssignmentsPage


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindJobTitles
export type bzaDirectoryFindJobTitlesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindJobTitlesResponseSuccess = (bzaDirectoryFindJobTitlesResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindJobTitlesResponse = (bzaDirectoryFindJobTitlesResponseSuccess)

export const getBzaDirectoryFindJobTitlesUrl = () => `/api/bza/directory/job-titles`;

export const bzaDirectoryFindJobTitles = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindJobTitlesResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindJobTitlesResponse>(getBzaDirectoryFindJobTitlesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaDirectoryFindJobTitlesQueryKey = () => ["api", "bza", "directory", "job-titles"] as const;

export const getBzaDirectoryFindJobTitlesQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindJobTitlesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>> = ({ signal }) => bzaDirectoryFindJobTitles({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>, TError, TData>;
};

export type BzaDirectoryFindJobTitlesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>>;
export type BzaDirectoryFindJobTitlesQueryError = unknown;

export function useBzaDirectoryFindJobTitles<TData = Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindJobTitlesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindJobTitles


// CPF PRE-RUNTIME FALLBACK START bzaDirectorySaveJobTitle
export type bzaDirectorySaveJobTitleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectorySaveJobTitleResponseSuccess = (bzaDirectorySaveJobTitleResponse200) & {
  headers: Headers;
};

export type bzaDirectorySaveJobTitleResponse = (bzaDirectorySaveJobTitleResponseSuccess)

export const getBzaDirectorySaveJobTitleUrl = () => `/api/bza/directory/job-titles`;

export const bzaDirectorySaveJobTitle = async (data: JobTitleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectorySaveJobTitleResponse> => {
  return cpfOrvalRequest<bzaDirectorySaveJobTitleResponse>(getBzaDirectorySaveJobTitleUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaDirectorySaveJobTitleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>, TError, {data: JobTitleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>, TError, {data: JobTitleRequest}, TContext> => {
  const mutationKey = ['bzaDirectorySaveJobTitle'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>, {data: JobTitleRequest}> = (props) => {
    const { data } = props;
    return bzaDirectorySaveJobTitle(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaDirectorySaveJobTitleMutationResult = NonNullable<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>>;
export type BzaDirectorySaveJobTitleMutationBody = JobTitleRequest;
export type BzaDirectorySaveJobTitleMutationError = unknown;

export const useBzaDirectorySaveJobTitle = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>, TError, {data: JobTitleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaDirectorySaveJobTitle>>, TError, {data: JobTitleRequest}, TContext> => {
  return useMutation(getBzaDirectorySaveJobTitleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaDirectorySaveJobTitle


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindJobTitlesPage
export type bzaDirectoryFindJobTitlesPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindJobTitlesPageResponseSuccess = (bzaDirectoryFindJobTitlesPageResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindJobTitlesPageResponse = (bzaDirectoryFindJobTitlesPageResponseSuccess)

export const getBzaDirectoryFindJobTitlesPageUrl = () => `/api/bza/directory/job-titles/page`;

export const bzaDirectoryFindJobTitlesPage = async (params?: BzaDirectoryFindJobTitlesPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindJobTitlesPageResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindJobTitlesPageResponse>(getBzaDirectoryFindJobTitlesPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaDirectoryFindJobTitlesPageQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindJobTitlesPageParams>) => ["api", "bza", "directory", "job-titles", "page", toValue(params)] as const;

export const getBzaDirectoryFindJobTitlesPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindJobTitlesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindJobTitlesPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>> = ({ signal }) => bzaDirectoryFindJobTitlesPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>, TError, TData>;
};

export type BzaDirectoryFindJobTitlesPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>>;
export type BzaDirectoryFindJobTitlesPageQueryError = unknown;

export function useBzaDirectoryFindJobTitlesPage<TData = Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindJobTitlesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindJobTitlesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindJobTitlesPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindJobTitlesPage


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindPositions
export type bzaDirectoryFindPositionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindPositionsResponseSuccess = (bzaDirectoryFindPositionsResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindPositionsResponse = (bzaDirectoryFindPositionsResponseSuccess)

export const getBzaDirectoryFindPositionsUrl = () => `/api/bza/directory/positions`;

export const bzaDirectoryFindPositions = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindPositionsResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindPositionsResponse>(getBzaDirectoryFindPositionsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaDirectoryFindPositionsQueryKey = () => ["api", "bza", "directory", "positions"] as const;

export const getBzaDirectoryFindPositionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindPositions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindPositionsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindPositions>>> = ({ signal }) => bzaDirectoryFindPositions({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositions>>, TError, TData>;
};

export type BzaDirectoryFindPositionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindPositions>>>;
export type BzaDirectoryFindPositionsQueryError = unknown;

export function useBzaDirectoryFindPositions<TData = Awaited<ReturnType<typeof bzaDirectoryFindPositions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindPositionsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindPositions


// CPF PRE-RUNTIME FALLBACK START bzaDirectorySavePosition
export type bzaDirectorySavePositionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectorySavePositionResponseSuccess = (bzaDirectorySavePositionResponse200) & {
  headers: Headers;
};

export type bzaDirectorySavePositionResponse = (bzaDirectorySavePositionResponseSuccess)

export const getBzaDirectorySavePositionUrl = () => `/api/bza/directory/positions`;

export const bzaDirectorySavePosition = async (data: PositionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectorySavePositionResponse> => {
  return cpfOrvalRequest<bzaDirectorySavePositionResponse>(getBzaDirectorySavePositionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaDirectorySavePositionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySavePosition>>, TError, {data: PositionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySavePosition>>, TError, {data: PositionRequest}, TContext> => {
  const mutationKey = ['bzaDirectorySavePosition'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaDirectorySavePosition>>, {data: PositionRequest}> = (props) => {
    const { data } = props;
    return bzaDirectorySavePosition(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaDirectorySavePositionMutationResult = NonNullable<Awaited<ReturnType<typeof bzaDirectorySavePosition>>>;
export type BzaDirectorySavePositionMutationBody = PositionRequest;
export type BzaDirectorySavePositionMutationError = unknown;

export const useBzaDirectorySavePosition = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySavePosition>>, TError, {data: PositionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaDirectorySavePosition>>, TError, {data: PositionRequest}, TContext> => {
  return useMutation(getBzaDirectorySavePositionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaDirectorySavePosition


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindPositionsPage
export type bzaDirectoryFindPositionsPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindPositionsPageResponseSuccess = (bzaDirectoryFindPositionsPageResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindPositionsPageResponse = (bzaDirectoryFindPositionsPageResponseSuccess)

export const getBzaDirectoryFindPositionsPageUrl = () => `/api/bza/directory/positions/page`;

export const bzaDirectoryFindPositionsPage = async (params?: BzaDirectoryFindPositionsPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindPositionsPageResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindPositionsPageResponse>(getBzaDirectoryFindPositionsPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaDirectoryFindPositionsPageQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindPositionsPageParams>) => ["api", "bza", "directory", "positions", "page", toValue(params)] as const;

export const getBzaDirectoryFindPositionsPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindPositionsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindPositionsPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>> = ({ signal }) => bzaDirectoryFindPositionsPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>, TError, TData>;
};

export type BzaDirectoryFindPositionsPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>>;
export type BzaDirectoryFindPositionsPageQueryError = unknown;

export function useBzaDirectoryFindPositionsPage<TData = Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindPositionsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindPositionsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindPositionsPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindPositionsPage


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindResponsibilities
export type bzaDirectoryFindResponsibilitiesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindResponsibilitiesResponseSuccess = (bzaDirectoryFindResponsibilitiesResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindResponsibilitiesResponse = (bzaDirectoryFindResponsibilitiesResponseSuccess)

export const getBzaDirectoryFindResponsibilitiesUrl = () => `/api/bza/directory/responsibilities`;

export const bzaDirectoryFindResponsibilities = async (params?: BzaDirectoryFindResponsibilitiesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindResponsibilitiesResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindResponsibilitiesResponse>(getBzaDirectoryFindResponsibilitiesUrl(), {
    ...options,
    method: 'GET',
    params: { organizationCode: params?.organizationCode, effectiveAt: params?.effectiveAt },
  });
};

export const getBzaDirectoryFindResponsibilitiesQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesParams>) => ["api", "bza", "directory", "responsibilities", toValue(params)] as const;

export const getBzaDirectoryFindResponsibilitiesQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindResponsibilitiesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>> = ({ signal }) => bzaDirectoryFindResponsibilities(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>, TError, TData>;
};

export type BzaDirectoryFindResponsibilitiesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>>;
export type BzaDirectoryFindResponsibilitiesQueryError = unknown;

export function useBzaDirectoryFindResponsibilities<TData = Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilities>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindResponsibilitiesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindResponsibilities


// CPF PRE-RUNTIME FALLBACK START bzaDirectorySaveResponsibility
export type bzaDirectorySaveResponsibilityResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectorySaveResponsibilityResponseSuccess = (bzaDirectorySaveResponsibilityResponse200) & {
  headers: Headers;
};

export type bzaDirectorySaveResponsibilityResponse = (bzaDirectorySaveResponsibilityResponseSuccess)

export const getBzaDirectorySaveResponsibilityUrl = () => `/api/bza/directory/responsibilities`;

export const bzaDirectorySaveResponsibility = async (data: ResponsibilityRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectorySaveResponsibilityResponse> => {
  return cpfOrvalRequest<bzaDirectorySaveResponsibilityResponse>(getBzaDirectorySaveResponsibilityUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaDirectorySaveResponsibilityMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>, TError, {data: ResponsibilityRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>, TError, {data: ResponsibilityRequest}, TContext> => {
  const mutationKey = ['bzaDirectorySaveResponsibility'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>, {data: ResponsibilityRequest}> = (props) => {
    const { data } = props;
    return bzaDirectorySaveResponsibility(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaDirectorySaveResponsibilityMutationResult = NonNullable<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>>;
export type BzaDirectorySaveResponsibilityMutationBody = ResponsibilityRequest;
export type BzaDirectorySaveResponsibilityMutationError = unknown;

export const useBzaDirectorySaveResponsibility = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>, TError, {data: ResponsibilityRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaDirectorySaveResponsibility>>, TError, {data: ResponsibilityRequest}, TContext> => {
  return useMutation(getBzaDirectorySaveResponsibilityMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaDirectorySaveResponsibility


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindResponsibilitiesPage
export type bzaDirectoryFindResponsibilitiesPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindResponsibilitiesPageResponseSuccess = (bzaDirectoryFindResponsibilitiesPageResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindResponsibilitiesPageResponse = (bzaDirectoryFindResponsibilitiesPageResponseSuccess)

export const getBzaDirectoryFindResponsibilitiesPageUrl = () => `/api/bza/directory/responsibilities/page`;

export const bzaDirectoryFindResponsibilitiesPage = async (params?: BzaDirectoryFindResponsibilitiesPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindResponsibilitiesPageResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindResponsibilitiesPageResponse>(getBzaDirectoryFindResponsibilitiesPageUrl(), {
    ...options,
    method: 'GET',
    params: { organizationCode: params?.organizationCode, effectiveAt: params?.effectiveAt, page: params?.page, size: params?.size },
  });
};

export const getBzaDirectoryFindResponsibilitiesPageQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesPageParams>) => ["api", "bza", "directory", "responsibilities", "page", toValue(params)] as const;

export const getBzaDirectoryFindResponsibilitiesPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindResponsibilitiesPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>> = ({ signal }) => bzaDirectoryFindResponsibilitiesPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>, TError, TData>;
};

export type BzaDirectoryFindResponsibilitiesPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>>;
export type BzaDirectoryFindResponsibilitiesPageQueryError = unknown;

export function useBzaDirectoryFindResponsibilitiesPage<TData = Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindResponsibilitiesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindResponsibilitiesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindResponsibilitiesPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindResponsibilitiesPage


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindUserRoles
export type bzaDirectoryFindUserRolesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindUserRolesResponseSuccess = (bzaDirectoryFindUserRolesResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindUserRolesResponse = (bzaDirectoryFindUserRolesResponseSuccess)

export const getBzaDirectoryFindUserRolesUrl = () => `/api/bza/directory/user-roles`;

export const bzaDirectoryFindUserRoles = async (params?: BzaDirectoryFindUserRolesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindUserRolesResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindUserRolesResponse>(getBzaDirectoryFindUserRolesUrl(), {
    ...options,
    method: 'GET',
    params: { loginId: params?.loginId, effectiveAt: params?.effectiveAt },
  });
};

export const getBzaDirectoryFindUserRolesQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesParams>) => ["api", "bza", "directory", "user-roles", toValue(params)] as const;

export const getBzaDirectoryFindUserRolesQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindUserRolesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>> = ({ signal }) => bzaDirectoryFindUserRoles(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>, TError, TData>;
};

export type BzaDirectoryFindUserRolesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>>;
export type BzaDirectoryFindUserRolesQueryError = unknown;

export function useBzaDirectoryFindUserRoles<TData = Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindUserRolesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindUserRoles


// CPF PRE-RUNTIME FALLBACK START bzaDirectorySaveUserRole
export type bzaDirectorySaveUserRoleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectorySaveUserRoleResponseSuccess = (bzaDirectorySaveUserRoleResponse200) & {
  headers: Headers;
};

export type bzaDirectorySaveUserRoleResponse = (bzaDirectorySaveUserRoleResponseSuccess)

export const getBzaDirectorySaveUserRoleUrl = () => `/api/bza/directory/user-roles`;

export const bzaDirectorySaveUserRole = async (data: UserRoleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectorySaveUserRoleResponse> => {
  return cpfOrvalRequest<bzaDirectorySaveUserRoleResponse>(getBzaDirectorySaveUserRoleUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaDirectorySaveUserRoleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>, TError, {data: UserRoleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>, TError, {data: UserRoleRequest}, TContext> => {
  const mutationKey = ['bzaDirectorySaveUserRole'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>, {data: UserRoleRequest}> = (props) => {
    const { data } = props;
    return bzaDirectorySaveUserRole(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaDirectorySaveUserRoleMutationResult = NonNullable<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>>;
export type BzaDirectorySaveUserRoleMutationBody = UserRoleRequest;
export type BzaDirectorySaveUserRoleMutationError = unknown;

export const useBzaDirectorySaveUserRole = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>, TError, {data: UserRoleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaDirectorySaveUserRole>>, TError, {data: UserRoleRequest}, TContext> => {
  return useMutation(getBzaDirectorySaveUserRoleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaDirectorySaveUserRole


// CPF PRE-RUNTIME FALLBACK START bzaDirectoryFindUserRolesPage
export type bzaDirectoryFindUserRolesPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaDirectoryFindUserRolesPageResponseSuccess = (bzaDirectoryFindUserRolesPageResponse200) & {
  headers: Headers;
};

export type bzaDirectoryFindUserRolesPageResponse = (bzaDirectoryFindUserRolesPageResponseSuccess)

export const getBzaDirectoryFindUserRolesPageUrl = () => `/api/bza/directory/user-roles/page`;

export const bzaDirectoryFindUserRolesPage = async (params?: BzaDirectoryFindUserRolesPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaDirectoryFindUserRolesPageResponse> => {
  return cpfOrvalRequest<bzaDirectoryFindUserRolesPageResponse>(getBzaDirectoryFindUserRolesPageUrl(), {
    ...options,
    method: 'GET',
    params: { loginId: params?.loginId, effectiveAt: params?.effectiveAt, page: params?.page, size: params?.size },
  });
};

export const getBzaDirectoryFindUserRolesPageQueryKey = (params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesPageParams>) => ["api", "bza", "directory", "user-roles", "page", toValue(params)] as const;

export const getBzaDirectoryFindUserRolesPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaDirectoryFindUserRolesPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>> = ({ signal }) => bzaDirectoryFindUserRolesPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>, TError, TData>;
};

export type BzaDirectoryFindUserRolesPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>>;
export type BzaDirectoryFindUserRolesPageQueryError = unknown;

export function useBzaDirectoryFindUserRolesPage<TData = Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaDirectoryFindUserRolesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaDirectoryFindUserRolesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaDirectoryFindUserRolesPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaDirectoryFindUserRolesPage


// CPF PRE-RUNTIME FALLBACK START bzaSupportFindDownloadAudits
export type bzaSupportFindDownloadAuditsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportFindDownloadAuditsResponseSuccess = (bzaSupportFindDownloadAuditsResponse200) & {
  headers: Headers;
};

export type bzaSupportFindDownloadAuditsResponse = (bzaSupportFindDownloadAuditsResponseSuccess)

export const getBzaSupportFindDownloadAuditsUrl = () => `/api/bza/download-audits`;

export const bzaSupportFindDownloadAudits = async (params?: BzaSupportFindDownloadAuditsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportFindDownloadAuditsResponse> => {
  return cpfOrvalRequest<bzaSupportFindDownloadAuditsResponse>(getBzaSupportFindDownloadAuditsUrl(), {
    ...options,
    method: 'GET',
    params: { limit: params?.limit },
  });
};

export const getBzaSupportFindDownloadAuditsQueryKey = (params?: MaybeRefOrGetter<BzaSupportFindDownloadAuditsParams>) => ["api", "bza", "download-audits", toValue(params)] as const;

export const getBzaSupportFindDownloadAuditsQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindDownloadAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportFindDownloadAuditsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>> = ({ signal }) => bzaSupportFindDownloadAudits(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>, TError, TData>;
};

export type BzaSupportFindDownloadAuditsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>>;
export type BzaSupportFindDownloadAuditsQueryError = unknown;

export function useBzaSupportFindDownloadAudits<TData = Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindDownloadAuditsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindDownloadAudits>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportFindDownloadAuditsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportFindDownloadAudits


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindDownloadPolicies
export type bzaOperationFindDownloadPoliciesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindDownloadPoliciesResponseSuccess = (bzaOperationFindDownloadPoliciesResponse200) & {
  headers: Headers;
};

export type bzaOperationFindDownloadPoliciesResponse = (bzaOperationFindDownloadPoliciesResponseSuccess)

export const getBzaOperationFindDownloadPoliciesUrl = () => `/api/bza/downloads`;

export const bzaOperationFindDownloadPolicies = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindDownloadPoliciesResponse> => {
  return cpfOrvalRequest<bzaOperationFindDownloadPoliciesResponse>(getBzaOperationFindDownloadPoliciesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindDownloadPoliciesQueryKey = () => ["api", "bza", "downloads"] as const;

export const getBzaOperationFindDownloadPoliciesQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindDownloadPoliciesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>> = ({ signal }) => bzaOperationFindDownloadPolicies({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>, TError, TData>;
};

export type BzaOperationFindDownloadPoliciesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>>;
export type BzaOperationFindDownloadPoliciesQueryError = unknown;

export function useBzaOperationFindDownloadPolicies<TData = Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindDownloadPolicies>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindDownloadPoliciesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindDownloadPolicies


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindMenus
export type bzaOperationFindMenusResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindMenusResponseSuccess = (bzaOperationFindMenusResponse200) & {
  headers: Headers;
};

export type bzaOperationFindMenusResponse = (bzaOperationFindMenusResponseSuccess)

export const getBzaOperationFindMenusUrl = () => `/api/bza/menus`;

export const bzaOperationFindMenus = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindMenusResponse> => {
  return cpfOrvalRequest<bzaOperationFindMenusResponse>(getBzaOperationFindMenusUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindMenusQueryKey = () => ["api", "bza", "menus"] as const;

export const getBzaOperationFindMenusQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindMenusQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindMenus>>> = ({ signal }) => bzaOperationFindMenus({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenus>>, TError, TData>;
};

export type BzaOperationFindMenusQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindMenus>>>;
export type BzaOperationFindMenusQueryError = unknown;

export function useBzaOperationFindMenus<TData = Awaited<ReturnType<typeof bzaOperationFindMenus>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenus>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindMenusQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindMenus


// CPF PRE-RUNTIME FALLBACK START bzaOperationSaveMenu
export type bzaOperationSaveMenuResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationSaveMenuResponseSuccess = (bzaOperationSaveMenuResponse200) & {
  headers: Headers;
};

export type bzaOperationSaveMenuResponse = (bzaOperationSaveMenuResponseSuccess)

export const getBzaOperationSaveMenuUrl = () => `/api/bza/menus`;

export const bzaOperationSaveMenu = async (data: MenuRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationSaveMenuResponse> => {
  return cpfOrvalRequest<bzaOperationSaveMenuResponse>(getBzaOperationSaveMenuUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaOperationSaveMenuMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveMenu>>, TError, {data: MenuRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveMenu>>, TError, {data: MenuRequest}, TContext> => {
  const mutationKey = ['bzaOperationSaveMenu'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaOperationSaveMenu>>, {data: MenuRequest}> = (props) => {
    const { data } = props;
    return bzaOperationSaveMenu(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaOperationSaveMenuMutationResult = NonNullable<Awaited<ReturnType<typeof bzaOperationSaveMenu>>>;
export type BzaOperationSaveMenuMutationBody = MenuRequest;
export type BzaOperationSaveMenuMutationError = unknown;

export const useBzaOperationSaveMenu = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveMenu>>, TError, {data: MenuRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaOperationSaveMenu>>, TError, {data: MenuRequest}, TContext> => {
  return useMutation(getBzaOperationSaveMenuMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaOperationSaveMenu


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindMenusPage
export type bzaOperationFindMenusPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindMenusPageResponseSuccess = (bzaOperationFindMenusPageResponse200) & {
  headers: Headers;
};

export type bzaOperationFindMenusPageResponse = (bzaOperationFindMenusPageResponseSuccess)

export const getBzaOperationFindMenusPageUrl = () => `/api/bza/menus/page`;

export const bzaOperationFindMenusPage = async (params?: BzaOperationFindMenusPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindMenusPageResponse> => {
  return cpfOrvalRequest<bzaOperationFindMenusPageResponse>(getBzaOperationFindMenusPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaOperationFindMenusPageQueryKey = (params?: MaybeRefOrGetter<BzaOperationFindMenusPageParams>) => ["api", "bza", "menus", "page", toValue(params)] as const;

export const getBzaOperationFindMenusPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindMenusPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindMenusPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenusPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindMenusPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindMenusPage>>> = ({ signal }) => bzaOperationFindMenusPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenusPage>>, TError, TData>;
};

export type BzaOperationFindMenusPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindMenusPage>>>;
export type BzaOperationFindMenusPageQueryError = unknown;

export function useBzaOperationFindMenusPage<TData = Awaited<ReturnType<typeof bzaOperationFindMenusPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindMenusPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenusPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindMenusPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindMenusPage


// CPF PRE-RUNTIME FALLBACK START bzaOperationDeleteMenu
export type bzaOperationDeleteMenuResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationDeleteMenuResponseSuccess = (bzaOperationDeleteMenuResponse200) & {
  headers: Headers;
};

export type bzaOperationDeleteMenuResponse = (bzaOperationDeleteMenuResponseSuccess)

export const getBzaOperationDeleteMenuUrl = (menuCode: string) => `/api/bza/menus/${encodeURIComponent(String(menuCode))}`;

export const bzaOperationDeleteMenu = async (menuCode: string, data: MenuDeleteRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationDeleteMenuResponse> => {
  return cpfOrvalRequest<bzaOperationDeleteMenuResponse>(getBzaOperationDeleteMenuUrl(menuCode), {
    ...options,
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaOperationDeleteMenuMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>, TError, {menuCode: string; data: MenuDeleteRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>, TError, {menuCode: string; data: MenuDeleteRequest}, TContext> => {
  const mutationKey = ['bzaOperationDeleteMenu'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>, {menuCode: string; data: MenuDeleteRequest}> = (props) => {
    const { menuCode, data } = props;
    return bzaOperationDeleteMenu(menuCode, data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaOperationDeleteMenuMutationResult = NonNullable<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>>;
export type BzaOperationDeleteMenuMutationBody = MenuDeleteRequest;
export type BzaOperationDeleteMenuMutationError = unknown;

export const useBzaOperationDeleteMenu = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>, TError, {menuCode: string; data: MenuDeleteRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaOperationDeleteMenu>>, TError, {menuCode: string; data: MenuDeleteRequest}, TContext> => {
  return useMutation(getBzaOperationDeleteMenuMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaOperationDeleteMenu


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindMenuImpact
export type bzaOperationFindMenuImpactResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindMenuImpactResponseSuccess = (bzaOperationFindMenuImpactResponse200) & {
  headers: Headers;
};

export type bzaOperationFindMenuImpactResponse = (bzaOperationFindMenuImpactResponseSuccess)

export const getBzaOperationFindMenuImpactUrl = (menuCode: string) => `/api/bza/menus/${encodeURIComponent(String(menuCode))}/impact`;

export const bzaOperationFindMenuImpact = async (menuCode: string, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindMenuImpactResponse> => {
  return cpfOrvalRequest<bzaOperationFindMenuImpactResponse>(getBzaOperationFindMenuImpactUrl(menuCode), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindMenuImpactQueryKey = (menuCode: MaybeRefOrGetter<string>) => ["api", "bza", "menus", menuCode, "impact"] as const;

export const getBzaOperationFindMenuImpactQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>, TError = unknown>(
  menuCode: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindMenuImpactQueryKey(toValue(menuCode));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>> = ({ signal }) => bzaOperationFindMenuImpact(toValue(menuCode), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(menuCode) !== null && toValue(menuCode) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>, TError, TData>;
};

export type BzaOperationFindMenuImpactQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>>;
export type BzaOperationFindMenuImpactQueryError = unknown;

export function useBzaOperationFindMenuImpact<TData = Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>, TError = unknown>(
  menuCode: MaybeRefOrGetter<string>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindMenuImpact>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindMenuImpactQueryOptions(menuCode, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindMenuImpact


// CPF PRE-RUNTIME FALLBACK START bzaSupportFindNotifications
export type bzaSupportFindNotificationsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportFindNotificationsResponseSuccess = (bzaSupportFindNotificationsResponse200) & {
  headers: Headers;
};

export type bzaSupportFindNotificationsResponse = (bzaSupportFindNotificationsResponseSuccess)

export const getBzaSupportFindNotificationsUrl = () => `/api/bza/notifications`;

export const bzaSupportFindNotifications = async (params?: BzaSupportFindNotificationsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportFindNotificationsResponse> => {
  return cpfOrvalRequest<bzaSupportFindNotificationsResponse>(getBzaSupportFindNotificationsUrl(), {
    ...options,
    method: 'GET',
    params: { unreadOnly: params?.unreadOnly, limit: params?.limit },
  });
};

export const getBzaSupportFindNotificationsQueryKey = (params?: MaybeRefOrGetter<BzaSupportFindNotificationsParams>) => ["api", "bza", "notifications", toValue(params)] as const;

export const getBzaSupportFindNotificationsQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportFindNotifications>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindNotificationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindNotifications>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportFindNotificationsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportFindNotifications>>> = ({ signal }) => bzaSupportFindNotifications(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindNotifications>>, TError, TData>;
};

export type BzaSupportFindNotificationsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportFindNotifications>>>;
export type BzaSupportFindNotificationsQueryError = unknown;

export function useBzaSupportFindNotifications<TData = Awaited<ReturnType<typeof bzaSupportFindNotifications>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindNotificationsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindNotifications>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportFindNotificationsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportFindNotifications


// CPF PRE-RUNTIME FALLBACK START bzaSupportCreateNotification
export type bzaSupportCreateNotificationResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportCreateNotificationResponseSuccess = (bzaSupportCreateNotificationResponse200) & {
  headers: Headers;
};

export type bzaSupportCreateNotificationResponse = (bzaSupportCreateNotificationResponseSuccess)

export const getBzaSupportCreateNotificationUrl = () => `/api/bza/notifications`;

export const bzaSupportCreateNotification = async (data: NotificationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportCreateNotificationResponse> => {
  return cpfOrvalRequest<bzaSupportCreateNotificationResponse>(getBzaSupportCreateNotificationUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaSupportCreateNotificationMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportCreateNotification>>, TError, {data: NotificationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportCreateNotification>>, TError, {data: NotificationRequest}, TContext> => {
  const mutationKey = ['bzaSupportCreateNotification'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportCreateNotification>>, {data: NotificationRequest}> = (props) => {
    const { data } = props;
    return bzaSupportCreateNotification(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportCreateNotificationMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportCreateNotification>>>;
export type BzaSupportCreateNotificationMutationBody = NotificationRequest;
export type BzaSupportCreateNotificationMutationError = unknown;

export const useBzaSupportCreateNotification = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportCreateNotification>>, TError, {data: NotificationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportCreateNotification>>, TError, {data: NotificationRequest}, TContext> => {
  return useMutation(getBzaSupportCreateNotificationMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportCreateNotification


// CPF PRE-RUNTIME FALLBACK START bzaSupportReadAllNotifications
export type bzaSupportReadAllNotificationsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportReadAllNotificationsResponseSuccess = (bzaSupportReadAllNotificationsResponse200) & {
  headers: Headers;
};

export type bzaSupportReadAllNotificationsResponse = (bzaSupportReadAllNotificationsResponseSuccess)

export const getBzaSupportReadAllNotificationsUrl = () => `/api/bza/notifications/read-all`;

export const bzaSupportReadAllNotifications = async (params: BzaSupportReadAllNotificationsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportReadAllNotificationsResponse> => {
  return cpfOrvalRequest<bzaSupportReadAllNotificationsResponse>(getBzaSupportReadAllNotificationsUrl(), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getBzaSupportReadAllNotificationsMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>, TError, {params: BzaSupportReadAllNotificationsParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>, TError, {params: BzaSupportReadAllNotificationsParams}, TContext> => {
  const mutationKey = ['bzaSupportReadAllNotifications'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>, {params: BzaSupportReadAllNotificationsParams}> = (props) => {
    const { params } = props;
    return bzaSupportReadAllNotifications(params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportReadAllNotificationsMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>>;
export type BzaSupportReadAllNotificationsMutationBody = never;
export type BzaSupportReadAllNotificationsMutationError = unknown;

export const useBzaSupportReadAllNotifications = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>, TError, {params: BzaSupportReadAllNotificationsParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportReadAllNotifications>>, TError, {params: BzaSupportReadAllNotificationsParams}, TContext> => {
  return useMutation(getBzaSupportReadAllNotificationsMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportReadAllNotifications


// CPF PRE-RUNTIME FALLBACK START bzaSupportReadNotification
export type bzaSupportReadNotificationResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportReadNotificationResponseSuccess = (bzaSupportReadNotificationResponse200) & {
  headers: Headers;
};

export type bzaSupportReadNotificationResponse = (bzaSupportReadNotificationResponseSuccess)

export const getBzaSupportReadNotificationUrl = (notificationId: number) => `/api/bza/notifications/${encodeURIComponent(String(notificationId))}/read`;

export const bzaSupportReadNotification = async (notificationId: number, params: BzaSupportReadNotificationParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportReadNotificationResponse> => {
  return cpfOrvalRequest<bzaSupportReadNotificationResponse>(getBzaSupportReadNotificationUrl(notificationId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getBzaSupportReadNotificationMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadNotification>>, TError, {notificationId: number; params: BzaSupportReadNotificationParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadNotification>>, TError, {notificationId: number; params: BzaSupportReadNotificationParams}, TContext> => {
  const mutationKey = ['bzaSupportReadNotification'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportReadNotification>>, {notificationId: number; params: BzaSupportReadNotificationParams}> = (props) => {
    const { notificationId, params } = props;
    return bzaSupportReadNotification(notificationId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportReadNotificationMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportReadNotification>>>;
export type BzaSupportReadNotificationMutationBody = never;
export type BzaSupportReadNotificationMutationError = unknown;

export const useBzaSupportReadNotification = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportReadNotification>>, TError, {notificationId: number; params: BzaSupportReadNotificationParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportReadNotification>>, TError, {notificationId: number; params: BzaSupportReadNotificationParams}, TContext> => {
  return useMutation(getBzaSupportReadNotificationMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportReadNotification


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindPermissions
export type bzaOperationFindPermissionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindPermissionsResponseSuccess = (bzaOperationFindPermissionsResponse200) & {
  headers: Headers;
};

export type bzaOperationFindPermissionsResponse = (bzaOperationFindPermissionsResponseSuccess)

export const getBzaOperationFindPermissionsUrl = () => `/api/bza/permissions`;

export const bzaOperationFindPermissions = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindPermissionsResponse> => {
  return cpfOrvalRequest<bzaOperationFindPermissionsResponse>(getBzaOperationFindPermissionsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindPermissionsQueryKey = () => ["api", "bza", "permissions"] as const;

export const getBzaOperationFindPermissionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindPermissions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindPermissionsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindPermissions>>> = ({ signal }) => bzaOperationFindPermissions({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissions>>, TError, TData>;
};

export type BzaOperationFindPermissionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindPermissions>>>;
export type BzaOperationFindPermissionsQueryError = unknown;

export function useBzaOperationFindPermissions<TData = Awaited<ReturnType<typeof bzaOperationFindPermissions>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindPermissionsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindPermissions


// CPF PRE-RUNTIME FALLBACK START bzaOperationSavePermission
export type bzaOperationSavePermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationSavePermissionResponseSuccess = (bzaOperationSavePermissionResponse200) & {
  headers: Headers;
};

export type bzaOperationSavePermissionResponse = (bzaOperationSavePermissionResponseSuccess)

export const getBzaOperationSavePermissionUrl = () => `/api/bza/permissions`;

export const bzaOperationSavePermission = async (data: PermissionRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationSavePermissionResponse> => {
  return cpfOrvalRequest<bzaOperationSavePermissionResponse>(getBzaOperationSavePermissionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaOperationSavePermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSavePermission>>, TError, {data: PermissionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSavePermission>>, TError, {data: PermissionRequest}, TContext> => {
  const mutationKey = ['bzaOperationSavePermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaOperationSavePermission>>, {data: PermissionRequest}> = (props) => {
    const { data } = props;
    return bzaOperationSavePermission(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaOperationSavePermissionMutationResult = NonNullable<Awaited<ReturnType<typeof bzaOperationSavePermission>>>;
export type BzaOperationSavePermissionMutationBody = PermissionRequest;
export type BzaOperationSavePermissionMutationError = unknown;

export const useBzaOperationSavePermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSavePermission>>, TError, {data: PermissionRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaOperationSavePermission>>, TError, {data: PermissionRequest}, TContext> => {
  return useMutation(getBzaOperationSavePermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaOperationSavePermission


// CPF PRE-RUNTIME FALLBACK START bzaSupportCompareRolePermissions
export type bzaSupportCompareRolePermissionsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportCompareRolePermissionsResponseSuccess = (bzaSupportCompareRolePermissionsResponse200) & {
  headers: Headers;
};

export type bzaSupportCompareRolePermissionsResponse = (bzaSupportCompareRolePermissionsResponseSuccess)

export const getBzaSupportCompareRolePermissionsUrl = () => `/api/bza/permissions/compare`;

export const bzaSupportCompareRolePermissions = async (params: BzaSupportCompareRolePermissionsParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportCompareRolePermissionsResponse> => {
  return cpfOrvalRequest<bzaSupportCompareRolePermissionsResponse>(getBzaSupportCompareRolePermissionsUrl(), {
    ...options,
    method: 'GET',
    params: { leftRoleCode: params.leftRoleCode, rightRoleCode: params.rightRoleCode },
  });
};

export const getBzaSupportCompareRolePermissionsQueryKey = (params: MaybeRefOrGetter<BzaSupportCompareRolePermissionsParams>) => ["api", "bza", "permissions", "compare", toValue(params)] as const;

export const getBzaSupportCompareRolePermissionsQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaSupportCompareRolePermissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportCompareRolePermissionsQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>> = ({ signal }) => bzaSupportCompareRolePermissions(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, enabled: computed(() => toValue(params) !== null && toValue(params) !== undefined), ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>, TError, TData>;
};

export type BzaSupportCompareRolePermissionsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>>;
export type BzaSupportCompareRolePermissionsQueryError = unknown;

export function useBzaSupportCompareRolePermissions<TData = Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>, TError = unknown>(
  params: MaybeRefOrGetter<BzaSupportCompareRolePermissionsParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportCompareRolePermissions>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportCompareRolePermissionsQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportCompareRolePermissions


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindPermissionsPage
export type bzaOperationFindPermissionsPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindPermissionsPageResponseSuccess = (bzaOperationFindPermissionsPageResponse200) & {
  headers: Headers;
};

export type bzaOperationFindPermissionsPageResponse = (bzaOperationFindPermissionsPageResponseSuccess)

export const getBzaOperationFindPermissionsPageUrl = () => `/api/bza/permissions/page`;

export const bzaOperationFindPermissionsPage = async (params?: BzaOperationFindPermissionsPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindPermissionsPageResponse> => {
  return cpfOrvalRequest<bzaOperationFindPermissionsPageResponse>(getBzaOperationFindPermissionsPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaOperationFindPermissionsPageQueryKey = (params?: MaybeRefOrGetter<BzaOperationFindPermissionsPageParams>) => ["api", "bza", "permissions", "page", toValue(params)] as const;

export const getBzaOperationFindPermissionsPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindPermissionsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindPermissionsPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>> = ({ signal }) => bzaOperationFindPermissionsPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>, TError, TData>;
};

export type BzaOperationFindPermissionsPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>>;
export type BzaOperationFindPermissionsPageQueryError = unknown;

export function useBzaOperationFindPermissionsPage<TData = Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindPermissionsPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindPermissionsPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindPermissionsPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindPermissionsPage


// CPF PRE-RUNTIME FALLBACK START bzaSupportSimulatePermission
export type bzaSupportSimulatePermissionResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportSimulatePermissionResponseSuccess = (bzaSupportSimulatePermissionResponse200) & {
  headers: Headers;
};

export type bzaSupportSimulatePermissionResponse = (bzaSupportSimulatePermissionResponseSuccess)

export const getBzaSupportSimulatePermissionUrl = () => `/api/bza/permissions/simulate`;

export const bzaSupportSimulatePermission = async (data: PermissionSimulationRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportSimulatePermissionResponse> => {
  return cpfOrvalRequest<bzaSupportSimulatePermissionResponse>(getBzaSupportSimulatePermissionUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaSupportSimulatePermissionMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>, TError, {data: PermissionSimulationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>, TError, {data: PermissionSimulationRequest}, TContext> => {
  const mutationKey = ['bzaSupportSimulatePermission'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>, {data: PermissionSimulationRequest}> = (props) => {
    const { data } = props;
    return bzaSupportSimulatePermission(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportSimulatePermissionMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>>;
export type BzaSupportSimulatePermissionMutationBody = PermissionSimulationRequest;
export type BzaSupportSimulatePermissionMutationError = unknown;

export const useBzaSupportSimulatePermission = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>, TError, {data: PermissionSimulationRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportSimulatePermission>>, TError, {data: PermissionSimulationRequest}, TContext> => {
  return useMutation(getBzaSupportSimulatePermissionMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportSimulatePermission


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindRoles
export type bzaOperationFindRolesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindRolesResponseSuccess = (bzaOperationFindRolesResponse200) & {
  headers: Headers;
};

export type bzaOperationFindRolesResponse = (bzaOperationFindRolesResponseSuccess)

export const getBzaOperationFindRolesUrl = () => `/api/bza/roles`;

export const bzaOperationFindRoles = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindRolesResponse> => {
  return cpfOrvalRequest<bzaOperationFindRolesResponse>(getBzaOperationFindRolesUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindRolesQueryKey = () => ["api", "bza", "roles"] as const;

export const getBzaOperationFindRolesQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindRolesQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindRoles>>> = ({ signal }) => bzaOperationFindRoles({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRoles>>, TError, TData>;
};

export type BzaOperationFindRolesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindRoles>>>;
export type BzaOperationFindRolesQueryError = unknown;

export function useBzaOperationFindRoles<TData = Awaited<ReturnType<typeof bzaOperationFindRoles>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRoles>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindRolesQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindRoles


// CPF PRE-RUNTIME FALLBACK START bzaOperationSaveRole
export type bzaOperationSaveRoleResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationSaveRoleResponseSuccess = (bzaOperationSaveRoleResponse200) & {
  headers: Headers;
};

export type bzaOperationSaveRoleResponse = (bzaOperationSaveRoleResponseSuccess)

export const getBzaOperationSaveRoleUrl = () => `/api/bza/roles`;

export const bzaOperationSaveRole = async (data: RoleRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationSaveRoleResponse> => {
  return cpfOrvalRequest<bzaOperationSaveRoleResponse>(getBzaOperationSaveRoleUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaOperationSaveRoleMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveRole>>, TError, {data: RoleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveRole>>, TError, {data: RoleRequest}, TContext> => {
  const mutationKey = ['bzaOperationSaveRole'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaOperationSaveRole>>, {data: RoleRequest}> = (props) => {
    const { data } = props;
    return bzaOperationSaveRole(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaOperationSaveRoleMutationResult = NonNullable<Awaited<ReturnType<typeof bzaOperationSaveRole>>>;
export type BzaOperationSaveRoleMutationBody = RoleRequest;
export type BzaOperationSaveRoleMutationError = unknown;

export const useBzaOperationSaveRole = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaOperationSaveRole>>, TError, {data: RoleRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaOperationSaveRole>>, TError, {data: RoleRequest}, TContext> => {
  return useMutation(getBzaOperationSaveRoleMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaOperationSaveRole


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindRolesPage
export type bzaOperationFindRolesPageResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindRolesPageResponseSuccess = (bzaOperationFindRolesPageResponse200) & {
  headers: Headers;
};

export type bzaOperationFindRolesPageResponse = (bzaOperationFindRolesPageResponseSuccess)

export const getBzaOperationFindRolesPageUrl = () => `/api/bza/roles/page`;

export const bzaOperationFindRolesPage = async (params?: BzaOperationFindRolesPageParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindRolesPageResponse> => {
  return cpfOrvalRequest<bzaOperationFindRolesPageResponse>(getBzaOperationFindRolesPageUrl(), {
    ...options,
    method: 'GET',
    params: { page: params?.page, size: params?.size },
  });
};

export const getBzaOperationFindRolesPageQueryKey = (params?: MaybeRefOrGetter<BzaOperationFindRolesPageParams>) => ["api", "bza", "roles", "page", toValue(params)] as const;

export const getBzaOperationFindRolesPageQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindRolesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindRolesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRolesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindRolesPageQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindRolesPage>>> = ({ signal }) => bzaOperationFindRolesPage(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRolesPage>>, TError, TData>;
};

export type BzaOperationFindRolesPageQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindRolesPage>>>;
export type BzaOperationFindRolesPageQueryError = unknown;

export function useBzaOperationFindRolesPage<TData = Awaited<ReturnType<typeof bzaOperationFindRolesPage>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaOperationFindRolesPageParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindRolesPage>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindRolesPageQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindRolesPage


// CPF PRE-RUNTIME FALLBACK START bzaSupportFindSavedSearches
export type bzaSupportFindSavedSearchesResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportFindSavedSearchesResponseSuccess = (bzaSupportFindSavedSearchesResponse200) & {
  headers: Headers;
};

export type bzaSupportFindSavedSearchesResponse = (bzaSupportFindSavedSearchesResponseSuccess)

export const getBzaSupportFindSavedSearchesUrl = () => `/api/bza/saved-searches`;

export const bzaSupportFindSavedSearches = async (params?: BzaSupportFindSavedSearchesParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportFindSavedSearchesResponse> => {
  return cpfOrvalRequest<bzaSupportFindSavedSearchesResponse>(getBzaSupportFindSavedSearchesUrl(), {
    ...options,
    method: 'GET',
    params: { screenCode: params?.screenCode },
  });
};

export const getBzaSupportFindSavedSearchesQueryKey = (params?: MaybeRefOrGetter<BzaSupportFindSavedSearchesParams>) => ["api", "bza", "saved-searches", toValue(params)] as const;

export const getBzaSupportFindSavedSearchesQueryOptions = <TData = Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindSavedSearchesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaSupportFindSavedSearchesQueryKey(toValue(params));
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>> = ({ signal }) => bzaSupportFindSavedSearches(toValue(params), { signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>, TError, TData>;
};

export type BzaSupportFindSavedSearchesQueryResult = NonNullable<Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>>;
export type BzaSupportFindSavedSearchesQueryError = unknown;

export function useBzaSupportFindSavedSearches<TData = Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>, TError = unknown>(
  params?: MaybeRefOrGetter<BzaSupportFindSavedSearchesParams>, options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaSupportFindSavedSearches>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaSupportFindSavedSearchesQueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaSupportFindSavedSearches


// CPF PRE-RUNTIME FALLBACK START bzaSupportSaveSavedSearch
export type bzaSupportSaveSavedSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportSaveSavedSearchResponseSuccess = (bzaSupportSaveSavedSearchResponse200) & {
  headers: Headers;
};

export type bzaSupportSaveSavedSearchResponse = (bzaSupportSaveSavedSearchResponseSuccess)

export const getBzaSupportSaveSavedSearchUrl = () => `/api/bza/saved-searches`;

export const bzaSupportSaveSavedSearch = async (data: SavedSearchRequest, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportSaveSavedSearchResponse> => {
  return cpfOrvalRequest<bzaSupportSaveSavedSearchResponse>(getBzaSupportSaveSavedSearchUrl(), {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    data,
  });
};

export const getBzaSupportSaveSavedSearchMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>, TError, {data: SavedSearchRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>, TError, {data: SavedSearchRequest}, TContext> => {
  const mutationKey = ['bzaSupportSaveSavedSearch'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>, {data: SavedSearchRequest}> = (props) => {
    const { data } = props;
    return bzaSupportSaveSavedSearch(data, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportSaveSavedSearchMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>>;
export type BzaSupportSaveSavedSearchMutationBody = SavedSearchRequest;
export type BzaSupportSaveSavedSearchMutationError = unknown;

export const useBzaSupportSaveSavedSearch = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>, TError, {data: SavedSearchRequest}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportSaveSavedSearch>>, TError, {data: SavedSearchRequest}, TContext> => {
  return useMutation(getBzaSupportSaveSavedSearchMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportSaveSavedSearch


// CPF PRE-RUNTIME FALLBACK START bzaSupportDisableSavedSearch
export type bzaSupportDisableSavedSearchResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaSupportDisableSavedSearchResponseSuccess = (bzaSupportDisableSavedSearchResponse200) & {
  headers: Headers;
};

export type bzaSupportDisableSavedSearchResponse = (bzaSupportDisableSavedSearchResponseSuccess)

export const getBzaSupportDisableSavedSearchUrl = (savedSearchId: number) => `/api/bza/saved-searches/${encodeURIComponent(String(savedSearchId))}/disable`;

export const bzaSupportDisableSavedSearch = async (savedSearchId: number, params: BzaSupportDisableSavedSearchParams, options?: CpfOrvalGeneratedRequestOptions): Promise<bzaSupportDisableSavedSearchResponse> => {
  return cpfOrvalRequest<bzaSupportDisableSavedSearchResponse>(getBzaSupportDisableSavedSearchUrl(savedSearchId), {
    ...options,
    method: 'POST',
    params: { reason: params.reason },
  });
};

export const getBzaSupportDisableSavedSearchMutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>, TError, {savedSearchId: number; params: BzaSupportDisableSavedSearchParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>, TError, {savedSearchId: number; params: BzaSupportDisableSavedSearchParams}, TContext> => {
  const mutationKey = ['bzaSupportDisableSavedSearch'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>, {savedSearchId: number; params: BzaSupportDisableSavedSearchParams}> = (props) => {
    const { savedSearchId, params } = props;
    return bzaSupportDisableSavedSearch(savedSearchId, params, requestOptions);
  };
  return { mutationFn, ...mutationOptions };
};

export type BzaSupportDisableSavedSearchMutationResult = NonNullable<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>>;
export type BzaSupportDisableSavedSearchMutationBody = never;
export type BzaSupportDisableSavedSearchMutationError = unknown;

export const useBzaSupportDisableSavedSearch = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>, TError, {savedSearchId: number; params: BzaSupportDisableSavedSearchParams}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof bzaSupportDisableSavedSearch>>, TError, {savedSearchId: number; params: BzaSupportDisableSavedSearchParams}, TContext> => {
  return useMutation(getBzaSupportDisableSavedSearchMutationOptions(options), queryClient);
};
// CPF PRE-RUNTIME FALLBACK END bzaSupportDisableSavedSearch


// CPF PRE-RUNTIME FALLBACK START bzaOperationFindSettings
export type bzaOperationFindSettingsResponse200 = {
  data: CpfControllerSourceResponse
  status: 200
}

export type bzaOperationFindSettingsResponseSuccess = (bzaOperationFindSettingsResponse200) & {
  headers: Headers;
};

export type bzaOperationFindSettingsResponse = (bzaOperationFindSettingsResponseSuccess)

export const getBzaOperationFindSettingsUrl = () => `/api/bza/settings`;

export const bzaOperationFindSettings = async (options?: CpfOrvalGeneratedRequestOptions): Promise<bzaOperationFindSettingsResponse> => {
  return cpfOrvalRequest<bzaOperationFindSettingsResponse>(getBzaOperationFindSettingsUrl(), {
    ...options,
    method: 'GET',

  });
};

export const getBzaOperationFindSettingsQueryKey = () => ["api", "bza", "settings"] as const;

export const getBzaOperationFindSettingsQueryOptions = <TData = Awaited<ReturnType<typeof bzaOperationFindSettings>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindSettings>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = getBzaOperationFindSettingsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof bzaOperationFindSettings>>> = ({ signal }) => bzaOperationFindSettings({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindSettings>>, TError, TData>;
};

export type BzaOperationFindSettingsQueryResult = NonNullable<Awaited<ReturnType<typeof bzaOperationFindSettings>>>;
export type BzaOperationFindSettingsQueryError = unknown;

export function useBzaOperationFindSettings<TData = Awaited<ReturnType<typeof bzaOperationFindSettings>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof bzaOperationFindSettings>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getBzaOperationFindSettingsQueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}
// CPF PRE-RUNTIME FALLBACK END bzaOperationFindSettings
