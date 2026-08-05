import fs from "node:fs";
import path from "node:path";
const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
const mutatorPath = path.resolve(root, "src/shared/orval-mutator.ts");
const cpfApiPath = path.resolve(root, "src/shared/cpfApi.ts");
for (const file of [clientPath, mutatorPath, cpfApiPath]) if (!fs.existsSync(file)) throw new Error(`Required boundary source missing: ${file}`);
const client = fs.readFileSync(clientPath, "utf8");
const mutator = fs.readFileSync(mutatorPath, "utf8");
const cpfApi = fs.readFileSync(cpfApiPath, "utf8");
const failures = [];
if (!mutator.includes('export type CpfOrvalGeneratedRequestOptions = Pick<CpfOrvalRequestConfig, "headers" | "signal">')) failures.push("safe generated request options type missing");
if (!mutator.includes("export interface CpfOrvalResponse<T>")) failures.push("response envelope type missing");
if (client.includes("Parameters<typeof cpfOrvalRequest>[1]")) failures.push("generated operation exposes internal method/data/params options");
if (!client.includes("CpfOrvalGeneratedRequestOptions")) failures.push("generated safe request boundary missing");
if (!client.includes("type SecondParameter<T extends (...args: never) => unknown> = CpfOrvalGeneratedRequestOptions")) failures.push("query/mutation request option alias is unsafe");
const exportedOperationOptions = [...client.matchAll(/export const ([A-Za-z_$][\w$]*) = async \(([^)]*)\)/g)];
if (!exportedOperationOptions.length) failures.push("no generated operations inspected");
for (const [, operationId, parameters] of exportedOperationOptions) {
  if (parameters.includes("options?:") && !parameters.includes("CpfOrvalGeneratedRequestOptions")) failures.push(`${operationId}: unsafe options type`);
}
if (!cpfApi.includes("cpfOrvalPayload<T>")) failures.push("legacy cpfApi envelope adapter missing");
if (!cpfApi.includes("return response.data")) failures.push("legacy cpfApi does not unwrap generated envelope");
if (failures.length) {
  console.error(`[CPF][FRONTEND][FAIL] generated request boundary failures=${failures.length}`);
  failures.slice(0, 100).forEach(value => console.error(` - ${value}`));
  process.exit(1);
}
console.log(`[CPF][FRONTEND][PASS] generated request boundary operations=${exportedOperationOptions.length}`);
