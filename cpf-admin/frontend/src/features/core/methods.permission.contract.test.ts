import { describe, expect, it } from "vitest";
import { coreMethods } from "./methods";
describe("ADM permission helpers", () => {
  it("fails closed when delete projection is absent", () => { const ctx={permission:()=>({})}; expect(coreMethods.canDelete.call(ctx,"SERVICE_REGISTRY")).toBe(false); });
  it("requires explicit delete permission", () => { expect(coreMethods.canDelete.call({permission:()=>({deleteAllowed:true})},"SERVICE_REGISTRY")).toBe(true); expect(coreMethods.canDelete.call({permission:()=>({deleteAllowed:false})},"SERVICE_REGISTRY")).toBe(false); });
  it("does not inherit menu write while button projection is missing", () => { const ctx={buttonsLoaded:false,authorizedButtons:[],canWrite:()=>true}; expect(coreMethods.canButton.call(ctx,"MAINTENANCE_EXECUTE","MAINTENANCE")).toBe(false); });
});
