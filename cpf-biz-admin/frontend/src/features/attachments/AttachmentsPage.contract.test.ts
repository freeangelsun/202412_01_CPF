import { describe, expect, it } from "vitest";
import source from "./AttachmentsPage.vue?raw";

describe("AttachmentsPage product workflow", () => {
  it("does not use browser prompt and exposes all canonical attachment operations", () => {
    expect(source).not.toContain("prompt(");
    for (const operationId of [
      "bzaSupportFindAttachments",
      "bzaSupportUploadAttachment",
      "bzaSupportDownloadAttachment",
      "bzaSupportRecheckAttachment",
      "bzaSupportUpdateAttachmentSecurity"
    ]) expect(source).toContain(operationId);
  });

  it("uses permission-gated actions and accessible error/status feedback", () => {
    expect(source).toContain('hasBzaPermission("ATTACHMENT"');
    expect(source).toContain('role="alert"');
    expect(source).toContain('role="status"');
    expect(source).toContain('aria-live="polite"');
  });
});
