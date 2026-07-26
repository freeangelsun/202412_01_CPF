import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  bzaApi,
  bzaSession,
  clearBzaSession
} from "./session";

describe("BZA refresh session", () => {
  beforeEach(() => {
    clearBzaSession();
    bzaSession.accessToken = "expired-access";
    bzaSession.refreshToken = "refresh-token";
    bzaSession.operator = { loginId: "bza-admin" };
    sessionStorage.setItem("bza.accessToken", "expired-access");
    sessionStorage.setItem("bza.refreshToken", "refresh-token");
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    clearBzaSession();
  });

  it("동시 401은 하나의 refresh rotation만 수행한다", async () => {
    let protectedCalls = 0;
    let refreshCalls = 0;
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      const url = String(input);
      if (url === "/api/bza/auth/refresh") {
        refreshCalls += 1;
        return new Response(JSON.stringify({
          accessToken: "rotated-access",
          refreshToken: "rotated-refresh"
        }), { status: 200, headers: { "Content-Type": "application/json" } });
      }
      protectedCalls += 1;
      if (protectedCalls <= 2) {
        return new Response(JSON.stringify({ message: "expired" }), {
          status: 401,
          headers: { "Content-Type": "application/json" }
        });
      }
      return new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    }));

    const [first, second] = await Promise.all([
      bzaApi<{ ok: boolean }>("/api/bza/dashboard"),
      bzaApi<{ ok: boolean }>("/api/bza/dashboard")
    ]);

    expect(first.ok).toBe(true);
    expect(second.ok).toBe(true);
    expect(refreshCalls).toBe(1);
    expect(bzaSession.accessToken).toBe("rotated-access");
    expect(bzaSession.refreshToken).toBe("rotated-refresh");
  });

  it("refresh 실패 시 stale token과 operator state를 모두 정리한다", async () => {
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      const status = String(input) === "/api/bza/auth/refresh" ? 401 : 401;
      return new Response(JSON.stringify({ message: "revoked" }), {
        status,
        headers: { "Content-Type": "application/json" }
      });
    }));

    await expect(bzaApi("/api/bza/dashboard")).rejects.toThrow("revoked");

    expect(bzaSession.accessToken).toBeNull();
    expect(bzaSession.refreshToken).toBeNull();
    expect(bzaSession.operator).toBeNull();
    expect(sessionStorage.getItem("bza.accessToken")).toBeNull();
    expect(sessionStorage.getItem("bza.refreshToken")).toBeNull();
  });

  it("늦게 도착한 기존 access token의 401은 회전된 token으로 재시도한다", async () => {
    let expiredCalls = 0;
    let refreshCalls = 0;
    let releaseLateResponse!: () => void;
    const lateResponse = new Promise<void>(resolve => {
      releaseLateResponse = resolve;
    });

    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);
      if (url === "/api/bza/auth/refresh") {
        refreshCalls += 1;
        return new Response(JSON.stringify({
          accessToken: "rotated-access",
          refreshToken: "rotated-refresh"
        }), { status: 200, headers: { "Content-Type": "application/json" } });
      }
      const authorization = new Headers(init?.headers).get("Authorization");
      if (authorization === "Bearer expired-access") {
        expiredCalls += 1;
        if (expiredCalls === 2) await lateResponse;
        return new Response(JSON.stringify({ message: "expired" }), {
          status: 401,
          headers: { "Content-Type": "application/json" }
        });
      }
      return new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    }));

    const first = bzaApi<{ ok: boolean }>("/api/bza/dashboard");
    const second = bzaApi<{ ok: boolean }>("/api/bza/dashboard");
    await expect(first).resolves.toEqual({ ok: true });
    releaseLateResponse();
    await expect(second).resolves.toEqual({ ok: true });

    expect(refreshCalls).toBe(1);
  });

  it("종료된 session의 늦은 refresh 응답은 새 session을 덮어쓰거나 정리하지 않는다", async () => {
    let resolveRefresh!: (response: Response) => void;
    let refreshCalls = 0;
    vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL) => {
      if (String(input) !== "/api/bza/auth/refresh") {
        return new Response(JSON.stringify({ message: "expired" }), {
          status: 401,
          headers: { "Content-Type": "application/json" }
        });
      }
      refreshCalls += 1;
      return new Promise<Response>(resolve => {
        resolveRefresh = resolve;
      });
    }));

    const staleRequest = bzaApi("/api/bza/dashboard");
    await vi.waitFor(() => expect(refreshCalls).toBe(1));

    clearBzaSession();
    bzaSession.accessToken = "new-access";
    bzaSession.refreshToken = "new-refresh";
    bzaSession.operator = { loginId: "new-operator" };
    sessionStorage.setItem("bza.accessToken", "new-access");
    sessionStorage.setItem("bza.refreshToken", "new-refresh");

    resolveRefresh(new Response(JSON.stringify({
      accessToken: "stale-access",
      refreshToken: "stale-refresh"
    }), { status: 200, headers: { "Content-Type": "application/json" } }));

    await expect(staleRequest).rejects.toThrow("이미 종료된 BZA 세션");
    expect(bzaSession.accessToken).toBe("new-access");
    expect(bzaSession.refreshToken).toBe("new-refresh");
    expect(bzaSession.operator?.loginId).toBe("new-operator");
  });
});
