import { describe, expect, it } from "vitest";
import { operationForRegistryTarget, validateRegistryDeleteRequest } from "./serviceRegistryWorkflow";

describe("serviceRegistryWorkflow", () => {
  it("maps each target to its canonical generated operation", () => {
    expect(operationForRegistryTarget("service")).toBe("admServiceRegistryDeleteService");
    expect(operationForRegistryTarget("endpoint")).toBe("admServiceRegistryDeleteEndpoint");
    expect(operationForRegistryTarget("instance")).toBe("admServiceRegistryDeleteInstance");
  });
  it("requires id, non-negative integer version, and explicit reason", () => {
    expect(validateRegistryDeleteRequest({ kind:"service", targetId:"", expectedVersion:0, reason:"충분한 삭제 사유" }).ok).toBe(false);
    expect(validateRegistryDeleteRequest({ kind:"service", targetId:"SVC", expectedVersion:-1, reason:"충분한 삭제 사유" }).ok).toBe(false);
    expect(validateRegistryDeleteRequest({ kind:"service", targetId:"SVC", expectedVersion:1, reason:"짧음" }).ok).toBe(false);
    expect(validateRegistryDeleteRequest({ kind:"service", targetId:"SVC", expectedVersion:1, reason:"운영 종료에 따른 삭제" })).toEqual({ok:true});
  });
});
