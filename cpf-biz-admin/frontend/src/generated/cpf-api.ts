// GENERATED FROM canonical openapi/cpf-openapi.json. DO NOT EDIT.
// CPF_CANONICAL_ORVAL_DELEGATE: application-facing compatibility surface delegates every operation to the verified Orval client.
import { bzaApprovalCancel as orvalBzaApprovalCancel, bzaApprovalDelegations as orvalBzaApprovalDelegations, bzaApprovalDelegationSave as orvalBzaApprovalDelegationSave, bzaApprovalExpireDue as orvalBzaApprovalExpireDue, bzaApprovalInbox as orvalBzaApprovalInbox, bzaApprovalParticipantDecision as orvalBzaApprovalParticipantDecision, bzaApprovalPolicies as orvalBzaApprovalPolicies, bzaApprovalPolicyDetail as orvalBzaApprovalPolicyDetail, bzaApprovalPolicySave as orvalBzaApprovalPolicySave, bzaApprovalPolicySimulate as orvalBzaApprovalPolicySimulate, bzaApprovalPolicySubmit as orvalBzaApprovalPolicySubmit, bzaApprovalResubmit as orvalBzaApprovalResubmit, bzaApprovalSubmissionDetail as orvalBzaApprovalSubmissionDetail, bzaApprovalSubmissions as orvalBzaApprovalSubmissions, bzaApprovalWithdraw as orvalBzaApprovalWithdraw, bzaAuthChangePassword as orvalBzaAuthChangePassword, bzaAuthLogin as orvalBzaAuthLogin, bzaAuthLoginHistories as orvalBzaAuthLoginHistories, bzaAuthLogout as orvalBzaAuthLogout, bzaAuthMe as orvalBzaAuthMe, bzaAuthRefresh as orvalBzaAuthRefresh, bzaAuthRevokeSession as orvalBzaAuthRevokeSession, bzaAuthSessions as orvalBzaAuthSessions, bzaBackofficeEmployeeRawContact as orvalBzaBackofficeEmployeeRawContact, bzaBackofficeFindBusinessAudits as orvalBzaBackofficeFindBusinessAudits, bzaBackofficeFindEffectivePermissions as orvalBzaBackofficeFindEffectivePermissions, bzaBackofficeFindEmployees as orvalBzaBackofficeFindEmployees, bzaBackofficeFindEmployeesPage as orvalBzaBackofficeFindEmployeesPage, bzaBackofficeFindOrganizations as orvalBzaBackofficeFindOrganizations, bzaBackofficeFindOrganizationsPage as orvalBzaBackofficeFindOrganizationsPage, bzaBackofficeSaveEmployee as orvalBzaBackofficeSaveEmployee, bzaBackofficeSaveOrganization as orvalBzaBackofficeSaveOrganization, bzaBusinessAuditVerify as orvalBzaBusinessAuditVerify, bzaCommonCatalogRefresh as orvalBzaCommonCatalogRefresh, bzaCommonCreate as orvalBzaCommonCreate, bzaCommonDelete as orvalBzaCommonDelete, bzaCommonDetail as orvalBzaCommonDetail, bzaCommonMessageCreate as orvalBzaCommonMessageCreate, bzaCommonMessageDetail as orvalBzaCommonMessageDetail, bzaCommonMessageDisable as orvalBzaCommonMessageDisable, bzaCommonMessageSearch as orvalBzaCommonMessageSearch, bzaCommonMessageUpdate as orvalBzaCommonMessageUpdate, bzaCommonResponseCodeCreate as orvalBzaCommonResponseCodeCreate, bzaCommonResponseCodeDetail as orvalBzaCommonResponseCodeDetail, bzaCommonResponseCodeDisable as orvalBzaCommonResponseCodeDisable, bzaCommonResponseCodeSearch as orvalBzaCommonResponseCodeSearch, bzaCommonResponseCodeUpdate as orvalBzaCommonResponseCodeUpdate, bzaCommonSearch as orvalBzaCommonSearch, bzaCommonUpdate as orvalBzaCommonUpdate, bzaDirectoryFindAssignments as orvalBzaDirectoryFindAssignments, bzaDirectoryFindAssignmentsPage as orvalBzaDirectoryFindAssignmentsPage, bzaDirectoryFindJobTitles as orvalBzaDirectoryFindJobTitles, bzaDirectoryFindJobTitlesPage as orvalBzaDirectoryFindJobTitlesPage, bzaDirectoryFindPositions as orvalBzaDirectoryFindPositions, bzaDirectoryFindPositionsPage as orvalBzaDirectoryFindPositionsPage, bzaDirectoryFindResponsibilities as orvalBzaDirectoryFindResponsibilities, bzaDirectoryFindResponsibilitiesPage as orvalBzaDirectoryFindResponsibilitiesPage, bzaDirectoryFindUserRoles as orvalBzaDirectoryFindUserRoles, bzaDirectoryFindUserRolesPage as orvalBzaDirectoryFindUserRolesPage, bzaDirectorySaveAssignment as orvalBzaDirectorySaveAssignment, bzaDirectorySaveJobTitle as orvalBzaDirectorySaveJobTitle, bzaDirectorySavePosition as orvalBzaDirectorySavePosition, bzaDirectorySaveResponsibility as orvalBzaDirectorySaveResponsibility, bzaDirectorySaveUserRole as orvalBzaDirectorySaveUserRole, bzaOperationDeleteMenu as orvalBzaOperationDeleteMenu, bzaOperationFindAdminUsers as orvalBzaOperationFindAdminUsers, bzaOperationFindAdminUsersPage as orvalBzaOperationFindAdminUsersPage, bzaOperationFindDownloadPolicies as orvalBzaOperationFindDownloadPolicies, bzaOperationFindMenuImpact as orvalBzaOperationFindMenuImpact, bzaOperationFindMenus as orvalBzaOperationFindMenus, bzaOperationFindMenusPage as orvalBzaOperationFindMenusPage, bzaOperationFindPermissions as orvalBzaOperationFindPermissions, bzaOperationFindPermissionsPage as orvalBzaOperationFindPermissionsPage, bzaOperationFindRoles as orvalBzaOperationFindRoles, bzaOperationFindRolesPage as orvalBzaOperationFindRolesPage, bzaOperationFindSettings as orvalBzaOperationFindSettings, bzaOperationSaveAdminUser as orvalBzaOperationSaveAdminUser, bzaOperationSaveMenu as orvalBzaOperationSaveMenu, bzaOperationSavePermission as orvalBzaOperationSavePermission, bzaOperationSaveRole as orvalBzaOperationSaveRole, bzaSupportCompareRolePermissions as orvalBzaSupportCompareRolePermissions, bzaSupportCreateNotification as orvalBzaSupportCreateNotification, bzaSupportDashboard as orvalBzaSupportDashboard, bzaSupportDisableSavedSearch as orvalBzaSupportDisableSavedSearch, bzaSupportDownloadAttachment as orvalBzaSupportDownloadAttachment, bzaSupportFindAttachments as orvalBzaSupportFindAttachments, bzaSupportFindDownloadAudits as orvalBzaSupportFindDownloadAudits, bzaSupportFindNotifications as orvalBzaSupportFindNotifications, bzaSupportFindSavedSearches as orvalBzaSupportFindSavedSearches, bzaSupportReadAllNotifications as orvalBzaSupportReadAllNotifications, bzaSupportReadNotification as orvalBzaSupportReadNotification, bzaSupportRecheckAttachment as orvalBzaSupportRecheckAttachment, bzaSupportSaveSavedSearch as orvalBzaSupportSaveSavedSearch, bzaSupportSimulatePermission as orvalBzaSupportSimulatePermission, bzaSupportUpdateAttachmentSecurity as orvalBzaSupportUpdateAttachmentSecurity, bzaSupportUploadAttachment as orvalBzaSupportUploadAttachment } from "./orval/cpf-api";

export type CpfGeneratedHeaders = HeadersInit | Record<string, string>;
export interface CpfGeneratedBaseOptions { signal?: AbortSignal; headers?: CpfGeneratedHeaders; }
function headerValue(headers: CpfGeneratedHeaders | undefined, name: string): string | undefined { if (!headers) return undefined; if (headers instanceof Headers) return headers.get(name) ?? undefined; if (Array.isArray(headers)) { const found = headers.find(([key]) => String(key).toLowerCase() === name.toLowerCase()); return found ? String(found[1]) : undefined; } for (const [key,value] of Object.entries(headers)) if (key.toLowerCase() === name.toLowerCase()) return String(value); return undefined; }

