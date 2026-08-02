// Generated from canonical Backend OpenAPI. Do not edit manually.
export type CpfOperationId = "bzaOperationDeleteMenu" | "bzaOperationFindAdminUsers" | "bzaOperationFindAdminUsersPage" | "bzaApprovalDelegations" | "bzaApprovalInbox" | "bzaApprovalPolicies" | "bzaApprovalPolicyDetail" | "bzaApprovalSubmissions" | "bzaApprovalSubmissionDetail" | "bzaSupportFindAttachments" | "bzaSupportDownloadAttachment" | "bzaBusinessAuditVerify" | "bzaAuthLoginHistories" | "bzaAuthMe" | "bzaAuthSessions" | "bzaBackofficeFindApprovals" | "bzaBackofficeFindApproval" | "bzaBackofficeFindBusinessAudits" | "bzaBackofficeFindEmployees" | "bzaBackofficeFindEmployeesPage" | "bzaBackofficeFindOrganizations" | "bzaBackofficeFindOrganizationsPage" | "bzaBackofficeFindEffectivePermissions" | "bzaSupportDashboard" | "bzaDirectoryFindAssignments" | "bzaDirectoryFindAssignmentsPage" | "bzaDirectoryFindJobTitles" | "bzaDirectoryFindJobTitlesPage" | "bzaDirectoryFindPositions" | "bzaDirectoryFindPositionsPage" | "bzaDirectoryFindResponsibilities" | "bzaDirectoryFindResponsibilitiesPage" | "bzaDirectoryFindUserRoles" | "bzaDirectoryFindUserRolesPage" | "bzaSupportFindDownloadAudits" | "bzaOperationFindDownloadPolicies" | "bzaOperationFindMenus" | "bzaOperationFindMenuImpact" | "bzaOperationFindMenusPage" | "bzaSupportFindNotifications" | "bzaOperationFindPermissions" | "bzaSupportCompareRolePermissions" | "bzaOperationFindPermissionsPage" | "bzaOperationFindRoles" | "bzaOperationFindRolesPage" | "bzaSupportFindSavedSearches" | "bzaOperationFindSettings" | "bzaOperationSaveAdminUser" | "bzaApprovalParticipantDecision" | "bzaApprovalDelegationSave" | "bzaApprovalPolicySave" | "bzaApprovalPolicySimulate" | "bzaApprovalPolicySubmit" | "bzaApprovalCancel" | "bzaApprovalResubmit" | "bzaApprovalWithdraw" | "bzaApprovalExpireDue" | "bzaSupportUploadAttachment" | "bzaSupportRecheckAttachment" | "bzaSupportUpdateAttachmentSecurity" | "bzaAuthLogin" | "bzaAuthLogout" | "bzaAuthChangePassword" | "bzaAuthRefresh" | "bzaAuthRevokeSession" | "bzaBackofficeCreateApproval" | "bzaBackofficeActApproval" | "bzaBackofficeSaveEmployee" | "bzaBackofficeEmployeeRawContact" | "bzaBackofficeSaveOrganization" | "bzaDirectorySaveAssignment" | "bzaDirectorySaveJobTitle" | "bzaDirectorySavePosition" | "bzaDirectorySaveResponsibility" | "bzaDirectorySaveUserRole" | "bzaOperationSaveMenu" | "bzaSupportCreateNotification" | "bzaSupportReadNotification" | "bzaSupportReadAllNotifications" | "bzaOperationSavePermission" | "bzaSupportSimulatePermission" | "bzaOperationSaveRole" | "bzaSupportSaveSavedSearch" | "bzaSupportDisableSavedSearch";
export interface CpfOperationDescriptor { method: string; template: string; operationId: CpfOperationId; }
export const cpfOperationDescriptors: readonly CpfOperationDescriptor[] = [
  { method: "DELETE", template: "/api/bza/menus/{menuCode}", operationId: "bzaOperationDeleteMenu" },
  { method: "GET", template: "/api/bza/admin-users", operationId: "bzaOperationFindAdminUsers" },
  { method: "GET", template: "/api/bza/admin-users/page", operationId: "bzaOperationFindAdminUsersPage" },
  { method: "GET", template: "/api/bza/approvals/delegations", operationId: "bzaApprovalDelegations" },
  { method: "GET", template: "/api/bza/approvals/inbox", operationId: "bzaApprovalInbox" },
  { method: "GET", template: "/api/bza/approvals/policies", operationId: "bzaApprovalPolicies" },
  { method: "GET", template: "/api/bza/approvals/policies/{policyCode}/{version}", operationId: "bzaApprovalPolicyDetail" },
  { method: "GET", template: "/api/bza/approvals/submissions", operationId: "bzaApprovalSubmissions" },
  { method: "GET", template: "/api/bza/approvals/submissions/{approvalId}", operationId: "bzaApprovalSubmissionDetail" },
  { method: "GET", template: "/api/bza/attachments", operationId: "bzaSupportFindAttachments" },
  { method: "GET", template: "/api/bza/attachments/{attachmentId}/download", operationId: "bzaSupportDownloadAttachment" },
  { method: "GET", template: "/api/bza/audits/verify", operationId: "bzaBusinessAuditVerify" },
  { method: "GET", template: "/api/bza/auth/login-history", operationId: "bzaAuthLoginHistories" },
  { method: "GET", template: "/api/bza/auth/me", operationId: "bzaAuthMe" },
  { method: "GET", template: "/api/bza/auth/sessions", operationId: "bzaAuthSessions" },
  { method: "GET", template: "/api/bza/backoffice/approvals", operationId: "bzaBackofficeFindApprovals" },
  { method: "GET", template: "/api/bza/backoffice/approvals/{approvalId}", operationId: "bzaBackofficeFindApproval" },
  { method: "GET", template: "/api/bza/backoffice/audits", operationId: "bzaBackofficeFindBusinessAudits" },
  { method: "GET", template: "/api/bza/backoffice/employees", operationId: "bzaBackofficeFindEmployees" },
  { method: "GET", template: "/api/bza/backoffice/employees/page", operationId: "bzaBackofficeFindEmployeesPage" },
  { method: "GET", template: "/api/bza/backoffice/organizations", operationId: "bzaBackofficeFindOrganizations" },
  { method: "GET", template: "/api/bza/backoffice/organizations/page", operationId: "bzaBackofficeFindOrganizationsPage" },
  { method: "GET", template: "/api/bza/backoffice/permissions/effective", operationId: "bzaBackofficeFindEffectivePermissions" },
  { method: "GET", template: "/api/bza/dashboard", operationId: "bzaSupportDashboard" },
  { method: "GET", template: "/api/bza/directory/assignments", operationId: "bzaDirectoryFindAssignments" },
  { method: "GET", template: "/api/bza/directory/assignments/page", operationId: "bzaDirectoryFindAssignmentsPage" },
  { method: "GET", template: "/api/bza/directory/job-titles", operationId: "bzaDirectoryFindJobTitles" },
  { method: "GET", template: "/api/bza/directory/job-titles/page", operationId: "bzaDirectoryFindJobTitlesPage" },
  { method: "GET", template: "/api/bza/directory/positions", operationId: "bzaDirectoryFindPositions" },
  { method: "GET", template: "/api/bza/directory/positions/page", operationId: "bzaDirectoryFindPositionsPage" },
  { method: "GET", template: "/api/bza/directory/responsibilities", operationId: "bzaDirectoryFindResponsibilities" },
  { method: "GET", template: "/api/bza/directory/responsibilities/page", operationId: "bzaDirectoryFindResponsibilitiesPage" },
  { method: "GET", template: "/api/bza/directory/user-roles", operationId: "bzaDirectoryFindUserRoles" },
  { method: "GET", template: "/api/bza/directory/user-roles/page", operationId: "bzaDirectoryFindUserRolesPage" },
  { method: "GET", template: "/api/bza/download-audits", operationId: "bzaSupportFindDownloadAudits" },
  { method: "GET", template: "/api/bza/downloads", operationId: "bzaOperationFindDownloadPolicies" },
  { method: "GET", template: "/api/bza/menus", operationId: "bzaOperationFindMenus" },
  { method: "GET", template: "/api/bza/menus/{menuCode}/impact", operationId: "bzaOperationFindMenuImpact" },
  { method: "GET", template: "/api/bza/menus/page", operationId: "bzaOperationFindMenusPage" },
  { method: "GET", template: "/api/bza/notifications", operationId: "bzaSupportFindNotifications" },
  { method: "GET", template: "/api/bza/permissions", operationId: "bzaOperationFindPermissions" },
  { method: "GET", template: "/api/bza/permissions/compare", operationId: "bzaSupportCompareRolePermissions" },
  { method: "GET", template: "/api/bza/permissions/page", operationId: "bzaOperationFindPermissionsPage" },
  { method: "GET", template: "/api/bza/roles", operationId: "bzaOperationFindRoles" },
  { method: "GET", template: "/api/bza/roles/page", operationId: "bzaOperationFindRolesPage" },
  { method: "GET", template: "/api/bza/saved-searches", operationId: "bzaSupportFindSavedSearches" },
  { method: "GET", template: "/api/bza/settings", operationId: "bzaOperationFindSettings" },
  { method: "POST", template: "/api/bza/admin-users", operationId: "bzaOperationSaveAdminUser" },
  { method: "POST", template: "/api/bza/approvals/{approvalId}/decisions", operationId: "bzaApprovalParticipantDecision" },
  { method: "POST", template: "/api/bza/approvals/delegations", operationId: "bzaApprovalDelegationSave" },
  { method: "POST", template: "/api/bza/approvals/policies", operationId: "bzaApprovalPolicySave" },
  { method: "POST", template: "/api/bza/approvals/simulate", operationId: "bzaApprovalPolicySimulate" },
  { method: "POST", template: "/api/bza/approvals/submissions", operationId: "bzaApprovalPolicySubmit" },
  { method: "POST", template: "/api/bza/approvals/submissions/{approvalId}/cancel", operationId: "bzaApprovalCancel" },
  { method: "POST", template: "/api/bza/approvals/submissions/{approvalId}/resubmit", operationId: "bzaApprovalResubmit" },
  { method: "POST", template: "/api/bza/approvals/submissions/{approvalId}/withdraw", operationId: "bzaApprovalWithdraw" },
  { method: "POST", template: "/api/bza/approvals/submissions/expire-due", operationId: "bzaApprovalExpireDue" },
  { method: "POST", template: "/api/bza/attachments", operationId: "bzaSupportUploadAttachment" },
  { method: "POST", template: "/api/bza/attachments/{attachmentId}/recheck", operationId: "bzaSupportRecheckAttachment" },
  { method: "POST", template: "/api/bza/attachments/{attachmentId}/security", operationId: "bzaSupportUpdateAttachmentSecurity" },
  { method: "POST", template: "/api/bza/auth/login", operationId: "bzaAuthLogin" },
  { method: "POST", template: "/api/bza/auth/logout", operationId: "bzaAuthLogout" },
  { method: "POST", template: "/api/bza/auth/password/change", operationId: "bzaAuthChangePassword" },
  { method: "POST", template: "/api/bza/auth/refresh", operationId: "bzaAuthRefresh" },
  { method: "POST", template: "/api/bza/auth/sessions/{sessionId}/revoke", operationId: "bzaAuthRevokeSession" },
  { method: "POST", template: "/api/bza/backoffice/approvals", operationId: "bzaBackofficeCreateApproval" },
  { method: "POST", template: "/api/bza/backoffice/approvals/{approvalId}/actions", operationId: "bzaBackofficeActApproval" },
  { method: "POST", template: "/api/bza/backoffice/employees", operationId: "bzaBackofficeSaveEmployee" },
  { method: "POST", template: "/api/bza/backoffice/employees/{employeeNo}/contacts/raw", operationId: "bzaBackofficeEmployeeRawContact" },
  { method: "POST", template: "/api/bza/backoffice/organizations", operationId: "bzaBackofficeSaveOrganization" },
  { method: "POST", template: "/api/bza/directory/assignments", operationId: "bzaDirectorySaveAssignment" },
  { method: "POST", template: "/api/bza/directory/job-titles", operationId: "bzaDirectorySaveJobTitle" },
  { method: "POST", template: "/api/bza/directory/positions", operationId: "bzaDirectorySavePosition" },
  { method: "POST", template: "/api/bza/directory/responsibilities", operationId: "bzaDirectorySaveResponsibility" },
  { method: "POST", template: "/api/bza/directory/user-roles", operationId: "bzaDirectorySaveUserRole" },
  { method: "POST", template: "/api/bza/menus", operationId: "bzaOperationSaveMenu" },
  { method: "POST", template: "/api/bza/notifications", operationId: "bzaSupportCreateNotification" },
  { method: "POST", template: "/api/bza/notifications/{notificationId}/read", operationId: "bzaSupportReadNotification" },
  { method: "POST", template: "/api/bza/notifications/read-all", operationId: "bzaSupportReadAllNotifications" },
  { method: "POST", template: "/api/bza/permissions", operationId: "bzaOperationSavePermission" },
  { method: "POST", template: "/api/bza/permissions/simulate", operationId: "bzaSupportSimulatePermission" },
  { method: "POST", template: "/api/bza/roles", operationId: "bzaOperationSaveRole" },
  { method: "POST", template: "/api/bza/saved-searches", operationId: "bzaSupportSaveSavedSearch" },
  { method: "POST", template: "/api/bza/saved-searches/{savedSearchId}/disable", operationId: "bzaSupportDisableSavedSearch" }
] as const;

function matchesTemplate(template: string, pathname: string): boolean {
  const expected = template.split("/");
  const actual = pathname.split("/");
  if (expected.length !== actual.length) return false;
  return expected.every((segment, index) =>
    (segment.startsWith("{") && segment.endsWith("}")) || segment === actual[index]
  );
}
export function resolveCpfOperation(method: string, rawUrl: string): CpfOperationDescriptor {
  const pathname = new URL(rawUrl, window.location.origin).pathname;
  const normalizedMethod = method.trim().toUpperCase();
  const found = cpfOperationDescriptors.find(value => value.method === normalizedMethod && matchesTemplate(value.template, pathname));
  if (!found) throw new Error(`CPF OpenAPI operation is not registered: ${normalizedMethod} ${pathname}`);
  return found;
}
