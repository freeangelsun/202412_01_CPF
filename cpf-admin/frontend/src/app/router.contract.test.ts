import { describe, expect, it } from "vitest";
import { admRouter } from "./router";

describe("ADM router status contract", () => {
  it("declares explicit forbidden and not-found routes", () => {
    expect(admRouter.hasRoute("forbidden")).toBe(true);
    expect(admRouter.hasRoute("not-found")).toBe(true);
    expect(admRouter.resolve("/__cpf_missing_route__").name).toBe("not-found");
  });
});
