import { clearAdmAccessToken, createAdmHeaders } from "../../shared/cpfApi";

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
  apiHeaders(extraHeaders = {}) {
        return createAdmHeaders(extraHeaders);
      },
  async getJson(url) {
        const response = await fetch(url, { headers: this.apiHeaders(), credentials: "include", cache: "no-store" });
        return this.parseResponse(response);
      },
  async sendJson(url, method, body) {
        const mutation = ["POST", "PUT", "PATCH", "DELETE"].includes(String(method).toUpperCase());
        let resolvedUrl = url;
        let resolvedBody = body;
        if (mutation) {
          const actor = this.currentOperator?.operatorId;
          if (!actor) throw new Error("검증된 ADM 운영자 ID가 없습니다.");
          const parsed = new URL(url, window.location.origin);
          if (parsed.searchParams.has("requestUser")) parsed.searchParams.set("requestUser", actor);
          resolvedUrl = parsed.pathname + parsed.search + parsed.hash;
          if (body && typeof body === "object" && !Array.isArray(body) && Object.prototype.hasOwnProperty.call(body, "requestUser")) {
            resolvedBody = { ...body, requestUser: actor };
          }
        }
        const response = await fetch(resolvedUrl, {
          method,
          headers: this.apiHeaders({ "Content-Type": "application/json" }),
          body: resolvedBody ? JSON.stringify(resolvedBody) : undefined
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
        this.permissionsLoaded = false;
        try {
          const data = await this.getJson("/adm/api/auth/me") || {};
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
        this.permissionsLoaded = false;
        this.currentOperator = {};
        this.authorizedMenus = [];
        this.authorizedButtons = [];
        this.buttonsLoaded = false;
        this.permissionsLoaded = false;
        clearAdmAccessToken();
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
        this.reliabilityResult = await this.sendJson(
          `/adm/api/reliability/broker/dlq/${encodeURIComponent(this.reliabilityAction.messageId)}/replay`,
          "POST",
          { reason: this.reliabilityAction.reason, requestUser: this.currentOperator.operatorId }
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
            requestUser: this.currentOperator.operatorId
          }
        );
        this.setMessage("결과 미확정 건의 수동 처리를 요청했습니다.");
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
          requestUser: this.currentOperator.operatorId,
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
