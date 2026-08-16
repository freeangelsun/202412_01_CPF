import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawnSync } from "node:child_process";
const root = process.cwd();
const verifier = path.join(root, "scripts/verify-generated-client.mjs");
const marker = path.join(root, "src/generated/.cpf-openapi-source.json");
if (!fs.existsSync(marker)) throw new Error("marker missing");
const original = fs.readFileSync(marker);
function mustFail(name, mutate, env={}) {
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), `cpf-${name}-`));
  fs.cpSync(root, temp, { recursive: true, filter: source => !source.includes(`${path.sep}node_modules`) && !source.includes(`${path.sep}dist`) });
  mutate(temp);
  const result = spawnSync(process.execPath, [path.join(temp, "scripts/verify-generated-client.mjs")], {
    cwd: temp, encoding: "utf8", env: { ...process.env, CPF_SOURCE_SHA: "0".repeat(40), ...env }
  });
  if (result.status === 0) throw new Error(`${name} fixture false green`);
  fs.rmSync(temp, { recursive: true, force: true });
}
mustFail("marker-schema", temp => { const p=path.join(temp,"src/generated/.cpf-openapi-source.json"); const m=JSON.parse(fs.readFileSync(p)); m.schemaVersion=2; fs.writeFileSync(p,JSON.stringify(m)); });
mustFail("openapi-drift", temp => fs.appendFileSync(path.join(temp,"openapi/cpf-openapi.json"),"\n"));
mustFail("generated-drift", temp => { const m=JSON.parse(original); fs.appendFileSync(path.join(temp,m.generatedFiles[0].path),"\n"); });
mustFail("operation-contract-drift", temp => fs.appendFileSync(path.join(temp,"src/generated/cpf-operation-contract.ts"),"\n"));
mustFail("orval-client-missing", temp => fs.rmSync(path.join(temp,"src/generated/orval/cpf-api.ts")));
mustFail("lock-drift", temp => fs.appendFileSync(path.join(temp,"package-lock.json"),"\n"));
mustFail("stale-source", temp => {}, { CPF_EXPECTED_SOURCE_SHA: "1".repeat(40) });
{
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), "cpf-source-required-"));
  fs.cpSync(root, temp, { recursive: true, filter: source => !source.includes(`${path.sep}node_modules`) && !source.includes(`${path.sep}dist`) && !source.includes(`${path.sep}.git`) });
  const env = { ...process.env }; delete env.CPF_SOURCE_SHA; delete env.CPF_EXPECTED_SOURCE_SHA;
  const result = spawnSync(process.execPath, [path.join(temp, "scripts/verify-generated-client.mjs")], { cwd: temp, encoding: "utf8", env });
  if (result.status === 0) throw new Error("missing-source fixture false green");
  fs.rmSync(temp, { recursive: true, force: true });
}
console.log("[CPF][FRONTEND][PASS] generated client negative fixtures");
