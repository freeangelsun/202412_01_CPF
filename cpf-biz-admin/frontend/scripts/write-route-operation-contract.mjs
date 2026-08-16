import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const routesPath = path.join(root, "src/app/routes.ts");
const specPath = path.resolve(process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const generatedDir = path.join(root, "src/generated");
if (!fs.existsSync(routesPath)) throw new Error(`BZA route registry missing: ${routesPath}`);
if (!fs.existsSync(specPath)) throw new Error(`OpenAPI source missing: ${specPath}`);
const routesText = fs.readFileSync(routesPath, "utf8");
const spec = JSON.parse(fs.readFileSync(specPath, "utf8"));
const methods = new Set(["get", "post", "put", "patch", "delete", "head", "options", "trace"]);
const operations = [];
for (const [template, item] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(item || {})) {
    if (!methods.has(method) || !operation?.operationId) continue;
    operations.push({ method: method.toUpperCase(), template, operationId: operation.operationId });
  }
}
const byId = new Map(operations.map(value => [value.operationId, value]));
const routePattern = /\{\s*id:\s*"([^"]+)"[\s\S]*?expectedOperationIds:\s*\[([^\]]*)\][\s\S]*?load:\s*\(\)\s*=>\s*import\("([^"]+)"\)\s*\}/g;
const routes = [];
let match;
while ((match = routePattern.exec(routesText))) {
  const explicit = [...match[2].matchAll(/"([^"]+)"/g)].map(value => value[1]);
  routes.push({ routeId: match[1], explicit, componentImport: match[3] });
}
const routeType = routesText.match(/export type BzaRouteId\s*=([\s\S]*?);\n\nexport interface BzaRoute/);
if (!routeType) throw new Error("BZA route id type contract missing");
const declaredRouteIds = [...routeType[1].matchAll(/"([^"]+)"/g)].map(value => value[1]);
const actualRouteIds = routes.map(value => value.routeId);
const duplicates = actualRouteIds.filter((value, index, all) => all.indexOf(value) !== index);
if (duplicates.length) throw new Error(`BZA duplicate route id: ${[...new Set(duplicates)].join(", ")}`);
const missingFromRegistry = declaredRouteIds.filter(value => !actualRouteIds.includes(value));
const missingFromType = actualRouteIds.filter(value => !declaredRouteIds.includes(value));
if (missingFromRegistry.length || missingFromType.length) {
  throw new Error(`BZA route registry/type drift: missingRegistry=${missingFromRegistry.join(",") || "-"} missingType=${missingFromType.join(",") || "-"}`);
}

function resolveLocalImport(fromFile, request) {
  if (!request.startsWith(".")) return null;
  const base = path.resolve(path.dirname(fromFile), request);
  for (const candidate of [base, `${base}.ts`, `${base}.vue`, `${base}.js`, `${base}.mjs`, path.join(base, "index.ts")]) {
    if (fs.existsSync(candidate) && fs.statSync(candidate).isFile()) return candidate;
  }
  return null;
}
function collectSource(entry, visited = new Set()) {
  const absolute = path.resolve(entry);
  if (visited.has(absolute) || !fs.existsSync(absolute)) return "";
  visited.add(absolute);
  const source = fs.readFileSync(absolute, "utf8");
  let combined = `\n/* ${path.relative(root, absolute)} */\n${source}`;
  const imports = [...source.matchAll(/(?:import|export)\s+(?:[\s\S]*?\s+from\s+)?["']([^"']+)["']/g)].map(value => value[1]);
  for (const request of imports) {
    const child = resolveLocalImport(absolute, request);
    if (child && child.startsWith(path.join(root, "src")) && !isTransportBoundary(child)) combined += collectSource(child, visited);
  }
  return combined;
}
function isTransportBoundary(file) {
  const relative = path.relative(root, file).replaceAll("\\", "/");
  return relative.startsWith("src/generated/")
    || [
      "src/shared/cpfApi.ts",
      "src/shared/orval-mutator.ts",
      "src/shared/queryClient.ts",
      "src/features/auth/session.ts"
    ].includes(relative);
}
function normalizedCandidate(raw) {
  return raw.replace(/\$\{[^}]+\}/g, "{dynamic}").replace(/[?#].*$/, "");
}
function templateMatches(template, candidate) {
  const expected = template.split("/").filter(Boolean);
  const actual = candidate.split("/").filter(Boolean);
  if (expected.length !== actual.length) return false;
  return expected.every((segment, index) => /^\{[^}]+\}$/.test(segment) || actual[index] === "{dynamic}" || segment === actual[index]);
}
function inferMethod(context) {
  if (/bzaQuery\s*</.test(context) || /bzaQuery\s*\(/.test(context)) return "GET";
  const direct = context.match(/bzaMutation[\s\S]{0,240}?["'](POST|PUT|PATCH|DELETE)["']/i);
  if (direct) return direct[1].toUpperCase();
  const option = context.match(/method\s*:\s*["'](GET|POST|PUT|PATCH|DELETE)["']/i);
  return option ? option[1].toUpperCase() : null;
}
function discoverOperations(source) {
  const found = new Set();
  const apiLiteral = /([`"'])(\/api\/bza[^`"']*)\1/g;
  let urlMatch;
  while ((urlMatch = apiLiteral.exec(source))) {
    const candidate = normalizedCandidate(urlMatch[2]);
    const context = source.slice(Math.max(0, urlMatch.index - 240), Math.min(source.length, urlMatch.index + 360));
    const inferred = inferMethod(context);
    const candidates = operations.filter(operation => templateMatches(operation.template, candidate) && (!inferred || operation.method === inferred));
    if (candidates.length === 1) found.add(candidates[0].operationId);
  }
  return [...found].sort();
}

const failures = [];
const contracts = {};
for (const route of routes) {
  const component = path.resolve(path.dirname(routesPath), route.componentImport);
  const actualComponent = [component, `${component}.vue`, `${component}.ts`].find(value => fs.existsSync(value));
  if (!actualComponent) throw new Error(`Route component missing: ${route.routeId} ${route.componentImport}`);
  const source = collectSource(actualComponent);
  const discovered = discoverOperations(source);
  for (const operationId of route.explicit) {
    if (!byId.has(operationId)) failures.push(`${route.routeId}: expected operation missing from OpenAPI: ${operationId}`);
  }
  const privilegedLiterals = [...source.matchAll(/[`"']\/api\/bza\b/g)].length;
  if (privilegedLiterals > 0 && discovered.length === 0 && route.explicit.length === 0) {
    failures.push(`${route.routeId}: privileged API consumer has no route operation contract`);
  }
  contracts[route.routeId] = [...new Set([...route.explicit, ...discovered])].sort();
}
if (failures.length) throw new Error(failures.join("\n"));
fs.mkdirSync(generatedDir, { recursive: true });
const rows = Object.entries(contracts).map(([routeId, operationIds]) => `  ${JSON.stringify(routeId)}: ${JSON.stringify(operationIds)}`).join(",\n");
const output = `// Generated from BZA route registry, component consumers and canonical runtime OpenAPI.\nexport const bzaRouteOperationContract = {\n${rows}\n} as const;\nexport type BzaRouteOperationContract = typeof bzaRouteOperationContract;\n`;
fs.writeFileSync(path.join(generatedDir, "bza-route-operation-contract.ts"), output, "utf8");
const covered = Object.values(contracts).filter(values => values.length > 0).length;
console.log(`[CPF][FRONTEND][PASS] route operation contract routes=${routes.length} operationRoutes=${covered}`);
