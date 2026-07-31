import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const specPath = path.resolve(process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const generatedDir = path.join(root, "src/generated");
if (!fs.existsSync(specPath)) throw new Error(`OpenAPI source missing: ${specPath}`);
fs.mkdirSync(generatedDir, { recursive: true });
const spec = JSON.parse(fs.readFileSync(specPath, "utf8"));
const methods = new Set(["get", "post", "put", "patch", "delete", "head", "options", "trace"]);
const operations = [];
for (const [template, pathItem] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(pathItem || {})) {
    if (!methods.has(method) || !operation || typeof operation !== "object") continue;
    const operationId = operation.operationId;
    if (!operationId || !/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(operationId)) {
      throw new Error(`Invalid or missing operationId: ${method.toUpperCase()} ${template}`);
    }
    operations.push({ method: method.toUpperCase(), template, operationId });
  }
}
operations.sort((a, b) => `${a.method} ${a.template}`.localeCompare(`${b.method} ${b.template}`));
if (!operations.length) throw new Error("OpenAPI has no operations");
const duplicateIds = operations.map(value => value.operationId).filter((value, index, all) => all.indexOf(value) !== index);
if (duplicateIds.length) throw new Error(`Duplicate operationId: ${[...new Set(duplicateIds)].join(", ")}`);
const quote = value => JSON.stringify(value);
const typeUnion = operations.map(value => quote(value.operationId)).join(" | ");
const records = operations.map(value => `  { method: ${quote(value.method)}, template: ${quote(value.template)}, operationId: ${quote(value.operationId)} }`).join(",\n");
const contract = `/* eslint-disable */
// Generated from canonical Backend OpenAPI. Do not edit manually.
export type CpfOperationId = ${typeUnion};
export interface CpfOperationDescriptor { method: string; template: string; operationId: CpfOperationId; }
export const cpfOperationDescriptors: readonly CpfOperationDescriptor[] = [
${records}
] as const;

function matchesTemplate(template: string, pathname: string): boolean {
  const expected = template.split("/");
  const actual = pathname.split("/");
  if (expected.length !== actual.length) return false;
  return expected.every((segment, index) =>
    (segment.startsWith("{") && segment.endsWith("}")) || segment === actual[index]
  );
}
export function resolveCpfOperation(method: string, rawUrl: string): CpfOperationDescriptor {
  const pathname = new URL(rawUrl, window.location.origin).pathname;
  const normalizedMethod = method.trim().toUpperCase();
  const found = cpfOperationDescriptors.find(value => value.method === normalizedMethod && matchesTemplate(value.template, pathname));
  if (!found) throw new Error(\`CPF OpenAPI operation is not registered: \${normalizedMethod} \${pathname}\`);
  return found;
}
`;
fs.writeFileSync(path.join(generatedDir, "cpf-operation-contract.ts"), contract, "utf8");

const compatibility = [];
compatibility.push("/* eslint-disable */");
compatibility.push("// Generated compatibility adapter backed by the canonical full OpenAPI.");
compatibility.push('import { cpfGeneratedRequest } from "../shared/cpfApi";');
compatibility.push("export interface CpfGeneratedRequestOptions { data?: unknown; signal?: AbortSignal; headers?: HeadersInit; path?: Record<string, string | number>; query?: Record<string, unknown>; }");
compatibility.push('function renderPath(template: string, values: Record<string, string | number> = {}): string { return template.replace(/\\{([^}]+)\\}/g, (_, name) => { const value = values[name]; if (value === undefined || value === null || String(value).trim() === "") throw new Error(`Missing path parameter: ${name}`); return encodeURIComponent(String(value)); }); }');
for (const operation of operations) {
  compatibility.push(`export async function ${operation.operationId}<T = unknown>(options: CpfGeneratedRequestOptions = {}): Promise<T> {`);
  compatibility.push(`  return cpfGeneratedRequest<T>({ url: renderPath(${quote(operation.template)}, options.path), method: ${quote(operation.method)}, data: options.data, params: options.query, signal: options.signal, headers: options.headers });`);
  compatibility.push("}");
}
fs.writeFileSync(path.join(generatedDir, "cpf-api.ts"), compatibility.join("\n") + "\n", "utf8");
console.log(`[CPF][FRONTEND][PASS] operation contract operations=${operations.length}`);
