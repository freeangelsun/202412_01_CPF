import { describe, expect, it } from "vitest";
import source from "./ServiceRegistryPage.vue?raw";

describe("ServiceRegistryPage contract", () => {
  it("uses canonical generated mutation operations", () => {
    for (const operation of [
      "admServiceRegistrySaveService", "admServiceRegistrySaveEndpoint", "admServiceRegistrySaveInstance"
    ]) expect(source).toContain(operation);
    expect(source).not.toContain("this.sendJson");
  });
  it("routes high-risk state change and delete through the approval owner boundary", () => {
    // 고위험 조치(Instance 상태 변경/삭제)는 화면에서 즉시 실행하지 않고 독립 승인 후
    // Owner Command 로만 실행한다. 직접 실행 mutation 으로 회귀하면 승인 경계가 무력화된다.
    expect(source).toContain("requestServiceInstanceApproval");
    expect(source).toContain("requestServiceRegistryDeleteApproval");
    for (const retiredDirectMutation of [
      "admServiceRegistryChangeInstanceState", "admServiceRegistryDeleteService",
      "admServiceRegistryDeleteEndpoint", "admServiceRegistryDeleteInstance"
    ]) expect(source).not.toContain(retiredDirectMutation);
  });
  it("fails closed on delete permission and explicit reason", () => {
    expect(source).toContain("canDelete('SERVICE_REGISTRY')");
    expect(source).toContain("validateRegistryDeleteRequest");
    expect(source).not.toMatch(/window\.(confirm|prompt)/);
  });
});
