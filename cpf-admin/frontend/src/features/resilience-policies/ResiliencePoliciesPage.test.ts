import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { createPinia, setActivePinia } from "pinia";
import ResiliencePoliciesPage from "./ResiliencePoliciesPage.vue";
import { useAdmSessionStore } from "../../stores/admSessionStore";

describe("ResiliencePoliciesPage", () => {
  it("exposes two-person approval controls only to permitted operators", () => {
    const pinia = createPinia();
    setActivePinia(pinia);

    // 권한이 없으면 요청/승인/반려 Control이 노출되지 않아야 한다(fail-closed).
    const denied = mount(ResiliencePoliciesPage, { global: { plugins: [pinia] } });
    expect(denied.text()).toContain("2인 승인");
    expect(denied.text()).not.toContain("승인 요청");

    // Button 권한이 부여되면 요청/승인/반려가 각각 독립적으로 노출된다.
    const session = useAdmSessionStore();
    session.buttonIds = [
      "RESILIENCE_POLICY_REQUEST",
      "RESILIENCE_POLICY_APPROVE",
      "RESILIENCE_POLICY_REJECT"
    ];
    const granted = mount(ResiliencePoliciesPage, { global: { plugins: [pinia] } });
    expect(granted.text()).toContain("승인 요청");
    expect(granted.text()).toContain("승인");
    expect(granted.text()).toContain("반려");
  });
});
