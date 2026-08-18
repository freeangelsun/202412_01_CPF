import {
  admApprovalRequest,
  admRuntimeControlFindCapabilities,
  admRuntimeControlFindChange,
  admRuntimeControlFindStateCatalog,
  admRuntimeControlPreviewChange,
  admRuntimeControlPreviewTargets,
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
      commandId: this.approvalForm.commandId || this.approvalForm.operationId || crypto.randomUUID(),
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
    const requestKey = crypto.randomUUID();
    this.approvalResult = await admApprovalRequest<Record<string, unknown>>({ data: {
      requestKey,
      actionType: "RUNTIME_CONFIG_CHANGE",
      ownerModule: "cpf-starter-platform-operations-runtime-control",
      ownerCommand: "RUNTIME_CONTROL_CREATE",
      targetType: "CPF_RUNTIME_CHANGE",
      targetId: body.commandId,
      payloadSnapshot: JSON.stringify(body),
      reason: body.reason
    }});
    const requestId = String(this.approvalResult?.approvalRequestId ?? this.approvalResult?.requestId ?? this.approvalResult?.id ?? "");
    this.approvalForm.commandId = body.commandId;
    this.approvalForm.operationId = body.commandId;
    this.approvalForm.approvalId = requestId;
    this.approvalEngine.requestId = requestId;
    this.approvalEngine.actionType = "RUNTIME_CONFIG_CHANGE";
    this.approvalEngine.ownerModule = "cpf-starter-platform-operations-runtime-control";
    this.approvalEngine.ownerCommand = "RUNTIME_CONTROL_CREATE";
    this.approvalEngine.targetType = "CPF_RUNTIME_CHANGE";
    this.approvalEngine.targetId = body.commandId;
    this.approvalEngine.payloadSnapshot = JSON.stringify(body, null, 2);
    this.setMessage("Runtime 위험 변경 승인 요청을 생성했습니다. 승인 후 Owner Command를 실행하세요.");
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
    const commandId = this.approvalForm.controlCommandId || this.approvalForm.controlOperationId || crypto.randomUUID();
    const ownerCommand = action === "ROLLBACK" ? "RUNTIME_CONTROL_ROLLBACK" : "RUNTIME_CONTROL_CANCEL";
    const payload = { changeId, commandId, reason: this.approvalForm.reason };
    this.approvalResult = await admApprovalRequest<Record<string, unknown>>({ data: {
      requestKey: crypto.randomUUID(),
      actionType: "RUNTIME_CONFIG_CHANGE",
      ownerModule: "cpf-starter-platform-operations-runtime-control",
      ownerCommand,
      targetType: "CPF_RUNTIME_CHANGE",
      targetId: changeId,
      payloadSnapshot: JSON.stringify(payload),
      reason: this.approvalForm.reason
    }});
    const requestId = String(this.approvalResult?.approvalRequestId ?? this.approvalResult?.requestId ?? this.approvalResult?.id ?? "");
    this.approvalForm.controlCommandId = commandId;
    this.approvalForm.controlOperationId = commandId;
    this.approvalForm.approvalId = requestId;
    this.approvalEngine.requestId = requestId;
    this.approvalEngine.actionType = "RUNTIME_CONFIG_CHANGE";
    this.approvalEngine.ownerModule = "cpf-starter-platform-operations-runtime-control";
    this.approvalEngine.ownerCommand = ownerCommand;
    this.approvalEngine.targetType = "CPF_RUNTIME_CHANGE";
    this.approvalEngine.targetId = changeId;
    this.approvalEngine.payloadSnapshot = JSON.stringify(payload, null, 2);
    this.setMessage(action === "ROLLBACK" ? "Rollback 승인 요청을 생성했습니다." : "변경 취소 승인 요청을 생성했습니다.");
  },
  async executeApprovedRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId) return this.setMessage("검증할 Change ID를 입력하세요.");
    this.approvalResult = await admRuntimeControlVerifyAudit({ path: { changeId } });
    this.setMessage("Runtime 변경 Audit Chain을 검증했습니다.");
  }
};
