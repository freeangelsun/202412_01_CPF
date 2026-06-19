const defaultHeaders = {
  "X-Request-Type": "INQUIRY",
  "X-Original-Channel-Code": "ADM",
  "X-Channel-Code": "ADM",
  "X-User-Id": "admin-ui",
  "X-Client-App-Id": "cpf-adm-ui",
  "X-Client-Version": "1.0.0",
  "X-Caller-Service": "adm-ui"
};

if (!window.Vue) {
  document.body.innerHTML = "<main class=\"panel\"><h2>Vue 로드 실패</h2><p>/adm/vendor/vue.global.prod.js 파일을 확인하세요.</p></main>";
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
          { id: "logs", menuId: "LOG_LIST", label: "거래 로그" },
          { id: "auditLogs", menuId: "AUDIT_LOG", label: "감사 로그" },
          { id: "members", menuId: "MEMBER", label: "회원" },
          { id: "batch", menuId: "BATCH", label: "배치" },
          { id: "notifications", menuId: "NOTIFICATION", label: "알림" },
          { id: "downloads", menuId: "DOWNLOAD", label: "다운로드" },
          { id: "cache", menuId: "CACHE", label: "캐시" },
          { id: "messages", menuId: "MESSAGE", label: "메시지" },
          { id: "codes", menuId: "CODE", label: "코드" },
          { id: "responseCodes", menuId: "RESPONSE_CODE", label: "응답코드" },
          { id: "configs", menuId: "CONFIG", label: "설정" },
          { id: "logLevel", menuId: "DYNAMIC_LOG", label: "동적 로그" },
          { id: "permissions", menuId: "PERMISSION", label: "권한" },
          { id: "password", menuId: "PASSWORD", label: "비밀번호" },
          { id: "security", menuId: "SECURITY", label: "보안" },
          { id: "operators", menuId: "OPERATOR", label: "운영자" }
        ],
        logSearch: {
          transactionId: "",
          traceId: "",
          businessTransactionId: "",
          uri: "",
          responseCode: "",
          httpStatus: "",
          memberNo: "",
          customerNo: "",
          channelCode: "",
          logType: ""
        },
        logSort: { key: "LOG_IDX", direction: "desc" },
        logPage: { page: 1, size: 10 },
        logDetailTab: "요약",
        logDetailTabs: ["요약", "헤더", "요청", "응답", "오류", "상세", "전문"],
        auditSearch: { operatorId: "", actionType: "", targetType: "", targetId: "", limit: 100 },
        memberSearch: {
          memberNo: "",
          customerNo: "",
          loginId: "",
          name: "",
          email: "",
          mobileNo: "",
          memberStatus: "",
          channelCode: "",
          roleCode: "",
          limit: 100
        },
        memberForm: {
          memberId: null,
          memberNo: "",
          customerNo: "",
          loginId: "",
          name: "",
          email: "",
          mobileNo: "",
          memberStatus: "ACTIVE",
          channelCode: "WEB",
          description: "",
          requestUser: "admin-ui",
          reason: "회원 운영 변경"
        },
        memberStatusForm: { memberStatus: "ACTIVE", lockYn: "N", withdrawYn: "N" },
        memberRoleForm: {
          serviceCode: "MBR",
          roleCode: "MBR_USER",
          roleName: "일반 회원",
          temporaryYn: "N",
          expireAt: "",
          reason: "회원 권한 변경",
          requestUser: "admin-ui"
        },
        batchForm: {
          jobId: "CPF_EDU_TASKLET_JOB",
          jobName: "CPF EDU Tasklet Job",
          jobType: "TASKLET",
          executionId: null,
          scheduleId: "CPF_EDU_TASKLET_DAILY",
          jobParameters: "{\"sample\":true}",
          calendarId: "DEFAULT",
          businessDate: new Date().toISOString().slice(0, 10),
          simulationDays: 14,
          dispatchStatus: "WAITING",
          holidayYn: "N",
          businessDayYn: "Y",
          description: "ADM 영업일 샘플",
          reason: "배치 운영 변경"
        },
        notificationForm: {
          ruleId: null,
          eventType: "BATCH",
          eventSubType: "FAILED",
          channelCode: "ADM",
          templateCode: "",
          severity: "WARN",
          receiverGroup: "ADM_OPERATOR",
          useYn: "Y",
          targetType: "ADM_TEST",
          targetId: "TEST",
          receiver: "ADM_OPERATOR",
          message: "ADM 알림 테스트 발송입니다.",
          reason: "알림 규칙 변경",
          requestUser: "admin-ui"
        },
        downloadForm: {
          downloadType: "TRANSACTION_LOGS",
          targetType: "LOG_LIST",
          fromDate: "",
          toDate: "",
          transactionId: "",
          traceId: "",
          jobId: "",
          limit: 1000,
          includeSensitive: false,
          reason: "운영 점검 다운로드",
          requestUser: "admin-ui"
        },
        cacheTargets: ["ALL", "CODE", "MESSAGE", "RESPONSE_CODE", "CONFIG"],
        cacheReason: "ADM 캐시 갱신",
        responseCodeReason: "ADM 응답코드 변경",
        logLevelForm: { businessTransactionId: "", transactionId: "", logLevel: "DEBUG", ttlSeconds: 600, reason: "운영 진단" },
        operatorForm: { operatorId: "", operatorName: "", password: "", reason: "운영자 등록" },
        messageForm: {
          messageId: null,
          messageCode: "MPFW990099",
          locale: "ko",
          messageFormatType: "FIXED",
          externalMessage: "샘플 메시지",
          internalMessage: "샘플 내부 메시지",
          parameterCount: 0,
          parameterSample: "[]",
          description: "ADM 샘플",
          useYn: "Y",
          requestUser: "admin-ui",
          reason: "메시지 변경"
        },
        codeForm: {
          codeId: null,
          parentId: null,
          codeKey: "ADM_SAMPLE",
          codeValue: "SAMPLE",
          description: "ADM 샘플 코드",
          useYn: "Y",
          requestUser: "admin-ui",
          reason: "코드 변경"
        },
        configForm: {
          configId: null,
          configKey: "CPF.ADM.SAMPLE",
          configValue: "Y",
          configType: "BOOLEAN",
          description: "ADM 샘플 설정",
          encryptedYn: "N",
          useYn: "Y",
          requestUser: "admin-ui",
          reason: "설정 변경"
        },
        permissionForm: { roleId: "ADM_VIEWER", menuId: "LOG_LIST", buttonId: "LOG_LIST_READ", readYn: "Y", writeYn: "N", deleteYn: "N", reason: "권한 변경" },
        passwordForm: { operatorId: "", newPassword: "", forceChange: true, sessionId: "", reason: "비밀번호 운영" },
        securityForm: { ipPattern: "127.0.0.1", description: "로컬 개발", operatorId: "admin", secretRef: "ENV:ADM_ADMIN_OTP_SECRET", otpCode: "", reason: "보안 운영" },
        responseCodeForm: {
          responseCode: "EXYZ010001",
          messageCode: "MXYZ090001",
          resultType: "E",
          moduleId: "XYZ",
          responseGroup: "01",
          sequenceNo: "0001",
          httpStatus: 400,
          description: "XYZ 샘플 응답코드",
          useYn: "Y",
          requestUser: "admin-ui"
        },
        logs: [],
        auditLogs: [],
        logDetail: {},
        memberResult: { items: [] },
        memberDetail: {},
        auditResult: {},
        batchResult: {},
        notificationResult: {},
        downloadResult: {},
        cacheResult: {},
        responseCodeResult: {},
        logLevelResult: {},
        operatorResult: {},
        messageResult: {},
        codeResult: {},
        configResult: {},
        permissionResult: {},
        passwordResult: {},
        securityResult: {}
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
      },
      activeLogDetailPayload() {
        const detail = this.logDetail?.item || this.logDetail || {};
        const tabMap = {
          요약: detail.summary || detail,
          헤더: detail.headers || detail.HEADERS || {},
          요청: detail.request || detail.REQUEST_BODY || {},
          응답: detail.response || detail.RESPONSE || {},
          오류: detail.error || detail.ERROR_MESSAGE || {},
          상세: detail.formattedDetails || detail.details || [],
          전문: this.fixedLengthDetails(detail)
        };
        return this.pretty(tabMap[this.logDetailTab] || {});
      }
    },
    watch: {
      logs() {
        this.logPage.page = 1;
      }
    },
    mounted() {
      if (this.authenticated) {
        this.loadInitialData();
      }
    },
    methods: {
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
          this.setMessage("감사 사유는 필수입니다.");
          return false;
        }
        return true;
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
          this.clearToken("세션이 만료되었습니다. 다시 로그인하세요.");
        } else if (response.status === 403) {
          this.setMessage(data.message || "해당 작업 권한이 없습니다.");
        } else if (!response.ok) {
          this.setMessage(data.message || `요청 실패: status=${response.status}`);
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
          this.authMessage = "운영자 ID와 비밀번호를 입력하세요.";
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
        this.setMessage("로그인되었습니다.");
        this.loadInitialData();
      },
      async loadInitialData() {
        await this.loadMe();
        await Promise.allSettled([
          this.searchLogs(),
          this.loadAuditLogs(),
          this.searchMembers(),
          this.loadBatch(),
          this.loadNotifications(),
          this.loadDownloadPolicies(),
          this.loadOperators(),
          this.loadResponseCodes(),
          this.loadLogLevelRules(),
          this.loadMessages(),
          this.loadCodes(),
          this.loadConfigs(),
          this.loadPermissions(),
          this.loadSecurity()
        ]);
      },
      async loadMe() {
        const data = await this.getJson("/adm/api/auth/me") || {};
        this.currentOperator = data.operatorId ? data : {};
        this.authorizedMenus = data.menus || [];
      },
      async logout() {
        await this.sendJson("/adm/api/auth/logout", "POST");
        this.clearToken("로그아웃되었습니다.");
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
            params.set(key, value);
          }
        });
        return params;
      },
      sortLogs(key) {
        this.logSort = this.logSort.key === key
          ? { key, direction: this.logSort.direction === "asc" ? "desc" : "asc" }
          : { key, direction: "asc" };
      },
      moveLogPage(delta) {
        this.logPage.page = Math.min(this.logTotalPages, Math.max(1, this.logPage.page + delta));
      },
      fixedLengthDetails(detail) {
        const formatted = detail?.formattedDetails || [];
        return formatted.filter(item => {
          const key = String(item.detailKey || item.DETAIL_KEY || "").toLowerCase();
          return key.includes("fixed") || key.includes("telegram") || key.includes("전문");
        });
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
      async searchMembers() {
        const params = this.buildParams(this.memberSearch);
        const data = await this.getJson(`/adm/api/members?${params.toString()}`);
        this.memberResult = { items: Array.isArray(data) ? data : data.items || [] };
      },
      async loadMemberDetail(memberId) {
        if (!memberId) return;
        this.memberDetail = await this.getJson(`/adm/api/members/${memberId}`);
        const member = this.memberDetail.member || {};
        this.memberForm.memberId = member.id || member.ID || memberId;
        this.memberForm.memberNo = member.member_no || member.MEMBER_NO || "";
        this.memberForm.customerNo = member.customer_no || member.CUSTOMER_NO || "";
        this.memberForm.loginId = member.login_id || member.LOGIN_ID || "";
        this.memberForm.name = member.name || member.NAME || "";
        this.memberForm.email = member.email || member.EMAIL || "";
        this.memberForm.mobileNo = member.mobile_no || member.MOBILE_NO || "";
        this.memberStatusForm.memberStatus = member.member_status || member.MEMBER_STATUS || "ACTIVE";
        this.memberStatusForm.lockYn = member.lock_yn || member.LOCK_YN || "N";
        this.memberStatusForm.withdrawYn = member.withdraw_yn || member.WITHDRAW_YN || "N";
      },
      memberPayload() {
        return {
          memberNo: this.memberForm.memberNo,
          customerNo: this.memberForm.customerNo,
          loginId: this.memberForm.loginId,
          name: this.memberForm.name,
          email: this.memberForm.email,
          mobileNo: this.memberForm.mobileNo,
          memberStatus: this.memberForm.memberStatus,
          channelCode: this.memberForm.channelCode,
          description: this.memberForm.description,
          requestUser: "admin-ui",
          reason: this.memberForm.reason
        };
      },
      async createMember() {
        if (!this.memberForm.loginId || !this.memberForm.name || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson("/adm/api/members", "POST", this.memberPayload());
        await this.searchMembers();
        this.setMessage("회원 등록을 완료했습니다.");
      },
      async updateMember() {
        if (!this.memberForm.memberId || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}`, "PUT", this.memberPayload());
        await this.searchMembers();
        this.setMessage("회원 수정을 완료했습니다.");
      },
      async updateMemberStatus() {
        if (!this.memberForm.memberId || !this.requireReason(this.memberForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/status`, "PUT", {
          ...this.memberStatusForm,
          requestUser: "admin-ui",
          reason: this.memberForm.reason
        });
        await this.searchMembers();
        this.setMessage("회원 상태를 변경했습니다.");
      },
      async grantMemberRole() {
        if (!this.memberForm.memberId || !this.memberRoleForm.roleCode || !this.requireReason(this.memberRoleForm.reason)) return;
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/roles`, "POST", this.memberRoleForm);
        this.setMessage("회원 권한을 부여했습니다.");
      },
      async revokeMemberRole() {
        if (!this.memberForm.memberId || !this.memberRoleForm.roleCode || !this.requireReason(this.memberRoleForm.reason)) return;
        const params = this.buildParams({
          serviceCode: this.memberRoleForm.serviceCode,
          reason: this.memberRoleForm.reason,
          requestUser: "admin-ui"
        });
        this.memberDetail = await this.sendJson(`/adm/api/members/${this.memberForm.memberId}/roles/${this.memberRoleForm.roleCode}?${params.toString()}`, "DELETE");
        this.setMessage("회원 권한을 회수했습니다.");
      },
      async loadBatch() {
        const [jobs, executions, schedules, instances, relations, targets, calendar] = await Promise.all([
          this.getJson("/adm/api/batch/jobs"),
          this.getJson("/adm/api/batch/executions?limit=50"),
          this.getJson("/adm/api/batch/schedules"),
          this.getJson("/adm/api/batch/instances"),
          this.getJson(`/adm/api/batch/relations?${this.buildParams({ jobId: this.batchForm.jobId }).toString()}`),
          this.getJson(`/adm/api/batch/execution-targets?${this.buildParams({ jobId: this.batchForm.jobId, dispatchStatus: this.batchForm.dispatchStatus, limit: 50 }).toString()}`),
          this.getJson(`/adm/api/batch/calendar?${this.buildParams({ calendarId: this.batchForm.calendarId }).toString()}`)
        ]);
        this.batchResult = { jobs, executions, schedules, instances, relations, targets, calendar };
      },
      async registerBatchJob() {
        if (!this.batchForm.jobId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/jobs", "POST", {
          jobId: this.batchForm.jobId,
          jobName: this.batchForm.jobName,
          jobType: this.batchForm.jobType,
          description: this.batchForm.description,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 Job을 등록했습니다.");
      },
      async runBatchJob() {
        if (!this.batchForm.jobId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/jobs/${this.batchForm.jobId}/run`, "POST", {
          jobParameters: this.batchForm.jobParameters,
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 수동 실행을 요청했습니다.");
      },
      async retryBatchExecution() {
        if (!this.batchForm.executionId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/executions/${this.batchForm.executionId}/retry`, "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 재수행을 요청했습니다.");
      },
      async stopBatchExecution() {
        if (!this.batchForm.executionId || !this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson(`/adm/api/batch/executions/${this.batchForm.executionId}/stop`, "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 중지를 요청했습니다.");
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
      async simulateBatchSchedule() {
        if (!this.batchForm.scheduleId) return;
        const params = this.buildParams({ baseDate: this.batchForm.businessDate, days: this.batchForm.simulationDays || 14 });
        this.batchResult = { simulation: await this.getJson(`/adm/api/batch/schedules/${this.batchForm.scheduleId}/simulation?${params.toString()}`) };
        this.setMessage("배치 수행 시뮬레이션을 조회했습니다.");
      },
      async loadBatchRelations() {
        const params = this.buildParams({ jobId: this.batchForm.jobId });
        this.batchResult = { relations: await this.getJson(`/adm/api/batch/relations?${params.toString()}`) };
      },
      async loadBatchTargets() {
        const params = this.buildParams({ jobId: this.batchForm.jobId, dispatchStatus: this.batchForm.dispatchStatus, limit: 100 });
        this.batchResult = { targets: await this.getJson(`/adm/api/batch/execution-targets?${params.toString()}`) };
      },
      async runBatchSchedulerOnce() {
        if (!this.requireReason(this.batchForm.reason)) return;
        this.batchResult = await this.sendJson("/adm/api/batch/scheduler/run-once", "POST", {
          requestUser: "admin-ui",
          reason: this.batchForm.reason
        });
        this.setMessage("배치 스케줄러 1회 실행을 요청했습니다.");
      },
      async loadNotifications() {
        const [rules, deliveryLogs] = await Promise.all([
          this.getJson("/adm/api/notifications/rules"),
          this.getJson("/adm/api/notifications/delivery-logs?limit=50")
        ]);
        this.notificationResult = { rules, deliveryLogs };
      },
      selectNotificationRule(rule) {
        this.notificationForm.ruleId = rule.ruleId || rule.rule_id;
        this.notificationForm.eventType = rule.eventType || rule.event_type || "";
        this.notificationForm.eventSubType = rule.eventSubType || rule.event_sub_type || "";
        this.notificationForm.channelCode = rule.channelCode || rule.channel_code || "ADM";
        this.notificationForm.templateCode = rule.templateCode || rule.template_code || "";
        this.notificationForm.severity = rule.severity || "INFO";
        this.notificationForm.receiverGroup = rule.receiverGroup || rule.receiver_group || "";
        this.notificationForm.useYn = rule.useYn || rule.use_yn || "Y";
      },
      notificationPayload() {
        return {
          eventType: this.notificationForm.eventType,
          eventSubType: this.notificationForm.eventSubType,
          channelCode: this.notificationForm.channelCode,
          templateCode: this.notificationForm.templateCode,
          severity: this.notificationForm.severity,
          receiverGroup: this.notificationForm.receiverGroup,
          useYn: this.notificationForm.useYn,
          reason: this.notificationForm.reason,
          requestUser: "admin-ui"
        };
      },
      async saveNotificationRule() {
        if (!this.notificationForm.eventType || !this.requireReason(this.notificationForm.reason)) return;
        const method = this.notificationForm.ruleId ? "PUT" : "POST";
        const url = this.notificationForm.ruleId
          ? `/adm/api/notifications/rules/${this.notificationForm.ruleId}`
          : "/adm/api/notifications/rules";
        this.notificationResult = await this.sendJson(url, method, this.notificationPayload());
        await this.loadNotifications();
        this.setMessage("알림 규칙을 저장했습니다.");
      },
      async disableNotificationRule() {
        if (!this.notificationForm.ruleId || !this.requireReason(this.notificationForm.reason)) return;
        this.notificationResult = await this.sendJson(`/adm/api/notifications/rules/${this.notificationForm.ruleId}/disable`, "PUT", {
          reason: this.notificationForm.reason,
          requestUser: "admin-ui"
        });
        await this.loadNotifications();
        this.setMessage("알림 규칙을 비활성화했습니다.");
      },
      async sendNotificationTest() {
        if (!this.notificationForm.ruleId || !this.requireReason(this.notificationForm.reason)) return;
        this.notificationResult = await this.sendJson(`/adm/api/notifications/rules/${this.notificationForm.ruleId}/test-send`, "POST", {
          targetType: this.notificationForm.targetType,
          targetId: this.notificationForm.targetId,
          receiver: this.notificationForm.receiver,
          message: this.notificationForm.message,
          reason: this.notificationForm.reason,
          requestUser: "admin-ui"
        });
        await this.loadNotifications();
        this.setMessage("알림 테스트 발송을 요청했습니다.");
      },
      async loadDownloadPolicies() {
        const [policies, auditLogs] = await Promise.all([
          this.getJson("/adm/api/downloads/policies"),
          this.getJson("/adm/api/downloads/audit-logs?limit=50")
        ]);
        this.downloadResult = { policies, auditLogs };
      },
      async refreshCache(target) {
        if (!this.requireReason(this.cacheReason)) return;
        const params = this.buildParams({ target, reason: this.cacheReason, requestUser: "admin-ui" });
        this.cacheResult = await this.sendJson(`/adm/api/cache/refresh?${params.toString()}`, "POST");
        this.setMessage(`${target} 캐시 갱신을 요청했습니다.`);
      },
      async loadMessages() {
        this.messageResult = await this.getJson("/adm/api/messages");
      },
      async createMessage() {
        if (!this.messageForm.messageCode || !this.messageForm.locale || !this.requireReason(this.messageForm.reason)) return;
        this.messageResult = await this.sendJson("/adm/api/messages", "POST", this.messageForm);
        this.setMessage("메시지를 등록했습니다.");
      },
      async updateMessage() {
        if (!this.messageForm.messageId || !this.requireReason(this.messageForm.reason)) return;
        this.messageResult = await this.sendJson(`/adm/api/messages/${this.messageForm.messageId}`, "PUT", this.messageForm);
        this.setMessage("메시지를 수정했습니다.");
      },
      async loadCodes() {
        this.codeResult = await this.getJson("/adm/api/codes");
      },
      async createCode() {
        if (!this.codeForm.codeKey || !this.codeForm.codeValue || !this.requireReason(this.codeForm.reason)) return;
        this.codeResult = await this.sendJson("/adm/api/codes", "POST", this.codeForm);
        this.setMessage("코드를 등록했습니다.");
      },
      async updateCode() {
        if (!this.codeForm.codeId || !this.requireReason(this.codeForm.reason)) return;
        this.codeResult = await this.sendJson(`/adm/api/codes/${this.codeForm.codeId}`, "PUT", this.codeForm);
        this.setMessage("코드를 수정했습니다.");
      },
      async loadConfigs() {
        this.configResult = await this.getJson("/adm/api/configs");
      },
      async createConfig() {
        if (!this.configForm.configKey || !this.configForm.configValue || !this.requireReason(this.configForm.reason)) return;
        this.configResult = await this.sendJson("/adm/api/configs", "POST", this.configForm);
        this.setMessage("설정을 등록했습니다.");
      },
      async updateConfig() {
        if (!this.configForm.configId || !this.requireReason(this.configForm.reason)) return;
        this.configResult = await this.sendJson(`/adm/api/configs/${this.configForm.configId}`, "PUT", this.configForm);
        this.setMessage("설정을 수정했습니다.");
      },
      async loadPermissions() {
        const menuMatrix = await this.getJson("/adm/api/permissions/menu-matrix");
        const buttonMatrix = await this.getJson("/adm/api/permissions/button-matrix");
        this.permissionResult = { menuMatrix, buttonMatrix };
      },
      async updateMenuPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.menuId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/menus/${this.permissionForm.menuId}`, "PUT", {
          readYn: this.permissionForm.readYn,
          writeYn: this.permissionForm.writeYn,
          deleteYn: this.permissionForm.deleteYn,
          requestUser: "admin-ui",
          reason: this.permissionForm.reason
        });
        this.setMessage("메뉴 권한을 저장했습니다.");
      },
      async updateButtonPermission() {
        if (!this.permissionForm.roleId || !this.permissionForm.buttonId || !this.requireReason(this.permissionForm.reason)) return;
        this.permissionResult = await this.sendJson(`/adm/api/permissions/roles/${this.permissionForm.roleId}/buttons/${this.permissionForm.buttonId}`, "PUT", {
          allowYn: this.permissionForm.deleteYn,
          requestUser: "admin-ui",
          reason: this.permissionForm.reason
        });
        this.setMessage("버튼 권한을 저장했습니다.");
      },
      async loadResponseCodes() {
        this.responseCodeResult = await this.getJson("/adm/api/response-codes");
      },
      validateResponseCodeForm() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code)) return "응답코드는 EACC010001 또는 SACC000000 형식이어야 합니다.";
        if (code[0] !== this.responseCodeForm.resultType) return "결과 유형은 응답코드 첫 글자와 같아야 합니다.";
        if (code.substring(1, 4) !== this.responseCodeForm.moduleId) return "모듈 ID는 응답코드 2~4번째 자리와 같아야 합니다.";
        if (code.substring(4, 6) !== this.responseCodeForm.responseGroup) return "응답 그룹은 응답코드 5~6번째 자리와 같아야 합니다.";
        if (code.substring(6, 10) !== this.responseCodeForm.sequenceNo) return "일련번호는 응답코드 7~10번째 자리와 같아야 합니다.";
        if (!/^M[A-Z]{3}[0-9]{6}$/.test(this.responseCodeForm.messageCode || "")) return "메시지코드는 MCMN000001 형식이어야 합니다.";
        if (!this.requireReason(this.responseCodeReason)) return "감사 사유는 필수입니다.";
        return "";
      },
      async createResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        const params = this.buildParams({ reason: this.responseCodeReason });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes?${params.toString()}`, "POST", this.responseCodeForm);
        this.setMessage("응답코드를 등록했습니다.");
      },
      async updateResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        const params = this.buildParams({ reason: this.responseCodeReason });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes/${this.responseCodeForm.responseCode}?${params.toString()}`, "PUT", this.responseCodeForm);
        this.setMessage("응답코드를 수정했습니다.");
      },
      async deleteResponseCode() {
        if (!this.requireReason(this.responseCodeReason)) return;
        const params = this.buildParams({ reason: this.responseCodeReason, requestUser: "admin-ui" });
        this.responseCodeResult = await this.sendJson(`/adm/api/response-codes/${this.responseCodeForm.responseCode}?${params.toString()}`, "DELETE");
        this.setMessage("응답코드 삭제를 요청했습니다.");
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
      async loadOperators() {
        this.operatorResult = await this.getJson("/adm/api/operators");
      },
      async createOperator() {
        if (!this.operatorForm.operatorId || !this.operatorForm.operatorName || !this.operatorForm.password) {
          this.setMessage("운영자 ID, 이름, 초기 비밀번호가 필요합니다.");
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
        this.setMessage("운영자를 등록했습니다.");
      },
      async loadPasswordPolicy() {
        this.passwordResult = await this.getJson("/adm/api/operators/password-policy");
      },
      async resetOperatorPassword() {
        if (!this.passwordForm.operatorId || !this.passwordForm.newPassword || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/password/reset`, "POST", {
          newPassword: this.passwordForm.newPassword,
          forceChange: this.passwordForm.forceChange,
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("비밀번호 초기화를 요청했습니다.");
      },
      async unlockOperator() {
        if (!this.passwordForm.operatorId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/${this.passwordForm.operatorId}/unlock`, "POST", {
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("계정 잠금 해제를 요청했습니다.");
      },
      async loadSessions() {
        const params = this.buildParams({ operatorId: this.passwordForm.operatorId });
        this.passwordResult = await this.getJson(`/adm/api/operators/sessions?${params.toString()}`);
      },
      async revokeSession() {
        if (!this.passwordForm.sessionId || !this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson(`/adm/api/operators/sessions/${this.passwordForm.sessionId}/revoke`, "POST", {
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("세션 강제 종료를 요청했습니다.");
      },
      async cleanupExpiredSessions() {
        if (!this.requireReason(this.passwordForm.reason)) return;
        this.passwordResult = await this.sendJson("/adm/api/operators/sessions/cleanup-expired", "POST", {
          requestUser: "admin-ui",
          reason: this.passwordForm.reason
        });
        this.setMessage("만료 세션 정리를 요청했습니다.");
      },
      async loadSecurity() {
        const ipAllowlist = await this.getJson("/adm/api/security/ip-allowlist");
        const mfa = await this.getJson("/adm/api/security/mfa");
        this.securityResult = { ipAllowlist, mfa };
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
      async registerMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.secretRef || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson(`/adm/api/security/mfa/${this.securityForm.operatorId}/register`, "POST", {
          secretRef: this.securityForm.secretRef,
          requestUser: "admin-ui",
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 등록을 요청했습니다.");
      },
      async verifyMfa() {
        if (!this.securityForm.operatorId || !this.securityForm.otpCode || !this.requireReason(this.securityForm.reason)) return;
        this.securityResult = await this.sendJson(`/adm/api/security/mfa/${this.securityForm.operatorId}/verify`, "POST", {
          otpCode: this.securityForm.otpCode,
          requestUser: "admin-ui",
          reason: this.securityForm.reason
        });
        this.setMessage("MFA 검증을 요청했습니다.");
      }
    }
  }).mount("#app");
}
