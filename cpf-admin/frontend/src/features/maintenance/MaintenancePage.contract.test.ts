import { describe, expect, it } from "vitest";
import source from "./MaintenancePage.vue?raw";
describe("MaintenancePage contract", () => {
  it("uses canonical generated operations, the approval-gated command path, and server-authoritative permission", () => { expect(source).toContain('admMaintenanceFindActions'); expect(source).toContain('requestServiceInstanceApproval'); expect(source).toContain('session.canWrite("maintenance", "MAINTENANCE", "/maintenance")'); expect(source).not.toMatch(/admApi/); });
  // admMaintenanceExecuteAction(POST /adm/api/maintenance/actions)은 승인 Engine 우회를 막는
  // fail-closed 안내 endpoint(항상 409 APPROVAL_REQUIRED)로, 프론트엔드가 직접 호출하지 않는다.
  // 실제 실행은 승인 후 ServiceRegistryApprovalOwnerCommandAdapter가 Owner Port로 수행한다
  // (cpf-admin/frontend/openapi/cpf-consumer-waivers.json 참조).
  it("never calls the fail-closed direct maintenance execute endpoint", () => { expect(source).not.toMatch(/admMaintenanceExecuteAction/); });
  it("requires explicit reason and exposes loading empty and unknown-result guidance", () => { expect(source).toContain('minlength="8"'); expect(source).toContain('v-if="loading"'); expect(source).toContain('v-else-if="hasRows"'); expect(source).toContain('결과 불명확'); expect(source).not.toMatch(/reason:\s*["']정기 점검/); });
});
