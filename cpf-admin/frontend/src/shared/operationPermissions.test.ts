import { describe, expect, it } from "vitest";
import { canInvokeOperation, parseOperationPermissions } from "./operationPermissions";
describe("operationPermissions",()=>{
  it("fails closed when server permissions are absent",()=>expect(canInvokeOperation(parseOperationPermissions(undefined),"dangerousOperation")).toBe(false));
  it("allows only an explicitly granted operation",()=>{const grants=parseOperationPermissions("readOperation, executeOperation");expect(canInvokeOperation(grants,"readOperation")).toBe(true);expect(canInvokeOperation(grants,"replayOperation")).toBe(false);});
});
