export const approvalMethods = {
  async loadApprovalPolicies(this: any) {
    this.approvalPolicyResult = await this.getJson("/adm/api/approvals/policies");
  },
  async requestDangerousApproval(this: any) {
    if (!this.requireReason(this.approvalForm.reason)) return;
    if (!this.approvalForm.requestKey) this.approvalForm.requestKey = crypto.randomUUID();
    const policyVersion = this.approvalForm.policyVersion ? Number(this.approvalForm.policyVersion) : null;
    this.approvalResult = await this.sendJson("/adm/api/approvals/requests", "POST", {
      requestKey: this.approvalForm.requestKey,
      policyCode: this.approvalForm.policyCode || null,
      policyVersion,
      actionType: this.approvalForm.actionType,
      ownerModule: this.approvalForm.ownerModule,
      ownerCommand: this.approvalForm.ownerCommand,
      targetType: this.approvalForm.targetType,
      targetId: this.approvalForm.targetId,
      payloadSnapshot: this.approvalForm.payloadSnapshot || "{}",
      expireAt: this.approvalForm.expireAt || null,
      reason: this.approvalForm.reason
    });
    this.approvalForm.selectedRequestId = String(this.approvalResult?.approvalRequestId || "");
    this.setMessage("위험조치 승인 요청을 생성했습니다.");
  },
  async loadApprovalRequest(this: any) {
    if (!this.approvalForm.selectedRequestId) return;
    this.approvalResult = await this.getJson(`/adm/api/approvals/requests/${this.approvalForm.selectedRequestId}`);
  },
  async decideApprovalRequest(this: any) {
    if (!this.approvalForm.selectedRequestId || !this.requireReason(this.approvalForm.reason)) return;
    if (!this.approvalForm.idempotencyKey) this.approvalForm.idempotencyKey = crypto.randomUUID();
    this.approvalResult = await this.sendJson(
      `/adm/api/approvals/requests/${this.approvalForm.selectedRequestId}/decisions`, "POST", {
        action: this.approvalForm.decisionAction,
        idempotencyKey: this.approvalForm.idempotencyKey,
        reason: this.approvalForm.reason
      });
    this.setMessage("승인 결정을 반영했습니다.");
  },
  async executeApprovedRequest(this: any) {
    if (!this.approvalForm.selectedRequestId || !this.requireReason(this.approvalForm.reason)) return;
    const q = encodeURIComponent(this.approvalForm.reason);
    this.approvalResult = await this.sendJson(
      `/adm/api/approvals/requests/${this.approvalForm.selectedRequestId}/execute?reason=${q}`, "POST", {});
    this.setMessage("승인된 Owner Command 실행을 요청했습니다.");
  }
};
