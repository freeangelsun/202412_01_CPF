import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const verifier = path.join(root, "scripts", "verify-openapi-schema-refs.mjs");
const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-openapi-refs-"));
function run(name, spec) {
  const file = path.join(temp, `${name}.json`);
  fs.writeFileSync(file, JSON.stringify(spec));
  return spawnSync(process.execPath, [verifier, file], { cwd: root, encoding: "utf8" });
}
const base = {
  openapi: "3.0.3",
  paths: { "/ok": { get: { operationId: "ok", responses: { "200": { description: "ok" } } } } },
  components: { schemas: { Request: { type: "object", properties: { value: { type: "string" } } } } }
};
const valid = run("valid", base);
if (valid.status !== 0 || !valid.stdout.includes("[PASS]")) throw new Error(`valid fixture failed: ${valid.stderr}`);
const unresolved = structuredClone(base);
unresolved.components.schemas.Request.properties.child = { $ref: "#/components/schemas/Missing" };
if (run("unresolved", unresolved).status === 0) throw new Error("unresolved schema fixture did not fail");
const contaminated = structuredClone(base);
contaminated.components.schemas.BadController = { type: "object" };
if (run("contaminated", contaminated).status === 0) throw new Error("controller schema contamination fixture did not fail");
const duplicate = structuredClone(base);
duplicate.paths["/duplicate"] = { post: { operationId: "ok", responses: { "200": { description: "ok" } } } };
if (run("duplicate", duplicate).status === 0) throw new Error("duplicate operationId fixture did not fail");
console.log("[CPF][OPENAPI][PASS] schema-reference negative fixtures=3 positive=1");
