import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const openApiPath = path.resolve(root, process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
const mutationMethods = new Set(["post", "put", "patch", "delete"]);

for (const required of [openApiPath, clientPath]) {
  if (!fs.existsSync(required)) throw new Error(`Required contract input missing: ${required}`);
}
const openapi = JSON.parse(fs.readFileSync(openApiPath, "utf8"));
const client = fs.readFileSync(clientPath, "utf8");
const failures = [];
const generatedRoot = path.resolve(root, "src/generated");
for (const file of fs.readdirSync(path.dirname(clientPath), { withFileTypes: true })) {
  // Full recursive generated whitespace verification is delegated to the deterministic normalizer.
}
const generatedFiles = [];
function collectGenerated(directory) {
  if (!fs.existsSync(directory)) return;
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) collectGenerated(absolute);
    else if (entry.isFile() && /\.(?:ts|tsx|js|mjs|json)$/.test(entry.name)) generatedFiles.push(absolute);
  }
}
collectGenerated(generatedRoot);
for (const file of generatedFiles) {
  const text = fs.readFileSync(file, "utf8");
  if (/[ \t]+$/m.test(text) || !text.endsWith("\n") || /\n\n+$/.test(text)) {
    failures.push(`${path.relative(root, file).split(path.sep).join("/")}: generated whitespace drift`);
  }
}
let verifiedMutations = 0;
let verifiedOperations = 0;

