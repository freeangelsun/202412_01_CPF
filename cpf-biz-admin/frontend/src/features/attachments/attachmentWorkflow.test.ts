import { describe, expect, it } from "vitest";
import { filenameFromContentDisposition, requireAuditReason, safeAttachmentFilename } from "./attachmentWorkflow";

describe("attachment workflow", () => {
  it("requires an auditable reason", () => {
    expect(requireAuditReason("  업무상 재검사 요청  ")).toBe("업무상 재검사 요청");
    expect(() => requireAuditReason("짧음")).toThrow(/5자 이상/);
  });

  it("sanitizes download filenames", () => {
    expect(safeAttachmentFilename("../../secret?.txt", "ATT-1")).toBe(".._.._secret_.txt");
    expect(safeAttachmentFilename("", "ATT-1")).toBe("attachment-ATT-1");
  });

  it("parses RFC 5987 and plain content-disposition names", () => {
    expect(filenameFromContentDisposition("attachment; filename*=UTF-8''%ED%95%9C%EA%B8%80.txt", "fallback"))
      .toBe("한글.txt");
    expect(filenameFromContentDisposition('attachment; filename="report.csv"', "fallback")).toBe("report.csv");
  });
});
