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

  it("contains exactly the canonical 59 route capabilities", () => {
    expect(Object.keys(admCapabilityRegistry)).toHaveLength(59);
    expect(admRouterRecords).toHaveLength(59);
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
      "admBatchFindExecutionPage",
      "admBatchFindExecutionDetail",
      "admBatchFindStepExecutions",
      "admBatchRetryExecution",
      "admBatchStopExecution"
    ]);
  });
});
