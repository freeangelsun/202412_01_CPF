import { describe, expect, it } from "vitest";
import { requireSessionId, requireSessionRevokeReason } from "./sessionWorkflow";

describe("session revoke workflow", () => {
  it("requires a session id and auditable reason", () => {
    expect(requireSessionId("S-1")).toBe("S-1");
    expect(requireSessionRevokeReason("  계정 탈취 의심 세션 폐기  ")).toBe("계정 탈취 의심 세션 폐기");
    expect(() => requireSessionId("")).toThrow(/식별자/);
    expect(() => requireSessionRevokeReason("짧음")).toThrow(/5자 이상/);
  });
});
