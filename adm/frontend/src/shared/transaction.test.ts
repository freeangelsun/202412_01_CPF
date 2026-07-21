import { describe, expect, it } from "vitest";
import { createTransactionGlobalId, defaultHeaders } from "./transaction";

describe("ADM 거래 메타", () => {
  it("34자리 표준 거래 ID를 생성한다", () => {
    expect(createTransactionGlobalId()).toMatch(/^\d{17}ADMadmUI01\d{7}$/);
  });

  it("기본 호출자 헤더를 변경 불가능하게 제공한다", () => {
    expect(defaultHeaders["X-Caller-Service"]).toBe("adm-ui");
    expect(Object.isFrozen(defaultHeaders)).toBe(true);
  });
});
