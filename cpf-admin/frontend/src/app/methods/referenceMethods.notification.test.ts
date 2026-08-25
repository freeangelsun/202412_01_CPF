import { beforeEach, describe, expect, it, vi } from "vitest";

const api = vi.hoisted(() => ({
  admCacheEvictKey: vi.fn(),
  admCacheEvictNamespace: vi.fn(),
  admCacheReconcile: vi.fn(),
  admCacheRefresh: vi.fn(),
  admCacheSummary: vi.fn(),
  admNotificationCancelDelivery: vi.fn(),
  admNotificationDisableRule: vi.fn(),
  admNotificationFindDeliveryAttempts: vi.fn(),
  admNotificationFindDeliveryLogs: vi.fn(),
  admNotificationFindDlq: vi.fn(),
  admNotificationFindRule: vi.fn(),
  admNotificationFindRules: vi.fn(),
  admNotificationRetryDelivery: vi.fn(),
  admNotificationSaveRule: vi.fn(),
  admNotificationSendTest: vi.fn(),
  admNotificationUpdateRule: vi.fn()
}));

vi.mock("../../generated/orval/cpf-api", () => api);

import { referenceMethods } from "./referenceMethods";

function response<T>(data: T) {
  return { data, status: 200 as const, headers: new Headers() };
}

describe("notification route generated-client actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("loads a selected rule detail through the generated operation", async () => {
    const rule = { ruleId: 17, eventType: "BATCH", channelCode: "ADM", severity: "WARN", useYn: "Y" };
    api.admNotificationFindRule.mockResolvedValue(response(rule));
    const context: any = {
      notificationForm: { ruleId: 17 },
      notificationResult: { rules: [] },
      selectNotificationRule: vi.fn(),
      setMessage: vi.fn()
    };

    await referenceMethods.loadNotificationRuleDetail.call(context);

    expect(api.admNotificationFindRule).toHaveBeenCalledWith(17);
    expect(context.selectNotificationRule).toHaveBeenCalledWith(rule);
    expect(context.notificationResult.ruleDetail).toEqual(rule);
  });

  it("loads rule and delivery collections with typed query parameters", async () => {
    const rules = [{ ruleId: 1 }];
    const deliveries = [{ deliveryId: 2 }];
    api.admNotificationFindRules.mockResolvedValue(response(rules));
    api.admNotificationFindDeliveryLogs.mockResolvedValue(response(deliveries));
    const context: any = {
      notificationResult: {},
      selectedNotificationDelivery: null
    };

    await referenceMethods.loadNotifications.call(context);

    expect(api.admNotificationFindRules).toHaveBeenCalledWith({ limit: 100 });
    expect(api.admNotificationFindDeliveryLogs).toHaveBeenCalledWith({ limit: 50 });
    expect(context.notificationResult).toMatchObject({ rules, deliveryLogs: deliveries });
  });

  it("creates a rule without client-supplied operator identity", async () => {
    api.admNotificationSaveRule.mockResolvedValue(response({ ruleId: 33 }));
    const context: any = {
      notificationForm: {
        ruleId: null,
        eventType: "BATCH",
        eventSubType: "FAILED",
        channelCode: "ADM",
        templateCode: "BATCH_FAILED",
        severity: "WARN",
        receiverGroup: "ADM_OPERATOR",
        useYn: "Y",
        reason: "신규 규칙 등록"
      },
      notificationPayload: referenceMethods.notificationPayload,
      requireReason: vi.fn().mockReturnValue(true),
      loadNotifications: vi.fn(),
      setMessage: vi.fn()
    };

    await referenceMethods.saveNotificationRule.call(context);

    const request = api.admNotificationSaveRule.mock.calls[0][0];
    expect(request).toMatchObject({ eventType: "BATCH", reason: "신규 규칙 등록" });
    expect(request).not.toHaveProperty("requestUser");
    expect(context.notificationResult).toEqual({ ruleId: 33 });
  });

  it("updates a selected rule through the generated body operation", async () => {
    api.admNotificationUpdateRule.mockResolvedValue(response({ ruleId: 23 }));
    const context: any = {
      notificationForm: {
        ruleId: 23,
        eventType: "ONLINE",
        eventSubType: "",
        channelCode: "ADM",
        templateCode: "ONLINE",
        severity: "INFO",
        receiverGroup: "OPS",
        useYn: "Y",
        reason: "규칙 수정"
      },
      notificationPayload: referenceMethods.notificationPayload,
      requireReason: vi.fn().mockReturnValue(true),
      loadNotifications: vi.fn(),
      setMessage: vi.fn()
    };

    await referenceMethods.saveNotificationRule.call(context);

    expect(api.admNotificationUpdateRule).toHaveBeenCalledWith(
      23,
      expect.objectContaining({ eventType: "ONLINE", reason: "규칙 수정" })
    );
  });

  it("sends optimistic-lock retry parameters without raw URL construction", async () => {
    api.admNotificationRetryDelivery.mockResolvedValue(response({ deliveryId: 41, status: "RETRY" }));
    const context: any = {
      notificationDeliveryForm: {
        deliveryId: 41,
        expectedVersion: 7,
        deliveryStatus: "DLQ",
        reason: "운영 재시도"
      },
      notificationResult: {},
      notificationDeliveryActionAllowed: referenceMethods.notificationDeliveryActionAllowed,
      requireReason: vi.fn().mockReturnValue(true),
      loadNotifications: vi.fn(),
      setMessage: vi.fn()
    };

    await referenceMethods.retryNotificationDelivery.call(context);

    expect(api.admNotificationRetryDelivery).toHaveBeenCalledWith(
      41,
      { expectedVersion: 7, reason: "운영 재시도" }
    );
  });

  it("sends test payload without browser actor fields", async () => {
    api.admNotificationSendTest.mockResolvedValue(response({ deliveryId: 51, status: "READY" }));
    const context: any = {
      notificationForm: {
        ruleId: 9,
        targetType: "ADM_TEST",
        targetId: "TEST",
        receiver: "ADM_OPERATOR",
        message: "test",
        reason: "테스트 발송"
      },
      requireReason: vi.fn().mockReturnValue(true),
      loadNotifications: vi.fn(),
      setMessage: vi.fn()
    };

    await referenceMethods.sendNotificationTest.call(context);

    const request = api.admNotificationSendTest.mock.calls[0][1];
    expect(request).not.toHaveProperty("requestUser");
    expect(api.admNotificationSendTest).toHaveBeenCalledWith(
      9,
      expect.objectContaining({ reason: "테스트 발송" })
    );
  });

  it("fails closed when the generated mutator envelope is absent", async () => {
    api.admNotificationFindRule.mockResolvedValue({ ruleId: 17 });
    const context: any = {
      notificationForm: { ruleId: 17 },
      notificationResult: {},
      selectNotificationRule: vi.fn(),
      setMessage: vi.fn()
    };

    await expect(referenceMethods.loadNotificationRuleDetail.call(context))
      .rejects.toThrow("Generated client response contract mismatch");
  });
});
