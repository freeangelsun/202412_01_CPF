import { defineAsyncComponent, type Component } from "vue";

export type AdmFeatureGroup = "observability" | "platform" | "business" | "batch" | "access" | "approval";

export interface AdmFeatureRoute {
  group: AdmFeatureGroup;
  component: Component;
}

export const admGroupLabels: Record<AdmFeatureGroup, string> = {
  "observability": "관측·거래",
  "platform": "플랫폼 제어",
  "business": "업무 운영",
  "batch": "Batch / Center-Cut",
  "access": "보안·권한",
  "approval": "승인·통제"
};

export const admFeatureRoutes: Record<string, AdmFeatureRoute> = {
  logs: { group: "observability", component: defineAsyncComponent(() => import("../features/logs/LogsPage.vue")) },
  transactionGroups: { group: "observability", component: defineAsyncComponent(() => import("../features/transaction-groups/TransactionGroupsPage.vue")) },
  transactions: { group: "observability", component: defineAsyncComponent(() => import("../features/transactions/TransactionsPage.vue")) },
  remoteLogs: { group: "observability", component: defineAsyncComponent(() => import("../features/remote-logs/RemoteLogsPage.vue")) },
  auditLogs: { group: "observability", component: defineAsyncComponent(() => import("../features/audit-logs/AuditLogsPage.vue")) },
  logLevel: { group: "observability", component: defineAsyncComponent(() => import("../features/log-level/LogLevelPage.vue")) },
  logPolicies: { group: "observability", component: defineAsyncComponent(() => import("../features/log-policies/LogPoliciesPage.vue")) },
  standardExecutions: { group: "platform", component: defineAsyncComponent(() => import("../features/standard-executions/StandardExecutionsPage.vue")) },
  channelPolicy: { group: "platform", component: defineAsyncComponent(() => import("../features/channel-policy/ChannelPolicyPage.vue")) },
  reliability: { group: "platform", component: defineAsyncComponent(() => import("../features/reliability/ReliabilityPage.vue")) },
  serviceRegistry: { group: "platform", component: defineAsyncComponent(() => import("../features/service-registry/ServiceRegistryPage.vue")) },
  cache: { group: "platform", component: defineAsyncComponent(() => import("../features/cache/CachePage.vue")) },
  configs: { group: "platform", component: defineAsyncComponent(() => import("../features/configs/ConfigsPage.vue")) },
  responseCodes: { group: "platform", component: defineAsyncComponent(() => import("../features/response-codes/ResponseCodesPage.vue")) },
  notifications: { group: "business", component: defineAsyncComponent(() => import("../features/notifications/NotificationsPage.vue")) },
  downloads: { group: "business", component: defineAsyncComponent(() => import("../features/downloads/DownloadsPage.vue")) },
  messages: { group: "business", component: defineAsyncComponent(() => import("../features/messages/MessagesPage.vue")) },
  codes: { group: "business", component: defineAsyncComponent(() => import("../features/codes/CodesPage.vue")) },
  batch: { group: "batch", component: defineAsyncComponent(() => import("../features/batch/BatchPage.vue")) },
  permissions: { group: "access", component: defineAsyncComponent(() => import("../features/permissions/PermissionsPage.vue")) },
  password: { group: "access", component: defineAsyncComponent(() => import("../features/password/PasswordPage.vue")) },
  security: { group: "access", component: defineAsyncComponent(() => import("../features/security/SecurityPage.vue")) },
  operators: { group: "access", component: defineAsyncComponent(() => import("../features/operators/OperatorsPage.vue")) },
  approvals: { group: "approval", component: defineAsyncComponent(() => import("../features/approvals/ApprovalsPage.vue")) },
};

export function featureGroupForMenu(menuId: string): AdmFeatureGroup {
  return admFeatureRoutes[menuId]?.group || "observability";
}

export function componentForMenu(menuId: string): Component {
  return admFeatureRoutes[menuId]?.component || admFeatureRoutes.logs.component;
}

export function menuIdFromHash(hash: string): string {
  return hash.replace(/^#\/?/, "").trim();
}
