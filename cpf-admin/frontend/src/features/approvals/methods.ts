import {
  admRuntimeControlCancelChange,
  admRuntimeControlCreateChange,
  admRuntimeControlFindCapabilities,
  admRuntimeControlFindChange,
  admRuntimeControlFindStateCatalog,
  admRuntimeControlPreviewChange,
  admRuntimeControlPreviewTargets,
  admRuntimeControlRollbackChange,
  admRuntimeControlVerifyAudit
} from "../../generated/cpf-api";
import type { AdmRuntimeControlCreateChangeBody } from "../../generated/cpf-api";

export const approvalMethods = {
  async loadApprovalPolicies(this: any) {
    const [capabilities, states] = await Promise.all([
      admRuntimeControlFindCapabilities(),
      admRuntimeControlFindStateCatalog()
    ]);
    this.approvalPolicyResult = { capabilities, states };
  },
  runtimeChangePayload(this: any): AdmRuntimeControlCreateChangeBody {
    let targetInput: Record<string, unknown> = {};
    let payload: Record<string, unknown> = {};
    try { targetInput = JSON.parse(this.approvalForm.targetJson || "{}"); }
    catch { throw new Error("Target JSON 형식을 확인하세요."); }
    try { payload = JSON.parse(this.approvalForm.payloadJson || "{}"); }
    catch { throw new Error("Payload JSON 형식을 확인하세요."); }
    const target: NonNullable<AdmRuntimeControlCreateChangeBody["target"]> = {
      ...targetInput,
      includeDraining: targetInput.includeDraining === true,
      includeMaintenance: targetInput.includeMaintenance === true,
      allowAll: targetInput.allowAll === true,
    };
    return {
      operationId: this.approvalForm.operationId || crypto.randomUUID(),
      changeType: this.approvalForm.changeType,
      payloadSchemaVersion: Number(this.approvalForm.payloadSchemaVersion || 1),
      target,
      payload,
      expectedVersion: Number(this.approvalForm.expectedVersion),
      rolloutMode: this.approvalForm.rolloutMode || "ALL_AT_ONCE",
      waveSize: Number(this.approvalForm.waveSize || 1),
      quorumPercent: Number(this.approvalForm.quorumPercent || 100),
      scheduledAt: this.approvalForm.scheduledAt || undefined,
      expiresAt: this.approvalForm.expiresAt || undefined,
      reason: this.approvalForm.reason,
      approvalId: this.approvalForm.approvalId || undefined,
      breakGlassId: this.approvalForm.breakGlassId || undefined
    };
  },
  async previewApprovalTargets(this: any) {
    const body = this.runtimeChangePayload();
    this.approvalResult = await admRuntimeControlPreviewTargets({ data: {
      changeType: body.changeType,
      payloadSchemaVersion: body.payloadSchemaVersion,
      target: body.target
    }});
  },
  async previewApprovalChange(this: any) {
    if (!this.requireReason(this.approvalForm.reason)) return;
    this.approvalResult = await admRuntimeControlPreviewChange({ data: this.runtimeChangePayload() });
  },
  async requestDangerousApproval(this: any) {
    if (!this.requireReason(this.approvalForm.reason)) return;
    if (!Number.isFinite(Number(this.approvalForm.expectedVersion))) return this.setMessage("expectedVersion CAS 값을 입력하세요.");
    const body = this.runtimeChangePayload();
    this.approvalResult = await admRuntimeControlCreateChange<Record<string, unknown>>({ data: body });
    this.approvalForm.operationId = body.operationId;
    this.approvalForm.selectedRequestId = String(this.approvalResult?.changeId || "");
    this.setMessage("Runtime 변경 요청을 생성했습니다.");
  },
  async loadApprovalRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId) return this.setMessage("조회할 Change ID를 입력하세요.");
    this.approvalResult = await admRuntimeControlFindChange({ path: { changeId } });
  },
  async decideApprovalRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId || !this.requireReason(this.approvalForm.reason)) return;
    const action = String(this.approvalForm.decisionAction || "CANCEL").toUpperCase();
    const operationId = this.approvalForm.controlOperationId || crypto.randomUUID();
    const request = { path: { changeId }, data: { operationId, reason: this.approvalForm.reason } };
    this.approvalResult = action === "ROLLBACK"
      ? await admRuntimeControlRollbackChange(request)
      : await admRuntimeControlCancelChange(request);
    this.approvalForm.controlOperationId = operationId;
    this.setMessage(action === "ROLLBACK" ? "Rollback을 요청했습니다." : "변경 취소를 요청했습니다.");
  },
  async executeApprovedRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId) return this.setMessage("검증할 Change ID를 입력하세요.");
    this.approvalResult = await admRuntimeControlVerifyAudit({ path: { changeId } });
    this.setMessage("Runtime 변경 Audit Chain을 검증했습니다.");
  }
};
