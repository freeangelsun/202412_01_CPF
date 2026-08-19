import type { Component } from "vue";

export type AdmFeatureGroup = "operations" | "traceLog" | "failureRecovery" | "configPolicy" | "auditChange" | "batch" | "gateway" | "security" | "deployment";
export type AdmRouteRiskLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface AdmCapabilityRoute {
  routeId: string;
  path: string;
  menuId: string;
  label: string;
  group: AdmFeatureGroup;
  component: Component;
  icon: string;
  ownerModule: "cpf-admin" | "cpf-biz-admin";
  riskLevel: AdmRouteRiskLevel;
  featureFlag: string;
  expectedOperationIds: readonly string[];
  props?: Record<string, unknown>;
}

export const admGroupLabels: Record<AdmFeatureGroup, string> = {
  operations: "운영 현황",
  traceLog: "로그 · 추적",
  failureRecovery: "장애 · 복구",
  configPolicy: "설정 · 정책",
  auditChange: "감사 · 변경이력",
  batch: "Batch",
  gateway: "Gateway",
  security: "Security / Approval",
  deployment: "Deployment"
};
