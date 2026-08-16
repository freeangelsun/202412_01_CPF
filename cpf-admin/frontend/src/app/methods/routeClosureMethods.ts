import { admApprovalRequest, admMaintenanceFindActions, admMaintenanceExecuteAction, getAdmTransactionLogRecoveryStatus, requestAdmBrokerDlqReplay, resolveAdmUnknownResult, runAdmTransactionLogRecovery, admBatchRuntimeInstances, admOperatorValidatePassword, admOperatorChangePassword, admSecurityDisableMfa, admOperatorFindRoles, admOperatorFindSessions, admOperatorUnlockOperator, admOperatorUpdateContact, admOperatorUpdateRoles, admBreakGlassFindSessions, admBreakGlassReviewSession, admRuntimeControlFindChange, admRuntimeControlFindByOperation } from "../../generated/cpf-api";
/**
 * Route-specific operational actions that close the ADM route -> generated
 * operation -> real consumer chain.  Every method uses the shared same-origin
 * BFF client; Browser supplied actor identity is intentionally prohibited.
 */
export const routeClosureMethods = {
  async downloadLogExportArtifact() {
    const exportId = String(this.operationForm.exportId || this.downloadResult?.exportId || "").trim();
    if (!exportId) return this.setMessage("다운로드할 Log Export ID를 입력하세요.");
    const response = await this.rawResponse(`/adm/api/log-exports/${encodeURIComponent(exportId)}/artifact`, "GET");
    if (!response.ok) return this.parseResponse(response);
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `cpf-log-export-${exportId}.json`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
    this.setMessage(`Log Export Artifact를 다운로드했습니다. exportId=${exportId}`);
  },

  async removeLogLevelRule() {
    const ruleId = String(this.operationForm.ruleId || "").trim();
    if (!ruleId || !this.requireReason(this.operationForm.reason)) return;
    this.logLevelResult = await admApprovalRequest({ data: {
      requestKey: `dynamic-log-remove-${ruleId}-${crypto.randomUUID()}`,
      actionType: "DYNAMIC_LOG_REMOVE",
      ownerModule: "CPF-PLATFORM-OBSERVABILITY",
      ownerCommand: "DYNAMIC_LOG_REMOVE",
      targetType: "DYNAMIC_LOG_LEVEL",
      targetId: ruleId,
      payloadSnapshot: JSON.stringify({ ruleId }),
      reason: this.operationForm.reason
    }});
    this.setMessage("동적 로그 규칙 제거 승인 요청을 생성했습니다. 독립 승인 후 승인 화면에서 실행하세요.");
  },

  logPolicyPayload() {
    const payload = { ...this.logPolicyForm };
    for (const key of [
      "selectedOverrideId", "traceBoostTransactionId", "traceBoostBusinessTransactionId",
      "traceBoostApiPath", "traceBoostStatus", "traceBoostFailureCode",
      "traceBoostDurationMsGreaterThan", "traceBoostTtlSeconds", "effectiveStartAt", "effectiveEndAt"
    ]) delete payload[key];
    return payload;
  },

  async createLogPolicy() {
    if (!this.logPolicyForm.policyKey || !this.logPolicyForm.policyName || !this.requireReason(this.logPolicyForm.reason)) return;
    this.logPolicyResult = await this.sendJson("/adm/api/log-policies", "POST", this.logPolicyPayload());
    const createdId = this.logPolicyResult?.policyId || this.logPolicyResult?.item?.policyId;
    if (createdId) this.logPolicyForm.policyId = Number(createdId);
    await this.loadLogPolicies();
    this.setMessage("로그 정책을 등록했습니다.");
  },

  async loadLogPolicyDetail() {
    const policyId = Number(this.logPolicyForm.policyId || this.operationForm.policyId);
    if (!Number.isFinite(policyId) || policyId <= 0) return this.setMessage("조회할 Policy ID를 입력하세요.");
    this.logPolicyResult = await this.getJson(`/adm/api/log-policies/${policyId}`);
  },

  async updateLogPolicy() {
    const policyId = Number(this.logPolicyForm.policyId || this.operationForm.policyId);
    if (!Number.isFinite(policyId) || policyId <= 0 || !this.requireReason(this.logPolicyForm.reason)) return;
    this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/${policyId}`, "PUT", this.logPolicyPayload());
    await this.loadLogPolicyDetail();
    this.setMessage(`로그 정책을 수정했습니다. policyId=${policyId}`);
  },

  async loadMaintenanceActions() {
    this.operationResult = await admMaintenanceFindActions({ query: { limit: 100 } });
  },

  async executeMaintenanceAction() {
    if (!this.operationForm.instanceId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await admMaintenanceExecuteAction({ data: {
      action: this.operationForm.maintenanceAction, serviceId: this.operationForm.serviceId || null,
      instanceId: this.operationForm.instanceId, expectedVersion: Number(this.operationForm.expectedVersion || 0),
      reason: this.operationForm.reason, approvalId: this.operationForm.approvalId || null
    } });
    await this.loadMaintenanceActions();
  },




  async resolveBusinessDate() {
    const calendarId = String(this.operationForm.calendarId || "DEFAULT").trim();
    const params = this.buildParams({ date: this.operationForm.date, offset: Number(this.operationForm.offset || 1) });
    this.operationResult = await this.getJson(`/adm/api/business-calendars/${encodeURIComponent(calendarId)}/resolve?${params.toString()}`);
  },

  async loadTransactionLogRecoveryStatus() {
    this.reliabilityResult = await getAdmTransactionLogRecoveryStatus();
  },

  async replayBrokerDlq() {
    const messageId = String(this.reliabilityAction.messageId || "").trim();
    if (!messageId || !this.requireReason(this.reliabilityAction.reason)) return;
    this.reliabilityResult = await requestAdmBrokerDlqReplay({ path: { messageId }, data: { reason: this.reliabilityAction.reason } });
  },

  async resolveUnknownResultFromRecoveryCenter() {
    const unknownId = String(this.reliabilityAction.unknownId || "").trim();
    if (!unknownId || !this.requireReason(this.reliabilityAction.reason)) return;
    this.reliabilityResult = await resolveAdmUnknownResult({ path: { unknownId }, data: { targetStatus: this.reliabilityAction.targetStatus, reason: this.reliabilityAction.reason } });
  },

  async retryTransactionLogRecoveryPoison() {
    const target = String(this.operationForm.recoveryTarget || "").trim();
    const eventId = String(this.operationForm.recoveryEventId || "").trim();
    if (!target || !eventId || !this.requireReason(this.operationForm.reason)) return;
    this.reliabilityResult = await this.sendJson(`/adm/api/reliability/transaction-log-recovery/poison/${encodeURIComponent(target)}/${encodeURIComponent(eventId)}/retry`, "POST", {
      reason: this.operationForm.reason
    });
  },

  async runTransactionLogRecovery() {
    if (!this.requireReason(this.operationForm.reason)) return;
    this.reliabilityResult = await runAdmTransactionLogRecovery({ data: { targetStatus: this.operationForm.recoveryTarget || undefined, reason: this.operationForm.reason } });
  },


  notificationRulePayload() {
    return {
      eventType: this.notificationForm.eventType,
      eventSubType: this.notificationForm.eventSubType,
      channelCode: this.notificationForm.channelCode,
      templateCode: this.notificationForm.templateCode,
      severity: this.notificationForm.severity,
      receiverGroup: this.notificationForm.receiverGroup,
      useYn: this.notificationForm.useYn,
      reason: this.notificationForm.reason
    };
  },



  async loadBatchExecutionPage() {
    const params = this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, page: 0, size: 50 });
    this.batchResult = { ...this.batchResult, executionPage: await this.getJson(`/adm/api/batch/executions/page?${params.toString()}`) };
  },

  async loadBatchRuntimeInstances() {
    this.batchResult = { ...this.batchResult, runtimeInstances: await admBatchRuntimeInstances() };
  },

  fileJobId() {
    return String(this.operationForm.fileJobId || this.selected?.jobId || "").trim();
  },

  async loadFileJobDetail() {
    const jobId = this.fileJobId();
    if (!jobId) return this.setMessage("조회할 File Job ID를 입력하세요.");
    this.operationResult = await this.getJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}`);
  },

  async requestFileJobApproval(action: "apply"|"retry"|"cancel"|"rollback") {
    const jobId = this.fileJobId();
    if (!jobId || !this.requireReason(this.operationForm.reason)) return;
    const current = await this.getJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}`);
    const commands: Record<string,string> = { apply:"FILE_JOB_APPLY", retry:"FILE_JOB_RETRY", cancel:"FILE_JOB_CANCEL", rollback:"FILE_JOB_ROLLBACK" };
    const ownerCommand = commands[action];
    const requestKey = `file-job-${action}-${jobId.slice(0,32)}-${crypto.randomUUID()}`;
    this.operationResult = await admApprovalRequest({ data: {
      requestKey, actionType: ownerCommand, ownerModule: "ADM", ownerCommand, targetType: "FILE_JOB", targetId: jobId,
      payloadSnapshot: JSON.stringify({ expectedState: String(current.state || "") }), reason: this.operationForm.reason
    } });
    this.setMessage(`File Job ${action} 위험조치 승인 요청을 생성했습니다. 독립 승인 후 승인 화면에서 실행하세요.`);
  },

  async applyFileJob() { return this.requestFileJobApproval("apply"); },
  async retryFileJob() { return this.requestFileJobApproval("retry"); },
  async cancelFileJob() { return this.requestFileJobApproval("cancel"); },
  async rollbackFileJob() { return this.requestFileJobApproval("rollback"); },

  async downloadFileJobArtifact() {
    const jobId = this.fileJobId();
    if (!jobId) return this.setMessage("다운로드할 File Job ID를 입력하세요.");
    const response = await this.rawResponse(`/adm/api/file-jobs/${encodeURIComponent(jobId)}/artifact`, "GET");
    if (!response.ok) return this.parseResponse(response);
    const disposition = response.headers.get("Content-Disposition") || "";
    const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    const url = URL.createObjectURL(await response.blob());
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = encodedName ? decodeURIComponent(encodedName) : `cpf-file-job-${jobId}.zip`;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
  },


  async deleteCodeById() {
    const codeId = Number(this.codeForm.codeId);
    if (!Number.isFinite(codeId) || codeId <= 0 || !this.requireReason(this.codeForm.reason)) return;
    const params = this.buildParams({ reason: this.codeForm.reason });
    this.codeResult = await this.sendJson(`/adm/api/codes/${codeId}?${params.toString()}`, "DELETE");
    await this.loadCodes();
  },

  async validateOperatorPassword() {
    this.passwordResult = await admOperatorValidatePassword({ query: { password: this.passwordForm.newPassword } });
  },

  async changeOperatorPassword() {
    const operatorId = String(this.passwordForm.operatorId || this.operationForm.operatorId || "").trim();
    if (!operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
    this.passwordResult = await admOperatorChangePassword({ path: { operatorId }, data: { newPassword: this.passwordForm.newPassword, reason: this.passwordForm.reason } });
  },

  async disableMfa() {
    const operatorId = String(this.securityForm.operatorId || this.operationForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.securityForm.reason)) return;
    this.securityResult = await admSecurityDisableMfa({ path: { operatorId }, query: { reason: this.securityForm.reason } });
  },

  async loadOperatorRoles() {
    this.operatorResult = { items: this.operatorResult?.items || this.operatorResult, roles: await admOperatorFindRoles() };
  },

  async loadOperatorSessions() {
    this.operatorResult = { items: this.operatorResult?.items || this.operatorResult, sessions: await admOperatorFindSessions({ query: { operatorId: this.operationForm.operatorId || this.operatorForm.operatorId || undefined } }) };
  },

  async unlockManagedOperator() {
    if (!this.canButton("PASSWORD_UNLOCK", "PASSWORD")) throw new Error("PASSWORD_UNLOCK 권한이 없습니다.");
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await admOperatorUnlockOperator({ path: { operatorId }, data: { reason: this.operationForm.reason } });
    await this.loadOperators();
  },

  async updateOperatorContact() {
    if (!this.canButton("OPERATOR_CONTACT_UPDATE", "OPERATOR")) throw new Error("OPERATOR_CONTACT_UPDATE 권한이 없습니다.");
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await admOperatorUpdateContact({ path: { operatorId }, data: { mobileNo: this.operatorForm.mobileNo || null, officePhoneNo: this.operatorForm.officePhoneNo || null, reason: this.operationForm.reason } });
    await this.loadOperators();
  },

  async updateOperatorRoles() {
    if (!this.canButton("OPERATOR_ROLE_UPDATE", "OPERATOR")) throw new Error("OPERATOR_ROLE_UPDATE 권한이 없습니다.");
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    const roleIds = String(this.operationForm.roleIds || "").split(",").map((value: string) => value.trim()).filter(Boolean);
    if (!operatorId || !roleIds.length || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await admOperatorUpdateRoles({ path: { operatorId }, data: { roleIds, reason: this.operationForm.reason } });
    await this.loadOperators();
  },

  async loadBreakGlassSessions() {
    this.approvalResult = { ...this.approvalResult, breakGlassSessions: await admBreakGlassFindSessions({ query: { limit: 100 } }) };
  },

  async reviewBreakGlassSession() {
    const sessionId = String(this.operationForm.breakGlassSessionId || "").trim();
    if (!sessionId || !this.requireReason(this.operationForm.reason)) return;
    this.approvalResult = { ...this.approvalResult, breakGlassReview: await admBreakGlassReviewSession({ path: { sessionId }, data: { status: this.operationForm.reviewStatus, reason: this.operationForm.reason } }) };
  },

  async loadRuntimeChange() {
    const changeId = String(this.operationForm.changeId || "").trim();
    if (!changeId) return this.setMessage("조회할 Runtime Change ID를 입력하세요.");
    this.approvalResult = { ...this.approvalResult, runtimeChange: await admRuntimeControlFindChange({ path: { changeId } }) };
  },

  async loadRuntimeOperation() {
    const operationId = String(this.operationForm.operationId || "").trim();
    if (!operationId) return this.setMessage("조회할 Runtime Operation ID를 입력하세요.");
    this.approvalResult = { ...this.approvalResult, runtimeOperation: await admRuntimeControlFindByOperation({ path: { operationId } }) };
  }
} satisfies Record<string, any>;
