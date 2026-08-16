import { beforeEach, describe, expect, it } from "vitest";
import {
  bzaSession,
  clearBzaSession,
  hasBzaMenu,
  hasBzaPermission
} from "../features/auth/session";
import {
  canonicalBzaMenuCode,
  isCanonicalBzaMenuCode,
  resolveBzaOperationPermission
} from "./bzaPermissionManifest";
import { bzaRoutes } from "../app/routes";

describe("BZA permission manifest", () => {
  beforeEach(() => clearBzaSession());

  it("canonical metadata의 legacy resource alias를 제품 메뉴 그룹으로 정규화한다", () => {
    expect(canonicalBzaMenuCode("USER")).toBe("AUTHORIZATION");
    expect(canonicalBzaMenuCode("permission")).toBe("AUTHORIZATION");
    expect(canonicalBzaMenuCode("saved_search")).toBe("SETTING");
    expect(isCanonicalBzaMenuCode("BZA_AUTHORIZATION")).toBe(true);
    expect(isCanonicalBzaMenuCode("AUTHORIZATION")).toBe(true);
  });

  it("BZA_ADMIN product seed의 group ALL 권한으로 하위 UI action을 허용한다", () => {
    bzaSession.operator = {
      loginId: "bza-admin",
      menus: ["BZA_AUTHORIZATION"],
      buttons: ["AUTHORIZATION:ALL"]
    };

    expect(hasBzaMenu("AUTHORIZATION")).toBe(true);
    expect(hasBzaPermission("AUTHORIZATION", "WRITE")).toBe(true);
    expect(hasBzaPermission("USER", "WRITE")).toBe(true);
    expect(hasBzaPermission("ROLE", "WRITE")).toBe(true);
    expect(hasBzaPermission("MENU", "WRITE")).toBe(true);
    expect(hasBzaPermission("PERMISSION", "WRITE")).toBe(true);
  });

  it("resolves operation permissions from ordered canonical actionRules", () => {
    expect(resolveBzaOperationPermission("GET", "/api/bza/backoffice/permissions/effective"))
      .toEqual({ menuCode: "AUTHORIZATION", actionCode: "SIMULATE" });
    expect(resolveBzaOperationPermission("POST", "/api/bza/backoffice/approvals/42/actions"))
      .toEqual({ menuCode: "APPROVAL", actionCode: "DECIDE" });
    expect(resolveBzaOperationPermission("PATCH", "/api/bza/backoffice/employees/42"))
      .toEqual({ menuCode: "EMPLOYEE", actionCode: "UPDATE" });
    expect(resolveBzaOperationPermission("POST", "/api/bza/unknown/resource")).toBeNull();
  });

  it("모든 제품 route는 canonical menu group에 연결된다", () => {
    for (const route of bzaRoutes) {
      expect(isCanonicalBzaMenuCode(route.menuCode), route.id).toBe(true);
    }
  });
});
