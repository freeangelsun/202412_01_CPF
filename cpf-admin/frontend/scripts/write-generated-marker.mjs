import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const specPath = path.resolve(process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const sourceSha = process.env.CPF_SOURCE_SHA || JSON.parse(fs.readFileSync(specPath, "utf8"))["x-cpf-source-sha"];
if (!/^[0-9a-f]{40}$/.test(sourceSha || "")) throw new Error("CPF_SOURCE_SHA 또는 x-cpf-source-sha가 필요합니다.");
const generatedDir = path.join(root, "src/generated");
if (!fs.existsSync(generatedDir)) throw new Error("src/generated가 없습니다. Orval generation을 먼저 실행하세요.");
const sha256 = value => crypto.createHash("sha256").update(value).digest("hex");
const generatedFiles = [];
function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const target = path.join(directory, entry.name);
    if (entry.isDirectory()) walk(target);
    else if (/\.(ts|tsx)$/.test(entry.name)) generatedFiles.push({
      path: path.relative(root, target).replaceAll("\\", "/"),
      sha256: sha256(fs.readFileSync(target))
    });
  }
}
walk(generatedDir);
generatedFiles.sort((left, right) => left.path.localeCompare(right.path));
if (!generatedFiles.length) throw new Error("Generated TypeScript artifact가 없습니다.");
const marker = {
  schemaVersion: 2,
  sourceSha,
  openApiPath: path.relative(root, specPath).replaceAll("\\", "/"),
  openApiSha256: sha256(fs.readFileSync(specPath)),
  generatedFiles,
  generator: "orval",
  generatorConfigPath: "orval.config.ts",
  generatorConfigSha256: sha256(fs.readFileSync(path.join(root, "orval.config.ts")))
};
fs.writeFileSync(path.join(generatedDir, ".cpf-openapi-source.json"), JSON.stringify(marker, null, 2) + "\n", "utf8");
fs.writeFileSync(path.join(generatedDir, "source-sha.json"), JSON.stringify({ sourceSha }, null, 2) + "\n", "utf8");
console.log(`[CPF][FRONTEND][PASS] marker source=${sourceSha} files=${generatedFiles.length}`);
