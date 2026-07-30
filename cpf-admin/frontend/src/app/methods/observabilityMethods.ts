export const observabilityMethods: Record<string, any> = {
  sortLogs(key) {
        this.logSort = this.logSort.key === key
          ? { key, direction: this.logSort.direction === "asc" ? "desc" : "asc" }
          : { key, direction: "asc" };
      },
  moveLogPage(delta) {
        this.logPage.page = Math.min(this.logTotalPages, Math.max(1, this.logPage.page + delta));
      },
  async copyLogDetail() {
        if (!this.requireReason(this.downloadForm.reason)) return;
        await this.requestLogDetailExport("CLIPBOARD");
      },
  async downloadLogDetail() {
        if (!this.requireReason(this.downloadForm.reason)) return;
        await this.requestLogDetailExport("DOWNLOAD");
      },
  async requestLogDetailExport(action) {
        const detail = this.logDetail?.item || this.logDetail || {};
        const logId = detail.logIdx || detail.log_idx || detail.transactionId || detail.transaction_id;
        if (!logId) {
          this.setMessage("먼저 로그 상세를 선택하세요.");
          return;
        }
        const response = await fetch(`/adm/api/log-exports`, {
          method: "POST",
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: JSON.stringify({
            logId: String(logId),
            action,
            reason: this.downloadForm.reason,
            format: "JSON",
            requestedBy: this.currentOperator.operatorId
          })
        });
        if (!response.ok) {
          await this.parseResponse(response, false);
          return;
        }
        const result = await response.json();
        if (action === "CLIPBOARD") {
          if (!result.maskedContent) throw new Error("서버가 마스킹된 Export 내용을 반환하지 않았습니다.");
          await navigator.clipboard.writeText(result.maskedContent);
          this.setMessage(`감사된 로그 Export를 복사했습니다. exportId=${result.exportId}`);
          return;
        }
        if (!result.downloadUrl) throw new Error("서버가 만료 다운로드 URL을 반환하지 않았습니다.");
        const artifact = await fetch(result.downloadUrl, { headers: this.apiHeaders() });
        if (!artifact.ok) {
          await this.parseResponse(artifact, false);
          return;
        }
        const url = URL.createObjectURL(await artifact.blob());
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = result.fileName || `cpf-log-export-${result.exportId}.json`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.setMessage(`감사된 로그 Export를 다운로드했습니다. exportId=${result.exportId}`);
      },
  async downloadCsv(downloadType) {
        if (!this.requireReason(this.downloadForm.reason)) return;
        const response = await fetch("/adm/api/downloads/csv", {
          method: "POST",
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: JSON.stringify({ ...this.downloadForm, downloadType })
        });
        if (!response.ok) {
          await this.parseResponse(response, false);
          return;
        }
        const blob = await response.blob();
        const disposition = response.headers.get("content-disposition") || "";
        const match = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/i);
        const fileName = decodeURIComponent(match?.[1] || match?.[2] || `cpf-${downloadType}-${Date.now()}.csv`);
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = fileName;
        anchor.click();
        URL.revokeObjectURL(url);
        await this.loadDownloadPolicies();
        this.setMessage(`${downloadType} CSV 다운로드를 요청했습니다.`);
      },
  async searchLogs() {
        const params = this.buildParams(this.logSearch);
        const data = await this.getJson(`/adm/api/logs?${params.toString()}`);
        this.logs = data.items || [];
        this.logDetail = data;
        this.setMessage(`거래 로그 ${this.logs.length}건을 조회했습니다.`);
      },
  transactionIdOf(item) {
        return item?.transaction_id || item?.transactionId || "";
      },
  async loadTransactionGroups() {
        const params = this.buildParams({
          ...this.transactionGroupSearch,
          sort: this.transactionGroupSort,
          limit: this.transactionGroupPage.size
        });
        const data = await this.getJson(`/adm/api/transaction-groups?${params.toString()}`);
        this.transactionGroupResult = data || { items: [] };
        this.setMessage(`거래 그룹 ${this.transactionGroups.length}건을 조회했습니다.`);
        const first = this.transactionGroups[0];
        if (first && !this.transactionGroupDetail?.transactionId) {
          await this.loadTransactionGroupDetail(this.transactionIdOf(first));
        }
      },
  async loadTransactionGroupDetail(transactionId) {
        if (!transactionId) return;
        const [detail, segments, timeline, headers, externalLogs] = await Promise.allSettled([
          this.getJson(`/adm/api/transaction-groups/${transactionId}`),
          this.getJson(`/adm/api/transaction-groups/${transactionId}/segments`),
          this.getJson(`/adm/api/transaction-groups/${transactionId}/timeline`),
          this.getJson(`/adm/api/transaction-groups/${transactionId}/headers`),
          this.getJson(`/adm/api/transaction-groups/${transactionId}/external-logs`)
        ]);
        const section = (result) => result.status === "fulfilled"
          ? { status: "AVAILABLE", data: result.value }
          : { status: "FAILED", data: null, message: result.reason?.message || "조회 실패" };
        const detailSection = section(detail);
        this.transactionGroupDetail = {
          ...(detailSection.data || {}),
          sectionStatus: {
            detail: detailSection.status,
            segments: section(segments).status,
            timeline: section(timeline).status,
            headers: section(headers).status,
            externalLogs: section(externalLogs).status
          },
          segments: section(segments),
          timeline: section(timeline),
          headers: section(headers),
          externalLogs: section(externalLogs)
        };
        this.transactionGroupDetailTab = "요약";
        this.setMessage(`거래 그룹 상세를 조회했습니다. transactionId=${transactionId}`);
      },
  moveTransactionGroupPage(delta) {
        this.transactionGroupPage.page = Math.min(
          this.transactionGroupTotalPages,
          Math.max(1, this.transactionGroupPage.page + delta)
        );
      },
  resetTransactionGroupSearch() {
        Object.keys(this.transactionGroupSearch).forEach(key => {
          this.transactionGroupSearch[key] = "";
        });
        this.transactionGroupSort = "startedAtDesc";
        this.transactionGroupDetail = {};
      },
  async loadTransactions() {
        const params = this.buildParams(this.transactionSearch);
        this.transactionResult = await this.getJson(`/adm/api/transactions?${params.toString()}`);
      },
  async loadRemoteLogs() {
        const params = this.buildParams(this.remoteLogSearch);
        for (const key of ["lastLines", "keyword", "reason"]) params.delete(key);
        this.remoteLogResult = await this.getJson(`/adm/api/remote-logs?${params.toString()}`);
        const visibleIds = new Set(this.remoteLogResult.map(item => item.artifactId));
        this.remoteLogSelectedIds = this.remoteLogSelectedIds.filter(id => visibleIds.has(id));
      },
  async loadRemoteLogDiagnostics() {
        this.remoteLogDiagnostics = await this.getJson('/adm/api/remote-logs/diagnostics');
      },
  async previewRemoteLog(item) {
        if (!item?.artifactId) return;
        this.selectedRemoteLog = item;
        const params = this.buildParams({
          lastLines: this.remoteLogSearch.lastLines,
          keyword: this.remoteLogSearch.keyword
        });
        this.remoteLogPreview = await this.getJson(`/adm/api/remote-logs/${encodeURIComponent(item.artifactId)}/preview?${params.toString()}`);
      },
  async downloadRemoteLog() {
        if (!this.selectedRemoteLog?.artifactId || !this.requireReason(this.remoteLogSearch.reason)) return;
        const params = this.buildParams({ reason: this.remoteLogSearch.reason });
        const response = await fetch(
          `/adm/api/remote-logs/${encodeURIComponent(this.selectedRemoteLog.artifactId)}/download?${params.toString()}`,
          { headers: this.apiHeaders() }
        );
        if (!response.ok) {
          await this.parseResponse(response, false);
          return;
        }
        const blobUrl = URL.createObjectURL(await response.blob());
        const anchor = document.createElement("a");
        anchor.href = blobUrl;
        anchor.download = this.selectedRemoteLog.fileName || "cpf-log-artifact.log";
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();
        URL.revokeObjectURL(blobUrl);
        this.setMessage("로그 아티팩트를 다운로드했습니다.");
      },
  async downloadRemoteLogBundle() {
        if (this.remoteLogSelectedIds.length === 0 || !this.requireReason(this.remoteLogSearch.reason)) return;
        const response = await fetch('/adm/api/remote-logs/bundles', {
          method: 'POST',
          headers: this.apiHeaders(),
          body: JSON.stringify({ artifactIds: this.remoteLogSelectedIds, reason: this.remoteLogSearch.reason })
        });
        if (!response.ok) {
          await this.parseResponse(response, false);
          return;
        }
        const disposition = response.headers.get('Content-Disposition') || '';
        const fileName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
          ? decodeURIComponent(disposition.match(/filename\*=UTF-8''([^;]+)/i)[1])
          : 'cpf-remote-logs.zip';
        const blobUrl = URL.createObjectURL(await response.blob());
        const anchor = document.createElement('a');
        anchor.href = blobUrl;
        anchor.download = fileName;
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();
        URL.revokeObjectURL(blobUrl);
        const failed = Number(response.headers.get('X-CPF-Partial-Failure-Count') || 0);
        this.setMessage(failed > 0 ? `로그 ZIP을 생성했지만 ${failed}건은 실패했습니다.` : '선택 로그 ZIP을 다운로드했습니다.');
      },
  async scanTransactions() {
        if (!this.requireReason(this.transactionSearch.reason)) return;
        const params = this.buildParams({ reason: this.transactionSearch.reason, requestUser: this.currentOperator.operatorId });
        this.transactionResult = await this.sendJson(`/adm/api/transactions/scan?${params.toString()}`, "POST");
        this.setMessage("거래 메타 재스캔을 요청했습니다.");
      },
  async inactivateTransaction() {
        if (!this.transactionSearch.selectedTransactionId || !this.requireReason(this.transactionSearch.reason)) return;
        const params = this.buildParams({ reason: this.transactionSearch.reason, requestUser: this.currentOperator.operatorId });
        this.transactionResult = await this.sendJson(`/adm/api/transactions/${this.transactionSearch.selectedTransactionId}/inactive?${params.toString()}`, "POST");
        this.setMessage("거래 메타를 비활성화했습니다.");
      },
  async loadLogDetail(logIdx) {
        if (!logIdx) return;
        this.logDetail = await this.getJson(`/adm/api/logs/${logIdx}`);
        this.logDetailTab = "요약";
      },
  async loadAuditLogs() {
        const params = this.buildParams(this.auditSearch);
        const data = await this.getJson(`/adm/api/audit-logs?${params.toString()}`);
        this.auditLogs = data.items || [];
        this.auditResult = data;
      },
  async loadAuditDeliveries() {
        const params = this.buildParams({ state: this.auditDeliveryState, limit: 100 });
        const data = await this.getJson(`/adm/api/audit-logs/deliveries?${params.toString()}`);
        this.auditDeliveries = data.items || data || [];
      },
  async retryAuditDelivery(deliveryId) {
        if (!deliveryId || !this.requireReason(this.auditRetryReason)) return;
        await this.sendJson(`/adm/api/audit-logs/deliveries/${deliveryId}/retry?reason=${encodeURIComponent(this.auditRetryReason)}`, "POST", {});
        await this.loadAuditDeliveries();
        this.setMessage("감사 전달 재처리를 요청했습니다.");
      },
  async loadDownloadPolicies() {
        const [policies, auditLogs] = await Promise.all([
          this.getJson("/adm/api/downloads/policies"),
          this.getJson("/adm/api/downloads/audit-logs?limit=50")
        ]);
        this.downloadResult = { policies, auditLogs };
      },
  async loadLogLevelRules() {
        this.logLevelResult = await this.getJson("/adm/api/log-level/rules");
      },
  async registerLogLevelRule() {
        if (!this.logLevelForm.businessTransactionId && !this.logLevelForm.transactionId) {
          this.setMessage("업무 거래 ID 또는 거래 ID가 필요합니다.");
          return;
        }
        if (Number(this.logLevelForm.ttlSeconds) <= 0) {
          this.setMessage("TTL은 0보다 커야 합니다.");
          return;
        }
        if (!this.requireReason(this.logLevelForm.reason)) return;
        const params = this.buildParams(this.logLevelForm);
        this.logLevelResult = await this.sendJson(`/adm/api/log-level/rules?${params.toString()}`, "PUT");
        this.setMessage("동적 로그 규칙을 등록했습니다.");
      },
  async loadLogPolicies() {
        const params = this.buildParams({
          targetType: this.logPolicyForm.targetType,
          targetId: this.logPolicyForm.targetId,
          activeYn: this.logPolicyForm.activeYn
        });
        this.logPolicyResult = await this.getJson(`/adm/api/log-policies?${params.toString()}`);
      },
  async saveLogPolicy() {
        if (!this.logPolicyForm.policyKey || !this.logPolicyForm.policyName || !this.requireReason(this.logPolicyForm.reason)) return;
        const method = this.logPolicyForm.policyId ? "PUT" : "POST";
        const url = this.logPolicyForm.policyId
          ? `/adm/api/log-policies/${this.logPolicyForm.policyId}`
          : "/adm/api/log-policies";
        const payload = { ...this.logPolicyForm };
        delete payload.selectedOverrideId;
        delete payload.traceBoostTransactionId;
        delete payload.traceBoostBusinessTransactionId;
        delete payload.traceBoostApiPath;
        delete payload.traceBoostStatus;
        delete payload.traceBoostFailureCode;
        delete payload.traceBoostDurationMsGreaterThan;
        delete payload.traceBoostTtlSeconds;
        delete payload.effectiveStartAt;
        delete payload.effectiveEndAt;
        payload.requestUser = undefined;
        this.logPolicyResult = await this.sendJson(url, method, payload);
        await this.loadLogPolicies();
        this.setMessage("로그 정책을 저장했습니다.");
      },
  async createLogPolicyOverride() {
        if (!this.logPolicyForm.targetType || !this.logPolicyForm.targetId || !this.logPolicyForm.effectiveStartAt || !this.logPolicyForm.effectiveEndAt || !this.requireReason(this.logPolicyForm.reason)) return;
        this.logPolicyResult = await this.sendJson("/adm/api/log-policies/overrides", "POST", {
          policyId: this.logPolicyForm.policyId,
          targetType: this.logPolicyForm.targetType,
          targetId: this.logPolicyForm.targetId,
          logLevel: this.logPolicyForm.logLevel,
          dbLogEnabledYn: this.logPolicyForm.dbLogEnabledYn,
          fileLogEnabledYn: this.logPolicyForm.fileLogEnabledYn,
          queryCaptureMode: this.logPolicyForm.queryCaptureMode,
          requestHeaderCaptureMode: this.logPolicyForm.requestHeaderCaptureMode,
          responseHeaderCaptureMode: this.logPolicyForm.responseHeaderCaptureMode,
          requestBodyCaptureMode: this.logPolicyForm.requestBodyCaptureMode,
          responseBodyCaptureMode: this.logPolicyForm.responseBodyCaptureMode,
          errorStackCaptureMode: this.logPolicyForm.errorStackCaptureMode,
          queryAllowlist: this.logPolicyForm.queryAllowlist,
          headerAllowlist: this.logPolicyForm.headerAllowlist,
          fieldAllowlist: this.logPolicyForm.fieldAllowlist,
          maxQueryBytes: this.logPolicyForm.maxQueryBytes,
          maxHeaderBytes: this.logPolicyForm.maxHeaderBytes,
          maxRequestBodyBytes: this.logPolicyForm.maxRequestBodyBytes,
          maxResponseBodyBytes: this.logPolicyForm.maxResponseBodyBytes,
          maxStackBytes: this.logPolicyForm.maxStackBytes,
          maskingPolicyKey: this.logPolicyForm.maskingPolicyKey,
          effectiveStartAt: this.logPolicyForm.effectiveStartAt,
          effectiveEndAt: this.logPolicyForm.effectiveEndAt,
          requestUser: undefined,
          reason: this.logPolicyForm.reason
        });
        this.setMessage("로그 정책 override를 등록했습니다.");
      },
  async disableLogPolicyOverride() {
        const overrideId = String(this.logPolicyForm.selectedOverrideId || "").trim();
        if (!overrideId) { this.setMessage("중지할 Override ID를 선택하거나 입력하세요."); return; }
        if (!this.requireReason(this.logPolicyForm.reason)) return;
        const params = this.buildParams({ reason: this.logPolicyForm.reason });
        this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/overrides/${overrideId}/disable?${params.toString()}`, "PATCH");
        this.setMessage("로그 정책 override를 중지했습니다.");
      },
  async createTraceBoost() {
        if (!this.requireReason(this.logPolicyForm.reason)) return;
        this.logPolicyResult = await this.sendJson("/adm/api/log-policies/trace-boost", "POST", {
          policyId: this.logPolicyForm.policyId,
          transactionId: this.logPolicyForm.traceBoostTransactionId,
          businessTransactionId: this.logPolicyForm.traceBoostBusinessTransactionId || this.logPolicyForm.targetId,
          apiPath: this.logPolicyForm.traceBoostApiPath,
          status: this.logPolicyForm.traceBoostStatus,
          failureCode: this.logPolicyForm.traceBoostFailureCode,
          durationMsGreaterThan: this.logPolicyForm.traceBoostDurationMsGreaterThan,
          logLevel: this.logPolicyForm.logLevel,
          ttlSeconds: this.logPolicyForm.traceBoostTtlSeconds,
          requestUser: this.currentOperator.operatorId,
          reason: this.logPolicyForm.reason
        });
        this.setMessage("Trace Boost를 등록했습니다.");
      },
  async loadTraceBoostRuntimeState() {
        const params = this.buildParams({ limit: 200 });
        this.logPolicyResult = await this.getJson(`/adm/api/log-policies/runtime-state?${params.toString()}`);
      },
  async loadTraceBoostHistory() {
        const params = this.buildParams({ limit: 200 });
        this.logPolicyResult = await this.getJson(`/adm/api/log-policies/history?${params.toString()}`);
      },
  async disableLogPolicy() {
        if (!this.logPolicyForm.policyId || !this.requireReason(this.logPolicyForm.reason)) return;
        const params = this.buildParams({ reason: this.logPolicyForm.reason });
        this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/${this.logPolicyForm.policyId}/disable?${params.toString()}`, "POST");
        this.setMessage("로그 정책을 비활성화했습니다.");
      },
  async refreshLogPolicyCache() {
        if (!this.logPolicyForm.targetType || !this.logPolicyForm.targetId || !this.requireReason(this.logPolicyForm.reason)) return;
        const params = this.buildParams({
          targetType: this.logPolicyForm.targetType,
          targetId: this.logPolicyForm.targetId,
          reason: this.logPolicyForm.reason
        });
        this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/cache/refresh?${params.toString()}`, "POST");
        this.setMessage("로그 정책 cache를 갱신했습니다.");
      },
  async clearLogPolicyCache() {
        if (!this.requireReason(this.logPolicyForm.reason)) return;
        const params = this.buildParams({ reason: this.logPolicyForm.reason });
        this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/cache/clear?${params.toString()}`, "POST");
        this.setMessage("로그 정책 cache를 전체 비웠습니다.");
      },
  async loadLogPolicyDistributionStatus() {
        const params = this.buildParams({
          targetType: this.logPolicyForm.targetType,
          targetId: this.logPolicyForm.targetId,
          limit: 500
        });
        this.logPolicyDistributionResult = await this.getJson(`/adm/api/log-policies/distribution?${params.toString()}`);
      }
};
