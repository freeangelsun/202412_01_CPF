import { describe, expect, it } from "vitest";
import { parseStrictJsonObject } from "./strictJsonObject";
describe("strictJsonObject",()=>{
  it("rejects exact duplicate keys",()=>expect(()=>parseStrictJsonObject('{"a":1,"a":2}')).toThrow(/중복/));
  it("rejects unicode-normalized duplicate keys",()=>expect(()=>parseStrictJsonObject('{"é":1,"e\u0301":2}')).toThrow(/중복/));
  it("rejects unsafe integers",()=>expect(()=>parseStrictJsonObject('{"id":9007199254740993}')).toThrow(/안전 정수/));
  it("rejects lossy high-scale decimals",()=>expect(()=>parseStrictJsonObject('{"amount":0.12345678901234567}')).toThrow(/정밀도/));
  it("preserves null fields",()=>expect(parseStrictJsonObject('{"name":null}')).toEqual({name:null}));
});
