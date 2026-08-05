import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { CpfOrvalError, cpfOrvalRequest } from "./orval-mutator";

describe("cpfOrvalRequest generated response contract", () => {
  beforeEach(() => {
    vi.stubGlobal("window", { location: { origin: "https://cpf.example" } });
    vi.stubGlobal("document", { cookie: "XSRF-TOKEN=csrf-value" });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("returns the Orval data/status/headers envelope", async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ value: 7 }),
      { status: 200, headers: { "Content-Type": "application/json", "X-Request-Id": "req-1" } }
    ));
    vi.stubGlobal("fetch", fetchMock);

    const result = await cpfOrvalRequest<{
      data: { value: number };
      status: 200;
      headers: Headers;
    }>({
      url: "/adm/api/example",
      method: "POST",
      data: { value: 7 }
    });

    expect(result.data).toEqual({ value: 7 });
    expect(result.status).toBe(200);
    expect(result.headers.get("X-Request-Id")).toBe("req-1");
    const request = fetchMock.mock.calls[0][1] as RequestInit;
    expect(new Headers(request.headers).get("X-XSRF-TOKEN")).toBe("csrf-value");
    expect(request.credentials).toBe("include");
    expect(request.redirect).toBe("error");
  });

  it("rejects browser-supplied operator identity before network I/O", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(cpfOrvalRequest({
      url: "/adm/api/notifications/rules",
      method: "POST",
      data: { eventType: "BATCH", reason: "test", requestUser: "spoofed" }
    })).rejects.toThrow("Browser actor field is forbidden");

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("rejects browser actor query fields", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(cpfOrvalRequest({
      url: "/adm/api/notifications/rules",
      method: "GET",
      params: { operatorId: "spoofed" }
    })).rejects.toThrow("Browser actor query field is forbidden");

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("maps non-success responses to CpfOrvalError", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ message: "version conflict" }),
      { status: 409, headers: { "Content-Type": "application/json" } }
    )));

    await expect(cpfOrvalRequest({
      url: "/adm/api/notifications/delivery-logs/1/retry",
      method: "POST"
    })).rejects.toEqual(expect.objectContaining<CpfOrvalError>({
      name: "CpfOrvalError",
      status: 409,
      message: "version conflict"
    }));
  });

  it("rejects cross-origin URLs and browser bearer headers", async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    await expect(cpfOrvalRequest({
      url: "https://evil.example/adm/api/example",
      method: "GET"
    })).rejects.toThrow("same-origin");

    await expect(cpfOrvalRequest({
      url: "/adm/api/example",
      method: "GET",
      headers: { Authorization: "Bearer forbidden" }
    })).rejects.toThrow("Browser Bearer Token");

    expect(fetchMock).not.toHaveBeenCalled();
  });
});
