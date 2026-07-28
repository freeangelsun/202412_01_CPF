import { defineAsyncComponent, type Component } from "vue";

export type AdmFeatureGroup = "overview" | "observability" | "platform" | "recovery" | "execution" | "access" | "approval" | "business";
export interface AdmFeatureRoute { group: AdmFeatureGroup; component: Component; icon: string; }
export const admGroupLabels: Record<AdmFeatureGroup,string> = { overview:"운영 개요", observability:"관측·거래", platform:"플랫폼 제어", recovery:"복구·장애", execution:"Batch / Worker", access:"보안·권한", approval:"승인·통제", business:"업무 운영" };
export const admFeatureRoutes: Record<string,AdmFeatureRoute> = {
 dashboard:{group:"overview",icon:"dashboard",component:defineAsyncComponent(()=>import("../features/dashboard/DashboardPage.vue"))},
 topology:{group:"overview",icon:"topology",component:defineAsyncComponent(()=>import("../features/topology/TopologyPage.vue"))},
 capacity:{group:"overview",icon:"capacity",component:defineAsyncComponent(()=>import("../features/capacity/CapacityPage.vue"))},
 logs:{group:"observability",icon:"logs",component:defineAsyncComponent(()=>import("../features/logs/LogsPage.vue"))},
 transactionGroups:{group:"observability",icon:"activity",component:defineAsyncComponent(()=>import("../features/transaction-groups/TransactionGroupsPage.vue"))},
 transactions:{group:"observability",icon:"activity",component:defineAsyncComponent(()=>import("../features/transactions/TransactionsPage.vue"))},
 remoteLogs:{group:"observability",icon:"logs",component:defineAsyncComponent(()=>import("../features/remote-logs/RemoteLogsPage.vue"))},
 auditLogs:{group:"observability",icon:"logs",component:defineAsyncComponent(()=>import("../features/audit-logs/AuditLogsPage.vue"))},
 logLevel:{group:"observability",icon:"settings",component:defineAsyncComponent(()=>import("../features/log-level/LogLevelPage.vue"))},
 logPolicies:{group:"observability",icon:"settings",component:defineAsyncComponent(()=>import("../features/log-policies/LogPoliciesPage.vue"))},
 standardExecutions:{group:"platform",icon:"service",component:defineAsyncComponent(()=>import("../features/standard-executions/StandardExecutionsPage.vue"))},
 channelPolicy:{group:"platform",icon:"service",component:defineAsyncComponent(()=>import("../features/channel-policy/ChannelPolicyPage.vue"))},
 serviceRegistry:{group:"platform",icon:"service",component:defineAsyncComponent(()=>import("../features/service-registry/ServiceRegistryPage.vue"))},
 runtimeControl:{group:"platform",icon:"settings",component:defineAsyncComponent(()=>import("../features/runtime-control/RuntimeControlPage.vue"))},
 maintenance:{group:"platform",icon:"maintenance",component:defineAsyncComponent(()=>import("../features/maintenance/MaintenancePage.vue"))},
 cache:{group:"platform",icon:"service",component:defineAsyncComponent(()=>import("../features/cache/CachePage.vue"))},
 configs:{group:"platform",icon:"settings",component:defineAsyncComponent(()=>import("../features/configs/ConfigsPage.vue"))},
 responseCodes:{group:"platform",icon:"settings",component:defineAsyncComponent(()=>import("../features/response-codes/ResponseCodesPage.vue"))},
 businessCalendar:{group:"platform",icon:"settings",component:defineAsyncComponent(()=>import("../features/business-calendar/BusinessCalendarPage.vue"))},
 recoveryCenter:{group:"recovery",icon:"recovery",component:defineAsyncComponent(()=>import("../features/recovery-center/RecoveryCenterPage.vue"))},
 incidents:{group:"recovery",icon:"incident",component:defineAsyncComponent(()=>import("../features/incidents/IncidentsPage.vue"))},
 reliability:{group:"recovery",icon:"recovery",component:defineAsyncComponent(()=>import("../features/reliability/ReliabilityPage.vue"))},
 notifications:{group:"recovery",icon:"incident",component:defineAsyncComponent(()=>import("../features/notifications/NotificationsPage.vue"))},
 batch:{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch/BatchPage.vue"))},
 "batch-overview":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-overview/BatchOverviewPage.vue"))},
 "batch-runtime":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-runtime-control/RuntimeTopologyPage.vue"))},
 "batch-instances":{group:"execution",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-instances/BatchInstancesPage.vue"))},
 "batch-scheduler":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-scheduler/BatchSchedulerPage.vue"))},
 "batch-worker-pools":{group:"execution",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-worker-pools/BatchWorkerPoolsPage.vue"))},
 "batch-center-cut":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-center-cut/BatchCenterCutPage.vue"))},
 "batch-agents":{group:"execution",icon:"worker",component:defineAsyncComponent(()=>import("../features/batch-agents/BatchAgentsPage.vue"))},
 "batch-job-packs":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-job-packs/BatchJobPacksPage.vue"))},
 "batch-executions":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-executions/BatchExecutionsPage.vue"))},
 "batch-deployment":{group:"execution",icon:"batch",component:defineAsyncComponent(()=>import("../features/batch-deployment/BatchDeploymentPage.vue"))},
 "batch-recovery":{group:"recovery",icon:"recovery",component:defineAsyncComponent(()=>import("../features/batch-recovery/BatchRecoveryPage.vue"))},
 "batch-leases":{group:"recovery",icon:"recovery",component:defineAsyncComponent(()=>import("../features/batch-leases/BatchLeasesPage.vue"))},
 "batch-alerts":{group:"recovery",icon:"incident",component:defineAsyncComponent(()=>import("../features/batch-alerts/BatchAlertsPage.vue"))},
 "batch-audit":{group:"observability",icon:"logs",component:defineAsyncComponent(()=>import("../features/batch-audit/BatchAuditEvidencePage.vue"))},
 workers:{group:"execution",icon:"worker",component:defineAsyncComponent(()=>import("../features/workers/WorkersPage.vue"))},
 downloads:{group:"execution",icon:"logs",component:defineAsyncComponent(()=>import("../features/downloads/DownloadsPage.vue"))},
 messages:{group:"execution",icon:"service",component:defineAsyncComponent(()=>import("../features/messages/MessagesPage.vue"))},
 codes:{group:"execution",icon:"settings",component:defineAsyncComponent(()=>import("../features/codes/CodesPage.vue"))},
 permissions:{group:"access",icon:"role",component:defineAsyncComponent(()=>import("../features/permissions/PermissionsPage.vue"))},
 password:{group:"access",icon:"security",component:defineAsyncComponent(()=>import("../features/password/PasswordPage.vue"))},
 security:{group:"access",icon:"security",component:defineAsyncComponent(()=>import("../features/security/SecurityPage.vue"))},
 operators:{group:"access",icon:"users",component:defineAsyncComponent(()=>import("../features/operators/OperatorsPage.vue"))},
 secrets:{group:"access",icon:"security",component:defineAsyncComponent(()=>import("../features/secrets/SecretsPage.vue"))},
 approvals:{group:"approval",icon:"approval",component:defineAsyncComponent(()=>import("../features/approvals/ApprovalsPage.vue"))},
 breakGlass:{group:"approval",icon:"security",component:defineAsyncComponent(()=>import("../features/break-glass/BreakGlassPage.vue"))}
};
export function featureGroupForMenu(menuId:string):AdmFeatureGroup{return admFeatureRoutes[menuId]?.group||"overview";}
export function componentForMenu(menuId:string):Component{return admFeatureRoutes[menuId]?.component||admFeatureRoutes.dashboard.component;}
export function iconForMenu(menuId:string):string{return admFeatureRoutes[menuId]?.icon||"logs";}
export function menuIdFromHash(hash:string):string{return hash.replace(/^#\/?/,"").trim();}
