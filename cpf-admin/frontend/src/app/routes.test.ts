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

describe("ADM canonical capability registry", () => {
  it("uses home plus five top-level operation groups", () => {
    expect(Object.values(admGroupLabels)).toEqual(["홈", "온라인 운영", "배치 운영", "연계 관리", "통합 관제", "프레임워크 관리"]);
  });

  it("contains exactly the canonical 60 route capabilities", () => {
    expect(Object.keys(admCapabilityRegistry)).toHaveLength(60);
    expect(admRouterRecords).toHaveLength(60);
    expect(admFeatureRoutes).toBe(admCapabilityRegistry);
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
