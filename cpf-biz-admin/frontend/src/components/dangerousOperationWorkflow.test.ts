import { describe, expect, it } from "vitest";
import { attachDangerousReason, requireDangerousOperationReason, validatePathValues } from "./dangerousOperationWorkflow";

describe("dangerous operation workflow", () => {
  it("requires an auditable reason", () => {
    expect(requireDangerousOperationReason("  운영 장애 긴급 복구  ")).toBe("운영 장애 긴급 복구");
    expect(() => requireDangerousOperationReason("짧음")).toThrow(/5자 이상/);
  });
  it("requires every path variable", () => {
    expect(validatePathValues(["sessionId"], { sessionId: "S-1" })).toEqual({ sessionId: "S-1" });
    expect(() => validatePathValues(["sessionId"], {})).toThrow(/sessionId/);
  });
  it("injects the reason only when the API payload does not already contain one", () => {
    expect(attachDangerousReason({}, {}, "운영 장애 긴급 복구").body.reason).toBe("운영 장애 긴급 복구");
    expect(attachDangerousReason({ reason: "query reason" }, {}, "운영 장애 긴급 복구").body.reason).toBeUndefined();
    expect(attachDangerousReason({}, { reason: "body reason" }, "운영 장애 긴급 복구").body.reason).toBe("body reason");
  });
});
