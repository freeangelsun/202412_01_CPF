import {
  admCacheEvictKey,
  admCacheEvictNamespace,
  admCacheReconcile,
  admCacheRefresh,
  admCacheSummary,
  admCodeCreateCode,
  admCodeDeleteCode,
  admCodeFindCode,
  admCodeFindCodes,
  admCodeUpdateCode,
  admConfigCreateConfig,
  admConfigDeleteConfig,
  admConfigFindConfig,
  admConfigFindConfigs,
  admConfigUpdateConfig,
  admResponseCodeCreate,
  admResponseCodeDelete,
  admResponseCodeFindAll,
  admResponseCodeFindOne,
  admResponseCodeUpdate,
  admMessageCreateMessage,
  admMessageDeleteMessage,
  admMessageFindMessage,
  admMessageFindMessages,
  admMessageUpdateMessage,
  admNotificationCancelDelivery,
  admNotificationDisableRule,
  admNotificationFindDeliveryAttempts,
  admNotificationFindDeliveryLogs,
  admNotificationFindDlq,
  admNotificationFindRule,
  admNotificationFindRules,
  admNotificationRetryDelivery,
  admNotificationSaveRule,
  admNotificationSendTest,
  admNotificationUpdateRule
} from "../../generated/orval/cpf-api";
import type {
  AdmCacheControlRequest,
  AdmCacheEvictKeyRequest,
  AdmCacheEvictNamespaceRequest,
  CommonCodeRequest,
  CommonConfigRequest,
  CommonResponseCodeRequest,
  AdmNotificationRuleRequest,
  CommonMessageRequest,
  AdmNotificationTestSendRequest
} from "../../generated/orval/model";

type CpfResponseLike = {
  headers: { get(name: string): string | null };
  json(): Promise<any>;
  text(): Promise<string>;
  ok: boolean;
  status: number;
};
type NotificationRecord = Record<string, any>;
type NotificationDeliveryAction = "retry" | "cancel";

function generatedData<T>(response: unknown): T {
  if (!response || typeof response !== "object" || !("data" in response)) {
    throw new Error("Generated client response contract mismatch: data envelope is required.");
  }
  return (response as { data: T }).data;
}

