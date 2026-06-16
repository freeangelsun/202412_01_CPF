const defaultHeaders = {
  "X-Request-Type": "INQUIRY",
  "X-Original-Channel-Code": "ADM",
  "X-Channel-Code": "ADM",
  "X-User-Id": "admin-ui"
};

if (!window.Vue) {
  document.body.innerHTML = "<main class=\"panel\"><h2>Vue load failed</h2><p>Check /adm/vendor/vue.global.prod.js.</p></main>";
} else {
  const { createApp } = window.Vue;

  createApp({
    data() {
      return {
        activeMenu: "logs",
        token: localStorage.getItem("admAccessToken") || "",
        currentOperator: {},
        authorizedMenus: [],
        authMessage: "",
        uiMessage: "",
        loginForm: { operatorId: "admin", password: "Adm!n12345" },
        menus: [
          { id: "logs", menuId: "LOG_LIST", label: "Logs" },
          { id: "auditLogs", menuId: "AUDIT_LOG", label: "Audit" },
          { id: "cache", menuId: "CACHE", label: "Cache" },
          { id: "responseCodes", menuId: "RESPONSE_CODE", label: "Response Codes" },
          { id: "logLevel", menuId: "DYNAMIC_LOG", label: "Dynamic Log" },
          { id: "operators", menuId: "OPERATOR", label: "Operators" }
        ],
        cacheTargets: ["ALL", "CODE", "MESSAGE", "RESPONSE_CODE", "CONFIG"],
        logSearch: { transactionId: "", businessTransactionId: "", memberNo: "", customerNo: "" },
        auditSearch: { operatorId: "", actionType: "", targetType: "", targetId: "", limit: 100 },
        logSort: { key: "LOG_IDX", direction: "desc" },
        logPage: { page: 1, size: 10 },
        cacheReason: "ADM cache refresh",
        responseCodeReason: "ADM response code change",
        logLevelForm: { businessTransactionId: "", transactionId: "", logLevel: "DEBUG", ttlSeconds: 600, reason: "ADM diagnostics" },
        operatorForm: { operatorId: "", operatorName: "", password: "", reason: "ADM operator provisioning" },
        responseCodeForm: {
          responseCode: "EXYZ010001",
          messageCode: "MXYZ090001",
          resultType: "E",
          moduleId: "XYZ",
          responseGroup: "01",
          sequenceNo: "0001",
          httpStatus: 400,
          description: "XYZ sample response code",
          useYn: "Y",
          requestUser: "admin-ui"
        },
        logs: [],
        auditLogs: [],
        logDetail: {},
        auditResult: {},
        cacheResult: {},
        responseCodeResult: {},
        logLevelResult: {},
        operatorResult: {}
      };
    },
    computed: {
      authenticated() {
        return !!this.token;
      },
      visibleMenus() {
        if (!this.authorizedMenus.length) {
          return this.menus;
        }
        const allowed = new Set(this.authorizedMenus.map(menu => menu.menuId || menu.id));
        return this.menus.filter(menu => allowed.has(menu.menuId));
      },
      sortedLogs() {
        const items = [...this.logs];
        const { key, direction } = this.logSort;
        items.sort((left, right) => {
          const a = left?.[key] ?? "";
          const b = right?.[key] ?? "";
          if (a === b) return 0;
          return (a > b ? 1 : -1) * (direction === "asc" ? 1 : -1);
        });
        return items;
      },
      pagedLogs() {
        const start = (this.logPage.page - 1) * this.logPage.size;
        return this.sortedLogs.slice(start, start + this.logPage.size);
      },
      logTotalPages() {
        return Math.max(1, Math.ceil(this.sortedLogs.length / this.logPage.size));
      }
    },
    watch: {
      logs() {
        this.logPage.page = 1;
      }
    },
    mounted() {
      if (this.authenticated) {
        this.loadMe();
        this.searchLogs();
        this.loadAuditLogs();
        this.loadOperators();
        this.loadResponseCodes();
        this.loadLogLevelRules();
      }
    },
    methods: {
      pretty(value) {
        return JSON.stringify(value, null, 2);
      },
      setMessage(message) {
        this.uiMessage = message || "";
      },
      permission(menuId) {
        const found = this.authorizedMenus.find(menu => (menu.menuId || menu.id) === menuId);
        return found || { readAllowed: true, writeAllowed: true, deleteAllowed: true };
      },
      canWrite(menuId) {
        return this.permission(menuId).writeAllowed !== false;
      },
      canDelete(menuId) {
        return this.permission(menuId).deleteAllowed !== false;
      },
      requireReason(reason) {
        if (!reason || !String(reason).trim()) {
          this.setMessage("Audit reason is required.");
          return false;
        }
        return true;
      },
      sortLogs(key) {
        if (this.logSort.key === key) {
          this.logSort.direction = this.logSort.direction === "asc" ? "desc" : "asc";
        } else {
          this.logSort = { key, direction: "asc" };
        }
      },
      moveLogPage(delta) {
        this.logPage.page = Math.min(this.logTotalPages, Math.max(1, this.logPage.page + delta));
      },
      apiHeaders(extraHeaders = {}) {
        const headers = { ...defaultHeaders, ...extraHeaders };
        if (this.token) {
          headers.Authorization = `Bearer ${this.token}`;
        }
        return headers;
      },
      async parseResponse(response) {
        const contentType = response.headers.get("content-type") || "";
        const data = contentType.includes("application/json")
          ? await response.json()
          : { message: await response.text() };
        if (response.status === 401) {
          this.clearToken("Session expired. Please sign in again.");
        } else if (response.status === 403) {
          this.setMessage(data.message || "Permission is required for this operation.");
        } else if (!response.ok) {
          this.setMessage(data.message || `Request failed. status=${response.status}`);
        }
        return data;
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
      async login() {
        if (!this.loginForm.operatorId || !this.loginForm.password) {
          this.authMessage = "Operator ID and password are required.";
          return;
        }
        const response = await fetch("/adm/api/auth/login", {
          method: "POST",
          headers: { ...defaultHeaders, "Content-Type": "application/json" },
          body: JSON.stringify(this.loginForm)
        });
        const data = await this.parseResponse(response);
        if (!response.ok || !data.accessToken) {
          this.authMessage = JSON.stringify(data, null, 2);
          return;
        }
        this.token = data.accessToken;
        this.currentOperator = data.operator || {};
        this.authorizedMenus = data.menus || [];
        localStorage.setItem("admAccessToken", this.token);
        this.authMessage = "";
        this.setMessage("Signed in.");
        this.searchLogs();
        this.loadAuditLogs();
        this.loadOperators();
        this.loadResponseCodes();
        this.loadLogLevelRules();
      },
      async loadMe() {
        const data = await this.getJson("/adm/api/auth/me") || {};
        this.currentOperator = data.operatorId ? data : {};
        this.authorizedMenus = data.menus || [];
      },
      async logout() {
        await this.sendJson("/adm/api/auth/logout", "POST");
        this.clearToken("Signed out.");
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
        Object.entries(values).forEach(([key, value]) => {
          if (value !== null && value !== undefined && String(value).trim() !== "") {
            params.set(key, value);
          }
        });
        return params;
      },
      async searchLogs() {
        const params = this.buildParams(this.logSearch);
        const data = await this.getJson(`/adm/api/logs?${params.toString()}`);
        this.logs = data.items || [];
        this.logDetail = data;
        this.setMessage(`Loaded ${this.logs.length} logs.`);
      },
      async loadLogDetail(logIdx) {
        if (!logIdx) return;
        this.logDetail = await this.getJson(`/adm/api/logs/${logIdx}`);
      },
      async loadAuditLogs() {
        const params = this.buildParams(this.auditSearch);
        const data = await this.getJson(`/adm/api/audit-logs?${params.toString()}`);
        this.auditLogs = data.items || [];
        this.auditResult = data;
        this.setMessage(`Loaded ${this.auditLogs.length} audit logs.`);
      },
      async refreshCache(target) {
        if (!this.requireReason(this.cacheReason)) return;
        const params = this.buildParams({ target, reason: this.cacheReason, requestUser: "admin-ui" });
        this.cacheResult = await this.sendJson(`/adm/api/cache/refresh?${params.toString()}`, "POST");
        this.setMessage(`${target} cache refresh requested.`);
      },
      async loadResponseCodes() {
        this.responseCodeResult = await this.getJson("/adm/api/response-codes");
      },
      validateResponseCodeForm() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code)) {
          return "Response Code must match EACC010001 or SACC000000 format.";
        }
        if (code[0] !== this.responseCodeForm.resultType) {
          return "Result Type must match the first character of Response Code.";
        }
        if (code.substring(1, 4) !== this.responseCodeForm.moduleId) {
          return "Module must match Response Code positions 2-4.";
        }
        if (code.substring(4, 6) !== this.responseCodeForm.responseGroup) {
          return "Group must match Response Code positions 5-6.";
        }
        if (code.substring(6, 10) !== this.responseCodeForm.sequenceNo) {
          return "Seq must match Response Code positions 7-10.";
        }
        if (!/^M[A-Z]{3}[0-9]{6}$/.test(this.responseCodeForm.messageCode || "")) {
          return "Message Code must match MCMN000001 format.";
        }
        if (!this.requireReason(this.responseCodeReason)) {
          return "Audit reason is required.";
        }
        return "";
      },
      async createResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        const params = this.buildParams({ reason: this.responseCodeReason });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes?${params.toString()}`, "POST", this.responseCodeForm);
        this.setMessage("Response code created.");
      },
      async updateResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        const params = this.buildParams({ reason: this.responseCodeReason });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes/${this.responseCodeForm.responseCode}?${params.toString()}`, "PUT", this.responseCodeForm);
        this.setMessage("Response code updated.");
      },
      async deleteResponseCode() {
        if (!this.requireReason(this.responseCodeReason)) return;
        const params = this.buildParams({ reason: this.responseCodeReason, requestUser: "admin-ui" });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes/${this.responseCodeForm.responseCode}?${params.toString()}`, "DELETE");
        this.setMessage("Response code delete requested.");
      },
      async loadLogLevelRules() {
        this.logLevelResult = await this.getJson("/adm/api/log-level/rules");
      },
      async registerLogLevelRule() {
        if (!this.logLevelForm.businessTransactionId && !this.logLevelForm.transactionId) {
          this.setMessage("Business Transaction ID or Transaction ID is required.");
          return;
        }
        if (Number(this.logLevelForm.ttlSeconds) <= 0) {
          this.setMessage("TTL must be greater than zero.");
          return;
        }
        if (!this.requireReason(this.logLevelForm.reason)) return;
        const params = this.buildParams(this.logLevelForm);
        this.logLevelResult = await this.sendJson(`/adm/api/log-level/rules?${params.toString()}`, "PUT");
        this.setMessage("Dynamic log rule registered.");
      },
      async removeLogLevelRule(ruleId) {
        if (!ruleId || !this.requireReason(this.logLevelForm.reason)) return;
        const params = this.buildParams({ reason: this.logLevelForm.reason, requestUser: "admin-ui" });
        this.logLevelResult = await this.sendJson(`/adm/api/log-level/rules/${ruleId}?${params.toString()}`, "DELETE");
        this.setMessage("Dynamic log rule removed.");
        this.loadLogLevelRules();
      },
      async loadOperators() {
        this.operatorResult = await this.getJson("/adm/api/operators");
      },
      async createOperator() {
        if (!this.operatorForm.operatorId || !this.operatorForm.operatorName || !this.operatorForm.password) {
          this.setMessage("Operator ID, name, and initial password are required.");
          return;
        }
        if (this.operatorForm.password.length < 10) {
          this.setMessage("Initial password must be at least 10 characters.");
          return;
        }
        if (!this.requireReason(this.operatorForm.reason)) return;
        this.operatorResult = await this.sendJson("/adm/api/operators", "POST", {
          operatorId: this.operatorForm.operatorId,
          operatorName: this.operatorForm.operatorName,
          password: this.operatorForm.password,
          roleIds: ["ADM_VIEWER"],
          requestUser: "admin-ui",
          reason: this.operatorForm.reason
        });
        this.setMessage("Operator create requested.");
      }
    }
  }).mount("#app");
}
