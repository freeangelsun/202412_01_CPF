import { admMutation, admQuery, admRawResponse, createAdmHeaders } from "../../shared/cpfApi";
import { getAdmAuthMe } from "../../generated/cpf-api";
import { useAdmInitializationStore } from "../../stores/admInitializationStore";

export const coreMethods: Record<string, any> = {
  apiHeaders(extraHeaders: HeadersInit = {}) { return createAdmHeaders(extraHeaders); },
  async getJson(url: string) { return admQuery(url); },
  async sendJson(url: string, method: "POST" | "PUT" | "PATCH" | "DELETE" = "POST", body?: unknown) { return admMutation(url, method, body); },
  async rawResponse(url: string, method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE" = "GET", body?: unknown, headers: HeadersInit = {}) { return admRawResponse(url, method, body, headers); },
  clearToken(message = "") { this.clearSession(message); },
  pretty(value) {
        if (value === null || value === undefined || value === "") {
          return "";
        }
        if (typeof value === "string") {
          try {
            return JSON.stringify(JSON.parse(value), null, 2);
          } catch (error) {
            return value;
          }
        }
        return JSON.stringify(value, null, 2);
      },
  canWrite(menuId) {
        return this.permission(menuId).writeAllowed === true;
      },
  canButton(buttonId, menuId = "") {
        if (this.buttonsLoaded) return this.authorizedButtons.includes(buttonId);
        // local/test MEMORY에서는 Button projection이 없을 수 있으므로 메뉴 권한을 사용하되 서버 Filter가 최종 차단한다.
        return menuId ? this.canWrite(menuId) : false;
      },
  canDelete(menuId) {
        return this.permission(menuId).deleteAllowed !== false;
      },
  requireReason(reason) {
        if (!reason || !String(reason).trim()) {
          this.setMessage("감사 사유는 필수입니다.");
          return false;
        }
        return true;
      },
  async loadInitialData() {
        const initialization = useAdmInitializationStore();
        initialization.begin();
        this.initializationStatus = initialization.status;
        this.initializationFailures = [];
        await this.loadMe();
        if (!this.authenticated || this.passwordChangeRequired) {
          initialization.blocked();
          this.initializationStatus = initialization.status;
          return;
        }

        const required = [
          { name: "permissions", run: () => this.loadPermissions() },
          { name: "security", run: () => this.loadSecurity() },
          { name: "service-registry", run: () => this.loadServiceRegistry() }
        ];
        for (const item of required) {
          try {
            await item.run();
          } catch (error) {
            const message = initialization.record(item.name, error, true);
            this.initializationFailures = initialization.failures;
            this.initializationStatus = initialization.status;
            this.setMessage(`필수 운영 API 초기화 실패: ${item.name} - ${message}`);
            throw error;
          }
        }

        const optional = [
          { name: "logs", run: () => this.searchLogs() },
          { name: "transaction-groups", run: () => this.loadTransactionGroups() },
          { name: "transactions", run: () => this.loadTransactions() },
          { name: "standard-executions", run: () => this.loadStandardExecutions() },
          { name: "channel-policy", run: () => this.loadChannelPolicy() },
          { name: "remote-logs", run: () => this.loadRemoteLogs() },
          { name: "audit-logs", run: () => this.loadAuditLogs() },
          { name: "batch", run: () => this.loadBatch() },
          { name: "center-cut", run: () => this.loadCenterCut() },
          { name: "notifications", run: () => this.loadNotifications() },
          { name: "downloads", run: () => this.loadDownloadPolicies() },
          { name: "operators", run: () => this.loadOperators() },
          { name: "response-codes", run: () => this.loadResponseCodes() },
          { name: "log-level", run: () => this.loadLogLevelRules() },
          { name: "log-policies", run: () => this.loadLogPolicies() },
          { name: "messages", run: () => this.loadMessages() },
          { name: "codes", run: () => this.loadCodes() },
          { name: "configs", run: () => this.loadConfigs() },
          { name: "cache", run: () => this.loadCacheSummary() }
        ];
        const settled = await Promise.allSettled(optional.map(item => item.run()));
        settled.forEach((result, index) => {
          if (result.status === "rejected") {
            initialization.record(optional[index].name, result.reason, false);
          }
        });
        initialization.complete();
        this.initializationFailures = initialization.failures;
        this.initializationStatus = initialization.status;
        if (this.initializationFailures.length) {
          this.setMessage(`선택 운영 API ${this.initializationFailures.length}건을 불러오지 못했습니다. 화면별 재시도를 사용하세요.`);
        }
      },
  async loadMe() {
        this.permissionsLoaded = false;
        try {
          const data = await getAdmAuthMe<any>() || {};
          this.currentOperator = data.operatorId ? data : {};
          this.authorizedMenus = Array.isArray(data.menus) ? data.menus : [];
          this.authorizedButtons = Array.isArray(data.buttonIds) ? data.buttonIds : [];
          this.buttonsLoaded = Array.isArray(data.buttonIds);
          this.permissionsLoaded = true;
        } catch (error) {
          this.currentOperator = {};
          this.authorizedMenus = [];
          this.authorizedButtons = [];
          this.buttonsLoaded = false;
          this.permissionsLoaded = false;
          throw error;
        }
      },
  clearSession(message) {
        this.sessionLoaded = false;
        this.currentOperator = {};
        this.authorizedMenus = [];
        this.authorizedButtons = [];
        this.buttonsLoaded = false;
        this.permissionsLoaded = false;
        // Browser에는 지울 access token이 존재하지 않는다. 서버 세션 종료 후 화면의 민감 상태만 폐기한다.
        if (typeof this.resetSensitiveState === "function") this.resetSensitiveState();
        this.authMessage = message || "";
      },
  buildParams(values) {
        const params = new URLSearchParams();
        Object.entries(values || {}).forEach(([key, value]) => {
          if (value !== null && value !== undefined && String(value).trim() !== "") {
            params.set(key, String(value));
          }
        });
        return params;
      },
  fixedLengthDetails(detail) {
        const formatted = detail?.formattedDetails || [];
        return formatted.filter(item => {
          const key = String(item.detailKey || item.DETAIL_KEY || "").toLowerCase();
          return key.includes("fixed") || key.includes("telegram") || key.includes("전문");
        });
      },
  async replayDlq() {
        if (!this.reliabilityAction.messageId || !this.requireReason(this.reliabilityAction.reason)) return;
        this.reliabilityResult = await admMutation(
          `/adm/api/reliability/broker/dlq/${encodeURIComponent(this.reliabilityAction.messageId)}/replay`,
          "POST",
          { reason: this.reliabilityAction.reason }
        );
        this.setMessage("DLQ 재처리를 요청했습니다.");
      },
  async resolveUnknownResult() {
        if (!this.reliabilityAction.unknownId || !this.requireReason(this.reliabilityAction.reason)) return;
        this.reliabilityResult = await admMutation(
          `/adm/api/reliability/unknown-results/${encodeURIComponent(this.reliabilityAction.unknownId)}/resolve`,
          "POST",
          {
            targetStatus: this.reliabilityAction.targetStatus,
            reason: this.reliabilityAction.reason,
          }
        );
        this.setMessage("결과 미확정 건의 수동 처리를 요청했습니다.");
      },

  async createRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await admMutation("/adm/api/permissions/roles", "POST", this.roleForm);
        this.setMessage("역할을 등록했습니다.");
      },
  async updateRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await admMutation(`/adm/api/permissions/roles/${this.roleForm.roleId}`, "PUT", this.roleForm);
        this.setMessage("역할을 수정했습니다.");
      },
  async createManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await admMutation("/adm/api/permissions/menus", "POST", this.menuManageForm);
        this.setMessage("메뉴를 등록했습니다.");
      },
  async updateManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await admMutation(`/adm/api/permissions/menus/${this.menuManageForm.menuId}`, "PUT", this.menuManageForm);
        this.setMessage("메뉴를 수정했습니다.");
      },
  async createButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await admMutation("/adm/api/permissions/buttons", "POST", this.buttonForm);
        this.setMessage("버튼을 등록했습니다.");
      },
  async updateButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await admMutation(`/adm/api/permissions/buttons/${this.buttonForm.buttonId}`, "PUT", this.buttonForm);
        this.setMessage("버튼을 수정했습니다.");
      },
  async saveIpAllowlist() {
        if (!this.securityForm.ipPattern || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await admMutation("/adm/api/security/ip-allowlist", "POST", {
          ipPattern: this.securityForm.ipPattern,
          description: this.securityForm.description,
          useYn: "Y",
          reason: this.securityForm.reason
        });
        this.setMessage("IP allowlist를 저장했습니다.");
      },
  settledValue(result) {
        if (result.status === "fulfilled") {
          return { status: "AVAILABLE", data: result.value, message: "" };
        }
        return {
          status: "FAILED",
          data: null,
          message: result.reason?.message || "API wrapper call failed."
        };
      }
};
