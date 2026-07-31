import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
const root = process.cwd();
const markerPath = path.join(root, "src/generated/.cpf-openapi-source.json");
if (!fs.existsSync(markerPath)) throw new Error("Generated client marker가 없습니다.");
const marker = JSON.parse(fs.readFileSync(markerPath, "utf8"));
if (marker.schemaVersion !== 2 || !/^[0-9a-f]{40}$/.test(marker.sourceSha || "")) throw new Error("Generated marker 계약이 유효하지 않습니다.");
const expectedSource = process.env.CPF_SOURCE_SHA;
if (expectedSource && marker.sourceSha !== expectedSource) throw new Error(`OpenAPI SHA 불일치: ${marker.sourceSha} != ${expectedSource}`);
const sha256 = value => crypto.createHash("sha256").update(value).digest("hex");
const openApi = path.join(root, marker.openApiPath);
if (!fs.existsSync(openApi) || sha256(fs.readFileSync(openApi)) !== marker.openApiSha256) throw new Error("OpenAPI snapshot hash가 marker와 다릅니다.");
for (const artifact of marker.generatedFiles || []) {
  const file = path.join(root, artifact.path);
  if (!fs.existsSync(file) || sha256(fs.readFileSync(file)) !== artifact.sha256) throw new Error(`Generated client drift: ${artifact.path}`);
}
const sourceMarker = JSON.parse(fs.readFileSync(path.join(root, "src/generated/source-sha.json"), "utf8"));
if (sourceMarker.sourceSha !== marker.sourceSha) throw new Error("source-sha.json과 marker가 다릅니다.");
console.log(`[CPF][FRONTEND][PASS] generated client source=${marker.sourceSha}`);
