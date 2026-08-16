import { describe, expect, it, vi } from "vitest";
import { createTransactionId, defaultHeaders, isValidTransactionId } from "./transaction";

describe("BZA transaction contract", () => {
  it("generates the canonical 34-character BZA transaction id", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-08-03T14:15:16.789Z"));
    const random = vi.spyOn(globalThis.crypto, "getRandomValues").mockImplementation((array: ArrayBufferView | null) => {
      (array as Uint32Array)[0] = 41;
      return array;
    });
    try {
      const value = createTransactionId();
      expect(value).toHaveLength(34);
      expect(value.slice(17, 20)).toBe("BZA");
      expect(value.slice(20, 27)).toBe("bzaUI01");
      expect(value.endsWith("0000042")).toBe(true);
      expect(isValidTransactionId(value)).toBe(true);
    } finally {
      random.mockRestore();
      vi.useRealTimers();
    }
  });

  it("publishes only BZA channel defaults and never a browser bearer token", () => {
    expect(defaultHeaders["X-Original-Channel-Code"]).toBe("BZA");
    expect(defaultHeaders["X-Channel-Code"]).toBe("BZA");
    expect(defaultHeaders["X-Caller-Service"]).toBe("bza-ui");
    expect(defaultHeaders.Authorization).toBeUndefined();
  });

  it("rejects malformed fixed-length transaction components", () => {
    expect(() => createTransactionId("BZ", "bzaUI01")).toThrow(/3 alphanumeric/);
    expect(() => createTransactionId("BZA", "bza-ui-01")).toThrow(/7 alphanumeric/);
    expect(isValidTransactionId("not-a-transaction-id")).toBe(false);
  });
});
