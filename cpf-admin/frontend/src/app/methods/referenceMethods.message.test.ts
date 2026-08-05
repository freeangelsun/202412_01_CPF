import { beforeEach, describe, expect, it, vi } from "vitest";

const api = vi.hoisted(() => ({
  admCacheEvictKey: vi.fn(),
  admCacheEvictNamespace: vi.fn(),
  admCacheReconcile: vi.fn(),
  admCacheRefresh: vi.fn(),
  admCacheSummary: vi.fn(),
  admMessageCreateMessage: vi.fn(),
  admMessageDeleteMessage: vi.fn(),
  admMessageFindMessage: vi.fn(),
  admMessageFindMessages: vi.fn(),
  admMessageUpdateMessage: vi.fn(),
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

function messageForm() {
  return {
    messageId: null,
    messageCode: "MCPF900001",
    messageKey: "",
    locale: "ko-KR",
    messageFormatType: "FIXED",
    externalMessage: "외부 메시지",
    internalMessage: "내부 메시지",
    messageValue: "",
    parameterCount: 0,
    parameterSample: "",
    description: "설명",
    useYn: "Y",
    requestUser: "browser-spoof",
    reason: "감사 사유"
  };
}

describe("message route generated-client actions", () => {
  beforeEach(() => vi.clearAllMocks());

  it("loads list and detail through generated operations", async () => {
    api.admMessageFindMessages.mockResolvedValue(response([{ messageId: 1 }]));
    api.admMessageFindMessage.mockResolvedValue(response({ messageId: 1 }));
    const context: any = { messageForm: { ...messageForm(), messageId: 1 }, setMessage: vi.fn() };

    await referenceMethods.loadMessages.call(context);
    expect(api.admMessageFindMessages).toHaveBeenCalledWith();
    expect(context.messageResult).toEqual([{ messageId: 1 }]);

    await referenceMethods.loadMessageDetail.call(context);
    expect(api.admMessageFindMessage).toHaveBeenCalledWith(1);
    expect(context.messageResult).toEqual({ messageId: 1 });
  });

  it("creates and updates without browser-supplied operator identity", async () => {
    api.admMessageCreateMessage.mockResolvedValue(response({ messageId: 2 }));
    api.admMessageUpdateMessage.mockResolvedValue(response({ messageId: 3 }));
    const context: any = {
      messageForm: messageForm(),
      messagePayload: referenceMethods.messagePayload,
      requireReason: vi.fn().mockReturnValue(true),
      setMessage: vi.fn()
    };

    await referenceMethods.createMessage.call(context);
    const createBody = api.admMessageCreateMessage.mock.calls[0][0];
    expect(createBody).toMatchObject({ messageCode: "MCPF900001", locale: "ko-KR", reason: "감사 사유" });
    expect(createBody).not.toHaveProperty("requestUser");

    context.messageForm.messageId = 3;
    await referenceMethods.updateMessage.call(context);
    const updateBody = api.admMessageUpdateMessage.mock.calls[0][1];
    expect(api.admMessageUpdateMessage.mock.calls[0][0]).toBe(3);
    expect(updateBody).not.toHaveProperty("requestUser");
  });

  it("disables with an audited query parameter and no request body", async () => {
    api.admMessageDeleteMessage.mockResolvedValue(response([{ messageId: 1, useYn: "N" }]));
    const context: any = {
      messageForm: { ...messageForm(), messageId: 1 },
      requireReason: vi.fn().mockReturnValue(true),
      setMessage: vi.fn()
    };

    await referenceMethods.deleteMessage.call(context);

    expect(api.admMessageDeleteMessage).toHaveBeenCalledWith(1, { reason: "감사 사유" });
    expect(context.messageResult).toEqual([{ messageId: 1, useYn: "N" }]);
  });

  it("fails closed for an invalid identifier or missing reason", async () => {
    const context: any = {
      messageForm: { ...messageForm(), messageId: null, reason: "" },
      requireReason: vi.fn().mockReturnValue(false),
      setMessage: vi.fn()
    };

    await referenceMethods.updateMessage.call(context);
    await referenceMethods.deleteMessage.call(context);
    expect(api.admMessageUpdateMessage).not.toHaveBeenCalled();
    expect(api.admMessageDeleteMessage).not.toHaveBeenCalled();
  });

  it("rejects a response without the generated envelope", async () => {
    api.admMessageFindMessages.mockResolvedValue([{ messageId: 1 }]);
    const context: any = {};
    await expect(referenceMethods.loadMessages.call(context))
      .rejects.toThrow("Generated client response contract mismatch");
  });
});
