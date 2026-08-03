import { describe, expect, it } from "vitest";
import { requireBreakGlassReason, validateBreakGlassRequest } from "./breakGlassWorkflow";

describe("break-glass workflow", () => {
  it("requires an auditable reason and bounded TTL", () => {
    expect(requireBreakGlassReason("  긴급 장애 복구 종료  ")).toBe("긴급 장애 복구 종료");
    expect(validateBreakGlassRequest("MBR-01", "긴급 장애 복구", 15)).toEqual({
      scopeValue: "MBR-01", reason: "긴급 장애 복구", ttlMinutes: 15
    });
    expect(() => validateBreakGlassRequest("", "긴급 장애 복구", 15)).toThrow(/대상/);
    expect(() => validateBreakGlassRequest("MBR-01", "긴급 장애 복구", 31)).toThrow(/30분 이하/);
  });
});
