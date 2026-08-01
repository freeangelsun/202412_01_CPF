export const approvalMethods = {
  async loadApprovalPolicies(this: any) {
    const [capabilities, states] = await Promise.all([
      this.getJson("/adm/api/runtime-control/capabilities"),
      this.getJson("/adm/api/runtime-control/states")
    ]);
    this.approvalPolicyResult = { capabilities, states };
  },
  runtimeChangePayload(this: any) {
    let target: Record<string, unknown> = {};
    let payload: Record<string, unknown> = {};
    try { target = JSON.parse(this.approvalForm.targetJson || "{}"); }
    catch { throw new Error("Target JSON 형식을 확인하세요."); }
    try { payload = JSON.parse(this.approvalForm.payloadJson || "{}"); }
    catch { throw new Error("Payload JSON 형식을 확인하세요."); }
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
      scheduledAt: this.approvalForm.scheduledAt || null,
      expiresAt: this.approvalForm.expiresAt || null,
      reason: this.approvalForm.reason,
      approvalId: this.approvalForm.approvalId || null,
      breakGlassId: this.approvalForm.breakGlassId || null
    };
  },
  async previewApprovalTargets(this: any) {
    const body = this.runtimeChangePayload();
    this.approvalResult = await this.sendJson("/adm/api/runtime-control/preview-targets", "POST", {
      changeType: body.changeType,
      payloadSchemaVersion: body.payloadSchemaVersion,
      target: body.target
    });
  },
  async previewApprovalChange(this: any) {
    if (!this.requireReason(this.approvalForm.reason)) return;
    this.approvalResult = await this.sendJson("/adm/api/runtime-control/preview-change", "POST", this.runtimeChangePayload());
  },
  async requestDangerousApproval(this: any) {
    if (!this.requireReason(this.approvalForm.reason)) return;
    if (!Number.isFinite(Number(this.approvalForm.expectedVersion))) return this.setMessage("expectedVersion CAS 값을 입력하세요.");
    const body = this.runtimeChangePayload();
    this.approvalResult = await this.sendJson("/adm/api/runtime-control/changes", "POST", body);
    this.approvalForm.operationId = body.operationId;
    this.approvalForm.selectedRequestId = String(this.approvalResult?.changeId || "");
    this.setMessage("Runtime 변경 요청을 생성했습니다.");
  },
  async loadApprovalRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId) return this.setMessage("조회할 Change ID를 입력하세요.");
    this.approvalResult = await this.getJson(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}`);
  },
  async decideApprovalRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId || !this.requireReason(this.approvalForm.reason)) return;
    const action = String(this.approvalForm.decisionAction || "CANCEL").toUpperCase();
    const endpoint = action === "ROLLBACK"
      ? `/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}/rollback`
      : `/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}/cancel`;
    const operationId = this.approvalForm.controlOperationId || crypto.randomUUID();
    this.approvalResult = await this.sendJson(endpoint, "POST", { operationId, reason: this.approvalForm.reason });
    this.approvalForm.controlOperationId = operationId;
    this.setMessage(action === "ROLLBACK" ? "Rollback을 요청했습니다." : "변경 취소를 요청했습니다.");
  },
  async executeApprovedRequest(this: any) {
    const changeId = String(this.approvalForm.selectedRequestId || "").trim();
    if (!changeId) return this.setMessage("검증할 Change ID를 입력하세요.");
    this.approvalResult = await this.getJson(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}/audit/verify`);
    this.setMessage("Runtime 변경 Audit Chain을 검증했습니다.");
  }
};
