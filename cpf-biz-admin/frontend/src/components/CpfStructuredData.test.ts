import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import CpfStructuredData from "./CpfStructuredData.vue";

describe("CpfStructuredData", () => {
  it("renders structured business data and masks credentials", () => {
    const wrapper = mount(CpfStructuredData, {
      props: { value: { approvalId: 17, status: "APPROVED", refreshToken: "hidden" } }
    });

    expect(wrapper.text()).toContain("APPROVED");
    expect(wrapper.text()).toContain("***");
    expect(wrapper.text()).not.toContain("hidden");
  });
});
