import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import ResiliencePoliciesPage from "./ResiliencePoliciesPage.vue";
describe("ResiliencePoliciesPage",()=>{it("exposes two-person approval controls",()=>{const wrapper=mount(ResiliencePoliciesPage);expect(wrapper.text()).toContain("2인 승인");expect(wrapper.text()).toContain("승인 요청");});});
