import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CpfStructuredData from "./CpfStructuredData.vue";

describe("CpfStructuredData", () => {
  it("renders nested operational data without exposing sensitive fields", () => {
    const wrapper = mount(CpfStructuredData, {
      props: {
        value: {
          serviceId: "cpf-gateway",
          status: "UP",
          apiToken: "must-not-render",
          target: { instanceId: "gateway-1", healthy: true }
        }
      }
    });

    expect(wrapper.text()).toContain("cpf-gateway");
    expect(wrapper.text()).toContain("gateway-1");
    expect(wrapper.text()).toContain("***");
    expect(wrapper.text()).not.toContain("must-not-render");
  });

  it("parses JSON object strings and bounds large collections", () => {
    const wrapper = mount(CpfStructuredData, {
      props: { value: JSON.stringify([{ id: 1 }, { id: 2 }, { id: 3 }]), maxItems: 2 }
    });

    expect(wrapper.text()).toContain("1개 항목은 안전한 화면 한도를 초과해 생략했습니다.");
    expect(wrapper.text()).toContain("1");
    expect(wrapper.text()).toContain("2");
    expect(wrapper.text()).not.toContain("3");
  });
});
