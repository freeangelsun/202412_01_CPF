import { describe, expect, it } from "vitest";
import {
  admApi,
  admInvokeOperation,
  admMutation,
  admQuery,
  bzaApi,
  bzaInvokeOperation,
  bzaMutation,
  bzaQuery,
  bzaRawResponse,
  cpfApi,
  createBzaHeaders
} from "./cpfApi";

describe("BZA public API compatibility and trust boundary", () => {
  it("기존 bza* public exports를 BZA의 정식 계약으로 유지한다", () => {
    expect(typeof bzaQuery).toBe("function");
    expect(typeof bzaMutation).toBe("function");
    expect(typeof bzaApi).toBe("function");
    expect(typeof bzaInvokeOperation).toBe("function");
    expect(cpfApi).toBe(bzaApi);
  });

  it("이전 회차에서 잘못 노출된 adm* 이름도 한 migration window 동안 호환한다", () => {
    expect(admQuery).toBe(bzaQuery);
    expect(admMutation).toBe(bzaMutation);
    expect(admApi).toBe(bzaApi);
    expect(admInvokeOperation).toBe(bzaInvokeOperation);
  });

  it("BZA 전용 오류와 Header 경계를 사용한다", () => {
    expect(() => createBzaHeaders({ Authorization: "Bearer forbidden" })).toThrow("BZA BFF");
  });

  it("일반 문자열 필드는 허용하되 JSON 문자열 내부 actor alias는 차단한다", async () => {
    const originalFetch = globalThis.fetch;
    globalThis.fetch = async () => new Response("{}", { status: 200 });
    try {
      await bzaRawResponse("/bza/api/users", "POST", { reason: "normal operation", nested: { description: "safe" } });
      await expect(bzaRawResponse("/bza/api/users", "POST", { payload: '{"operatorId":"browser"}' })).rejects.toThrow("operatorId");
    } finally {
      globalThis.fetch = originalFetch;
    }
  });

  it("Raw/Form/Query actor 우회를 차단한다", async () => {
    await expect(bzaRawResponse("/bza/api/users", "POST", '{"requestUser":"browser"}')).rejects.toThrow("requestUser");
    const form = new FormData(); form.set("actorId", "browser");
    await expect(bzaRawResponse("/bza/api/users", "POST", form)).rejects.toThrow("actorId");
    await expect(bzaRawResponse("/bza/api/users?operatorId=browser")).rejects.toThrow("operatorId");
  });

  it("multipart upload은 Browser가 boundary를 생성하도록 Content-Type을 강제하지 않는다", async () => {
    const originalFetch = globalThis.fetch;
    let captured: RequestInit | undefined;
    globalThis.fetch = async (_input, init) => {
      captured = init;
      return new Response(JSON.stringify({ attachmentId: "ATT-1" }), {
        status: 201,
        headers: { "Content-Type": "application/json" }
      });
    };
    try {
      const form = new FormData();
      form.set("groupId", "GENERAL");
      form.set("reason", "업무 첨부 등록");
      form.set("file", new File(["safe"], "safe.txt", { type: "text/plain" }));
      const result = await bzaMutation<{ attachmentId: string }>("/api/bza/attachments", "POST", form);
      expect(result.attachmentId).toBe("ATT-1");
      expect(captured?.body).toBe(form);
      const headers = new Headers(captured?.headers);
      expect(headers.has("Content-Type")).toBe(false);
      expect(headers.get("X-CPF-Operation-Id")).toBe("bzaSupportUploadAttachment");
      expect(captured?.credentials).toBe("include");
    } finally {
      globalThis.fetch = originalFetch;
    }
  });

  it("multipart 오류 응답을 성공으로 은폐하지 않는다", async () => {
    const originalFetch = globalThis.fetch;
    globalThis.fetch = async () => new Response(JSON.stringify({ message: "malware detected" }), {
      status: 409,
      headers: { "Content-Type": "application/json" }
    });
    try {
      const form = new FormData();
      form.set("groupId", "GENERAL");
      form.set("reason", "재현 테스트");
      form.set("file", new File(["unsafe"], "unsafe.txt", { type: "text/plain" }));
      await expect(bzaMutation("/api/bza/attachments", "POST", form)).rejects.toMatchObject({ status: 409 });
    } finally {
      globalThis.fetch = originalFetch;
    }
  });

});