export type BzaApprovalCancelBody = { comment?: string; idempotencyKey?: string; reason?: string };
export type BzaApprovalCancelPath = { approvalId: number };
export type BzaApprovalCancelQuery = Record<string, never>;
export type BzaApprovalCancelHeaders = Record<string, never>;
export type BzaApprovalCancelResponse = Record<string, unknown>;
export type BzaApprovalCancelOptions = CpfGeneratedBaseOptions & { data: BzaApprovalCancelBody; path: BzaApprovalCancelPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalCancel<T = BzaApprovalCancelResponse>(options: BzaApprovalCancelOptions): Promise<T> {
  const response = await orvalBzaApprovalCancel(options.path["approvalId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalDelegationsBody = never;
export type BzaApprovalDelegationsPath = Record<string, never>;
export type BzaApprovalDelegationsQuery = { employeeNo?: string; effectiveAt?: string };
export type BzaApprovalDelegationsHeaders = Record<string, never>;
export type BzaApprovalDelegationsResponse = Record<string, unknown>;
export type BzaApprovalDelegationsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaApprovalDelegationsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalDelegations<T = BzaApprovalDelegationsResponse>(options: BzaApprovalDelegationsOptions = {} as BzaApprovalDelegationsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaApprovalDelegations(contractParams as Parameters<typeof orvalBzaApprovalDelegations>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalDelegationSaveBody = { approvalType?: string; businessDomain?: string; delegateEmployeeNo?: string; delegationId?: number; delegatorEmployeeNo?: string; reason?: string; useYn?: string; validFrom?: string; validTo?: string };
export type BzaApprovalDelegationSavePath = Record<string, never>;
export type BzaApprovalDelegationSaveQuery = Record<string, never>;
export type BzaApprovalDelegationSaveHeaders = Record<string, never>;
export type BzaApprovalDelegationSaveResponse = Record<string, unknown>;
export type BzaApprovalDelegationSaveOptions = CpfGeneratedBaseOptions & { data: BzaApprovalDelegationSaveBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalDelegationSave<T = BzaApprovalDelegationSaveResponse>(options: BzaApprovalDelegationSaveOptions): Promise<T> {
  const response = await orvalBzaApprovalDelegationSave(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalExpireDueBody = never;
export type BzaApprovalExpireDuePath = Record<string, never>;
export type BzaApprovalExpireDueQuery = { limit?: number };
export type BzaApprovalExpireDueHeaders = Record<string, never>;
export type BzaApprovalExpireDueResponse = Record<string, unknown>;
export type BzaApprovalExpireDueOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaApprovalExpireDueQuery; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalExpireDue<T = BzaApprovalExpireDueResponse>(options: BzaApprovalExpireDueOptions = {} as BzaApprovalExpireDueOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaApprovalExpireDue(contractParams as Parameters<typeof orvalBzaApprovalExpireDue>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalInboxBody = never;
export type BzaApprovalInboxPath = Record<string, never>;
export type BzaApprovalInboxQuery = { decisionStatus?: string; limit?: number };
export type BzaApprovalInboxHeaders = Record<string, never>;
export type BzaApprovalInboxResponse = Record<string, unknown>;
export type BzaApprovalInboxOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaApprovalInboxQuery; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalInbox<T = BzaApprovalInboxResponse>(options: BzaApprovalInboxOptions = {} as BzaApprovalInboxOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaApprovalInbox(contractParams as Parameters<typeof orvalBzaApprovalInbox>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalParticipantDecisionBody = { action?: string; comment?: string; idempotencyKey?: string; reason?: string };
export type BzaApprovalParticipantDecisionPath = { approvalId: number };
export type BzaApprovalParticipantDecisionQuery = Record<string, never>;
export type BzaApprovalParticipantDecisionHeaders = Record<string, never>;
export type BzaApprovalParticipantDecisionResponse = Record<string, unknown>;
export type BzaApprovalParticipantDecisionOptions = CpfGeneratedBaseOptions & { data: BzaApprovalParticipantDecisionBody; path: BzaApprovalParticipantDecisionPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalParticipantDecision<T = BzaApprovalParticipantDecisionResponse>(options: BzaApprovalParticipantDecisionOptions): Promise<T> {
  const response = await orvalBzaApprovalParticipantDecision(options.path["approvalId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalPoliciesBody = never;
export type BzaApprovalPoliciesPath = Record<string, never>;
export type BzaApprovalPoliciesQuery = { businessDomain?: string; approvalType?: string };
export type BzaApprovalPoliciesHeaders = Record<string, never>;
export type BzaApprovalPoliciesResponse = Record<string, unknown>;
export type BzaApprovalPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaApprovalPoliciesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalPolicies<T = BzaApprovalPoliciesResponse>(options: BzaApprovalPoliciesOptions = {} as BzaApprovalPoliciesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaApprovalPolicies(contractParams as Parameters<typeof orvalBzaApprovalPolicies>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalPolicyDetailBody = never;
export type BzaApprovalPolicyDetailPath = { policyCode: string; version: number };
export type BzaApprovalPolicyDetailQuery = Record<string, never>;
export type BzaApprovalPolicyDetailHeaders = Record<string, never>;
export type BzaApprovalPolicyDetailResponse = Record<string, unknown>;
export type BzaApprovalPolicyDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaApprovalPolicyDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalPolicyDetail<T = BzaApprovalPolicyDetailResponse>(options: BzaApprovalPolicyDetailOptions): Promise<T> {
  const response = await orvalBzaApprovalPolicyDetail(options.path["policyCode"], options.path["version"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalPolicySaveBody = { approvalType?: string; businessDomain?: string; description?: string; effectiveFrom?: string; effectiveTo?: string; enabledYn?: string; policyCode?: string; policyName?: string; policyVersion?: number; reason?: string; selfApprovalAllowedYn?: string; steps?: Array<{ decisionRule?: string; requiredCount?: number; requiredYn?: string; sortOrder?: number; stepNo?: number; stepType?: string; targetCode?: string; targetType?: string }> };
export type BzaApprovalPolicySavePath = Record<string, never>;
export type BzaApprovalPolicySaveQuery = Record<string, never>;
export type BzaApprovalPolicySaveHeaders = Record<string, never>;
export type BzaApprovalPolicySaveResponse = Record<string, unknown>;
export type BzaApprovalPolicySaveOptions = CpfGeneratedBaseOptions & { data: BzaApprovalPolicySaveBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalPolicySave<T = BzaApprovalPolicySaveResponse>(options: BzaApprovalPolicySaveOptions): Promise<T> {
  const response = await orvalBzaApprovalPolicySave(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalPolicySimulateBody = { approvalType?: string; businessDomain?: string; effectiveAt?: string; policyCode?: string; policyVersion?: number; requesterEmployeeNo?: string };
export type BzaApprovalPolicySimulatePath = Record<string, never>;
export type BzaApprovalPolicySimulateQuery = Record<string, never>;
export type BzaApprovalPolicySimulateHeaders = Record<string, never>;
export type BzaApprovalPolicySimulateResponse = Record<string, unknown>;
export type BzaApprovalPolicySimulateOptions = CpfGeneratedBaseOptions & { data: BzaApprovalPolicySimulateBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalPolicySimulate<T = BzaApprovalPolicySimulateResponse>(options: BzaApprovalPolicySimulateOptions): Promise<T> {
  const response = await orvalBzaApprovalPolicySimulate(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalPolicySubmitBody = { approvalMode?: string; approvalType?: string; attachmentGroupId?: string; businessDomain?: string; dueAt?: string; payloadJson?: string; policyCode?: string; policyVersion?: number; reason?: string; requestIdempotencyKey?: string; requesterEmployeeNo?: string; title?: string };
export type BzaApprovalPolicySubmitPath = Record<string, never>;
export type BzaApprovalPolicySubmitQuery = Record<string, never>;
export type BzaApprovalPolicySubmitHeaders = Record<string, never>;
export type BzaApprovalPolicySubmitResponse = Record<string, unknown>;
export type BzaApprovalPolicySubmitOptions = CpfGeneratedBaseOptions & { data: BzaApprovalPolicySubmitBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalPolicySubmit<T = BzaApprovalPolicySubmitResponse>(options: BzaApprovalPolicySubmitOptions): Promise<T> {
  const response = await orvalBzaApprovalPolicySubmit(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalResubmitBody = { approvalMode?: string; approvalType?: string; attachmentGroupId?: string; businessDomain?: string; dueAt?: string; payloadJson?: string; policyCode?: string; policyVersion?: number; reason?: string; requestIdempotencyKey?: string; requesterEmployeeNo?: string; title?: string };
export type BzaApprovalResubmitPath = { approvalId: number };
export type BzaApprovalResubmitQuery = Record<string, never>;
export type BzaApprovalResubmitHeaders = Record<string, never>;
export type BzaApprovalResubmitResponse = Record<string, unknown>;
export type BzaApprovalResubmitOptions = CpfGeneratedBaseOptions & { data: BzaApprovalResubmitBody; path: BzaApprovalResubmitPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalResubmit<T = BzaApprovalResubmitResponse>(options: BzaApprovalResubmitOptions): Promise<T> {
  const response = await orvalBzaApprovalResubmit(options.path["approvalId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalSubmissionDetailBody = never;
export type BzaApprovalSubmissionDetailPath = { approvalId: number };
export type BzaApprovalSubmissionDetailQuery = Record<string, never>;
export type BzaApprovalSubmissionDetailHeaders = Record<string, never>;
export type BzaApprovalSubmissionDetailResponse = Record<string, unknown>;
export type BzaApprovalSubmissionDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaApprovalSubmissionDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalSubmissionDetail<T = BzaApprovalSubmissionDetailResponse>(options: BzaApprovalSubmissionDetailOptions): Promise<T> {
  const response = await orvalBzaApprovalSubmissionDetail(options.path["approvalId"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalSubmissionsBody = never;
export type BzaApprovalSubmissionsPath = Record<string, never>;
export type BzaApprovalSubmissionsQuery = { status?: string; limit?: number };
export type BzaApprovalSubmissionsHeaders = Record<string, never>;
export type BzaApprovalSubmissionsResponse = Record<string, unknown>;
export type BzaApprovalSubmissionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaApprovalSubmissionsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalSubmissions<T = BzaApprovalSubmissionsResponse>(options: BzaApprovalSubmissionsOptions = {} as BzaApprovalSubmissionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaApprovalSubmissions(contractParams as Parameters<typeof orvalBzaApprovalSubmissions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaApprovalWithdrawBody = { comment?: string; idempotencyKey?: string; reason?: string };
export type BzaApprovalWithdrawPath = { approvalId: number };
export type BzaApprovalWithdrawQuery = Record<string, never>;
export type BzaApprovalWithdrawHeaders = Record<string, never>;
export type BzaApprovalWithdrawResponse = Record<string, unknown>;
export type BzaApprovalWithdrawOptions = CpfGeneratedBaseOptions & { data: BzaApprovalWithdrawBody; path: BzaApprovalWithdrawPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaApprovalWithdraw<T = BzaApprovalWithdrawResponse>(options: BzaApprovalWithdrawOptions): Promise<T> {
  const response = await orvalBzaApprovalWithdraw(options.path["approvalId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthChangePasswordBody = { currentPassword?: string; newPassword?: string; newPasswordConfirm?: string };
export type BzaAuthChangePasswordPath = Record<string, never>;
export type BzaAuthChangePasswordQuery = Record<string, never>;
export type BzaAuthChangePasswordHeaders = Record<string, never>;
export type BzaAuthChangePasswordResponse = Record<string, unknown>;
export type BzaAuthChangePasswordOptions = CpfGeneratedBaseOptions & { data: BzaAuthChangePasswordBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaAuthChangePassword<T = BzaAuthChangePasswordResponse>(options: BzaAuthChangePasswordOptions): Promise<T> {
  const response = await orvalBzaAuthChangePassword(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthLoginBody = { loginId?: string; operationId?: string; password?: string };
export type BzaAuthLoginPath = Record<string, never>;
export type BzaAuthLoginQuery = Record<string, never>;
export type BzaAuthLoginHeaders = Record<string, never>;
export type BzaAuthLoginResponse = Record<string, unknown>;
export type BzaAuthLoginOptions = CpfGeneratedBaseOptions & { data: BzaAuthLoginBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaAuthLogin<T = BzaAuthLoginResponse>(options: BzaAuthLoginOptions): Promise<T> {
  const response = await orvalBzaAuthLogin(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthLoginHistoriesBody = never;
export type BzaAuthLoginHistoriesPath = Record<string, never>;
export type BzaAuthLoginHistoriesQuery = { limit?: number };
export type BzaAuthLoginHistoriesHeaders = Record<string, never>;
export type BzaAuthLoginHistoriesResponse = Record<string, unknown>;
export type BzaAuthLoginHistoriesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaAuthLoginHistoriesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaAuthLoginHistories<T = BzaAuthLoginHistoriesResponse>(options: BzaAuthLoginHistoriesOptions = {} as BzaAuthLoginHistoriesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaAuthLoginHistories(contractParams as Parameters<typeof orvalBzaAuthLoginHistories>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthLogoutBody = never;
export type BzaAuthLogoutPath = Record<string, never>;
export type BzaAuthLogoutQuery = Record<string, never>;
export type BzaAuthLogoutHeaders = Record<string, never>;
export type BzaAuthLogoutResponse = Record<string, unknown>;
export type BzaAuthLogoutOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaAuthLogout<T = BzaAuthLogoutResponse>(options: BzaAuthLogoutOptions = {} as BzaAuthLogoutOptions): Promise<T> {
  const response = await orvalBzaAuthLogout({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthMeBody = never;
export type BzaAuthMePath = Record<string, never>;
export type BzaAuthMeQuery = Record<string, never>;
export type BzaAuthMeHeaders = Record<string, never>;
export type BzaAuthMeResponse = Record<string, unknown>;
export type BzaAuthMeOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaAuthMe<T = BzaAuthMeResponse>(options: BzaAuthMeOptions = {} as BzaAuthMeOptions): Promise<T> {
  const response = await orvalBzaAuthMe({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthRefreshBody = never;
export type BzaAuthRefreshPath = Record<string, never>;
export type BzaAuthRefreshQuery = Record<string, never>;
export type BzaAuthRefreshHeaders = Record<string, never>;
export type BzaAuthRefreshResponse = Record<string, unknown>;
export type BzaAuthRefreshOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaAuthRefresh<T = BzaAuthRefreshResponse>(options: BzaAuthRefreshOptions = {} as BzaAuthRefreshOptions): Promise<T> {
  const response = await orvalBzaAuthRefresh({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthRevokeSessionBody = never;
export type BzaAuthRevokeSessionPath = { sessionId: string };
export type BzaAuthRevokeSessionQuery = { reason: string };
export type BzaAuthRevokeSessionHeaders = Record<string, never>;
export type BzaAuthRevokeSessionResponse = Record<string, unknown>;
export type BzaAuthRevokeSessionOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaAuthRevokeSessionPath; query?: BzaAuthRevokeSessionQuery; headers?: CpfGeneratedHeaders; };
export async function bzaAuthRevokeSession<T = BzaAuthRevokeSessionResponse>(options: BzaAuthRevokeSessionOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaAuthRevokeSession(options.path["sessionId"], contractParams as Parameters<typeof orvalBzaAuthRevokeSession>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaAuthSessionsBody = never;
export type BzaAuthSessionsPath = Record<string, never>;
export type BzaAuthSessionsQuery = { limit?: number };
export type BzaAuthSessionsHeaders = Record<string, never>;
export type BzaAuthSessionsResponse = Record<string, unknown>;
export type BzaAuthSessionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaAuthSessionsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaAuthSessions<T = BzaAuthSessionsResponse>(options: BzaAuthSessionsOptions = {} as BzaAuthSessionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaAuthSessions(contractParams as Parameters<typeof orvalBzaAuthSessions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeEmployeeRawContactBody = Record<string, unknown>;
export type BzaBackofficeEmployeeRawContactPath = { employeeNo: string };
export type BzaBackofficeEmployeeRawContactQuery = Record<string, never>;
export type BzaBackofficeEmployeeRawContactHeaders = Record<string, never>;
export type BzaBackofficeEmployeeRawContactResponse = Record<string, unknown>;
export type BzaBackofficeEmployeeRawContactOptions = CpfGeneratedBaseOptions & { data: BzaBackofficeEmployeeRawContactBody; path: BzaBackofficeEmployeeRawContactPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeEmployeeRawContact<T = BzaBackofficeEmployeeRawContactResponse>(options: BzaBackofficeEmployeeRawContactOptions): Promise<T> {
  const response = await orvalBzaBackofficeEmployeeRawContact(options.path["employeeNo"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindBusinessAuditsBody = never;
export type BzaBackofficeFindBusinessAuditsPath = Record<string, never>;
export type BzaBackofficeFindBusinessAuditsQuery = { limit?: number };
export type BzaBackofficeFindBusinessAuditsHeaders = Record<string, never>;
export type BzaBackofficeFindBusinessAuditsResponse = Record<string, unknown>;
export type BzaBackofficeFindBusinessAuditsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaBackofficeFindBusinessAuditsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindBusinessAudits<T = BzaBackofficeFindBusinessAuditsResponse>(options: BzaBackofficeFindBusinessAuditsOptions = {} as BzaBackofficeFindBusinessAuditsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaBackofficeFindBusinessAudits(contractParams as Parameters<typeof orvalBzaBackofficeFindBusinessAudits>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindEffectivePermissionsBody = never;
export type BzaBackofficeFindEffectivePermissionsPath = Record<string, never>;
export type BzaBackofficeFindEffectivePermissionsQuery = { loginId: string };
export type BzaBackofficeFindEffectivePermissionsHeaders = Record<string, never>;
export type BzaBackofficeFindEffectivePermissionsResponse = Record<string, unknown>;
export type BzaBackofficeFindEffectivePermissionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaBackofficeFindEffectivePermissionsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindEffectivePermissions<T = BzaBackofficeFindEffectivePermissionsResponse>(options: BzaBackofficeFindEffectivePermissionsOptions = {} as BzaBackofficeFindEffectivePermissionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaBackofficeFindEffectivePermissions(contractParams as Parameters<typeof orvalBzaBackofficeFindEffectivePermissions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindEmployeesBody = never;
export type BzaBackofficeFindEmployeesPath = Record<string, never>;
export type BzaBackofficeFindEmployeesQuery = { organizationCode?: string; status?: string };
export type BzaBackofficeFindEmployeesHeaders = Record<string, never>;
export type BzaBackofficeFindEmployeesResponse = Record<string, unknown>;
export type BzaBackofficeFindEmployeesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaBackofficeFindEmployeesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindEmployees<T = BzaBackofficeFindEmployeesResponse>(options: BzaBackofficeFindEmployeesOptions = {} as BzaBackofficeFindEmployeesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaBackofficeFindEmployees(contractParams as Parameters<typeof orvalBzaBackofficeFindEmployees>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindEmployeesPageBody = never;
export type BzaBackofficeFindEmployeesPagePath = Record<string, never>;
export type BzaBackofficeFindEmployeesPageQuery = { organizationCode?: string; status?: string; page?: number; size?: number };
export type BzaBackofficeFindEmployeesPageHeaders = Record<string, never>;
export type BzaBackofficeFindEmployeesPageResponse = Record<string, unknown>;
export type BzaBackofficeFindEmployeesPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaBackofficeFindEmployeesPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindEmployeesPage<T = BzaBackofficeFindEmployeesPageResponse>(options: BzaBackofficeFindEmployeesPageOptions = {} as BzaBackofficeFindEmployeesPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaBackofficeFindEmployeesPage(contractParams as Parameters<typeof orvalBzaBackofficeFindEmployeesPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindOrganizationsBody = never;
export type BzaBackofficeFindOrganizationsPath = Record<string, never>;
export type BzaBackofficeFindOrganizationsQuery = Record<string, never>;
export type BzaBackofficeFindOrganizationsHeaders = Record<string, never>;
export type BzaBackofficeFindOrganizationsResponse = Record<string, unknown>;
export type BzaBackofficeFindOrganizationsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindOrganizations<T = BzaBackofficeFindOrganizationsResponse>(options: BzaBackofficeFindOrganizationsOptions = {} as BzaBackofficeFindOrganizationsOptions): Promise<T> {
  const response = await orvalBzaBackofficeFindOrganizations({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeFindOrganizationsPageBody = never;
export type BzaBackofficeFindOrganizationsPagePath = Record<string, never>;
export type BzaBackofficeFindOrganizationsPageQuery = { page?: number; size?: number };
export type BzaBackofficeFindOrganizationsPageHeaders = Record<string, never>;
export type BzaBackofficeFindOrganizationsPageResponse = Record<string, unknown>;
export type BzaBackofficeFindOrganizationsPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaBackofficeFindOrganizationsPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeFindOrganizationsPage<T = BzaBackofficeFindOrganizationsPageResponse>(options: BzaBackofficeFindOrganizationsPageOptions = {} as BzaBackofficeFindOrganizationsPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaBackofficeFindOrganizationsPage(contractParams as Parameters<typeof orvalBzaBackofficeFindOrganizationsPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeSaveEmployeeBody = { adminUserId?: number; clearEmail: boolean; clearMobileNo: boolean; clearOfficePhoneNo: boolean; email?: string; employeeName?: string; employeeNo?: string; employmentStatus?: string; expectedVersion?: number; jobTitleCode?: string; joinDate?: string; leaveDate?: string; managerEmployeeNo?: string; mobileNo?: string; officePhoneNo?: string; organizationCode?: string; positionCode?: string; reason?: string; requestUser?: string; useYn?: string };
export type BzaBackofficeSaveEmployeePath = Record<string, never>;
export type BzaBackofficeSaveEmployeeQuery = Record<string, never>;
export type BzaBackofficeSaveEmployeeHeaders = Record<string, never>;
export type BzaBackofficeSaveEmployeeResponse = Record<string, unknown>;
export type BzaBackofficeSaveEmployeeOptions = CpfGeneratedBaseOptions & { data: BzaBackofficeSaveEmployeeBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeSaveEmployee<T = BzaBackofficeSaveEmployeeResponse>(options: BzaBackofficeSaveEmployeeOptions): Promise<T> {
  const response = await orvalBzaBackofficeSaveEmployee(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBackofficeSaveOrganizationBody = { effectiveFrom?: string; effectiveTo?: string; expectedVersion?: number; organizationCode?: string; organizationName?: string; organizationType?: string; parentOrganizationCode?: string; reason?: string; requestUser?: string; sortOrder?: number; useYn?: string };
export type BzaBackofficeSaveOrganizationPath = Record<string, never>;
export type BzaBackofficeSaveOrganizationQuery = Record<string, never>;
export type BzaBackofficeSaveOrganizationHeaders = Record<string, never>;
export type BzaBackofficeSaveOrganizationResponse = Record<string, unknown>;
export type BzaBackofficeSaveOrganizationOptions = CpfGeneratedBaseOptions & { data: BzaBackofficeSaveOrganizationBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaBackofficeSaveOrganization<T = BzaBackofficeSaveOrganizationResponse>(options: BzaBackofficeSaveOrganizationOptions): Promise<T> {
  const response = await orvalBzaBackofficeSaveOrganization(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaBusinessAuditVerifyBody = never;
export type BzaBusinessAuditVerifyPath = Record<string, never>;
export type BzaBusinessAuditVerifyQuery = Record<string, never>;
export type BzaBusinessAuditVerifyHeaders = Record<string, never>;
export type BzaBusinessAuditVerifyResponse = Record<string, unknown>;
export type BzaBusinessAuditVerifyOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaBusinessAuditVerify<T = BzaBusinessAuditVerifyResponse>(options: BzaBusinessAuditVerifyOptions = {} as BzaBusinessAuditVerifyOptions): Promise<T> {
  const response = await orvalBzaBusinessAuditVerify({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonCatalogRefreshBody = never;
export type BzaCommonCatalogRefreshPath = Record<string, never>;
export type BzaCommonCatalogRefreshQuery = Record<string, never>;
export type BzaCommonCatalogRefreshHeaders = { "X-CPF-Reason": string };
export type BzaCommonCatalogRefreshResponse = Record<string, unknown>;
export type BzaCommonCatalogRefreshOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers: CpfGeneratedHeaders & BzaCommonCatalogRefreshHeaders; };
export async function bzaCommonCatalogRefresh<T = BzaCommonCatalogRefreshResponse>(options: BzaCommonCatalogRefreshOptions): Promise<T> {
  const contractParams = { "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonCatalogRefresh(contractParams as Parameters<typeof orvalBzaCommonCatalogRefresh>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonCreateBody = Record<string, unknown>;
export type BzaCommonCreatePath = { resource: string };
export type BzaCommonCreateQuery = Record<string, never>;
export type BzaCommonCreateHeaders = Record<string, never>;
export type BzaCommonCreateResponse = Record<string, unknown>;
export type BzaCommonCreateOptions = CpfGeneratedBaseOptions & { data: BzaCommonCreateBody; path: BzaCommonCreatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonCreate<T = BzaCommonCreateResponse>(options: BzaCommonCreateOptions): Promise<T> {
  const response = await orvalBzaCommonCreate(options.path["resource"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonDeleteBody = Record<string, unknown>;
export type BzaCommonDeletePath = { resource: string };
export type BzaCommonDeleteQuery = Record<string, never>;
export type BzaCommonDeleteHeaders = Record<string, never>;
export type BzaCommonDeleteResponse = Record<string, unknown>;
export type BzaCommonDeleteOptions = CpfGeneratedBaseOptions & { data: BzaCommonDeleteBody; path: BzaCommonDeletePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonDelete<T = BzaCommonDeleteResponse>(options: BzaCommonDeleteOptions): Promise<T> {
  const response = await orvalBzaCommonDelete(options.path["resource"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonDetailBody = Record<string, unknown>;
export type BzaCommonDetailPath = { resource: string };
export type BzaCommonDetailQuery = Record<string, never>;
export type BzaCommonDetailHeaders = Record<string, never>;
export type BzaCommonDetailResponse = Record<string, unknown>;
export type BzaCommonDetailOptions = CpfGeneratedBaseOptions & { data: BzaCommonDetailBody; path: BzaCommonDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonDetail<T = BzaCommonDetailResponse>(options: BzaCommonDetailOptions): Promise<T> {
  const response = await orvalBzaCommonDetail(options.path["resource"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonMessageCreateBody = Record<string, unknown>;
export type BzaCommonMessageCreatePath = Record<string, never>;
export type BzaCommonMessageCreateQuery = Record<string, never>;
export type BzaCommonMessageCreateHeaders = { "X-CPF-Reason": string };
export type BzaCommonMessageCreateResponse = Record<string, unknown>;
export type BzaCommonMessageCreateOptions = CpfGeneratedBaseOptions & { data: BzaCommonMessageCreateBody; path?: never; query?: never; headers: CpfGeneratedHeaders & BzaCommonMessageCreateHeaders; };
export async function bzaCommonMessageCreate<T = BzaCommonMessageCreateResponse>(options: BzaCommonMessageCreateOptions): Promise<T> {
  const contractParams = { "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonMessageCreate(options.data, contractParams as Parameters<typeof orvalBzaCommonMessageCreate>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonMessageDetailBody = never;
export type BzaCommonMessageDetailPath = { id: number };
export type BzaCommonMessageDetailQuery = Record<string, never>;
export type BzaCommonMessageDetailHeaders = Record<string, never>;
export type BzaCommonMessageDetailResponse = Record<string, unknown>;
export type BzaCommonMessageDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaCommonMessageDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonMessageDetail<T = BzaCommonMessageDetailResponse>(options: BzaCommonMessageDetailOptions): Promise<T> {
  const response = await orvalBzaCommonMessageDetail(options.path["id"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonMessageDisableBody = never;
export type BzaCommonMessageDisablePath = { id: number };
export type BzaCommonMessageDisableQuery = { expectedVersion: number };
export type BzaCommonMessageDisableHeaders = { "X-CPF-Reason": string };
export type BzaCommonMessageDisableResponse = Record<string, unknown>;
export type BzaCommonMessageDisableOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaCommonMessageDisablePath; query?: BzaCommonMessageDisableQuery; headers: CpfGeneratedHeaders & BzaCommonMessageDisableHeaders; };
export async function bzaCommonMessageDisable<T = BzaCommonMessageDisableResponse>(options: BzaCommonMessageDisableOptions): Promise<T> {
  const contractParams = { ...(options.query || {}), "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonMessageDisable(options.path["id"], contractParams as Parameters<typeof orvalBzaCommonMessageDisable>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonMessageSearchBody = never;
export type BzaCommonMessageSearchPath = Record<string, never>;
export type BzaCommonMessageSearchQuery = { keyword?: string; locale?: string; active?: boolean; page?: number; size?: number };
export type BzaCommonMessageSearchHeaders = Record<string, never>;
export type BzaCommonMessageSearchResponse = Record<string, unknown>;
export type BzaCommonMessageSearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaCommonMessageSearchQuery; headers?: CpfGeneratedHeaders; };
export async function bzaCommonMessageSearch<T = BzaCommonMessageSearchResponse>(options: BzaCommonMessageSearchOptions = {} as BzaCommonMessageSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaCommonMessageSearch(contractParams as Parameters<typeof orvalBzaCommonMessageSearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonMessageUpdateBody = Record<string, unknown>;
export type BzaCommonMessageUpdatePath = { id: number };
export type BzaCommonMessageUpdateQuery = { expectedVersion: number };
export type BzaCommonMessageUpdateHeaders = { "X-CPF-Reason": string };
export type BzaCommonMessageUpdateResponse = Record<string, unknown>;
export type BzaCommonMessageUpdateOptions = CpfGeneratedBaseOptions & { data: BzaCommonMessageUpdateBody; path: BzaCommonMessageUpdatePath; query?: BzaCommonMessageUpdateQuery; headers: CpfGeneratedHeaders & BzaCommonMessageUpdateHeaders; };
export async function bzaCommonMessageUpdate<T = BzaCommonMessageUpdateResponse>(options: BzaCommonMessageUpdateOptions): Promise<T> {
  const contractParams = { ...(options.query || {}), "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonMessageUpdate(options.path["id"], options.data, contractParams as Parameters<typeof orvalBzaCommonMessageUpdate>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonResponseCodeCreateBody = Record<string, unknown>;
export type BzaCommonResponseCodeCreatePath = Record<string, never>;
export type BzaCommonResponseCodeCreateQuery = Record<string, never>;
export type BzaCommonResponseCodeCreateHeaders = { "X-CPF-Reason": string };
export type BzaCommonResponseCodeCreateResponse = Record<string, unknown>;
export type BzaCommonResponseCodeCreateOptions = CpfGeneratedBaseOptions & { data: BzaCommonResponseCodeCreateBody; path?: never; query?: never; headers: CpfGeneratedHeaders & BzaCommonResponseCodeCreateHeaders; };
export async function bzaCommonResponseCodeCreate<T = BzaCommonResponseCodeCreateResponse>(options: BzaCommonResponseCodeCreateOptions): Promise<T> {
  const contractParams = { "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonResponseCodeCreate(options.data, contractParams as Parameters<typeof orvalBzaCommonResponseCodeCreate>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonResponseCodeDetailBody = never;
export type BzaCommonResponseCodeDetailPath = { code: string };
export type BzaCommonResponseCodeDetailQuery = Record<string, never>;
export type BzaCommonResponseCodeDetailHeaders = Record<string, never>;
export type BzaCommonResponseCodeDetailResponse = Record<string, unknown>;
export type BzaCommonResponseCodeDetailOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaCommonResponseCodeDetailPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonResponseCodeDetail<T = BzaCommonResponseCodeDetailResponse>(options: BzaCommonResponseCodeDetailOptions): Promise<T> {
  const response = await orvalBzaCommonResponseCodeDetail(options.path["code"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonResponseCodeDisableBody = never;
export type BzaCommonResponseCodeDisablePath = { code: string };
export type BzaCommonResponseCodeDisableQuery = { expectedVersion: number };
export type BzaCommonResponseCodeDisableHeaders = { "X-CPF-Reason": string };
export type BzaCommonResponseCodeDisableResponse = Record<string, unknown>;
export type BzaCommonResponseCodeDisableOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaCommonResponseCodeDisablePath; query?: BzaCommonResponseCodeDisableQuery; headers: CpfGeneratedHeaders & BzaCommonResponseCodeDisableHeaders; };
export async function bzaCommonResponseCodeDisable<T = BzaCommonResponseCodeDisableResponse>(options: BzaCommonResponseCodeDisableOptions): Promise<T> {
  const contractParams = { ...(options.query || {}), "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonResponseCodeDisable(options.path["code"], contractParams as Parameters<typeof orvalBzaCommonResponseCodeDisable>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonResponseCodeSearchBody = never;
export type BzaCommonResponseCodeSearchPath = Record<string, never>;
export type BzaCommonResponseCodeSearchQuery = { keyword?: string; active?: boolean; page?: number; size?: number };
export type BzaCommonResponseCodeSearchHeaders = Record<string, never>;
export type BzaCommonResponseCodeSearchResponse = Record<string, unknown>;
export type BzaCommonResponseCodeSearchOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaCommonResponseCodeSearchQuery; headers?: CpfGeneratedHeaders; };
export async function bzaCommonResponseCodeSearch<T = BzaCommonResponseCodeSearchResponse>(options: BzaCommonResponseCodeSearchOptions = {} as BzaCommonResponseCodeSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaCommonResponseCodeSearch(contractParams as Parameters<typeof orvalBzaCommonResponseCodeSearch>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonResponseCodeUpdateBody = Record<string, unknown>;
export type BzaCommonResponseCodeUpdatePath = { code: string };
export type BzaCommonResponseCodeUpdateQuery = { expectedVersion: number };
export type BzaCommonResponseCodeUpdateHeaders = { "X-CPF-Reason": string };
export type BzaCommonResponseCodeUpdateResponse = Record<string, unknown>;
export type BzaCommonResponseCodeUpdateOptions = CpfGeneratedBaseOptions & { data: BzaCommonResponseCodeUpdateBody; path: BzaCommonResponseCodeUpdatePath; query?: BzaCommonResponseCodeUpdateQuery; headers: CpfGeneratedHeaders & BzaCommonResponseCodeUpdateHeaders; };
export async function bzaCommonResponseCodeUpdate<T = BzaCommonResponseCodeUpdateResponse>(options: BzaCommonResponseCodeUpdateOptions): Promise<T> {
  const contractParams = { ...(options.query || {}), "X-CPF-Reason": headerValue(options.headers, "X-CPF-Reason") };
  const response = await orvalBzaCommonResponseCodeUpdate(options.path["code"], options.data, contractParams as Parameters<typeof orvalBzaCommonResponseCodeUpdate>[2], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonSearchBody = never;
export type BzaCommonSearchPath = { resource: string };
export type BzaCommonSearchQuery = { query?: string; page?: number; size?: number; includeDisabled?: boolean; effectiveAt?: string };
export type BzaCommonSearchHeaders = Record<string, never>;
export type BzaCommonSearchResponse = Record<string, unknown>;
export type BzaCommonSearchOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaCommonSearchPath; query?: BzaCommonSearchQuery; headers?: CpfGeneratedHeaders; };
export async function bzaCommonSearch<T = BzaCommonSearchResponse>(options: BzaCommonSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaCommonSearch(options.path["resource"], contractParams as Parameters<typeof orvalBzaCommonSearch>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaCommonUpdateBody = Record<string, unknown>;
export type BzaCommonUpdatePath = { resource: string };
export type BzaCommonUpdateQuery = Record<string, never>;
export type BzaCommonUpdateHeaders = Record<string, never>;
export type BzaCommonUpdateResponse = Record<string, unknown>;
export type BzaCommonUpdateOptions = CpfGeneratedBaseOptions & { data: BzaCommonUpdateBody; path: BzaCommonUpdatePath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaCommonUpdate<T = BzaCommonUpdateResponse>(options: BzaCommonUpdateOptions): Promise<T> {
  const response = await orvalBzaCommonUpdate(options.path["resource"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindAssignmentsBody = never;
export type BzaDirectoryFindAssignmentsPath = Record<string, never>;
export type BzaDirectoryFindAssignmentsQuery = { employeeNo?: string; organizationCode?: string; effectiveAt?: string };
export type BzaDirectoryFindAssignmentsHeaders = Record<string, never>;
export type BzaDirectoryFindAssignmentsResponse = Record<string, unknown>;
export type BzaDirectoryFindAssignmentsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindAssignmentsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindAssignments<T = BzaDirectoryFindAssignmentsResponse>(options: BzaDirectoryFindAssignmentsOptions = {} as BzaDirectoryFindAssignmentsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindAssignments(contractParams as Parameters<typeof orvalBzaDirectoryFindAssignments>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindAssignmentsPageBody = never;
export type BzaDirectoryFindAssignmentsPagePath = Record<string, never>;
export type BzaDirectoryFindAssignmentsPageQuery = { employeeNo?: string; organizationCode?: string; effectiveAt?: string; page?: number; size?: number };
export type BzaDirectoryFindAssignmentsPageHeaders = Record<string, never>;
export type BzaDirectoryFindAssignmentsPageResponse = Record<string, unknown>;
export type BzaDirectoryFindAssignmentsPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindAssignmentsPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindAssignmentsPage<T = BzaDirectoryFindAssignmentsPageResponse>(options: BzaDirectoryFindAssignmentsPageOptions = {} as BzaDirectoryFindAssignmentsPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindAssignmentsPage(contractParams as Parameters<typeof orvalBzaDirectoryFindAssignmentsPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindJobTitlesBody = never;
export type BzaDirectoryFindJobTitlesPath = Record<string, never>;
export type BzaDirectoryFindJobTitlesQuery = Record<string, never>;
export type BzaDirectoryFindJobTitlesHeaders = Record<string, never>;
export type BzaDirectoryFindJobTitlesResponse = Record<string, unknown>;
export type BzaDirectoryFindJobTitlesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindJobTitles<T = BzaDirectoryFindJobTitlesResponse>(options: BzaDirectoryFindJobTitlesOptions = {} as BzaDirectoryFindJobTitlesOptions): Promise<T> {
  const response = await orvalBzaDirectoryFindJobTitles({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindJobTitlesPageBody = never;
export type BzaDirectoryFindJobTitlesPagePath = Record<string, never>;
export type BzaDirectoryFindJobTitlesPageQuery = { page?: number; size?: number };
export type BzaDirectoryFindJobTitlesPageHeaders = Record<string, never>;
export type BzaDirectoryFindJobTitlesPageResponse = Record<string, unknown>;
export type BzaDirectoryFindJobTitlesPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindJobTitlesPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindJobTitlesPage<T = BzaDirectoryFindJobTitlesPageResponse>(options: BzaDirectoryFindJobTitlesPageOptions = {} as BzaDirectoryFindJobTitlesPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindJobTitlesPage(contractParams as Parameters<typeof orvalBzaDirectoryFindJobTitlesPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindPositionsBody = never;
export type BzaDirectoryFindPositionsPath = Record<string, never>;
export type BzaDirectoryFindPositionsQuery = Record<string, never>;
export type BzaDirectoryFindPositionsHeaders = Record<string, never>;
export type BzaDirectoryFindPositionsResponse = Record<string, unknown>;
export type BzaDirectoryFindPositionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindPositions<T = BzaDirectoryFindPositionsResponse>(options: BzaDirectoryFindPositionsOptions = {} as BzaDirectoryFindPositionsOptions): Promise<T> {
  const response = await orvalBzaDirectoryFindPositions({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindPositionsPageBody = never;
export type BzaDirectoryFindPositionsPagePath = Record<string, never>;
export type BzaDirectoryFindPositionsPageQuery = { page?: number; size?: number };
export type BzaDirectoryFindPositionsPageHeaders = Record<string, never>;
export type BzaDirectoryFindPositionsPageResponse = Record<string, unknown>;
export type BzaDirectoryFindPositionsPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindPositionsPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindPositionsPage<T = BzaDirectoryFindPositionsPageResponse>(options: BzaDirectoryFindPositionsPageOptions = {} as BzaDirectoryFindPositionsPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindPositionsPage(contractParams as Parameters<typeof orvalBzaDirectoryFindPositionsPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindResponsibilitiesBody = never;
export type BzaDirectoryFindResponsibilitiesPath = Record<string, never>;
export type BzaDirectoryFindResponsibilitiesQuery = { organizationCode?: string; effectiveAt?: string };
export type BzaDirectoryFindResponsibilitiesHeaders = Record<string, never>;
export type BzaDirectoryFindResponsibilitiesResponse = Record<string, unknown>;
export type BzaDirectoryFindResponsibilitiesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindResponsibilitiesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindResponsibilities<T = BzaDirectoryFindResponsibilitiesResponse>(options: BzaDirectoryFindResponsibilitiesOptions = {} as BzaDirectoryFindResponsibilitiesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindResponsibilities(contractParams as Parameters<typeof orvalBzaDirectoryFindResponsibilities>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindResponsibilitiesPageBody = never;
export type BzaDirectoryFindResponsibilitiesPagePath = Record<string, never>;
export type BzaDirectoryFindResponsibilitiesPageQuery = { organizationCode?: string; effectiveAt?: string; page?: number; size?: number };
export type BzaDirectoryFindResponsibilitiesPageHeaders = Record<string, never>;
export type BzaDirectoryFindResponsibilitiesPageResponse = Record<string, unknown>;
export type BzaDirectoryFindResponsibilitiesPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindResponsibilitiesPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindResponsibilitiesPage<T = BzaDirectoryFindResponsibilitiesPageResponse>(options: BzaDirectoryFindResponsibilitiesPageOptions = {} as BzaDirectoryFindResponsibilitiesPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindResponsibilitiesPage(contractParams as Parameters<typeof orvalBzaDirectoryFindResponsibilitiesPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindUserRolesBody = never;
export type BzaDirectoryFindUserRolesPath = Record<string, never>;
export type BzaDirectoryFindUserRolesQuery = { loginId?: string; effectiveAt?: string };
export type BzaDirectoryFindUserRolesHeaders = Record<string, never>;
export type BzaDirectoryFindUserRolesResponse = Record<string, unknown>;
export type BzaDirectoryFindUserRolesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindUserRolesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindUserRoles<T = BzaDirectoryFindUserRolesResponse>(options: BzaDirectoryFindUserRolesOptions = {} as BzaDirectoryFindUserRolesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindUserRoles(contractParams as Parameters<typeof orvalBzaDirectoryFindUserRoles>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectoryFindUserRolesPageBody = never;
export type BzaDirectoryFindUserRolesPagePath = Record<string, never>;
export type BzaDirectoryFindUserRolesPageQuery = { loginId?: string; effectiveAt?: string; page?: number; size?: number };
export type BzaDirectoryFindUserRolesPageHeaders = Record<string, never>;
export type BzaDirectoryFindUserRolesPageResponse = Record<string, unknown>;
export type BzaDirectoryFindUserRolesPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaDirectoryFindUserRolesPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaDirectoryFindUserRolesPage<T = BzaDirectoryFindUserRolesPageResponse>(options: BzaDirectoryFindUserRolesPageOptions = {} as BzaDirectoryFindUserRolesPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaDirectoryFindUserRolesPage(contractParams as Parameters<typeof orvalBzaDirectoryFindUserRolesPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectorySaveAssignmentBody = { assignmentId?: number; assignmentType?: string; effectiveFrom?: string; effectiveTo?: string; employeeNo?: string; expectedVersion?: number; jobTitleCode?: string; organizationCode?: string; positionCode?: string; primaryYn?: string; reason?: string };
export type BzaDirectorySaveAssignmentPath = Record<string, never>;
export type BzaDirectorySaveAssignmentQuery = Record<string, never>;
export type BzaDirectorySaveAssignmentHeaders = Record<string, never>;
export type BzaDirectorySaveAssignmentResponse = Record<string, unknown>;
export type BzaDirectorySaveAssignmentOptions = CpfGeneratedBaseOptions & { data: BzaDirectorySaveAssignmentBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectorySaveAssignment<T = BzaDirectorySaveAssignmentResponse>(options: BzaDirectorySaveAssignmentOptions): Promise<T> {
  const response = await orvalBzaDirectorySaveAssignment(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectorySaveJobTitleBody = { expectedVersion?: number; jobTitleCode?: string; jobTitleName?: string; managerYn?: string; reason?: string; useYn?: string };
export type BzaDirectorySaveJobTitlePath = Record<string, never>;
export type BzaDirectorySaveJobTitleQuery = Record<string, never>;
export type BzaDirectorySaveJobTitleHeaders = Record<string, never>;
export type BzaDirectorySaveJobTitleResponse = Record<string, unknown>;
export type BzaDirectorySaveJobTitleOptions = CpfGeneratedBaseOptions & { data: BzaDirectorySaveJobTitleBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectorySaveJobTitle<T = BzaDirectorySaveJobTitleResponse>(options: BzaDirectorySaveJobTitleOptions): Promise<T> {
  const response = await orvalBzaDirectorySaveJobTitle(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectorySavePositionBody = { expectedVersion?: number; positionCode?: string; positionName?: string; rankOrder?: number; reason?: string; useYn?: string };
export type BzaDirectorySavePositionPath = Record<string, never>;
export type BzaDirectorySavePositionQuery = Record<string, never>;
export type BzaDirectorySavePositionHeaders = Record<string, never>;
export type BzaDirectorySavePositionResponse = Record<string, unknown>;
export type BzaDirectorySavePositionOptions = CpfGeneratedBaseOptions & { data: BzaDirectorySavePositionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectorySavePosition<T = BzaDirectorySavePositionResponse>(options: BzaDirectorySavePositionOptions): Promise<T> {
  const response = await orvalBzaDirectorySavePosition(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectorySaveResponsibilityBody = { effectiveFrom?: string; effectiveTo?: string; employeeNo?: string; expectedVersion?: number; organizationCode?: string; priorityNo?: number; reason?: string; responsibilityId?: number; responsibilityType?: string; useYn?: string };
export type BzaDirectorySaveResponsibilityPath = Record<string, never>;
export type BzaDirectorySaveResponsibilityQuery = Record<string, never>;
export type BzaDirectorySaveResponsibilityHeaders = Record<string, never>;
export type BzaDirectorySaveResponsibilityResponse = Record<string, unknown>;
export type BzaDirectorySaveResponsibilityOptions = CpfGeneratedBaseOptions & { data: BzaDirectorySaveResponsibilityBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectorySaveResponsibility<T = BzaDirectorySaveResponsibilityResponse>(options: BzaDirectorySaveResponsibilityOptions): Promise<T> {
  const response = await orvalBzaDirectorySaveResponsibility(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaDirectorySaveUserRoleBody = { loginId?: string; operationId?: string; primaryYn?: string; reason?: string; roleCode?: string; validFrom?: string; validTo?: string };
export type BzaDirectorySaveUserRolePath = Record<string, never>;
export type BzaDirectorySaveUserRoleQuery = Record<string, never>;
export type BzaDirectorySaveUserRoleHeaders = Record<string, never>;
export type BzaDirectorySaveUserRoleResponse = Record<string, unknown>;
export type BzaDirectorySaveUserRoleOptions = CpfGeneratedBaseOptions & { data: BzaDirectorySaveUserRoleBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaDirectorySaveUserRole<T = BzaDirectorySaveUserRoleResponse>(options: BzaDirectorySaveUserRoleOptions): Promise<T> {
  const response = await orvalBzaDirectorySaveUserRole(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationDeleteMenuBody = { expectedVersion?: number; operationId?: string; reason?: string };
export type BzaOperationDeleteMenuPath = { menuCode: string };
export type BzaOperationDeleteMenuQuery = Record<string, never>;
export type BzaOperationDeleteMenuHeaders = Record<string, never>;
export type BzaOperationDeleteMenuResponse = Record<string, unknown>;
export type BzaOperationDeleteMenuOptions = CpfGeneratedBaseOptions & { data: BzaOperationDeleteMenuBody; path: BzaOperationDeleteMenuPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationDeleteMenu<T = BzaOperationDeleteMenuResponse>(options: BzaOperationDeleteMenuOptions): Promise<T> {
  const response = await orvalBzaOperationDeleteMenu(options.path["menuCode"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindAdminUsersBody = never;
export type BzaOperationFindAdminUsersPath = Record<string, never>;
export type BzaOperationFindAdminUsersQuery = Record<string, never>;
export type BzaOperationFindAdminUsersHeaders = Record<string, never>;
export type BzaOperationFindAdminUsersResponse = Record<string, unknown>;
export type BzaOperationFindAdminUsersOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindAdminUsers<T = BzaOperationFindAdminUsersResponse>(options: BzaOperationFindAdminUsersOptions = {} as BzaOperationFindAdminUsersOptions): Promise<T> {
  const response = await orvalBzaOperationFindAdminUsers({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindAdminUsersPageBody = never;
export type BzaOperationFindAdminUsersPagePath = Record<string, never>;
export type BzaOperationFindAdminUsersPageQuery = { page?: number; size?: number };
export type BzaOperationFindAdminUsersPageHeaders = Record<string, never>;
export type BzaOperationFindAdminUsersPageResponse = Record<string, unknown>;
export type BzaOperationFindAdminUsersPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaOperationFindAdminUsersPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindAdminUsersPage<T = BzaOperationFindAdminUsersPageResponse>(options: BzaOperationFindAdminUsersPageOptions = {} as BzaOperationFindAdminUsersPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaOperationFindAdminUsersPage(contractParams as Parameters<typeof orvalBzaOperationFindAdminUsersPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindDownloadPoliciesBody = never;
export type BzaOperationFindDownloadPoliciesPath = Record<string, never>;
export type BzaOperationFindDownloadPoliciesQuery = Record<string, never>;
export type BzaOperationFindDownloadPoliciesHeaders = Record<string, never>;
export type BzaOperationFindDownloadPoliciesResponse = Record<string, unknown>;
export type BzaOperationFindDownloadPoliciesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindDownloadPolicies<T = BzaOperationFindDownloadPoliciesResponse>(options: BzaOperationFindDownloadPoliciesOptions = {} as BzaOperationFindDownloadPoliciesOptions): Promise<T> {
  const response = await orvalBzaOperationFindDownloadPolicies({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindMenuImpactBody = never;
export type BzaOperationFindMenuImpactPath = { menuCode: string };
export type BzaOperationFindMenuImpactQuery = Record<string, never>;
export type BzaOperationFindMenuImpactHeaders = Record<string, never>;
export type BzaOperationFindMenuImpactResponse = Record<string, unknown>;
export type BzaOperationFindMenuImpactOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaOperationFindMenuImpactPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindMenuImpact<T = BzaOperationFindMenuImpactResponse>(options: BzaOperationFindMenuImpactOptions): Promise<T> {
  const response = await orvalBzaOperationFindMenuImpact(options.path["menuCode"], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindMenusBody = never;
export type BzaOperationFindMenusPath = Record<string, never>;
export type BzaOperationFindMenusQuery = Record<string, never>;
export type BzaOperationFindMenusHeaders = Record<string, never>;
export type BzaOperationFindMenusResponse = Record<string, unknown>;
export type BzaOperationFindMenusOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindMenus<T = BzaOperationFindMenusResponse>(options: BzaOperationFindMenusOptions = {} as BzaOperationFindMenusOptions): Promise<T> {
  const response = await orvalBzaOperationFindMenus({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindMenusPageBody = never;
export type BzaOperationFindMenusPagePath = Record<string, never>;
export type BzaOperationFindMenusPageQuery = { page?: number; size?: number };
export type BzaOperationFindMenusPageHeaders = Record<string, never>;
export type BzaOperationFindMenusPageResponse = Record<string, unknown>;
export type BzaOperationFindMenusPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaOperationFindMenusPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindMenusPage<T = BzaOperationFindMenusPageResponse>(options: BzaOperationFindMenusPageOptions = {} as BzaOperationFindMenusPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaOperationFindMenusPage(contractParams as Parameters<typeof orvalBzaOperationFindMenusPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindPermissionsBody = never;
export type BzaOperationFindPermissionsPath = Record<string, never>;
export type BzaOperationFindPermissionsQuery = Record<string, never>;
export type BzaOperationFindPermissionsHeaders = Record<string, never>;
export type BzaOperationFindPermissionsResponse = Record<string, unknown>;
export type BzaOperationFindPermissionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindPermissions<T = BzaOperationFindPermissionsResponse>(options: BzaOperationFindPermissionsOptions = {} as BzaOperationFindPermissionsOptions): Promise<T> {
  const response = await orvalBzaOperationFindPermissions({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindPermissionsPageBody = never;
export type BzaOperationFindPermissionsPagePath = Record<string, never>;
export type BzaOperationFindPermissionsPageQuery = { page?: number; size?: number };
export type BzaOperationFindPermissionsPageHeaders = Record<string, never>;
export type BzaOperationFindPermissionsPageResponse = Record<string, unknown>;
export type BzaOperationFindPermissionsPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaOperationFindPermissionsPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindPermissionsPage<T = BzaOperationFindPermissionsPageResponse>(options: BzaOperationFindPermissionsPageOptions = {} as BzaOperationFindPermissionsPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaOperationFindPermissionsPage(contractParams as Parameters<typeof orvalBzaOperationFindPermissionsPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindRolesBody = never;
export type BzaOperationFindRolesPath = Record<string, never>;
export type BzaOperationFindRolesQuery = Record<string, never>;
export type BzaOperationFindRolesHeaders = Record<string, never>;
export type BzaOperationFindRolesResponse = Record<string, unknown>;
export type BzaOperationFindRolesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindRoles<T = BzaOperationFindRolesResponse>(options: BzaOperationFindRolesOptions = {} as BzaOperationFindRolesOptions): Promise<T> {
  const response = await orvalBzaOperationFindRoles({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindRolesPageBody = never;
export type BzaOperationFindRolesPagePath = Record<string, never>;
export type BzaOperationFindRolesPageQuery = { page?: number; size?: number };
export type BzaOperationFindRolesPageHeaders = Record<string, never>;
export type BzaOperationFindRolesPageResponse = Record<string, unknown>;
export type BzaOperationFindRolesPageOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaOperationFindRolesPageQuery; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindRolesPage<T = BzaOperationFindRolesPageResponse>(options: BzaOperationFindRolesPageOptions = {} as BzaOperationFindRolesPageOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaOperationFindRolesPage(contractParams as Parameters<typeof orvalBzaOperationFindRolesPage>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationFindSettingsBody = never;
export type BzaOperationFindSettingsPath = Record<string, never>;
export type BzaOperationFindSettingsQuery = Record<string, never>;
export type BzaOperationFindSettingsHeaders = Record<string, never>;
export type BzaOperationFindSettingsResponse = Record<string, unknown>;
export type BzaOperationFindSettingsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationFindSettings<T = BzaOperationFindSettingsResponse>(options: BzaOperationFindSettingsOptions = {} as BzaOperationFindSettingsOptions): Promise<T> {
  const response = await orvalBzaOperationFindSettings({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationSaveAdminUserBody = { accountStatus?: string; adminName?: string; expectedVersion?: number; lockYn?: string; loginId?: string; passwordChangeRequiredYn?: string; rawPassword?: string; reason?: string; requestUser?: string; roleCode?: string; useYn?: string };
export type BzaOperationSaveAdminUserPath = Record<string, never>;
export type BzaOperationSaveAdminUserQuery = Record<string, never>;
export type BzaOperationSaveAdminUserHeaders = Record<string, never>;
export type BzaOperationSaveAdminUserResponse = Record<string, unknown>;
export type BzaOperationSaveAdminUserOptions = CpfGeneratedBaseOptions & { data: BzaOperationSaveAdminUserBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationSaveAdminUser<T = BzaOperationSaveAdminUserResponse>(options: BzaOperationSaveAdminUserOptions): Promise<T> {
  const response = await orvalBzaOperationSaveAdminUser(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationSaveMenuBody = { apiPath?: string; environmentCode?: string; expectedVersion?: number; iconCode?: string; menuCode?: string; menuName?: string; moduleCode?: string; parentMenuCode?: string; reason?: string; requestUser?: string; routePath?: string; sortOrder?: number; useYn?: string };
export type BzaOperationSaveMenuPath = Record<string, never>;
export type BzaOperationSaveMenuQuery = Record<string, never>;
export type BzaOperationSaveMenuHeaders = Record<string, never>;
export type BzaOperationSaveMenuResponse = Record<string, unknown>;
export type BzaOperationSaveMenuOptions = CpfGeneratedBaseOptions & { data: BzaOperationSaveMenuBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationSaveMenu<T = BzaOperationSaveMenuResponse>(options: BzaOperationSaveMenuOptions): Promise<T> {
  const response = await orvalBzaOperationSaveMenu(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationSavePermissionBody = { allowYn?: string; apiPattern?: string; buttonCode?: string; dataScope?: string; domainCode?: string; environmentCode?: string; expectedVersion?: number; httpMethod?: string; menuCode?: string; permissionId?: number; permissionType?: string; reason?: string; requestUser?: string; roleCode?: string; useYn?: string };
export type BzaOperationSavePermissionPath = Record<string, never>;
export type BzaOperationSavePermissionQuery = Record<string, never>;
export type BzaOperationSavePermissionHeaders = Record<string, never>;
export type BzaOperationSavePermissionResponse = Record<string, unknown>;
export type BzaOperationSavePermissionOptions = CpfGeneratedBaseOptions & { data: BzaOperationSavePermissionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationSavePermission<T = BzaOperationSavePermissionResponse>(options: BzaOperationSavePermissionOptions): Promise<T> {
  const response = await orvalBzaOperationSavePermission(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaOperationSaveRoleBody = { dataScope?: string; expectedVersion?: number; reason?: string; requestUser?: string; roleCode?: string; roleName?: string; useYn?: string; writeAllowedYn?: string };
export type BzaOperationSaveRolePath = Record<string, never>;
export type BzaOperationSaveRoleQuery = Record<string, never>;
export type BzaOperationSaveRoleHeaders = Record<string, never>;
export type BzaOperationSaveRoleResponse = Record<string, unknown>;
export type BzaOperationSaveRoleOptions = CpfGeneratedBaseOptions & { data: BzaOperationSaveRoleBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaOperationSaveRole<T = BzaOperationSaveRoleResponse>(options: BzaOperationSaveRoleOptions): Promise<T> {
  const response = await orvalBzaOperationSaveRole(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportCompareRolePermissionsBody = never;
export type BzaSupportCompareRolePermissionsPath = Record<string, never>;
export type BzaSupportCompareRolePermissionsQuery = { leftRoleCode: string; rightRoleCode: string };
export type BzaSupportCompareRolePermissionsHeaders = Record<string, never>;
export type BzaSupportCompareRolePermissionsResponse = Record<string, unknown>;
export type BzaSupportCompareRolePermissionsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportCompareRolePermissionsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportCompareRolePermissions<T = BzaSupportCompareRolePermissionsResponse>(options: BzaSupportCompareRolePermissionsOptions = {} as BzaSupportCompareRolePermissionsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportCompareRolePermissions(contractParams as Parameters<typeof orvalBzaSupportCompareRolePermissions>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportCreateNotificationBody = { messageBody?: string; notificationType?: string; reason?: string; recipientLoginId?: string; referenceId?: string; referenceType?: string; title?: string };
export type BzaSupportCreateNotificationPath = Record<string, never>;
export type BzaSupportCreateNotificationQuery = Record<string, never>;
export type BzaSupportCreateNotificationHeaders = Record<string, never>;
export type BzaSupportCreateNotificationResponse = Record<string, unknown>;
export type BzaSupportCreateNotificationOptions = CpfGeneratedBaseOptions & { data: BzaSupportCreateNotificationBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaSupportCreateNotification<T = BzaSupportCreateNotificationResponse>(options: BzaSupportCreateNotificationOptions): Promise<T> {
  const response = await orvalBzaSupportCreateNotification(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportDashboardBody = never;
export type BzaSupportDashboardPath = Record<string, never>;
export type BzaSupportDashboardQuery = Record<string, never>;
export type BzaSupportDashboardHeaders = Record<string, never>;
export type BzaSupportDashboardResponse = Record<string, unknown>;
export type BzaSupportDashboardOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaSupportDashboard<T = BzaSupportDashboardResponse>(options: BzaSupportDashboardOptions = {} as BzaSupportDashboardOptions): Promise<T> {
  const response = await orvalBzaSupportDashboard({ signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportDisableSavedSearchBody = never;
export type BzaSupportDisableSavedSearchPath = { savedSearchId: number };
export type BzaSupportDisableSavedSearchQuery = { reason: string };
export type BzaSupportDisableSavedSearchHeaders = Record<string, never>;
export type BzaSupportDisableSavedSearchResponse = Record<string, unknown>;
export type BzaSupportDisableSavedSearchOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaSupportDisableSavedSearchPath; query?: BzaSupportDisableSavedSearchQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportDisableSavedSearch<T = BzaSupportDisableSavedSearchResponse>(options: BzaSupportDisableSavedSearchOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportDisableSavedSearch(options.path["savedSearchId"], contractParams as Parameters<typeof orvalBzaSupportDisableSavedSearch>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportDownloadAttachmentBody = never;
export type BzaSupportDownloadAttachmentPath = { attachmentId: number };
export type BzaSupportDownloadAttachmentQuery = { reason: string };
export type BzaSupportDownloadAttachmentHeaders = Record<string, never>;
export type BzaSupportDownloadAttachmentResponse = Record<string, unknown>;
export type BzaSupportDownloadAttachmentOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaSupportDownloadAttachmentPath; query?: BzaSupportDownloadAttachmentQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportDownloadAttachment<T = BzaSupportDownloadAttachmentResponse>(options: BzaSupportDownloadAttachmentOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportDownloadAttachment(options.path["attachmentId"], contractParams as Parameters<typeof orvalBzaSupportDownloadAttachment>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportFindAttachmentsBody = never;
export type BzaSupportFindAttachmentsPath = Record<string, never>;
export type BzaSupportFindAttachmentsQuery = { groupId: string };
export type BzaSupportFindAttachmentsHeaders = Record<string, never>;
export type BzaSupportFindAttachmentsResponse = Record<string, unknown>;
export type BzaSupportFindAttachmentsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportFindAttachmentsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportFindAttachments<T = BzaSupportFindAttachmentsResponse>(options: BzaSupportFindAttachmentsOptions = {} as BzaSupportFindAttachmentsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportFindAttachments(contractParams as Parameters<typeof orvalBzaSupportFindAttachments>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportFindDownloadAuditsBody = never;
export type BzaSupportFindDownloadAuditsPath = Record<string, never>;
export type BzaSupportFindDownloadAuditsQuery = { limit?: number };
export type BzaSupportFindDownloadAuditsHeaders = Record<string, never>;
export type BzaSupportFindDownloadAuditsResponse = Record<string, unknown>;
export type BzaSupportFindDownloadAuditsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportFindDownloadAuditsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportFindDownloadAudits<T = BzaSupportFindDownloadAuditsResponse>(options: BzaSupportFindDownloadAuditsOptions = {} as BzaSupportFindDownloadAuditsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportFindDownloadAudits(contractParams as Parameters<typeof orvalBzaSupportFindDownloadAudits>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportFindNotificationsBody = never;
export type BzaSupportFindNotificationsPath = Record<string, never>;
export type BzaSupportFindNotificationsQuery = { unreadOnly?: boolean; limit?: number };
export type BzaSupportFindNotificationsHeaders = Record<string, never>;
export type BzaSupportFindNotificationsResponse = Record<string, unknown>;
export type BzaSupportFindNotificationsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportFindNotificationsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportFindNotifications<T = BzaSupportFindNotificationsResponse>(options: BzaSupportFindNotificationsOptions = {} as BzaSupportFindNotificationsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportFindNotifications(contractParams as Parameters<typeof orvalBzaSupportFindNotifications>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportFindSavedSearchesBody = never;
export type BzaSupportFindSavedSearchesPath = Record<string, never>;
export type BzaSupportFindSavedSearchesQuery = { screenCode?: string };
export type BzaSupportFindSavedSearchesHeaders = Record<string, never>;
export type BzaSupportFindSavedSearchesResponse = Record<string, unknown>;
export type BzaSupportFindSavedSearchesOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportFindSavedSearchesQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportFindSavedSearches<T = BzaSupportFindSavedSearchesResponse>(options: BzaSupportFindSavedSearchesOptions = {} as BzaSupportFindSavedSearchesOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportFindSavedSearches(contractParams as Parameters<typeof orvalBzaSupportFindSavedSearches>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportReadAllNotificationsBody = never;
export type BzaSupportReadAllNotificationsPath = Record<string, never>;
export type BzaSupportReadAllNotificationsQuery = { reason: string };
export type BzaSupportReadAllNotificationsHeaders = Record<string, never>;
export type BzaSupportReadAllNotificationsResponse = Record<string, unknown>;
export type BzaSupportReadAllNotificationsOptions = CpfGeneratedBaseOptions & { data?: never; path?: never; query?: BzaSupportReadAllNotificationsQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportReadAllNotifications<T = BzaSupportReadAllNotificationsResponse>(options: BzaSupportReadAllNotificationsOptions = {} as BzaSupportReadAllNotificationsOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportReadAllNotifications(contractParams as Parameters<typeof orvalBzaSupportReadAllNotifications>[0], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportReadNotificationBody = never;
export type BzaSupportReadNotificationPath = { notificationId: number };
export type BzaSupportReadNotificationQuery = { reason: string };
export type BzaSupportReadNotificationHeaders = Record<string, never>;
export type BzaSupportReadNotificationResponse = Record<string, unknown>;
export type BzaSupportReadNotificationOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaSupportReadNotificationPath; query?: BzaSupportReadNotificationQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportReadNotification<T = BzaSupportReadNotificationResponse>(options: BzaSupportReadNotificationOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportReadNotification(options.path["notificationId"], contractParams as Parameters<typeof orvalBzaSupportReadNotification>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportRecheckAttachmentBody = never;
export type BzaSupportRecheckAttachmentPath = { attachmentId: number };
export type BzaSupportRecheckAttachmentQuery = { reason: string };
export type BzaSupportRecheckAttachmentHeaders = Record<string, never>;
export type BzaSupportRecheckAttachmentResponse = Record<string, unknown>;
export type BzaSupportRecheckAttachmentOptions = CpfGeneratedBaseOptions & { data?: never; path: BzaSupportRecheckAttachmentPath; query?: BzaSupportRecheckAttachmentQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportRecheckAttachment<T = BzaSupportRecheckAttachmentResponse>(options: BzaSupportRecheckAttachmentOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportRecheckAttachment(options.path["attachmentId"], contractParams as Parameters<typeof orvalBzaSupportRecheckAttachment>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportSaveSavedSearchBody = { criteriaJson?: string; reason?: string; screenCode?: string; searchName?: string; sharedYn?: string };
export type BzaSupportSaveSavedSearchPath = Record<string, never>;
export type BzaSupportSaveSavedSearchQuery = Record<string, never>;
export type BzaSupportSaveSavedSearchHeaders = Record<string, never>;
export type BzaSupportSaveSavedSearchResponse = Record<string, unknown>;
export type BzaSupportSaveSavedSearchOptions = CpfGeneratedBaseOptions & { data: BzaSupportSaveSavedSearchBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaSupportSaveSavedSearch<T = BzaSupportSaveSavedSearchResponse>(options: BzaSupportSaveSavedSearchOptions): Promise<T> {
  const response = await orvalBzaSupportSaveSavedSearch(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportSimulatePermissionBody = { actionCode?: string; apiPath?: string; domainCode?: string; environmentCode?: string; httpMethod?: string; menuCode?: string; reason?: string; roleCode?: string };
export type BzaSupportSimulatePermissionPath = Record<string, never>;
export type BzaSupportSimulatePermissionQuery = Record<string, never>;
export type BzaSupportSimulatePermissionHeaders = Record<string, never>;
export type BzaSupportSimulatePermissionResponse = Record<string, unknown>;
export type BzaSupportSimulatePermissionOptions = CpfGeneratedBaseOptions & { data: BzaSupportSimulatePermissionBody; path?: never; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaSupportSimulatePermission<T = BzaSupportSimulatePermissionResponse>(options: BzaSupportSimulatePermissionOptions): Promise<T> {
  const response = await orvalBzaSupportSimulatePermission(options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportUpdateAttachmentSecurityBody = { dataClassification?: string; quarantineYn?: string; reason?: string; retentionUntil?: string; scanStatus?: string; useYn?: string };
export type BzaSupportUpdateAttachmentSecurityPath = { attachmentId: number };
export type BzaSupportUpdateAttachmentSecurityQuery = Record<string, never>;
export type BzaSupportUpdateAttachmentSecurityHeaders = Record<string, never>;
export type BzaSupportUpdateAttachmentSecurityResponse = Record<string, unknown>;
export type BzaSupportUpdateAttachmentSecurityOptions = CpfGeneratedBaseOptions & { data: BzaSupportUpdateAttachmentSecurityBody; path: BzaSupportUpdateAttachmentSecurityPath; query?: never; headers?: CpfGeneratedHeaders; };
export async function bzaSupportUpdateAttachmentSecurity<T = BzaSupportUpdateAttachmentSecurityResponse>(options: BzaSupportUpdateAttachmentSecurityOptions): Promise<T> {
  const response = await orvalBzaSupportUpdateAttachmentSecurity(options.path["attachmentId"], options.data, { signal: options.signal, headers: options.headers });
  return response.data as T;
}

export type BzaSupportUploadAttachmentBody = FormData;
export type BzaSupportUploadAttachmentPath = Record<string, never>;
export type BzaSupportUploadAttachmentQuery = { groupId: string; reason: string };
export type BzaSupportUploadAttachmentHeaders = Record<string, never>;
export type BzaSupportUploadAttachmentResponse = Record<string, unknown>;
export type BzaSupportUploadAttachmentOptions = CpfGeneratedBaseOptions & { data: BzaSupportUploadAttachmentBody; path?: never; query?: BzaSupportUploadAttachmentQuery; headers?: CpfGeneratedHeaders; };
export async function bzaSupportUploadAttachment<T = BzaSupportUploadAttachmentResponse>(options: BzaSupportUploadAttachmentOptions): Promise<T> {
  const contractParams = options.query || {};
  const response = await orvalBzaSupportUploadAttachment(options.data, contractParams as Parameters<typeof orvalBzaSupportUploadAttachment>[1], { signal: options.signal, headers: options.headers });
  return response.data as T;
}

