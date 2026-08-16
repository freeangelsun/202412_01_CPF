import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);
const source = fs.readFileSync(clientPath, "utf8");

if (!source.includes("export const admCacheSummary")) {
  throw new Error("Generated Cache summary operation missing: admCacheSummary");
}
const retiredDirectMutations = ["admCacheRefresh", "admCacheEvictKey", "admCacheEvictNamespace", "admCacheReconcile"];
for (const operationId of retiredDirectMutations) {
  if (source.includes(`export const ${operationId}`)) {
    throw new Error(`Dangerous Cache mutation must not be regenerated as a public client operation: ${operationId}`);
  }
}
console.log("[CPF][FRONTEND][PASS] ADM cache generated client exposes summary only; dangerous mutations use Approval Engine");