export const referenceMethods = {
  setMessage(message: string) {
        this.uiMessage = message || "";
      },
  async parseResponse(response: CpfResponseLike, throwOnError = true) {
        const contentType = response.headers.get("content-type") || "";
        let data;
        try {
          data = contentType.includes("application/json")
            ? await response.json()
            : { message: await response.text() };
        } catch (error) {
          data = { message: `응답 본문을 해석할 수 없습니다. status=${response.status}` };
        }
        if (!response.ok) {
          const message = response.status === 401
            ? "세션이 만료되었습니다. 다시 로그인하세요."
            : response.status === 403
              ? (data?.message || "해당 작업 권한이 없습니다.")
              : (data?.message || `요청 실패: status=${response.status}`);
          if (response.status === 401) this.clearSession(message);
          else this.setMessage(message);
          if (throwOnError) {
            throw Object.assign(new Error(message), {
              name: "CpfApiError",
              status: response.status,
              body: data
            });
          }
        }
        return data;
      },
  async loadNotificationDlq() {
        const deliveryLogs = generatedData<unknown[]>(
          await admNotificationFindDlq({ limit: 100 })
        );
        this.notificationResult = {
          ...this.notificationResult,
          deliveryLogs: Array.isArray(deliveryLogs) ? deliveryLogs : [],
          attempts: []
        };
        this.selectedNotificationDelivery = null;
        this.setMessage("알림 DLQ를 조회했습니다.");
      },
  async loadNotifications() {
        const [rulesResponse, deliveryLogsResponse] = await Promise.all([
          admNotificationFindRules({ limit: 100 }),
          admNotificationFindDeliveryLogs({ limit: 50 })
        ]);
        const rules = generatedData<unknown[]>(rulesResponse);
        const deliveryLogs = generatedData<unknown[]>(deliveryLogsResponse);
        this.notificationResult = {
          ...this.notificationResult,
          rules: Array.isArray(rules) ? rules : [],
          deliveryLogs: Array.isArray(deliveryLogs) ? deliveryLogs : []
        };
        if (this.selectedNotificationDelivery) {
          const current = this.notificationResult.deliveryLogs.find(
            (item: NotificationRecord) => Number(item.deliveryId) === Number(this.selectedNotificationDelivery.deliveryId)
          );
          this.selectedNotificationDelivery = current || null;
          if (current) await this.selectNotificationDelivery(current);
        }
      },
  async loadNotificationRuleDetail() {
        const ruleId = Number(this.notificationForm.ruleId);
        if (!Number.isInteger(ruleId) || ruleId < 1) {
          this.setMessage("상세 조회할 알림 Rule을 선택하세요.");
          return;
        }
        const rule = generatedData<Record<string, unknown>>(
          await admNotificationFindRule(Number(ruleId))
        );
        this.selectNotificationRule(rule);
        this.notificationResult = { ...this.notificationResult, ruleDetail: rule };
        this.setMessage("알림 규칙 상세를 조회했습니다.");
      },
  createNotificationRule() {
        Object.assign(this.notificationForm, {
          ruleId: null,
          eventType: "",
          eventSubType: "",
          channelCode: "ADM",
          templateCode: "",
          severity: "WARN",
          receiverGroup: "ADM_OPERATOR",
          useYn: "Y",
          targetType: "ADM_TEST",
          targetId: "TEST",
          receiver: "ADM_OPERATOR",
          message: "ADM notification test message.",
          reason: "알림 규칙 등록",
        });
        this.notificationResult = { ...this.notificationResult, ruleDetail: null };
        this.setMessage("새 알림 규칙 정보를 입력한 뒤 규칙 저장을 실행하세요.");
      },
  async updateNotificationRule() {
        const ruleId = Number(this.notificationForm.ruleId);
        if (!Number.isInteger(ruleId) || ruleId < 1) {
          this.setMessage("수정할 알림 Rule을 선택하세요.");
          return;
        }
        await this.saveNotificationRule();
      },
  selectNotificationRule(rule: NotificationRecord) {
        this.notificationForm.ruleId = rule.ruleId || rule.rule_id;
        this.notificationForm.eventType = rule.eventType || rule.event_type || "";
        this.notificationForm.eventSubType = rule.eventSubType || rule.event_sub_type || "";
        this.notificationForm.channelCode = rule.channelCode || rule.channel_code || "ADM";
        this.notificationForm.templateCode = rule.templateCode || rule.template_code || "";
        this.notificationForm.severity = rule.severity || "INFO";
        this.notificationForm.receiverGroup = rule.receiverGroup || rule.receiver_group || "";
        this.notificationForm.useYn = rule.useYn || rule.use_yn || "Y";
      },
  async selectNotificationDelivery(delivery: NotificationRecord) {
        this.selectedNotificationDelivery = delivery;
        this.notificationDeliveryForm.deliveryId = delivery.deliveryId;
        this.notificationDeliveryForm.expectedVersion = delivery.version;
        this.notificationDeliveryForm.deliveryStatus = delivery.deliveryStatus || "";
        this.notificationDeliveryForm.operationId = delivery.operationId || "";
        const attempts = generatedData<unknown[]>(
          await admNotificationFindDeliveryAttempts(Number(delivery.deliveryId), { limit: 100 })
        );
        this.notificationResult = {
          ...this.notificationResult,
          attempts: Array.isArray(attempts) ? attempts : []
        };
      },
  notificationDeliveryActionAllowed(action: NotificationDeliveryAction) {
        const status = this.notificationDeliveryForm.deliveryStatus;
        if (action === "retry") return ["DLQ", "FAILED", "UNKNOWN_RESULT", "CANCELLED"].includes(status);
        if (action === "cancel") return ["READY", "RETRY", "UNKNOWN_RESULT", "DLQ"].includes(status);
        return false;
      },
  async retryNotificationDelivery() {
        if (!this.notificationDeliveryForm.deliveryId
            || this.notificationDeliveryForm.expectedVersion === null
            || !this.notificationDeliveryActionAllowed("retry")
            || !this.requireReason(this.notificationDeliveryForm.reason)) return;
        const action = generatedData<Record<string, unknown>>(
          await admNotificationRetryDelivery(
            Number(this.notificationDeliveryForm.deliveryId),
            {
              expectedVersion: Number(this.notificationDeliveryForm.expectedVersion),
              reason: this.notificationDeliveryForm.reason
            }
          )
        );
        this.notificationResult = { ...this.notificationResult, action };
        await this.loadNotifications();
        this.setMessage("알림 발송 재시도를 요청했습니다.");
      },
  async cancelNotificationDelivery() {
        if (!this.notificationDeliveryForm.deliveryId
            || this.notificationDeliveryForm.expectedVersion === null
            || !this.notificationDeliveryActionAllowed("cancel")
            || !this.requireReason(this.notificationDeliveryForm.reason)) return;
        const action = generatedData<Record<string, unknown>>(
          await admNotificationCancelDelivery(
            Number(this.notificationDeliveryForm.deliveryId),
            {
              expectedVersion: Number(this.notificationDeliveryForm.expectedVersion),
              reason: this.notificationDeliveryForm.reason
            }
          )
        );
        this.notificationResult = { ...this.notificationResult, action };
        await this.loadNotifications();
        this.setMessage("알림 발송을 취소했습니다.");
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
        };
      },
  async saveNotificationRule() {
        if (!this.notificationForm.eventType || !this.requireReason(this.notificationForm.reason)) return;
        const payload = this.notificationPayload() as AdmNotificationRuleRequest;
        const response = this.notificationForm.ruleId
          ? await admNotificationUpdateRule(Number(this.notificationForm.ruleId), payload)
          : await admNotificationSaveRule(payload);
        this.notificationResult = generatedData<Record<string, unknown>>(response);
        await this.loadNotifications();
        this.setMessage("알림 규칙을 저장했습니다.");
      },
  async disableNotificationRule() {
        if (!this.notificationForm.ruleId || !this.requireReason(this.notificationForm.reason)) return;
        const action = generatedData<Record<string, unknown>>(
          await admNotificationDisableRule(
            Number(this.notificationForm.ruleId),
            { reason: this.notificationForm.reason }
          )
        );
        this.notificationResult = { ...this.notificationResult, action };
        await this.loadNotifications();
        this.setMessage("알림 규칙을 비활성화했습니다.");
      },
  async sendNotificationTest() {
        if (!this.notificationForm.ruleId || !this.requireReason(this.notificationForm.reason)) return;
        const payload: AdmNotificationTestSendRequest = {
          targetType: this.notificationForm.targetType,
          targetId: this.notificationForm.targetId,
          receiver: this.notificationForm.receiver,
          message: this.notificationForm.message,
          reason: this.notificationForm.reason
        };
        this.notificationResult = generatedData<Record<string, unknown>>(
          await admNotificationSendTest(Number(this.notificationForm.ruleId), payload)
        );
        await this.loadNotifications();
        this.setMessage("알림 테스트 발송을 요청했습니다.");
      },
  async refreshCache(target: string) {
        const allowedTargets = ["ALL", "CODE", "MESSAGE", "RESPONSE_CODE", "CONFIG"];
        if (!allowedTargets.includes(target) || !this.requireReason(this.cacheReason)) return;
        this.cacheResult = generatedData<Record<string, unknown>>(
          await admCacheRefresh({ target: target as "ALL" | "CODE" | "MESSAGE" | "RESPONSE_CODE" | "CONFIG", reason: this.cacheReason })
        );
        this.setMessage(`${target} 캐시 갱신을 요청했습니다.`);
      },
  async loadCacheSummary() {
        this.cacheResult = generatedData<Record<string, unknown>>(await admCacheSummary());
      },
  cacheVersionValid() {
        const version = Number(this.cacheControl.version);
        if (!Number.isInteger(version) || version < 0) {
          this.setMessage("캐시 버전은 0 이상의 정수여야 합니다.");
          return false;
        }
        return true;
      },
  async evictCacheKey() {
        if (!this.cacheControl.namespace
            || !this.cacheControl.key
            || !this.cacheVersionValid()
            || !this.requireReason(this.cacheReason)) return;
        const payload: AdmCacheEvictKeyRequest = {
          tenantId: this.cacheControl.tenantId || undefined,
          namespace: this.cacheControl.namespace,
          key: this.cacheControl.key,
          version: Number(this.cacheControl.version),
          reason: this.cacheReason
        };
        this.cacheResult = generatedData<Record<string, unknown>>(await admCacheEvictKey(payload));
        await this.loadCacheSummary();
      },
  async evictCacheNamespace() {
        if (!this.cacheControl.namespace
            || !this.cacheVersionValid()
            || !this.requireReason(this.cacheReason)) return;
        const payload: AdmCacheEvictNamespaceRequest = {
          tenantId: this.cacheControl.tenantId || undefined,
          namespace: this.cacheControl.namespace,
          version: Number(this.cacheControl.version),
          reason: this.cacheReason
        };
        this.cacheResult = generatedData<Record<string, unknown>>(await admCacheEvictNamespace(payload));
        await this.loadCacheSummary();
      },
  async reconcileCache() {
        if (!this.requireReason(this.cacheReason)) return;
        const payload: AdmCacheControlRequest = { reason: this.cacheReason };
        this.cacheResult = generatedData<Record<string, unknown>>(await admCacheReconcile(payload));
        await this.loadCacheSummary();
      },
  messagePayload() {
        return {
          messageId: this.messageForm.messageId || undefined,
          messageCode: this.messageForm.messageCode || undefined,
          messageKey: this.messageForm.messageKey || undefined,
          locale: this.messageForm.locale,
          messageFormatType: this.messageForm.messageFormatType || "FIXED",
          externalMessage: this.messageForm.externalMessage || undefined,
          internalMessage: this.messageForm.internalMessage || undefined,
          messageValue: this.messageForm.messageValue || undefined,
          parameterCount: Number(this.messageForm.parameterCount || 0),
          parameterSample: this.messageForm.parameterSample || undefined,
          description: this.messageForm.description || undefined,
          useYn: this.messageForm.useYn || "Y",
          reason: this.messageForm.reason
        } satisfies CommonMessageRequest;
      },
  async loadMessages() {
        const messages = generatedData<unknown[]>(await admMessageFindMessages());
        this.messageResult = Array.isArray(messages) ? messages : [];
      },
  async loadMessageDetail() {
        const messageId = Number(this.messageForm.messageId);
        if (!Number.isInteger(messageId) || messageId < 1) {
          this.setMessage("상세 조회할 메시지를 선택하세요.");
          return;
        }
        this.messageResult = generatedData<Record<string, unknown>>(
          await admMessageFindMessage(messageId)
        );
        this.setMessage("메시지 상세를 조회했습니다.");
      },
  async createMessage() {
        if (!this.messageForm.messageCode || !this.messageForm.locale || !this.requireReason(this.messageForm.reason)) return;
        this.messageResult = generatedData<Record<string, unknown>>(
          await admMessageCreateMessage(this.messagePayload())
        );
        this.setMessage("메시지를 등록했습니다.");
      },
  async updateMessage() {
        const messageId = Number(this.messageForm.messageId);
        if (!Number.isInteger(messageId) || messageId < 1 || !this.requireReason(this.messageForm.reason)) return;
        this.messageResult = generatedData<Record<string, unknown>>(
          await admMessageUpdateMessage(messageId, this.messagePayload())
        );
        this.setMessage("메시지를 수정했습니다.");
      },
  async deleteMessage() {
        const messageId = Number(this.messageForm.messageId);
        if (!Number.isInteger(messageId) || messageId < 1 || !this.requireReason(this.messageForm.reason)) return;
        const messages = generatedData<unknown[]>(
          await admMessageDeleteMessage(messageId, { reason: this.messageForm.reason })
        );
        this.messageResult = Array.isArray(messages) ? messages : [];
        this.setMessage("메시지를 비활성화했습니다.");
      },
  codePayload() {
        return {
          codeId: this.codeForm.codeId ? Number(this.codeForm.codeId) : undefined,
          parentId: this.codeForm.parentId ? Number(this.codeForm.parentId) : undefined,
          codeKey: this.codeForm.codeKey,
          codeValue: this.codeForm.codeValue,
          description: this.codeForm.description || undefined,
          useYn: this.codeForm.useYn || "Y",
          reason: this.codeForm.reason
        } satisfies CommonCodeRequest;
      },
  async loadCodes() {
        const codes = generatedData<unknown[]>(await admCodeFindCodes());
        this.codeResult = Array.isArray(codes) ? codes : [];
      },
  async loadCodeDetail() {
        const codeId = Number(this.codeForm.codeId);
        if (!Number.isInteger(codeId) || codeId < 1) return this.setMessage("상세 조회할 코드를 선택하세요.");
        this.codeResult = generatedData<Record<string, unknown>>(await admCodeFindCode(codeId));
      },
  async createCode() {
        if (!this.codeForm.codeKey || !this.codeForm.codeValue || !this.requireReason(this.codeForm.reason)) return;
        this.codeResult = generatedData<Record<string, unknown>>(await admCodeCreateCode(this.codePayload()));
        this.setMessage("코드를 등록했습니다.");
      },
  async updateCode() {
        const codeId = Number(this.codeForm.codeId);
        if (!Number.isInteger(codeId) || codeId < 1 || !this.requireReason(this.codeForm.reason)) return;
        this.codeResult = generatedData<Record<string, unknown>>(await admCodeUpdateCode(codeId, this.codePayload()));
        this.setMessage("코드를 수정했습니다.");
      },
  async deleteCode() {
        const codeId = Number(this.codeForm.codeId);
        if (!Number.isInteger(codeId) || codeId < 1 || !this.requireReason(this.codeForm.reason)) return;
        const codes = generatedData<unknown[]>(await admCodeDeleteCode(codeId, { reason: this.codeForm.reason }));
        this.codeResult = Array.isArray(codes) ? codes : [];
        this.setMessage("코드를 비활성화했습니다.");
      },
  configPayload() {
        return {
          configId: this.configForm.configId ? Number(this.configForm.configId) : undefined,
          configKey: this.configForm.configKey,
          configValue: this.configForm.configValue,
          configType: this.configForm.configType || "STRING",
          description: this.configForm.description || undefined,
          encryptedYn: this.configForm.encryptedYn || "N",
          useYn: this.configForm.useYn || "Y",
          reason: this.configForm.reason
        } satisfies CommonConfigRequest;
      },
  configValueCanBeSubmitted() {
        if (this.configForm.encryptedYn === "Y" && this.configForm.configValue === "********") {
          this.setMessage("마스킹된 암호화 설정 값은 그대로 저장할 수 없습니다. 새 값을 입력하세요.");
          return false;
        }
        return true;
      },
  async loadConfigs() {
        const configs = generatedData<unknown[]>(await admConfigFindConfigs());
        this.configResult = Array.isArray(configs) ? configs : [];
      },
  async loadConfigDetail() {
        const configId = Number(this.configForm.configId);
        if (!Number.isInteger(configId) || configId < 1) return this.setMessage("상세 조회할 설정을 선택하세요.");
        this.configResult = generatedData<Record<string, unknown>>(await admConfigFindConfig(configId));
      },
  async createConfig() {
        if (!this.configForm.configKey || !this.configForm.configValue || !this.configValueCanBeSubmitted() || !this.requireReason(this.configForm.reason)) return;
        this.configResult = generatedData<Record<string, unknown>>(await admConfigCreateConfig(this.configPayload()));
        this.setMessage("설정을 등록했습니다.");
      },
  async updateConfig() {
        const configId = Number(this.configForm.configId);
        if (!Number.isInteger(configId) || configId < 1 || !this.configValueCanBeSubmitted() || !this.requireReason(this.configForm.reason)) return;
        this.configResult = generatedData<Record<string, unknown>>(await admConfigUpdateConfig(configId, this.configPayload()));
        this.setMessage("설정을 수정했습니다.");
      },
  async deleteConfig() {
        const configId = Number(this.configForm.configId);
        if (!Number.isInteger(configId) || configId < 1 || !this.requireReason(this.configForm.reason)) return;
        const configs = generatedData<unknown[]>(await admConfigDeleteConfig(configId, { reason: this.configForm.reason }));
        this.configResult = Array.isArray(configs) ? configs : [];
        this.setMessage("설정을 비활성화했습니다.");
      },
  responseCodePayload() {
        return {
          responseCode: this.responseCodeForm.responseCode,
          messageCode: this.responseCodeForm.messageCode,
          resultType: this.responseCodeForm.resultType,
          moduleId: this.responseCodeForm.moduleId,
          responseGroup: this.responseCodeForm.responseGroup,
          sequenceNo: this.responseCodeForm.sequenceNo,
          httpStatus: Number(this.responseCodeForm.httpStatus),
          description: this.responseCodeForm.description || undefined,
          useYn: this.responseCodeForm.useYn || "Y"
        } satisfies CommonResponseCodeRequest;
      },
  async loadResponseCodes() {
        this.responseCodeResult = generatedData<Record<string, unknown>>(await admResponseCodeFindAll());
      },
  async loadResponseCodeDetail() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code)) return this.setMessage("상세 조회할 응답코드를 선택하세요.");
        this.responseCodeResult = generatedData<Record<string, unknown>>(await admResponseCodeFindOne(code));
      },
  validateResponseCodeForm() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code)) return "응답코드는 EREF010001 또는 SREF000000 형식이어야 합니다.";
        if (code[0] !== this.responseCodeForm.resultType) return "결과 유형은 응답코드 첫 글자와 같아야 합니다.";
        if (code.substring(1, 4) !== this.responseCodeForm.moduleId) return "모듈 ID는 응답코드 2~4번째 자리와 같아야 합니다.";
        if (code.substring(4, 6) !== this.responseCodeForm.responseGroup) return "응답 그룹은 응답코드 5~6번째 자리와 같아야 합니다.";
        if (code.substring(6, 10) !== this.responseCodeForm.sequenceNo) return "일련번호는 응답코드 7~10번째 자리와 같아야 합니다.";
        if (!/^M[A-Z]{3}[0-9]{6}$/.test(this.responseCodeForm.messageCode || "")) return "메시지코드는 MCMN000001 형식이어야 합니다.";
        const status = Number(this.responseCodeForm.httpStatus);
        if (!Number.isInteger(status) || status < 100 || status > 599) return "HTTP 상태 코드는 100~599 정수여야 합니다.";
        if (!this.requireReason(this.responseCodeReason)) return "감사 사유는 필수입니다.";
        return "";
      },
  async createResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        this.responseCodeResult = generatedData<Record<string, unknown>>(
          await admResponseCodeCreate({ reason: this.responseCodeReason }, this.responseCodePayload())
        );
        this.setMessage("응답코드를 등록했습니다.");
      },
  async updateResponseCode() {
        const error = this.validateResponseCodeForm();
        if (error) return this.setMessage(error);
        this.responseCodeResult = generatedData<Record<string, unknown>>(
          await admResponseCodeUpdate(this.responseCodeForm.responseCode, { reason: this.responseCodeReason }, this.responseCodePayload())
        );
        this.setMessage("응답코드를 수정했습니다.");
      },
  async deleteResponseCode() {
        const code = this.responseCodeForm.responseCode || "";
        if (!/^[SE][A-Z]{3}[0-9]{6}$/.test(code) || !this.requireReason(this.responseCodeReason)) return;
        this.responseCodeResult = generatedData<Record<string, unknown>>(
          await admResponseCodeDelete(code, { reason: this.responseCodeReason })
        );
        this.setMessage("응답코드 삭제를 요청했습니다.");
      }
} satisfies Record<string, any>;
