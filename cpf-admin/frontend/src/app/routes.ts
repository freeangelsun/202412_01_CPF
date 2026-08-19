import type { Component } from "vue";
import type { RouteRecordRaw } from "vue-router";
import { operationsRoutes } from "./routes/operations";
import { traceLogRoutes } from "./routes/trace-log";
import { failureRecoveryRoutes } from "./routes/failure-recovery";
import { configPolicyRoutes } from "./routes/config-policy";
import { auditChangeRoutes } from "./routes/audit-change";
import { batchRoutes } from "./routes/batch";
import { gatewayRoutes } from "./routes/gateway";
import { securityRoutes } from "./routes/security";
import { deploymentRoutes } from "./routes/deployment";
import { admGroupLabels, type AdmCapabilityRoute, type AdmFeatureGroup, type AdmRouteRiskLevel } from "./routes/types";

export { admGroupLabels };
export type { AdmCapabilityRoute, AdmFeatureGroup, AdmRouteRiskLevel };

/**
 * Canonical ADM capability registry assembled from feature-group-owned route registries.
 * Keep route ownership close to the operational feature group instead of growing one monolithic file.
 */
export const admCapabilityRegistry: Record<string, AdmCapabilityRoute> = {
  ...operationsRoutes,
  ...traceLogRoutes,
  ...failureRecoveryRoutes,
  ...configPolicyRoutes,
  ...auditChangeRoutes,
  ...batchRoutes,
  ...gatewayRoutes,
  ...securityRoutes,
  ...deploymentRoutes
};

// Backward-compatible export name. The registry remains the single route/menu/component capability source.
export const admFeatureRoutes = admCapabilityRegistry;

export function findCapabilityByRouteName(name: unknown): AdmCapabilityRoute | undefined {
  return typeof name === "string" ? admCapabilityRegistry[name] : undefined;
}

export function findCapabilityByPath(path: string): AdmCapabilityRoute | undefined {
  return Object.values(admCapabilityRegistry).find(route => route.path === path);
}

export function featureGroupForMenu(routeId: string): AdmFeatureGroup | undefined {
  return admCapabilityRegistry[routeId]?.group;
}

export function componentForMenu(routeId: string): Component | undefined {
  return admCapabilityRegistry[routeId]?.component;
}

export function iconForMenu(routeId: string): string {
  return admCapabilityRegistry[routeId]?.icon ?? "logs";
}

export const admRouterRecords: RouteRecordRaw[] = Object.values(admCapabilityRegistry).map(route => ({
  path: route.path,
  name: route.routeId,
  component: route.component,
  props: route.props,
  meta: {
    routeId: route.routeId,
    menuId: route.menuId,
    group: route.group,
    ownerModule: route.ownerModule,
    riskLevel: route.riskLevel,
    featureFlag: route.featureFlag,
    expectedOperationIds: route.expectedOperationIds
  }
}));

export function menuIdFromRouteName(name: unknown): string | undefined {
  return findCapabilityByRouteName(name)?.menuId;
}
