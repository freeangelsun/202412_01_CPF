import { describe, expect, it } from "vitest";
import source from "./ServiceRegistryPage.vue?raw";

describe("ServiceRegistryPage contract", () => {
  it("uses canonical generated mutation operations", () => {
    for (const operation of [
      "admServiceRegistrySaveService", "admServiceRegistrySaveEndpoint", "admServiceRegistrySaveInstance",
      "admServiceRegistryChangeInstanceState", "admServiceRegistryDeleteService",
      "admServiceRegistryDeleteEndpoint", "admServiceRegistryDeleteInstance"
    ]) expect(source).toContain(operation);
    expect(source).not.toContain("this.sendJson");
  });
  it("fails closed on delete permission and explicit reason", () => {
    expect(source).toContain("canDelete('SERVICE_REGISTRY')");
    expect(source).toContain("validateRegistryDeleteRequest");
    expect(source).not.toMatch(/window\.(confirm|prompt)/);
  });
});
