/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */
import { invokeBackoffice } from '../shared/api/channelHttpClient'
export const cpfBackofficeGeneratedOperations = [
  {
    "operationId": "MBW_APPROVAL_CANCEL",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/submissions/{approvalId}/cancel",
    "name": "approvalCancel"
  },
  {
    "operationId": "MBW_APPROVAL_DELEGATION_SAVE",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/delegations",
    "name": "approvalDelegationSave"
  },
  {
    "operationId": "MBW_APPROVAL_DELEGATIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/delegations",
    "name": "approvalDelegations"
  },
  {
    "operationId": "MBW_APPROVAL_EXPIRE_DUE",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/submissions/expire-due",
    "name": "approvalExpireDue"
  },
  {
    "operationId": "MBW_APPROVAL_INBOX",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/inbox",
    "name": "approvalInbox"
  },
  {
    "operationId": "MBW_APPROVAL_PARTICIPANT_DECISION",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/{approvalId}/decisions",
    "name": "approvalParticipantDecision"
  },
  {
    "operationId": "MBW_APPROVAL_POLICIES",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/policies",
    "name": "approvalPolicies"
  },
  {
    "operationId": "MBW_APPROVAL_POLICY_DETAIL",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/policies/{policyCode}/{version}",
    "name": "approvalPolicyDetail"
  },
  {
    "operationId": "MBW_APPROVAL_POLICY_SAVE",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/policies",
    "name": "approvalPolicySave"
  },
  {
    "operationId": "MBW_APPROVAL_POLICY_SIMULATE",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/simulate",
    "name": "approvalPolicySimulate"
  },
  {
    "operationId": "MBW_APPROVAL_POLICY_SUBMIT",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/submissions",
    "name": "approvalPolicySubmit"
  },
  {
    "operationId": "MBW_APPROVAL_RESUBMIT",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/submissions/{approvalId}/resubmit",
    "name": "approvalResubmit"
  },
  {
    "operationId": "MBW_APPROVAL_SUBMISSION_DETAIL",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/submissions/{approvalId}",
    "name": "approvalSubmissionDetail"
  },
  {
    "operationId": "MBW_APPROVAL_SUBMISSIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/approvals/submissions",
    "name": "approvalSubmissions"
  },
  {
    "operationId": "MBW_APPROVAL_WITHDRAW",
    "method": "POST",
    "path": "/api/v1/backoffice/approvals/submissions/{approvalId}/withdraw",
    "name": "approvalWithdraw"
  },
  {
    "operationId": "MBW_AUTH_CHANGE_PASSWORD",
    "method": "POST",
    "path": "/api/v1/backoffice/auth/password/change",
    "name": "authChangePassword"
  },
  {
    "operationId": "MBW_AUTH_LOGIN",
    "method": "POST",
    "path": "/api/v1/backoffice/auth/login",
    "name": "authLogin"
  },
  {
    "operationId": "MBW_AUTH_LOGIN_HISTORIES",
    "method": "GET",
    "path": "/api/v1/backoffice/auth/login-history",
    "name": "authLoginHistories"
  },
  {
    "operationId": "MBW_AUTH_LOGOUT",
    "method": "POST",
    "path": "/api/v1/backoffice/auth/logout",
    "name": "authLogout"
  },
  {
    "operationId": "MBW_AUTH_ME",
    "method": "GET",
    "path": "/api/v1/backoffice/auth/me",
    "name": "authMe"
  },
  {
    "operationId": "MBW_AUTH_REFRESH",
    "method": "POST",
    "path": "/api/v1/backoffice/auth/refresh",
    "name": "authRefresh"
  },
  {
    "operationId": "MBW_AUTH_REVOKE_SESSION",
    "method": "POST",
    "path": "/api/v1/backoffice/auth/sessions/{sessionId}/revoke",
    "name": "authRevokeSession"
  },
  {
    "operationId": "MBW_AUTH_SESSIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/auth/sessions",
    "name": "authSessions"
  },
  {
    "operationId": "MBW_BACKOFFICE_EMPLOYEE_RAW_CONTACT",
    "method": "POST",
    "path": "/api/v1/backoffice/backoffice/employees/{employeeNo}/contacts/raw",
    "name": "backofficeEmployeeRawContact"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_BUSINESS_AUDITS",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/audits",
    "name": "backofficeFindBusinessAudits"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_EFFECTIVE_PERMISSIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/permissions/effective",
    "name": "backofficeFindEffectivePermissions"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_EMPLOYEES",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/employees",
    "name": "backofficeFindEmployees"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_EMPLOYEES_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/employees/page",
    "name": "backofficeFindEmployeesPage"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_ORGANIZATIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/organizations",
    "name": "backofficeFindOrganizations"
  },
  {
    "operationId": "MBW_BACKOFFICE_FIND_ORGANIZATIONS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/backoffice/organizations/page",
    "name": "backofficeFindOrganizationsPage"
  },
  {
    "operationId": "MBW_BACKOFFICE_SAVE_EMPLOYEE",
    "method": "POST",
    "path": "/api/v1/backoffice/backoffice/employees",
    "name": "backofficeSaveEmployee"
  },
  {
    "operationId": "MBW_BACKOFFICE_SAVE_ORGANIZATION",
    "method": "POST",
    "path": "/api/v1/backoffice/backoffice/organizations",
    "name": "backofficeSaveOrganization"
  },
  {
    "operationId": "MBW_BUSINESS_AUDIT_VERIFY",
    "method": "GET",
    "path": "/api/v1/backoffice/audits/verify",
    "name": "businessAuditVerify"
  },
  {
    "operationId": "MBW_COMMON_CATALOG_REFRESH",
    "method": "POST",
    "path": "/api/v1/backoffice/common-catalog/cache/refresh",
    "name": "commonCatalogRefresh"
  },
  {
    "operationId": "MBW_COMMON_CREATE",
    "method": "POST",
    "path": "/api/v1/backoffice/common/{resource}",
    "name": "commonCreate"
  },
  {
    "operationId": "MBW_COMMON_DELETE",
    "method": "DELETE",
    "path": "/api/v1/backoffice/common/{resource}",
    "name": "commonDelete"
  },
  {
    "operationId": "MBW_COMMON_DETAIL",
    "method": "POST",
    "path": "/api/v1/backoffice/common/{resource}/detail",
    "name": "commonDetail"
  },
  {
    "operationId": "MBW_COMMON_MESSAGE_CREATE",
    "method": "POST",
    "path": "/api/v1/backoffice/common-catalog/messages",
    "name": "commonMessageCreate"
  },
  {
    "operationId": "MBW_COMMON_MESSAGE_DETAIL",
    "method": "GET",
    "path": "/api/v1/backoffice/common-catalog/messages/{id}",
    "name": "commonMessageDetail"
  },
  {
    "operationId": "MBW_COMMON_MESSAGE_DISABLE",
    "method": "DELETE",
    "path": "/api/v1/backoffice/common-catalog/messages/{id}",
    "name": "commonMessageDisable"
  },
  {
    "operationId": "MBW_COMMON_MESSAGE_SEARCH",
    "method": "GET",
    "path": "/api/v1/backoffice/common-catalog/messages",
    "name": "commonMessageSearch"
  },
  {
    "operationId": "MBW_COMMON_MESSAGE_UPDATE",
    "method": "PUT",
    "path": "/api/v1/backoffice/common-catalog/messages/{id}",
    "name": "commonMessageUpdate"
  },
  {
    "operationId": "MBW_COMMON_RESPONSE_CODE_CREATE",
    "method": "POST",
    "path": "/api/v1/backoffice/common-catalog/response-codes",
    "name": "commonResponseCodeCreate"
  },
  {
    "operationId": "MBW_COMMON_RESPONSE_CODE_DETAIL",
    "method": "GET",
    "path": "/api/v1/backoffice/common-catalog/response-codes/{code}",
    "name": "commonResponseCodeDetail"
  },
  {
    "operationId": "MBW_COMMON_RESPONSE_CODE_DISABLE",
    "method": "DELETE",
    "path": "/api/v1/backoffice/common-catalog/response-codes/{code}",
    "name": "commonResponseCodeDisable"
  },
  {
    "operationId": "MBW_COMMON_RESPONSE_CODE_SEARCH",
    "method": "GET",
    "path": "/api/v1/backoffice/common-catalog/response-codes",
    "name": "commonResponseCodeSearch"
  },
  {
    "operationId": "MBW_COMMON_RESPONSE_CODE_UPDATE",
    "method": "PUT",
    "path": "/api/v1/backoffice/common-catalog/response-codes/{code}",
    "name": "commonResponseCodeUpdate"
  },
  {
    "operationId": "MBW_COMMON_SEARCH",
    "method": "GET",
    "path": "/api/v1/backoffice/common/{resource}",
    "name": "commonSearch"
  },
  {
    "operationId": "MBW_COMMON_UPDATE",
    "method": "PUT",
    "path": "/api/v1/backoffice/common/{resource}",
    "name": "commonUpdate"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_ASSIGNMENTS",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/assignments",
    "name": "directoryFindAssignments"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_ASSIGNMENTS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/assignments/page",
    "name": "directoryFindAssignmentsPage"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_JOB_TITLES",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/job-titles",
    "name": "directoryFindJobTitles"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_JOB_TITLES_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/job-titles/page",
    "name": "directoryFindJobTitlesPage"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_POSITIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/positions",
    "name": "directoryFindPositions"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_POSITIONS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/positions/page",
    "name": "directoryFindPositionsPage"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_RESPONSIBILITIES",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/responsibilities",
    "name": "directoryFindResponsibilities"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_RESPONSIBILITIES_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/responsibilities/page",
    "name": "directoryFindResponsibilitiesPage"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_USER_ROLES",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/user-roles",
    "name": "directoryFindUserRoles"
  },
  {
    "operationId": "MBW_DIRECTORY_FIND_USER_ROLES_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/directory/user-roles/page",
    "name": "directoryFindUserRolesPage"
  },
  {
    "operationId": "MBW_DIRECTORY_SAVE_ASSIGNMENT",
    "method": "POST",
    "path": "/api/v1/backoffice/directory/assignments",
    "name": "directorySaveAssignment"
  },
  {
    "operationId": "MBW_DIRECTORY_SAVE_JOB_TITLE",
    "method": "POST",
    "path": "/api/v1/backoffice/directory/job-titles",
    "name": "directorySaveJobTitle"
  },
  {
    "operationId": "MBW_DIRECTORY_SAVE_POSITION",
    "method": "POST",
    "path": "/api/v1/backoffice/directory/positions",
    "name": "directorySavePosition"
  },
  {
    "operationId": "MBW_DIRECTORY_SAVE_RESPONSIBILITY",
    "method": "POST",
    "path": "/api/v1/backoffice/directory/responsibilities",
    "name": "directorySaveResponsibility"
  },
  {
    "operationId": "MBW_DIRECTORY_SAVE_USER_ROLE",
    "method": "POST",
    "path": "/api/v1/backoffice/directory/user-roles",
    "name": "directorySaveUserRole"
  },
  {
    "operationId": "MBW_OPERATION_DELETE_MENU",
    "method": "DELETE",
    "path": "/api/v1/backoffice/menus/{menuCode}",
    "name": "operationDeleteMenu"
  },
  {
    "operationId": "MBW_OPERATION_FIND_ADMIN_USERS",
    "method": "GET",
    "path": "/api/v1/backoffice/admin-users",
    "name": "operationFindAdminUsers"
  },
  {
    "operationId": "MBW_OPERATION_FIND_ADMIN_USERS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/admin-users/page",
    "name": "operationFindAdminUsersPage"
  },
  {
    "operationId": "MBW_OPERATION_FIND_DOWNLOAD_POLICIES",
    "method": "GET",
    "path": "/api/v1/backoffice/downloads",
    "name": "operationFindDownloadPolicies"
  },
  {
    "operationId": "MBW_OPERATION_FIND_MENU_IMPACT",
    "method": "GET",
    "path": "/api/v1/backoffice/menus/{menuCode}/impact",
    "name": "operationFindMenuImpact"
  },
  {
    "operationId": "MBW_OPERATION_FIND_MENUS",
    "method": "GET",
    "path": "/api/v1/backoffice/menus",
    "name": "operationFindMenus"
  },
  {
    "operationId": "MBW_OPERATION_FIND_MENUS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/menus/page",
    "name": "operationFindMenusPage"
  },
  {
    "operationId": "MBW_OPERATION_FIND_PERMISSIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/permissions",
    "name": "operationFindPermissions"
  },
  {
    "operationId": "MBW_OPERATION_FIND_PERMISSIONS_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/permissions/page",
    "name": "operationFindPermissionsPage"
  },
  {
    "operationId": "MBW_OPERATION_FIND_ROLES",
    "method": "GET",
    "path": "/api/v1/backoffice/roles",
    "name": "operationFindRoles"
  },
  {
    "operationId": "MBW_OPERATION_FIND_ROLES_PAGE",
    "method": "GET",
    "path": "/api/v1/backoffice/roles/page",
    "name": "operationFindRolesPage"
  },
  {
    "operationId": "MBW_OPERATION_FIND_SETTINGS",
    "method": "GET",
    "path": "/api/v1/backoffice/settings",
    "name": "operationFindSettings"
  },
  {
    "operationId": "MBW_OPERATION_SAVE_ADMIN_USER",
    "method": "POST",
    "path": "/api/v1/backoffice/admin-users",
    "name": "operationSaveAdminUser"
  },
  {
    "operationId": "MBW_OPERATION_SAVE_MENU",
    "method": "POST",
    "path": "/api/v1/backoffice/menus",
    "name": "operationSaveMenu"
  },
  {
    "operationId": "MBW_OPERATION_SAVE_PERMISSION",
    "method": "POST",
    "path": "/api/v1/backoffice/permissions",
    "name": "operationSavePermission"
  },
  {
    "operationId": "MBW_OPERATION_SAVE_ROLE",
    "method": "POST",
    "path": "/api/v1/backoffice/roles",
    "name": "operationSaveRole"
  },
  {
    "operationId": "MBW_SUPPORT_COMPARE_ROLE_PERMISSIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/permissions/compare",
    "name": "supportCompareRolePermissions"
  },
  {
    "operationId": "MBW_SUPPORT_CREATE_NOTIFICATION",
    "method": "POST",
    "path": "/api/v1/backoffice/notifications",
    "name": "supportCreateNotification"
  },
  {
    "operationId": "MBW_SUPPORT_DASHBOARD",
    "method": "GET",
    "path": "/api/v1/backoffice/dashboard",
    "name": "supportDashboard"
  },
  {
    "operationId": "MBW_SUPPORT_DISABLE_SAVED_SEARCH",
    "method": "POST",
    "path": "/api/v1/backoffice/saved-searches/{savedSearchId}/disable",
    "name": "supportDisableSavedSearch"
  },
  {
    "operationId": "MBW_SUPPORT_DOWNLOAD_ATTACHMENT",
    "method": "GET",
    "path": "/api/v1/backoffice/attachments/{attachmentId}/download",
    "name": "supportDownloadAttachment"
  },
  {
    "operationId": "MBW_SUPPORT_FIND_ATTACHMENTS",
    "method": "GET",
    "path": "/api/v1/backoffice/attachments",
    "name": "supportFindAttachments"
  },
  {
    "operationId": "MBW_SUPPORT_FIND_DOWNLOAD_AUDITS",
    "method": "GET",
    "path": "/api/v1/backoffice/download-audits",
    "name": "supportFindDownloadAudits"
  },
  {
    "operationId": "MBW_SUPPORT_FIND_NOTIFICATIONS",
    "method": "GET",
    "path": "/api/v1/backoffice/notifications",
    "name": "supportFindNotifications"
  },
  {
    "operationId": "MBW_SUPPORT_FIND_SAVED_SEARCHES",
    "method": "GET",
    "path": "/api/v1/backoffice/saved-searches",
    "name": "supportFindSavedSearches"
  },
  {
    "operationId": "MBW_SUPPORT_READ_ALL_NOTIFICATIONS",
    "method": "POST",
    "path": "/api/v1/backoffice/notifications/read-all",
    "name": "supportReadAllNotifications"
  },
  {
    "operationId": "MBW_SUPPORT_READ_NOTIFICATION",
    "method": "POST",
    "path": "/api/v1/backoffice/notifications/{notificationId}/read",
    "name": "supportReadNotification"
  },
  {
    "operationId": "MBW_SUPPORT_RECHECK_ATTACHMENT",
    "method": "POST",
    "path": "/api/v1/backoffice/attachments/{attachmentId}/recheck",
    "name": "supportRecheckAttachment"
  },
  {
    "operationId": "MBW_SUPPORT_SAVE_SAVED_SEARCH",
    "method": "POST",
    "path": "/api/v1/backoffice/saved-searches",
    "name": "supportSaveSavedSearch"
  },
  {
    "operationId": "MBW_SUPPORT_SIMULATE_PERMISSION",
    "method": "POST",
    "path": "/api/v1/backoffice/permissions/simulate",
    "name": "supportSimulatePermission"
  },
  {
    "operationId": "MBW_SUPPORT_UPDATE_ATTACHMENT_SECURITY",
    "method": "POST",
    "path": "/api/v1/backoffice/attachments/{attachmentId}/security",
    "name": "supportUpdateAttachmentSecurity"
  },
  {
    "operationId": "MBW_SUPPORT_UPLOAD_ATTACHMENT",
    "method": "POST",
    "path": "/api/v1/backoffice/attachments",
    "name": "supportUploadAttachment"
  }
] as const
export async function approvalCancel(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/submissions/${encodeURIComponent(approvalId)}/cancel`, options) }
export async function approvalDelegationSave(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/delegations`, options) }
export async function approvalDelegations(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/delegations`, options) }
export async function approvalExpireDue(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/submissions/expire-due`, options) }
export async function approvalInbox(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/inbox`, options) }
export async function approvalParticipantDecision(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/${encodeURIComponent(approvalId)}/decisions`, options) }
export async function approvalPolicies(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/policies`, options) }
export async function approvalPolicyDetail(policyCode: string, version: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/policies/${encodeURIComponent(policyCode)}/${encodeURIComponent(version)}`, options) }
export async function approvalPolicySave(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/policies`, options) }
export async function approvalPolicySimulate(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/simulate`, options) }
export async function approvalPolicySubmit(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/submissions`, options) }
export async function approvalResubmit(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/submissions/${encodeURIComponent(approvalId)}/resubmit`, options) }
export async function approvalSubmissionDetail(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/submissions/${encodeURIComponent(approvalId)}`, options) }
export async function approvalSubmissions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/submissions`, options) }
export async function approvalWithdraw(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/submissions/${encodeURIComponent(approvalId)}/withdraw`, options) }
export async function authChangePassword(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/auth/password/change`, options) }
export async function authLogin(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/auth/login`, options) }
export async function authLoginHistories(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/auth/login-history`, options) }
export async function authLogout(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/auth/logout`, options) }
export async function authMe(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/auth/me`, options) }
export async function authRefresh(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/auth/refresh`, options) }
export async function authRevokeSession(sessionId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/auth/sessions/${encodeURIComponent(sessionId)}/revoke`, options) }
export async function authSessions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/auth/sessions`, options) }
export async function backofficeEmployeeRawContact(employeeNo: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/backoffice/employees/${encodeURIComponent(employeeNo)}/contacts/raw`, options) }
export async function backofficeFindBusinessAudits(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/audits`, options) }
export async function backofficeFindEffectivePermissions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/permissions/effective`, options) }
export async function backofficeFindEmployees(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/employees`, options) }
export async function backofficeFindEmployeesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/employees/page`, options) }
export async function backofficeFindOrganizations(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/organizations`, options) }
export async function backofficeFindOrganizationsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/organizations/page`, options) }
export async function backofficeSaveEmployee(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/backoffice/employees`, options) }
export async function backofficeSaveOrganization(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/backoffice/organizations`, options) }
export async function businessAuditVerify(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/audits/verify`, options) }
export async function commonCatalogRefresh(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/common-catalog/cache/refresh`, options) }
export async function commonCreate(resource: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/common/${encodeURIComponent(resource)}`, options) }
export async function commonDelete(resource: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("DELETE", `/api/v1/backoffice/common/${encodeURIComponent(resource)}`, options) }
export async function commonDetail(resource: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/common/${encodeURIComponent(resource)}/detail`, options) }
export async function commonMessageCreate(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/common-catalog/messages`, options) }
export async function commonMessageDetail(id: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/common-catalog/messages/${encodeURIComponent(id)}`, options) }
export async function commonMessageDisable(id: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("DELETE", `/api/v1/backoffice/common-catalog/messages/${encodeURIComponent(id)}`, options) }
export async function commonMessageSearch(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/common-catalog/messages`, options) }
export async function commonMessageUpdate(id: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("PUT", `/api/v1/backoffice/common-catalog/messages/${encodeURIComponent(id)}`, options) }
export async function commonResponseCodeCreate(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/common-catalog/response-codes`, options) }
export async function commonResponseCodeDetail(code: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/common-catalog/response-codes/${encodeURIComponent(code)}`, options) }
export async function commonResponseCodeDisable(code: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("DELETE", `/api/v1/backoffice/common-catalog/response-codes/${encodeURIComponent(code)}`, options) }
export async function commonResponseCodeSearch(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/common-catalog/response-codes`, options) }
export async function commonResponseCodeUpdate(code: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("PUT", `/api/v1/backoffice/common-catalog/response-codes/${encodeURIComponent(code)}`, options) }
export async function commonSearch(resource: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/common/${encodeURIComponent(resource)}`, options) }
export async function commonUpdate(resource: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("PUT", `/api/v1/backoffice/common/${encodeURIComponent(resource)}`, options) }
export async function directoryFindAssignments(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/assignments`, options) }
export async function directoryFindAssignmentsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/assignments/page`, options) }
export async function directoryFindJobTitles(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/job-titles`, options) }
export async function directoryFindJobTitlesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/job-titles/page`, options) }
export async function directoryFindPositions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/positions`, options) }
export async function directoryFindPositionsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/positions/page`, options) }
export async function directoryFindResponsibilities(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/responsibilities`, options) }
export async function directoryFindResponsibilitiesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/responsibilities/page`, options) }
export async function directoryFindUserRoles(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/user-roles`, options) }
export async function directoryFindUserRolesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/directory/user-roles/page`, options) }
export async function directorySaveAssignment(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/directory/assignments`, options) }
export async function directorySaveJobTitle(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/directory/job-titles`, options) }
export async function directorySavePosition(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/directory/positions`, options) }
export async function directorySaveResponsibility(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/directory/responsibilities`, options) }
export async function directorySaveUserRole(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/directory/user-roles`, options) }
export async function operationDeleteMenu(menuCode: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("DELETE", `/api/v1/backoffice/menus/${encodeURIComponent(menuCode)}`, options) }
export async function operationFindAdminUsers(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/admin-users`, options) }
export async function operationFindAdminUsersPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/admin-users/page`, options) }
export async function operationFindDownloadPolicies(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/downloads`, options) }
export async function operationFindMenuImpact(menuCode: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/menus/${encodeURIComponent(menuCode)}/impact`, options) }
export async function operationFindMenus(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/menus`, options) }
export async function operationFindMenusPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/menus/page`, options) }
export async function operationFindPermissions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/permissions`, options) }
export async function operationFindPermissionsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/permissions/page`, options) }
export async function operationFindRoles(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/roles`, options) }
export async function operationFindRolesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/roles/page`, options) }
export async function operationFindSettings(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/settings`, options) }
export async function operationSaveAdminUser(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/admin-users`, options) }
export async function operationSaveMenu(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/menus`, options) }
export async function operationSavePermission(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/permissions`, options) }
export async function operationSaveRole(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/roles`, options) }
export async function supportCompareRolePermissions(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/permissions/compare`, options) }
export async function supportCreateNotification(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/notifications`, options) }
export async function supportDashboard(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/dashboard`, options) }
export async function supportDisableSavedSearch(savedSearchId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/saved-searches/${encodeURIComponent(savedSearchId)}/disable`, options) }
export async function supportDownloadAttachment(attachmentId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/attachments/${encodeURIComponent(attachmentId)}/download`, options) }
export async function supportFindAttachments(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/attachments`, options) }
export async function supportFindDownloadAudits(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/download-audits`, options) }
export async function supportFindNotifications(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/notifications`, options) }
export async function supportFindSavedSearches(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/saved-searches`, options) }
export async function supportReadAllNotifications(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/notifications/read-all`, options) }
export async function supportReadNotification(notificationId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/notifications/${encodeURIComponent(notificationId)}/read`, options) }
export async function supportRecheckAttachment(attachmentId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/attachments/${encodeURIComponent(attachmentId)}/recheck`, options) }
export async function supportSaveSavedSearch(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/saved-searches`, options) }
export async function supportSimulatePermission(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/permissions/simulate`, options) }
export async function supportUpdateAttachmentSecurity(attachmentId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/attachments/${encodeURIComponent(attachmentId)}/security`, options) }
export async function supportUploadAttachment(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/attachments`, options) }
