import { describe, expect, it } from "vitest";
import { validateMaintenanceAction } from "./maintenanceWorkflow";
describe("maintenance workflow", () => {
  it("normalizes an audited command", () => expect(validateMaintenanceAction({ serviceId: " MBR ", endpointCode: " API ", instanceId: " I-1 ", action: "DRAIN", reason: " 장애 대응 점검 " })).toEqual({ serviceId: "MBR", endpointCode: "API", instanceId: "I-1", action: "DRAIN", reason: "장애 대응 점검" }));
  it("rejects missing target and short reason", () => { expect(() => validateMaintenanceAction({ serviceId: "", endpointCode: "API", instanceId: "I-1", action: "DRAIN", reason: "장애 대응 점검" })).toThrow(/Service/); expect(() => validateMaintenanceAction({ serviceId: "MBR", endpointCode: "API", instanceId: "I-1", action: "RESUME", reason: "짧음" })).toThrow(/5자/); });
  it("rejects an unknown action", () => expect(() => validateMaintenanceAction({ serviceId: "MBR", endpointCode: "API", instanceId: "I-1", action: "DROP" as never, reason: "장애 대응 점검" })).toThrow(/지원하지 않는/));
});
