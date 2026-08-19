import { defineAsyncComponent } from "vue";
import type { AdmCapabilityRoute } from "./types";

export const auditChangeRoutes = {
  "auditLogs": { routeId: "auditLogs", path: "/auditLogs", menuId: "AUDIT_LOG", label: "감사 로그", group: "auditChange", icon: "logs", ownerModule: "cpf-admin", riskLevel: "MEDIUM", featureFlag: "adm.route.auditLogs.enabled", expectedOperationIds: ["admAuditLogFindAuditLogs", "admAuditDeliveryList", "admAuditDeliveryRetry", "admLogPolicyAuditFindPolicyAudits"], component: defineAsyncComponent(() => import("../../features/audit-logs/AuditLogsPage.vue")), props: undefined },
} satisfies Record<string, AdmCapabilityRoute>;
