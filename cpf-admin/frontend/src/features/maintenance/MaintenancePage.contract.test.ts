import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
describe("MaintenancePage contract", () => {
  const source = readFileSync(fileURLToPath(new URL("./MaintenancePage.vue", import.meta.url)), "utf8");
  it("uses canonical generated operations and server-authoritative permission", () => { expect(source).toContain('admInvokeOperation("admMaintenanceFindActions"'); expect(source).toContain('admInvokeOperation("admMaintenanceExecuteAction"'); expect(source).toContain('session.canWrite("maintenance", "MAINTENANCE", "/maintenance")'); expect(source).not.toMatch(/admApi/); });
  it("requires explicit reason and exposes loading empty and unknown-result guidance", () => { expect(source).toContain('minlength="5"'); expect(source).toContain('v-if="loading"'); expect(source).toContain('v-else-if="hasRows"'); expect(source).toContain('결과 불명확'); expect(source).not.toMatch(/reason:\s*["']정기 점검/); });
});
