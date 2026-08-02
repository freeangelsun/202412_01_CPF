import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { admRawResponse, createAdmHeaders } from "./cpfApi";
import { createTransactionId, isValidTransactionId } from "./transaction";

describe("ADM BFF 공통 API client", () => {
  beforeEach(() => {
    document.cookie = "XSRF-TOKEN=; Max-Age=0; Path=/";
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; Path=/";
  });

  it("Options API와 Composition API가 동일한 표준 Header 계약을 사용한다", () => {
    const headers = createAdmHeaders({
      "Content-Type": "application/json",
      "X-Transaction-Id": "legacy-invalid-id"
    });

    expect(headers.has("Authorization")).toBe(false);
    expect(headers.get("X-Caller-Service")).toBe("adm-ui");
    expect(headers.get("X-Original-Channel-Code")).toBe("ADM");
    expect(isValidTransactionId(headers.get("X-Transaction-Id"))).toBe(true);
  });

  it("유효한 상위 transactionId는 새 값으로 바꾸지 않는다", () => {
    const inheritedTransactionId = createTransactionId();
    const headers = createAdmHeaders({ "X-Transaction-Id": inheritedTransactionId });
    expect(headers.get("X-Transaction-Id")).toBe(inheritedTransactionId);
  });

  it("Browser Bearer Token을 fail-closed로 거부한다", () => {
    expect(() => createAdmHeaders({ Authorization: "Bearer forbidden" })).toThrow(
      "ADM BFF는 Browser Bearer Token을 허용하지 않습니다."
    );
  });

  it("same-origin BFF 호출에 CSRF와 secure-cookie 요청 정책을 적용한다", async () => {
    document.cookie = "XSRF-TOKEN=csrf%20token; Path=/";
    const fetchMock = vi.fn().mockResolvedValue(new Response("{}", {
      status: 200,
      headers: { "Content-Type": "application/json" }
    }));
    vi.stubGlobal("fetch", fetchMock);

    await admRawResponse("/adm/api/runtime-control/status");

    const [target, options] = fetchMock.mock.calls[0] as [string, RequestInit];
    const headers = new Headers(options.headers);
    expect(target).toBe("/adm/api/runtime-control/status");
    expect(headers.get("X-XSRF-TOKEN")).toBe("csrf token");
    expect(headers.has("Authorization")).toBe(false);
    expect(options.credentials).toBe("include");
    expect(options.cache).toBe("no-store");
    expect(options.redirect).toBe("error");
  });
});
