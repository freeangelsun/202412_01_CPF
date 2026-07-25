export type AdmFeatureGroup = "observability" | "platform" | "business" | "batch" | "access";

const groupByMenu: Record<string, AdmFeatureGroup> = {
  logs: "observability", transactionGroups: "observability", transactions: "observability", remoteLogs: "observability", auditLogs: "observability", logLevel: "observability", logPolicies: "observability",
  standardExecutions: "platform", channelPolicy: "platform", reliability: "platform", serviceRegistry: "platform", cache: "platform", configs: "platform", responseCodes: "platform",
  members: "business", notifications: "business", downloads: "business", messages: "business", codes: "business",
  batch: "batch",
  permissions: "access", password: "access", security: "access", operators: "access"
};

export const admGroupLabels: Record<AdmFeatureGroup, string> = {
  observability: "관측·거래",
  platform: "플랫폼 제어",
  business: "업무 운영",
  batch: "Batch / Center-Cut",
  access: "보안·권한"
};

export function featureGroupForMenu(menuId: string): AdmFeatureGroup {
  return groupByMenu[menuId] || "observability";
}

export function menuIdFromHash(hash: string): string {
  return hash.replace(/^#\/?/, "").trim();
}
