import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { admMutation, admRawResponse, createAdmHeaders } from "./cpfApi";
import { createTransactionId, isValidTransactionId } from "./transaction";

describe("ADM BFF 공통 API client", () => {
  beforeEach(() => { document.cookie = "XSRF-TOKEN=; Max-Age=0; Path=/"; });
  afterEach(() => { vi.unstubAllGlobals(); document.cookie = "XSRF-TOKEN=; Max-Age=0; Path=/"; });

  it("Options API와 Composition API가 동일한 표준 Header 계약을 사용한다", () => {
    const headers = createAdmHeaders({ "Content-Type": "application/json", "X-Transaction-Id": "legacy-invalid-id" });
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

  it("Browser Bearer Token과 cross-origin 호출을 fail-closed로 거부한다", async () => {
    expect(() => createAdmHeaders({ Authorization: "Bearer forbidden" })).toThrow("Browser Bearer Token");
    await expect(admRawResponse("https://evil.example/adm/api/runtime-control/status")).rejects.toThrow("same-origin");
  });

  it("JSON object와 JSON string의 중첩 actor alias를 차단한다", async () => {
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", { nested: { operatorId: "browser" } })).rejects.toThrow("operatorId");
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", '{"nested":{"requestedBy":"browser"}}')).rejects.toThrow("requestedBy");
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", "plain text")).rejects.toThrow("raw string body is forbidden");
  });

  it("FormData와 URLSearchParams actor alias를 대소문자와 관계없이 차단한다", async () => {
    const form = new FormData(); form.set("OperatorId", "browser");
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", form)).rejects.toThrow("OperatorId");
    const params = new URLSearchParams(); params.set("requestedBy", "browser");
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", params)).rejects.toThrow("requestedBy");
  });

  it("일반 문자열 필드는 허용하되 JSON 문자열 내부 actor alias는 차단한다", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response("{}", { status: 200 }));
    vi.stubGlobal("fetch", fetchMock);
    await admRawResponse("/adm/api/runtime-control/status", "POST", { reason: "normal operation", nested: { description: "safe" } });
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", { payload: '{"operatorId":"browser"}' })).rejects.toThrow("operatorId");
  });

  it("로그인과 운영자 관리의 최상위 operatorId는 대상 식별자로 허용하고 중첩 actor는 차단한다", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response("{}", { status: 200 }));
    vi.stubGlobal("fetch", fetchMock);
    await admRawResponse("/adm/api/auth/login", "POST", { operatorId: "admin", password: "secret" });
    await expect(admRawResponse("/adm/api/auth/login", "POST", { credentials: { operatorId: "browser" } })).rejects.toThrow("operatorId");
  });

  it("Blob mutation body를 검증 불가능한 privileged payload로 거부한다", async () => {
    await expect(admRawResponse("/adm/api/runtime-control/status", "POST", new Blob(["payload"]))).rejects.toThrow("Blob body is forbidden");
  });

  it("same-origin BFF 호출에 CSRF와 secure-cookie 요청 정책을 적용한다", async () => {
    document.cookie = "XSRF-TOKEN=csrf%20token; Path=/";
    const fetchMock = vi.fn().mockResolvedValue(new Response("{}", { status: 200, headers: { "Content-Type": "application/json" } }));
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
