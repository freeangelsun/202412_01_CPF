import { describe, expect, it } from "vitest";
import { bzaOperationState } from "./operationState";

describe("bzaOperationState", () => {
  it.each([
    [401, "SIGN_IN", false], [403, "CHECK_PERMISSION", false], [404, "REFRESH", false], [409, "REFRESH", false],
    [429, "RETRY_LATER", true], [500, "CONTACT_SUPPORT", true], [503, "RETRY_LATER", true],
  ] as const)("maps %s", (status, action, retryable) => {
    expect(bzaOperationState({ status })).toMatchObject({ status, action, retryable });
  });
});
