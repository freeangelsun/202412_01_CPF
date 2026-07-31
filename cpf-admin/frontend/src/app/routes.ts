import { defineAsyncComponent, type Component } from "vue";

export type AdmFeatureGroup = "home" | "online" | "batch" | "integration" | "monitoring" | "framework";
export interface AdmFeatureRoute { group: AdmFeatureGroup; component: Component; icon: string; }
export const admGroupLabels: Record<AdmFeatureGroup,string> = {
  home:"홈", online:"온라인 운영", batch:"배치 운영", integration:"연계 관리",
  monitoring:"통합 관제", framework:"프레임워크 관리"
};
export const admFeatureRoutes: Record<string,AdmFeatureRoute> = {
 dashboard:{group:"home",icon:"dashboard",component:defineAsyncComponent(()=>import("../features/dashboard/DashboardPage.vue"))},
 topology:{group:"home",icon:"topology",component:defineAsyncComponent(()=>import("../features/topology/TopologyPage.vue"))},
 capacity:{group:"home",icon:"capacity",component:defineAsyncComponent(()=>import("../features/capacity/CapacityPage.vue"))},
 logs:{group:"monitoring",icon:"logs",component:defineAsyncComponent(()=>import("../features/logs/LogsPage.vue"))},
 transactionGroups:{group:"online",icon:"activity",component:defineAsyncComponent(()=>import("../features/transaction-groups/TransactionGroupsPage.vue"))},
 transactions:{group:"online",icon:"activity",component:defineAsyncComponent(()=>import("../features/transactions/TransactionsPage.vue"))},
 remoteLogs:{group:"monitoring",icon:"logs",component:defineAsyncComponent(()=>import("../features/remote-logs/RemoteLogsPage.vue"))},
 auditLogs:{group:"monitoring",icon:"logs",component:defineAsyncComponent(()=>import("../features/audit-logs/AuditLogsPage.vue"))},
 logLevel:{group:"monitoring",icon:"settings",component:defineAsyncComponent(()=>import("../features/log-level/LogLevelPage.vue"))},
 logPolicies:{group:"monitoring",icon:"settings",component:defineAsyncComponent(()=>import("../features/log-policies/LogPoliciesPage.vue"))},
 standardExecutions:{group:"online",icon:"service",component:defineAsyncComponent(()=>import("../features/standard-executions/StandardExecutionsPage.vue"))},
 channelPolicy:{group:"online",icon:"service",component:defineAsyncComponent(()=>import("../features/channel-policy/ChannelPolicyPage.vue"))},
 serviceRegistry:{group:"online",icon:"service",component:defineAsyncComponent(()=>import("../features/service-registry/ServiceRegistryPage.vue"))},
 runtimeControl:{group:"online",icon:"settings",component:defineAsyncComponent(()=>import("../features/runtime-control/RuntimeControlPage.vue"))},
 maintenance:{group:"framework",icon:"maintenance",component:defineAsyncComponent(()=>import("../features/maintenance/MaintenancePage.vue"))},
 cache:{group:"framework",icon:"service",component:defineAsyncComponent(()=>import("../features/cache/CachePage.vue"))},
 configs:{group:"framework",icon:"settings",component:defineAsyncComponent(()=>import("../features/configs/ConfigsPage.vue"))},
 responseCodes:{group:"framework",icon:"settings",component:defineAsyncComponent(()=>import("../features/response-codes/ResponseCodesPage.vue"))},
 businessCalendar:{group:"framework",icon:"settings",component:defineAsyncComponent(()=>import("../features/business-calendar/BusinessCalendarPage.vue"))},
 recoveryCenter:{group:"monitoring",icon:"recovery",component:defineAsyncComponent(()=>import("../features/recovery-center/RecoveryCenterPage.vue"))},
 incidents:{group:"monitoring",icon:"incident",component:defineAsyncComponent(()=>import("../features/incidents/IncidentsPage.vue"))},
 reliability:{group:"monitoring",icon:"recovery",component:defineAsyncComponent(()=>import("../features/reliability/ReliabilityPage.vue"))},
 notifications:{group:"integration",icon:"incident",component:defineAsyncComponent(()=>import("../features/notifications/NotificationsPage.vue"))},
 batch:{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch/BatchPage.vue"))},
 "batch-overview":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-overview/BatchOverviewPage.vue"))},
 "batch-runtime":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-runtime-control/RuntimeTopologyPage.vue"))},
 "batch-instances":{group:"batch",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-instances/BatchInstancesPage.vue"))},
 "batch-scheduler":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-scheduler/BatchSchedulerPage.vue"))},
 "batch-worker-pools":{group:"batch",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-worker-pools/BatchWorkerPoolsPage.vue"))},
 "batch-center-cut":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-center-cut/BatchCenterCutPage.vue"))},
 "batch-agents":{group:"batch",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-agents/BatchAgentsPage.vue"))},
 "batch-job-packs":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-job-packs/BatchJobPacksPage.vue"))},
 "batch-executions":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-executions/BatchExecutionsPage.vue"))},
 "batch-deployment":{group:"batch",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-deployment/BatchDeploymentPage.vue"))},
 "batch-recovery":{group:"monitoring",icon:"recovery",component:defineAsyncComponent(()=>import("../features/batch-recovery/BatchRecoveryPage.vue"))},
 "batch-leases":{group:"monitoring",icon:"recovery",component:defineAsyncComponent(()=>import("../features/batch-leases/BatchLeasesPage.vue"))},
 "batch-alerts":{group:"monitoring",icon:"incident",component:defineAsyncComponent(()=>import("../features/batch-alerts/BatchAlertsPage.vue"))},
 "batch-audit":{group:"monitoring",icon:"logs",component:defineAsyncComponent(()=>import("../features/batch-audit/BatchAuditEvidencePage.vue"))},
 workers:{group:"batch",icon:"worker",component:defineAsyncComponent(()=>import("../features/workers/WorkersPage.vue"))},
 downloads:{group:"integration",icon:"logs",component:defineAsyncComponent(()=>import("../features/downloads/DownloadsPage.vue"))},
 "file-jobs":{group:"batch",icon:"logs",component:defineAsyncComponent(()=>import("../features/file-jobs/FileJobsPage.vue"))},
 messages:{group:"integration",icon:"service",component:defineAsyncComponent(()=>import("../features/messages/MessagesPage.vue"))},
 codes:{group:"framework",icon:"settings",component:defineAsyncComponent(()=>import("../features/codes/CodesPage.vue"))},
 "gateway-dashboard":{group:"online",icon:"dashboard",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-servers":{group:"online",icon:"service",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-groups":{group:"online",icon:"topology",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-routes":{group:"online",icon:"service",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-security":{group:"online",icon:"security",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-health":{group:"online",icon:"activity",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-transactions":{group:"online",icon:"logs",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-log-policies":{group:"online",icon:"settings",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 "gateway-apply-status":{group:"online",icon:"approval",component:defineAsyncComponent(()=>import("../features/gateway-operations/GatewayOperationsPage.vue"))},
 permissions:{group:"framework",icon:"role",component:defineAsyncComponent(()=>import("../features/permissions/PermissionsPage.vue"))},
 password:{group:"framework",icon:"security",component:defineAsyncComponent(()=>import("../features/password/PasswordPage.vue"))},
 security:{group:"framework",icon:"security",component:defineAsyncComponent(()=>import("../features/security/SecurityPage.vue"))},
 operators:{group:"framework",icon:"users",component:defineAsyncComponent(()=>import("../features/operators/OperatorsPage.vue"))},
 secrets:{group:"framework",icon:"security",component:defineAsyncComponent(()=>import("../features/secrets/SecretsPage.vue"))},
 approvals:{group:"framework",icon:"approval",component:defineAsyncComponent(()=>import("../features/approvals/ApprovalsPage.vue"))},
 breakGlass:{group:"framework",icon:"security",component:defineAsyncComponent(()=>import("../features/break-glass/BreakGlassPage.vue"))}
};
export function featureGroupForMenu(menuId:string):AdmFeatureGroup{return admFeatureRoutes[menuId]?.group||"home";}
export function componentForMenu(menuId:string):Component{return admFeatureRoutes[menuId]?.component||admFeatureRoutes.dashboard.component;}
export function iconForMenu(menuId:string):string{return admFeatureRoutes[menuId]?.icon||"logs";}


import type { RouteRecordRaw } from "vue-router";
export const admRouterRecords: RouteRecordRaw[] = Object.entries(admFeatureRoutes).map(([id, route]) => ({
  path: id === "dashboard" ? "/" : `/${id}`,
  name: id,
  component: route.component,
  meta: { menuId: id, group: route.group }
}));
export function menuIdFromRouteName(name: unknown): string { return typeof name === "string" && admFeatureRoutes[name] ? name : "dashboard"; }
