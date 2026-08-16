import { describe, expect, it } from "vitest";
import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

describe("ADM 거래 식별자", () => {
  it("Core와 동일한 34자리 transactionId 규격을 생성한다", () => {
    const transactionId = createTransactionId();
    expect(transactionId).toMatch(/^\d{17}ADMadmUI01\d{7}$/);
    expect(isValidTransactionId(transactionId)).toBe(true);
  });

  it("잘못된 transactionId는 거부한다", () => {
    expect(isValidTransactionId("OADM-AA-0001")).toBe(false);
    expect(isValidTransactionId("too-short")).toBe(false);
  });

  it("기본 호출자 헤더를 변경 불가능하게 제공한다", () => {
    expect(defaultHeaders["X-Caller-Service"]).toBe("adm-ui");
    expect(Object.isFrozen(defaultHeaders)).toBe(true);
  });
});
