import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  ADM_ACCESS_TOKEN_STORAGE_KEY,
  admApi,
  clearAdmAccessToken,
  createAdmHeaders,
  setAdmAccessToken
} from "./cpfApi";
import { createTransactionId, isValidTransactionId } from "./transaction";

describe("ADM 공통 API client", () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("Options API와 Composition API가 동일한 표준 Header 계약을 사용한다", () => {
    const headers = createAdmHeaders(
      { "Content-Type": "application/json", "X-Transaction-Id": "legacy-invalid-id" },
      "memory-token"
    );

    expect(headers.get("Authorization")).toBe("Bearer memory-token");
    expect(headers.get("X-Caller-Service")).toBe("adm-ui");
    expect(headers.get("X-Original-Channel-Code")).toBe("ADM");
    expect(isValidTransactionId(headers.get("X-Transaction-Id"))).toBe(true);
  });

  it("유효한 상위 transactionId는 새 값으로 바꾸지 않는다", () => {
    const inheritedTransactionId = createTransactionId();

    const headers = createAdmHeaders({ "X-Transaction-Id": inheritedTransactionId }, "");

    expect(headers.get("X-Transaction-Id")).toBe(inheritedTransactionId);
  });

  it("sessionStorage token만 사용하고 영구 저장된 legacy token은 제거한다", async () => {
    setAdmAccessToken("session-token");
    localStorage.setItem(ADM_ACCESS_TOKEN_STORAGE_KEY, "legacy-persistent-token");
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ status: "ok" }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    ));
    vi.stubGlobal("fetch", fetchMock);

    await admApi("/adm/api/runtime-control/status");

    const options = fetchMock.mock.calls[0][1] as RequestInit;
    const headers = new Headers(options.headers);
    expect(headers.get("Authorization")).toBe("Bearer session-token");
    expect(isValidTransactionId(headers.get("X-Transaction-Id"))).toBe(true);
    expect(localStorage.getItem(ADM_ACCESS_TOKEN_STORAGE_KEY)).toBeNull();

    clearAdmAccessToken();
    expect(sessionStorage.getItem(ADM_ACCESS_TOKEN_STORAGE_KEY)).toBeNull();
  });
});
