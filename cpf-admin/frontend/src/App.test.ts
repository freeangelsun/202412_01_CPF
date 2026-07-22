import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it } from "vitest";
import App from "./App.vue";

describe("ADM 운영 화면", () => {
  beforeEach(() => localStorage.clear());

  it("인증 정보가 없으면 로그인 화면을 표시한다", () => {
    const wrapper = mount(App);

    expect(wrapper.get("h1").text()).toBe("CPF ADM");
    expect(wrapper.text()).toContain("운영자 로그인");
    expect(wrapper.find("input[autocomplete='username']").exists()).toBe(true);
  });
});
