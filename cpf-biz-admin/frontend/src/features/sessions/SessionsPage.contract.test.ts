import { describe, expect, it } from "vitest";
import source from "./SessionsPage.vue?raw";

describe("SessionsPage contract", () => {
  it("uses the generated operation and an explicit auditable dialog", () => {
    expect(source).toContain("bzaAuthRevokeSession");
    expect(source).toContain("감사 사유");
    expect(source).not.toContain("prompt(");
    expect(source).not.toContain("window.confirm");
  });
});
