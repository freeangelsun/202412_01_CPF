import type { Component } from "vue";
import type { CpfOperationId } from "../generated/cpf-operation-contract";

export type BzaRouteId =
  | "dashboard"
  | "organizations" | "employees" | "positions" | "jobTitles" | "assignments" | "organizationResponsibilities"
  | "users" | "roles" | "userRoles" | "menus" | "permissions" | "permissionTools"
  | "approvalInbox" | "approvalSubmissions" | "approvalPolicies" | "approvalSimulation" | "approvalDelegations"
  | "sessions" | "audits" | "notifications" | "attachments" | "savedSearches"
  | "settings" | "commonCatalog" | "downloads" | "downloadAudits";

export interface BzaRoute {
  id: BzaRouteId;
  label: string;
  menuCode: string;
  group: "overview" | "people" | "access" | "approval" | "support";
  description: string;
  expectedOperationIds: readonly CpfOperationId[];
  load: () => Promise<{ default: Component }>;
}

export const bzaRoutes: BzaRoute[] = [
  { id:"dashboard", label:"대시보드", menuCode:"DASHBOARD", group:"overview", description:"업무 운영 현황", expectedOperationIds: ["bzaSupportDashboard"], load:()=>import("../features/dashboard/DashboardPage.vue") },

  { id:"organizations", label:"조직", menuCode:"ORGANIZATION", group:"people", description:"조직 계층", expectedOperationIds: ["bzaBackofficeFindOrganizations", "bzaBackofficeSaveOrganization", "bzaBackofficeFindOrganizationsPage"], load:()=>import("../features/organizations/OrganizationsPage.vue") },
  { id:"employees", label:"직원", menuCode:"EMPLOYEE", group:"people", description:"직원 Profile", expectedOperationIds: ["bzaBackofficeFindEmployees", "bzaBackofficeSaveEmployee", "bzaBackofficeFindEmployeesPage", "bzaBackofficeEmployeeRawContact"], load:()=>import("../features/employees/EmployeesPage.vue") },
  { id:"positions", label:"직급", menuCode:"EMPLOYEE", group:"people", description:"직급 기준정보", expectedOperationIds: ["bzaDirectoryFindPositions", "bzaDirectorySavePosition", "bzaDirectoryFindPositionsPage"], load:()=>import("../features/positions/PositionsPage.vue") },
  { id:"jobTitles", label:"직책", menuCode:"EMPLOYEE", group:"people", description:"직책 기준정보", expectedOperationIds: ["bzaDirectoryFindJobTitles", "bzaDirectorySaveJobTitle", "bzaDirectoryFindJobTitlesPage"], load:()=>import("../features/job-titles/JobTitlesPage.vue") },
  { id:"assignments", label:"발령·겸직", menuCode:"EMPLOYEE", group:"people", description:"다중 소속·겸직·파견·대행", expectedOperationIds: ["bzaDirectoryFindAssignments", "bzaDirectorySaveAssignment", "bzaDirectoryFindAssignmentsPage"], load:()=>import("../features/assignments/AssignmentsPage.vue") },
  { id:"organizationResponsibilities", label:"조직 책임", menuCode:"ORGANIZATION", group:"people", description:"조직장·대행·승인 Owner", expectedOperationIds: ["bzaDirectoryFindResponsibilities", "bzaDirectorySaveResponsibility", "bzaDirectoryFindResponsibilitiesPage"], load:()=>import("../features/organization-responsibilities/OrganizationResponsibilitiesPage.vue") },

  { id:"users", label:"사용자", menuCode:"AUTHORIZATION", group:"access", description:"BZA 인증 사용자", expectedOperationIds: ["bzaOperationFindAdminUsers", "bzaOperationSaveAdminUser", "bzaOperationFindAdminUsersPage"], load:()=>import("../features/users/UsersPage.vue") },
  { id:"roles", label:"역할", menuCode:"AUTHORIZATION", group:"access", description:"업무 역할", expectedOperationIds: ["bzaOperationFindRoles", "bzaOperationSaveRole", "bzaOperationFindRolesPage"], load:()=>import("../features/roles/RolesPage.vue") },
  { id:"userRoles", label:"사용자 Role", menuCode:"AUTHORIZATION", group:"access", description:"다중 Role 유효기간", expectedOperationIds: ["bzaDirectoryFindUserRoles", "bzaDirectorySaveUserRole", "bzaDirectoryFindUserRolesPage"], load:()=>import("../features/user-roles/UserRolesPage.vue") },
  { id:"menus", label:"메뉴", menuCode:"AUTHORIZATION", group:"access", description:"화면 메뉴 Registry", expectedOperationIds: ["bzaOperationFindMenus", "bzaOperationSaveMenu", "bzaOperationFindMenusPage", "bzaOperationDeleteMenu", "bzaOperationFindMenuImpact"], load:()=>import("../features/authorization/MenusPage.vue") },
  { id:"permissions", label:"권한", menuCode:"AUTHORIZATION", group:"access", description:"화면·행위·API·Data Scope 권한", expectedOperationIds: ["bzaOperationFindPermissions", "bzaOperationSavePermission", "bzaOperationFindPermissionsPage"], load:()=>import("../features/authorization/PermissionsPage.vue") },
  { id:"permissionTools", label:"권한 분석", menuCode:"AUTHORIZATION", group:"access", description:"Role 비교와 권한 Simulation", expectedOperationIds: ["bzaBackofficeFindEffectivePermissions", "bzaSupportCompareRolePermissions", "bzaSupportSimulatePermission"], load:()=>import("../features/permission-tools/PermissionToolsPage.vue") },

  { id:"approvalInbox", label:"결재 처리", menuCode:"APPROVAL", group:"approval", description:"Snapshot 참여자 Inbox", expectedOperationIds: ["bzaApprovalInbox"], load:()=>import("../features/approval-inbox/ApprovalInboxPage.vue") },
  { id:"approvalSubmissions", label:"결재 상신", menuCode:"APPROVAL", group:"approval", description:"정책 기반 멱등 상신", expectedOperationIds: ["bzaApprovalSubmissions", "bzaApprovalPolicySubmit", "bzaApprovalExpireDue", "bzaApprovalSubmissionDetail", "bzaApprovalCancel", "bzaApprovalResubmit", "bzaApprovalWithdraw", "bzaApprovalParticipantDecision"], load:()=>import("../features/approval-submissions/ApprovalSubmissionsPage.vue") },
  { id:"approvalPolicies", label:"결재 정책", menuCode:"APPROVAL", group:"approval", description:"Versioned Policy/ALL·ANY·N_OF_M", expectedOperationIds: ["bzaApprovalPolicies", "bzaApprovalPolicySave", "bzaApprovalPolicyDetail"], load:()=>import("../features/approval-policies/ApprovalPoliciesPage.vue") },
  { id:"approvalSimulation", label:"경로 Simulation", menuCode:"APPROVAL", group:"approval", description:"조직/Role/위임 사전 해석", expectedOperationIds: ["bzaApprovalPolicySimulate"], load:()=>import("../features/approval-simulation/ApprovalSimulationPage.vue") },
  { id:"approvalDelegations", label:"결재 위임", menuCode:"APPROVAL", group:"approval", description:"유효기간 위임·대결", expectedOperationIds: ["bzaApprovalDelegations", "bzaApprovalDelegationSave"], load:()=>import("../features/approval-delegations/ApprovalDelegationsPage.vue") },

  { id:"sessions", label:"내 세션", menuCode:"AUTHORIZATION", group:"support", description:"Refresh session 관리", expectedOperationIds: ["bzaAuthSessions", "bzaAuthRevokeSession"], load:()=>import("../features/sessions/SessionsPage.vue") },
  { id:"audits", label:"업무 감사", menuCode:"AUDIT", group:"support", description:"Immutable 업무 감사", expectedOperationIds: ["bzaBusinessAuditVerify", "bzaBackofficeFindBusinessAudits"], load:()=>import("../features/audits/AuditsPage.vue") },
  { id:"notifications", label:"알림", menuCode:"SETTING", group:"support", description:"업무 알림", expectedOperationIds: ["bzaSupportFindNotifications", "bzaSupportCreateNotification", "bzaSupportReadAllNotifications", "bzaSupportReadNotification"], load:()=>import("../features/notifications/NotificationsPage.vue") },
  { id:"attachments", label:"첨부파일", menuCode:"ATTACHMENT", group:"support", description:"첨부 업로드·검증", expectedOperationIds: ["bzaSupportFindAttachments", "bzaSupportUploadAttachment", "bzaSupportDownloadAttachment", "bzaSupportRecheckAttachment", "bzaSupportUpdateAttachmentSecurity"], load:()=>import("../features/attachments/AttachmentsPage.vue") },
  { id:"savedSearches", label:"저장 검색", menuCode:"SETTING", group:"support", description:"사용자 검색 조건", expectedOperationIds: ["bzaSupportFindSavedSearches", "bzaSupportSaveSavedSearch", "bzaSupportDisableSavedSearch"], load:()=>import("../features/saved-searches/SavedSearchesPage.vue") },
  { id:"settings", label:"업무 설정", menuCode:"SETTING", group:"support", description:"BZA 업무 설정", expectedOperationIds: ["bzaOperationFindSettings"], load:()=>import("../features/settings/SettingsPage.vue") },
  { id:"commonCatalog", label:"공통 코드·메시지", menuCode:"SETTING", group:"support", description:"Common 응답코드·다국어 메시지 Catalog", expectedOperationIds: ["bzaCommonResponseCodeSearch","bzaCommonResponseCodeDetail","bzaCommonResponseCodeCreate","bzaCommonResponseCodeUpdate","bzaCommonResponseCodeDisable","bzaCommonMessageSearch","bzaCommonMessageDetail","bzaCommonMessageCreate","bzaCommonMessageUpdate","bzaCommonMessageDisable","bzaCommonCatalogRefresh"], load:()=>import("../features/common-catalog/CommonCatalogPage.vue") },
  { id:"downloads", label:"다운로드 정책", menuCode:"SETTING", group:"support", description:"다운로드 정책", expectedOperationIds: ["bzaOperationFindDownloadPolicies"], load:()=>import("../features/downloads/DownloadsPage.vue") },
  { id:"downloadAudits", label:"다운로드 감사", menuCode:"AUDIT", group:"support", description:"다운로드 감사", expectedOperationIds: ["bzaSupportFindDownloadAudits"], load:()=>import("../features/download-audits/DownloadAuditsPage.vue") }
];



import type { RouteRecordRaw } from "vue-router";
export const bzaRouterRecords: RouteRecordRaw[] = bzaRoutes.map(route => ({
  path: route.id === "dashboard" ? "/" : `/${route.id}`,
  name: route.id,
  component: route.load,
  meta: { menuCode: route.menuCode, routeId: route.id, group: route.group, label: route.label, description: route.description }
}));
export function routeFromName(name: unknown): BzaRouteId | undefined {
  if (typeof name !== "string") return undefined;
  const candidate = name as BzaRouteId;
  return bzaRoutes.some(route => route.id === candidate) ? candidate : undefined;
}
