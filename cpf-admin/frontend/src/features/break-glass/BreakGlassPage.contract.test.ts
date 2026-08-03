import { describe, expect, it } from "vitest";
import source from "./BreakGlassPage.vue?raw";

describe("BreakGlassPage product workflow", () => {
  it("removes browser prompt and invokes all canonical operations", () => {
    expect(source).not.toMatch(/\bprompt\s*\(/);
    for (const operationId of ["admBreakGlassFindSessions", "admBreakGlassOpenSession", "admBreakGlassReviewSession", "admBreakGlassCloseSession"])
      expect(source).toContain(operationId);
  });
  it("gates dangerous actions and exposes accessible result feedback", () => {
    expect(source).toContain('session.canWrite("breakGlass"');
    expect(source).toContain('role="alert"');
    expect(source).toContain('role="status"');
  });
});
