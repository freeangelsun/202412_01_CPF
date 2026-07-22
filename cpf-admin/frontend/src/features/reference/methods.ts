export const referenceMethods: Record<string, any> = {
  setMessage(message) {
        this.uiMessage = message || "";
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
  async refreshCache(target) {
        if (!this.requireReason(this.cacheReason)) return;
        const params = this.buildParams({ target, reason: this.cacheReason, requestUser: "admin-ui" });
        this.cacheResult = await this.sendJson(`/adm/api/cache/refresh?${params.toString()}`, "POST");
        this.setMessage(`${target} 캐시 갱신을 요청했습니다.`);
      },
  async loadCacheSummary() {
        this.cacheResult = await this.getJson("/adm/api/cache/summary");
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
  async loadResponseCodes() {
        this.responseCodeResult = await this.getJson("/adm/api/response-codes");
      },
  validateResponseCodeForm() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code)) return "응답코드는 EXYZ010001 또는 SXYZ000000 형식이어야 합니다.";
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
      }
};
