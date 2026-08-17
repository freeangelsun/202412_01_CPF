import { describe, expect, it } from "vitest";
import { defaultHeaders, protectedCpfTransactionHeaders } from "./clientHeaders";
import { createBzaHeaders } from "./cpfApi";

describe("BZA browser Header ownership", () => {
  it("browser defaults contain only browser-owned metadata", () => {
    expect(defaultHeaders["X-Client-Id"]).toBe("cpf-bza-ui");
    expect(defaultHeaders["X-Client-Version"]).toBe("1.0.0");
    for (const name of protectedCpfTransactionHeaders) expect(defaultHeaders[name]).toBeUndefined();
    expect(Object.isFrozen(defaultHeaders)).toBe(true);
  });

  it("protected CPF transaction headers are rejected instead of generated or forwarded", () => {
    for (const name of protectedCpfTransactionHeaders) {
      expect(() => createBzaHeaders({ [name]: "forged" })).toThrow(name);
    }
  });
});
