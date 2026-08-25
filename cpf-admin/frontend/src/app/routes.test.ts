import { describe, expect, it } from "vitest";
import {
  admCapabilityRegistry,
  admFeatureRoutes,
  admGroupLabels,
  admRouterRecords,
  componentForMenu,
  featureGroupForMenu,
  menuIdFromRouteName
} from "./routes";
import { createAdmState } from "../state/createAdmState";
import { admRouteOperationContract } from "../generated/adm-route-operation-contract";

describe("ADM canonical capability registry", () => {
  it("declares the canonical operation group labels", () => {
    expect(Object.values(admGroupLabels)).toEqual([
      "운영 현황", "로그 · 추적", "장애 · 복구", "설정 · 정책", "감사 · 변경이력",
      "Batch", "Gateway", "Security / Approval", "Deployment"
    ]);
    // 모든 Route의 group은 반드시 선언된 Label을 가져야 한다(미선언 group 사용 차단).
    for (const route of Object.values(admCapabilityRegistry)) {
      expect(admGroupLabels[route.group]).toBeTruthy();
    }
  });

  it("contains exactly the canonical route capabilities declared by the generated contract", () => {
    // Route 수를 손으로 적어두면 Route가 늘어날 때마다 Test가 stale해진다(과거 65 고정값이 그래서 깨졌다).
    // Registry와 OpenAPI에서 생성된 정본 계약과 교차 검증하여 두 정본이 서로를 강제하게 한다.
    const canonicalRouteIds = Object.keys(admRouteOperationContract);
    expect(Object.keys(admCapabilityRegistry).sort()).toEqual([...canonicalRouteIds].sort());
    expect(admRouterRecords).toHaveLength(canonicalRouteIds.length);
    expect(admFeatureRoutes).toBe(admCapabilityRegistry);
  });

  it("projects sidebar state from the same canonical registry", () => {
    const state = createAdmState();
    expect(state.menus).toHaveLength(Object.keys(admCapabilityRegistry).length);
    expect(new Set(state.menus.map(item => item.id))).toEqual(new Set(Object.keys(admCapabilityRegistry)));
    for (const routeId of ["featureFlags", "integrationClosure", "openApiOperations", "resiliencePolicies"]) {
      expect(state.menus.some(item => item.id === routeId)).toBe(true);
    }
  });

  it("binds every route to a backend menu, owner, feature flag and risk", () => {
    for (const [routeId, route] of Object.entries(admCapabilityRegistry)) {
      expect(route.routeId).toBe(routeId);
      expect(route.path).toMatch(/^\//);
      expect(route.menuId).toMatch(/^[A-Z0-9_]+$/);
      expect(route.ownerModule).toBe("cpf-admin");
      expect(route.featureFlag).toBe(`adm.route.${routeId}.enabled`);
      expect(["LOW", "MEDIUM", "HIGH", "CRITICAL"]).toContain(route.riskLevel);
      expect(route.component).toBeTruthy();
    }
  });

  it("maps route names to backend menu identifiers", () => {
    expect(menuIdFromRouteName("transactions")).toBe("TRANSACTION_META");
    expect(menuIdFromRouteName("gateway-dashboard")).toBe("GATEWAY_DASHBOARD");
  });

  it("never silently replaces an unknown route with Dashboard", () => {
    expect(featureGroupForMenu("missing-route")).toBeUndefined();
    expect(componentForMenu("missing-route")).toBeUndefined();
    expect(menuIdFromRouteName("missing-route")).toBeUndefined();
  });

  it("gives every gateway menu a distinct initial operating mode", () => {
    const gatewayRoutes = Object.values(admCapabilityRegistry).filter(route => route.routeId.startsWith("gateway-"));
    expect(gatewayRoutes).toHaveLength(9);
    expect(new Set(gatewayRoutes.map(route => route.props?.initialMode))).toHaveLength(9);
  });

  it("declares the dedicated batch execution consumer operations", () => {
    expect(admCapabilityRegistry["batch-executions"].expectedOperationIds).toEqual([
      "admBatchWorkbenchExecutionDetail",
      "admBatchWorkbenchExecutions",
      "admBatchFindExecutionPage",
      "admBatchFindExecutionDetail",
      "admBatchFindStepExecutions",
      "admBatchRetryExecution",
      "admBatchStopExecution"
    ]);
  });

  it("binds integration closure to server-approved operations only", () => {
    const route = admCapabilityRegistry.integrationClosure;
    expect(route.menuId).toBe("INTEGRATION_CLOSURE");
    expect(route.riskLevel).toBe("CRITICAL");
    expect(route.expectedOperationIds).toEqual([
      "admIntegrationCryptoStatus",
      "admIntegrationTimeHealth",
      "admIntegrationDataQualityValidate",
      "admIntegrationDataQualityCorrectionApprovalRequest",
      "admIntegrationDataQualityCorrectionExecute",
      "admIntegrationDataQualityReplay",
      "admIntegrationWebhookDlq",
      "admIntegrationWebhookReplay"
    ]);
  });

});
