import {
  admSecuritySaveIpAllowlist,
  requestAdmBrokerDlqReplay,
  resolveAdmUnknownResult,
  admPermissionCreateRole, admPermissionUpdateRole,
  admPermissionCreateMenu, admPermissionUpdateMenu,
  admPermissionCreateButton, admPermissionUpdateButton
} from "../../generated/cpf-api";
import { admMutation, admQuery, admRawResponse, createAdmHeaders } from "../../shared/cpfApi";
import { admAuthMe, getAdmReadiness } from "../../generated/cpf-api";
import { useAdmInitializationStore } from "../../stores/admInitializationStore";

export const coreMethods = {
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
        // Button projection is a security boundary. Missing or not-yet-loaded
        // projection must never inherit broad menu WRITE permission.
        if (!this.buttonsLoaded) return false;
        return this.authorizedButtons.includes(buttonId);
      },
  canDelete(menuId) {
        // Absence is not permission. Only an explicit server projection grants DELETE.
        return this.permission(menuId).deleteAllowed === true;
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

        // Shell bootstrap must use only the least-privilege session projection returned by /auth/me.
        // Permission master/catalog and service-registry data are route-owned and loaded lazily only
        // after the server-projected menu/operation permission allows navigation to that feature.
        // A normal operator therefore never needs permission-management master APIs just to enter ADM.
        try {
          const health = await getAdmReadiness<any>();
          this.shellHealth = health || { status: "UNKNOWN" };
          this.shellHealthFetchedAt = Date.now();
        } catch {
          this.shellHealth = { status: "UNKNOWN" };
          this.shellHealthFetchedAt = 0;
        }
        initialization.complete();
        this.initializationFailures = initialization.failures;
        this.initializationStatus = initialization.status;
      },
  async loadRouteData(routeId: string) {
        const loaders: Record<string, () => Promise<unknown>> = {
          logs: () => this.searchLogs(true),
          transactionGroups: () => this.loadTransactionGroups(),
          transactions: () => this.loadTransactions(),
          remoteLogs: () => this.loadRemoteLogs(),
          auditLogs: () => this.loadAuditLogs(),
          logLevel: () => this.loadLogLevelRules(),
          logPolicies: () => this.loadLogPolicies(),
          standardExecutions: () => this.loadStandardExecutions(),
          channelPolicy: () => this.loadChannelPolicy(),
          serviceRegistry: () => this.loadServiceRegistry(),
          operators: () => this.loadOperators(),
          security: () => this.loadSecurity(),
          responseCodes: () => this.loadResponseCodes(),
          messages: () => this.loadMessages(),
          codes: () => this.loadCodes(),
          configs: () => this.loadConfigs(),
          cache: () => this.loadCacheSummary(),
          batch: () => this.loadBatch(),
          centerCut: () => this.loadCenterCut(),
          notifications: () => this.loadNotifications(),
          downloads: () => this.loadDownloadPolicies()
        };
        const loader = loaders[routeId];
        if (!loader) return;
        try {
          await loader();
        } catch (error) {
          // Route failures belong to the route boundary; they must not poison the entire ADM shell.
          this.setMessage(`${routeId} 화면 데이터를 불러오지 못했습니다. 화면에서 재시도하십시오.`);
          throw error;
        }
      },
  async loadMe() {
        this.permissionsLoaded = false;
        try {
          const data = await admAuthMe<any>() || {};
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
        this.reliabilityResult = await requestAdmBrokerDlqReplay({
          path: { messageId: this.reliabilityAction.messageId },
          data: { reason: this.reliabilityAction.reason }
        });
        this.setMessage("DLQ 재처리 승인 요청을 생성했습니다. 승인 완료 후 Owner Command가 실행됩니다.");
      },
  async resolveUnknownResult() {
        if (!this.reliabilityAction.unknownId || !this.requireReason(this.reliabilityAction.reason)) return;
        this.reliabilityResult = await resolveAdmUnknownResult({
          path: { unknownId: this.reliabilityAction.unknownId },
          data: {
            targetStatus: this.reliabilityAction.targetStatus,
            expectedVersion: Number(this.reliabilityAction.expectedVersion ?? 0),
            reason: this.reliabilityAction.reason
          }
        });
        this.setMessage("결과 미확정 건의 수동 처리를 요청했습니다.");
      },

  async createRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await admPermissionCreateRole({ data: this.roleForm });
        this.setMessage("역할을 등록했습니다.");
      },
  async updateRole() {
        if (!this.roleForm.roleId || !this.roleForm.roleName || !this.requireReason(this.roleForm.reason)) return;
        this.permissionResult = await admPermissionUpdateRole({ path: { roleId: this.roleForm.roleId }, data: this.roleForm });
        this.setMessage("역할을 수정했습니다.");
      },
  async createManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await admPermissionCreateMenu({ data: this.menuManageForm });
        this.setMessage("메뉴를 등록했습니다.");
      },
  async updateManagedMenu() {
        if (!this.menuManageForm.menuId || !this.menuManageForm.menuName || !this.requireReason(this.menuManageForm.reason)) return;
        this.permissionResult = await admPermissionUpdateMenu({ path: { menuId: this.menuManageForm.menuId }, data: this.menuManageForm });
        this.setMessage("메뉴를 수정했습니다.");
      },
  async createButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await admPermissionCreateButton({ data: this.buttonForm });
        this.setMessage("버튼을 등록했습니다.");
      },
  async updateButton() {
        if (!this.buttonForm.buttonId || !this.buttonForm.menuId || !this.buttonForm.buttonName || !this.requireReason(this.buttonForm.reason)) return;
        this.permissionResult = await admPermissionUpdateButton({ path: { buttonId: this.buttonForm.buttonId }, data: this.buttonForm });
        this.setMessage("버튼을 수정했습니다.");
      },
  async saveIpAllowlist() {
        if (!this.securityForm.ipPattern || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await admSecuritySaveIpAllowlist({ data: {
          ipPattern: this.securityForm.ipPattern,
          description: this.securityForm.description,
          useYn: "Y",
          reason: this.securityForm.reason
        } });
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
} satisfies Record<string, any>;
