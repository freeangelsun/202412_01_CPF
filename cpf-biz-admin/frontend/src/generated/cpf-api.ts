// Generated compatibility adapter backed by the canonical full OpenAPI.
import { cpfGeneratedRequest } from "../shared/cpfApi";
export interface CpfGeneratedRequestOptions { data?: unknown; signal?: AbortSignal; headers?: HeadersInit; path?: Record<string, string | number>; query?: Record<string, unknown>; }
function renderPath(template: string, values: Record<string, string | number> = {}): string { return template.replace(/\{([^}]+)\}/g, (_, name) => { const value = values[name]; if (value === undefined || value === null || String(value).trim() === "") throw new Error(`Missing path parameter: ${name}`); return encodeURIComponent(String(value)); }); }
export async function bzaOperationDeleteMenu<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/menus/{menuCode}", options.path), method: "DELETE", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindAdminUsers<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/admin-users", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindAdminUsersPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/admin-users/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalDelegations<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/delegations", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalInbox<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/inbox", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/policies", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalPolicyDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/policies/{policyCode}/{version}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalSubmissions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalSubmissionDetail<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions/{approvalId}", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportFindAttachments<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/attachments", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportDownloadAttachment<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/attachments/{attachmentId}/download", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBusinessAuditVerify<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/audits/verify", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthLoginHistories<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/login-history", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthMe<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/me", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthSessions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/sessions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindBusinessAudits<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/audits", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindEmployees<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/employees", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindEmployeesPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/employees/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindOrganizations<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/organizations", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindOrganizationsPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/organizations/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeFindEffectivePermissions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/permissions/effective", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportDashboard<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/dashboard", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindAssignments<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/assignments", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindAssignmentsPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/assignments/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindJobTitles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/job-titles", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindJobTitlesPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/job-titles/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindPositions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/positions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindPositionsPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/positions/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindResponsibilities<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/responsibilities", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindResponsibilitiesPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/responsibilities/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindUserRoles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/user-roles", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectoryFindUserRolesPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/user-roles/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportFindDownloadAudits<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/download-audits", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindDownloadPolicies<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/downloads", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindMenus<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/menus", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindMenuImpact<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/menus/{menuCode}/impact", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindMenusPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/menus/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportFindNotifications<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/notifications", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindPermissions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/permissions", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportCompareRolePermissions<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/permissions/compare", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindPermissionsPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/permissions/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindRoles<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/roles", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindRolesPage<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/roles/page", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportFindSavedSearches<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/saved-searches", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationFindSettings<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/settings", options.path), method: "GET", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationSaveAdminUser<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/admin-users", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalParticipantDecision<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/{approvalId}/decisions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalDelegationSave<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/delegations", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalPolicySave<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/policies", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalPolicySimulate<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/simulate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalPolicySubmit<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalCancel<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions/{approvalId}/cancel", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalResubmit<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions/{approvalId}/resubmit", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalWithdraw<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions/{approvalId}/withdraw", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaApprovalExpireDue<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/approvals/submissions/expire-due", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportUploadAttachment<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/attachments", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportRecheckAttachment<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/attachments/{attachmentId}/recheck", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportUpdateAttachmentSecurity<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/attachments/{attachmentId}/security", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthLogin<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/login", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthLogout<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/logout", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthChangePassword<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/password/change", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthRefresh<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/refresh", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaAuthRevokeSession<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/auth/sessions/{sessionId}/revoke", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeSaveEmployee<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/employees", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeEmployeeRawContact<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/employees/{employeeNo}/contacts/raw", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaBackofficeSaveOrganization<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/backoffice/organizations", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectorySaveAssignment<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/assignments", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectorySaveJobTitle<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/job-titles", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectorySavePosition<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/positions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectorySaveResponsibility<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/responsibilities", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaDirectorySaveUserRole<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/directory/user-roles", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationSaveMenu<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/menus", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportCreateNotification<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/notifications", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportReadNotification<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/notifications/{notificationId}/read", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportReadAllNotifications<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/notifications/read-all", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationSavePermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/permissions", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportSimulatePermission<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/permissions/simulate", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaOperationSaveRole<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/roles", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportSaveSavedSearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/saved-searches", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
export async function bzaSupportDisableSavedSearch<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {
  return cpfGeneratedRequest<T>({ url: renderPath("/api/bza/saved-searches/{savedSearchId}/disable", options.path), method: "POST", data: options.data, params: options.query, signal: options.signal, headers: options.headers });
}
