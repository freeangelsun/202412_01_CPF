import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import FeatureFlagsPage from "./FeatureFlagsPage.vue";
describe("FeatureFlagsPage",()=>{it("does not render raw override value in result table",()=>{const wrapper=mount(FeatureFlagsPage);expect(wrapper.text()).not.toContain("secret-value");expect(wrapper.text()).toContain("Feature Flag 운영");});});