function pascal(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
}
function compact(value) {
  return value.replace(/\s+/g, " ").replace(/\s*([{}:;,])\s*/g, "$1").trim();
}
function findBalancedEnd(source, start, open, close) {
  let depth = 0, quote = "", escaped = false;
  for (let index = start; index < source.length; index += 1) {
    const char = source[index];
    if (quote) {
      if (escaped) escaped = false;
      else if (char === "\\") escaped = true;
      else if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"' || char === "`") { quote = char; continue; }
    if (char === open) depth += 1;
    else if (char === close) {
      depth -= 1;
      if (depth === 0) return index;
    }
  }
  throw new Error(`Unbalanced ${open}${close} sequence at ${start}`);
}
function genericArguments(source, tokenStart) {
  const start = source.indexOf("<", tokenStart);
  if (start < 0) throw new Error(`Generic start missing near ${tokenStart}`);
  const end = findBalancedEnd(source, start, "<", ">");
  const body = source.slice(start + 1, end);
  const segments = [];
  let segmentStart = 0, angle = 0, brace = 0, paren = 0, bracket = 0, quote = "", escaped = false;
  for (let index = 0; index < body.length; index += 1) {
    const char = body[index];
    if (quote) {
      if (escaped) escaped = false;
      else if (char === "\\") escaped = true;
      else if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"' || char === "`") { quote = char; continue; }
    if (char === "<") angle += 1; else if (char === ">") angle -= 1;
    else if (char === "{") brace += 1; else if (char === "}") brace -= 1;
    else if (char === "(") paren += 1; else if (char === ")") paren -= 1;
    else if (char === "[") bracket += 1; else if (char === "]") bracket -= 1;
    else if (char === "," && angle === 0 && brace === 0 && paren === 0 && bracket === 0) {
      segments.push(body.slice(segmentStart, index).trim());
      segmentStart = index + 1;
    }
  }
  segments.push(body.slice(segmentStart).trim());
  return segments;
}
function operationFunction(operationId) {
  const token = `export const ${operationId} = async (`;
  const start = client.indexOf(token);
  if (start < 0) return null;
  const paren = client.indexOf("(", start + token.length - 1);
  const end = findBalancedEnd(client, paren, "(", ")");
  return { start, end, parameters: client.slice(paren + 1, end), tail: client.slice(end, Math.min(client.length, end + 1800)) };
}
function splitTopLevelParameters(parameters) {
  let start = 0, angle = 0, brace = 0, paren = 0, bracket = 0, quote = "", escaped = false;
  const values = [];
  for (let index = 0; index < parameters.length; index += 1) {
    const char = parameters[index];
    if (quote) {
      if (escaped) escaped = false;
      else if (char === "\\") escaped = true;
      else if (char === quote) quote = "";
      continue;
    }
    if (char === "'" || char === '"' || char === "`") { quote = char; continue; }
    if (char === "<") angle += 1; else if (char === ">") angle -= 1;
    else if (char === "{") brace += 1; else if (char === "}") brace -= 1;
    else if (char === "(") paren += 1; else if (char === ")") paren -= 1;
    else if (char === "[") bracket += 1; else if (char === "]") bracket -= 1;
    else if (char === "," && angle === 0 && brace === 0 && paren === 0 && bracket === 0) {
      values.push(parameters.slice(start, index)); start = index + 1;
    }
  }
  values.push(parameters.slice(start));
  return values;
}
function operationSourceBlock(operationId) {
  const token = `export const get${pascal(operationId)}Url`;
  const start = client.indexOf(token);
  if (start < 0) return "";
  const nextResponse = client.indexOf("\nexport type ", start + token.length);
  return client.slice(start, nextResponse < 0 ? client.length : nextResponse);
}
function parameterDeclarations(parameters) {
  const result = new Map();
  for (const value of splitTopLevelParameters(parameters)) {
    const match = value.trim().match(/^([A-Za-z_$][\w$]*)(\?)?\s*:\s*([\s\S]+)$/);
    if (match) result.set(match[1], { optional: Boolean(match[2]), type: match[3].trim() });
  }
  return result;
}
function schemaType(schema) {
  if (!schema) return "unknown";
  if (schema.$ref) return schema.$ref.split("/").pop();
  if (Array.isArray(schema.enum) && schema.enum.length) return schema.enum.map(value => JSON.stringify(value)).join(" | ");
  if (schema.type === "integer" || schema.type === "number") return "number";
  if (schema.type === "boolean") return "boolean";
  if (schema.type === "string") return "string";
  if (schema.type === "array") return `Array<${schemaType(schema.items)}>`;
  return "unknown";
}
function renderedPath(route, parameters) {
  let value = route;
  for (const parameter of parameters) {
    value = value.replace(`{${parameter.name}}`, `\${encodeURIComponent(String(${parameter.name}))}`);
  }
  return `\`${value}\``;
}

function requestBodyContract(operation) {
  const content = operation.requestBody?.content || {};
  if (content["application/json"]) return { mediaType: "application/json", schema: content["application/json"].schema };
  const first = Object.entries(content)[0];
  return first ? { mediaType: first[0], schema: first[1]?.schema } : undefined;
}

function resolveParameters(pathItem, operation) {
  return [...(pathItem.parameters || []), ...(operation.parameters || [])].map(parameter => {
    if (parameter?.$ref) {
      const name = parameter.$ref.split("/").pop();
      return openapi.components?.parameters?.[name] || parameter;
    }
    return parameter;
  });
}
function mutationBlock(operationId) {
  const token = `export const get${pascal(operationId)}MutationOptions`;
  const start = client.indexOf(token);
  if (start < 0) return null;
  const nextResponse = client.indexOf("\nexport type ", start + token.length);
  return client.slice(start, nextResponse < 0 ? client.length : nextResponse);
}

for (const [route, pathItem] of Object.entries(openapi.paths || {})) {
  if (!pathItem || typeof pathItem !== "object") continue;
  for (const [method, operation] of Object.entries(pathItem)) {
    if (!operation || typeof operation !== "object" || !operation.operationId) continue;
    const operationId = operation.operationId;
    const fn = operationFunction(operationId);
    if (!fn) {
      failures.push(`${operationId}: generated operation function missing`);
      continue;
    }
    verifiedOperations += 1;
    const declarations = parameterDeclarations(fn.parameters);
    const names = new Set(declarations.keys());
    const sourceBlock = operationSourceBlock(operationId);
    const parameters = resolveParameters(pathItem, operation);
    const declaredPathParameters = parameters.filter(value => value.in === "path");
    const declaredPathByName = new Map(declaredPathParameters.map(value => [value.name, value]));
    const templatePathNames = [...route.matchAll(/\{([^{}]+)\}/g)].map(match => match[1]);
    const pathParameters = templatePathNames.map(name => declaredPathByName.get(name) || { name, in: "path", required: true, schema: { type: "string" } });
    for (const name of templatePathNames) {
      const declared = declaredPathByName.get(name);
      if (!declared) failures.push(`${operationId}: OpenAPI path parameter declaration missing: ${name}`);
      else if (declared.required !== true) failures.push(`${operationId}: OpenAPI path parameter must be required: ${name}`);
    }
    for (const parameter of declaredPathParameters) {
      if (!templatePathNames.includes(parameter.name)) failures.push(`${operationId}: declared path parameter is absent from route template: ${parameter.name}`);
    }
    const urlLiteral = sourceBlock.match(/(?:return\s+|=>\s*)(`[^`]*`)/)?.[1]
      || sourceBlock.match(/const\s+base\s*=\s*(`[^`]*`)/)?.[1];
    const expectedUrlLiteral = renderedPath(route, pathParameters);
    if (!urlLiteral) failures.push(`${operationId}: generated URL template missing`);
    else if (compact(urlLiteral) !== compact(expectedUrlLiteral)) {
      failures.push(`${operationId}: generated URL mismatch expected=${expectedUrlLiteral} actual=${urlLiteral}`);
    }
    const queryOrHeader = parameters.filter(value => value.in === "query" || value.in === "header");
    for (const parameter of pathParameters) {
      if (!names.has(parameter.name)) failures.push(`${operationId}: path parameter missing from function signature: ${parameter.name}`);
    }
    if (queryOrHeader.length && !names.has("params")) {
      failures.push(`${operationId}: query/header contract exists but generated params argument is missing`);
    }
    const hasBody = Boolean(operation.requestBody);
    const generatedBodyName = fn.tail.match(/\bdata\s*:\s*([A-Za-z_$][\w$]*)/)?.[1];
    const functionHasBody = names.has("data") || Boolean(generatedBodyName && names.has(generatedBodyName));
    if (hasBody && !functionHasBody) failures.push(`${operationId}: requestBody exists but generated data argument is missing`);
    if (!hasBody && functionHasBody) failures.push(`${operationId}: generated data argument exists without OpenAPI requestBody`);

    for (const parameter of pathParameters) {
      const declaration = declarations.get(parameter.name);
      const expectedType = schemaType(parameter.schema);
      if (declaration && compact(declaration.type) !== compact(expectedType)) {
        failures.push(`${operationId}: path parameter type mismatch ${parameter.name} generated=${compact(declaration.type)} openapi=${compact(expectedType)}`);
      }
    }
    if (queryOrHeader.length) {
      const paramsDeclaration = declarations.get("params");
      const expectedParamsType = `${pascal(operationId)}Params`;
      if (paramsDeclaration && !compact(paramsDeclaration.type).includes(expectedParamsType)) {
        failures.push(`${operationId}: params type mismatch generated=${compact(paramsDeclaration.type)} expected=${expectedParamsType}`);
      }
      if (paramsDeclaration && queryOrHeader.some(value => value.required) && paramsDeclaration.optional) {
        failures.push(`${operationId}: required query/header contract exposed as optional params`);
      }
      for (const parameter of queryOrHeader) {
        const escaped = parameter.name.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
        const serialized = parameter.in === "header"
          ? new RegExp(`[\"']${escaped}[\"']\\s*:`).test(sourceBlock)
          : new RegExp(`(?:append|set)\\(\\s*[\"']${escaped}[\"']|\\b${escaped}\\s*:`).test(sourceBlock);
        if (!serialized) failures.push(`${operationId}: generated request does not serialize ${parameter.in} parameter ${parameter.name}`);
      }
    }
    const bodyContract = requestBodyContract(operation);
    const bodySchema = bodyContract?.schema;
    const schemaBodyType = bodySchema?.$ref ? bodySchema.$ref.split("/").pop() : undefined;
    if (hasBody && !schemaBodyType && openapi["x-cpf-export-origin"] === "CONTROLLER_SOURCE_PRE_RUNTIME") {
      failures.push(`${operationId}: pre-runtime requestBody must use a named schema ref`);
    }
    const expectedBodyType = bodyContract?.mediaType === "multipart/form-data" ? "FormData" : schemaBodyType;
    if (expectedBodyType) {
      const matchingBody = [...declarations.entries()].find(([name, declaration]) => name !== "options" && compact(declaration.type) === compact(expectedBodyType));
      if (!matchingBody) failures.push(`${operationId}: request body type mismatch expected=${expectedBodyType} signature=${compact(fn.parameters)}`);
    }

    if (!mutationMethods.has(method.toLowerCase())) continue;
    const block = mutationBlock(operationId);
    if (!block) {
      failures.push(`${operationId}: mutation options block missing`);
      continue;
    }
    const fnToken = block.indexOf("MutationFunction<");
    if (fnToken < 0) {
      failures.push(`${operationId}: MutationFunction type missing`);
      continue;
    }
    const canonical = genericArguments(block, fnToken)[1];
    const variableTypes = [];
    for (const token of ["UseMutationOptions<", "UseMutationReturnType<"]) {
      let cursor = 0;
      while (true) {
        const position = block.indexOf(token, cursor);
        if (position < 0) break;
        const args = genericArguments(block, position);
        if (args.length >= 3) variableTypes.push({ token, type: args[2] });
        cursor = position + token.length;
      }
    }
    for (const entry of variableTypes) {
      if (compact(entry.type) !== compact(canonical)) {
        failures.push(`${operationId}: ${entry.token} variables=${compact(entry.type)} mutationFn=${compact(canonical)}`);
      }
    }
    const canonicalHasData = /\bdata\s*:/.test(canonical);
    if (hasBody !== canonicalHasData) {
      failures.push(`${operationId}: mutation variable body mismatch requestBody=${hasBody} variables=${compact(canonical)}`);
    }
    if (expectedBodyType && !new RegExp(`\\bdata\\s*:\\s*${expectedBodyType}\\b`).test(canonical)) {
      failures.push(`${operationId}: mutation body type mismatch expected=${expectedBodyType} variables=${compact(canonical)}`);
    }
    if (queryOrHeader.length && !new RegExp(`\\bparams\\??\\s*:\\s*${pascal(operationId)}Params\\b`).test(canonical)) {
      failures.push(`${operationId}: mutation params type mismatch expected=${pascal(operationId)}Params variables=${compact(canonical)}`);
    }
    for (const parameter of pathParameters) {
      if (!new RegExp(`\\b${parameter.name}\\s*:`).test(canonical)) {
        failures.push(`${operationId}: mutation variables missing path parameter ${parameter.name}`);
      }
    }
    if (queryOrHeader.length && !/\bparams\??\s*:/.test(canonical)) {
      failures.push(`${operationId}: mutation variables missing params for query/header contract`);
    }
    verifiedMutations += 1;
  }
}

for (const schemaName of ["AdmReliabilityActionRequest", "CommonMessageRequest", "AdmNotificationRuleRequest", "AdmNotificationTestSendRequest"]) {
  const schema = openapi.components?.schemas?.[schemaName];
  if (schema?.properties?.requestUser) failures.push(`${schemaName}: public OpenAPI must not expose requestUser actor override`);
  const modelPath = path.resolve(root, `src/generated/orval/model/${schemaName.charAt(0).toLowerCase()}${schemaName.slice(1)}.ts`);
  if (fs.existsSync(modelPath) && /\brequestUser\??\s*:/.test(fs.readFileSync(modelPath, "utf8"))) {
    failures.push(`${schemaName}: generated model exposes requestUser actor override`);
  }
}

if (!verifiedOperations) failures.push("No generated operations were verified");
if (!verifiedMutations) failures.push("No generated mutations were verified");
if (failures.length) {
  console.error(`[CPF][FRONTEND][FAIL] Orval operation contract failures=${failures.length}`);
  for (const failure of failures.slice(0, 100)) console.error(` - ${failure}`);
  process.exit(1);
}
console.log(`[CPF][FRONTEND][PASS] Orval operation contracts operations=${verifiedOperations} mutations=${verifiedMutations}`);
