import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import App from "./App.vue";

describe("BZA 업무 운영 화면", () => {
  beforeEach(() => sessionStorage.clear());

  it("세션이 없으면 로그인 화면과 접근 가능한 폼을 표시한다", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: "/", name: "dashboard", component: { template: "<section />" } }]
    });
    await router.push("/");
    await router.isReady();
    const wrapper = mount(App, { attachTo: document.body, global: { plugins: [router] } });
    expect(wrapper.text()).toContain("BZA Backoffice");
    expect(wrapper.find("#loginForm").exists()).toBe(true);
    expect(wrapper.find("#appView").attributes("hidden")).toBeDefined();
    expect(wrapper.text()).toContain("Core Platform Framework");
    wrapper.unmount();
  });
});
