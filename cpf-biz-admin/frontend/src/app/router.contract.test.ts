import { describe, expect, it } from "vitest";
import { bzaRouter } from "./router";

describe("BZA router status contract", () => {
  it("declares explicit forbidden and not-found routes", () => {
    expect(bzaRouter.hasRoute("forbidden")).toBe(true);
    expect(bzaRouter.hasRoute("not-found")).toBe(true);
    expect(bzaRouter.resolve("/__cpf_missing_route__").name).toBe("not-found");
  });
});
