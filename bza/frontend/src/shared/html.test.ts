import { describe, expect, it } from "vitest";
import { displayValue, escapeHtml } from "./html";

describe("BZA 안전 출력", () => {
  it("HTML 특수문자를 이스케이프한다", () => {
    expect(escapeHtml("<script>alert('x')</script>"))
      .toBe("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;");
  });

  it("빈 값을 운영 화면 기본 표기로 변환한다", () => {
    expect(displayValue(null)).toBe("-");
  });
});
