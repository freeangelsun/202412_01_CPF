import { createTransactionGlobalId, defaultHeaders } from "../../shared/transaction";

export const coreMethods: Record<string, any> = {
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
        return this.permission(menuId).writeAllowed !== false;
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
  apiHeaders(extraHeaders = {}) {
        const headers: Record<string, string> = {
          ...defaultHeaders,
          "X-Transaction-Id": createTransactionGlobalId(),
          ...extraHeaders
        };
        if (this.token) {
          headers.Authorization = `Bearer ${this.token}`;
        }
        return headers;
      },
  async getJson(url) {
        const response = await fetch(url, { headers: this.apiHeaders() });
        return this.parseResponse(response);
      },
  async sendJson(url, method, body) {
        const response = await fetch(url, {
          method,
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: body ? JSON.stringify(body) : undefined
        });
        return this.parseResponse(response);
      },
  async loadInitialData() {
        await this.loadMe();
        if (!this.authenticated || this.passwordChangeRequired) {
          return;
        }
        await Promise.allSettled([
          this.searchLogs(),
          this.loadTransactionGroups(),
          this.loadTransactions(),
          this.loadStandardExecutions(),
          this.loadChannelPolicy(),
          this.loadRemoteLogs(),
          this.loadAuditLogs(),
          this.loadServiceRegistry(),
          this.searchMembers(),
          this.loadBatch(),
          this.loadCenterCut(),
          this.loadNotifications(),
          this.loadDownloadPolicies(),
          this.loadOperators(),
          this.loadResponseCodes(),
          this.loadLogLevelRules(),
          this.loadLogPolicies(),
          this.loadMessages(),
          this.loadCodes(),
          this.loadConfigs(),
          this.loadCacheSummary(),
          this.loadPermissions(),
          this.loadSecurity()
        ]);
      },
  async loadMe() {
        const data = await this.getJson("/adm/api/auth/me") || {};
        this.currentOperator = data.operatorId ? data : {};
        this.authorizedMenus = data.menus || [];
      },
  clearToken(message) {
        this.token = "";
        this.currentOperator = {};
        this.authorizedMenus = [];
        localStorage.removeItem("admAccessToken");
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
        this.reliabilityResult = await this.sendJson(
          `/adm/api/reliability/broker/dlq/${encodeURIComponent(this.reliabilityAction.messageId)}/replay`,
          "POST",
          { reason: this.reliabilityAction.reason, requestUser: "admin-ui" }
        );
        this.setMessage("DLQ 재처리를 요청했습니다.");
      },
  async resolveUnknownResult() {
        if (!this.reliabilityAction.unknownId || !this.requireReason(this.reliabilityAction.reason)) return;
        this.reliabilityResult = await this.sendJson(
          `/adm/api/reliability/unknown-results/${encodeURIComponent(this.reliabilityAction.unknownId)}/resolve`,
          "POST",
          {
            targetStatus: this.reliabilityAction.targetStatus,
            reason: this.reliabilityAction.reason,
            requestUser: "admin-ui"
          }
        );
        this.setMessage("결과 미확정 건의 수동 처리를 요청했습니다.");
      },
  async saveBusinessDay() {
        if (!this.batchForm.businessDate || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/calendar", "POST", {
          calendarId: this.batchForm.calendarId,
          businessDate: this.batchForm.businessDate,
          holidayYn: this.batchForm.holidayYn,
          businessDayYn: this.batchForm.businessDayYn,
          description: this.batchForm.description,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("영업일 캘린더를 저장했습니다.");
      },
  async createRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await this.sendJson("/adm/api/permissions/roles", "POST", this.roleForm);
        this.setMessage("역할을 등록했습니다.");
      },
  async updateRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.roleForm.roleId}`, "PUT", this.roleForm);
        this.setMessage("역할을 수정했습니다.");
      },
  async createManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await this.sendJson("/adm/api/permissions/menus", "POST", this.menuManageForm);
        this.setMessage("메뉴를 등록했습니다.");
      },
  async updateManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/menus/${this.menuManageForm.menuId}`, "PUT", this.menuManageForm);
        this.setMessage("메뉴를 수정했습니다.");
      },
  async createButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await this.sendJson("/adm/api/permissions/buttons", "POST", this.buttonForm);
        this.setMessage("버튼을 등록했습니다.");
      },
  async updateButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/buttons/${this.buttonForm.buttonId}`, "PUT", this.buttonForm);
        this.setMessage("버튼을 수정했습니다.");
      },
  async saveIpAllowlist() {
        if (!this.securityForm.ipPattern || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson("/adm/api/security/ip-allowlist", "POST", {
          ipPattern: this.securityForm.ipPattern,
          description: this.securityForm.description,
          useYn: "Y",
          requestUser: "admin-ui",
          reason: this.securityForm.reason
        });
        this.setMessage("IP allowlist를 저장했습니다.");
      },
  settledValue(result) {
        if (result.status === "fulfilled") {
          return result.value;
        }
        return {
          status: "미검증",
          message: result.reason?.message || "API wrapper call failed."
        };
      }
};
