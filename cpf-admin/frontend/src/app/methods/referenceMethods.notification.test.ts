import { describe, expect, it, vi } from "vitest";

import { referenceMethods } from "./referenceMethods";

describe("notification route actions", () => {
  it("loads a selected rule detail and projects it into the form", async () => {
    const rule = { ruleId: 17, eventType: "BATCH", channelCode: "ADM", severity: "WARN", useYn: "Y" };
    const context: any = {
      notificationForm: { ruleId: 17 },
      notificationResult: { rules: [] },
      getJson: vi.fn().mockResolvedValue(rule),
      selectNotificationRule: vi.fn(),
      setMessage: vi.fn(),
    };

    await referenceMethods.loadNotificationRuleDetail.call(context);

    expect(context.getJson).toHaveBeenCalledWith("/adm/api/notifications/rules/17");
    expect(context.selectNotificationRule).toHaveBeenCalledWith(rule);
    expect(context.notificationResult.ruleDetail).toEqual(rule);
  });

  it("initializes a safe typed create form without issuing a mutation", () => {
    const context: any = {
      notificationForm: { ruleId: 9, eventType: "OLD", channelCode: "SMS" },
      notificationResult: { ruleDetail: { ruleId: 9 } },
      setMessage: vi.fn(),
    };

    referenceMethods.createNotificationRule.call(context);

    expect(context.notificationForm).toMatchObject({
      ruleId: null,
      eventType: "",
      channelCode: "ADM",
      severity: "WARN",
      useYn: "Y",
      reason: "알림 규칙 등록",
    });
    expect(context.notificationResult.ruleDetail).toBeNull();
  });

  it("updates only an existing selected rule through the canonical save action", async () => {
    const context: any = {
      notificationForm: { ruleId: 23 },
      saveNotificationRule: vi.fn().mockResolvedValue(undefined),
      setMessage: vi.fn(),
    };

    await referenceMethods.updateNotificationRule.call(context);

    expect(context.saveNotificationRule).toHaveBeenCalledOnce();
  });

  it("does not update when no rule is selected", async () => {
    const context: any = {
      notificationForm: { ruleId: null },
      saveNotificationRule: vi.fn(),
      setMessage: vi.fn(),
    };

    await referenceMethods.updateNotificationRule.call(context);

    expect(context.saveNotificationRule).not.toHaveBeenCalled();
    expect(context.setMessage).toHaveBeenCalledWith("수정할 알림 Rule을 선택하세요.");
  });
});
