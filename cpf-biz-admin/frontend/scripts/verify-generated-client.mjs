import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";

const root = process.cwd();
const markerPath = path.join(root, "src/generated/.cpf-openapi-source.json");
const sha256 = value => crypto.createHash("sha256").update(value).digest("hex");
if (!fs.existsSync(markerPath)) throw new Error("Generated client marker가 없습니다.");
if (fs.existsSync(path.join(root, "src/generated/source-sha.json"))) {
  throw new Error("Legacy source-sha.json은 Git SHA 자기참조를 만들므로 삭제해야 합니다.");
}
const marker = JSON.parse(fs.readFileSync(markerPath, "utf8"));
const required = ["identityPolicy","openApiPath","openApiSha256","openApiOperationCount",
  "openApiOperationIdsSha256","generator","generatorConfigPath","generatorConfigSha256",
  "packageLockPath","packageLockSha256","nodeRequirement","npmRequirement","generatedFiles",
  "generatedFileSetSha256","sanitized"];
if (marker.schemaVersion !== 3 || required.some(name => marker[name] === undefined)) {
  throw new Error("Generated marker schemaVersion 3 계약이 유효하지 않습니다.");
}
if (marker.identityPolicy !== "TRACKED_HASHES_RELEASE_SHA_IN_EVIDENCE" || marker.sourceSha || marker.resultSha) {
  throw new Error("추적 Marker에 Git SHA를 기록할 수 없습니다.");
}
const resolve = relative => path.join(root, relative);
for (const [relative, expected, label] of [
  [marker.openApiPath, marker.openApiSha256, "OpenAPI"],
  [marker.generatorConfigPath, marker.generatorConfigSha256, "Generator config"],
  [marker.packageLockPath, marker.packageLockSha256, "Lockfile"]
]) {
  const file = resolve(relative);
  if (!fs.existsSync(file) || sha256(fs.readFileSync(file)) !== expected) throw new Error(`${label} hash drift`);
}
const spec = JSON.parse(fs.readFileSync(resolve(marker.openApiPath), "utf8"));
if (spec["x-cpf-source-sha"] || spec["x-cpf-result-sha"]) throw new Error("Tracked OpenAPI Git SHA self-reference");
const operationIds = [];
for (const methods of Object.values(spec.paths || {})) {
  for (const [method, operation] of Object.entries(methods || {})) {
    if (["get","post","put","patch","delete","head","options","trace"].includes(method) && operation?.operationId) operationIds.push(operation.operationId);
  }
}
operationIds.sort();
if (operationIds.length !== marker.openApiOperationCount || sha256(operationIds.join("\n")) !== marker.openApiOperationIdsSha256) {
  throw new Error("OpenAPI operation inventory drift");
}
const requiredGenerated = [
  "src/generated/cpf-api.ts",
  "src/generated/cpf-operation-contract.ts",
  "src/generated/bza-route-operation-contract.ts",
  "src/generated/orval/cpf-api.ts"
];
for (const relative of requiredGenerated) {
  if (!fs.existsSync(resolve(relative))) throw new Error(`Required generated artifact missing: ${relative}`);
}
const compatibilityText = fs.readFileSync(resolve("src/generated/cpf-api.ts"), "utf8");
const operationContractText = fs.readFileSync(resolve("src/generated/cpf-operation-contract.ts"), "utf8");
for (const operationId of operationIds) {
  if (!compatibilityText.includes(`function ${operationId}<`)) throw new Error(`Compatibility operation missing: ${operationId}`);
  if (!operationContractText.includes(`operationId: "${operationId}"`)) throw new Error(`Operation contract missing: ${operationId}`);
}
const orvalText = fs.readFileSync(resolve("src/generated/orval/cpf-api.ts"), "utf8");
if (!orvalText.includes("@tanstack/vue-query")) {
  throw new Error("Orval vue-query generated client is required");
}
for (const operationId of operationIds) {
  const escaped = operationId.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  if (!new RegExp(`export\\s+const\\s+${escaped}\\s*=`).test(orvalText)) {
    throw new Error(`Orval operation missing: ${operationId}`);
  }
}
const files = [...marker.generatedFiles].sort((a,b) => a.path.localeCompare(b.path));
for (const artifact of files) {
  const file = resolve(artifact.path);
  if (!fs.existsSync(file) || sha256(fs.readFileSync(file)) !== artifact.sha256) throw new Error(`Generated client drift: ${artifact.path}`);
}
if (sha256(files.map(item => `${item.path}:${item.sha256}`).join("\n")) !== marker.generatedFileSetSha256) {
  throw new Error("Generated file-set hash drift");
}
function currentSourceSha() {
  const explicit = process.env.CPF_SOURCE_SHA?.trim();
  if (explicit) return explicit;
  const repoRoot = path.resolve(root, "../..");
  const stateTool = path.join(repoRoot, "cpf-tools/verification/tools/cpf-source-state.py");
  if (!fs.existsSync(stateTool)) {
    throw new Error("CPF_SOURCE_SHA 또는 cpf-source-state.py가 필요합니다. Git 조회로 대체하지 않습니다.");
  }
  const python = process.env.CPF_PYTHON?.trim() || "python";
  try {
    const text = execFileSync(python, ["-B", stateTool, "--root", repoRoot, "--scope", "source"], {
      cwd: repoRoot, encoding: "utf8", env: { ...process.env, PYTHONDONTWRITEBYTECODE: "1" }
    });
    return JSON.parse(text).contentSha1;
  } catch (error) {
    throw new Error(`Git-independent Source identity 계산 실패: ${error?.message || error}`);
  }
}
const sourceSha = currentSourceSha();
if (!/^[0-9a-f]{40}$/i.test(sourceSha)) throw new Error("Source SHA는 exact 40자리여야 합니다.");
if (process.env.CPF_EXPECTED_SOURCE_SHA && sourceSha !== process.env.CPF_EXPECTED_SOURCE_SHA.trim()) {
  throw new Error(`Source SHA mismatch: ${sourceSha}`);
}
console.log(`[CPF][FRONTEND][PASS] generated client source=${sourceSha} operations=${operationIds.length}`);
