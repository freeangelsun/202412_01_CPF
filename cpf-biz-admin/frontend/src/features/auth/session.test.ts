import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cpfOrvalRequest } from "../../shared/orval-mutator";
import { bzaSession, clearBzaSession } from "./session";

function pathname(input: RequestInfo | URL): string {
  return new URL(String(input), window.location.origin).pathname;
}

function authenticatedOperator(loginId = "bza-admin"): void {
  bzaSession.operator = { loginId };
  bzaSession.loaded = true;
}

function protectedRequest(index = 0): Promise<{ ok: boolean }> {
  return cpfOrvalRequest(`/api/bza/test/${index}`, { method: "GET" });
}

describe("BZA BFF session recovery", () => {
  beforeEach(() => {
    clearBzaSession();
    sessionStorage.clear();
    localStorage.clear();
    authenticatedOperator();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    clearBzaSession();
    sessionStorage.clear();
    localStorage.clear();
  });

  it("10개 동시 401을 하나의 서버 BFF refresh로 회복하고 credential을 노출하지 않는다", async () => {
    const protectedCalls = new Map<string, number>();
    let refreshCalls = 0;
    const browserAuthorizationHeaders: string[] = [];
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const path = pathname(input);
      browserAuthorizationHeaders.push(new Headers(init?.headers).get("Authorization") || "");
      expect(init?.credentials).toBe("include");
      if (path === "/api/bza/auth/refresh") {
        refreshCalls += 1;
        await Promise.resolve();
        return new Response(JSON.stringify({ operator: { loginId: "bza-admin" } }), {
          status: 200,
          headers: { "Content-Type": "application/json" }
        });
      }
      const count = (protectedCalls.get(path) || 0) + 1;
      protectedCalls.set(path, count);
      return count === 1
        ? new Response(JSON.stringify({ message: "expired" }), {
            status: 401,
            headers: { "Content-Type": "application/json" }
          })
        : new Response(JSON.stringify({ ok: true }), {
            status: 200,
            headers: { "Content-Type": "application/json" }
          });
    }));

    const results = await Promise.all(Array.from({ length: 10 }, (_, index) => protectedRequest(index)));

    expect(results.every(result => result.ok)).toBe(true);
    expect(refreshCalls).toBe(1);
    expect(browserAuthorizationHeaders.every(value => value === "")).toBe(true);
    expect(Object.keys(sessionStorage).concat(Object.keys(localStorage)).filter(key => /token/i.test(key))).toEqual([]);
  });

  it("refresh 실패 시 Browser의 stale operator state를 정리한다", async () => {
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      const refresh = pathname(input) === "/api/bza/auth/refresh";
      return new Response(JSON.stringify({ message: refresh ? "revoked" : "expired" }), {
        status: 401,
        headers: { "Content-Type": "application/json" }
      });
    }));

    await expect(protectedRequest()).rejects.toThrow("revoked");

    expect(bzaSession.operator).toBeNull();
    expect(bzaSession.loaded).toBe(false);
    expect(Object.keys(sessionStorage).concat(Object.keys(localStorage)).filter(key => /token/i.test(key))).toEqual([]);
  });

  it("refresh 후 재시도도 401이면 세션을 fail-closed로 정리한다", async () => {
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      const path = pathname(input);
      return new Response(JSON.stringify(path.endsWith("/auth/refresh")
        ? { operator: { loginId: "bza-admin" } }
        : { message: "still unauthorized" }), {
        status: path.endsWith("/auth/refresh") ? 200 : 401,
        headers: { "Content-Type": "application/json" }
      });
    }));

    await expect(protectedRequest()).rejects.toThrow("still unauthorized");
    expect(bzaSession.operator).toBeNull();
    expect(bzaSession.loaded).toBe(false);
  });

  it("종료된 세션의 늦은 refresh 응답은 새 세션을 덮어쓰거나 정리하지 않는다", async () => {
    let resolveRefresh!: (response: Response) => void;
    let refreshCalls = 0;
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      if (pathname(input) !== "/api/bza/auth/refresh") {
        return new Response(JSON.stringify({ message: "expired" }), {
          status: 401,
          headers: { "Content-Type": "application/json" }
        });
      }
      refreshCalls += 1;
      return new Promise<Response>(resolve => { resolveRefresh = resolve; });
    }));

    const staleRequest = protectedRequest();
    await vi.waitFor(() => expect(refreshCalls).toBe(1));

    clearBzaSession();
    authenticatedOperator("new-operator");
    resolveRefresh(new Response(JSON.stringify({ operator: { loginId: "stale-operator" } }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    }));

    await expect(staleRequest).rejects.toThrow("이미 종료된 BZA 세션");
    expect(bzaSession.operator?.loginId).toBe("new-operator");
    expect(bzaSession.loaded).toBe(true);
  });
});
