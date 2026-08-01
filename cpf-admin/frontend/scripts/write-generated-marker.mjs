import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const specPath = path.resolve(process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const generatedDir = path.join(root, "src/generated");
const configPath = path.join(root, "orval.config.ts");
const lockPath = path.join(root, "package-lock.json");
const packagePath = path.join(root, "package.json");
const sha256 = value => crypto.createHash("sha256").update(value).digest("hex");
for (const file of [specPath, configPath, lockPath, packagePath]) {
  if (!fs.existsSync(file)) throw new Error(`필수 생성 입력이 없습니다: ${path.relative(root, file)}`);
}
if (!fs.existsSync(generatedDir)) throw new Error("src/generated가 없습니다. Orval generation을 먼저 실행하세요.");
const spec = JSON.parse(fs.readFileSync(specPath, "utf8"));
if (spec["x-cpf-source-sha"] || spec["x-cpf-result-sha"]) {
  throw new Error("Git SHA는 추적 OpenAPI에 기록할 수 없습니다. Release Evidence로 분리하세요.");
}
const generatedFiles = [];
function walk(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (entry.name === ".cpf-openapi-source.json" || entry.name === "source-sha.json") continue;
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
const packageJson = JSON.parse(fs.readFileSync(packagePath, "utf8"));
const operationIds = [];
for (const methods of Object.values(spec.paths || {})) {
  for (const [method, operation] of Object.entries(methods || {})) {
    if (!["get","post","put","patch","delete","head","options","trace"].includes(method)) continue;
    if (operation?.operationId) operationIds.push(operation.operationId);
  }
}
operationIds.sort();
const fileSetSha256 = sha256(generatedFiles.map(item => `${item.path}:${item.sha256}`).join("\n"));
const marker = {
  schemaVersion: 3,
  identityPolicy: "TRACKED_HASHES_RELEASE_SHA_IN_EVIDENCE",
  openApiPath: path.relative(root, specPath).replaceAll("\\", "/"),
  openApiSha256: sha256(fs.readFileSync(specPath)),
  openApiOperationCount: operationIds.length,
  openApiOperationIdsSha256: sha256(operationIds.join("\n")),
  generator: { name: "orval", version: packageJson.devDependencies?.orval || null },
  generatorConfigPath: "orval.config.ts",
  generatorConfigSha256: sha256(fs.readFileSync(configPath)),
  packageLockPath: "package-lock.json",
  packageLockSha256: sha256(fs.readFileSync(lockPath)),
  nodeRequirement: packageJson.engines?.node || null,
  npmRequirement: packageJson.engines?.npm || null,
  generatedFiles,
  generatedFileSetSha256: fileSetSha256,
  sanitized: true
};
if (!marker.generator.version || !marker.nodeRequirement || !marker.npmRequirement) {
  throw new Error("Orval/Node/npm exact requirement가 누락되었습니다.");
}
fs.writeFileSync(path.join(generatedDir, ".cpf-openapi-source.json"), JSON.stringify(marker, null, 2) + "\n", "utf8");
const legacy = path.join(generatedDir, "source-sha.json");
if (fs.existsSync(legacy)) throw new Error("Legacy source-sha.json must be removed explicitly using the tracked Delete Manifest; generation will not delete tracked files.");
console.log(`[CPF][FRONTEND][PASS] marker schema=3 operations=${operationIds.length} files=${generatedFiles.length}`);
