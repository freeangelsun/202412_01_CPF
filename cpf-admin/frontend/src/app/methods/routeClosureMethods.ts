/**
 * Route-specific operational actions that close the ADM route -> generated
 * operation -> real consumer chain.  Every method uses the shared same-origin
 * BFF client; Browser supplied actor identity is intentionally prohibited.
 */
export const routeClosureMethods: Record<string, any> = {
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
    const params = this.buildParams({ reason: this.operationForm.reason });
    this.logLevelResult = await this.sendJson(`/adm/api/log-level/rules/${encodeURIComponent(ruleId)}?${params.toString()}`, "DELETE");
    await this.loadLogLevelRules();
    this.setMessage(`동적 로그 규칙을 제거했습니다. ruleId=${ruleId}`);
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
    this.operationResult = await this.getJson("/adm/api/maintenance/actions?limit=100");
  },

  async executeMaintenanceAction() {
    if (!this.operationForm.instanceId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson("/adm/api/maintenance/actions", "POST", {
      action: this.operationForm.maintenanceAction,
      serviceId: this.operationForm.serviceId || null,
      instanceId: this.operationForm.instanceId,
      expectedVersion: Number(this.operationForm.expectedVersion || 0),
      reason: this.operationForm.reason,
      approvalId: this.operationForm.approvalId || null
    });
    await this.loadMaintenanceActions();
  },

  async loadConfigDetail() {
    const id = Number(this.configForm.configId);
    if (!Number.isFinite(id) || id <= 0) return this.setMessage("조회할 Config ID를 입력하세요.");
    this.configResult = await this.getJson(`/adm/api/configs/${id}`);
  },

  async deleteConfig() {
    const id = Number(this.configForm.configId);
    if (!Number.isFinite(id) || id <= 0 || !this.requireReason(this.configForm.reason)) return;
    const params = this.buildParams({ reason: this.configForm.reason });
    this.configResult = await this.sendJson(`/adm/api/configs/${id}?${params.toString()}`, "DELETE");
    await this.loadConfigs();
  },

  async loadResponseCodeDetail() {
    const responseCode = String(this.responseCodeForm.responseCode || "").trim();
    if (!responseCode) return this.setMessage("조회할 응답코드를 입력하세요.");
    this.responseCodeResult = await this.getJson(`/adm/api/response-codes/${encodeURIComponent(responseCode)}`);
  },

  async resolveBusinessDate() {
    const calendarId = String(this.operationForm.calendarId || "DEFAULT").trim();
    const params = this.buildParams({ date: this.operationForm.date, offset: Number(this.operationForm.offset || 1) });
    this.operationResult = await this.getJson(`/adm/api/business-calendars/${encodeURIComponent(calendarId)}/resolve?${params.toString()}`);
  },

  async loadTransactionLogRecoveryStatus() {
    this.reliabilityResult = await this.getJson("/adm/api/reliability/transaction-log-recovery");
  },

  async replayBrokerDlq() {
    const messageId = String(this.reliabilityAction.messageId || "").trim();
    if (!messageId || !this.requireReason(this.reliabilityAction.reason)) return;
    this.reliabilityResult = await this.sendJson(`/adm/api/reliability/broker/dlq/${encodeURIComponent(messageId)}/replay`, "POST", {
      reason: this.reliabilityAction.reason
    });
  },

  async resolveUnknownResultFromRecoveryCenter() {
    const unknownId = String(this.reliabilityAction.unknownId || "").trim();
    if (!unknownId || !this.requireReason(this.reliabilityAction.reason)) return;
    this.reliabilityResult = await this.sendJson(`/adm/api/reliability/unknown-results/${encodeURIComponent(unknownId)}/resolve`, "POST", {
      targetStatus: this.reliabilityAction.targetStatus,
      reason: this.reliabilityAction.reason
    });
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
    this.reliabilityResult = await this.sendJson("/adm/api/reliability/transaction-log-recovery/run", "POST", {
      target: this.operationForm.recoveryTarget || null,
      limit: Number(this.reliabilitySearch.limit || 100),
      reason: this.operationForm.reason
    });
  },

  async loadNotificationRuleDetail() {
    const ruleId = Number(this.notificationForm.ruleId || this.operationForm.notificationRuleId);
    if (!Number.isFinite(ruleId) || ruleId <= 0) return this.setMessage("조회할 알림 Rule ID를 입력하세요.");
    this.notificationResult = { ...this.notificationResult, selectedRule: await this.getJson(`/adm/api/notifications/rules/${ruleId}`) };
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

  async createNotificationRule() {
    if (!this.requireReason(this.notificationForm.reason)) return;
    const result = await this.sendJson("/adm/api/notifications/rules", "POST", this.notificationRulePayload());
    this.notificationForm.ruleId = result?.ruleId || null;
    this.notificationResult = { ...this.notificationResult, selectedRule: result };
    await this.loadNotifications();
  },

  async updateNotificationRule() {
    const ruleId = Number(this.notificationForm.ruleId || this.operationForm.notificationRuleId);
    if (!Number.isFinite(ruleId) || ruleId <= 0 || !this.requireReason(this.notificationForm.reason)) return;
    this.notificationResult = { ...this.notificationResult, selectedRule: await this.sendJson(`/adm/api/notifications/rules/${ruleId}`, "PUT", this.notificationRulePayload()) };
    await this.loadNotifications();
  },

  async loadBatchExecutionPage() {
    const params = this.buildParams({ jobId: this.batchForm.jobId, executionId: this.batchForm.executionId, page: 0, size: 50 });
    this.batchResult = { ...this.batchResult, executionPage: await this.getJson(`/adm/api/batch/executions/page?${params.toString()}`) };
  },

  async loadBatchRuntimeInstances() {
    this.batchResult = { ...this.batchResult, runtimeInstances: await this.getJson("/adm/api/batch-runtime/instances") };
  },

  fileJobId() {
    return String(this.operationForm.fileJobId || this.selected?.jobId || "").trim();
  },

  async loadFileJobDetail() {
    const jobId = this.fileJobId();
    if (!jobId) return this.setMessage("조회할 File Job ID를 입력하세요.");
    this.operationResult = await this.getJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}`);
  },

  async applyFileJob() {
    const jobId = this.fileJobId();
    if (!jobId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}/apply`, "POST", {
      reason: this.operationForm.reason, approvalId: this.operationForm.approvalId || null
    });
  },

  async retryFileJob() {
    const jobId = this.fileJobId();
    if (!jobId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}/retry`, "POST", { reason: this.operationForm.reason });
  },

  async cancelFileJob() {
    const jobId = this.fileJobId();
    if (!jobId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}/cancel`, "POST", { reason: this.operationForm.reason });
  },

  async rollbackFileJob() {
    const jobId = this.fileJobId();
    if (!jobId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/file-jobs/${encodeURIComponent(jobId)}/rollback`, "POST", {
      reason: this.operationForm.reason, approvalId: this.operationForm.approvalId || null
    });
  },

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

  async loadCodeDetail() {
    const codeId = Number(this.codeForm.codeId);
    if (!Number.isFinite(codeId) || codeId <= 0) return this.setMessage("조회할 Code ID를 입력하세요.");
    this.codeResult = await this.getJson(`/adm/api/codes/${codeId}`);
  },

  async deleteCodeById() {
    const codeId = Number(this.codeForm.codeId);
    if (!Number.isFinite(codeId) || codeId <= 0 || !this.requireReason(this.codeForm.reason)) return;
    const params = this.buildParams({ reason: this.codeForm.reason });
    this.codeResult = await this.sendJson(`/adm/api/codes/${codeId}?${params.toString()}`, "DELETE");
    await this.loadCodes();
  },

  async validateOperatorPassword() {
    const params = this.buildParams({ password: this.passwordForm.newPassword, operatorId: this.passwordForm.operatorId });
    this.passwordResult = await this.getJson(`/adm/api/operators/password-policy/validate?${params.toString()}`);
  },

  async changeOperatorPassword() {
    const operatorId = String(this.passwordForm.operatorId || this.operationForm.operatorId || "").trim();
    if (!operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
    this.passwordResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operatorId)}/password`, "POST", {
      newPassword: this.passwordForm.newPassword,
      reason: this.passwordForm.reason
    });
  },

  async disableMfa() {
    const operatorId = String(this.securityForm.operatorId || this.operationForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.securityForm.reason)) return;
    this.securityResult = await this.sendJson(`/adm/api/security/mfa/${encodeURIComponent(operatorId)}/disable`, "POST", {
      reason: this.securityForm.reason
    });
  },

  async loadOperatorRoles() {
    this.operatorResult = { items: this.operatorResult?.items || this.operatorResult, roles: await this.getJson("/adm/api/operators/roles") };
  },

  async loadOperatorSessions() {
    const params = this.buildParams({ operatorId: this.operationForm.operatorId || this.operatorForm.operatorId });
    this.operatorResult = { items: this.operatorResult?.items || this.operatorResult, sessions: await this.getJson(`/adm/api/operators/sessions?${params.toString()}`) };
  },

  async unlockManagedOperator() {
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operatorId)}/unlock`, "POST", { reason: this.operationForm.reason });
    await this.loadOperators();
  },

  async updateOperatorContact() {
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    if (!operatorId || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operatorId)}/contacts`, "PUT", {
      mobileNo: this.operatorForm.mobileNo || null,
      officePhoneNo: this.operatorForm.officePhoneNo || null,
      reason: this.operationForm.reason
    });
    await this.loadOperators();
  },

  async updateOperatorRoles() {
    const operatorId = String(this.operationForm.operatorId || this.operatorForm.operatorId || "").trim();
    const roleIds = String(this.operationForm.roleIds || "").split(",").map((value: string) => value.trim()).filter(Boolean);
    if (!operatorId || !roleIds.length || !this.requireReason(this.operationForm.reason)) return;
    this.operationResult = await this.sendJson(`/adm/api/operators/${encodeURIComponent(operatorId)}/roles`, "PUT", {
      roleIds,
      reason: this.operationForm.reason
    });
    await this.loadOperators();
  },

  async loadBreakGlassSessions() {
    this.approvalResult = { ...this.approvalResult, breakGlassSessions: await this.getJson("/adm/api/break-glass?limit=100") };
  },

  async reviewBreakGlassSession() {
    const sessionId = String(this.operationForm.breakGlassSessionId || "").trim();
    if (!sessionId || !this.requireReason(this.operationForm.reason)) return;
    this.approvalResult = { ...this.approvalResult, breakGlassReview: await this.sendJson(`/adm/api/break-glass/${encodeURIComponent(sessionId)}/review`, "POST", {
      status: this.operationForm.reviewStatus,
      reason: this.operationForm.reason
    }) };
  },

  async loadRuntimeChange() {
    const changeId = String(this.operationForm.changeId || "").trim();
    if (!changeId) return this.setMessage("조회할 Runtime Change ID를 입력하세요.");
    this.approvalResult = { ...this.approvalResult, runtimeChange: await this.getJson(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}`) };
  },

  async loadRuntimeOperation() {
    const operationId = String(this.operationForm.operationId || "").trim();
    if (!operationId) return this.setMessage("조회할 Runtime Operation ID를 입력하세요.");
    this.approvalResult = { ...this.approvalResult, runtimeOperation: await this.getJson(`/adm/api/runtime-control/operations/${encodeURIComponent(operationId)}`) };
  }
};
