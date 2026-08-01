import { describe, expect, it } from "vitest";
import { causalContextQuery, mergeCausalContext, parseCausalContext, withCausalContext } from "./causalContext";

describe("CPF causal navigation context", () => {
  it("preserves the supported operational identifiers", () => {
    const value = parseCausalContext({ transactionId: "TRX-1", executionId: "101", environment: "PROD" });
    expect(value).toEqual({ transactionId: "TRX-1", executionId: "101", environment: "PROD" });
  });

  it("rejects invalid or control-character values", () => {
    expect(parseCausalContext({ transactionId: "<script>", filter: "bad\u0000filter", from: "yesterday" })).toEqual({});
  });

  it("allows an explicit target query to override carried context", () => {
    const route = withCausalContext({ name: "batch-executions", query: { executionId: "202" } }, { transactionId: "TRX-1", executionId: "101" });
    expect(route).toMatchObject({ query: { transactionId: "TRX-1", executionId: "202" } });
  });

  it("merges a new deep link without losing the prior causal chain", () => {
    expect(mergeCausalContext({ transactionId: "TRX-1", serviceId: "svc" }, { executionId: "101" })).toEqual({ transactionId: "TRX-1", serviceId: "svc", executionId: "101" });
    expect(causalContextQuery({ transactionId: "TRX-1" })).toEqual({ transactionId: "TRX-1" });
  });
});
