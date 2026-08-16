import {
  admCacheEvictKey,
  admCacheEvictNamespace,
  admCacheReconcile,
  admCacheRefresh,
  admCacheSummary,
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
  admNotificationUpdateRule,
  getAdmMessageDeleteMessageMutationOptions,
  getAdmNotificationRetryDeliveryMutationOptions
} from "../../../src/generated/orval/cpf-api";
import type {
  AdmCacheReconcileRequest,
  AdmCacheEvictKeyRequest,
  AdmCacheEvictNamespaceRequest,
  AdmNotificationRuleRequest,
  AdmNotificationTestSendRequest,
  CommonMessageRequest
} from "../../../src/generated/orval/model";

const cacheKey: AdmCacheEvictKeyRequest = { namespace: "users", key: "42", version: 3, reason: "audited reason" };
const cacheNamespace: AdmCacheEvictNamespaceRequest = { namespace: "users", version: 3, reason: "audited reason" };
const cacheControl: AdmCacheReconcileRequest = { reason: "audited reason" };
void admCacheSummary();
void admCacheRefresh({ target: "MESSAGE", reason: "audited reason" });
void admCacheEvictKey(cacheKey);
void admCacheEvictNamespace(cacheNamespace);
void admCacheReconcile(cacheControl);

const rule: AdmNotificationRuleRequest = {
  eventType: "BATCH",
  channelCode: "ADM",
  severity: "WARN",
  useYn: "Y",
  reason: "audited reason"
};
const testSend: AdmNotificationTestSendRequest = {
  targetType: "ADM_TEST",
  targetId: "TEST",
  receiver: "ADM_OPERATOR",
  message: "message",
  reason: "audited reason"
};

void admNotificationFindRules({ limit: 100 });
void admNotificationFindRule("1");
void admNotificationSaveRule(rule);
void admNotificationUpdateRule("1", rule);
void admNotificationDisableRule("1", { reason: "disable reason" });
void admNotificationFindDeliveryLogs({ limit: 50 });
void admNotificationFindDlq({ limit: 100 });
void admNotificationFindDeliveryAttempts("10", { limit: 100 });
void admNotificationSendTest("1", testSend);
void admNotificationRetryDelivery("10", { expectedVersion: 3, reason: "retry reason" });
void admNotificationCancelDelivery("10", { expectedVersion: 3, reason: "cancel reason" });
const message: CommonMessageRequest = {
  messageCode: "MCPF900001",
  locale: "ko-KR",
  externalMessage: "message",
  reason: "audited reason"
};
void admMessageFindMessages();
void admMessageFindMessage(1);
void admMessageCreateMessage(message);
void admMessageUpdateMessage(1, message);
void admMessageDeleteMessage(1, { reason: "disable reason" });
getAdmMessageDeleteMessageMutationOptions().mutationFn?.({ messageId: 1, params: { reason: "disable reason" } });
getAdmNotificationRetryDeliveryMutationOptions().mutationFn?.({
  deliveryId: "10",
  params: { expectedVersion: 3, reason: "retry reason" }
});

// @ts-expect-error body is mandatory
void admNotificationSaveRule();
// @ts-expect-error actor identity is server-derived
const spoofedRule: AdmNotificationRuleRequest = { ...rule, requestUser: "spoof" };
void spoofedRule;
// @ts-expect-error expectedVersion is numeric
void admNotificationRetryDelivery("10", { expectedVersion: "3", reason: "retry" });
// @ts-expect-error reason is mandatory for dangerous action
void admNotificationDisableRule("1", {});
// @ts-expect-error message body is mandatory
void admMessageCreateMessage();
// @ts-expect-error actor identity is server-derived
const spoofedMessage: CommonMessageRequest = { ...message, requestUser: "spoof" };
void spoofedMessage;
// @ts-expect-error audited reason query is mandatory
void admMessageDeleteMessage(1, {});
// @ts-expect-error generated request options cannot inject a hidden DELETE body
void admMessageDeleteMessage(1, { reason: "disable" }, { data: { reason: "wrong body" } });

// @ts-expect-error refresh audit reason is mandatory
void admCacheRefresh({ target: "ALL" });
// @ts-expect-error unsupported refresh target
void admCacheRefresh({ target: "UNKNOWN", reason: "audited" });
// @ts-expect-error cache key eviction body is mandatory
void admCacheEvictKey();
// @ts-expect-error actor identity is server-derived
const spoofedCache: AdmCacheEvictKeyRequest = { ...cacheKey, requestUser: "spoof" };
void spoofedCache;
