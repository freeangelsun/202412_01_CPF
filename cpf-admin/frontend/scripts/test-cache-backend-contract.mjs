import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";

const repoRoot = path.resolve(process.cwd(), "../..");
const controller = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCacheController.java"), "utf8");
const authTest = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/test/java/com/cpf/admin/opr/controller/AdmCacheControllerAuthenticationTest.java"), "utf8");
const owner = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/main/java/com/cpf/admin/approval/owner/CacheApprovalOwnerCommandAdapter.java"), "utf8");
const approval = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java"), "utf8");
const ownerTest = fs.readFileSync(path.join(repoRoot, "cpf-admin/src/test/java/com/cpf/admin/approval/owner/CacheApprovalOwnerCommandAdapterTest.java"), "utf8");

assert.match(controller, /@Validated/);
assert.match(controller, /@Valid @RequestBody EvictKeyRequest/);
assert.match(controller, /@Valid @RequestBody EvictNamespaceRequest/);
assert.match(controller, /@Valid @RequestBody ControlRequest/);
assert.match(controller, /@PositiveOrZero long version/);
assert.match(controller, /@NotBlank String reason/);
assert.doesNotMatch(controller, /requestUser/);
assert.doesNotMatch(controller, /AdmAuditLogService/);

// Direct dangerous endpoints authenticate the caller but cannot mutate cache state;
// execution is owned exclusively by the approval-engine Owner Command adapter.
for (const method of ["refresh", "evictKey", "evictNamespace", "reconcile"]) {
  const start = controller.indexOf(`AdmCacheControlResponse ${method}(`);
  assert.ok(start >= 0, `missing ${method} endpoint`);
  const end = controller.indexOf("}", start);
  const body = controller.slice(start, end + 1);
  assert.ok(body.indexOf("requireOperator(request)") >= 0, `${method} must authenticate before rejection`);
  assert.ok(body.indexOf("throw approvalRequired()") > body.indexOf("requireOperator(request)"), `${method} must fail closed to approval engine`);
  assert.ok(!body.includes(`service.${method}(`), `${method} must not bypass approval engine`);
}
assert.match(controller, /Approval Engine의 CACHE_\* Owner Command로 실행해야 합니다/);

// Approval request validates a bounded non-blank reason before an immutable command reaches the Owner adapter.
assert.match(approval, /bounded\(required\(request\.reason\(\),"reason"\),"reason",8,500\)/);
assert.match(owner, /command\.requestedBy\(\)\.equals\(command\.approvedBy\(\)\)/);
assert.match(owner, /case EVICT_KEY -> service\.evictKey\(/);
assert.match(owner, /nonNegativeLong\(payload, "version"\)/);
assert.match(owner, /command\.approvedBy\(\), command\.reason\(\)/);
assert.match(owner, /audit\.record\(command\.transactionId\(\), command\.approvedBy\(\), command\.ownerCommand\(\)/);

assert.match(authTest, /never\(\)\)\.refresh/);
assert.match(authTest, /never\(\)\)\.evictKey/);
assert.match(ownerTest, /selfApprovalFailsClosedWithoutMutation/);
assert.match(ownerTest, /unknownEvictionReconcileNeverReplaysMutation/);
assert.match(ownerTest, /verifyNoInteractions\(service\)/);
console.log("[CPF][BACKEND][PASS] cache direct-mutation blocked, approval reason/independent approver/CAS/audit/reconcile contracts");
