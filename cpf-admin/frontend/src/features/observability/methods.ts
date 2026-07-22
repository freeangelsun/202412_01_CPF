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
        await navigator.clipboard.writeText(this.activeLogDetailPayload);
        this.setMessage("로그 상세 내용을 복사했습니다.");
      },
  downloadLogDetail() {
        const blob = new Blob([this.activeLogDetailPayload], { type: "application/json;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = `cpf-log-detail-${Date.now()}.json`;
        anchor.click();
        URL.revokeObjectURL(url);
      },
  async downloadCsv(downloadType) {
        if (!this.requireReason(this.downloadForm.reason)) return;
        const response = await fetch("/adm/api/downloads/csv", {
          method: "POST",
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: JSON.stringify({ ...this.downloadForm, downloadType })
        });
        if (!response.ok) {
          await this.parseResponse(response);
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
  transactionGlobalIdOf(item) {
        return item?.transaction_global_id || item?.transactionGlobalId || "";
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
        if (first && !this.transactionGroupDetail?.transactionGlobalId) {
          await this.loadTransactionGroupDetail(this.transactionGlobalIdOf(first));
        }
      },
  async loadTransactionGroupDetail(transactionGlobalId) {
        if (!transactionGlobalId) return;
        const [detail, segments, timeline, headers, externalLogs] = await Promise.all([
          this.getJson(`/adm/api/transaction-groups/${transactionGlobalId}`),
          this.getJson(`/adm/api/transaction-groups/${transactionGlobalId}/segments`),
          this.getJson(`/adm/api/transaction-groups/${transactionGlobalId}/timeline`),
          this.getJson(`/adm/api/transaction-groups/${transactionGlobalId}/headers`),
          this.getJson(`/adm/api/transaction-groups/${transactionGlobalId}/external-logs`)
        ]);
        this.transactionGroupDetail = {
          ...detail,
          segments,
          timeline,
          headers,
          externalLogs
        };
        this.transactionGroupDetailTab = "요약";
        this.setMessage(`거래 그룹 상세를 조회했습니다. transactionGlobalId=${transactionGlobalId}`);
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
          await this.parseResponse(response);
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
          await this.parseResponse(response);
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
        const params = this.buildParams({ reason: this.transactionSearch.reason, requestUser: "admin-ui" });
        this.transactionResult = await this.sendJson(`/adm/api/transactions/scan?${params.toString()}`, "POST");
        this.setMessage("거래 메타 재스캔을 요청했습니다.");
      },
  async inactivateTransaction() {
        if (!this.transactionSearch.selectedTransactionId || !this.requireReason(this.transactionSearch.reason)) return;
        const params = this.buildParams({ reason: this.transactionSearch.reason, requestUser: "admin-ui" });
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
        this.logPolicyResult = await this.sendJson(url, method, this.logPolicyForm);
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
          requestBodyLogYn: this.logPolicyForm.requestBodyLogYn,
          responseBodyLogYn: this.logPolicyForm.responseBodyLogYn,
          errorStackLogYn: this.logPolicyForm.errorStackLogYn,
          effectiveStartAt: this.logPolicyForm.effectiveStartAt,
          effectiveEndAt: this.logPolicyForm.effectiveEndAt,
          requestUser: "admin-ui",
          reason: this.logPolicyForm.reason
        });
        this.setMessage("로그 정책 override를 등록했습니다.");
      },
  async disableLogPolicyOverride() {
        const overrideId = prompt("중지할 override ID를 입력하세요.");
        if (!overrideId || !this.requireReason(this.logPolicyForm.reason)) return;
        const params = this.buildParams({ reason: this.logPolicyForm.reason });
        this.logPolicyResult = await this.sendJson(`/adm/api/log-policies/overrides/${overrideId}/disable?${params.toString()}`, "PATCH");
        this.setMessage("로그 정책 override를 중지했습니다.");
      },
  async createTraceBoost() {
        if (!this.requireReason(this.logPolicyForm.reason)) return;
        this.logPolicyResult = await this.sendJson("/adm/api/log-policies/trace-boost", "POST", {
          policyId: this.logPolicyForm.policyId,
          transactionGlobalId: this.logPolicyForm.traceBoostTransactionGlobalId,
          businessTransactionId: this.logPolicyForm.traceBoostBusinessTransactionId || this.logPolicyForm.targetId,
          apiPath: this.logPolicyForm.traceBoostApiPath,
          status: this.logPolicyForm.traceBoostStatus,
          failureCode: this.logPolicyForm.traceBoostFailureCode,
          durationMsGreaterThan: this.logPolicyForm.traceBoostDurationMsGreaterThan,
          logLevel: this.logPolicyForm.logLevel,
          ttlSeconds: this.logPolicyForm.traceBoostTtlSeconds,
          requestUser: "admin-ui",
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
      }
};
