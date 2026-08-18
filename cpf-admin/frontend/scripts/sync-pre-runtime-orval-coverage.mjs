import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const openApiPath = path.resolve(root, process.env.CPF_OPENAPI_FILE || "openapi/cpf-openapi.json");
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
const modelDir = path.resolve(root, "src/generated/orval/model");
const spec = JSON.parse(fs.readFileSync(openApiPath, "utf8"));
const preRuntime = spec["x-cpf-export-origin"] === "CONTROLLER_SOURCE_PRE_RUNTIME";
let source = preRuntime ? "" : fs.readFileSync(clientPath, "utf8");
source = source.replace(/\n*\/\/ CPF PRE-RUNTIME FALLBACK START [^\n]+\n[\s\S]*?\/\/ CPF PRE-RUNTIME FALLBACK END [^\n]+\n?/g, "\n");
if (preRuntime) {
  fs.rmSync(modelDir, { recursive: true, force: true });
  source = `/**
 * Generated from the CPF controller-source pre-runtime OpenAPI contract.
 * Runtime OpenAPI generation must replace this deterministic compatibility client.
 */
import { useMutation, useQuery } from '@tanstack/vue-query';
import type { DataTag, MutationFunction, QueryClient, QueryFunction, QueryKey, UseMutationOptions, UseMutationReturnType, UseQueryOptions, UseQueryReturnType } from '@tanstack/vue-query';
import { computed, toValue, unref } from 'vue';
import type { MaybeRefOrGetter } from 'vue';
import type { CpfControllerSourceResponse } from './model';
import { cpfOrvalRequest } from '../../shared/orval-mutator';
import type { CpfOrvalGeneratedRequestOptions } from '../../shared/orval-mutator';
type SecondParameter<T extends (...args: never) => unknown> = CpfOrvalGeneratedRequestOptions;
`;
}
const methods = new Set(["get", "post", "put", "patch", "delete"]);

function pascal(value) { return value.charAt(0).toUpperCase() + value.slice(1); }
function camel(value) { return value.charAt(0).toLowerCase() + value.slice(1); }
function compactIdentifier(value) { return value.replace(/[^A-Za-z0-9_$]/g, "_"); }
function quote(value) { return JSON.stringify(value); }
function propertyName(value) { return /^[A-Za-z_$][\w$]*$/.test(value) ? value : quote(value); }
function propertyAccess(base, value, optional = false) {
  const prefix = optional ? `${base}?` : base;
  return /^[A-Za-z_$][\w$]*$/.test(value) ? `${prefix}.${value}` : `${prefix}[${quote(value)}]`;
}
function memberAccess(object, value, optional = false) {
  if (/^[A-Za-z_$][\w$]*$/.test(value)) return `${object}${optional ? "?." : "."}${value}`;
  return `${object}${optional ? "?." : ""}[${quote(value)}]`;
}
function refName(schema) {
  const ref = schema?.$ref;
  return typeof ref === "string" ? ref.split("/").pop() : undefined;
}
function schemaRefs(schema, found = new Set()) {
  if (Array.isArray(schema)) {
    for (const value of schema) schemaRefs(value, found);
  } else if (schema && typeof schema === "object") {
    const ref = refName(schema);
    if (ref) found.add(ref);
    for (const value of Object.values(schema)) schemaRefs(value, found);
  }
  return found;
}
function modelImports(typeName, schema) {
  return [...schemaRefs(schema)].filter(name => name !== typeName).sort();
}
function resolveParameter(parameter) {
  if (!parameter?.$ref) return parameter;
  const name = parameter.$ref.split("/").pop();
  const resolved = spec.components?.parameters?.[name];
  if (!resolved) throw new Error(`OpenAPI parameter ref missing: ${parameter.$ref}`);
  return resolved;
}
function resolveParameters(pathItem, operation) {
  return [...(pathItem.parameters || []), ...(operation.parameters || [])].map(resolveParameter);
}
function schemaType(schema) {
  if (!schema) return "unknown";
  const ref = refName(schema);
  if (ref) return ref;
  if (Array.isArray(schema.enum) && schema.enum.length) return schema.enum.map(quote).join(" | ");
  if (Array.isArray(schema.oneOf) && schema.oneOf.length) return schema.oneOf.map(schemaType).join(" | ");
  if (Array.isArray(schema.anyOf) && schema.anyOf.length) return schema.anyOf.map(schemaType).join(" | ");
  switch (schema.type) {
    case "string": return schema.format === "binary" ? "Blob" : "string";
    case "integer":
    case "number": return "number";
    case "boolean": return "boolean";
    case "array": return `Array<${schemaType(schema.items)}>`;
    case "object": {
      if (schema.properties) {
        const required = new Set(schema.required || []);
        const fields = Object.entries(schema.properties).map(([name, value]) => `${propertyName(name)}${required.has(name) ? "" : "?"}: ${schemaType(value)}`).join("; ");
        return `{ ${fields} }`;
      }
      if (schema.additionalProperties && typeof schema.additionalProperties === "object") return `Record<string, ${schemaType(schema.additionalProperties)}>`;
      if (schema.additionalProperties === true) return "Record<string, unknown>";
      return "Record<string, never>";
    }
    default: return "unknown";
  }
}
function modelFileName(typeName) { return `${camel(typeName)}.ts`; }
function writeSchemaModel(typeName, schemaOverride = undefined) {
  const schema = schemaOverride || spec.components?.schemas?.[typeName];
  if (!schema) throw new Error(`OpenAPI schema missing: ${typeName}`);
  fs.mkdirSync(modelDir, { recursive: true });
  const description = typeof schema.description === "string" ? schema.description.replaceAll("*/", "* /") : "Generated from the CPF pre-runtime OpenAPI contract.";
  const imports = modelImports(typeName, schema);
  const importBlock = imports.length
    ? `${imports.map(name => `import type { ${name} } from './${camel(name)}';`).join("\n")}\n\n`
    : "";
  let text;
  if (schema.type === "object" && schema.properties) {
    const required = new Set(schema.required || []);
    const fields = Object.entries(schema.properties).map(([name, value]) => `  ${propertyName(name)}${required.has(name) ? "" : "?"}: ${schemaType(value)};`).join("\n");
    text = `${importBlock}/** ${description} */\nexport interface ${typeName} {\n${fields}\n}\n`;
  } else {
    text = `${importBlock}/** ${description} */\nexport type ${typeName} = ${schemaType(schema)};\n`;
  }
  fs.writeFileSync(path.join(modelDir, modelFileName(typeName)), text);
  return typeName;
}

function writeParamsModel(typeName, parameters) {
  fs.mkdirSync(modelDir, { recursive: true });
  const imports = [...new Set(parameters.flatMap(parameter => [...schemaRefs(parameter.schema)]))]
    .filter(name => name !== typeName).sort();
  const importBlock = imports.length
    ? `${imports.map(name => `import type { ${name} } from './${camel(name)}';`).join("\n")}\n\n`
    : "";
  const fields = parameters.map(parameter => {
    const optional = parameter.required ? "" : "?";
    return `  ${propertyName(parameter.name)}${optional}: ${schemaType(parameter.schema)};`;
  }).join("\n");
  fs.writeFileSync(path.join(modelDir, modelFileName(typeName)), `${importBlock}/** Generated from OpenAPI query/header parameters. */\nexport interface ${typeName} {\n${fields}\n}\n`);
  return typeName;
}

function ensureModelIndex(typeNames) {
  const indexPath = path.join(modelDir, "index.ts");
  const existing = fs.existsSync(indexPath) ? fs.readFileSync(indexPath, "utf8") : "";
  const exports = new Set([...existing.matchAll(/export \* from ['"]\.\/([^'"]+)['"]/g)].map(match => match[1]));
  for (const typeName of typeNames) exports.add(camel(typeName));
  const header = existing.split(/(?=export \* from)/)[0] || "/** Generated CPF OpenAPI models. */\n\n";
  const text = `${header.trimEnd()}\n\n${[...exports].sort().map(name => `export * from './${name}';`).join("\n")}\n`;
  fs.writeFileSync(indexPath, text);
}
function ensureClientImports(typeNames) {
  if (!typeNames.size) return;
  const match = source.match(/import type \{([^{}]*?)\} from '\.\/model';/);
  if (!match) throw new Error("Generated client model import block missing");
  const names = new Set(match[1].split(",").map(value => value.trim()).filter(Boolean));
  for (const name of typeNames) names.add(name);
  const replacement = `import type {\n  ${[...names].sort().join(",\n  ")}\n} from './model';`;
  source = source.replace(match[0], replacement);
}
function responseTypes(id) {
  return `export type ${id}Response200 = {\n  data: CpfControllerSourceResponse\n  status: 200\n}\n\nexport type ${id}ResponseSuccess = (${id}Response200) & {\n  headers: Headers;\n};\n\nexport type ${id}Response = (${id}ResponseSuccess)\n`;
}
function pathParameters(route, parameters) {
  return [...route.matchAll(/\{([^{}]+)\}/g)].map(match => {
    const declared = parameters.find(value => value?.name === match[1] && value?.in === "path");
    if (!declared) throw new Error(`Path parameter contract missing: ${route} ${match[1]}`);
    return { name: match[1], type: schemaType(declared.schema) };
  });
}
function renderedPath(route, params) {
  let value = route;
  for (const param of params) value = value.replace(`{${param.name}}`, `\${encodeURIComponent(String(${param.name}))}`);
  return `\`${value}\``;
}
function synchronizeExistingUrl(operationId, route, pathParams) {
  const token = `export const get${pascal(operationId)}Url`;
  const start = source.indexOf(token);
  if (start < 0) return false;
  const operationToken = `export const ${operationId} = async (`;
  const end = source.indexOf(operationToken, start + token.length);
  if (end < 0) throw new Error(`${operationId}: generated operation function missing after URL builder`);
  const block = source.slice(start, end);
  const expected = renderedPath(route, pathParams);
  let updated;
  if (/return\s+`[^`]*`/.test(block)) updated = block.replace(/return\s+`[^`]*`/, `return ${expected}`);
  else if (/=>\s*`[^`]*`\s*;?/.test(block)) updated = block.replace(/=>\s*`[^`]*`\s*;?/, `=> ${expected};`);
  else throw new Error(`${operationId}: generated URL template is not recognized`);
  if (updated === block) return false;
  source = source.slice(0, start) + updated + source.slice(end);
  return true;
}

function requestOptionLines(parameters, hasBody, bodyMediaType = "application/json") {
  const queryParameters = parameters.filter(value => value.in === "query");
  const headerParameters = parameters.filter(value => value.in === "header");
  const paramsOptional = parameters.length > 0 && parameters.every(value => !value.required);
  const lines = [];
  if (queryParameters.length) {
    const fields = queryParameters.map(value => `${propertyName(value.name)}: ${memberAccess("params", value.name, paramsOptional)}`).join(", ");
    lines.push(`    params: { ${fields} },`);
  }
  if (hasBody || headerParameters.length) {
    const headers = [];
    if (hasBody && bodyMediaType !== "multipart/form-data") headers.push(`'Content-Type': '${bodyMediaType}'`);
    for (const value of headerParameters) {
      const access = memberAccess("params", value.name, paramsOptional || !value.required);
      if (value.required) headers.push(`${quote(value.name)}: ${access}`);
      else headers.push(`...(${access} !== undefined ? { ${quote(value.name)}: ${access} } : {})`);
    }
    headers.push(`...options?.headers`);
    lines.push(`    headers: { ${headers.join(", ")} },`);
  }
  if (hasBody) lines.push("    data,");
  return lines.join("\n");
}

function getBlock(id, route, pathParams, contractParams, paramsType) {
  const symbol = pascal(id);
  const pathArgs = pathParams.map(value => `${value.name}: ${value.type}`);
  const paramsRequired = contractParams.some(value => value.required);
  const paramsArg = contractParams.length ? `params${paramsRequired ? "" : "?"}: ${paramsType}` : null;
  const args = [...pathArgs, ...(paramsArg ? [paramsArg] : [])];
  const reactiveArgs = [...pathParams.map(value => `${value.name}: MaybeRefOrGetter<${value.type}>`), ...(paramsArg ? [`params${paramsRequired ? "" : "?"}: MaybeRefOrGetter<${paramsType}>`] : [])];
  const urlArgs = pathParams.map(value => value.name).join(", ");
  const unwrapped = [...pathParams.map(value => `toValue(${value.name})`), ...(paramsArg ? ["toValue(params)"] : [])];
  const requestOptions = requestOptionLines(contractParams, false);
  const callArgs = [...unwrapped, `{ signal, ...requestOptions }`].join(", ");
  const optionCall = [...reactiveArgs.map(value => value.split(":")[0].replace("?", "")), "options"].join(", ");
  const keyValues = route.split("/").filter(Boolean).map(segment => segment.startsWith("{") ? segment.slice(1, -1) : quote(segment));
  if (paramsArg) keyValues.push("toValue(params)");
  const requiredChecks = pathParams.map(value => `toValue(${value.name}) !== null && toValue(${value.name}) !== undefined`);
  if (paramsArg && paramsRequired) requiredChecks.push("toValue(params) !== null && toValue(params) !== undefined");
  const enabled = requiredChecks.length ? `, enabled: computed(() => ${requiredChecks.join(" && ")})` : "";
  return `${responseTypes(id)}\nexport const get${symbol}Url = (${pathArgs.join(", ")}) => ${renderedPath(route, pathParams)};\n\nexport const ${id} = async (${[...args, `options?: CpfOrvalGeneratedRequestOptions`].join(", ")}): Promise<${id}Response> => {\n  return cpfOrvalRequest<${id}Response>(get${symbol}Url(${urlArgs}), {\n    ...options,\n    method: 'GET',\n${requestOptions}\n  });\n};\n\nexport const get${symbol}QueryKey = (${reactiveArgs.join(", ")}) => [${keyValues.join(", ")}] as const;\n\nexport const get${symbol}QueryOptions = <TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(\n  ${reactiveArgs.length ? reactiveArgs.join(", ") + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }\n) => {\n  const { query: queryOptions, request: requestOptions } = options ?? {};\n  const queryKey = get${symbol}QueryKey(${unwrapped.join(", ")});\n  const queryFn: QueryFunction<Awaited<ReturnType<typeof ${id}>>> = ({ signal }) => ${id}(${callArgs});\n  return { queryKey, queryFn${enabled}, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>;\n};\n\nexport type ${symbol}QueryResult = NonNullable<Awaited<ReturnType<typeof ${id}>>>;\nexport type ${symbol}QueryError = unknown;\n\nexport function use${symbol}<TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(\n  ${reactiveArgs.length ? reactiveArgs.join(", ") + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },\n  queryClient?: QueryClient\n): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {\n  const queryOptions = get${symbol}QueryOptions(${optionCall});\n  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };\n  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;\n  return query;\n}\n`;
}
function mutationBlock(id, method, route, pathParams, contractParams, paramsType, bodyType, bodyMediaType = "application/json") {
  const symbol = pascal(id);
  const pathArgs = pathParams.map(value => `${value.name}: ${value.type}`);
  const paramsRequired = contractParams.some(value => value.required);
  const paramsArg = contractParams.length ? `params${paramsRequired ? "" : "?"}: ${paramsType}` : null;
  const fnArgs = [...pathArgs, ...(bodyType ? [`data: ${bodyType}`] : []), ...(paramsArg ? [paramsArg] : []), "options?: CpfOrvalGeneratedRequestOptions"];
  const variableParts = [...pathParams.map(value => `${value.name}: ${value.type}`), ...(bodyType ? [`data: ${bodyType}`] : []), ...(paramsArg ? [`params${paramsRequired ? "" : "?"}: ${paramsType}`] : [])];
  const variableType = variableParts.length ? `{${variableParts.join("; ")}}` : "void";
  const names = [...pathParams.map(value => value.name), ...(bodyType ? ["data"] : []), ...(paramsArg ? ["params"] : [])];
  const callArgs = [...names, "requestOptions"].join(", ");
  const destructure = names.length ? `const { ${names.join(", ")} } = props;` : "";
  const mutationArgs = variableType === "void" ? "()" : "(props)";
  const requestOptions = requestOptionLines(contractParams, Boolean(bodyType), bodyMediaType);
  return `${responseTypes(id)}\nexport const get${symbol}Url = (${pathArgs.join(", ")}) => ${renderedPath(route, pathParams)};\n\nexport const ${id} = async (${fnArgs.join(", ")}): Promise<${id}Response> => {\n  return cpfOrvalRequest<${id}Response>(get${symbol}Url(${pathParams.map(value => value.name).join(", ")}), {\n    ...options,\n    method: '${method.toUpperCase()}',\n${requestOptions}\n  });\n};\n\nexport const get${symbol}MutationOptions = <TError = unknown, TContext = unknown>(\n  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }\n): UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext> => {\n  const mutationKey = ['${id}'];\n  const { mutation: mutationOptions, request: requestOptions } = options\n    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey\n      ? options\n      : { ...options, mutation: { ...options.mutation, mutationKey } }\n    : { mutation: { mutationKey }, request: undefined };\n  const mutationFn: MutationFunction<Awaited<ReturnType<typeof ${id}>>, ${variableType}> = ${mutationArgs} => {\n    ${destructure}\n    return ${id}(${callArgs});\n  };\n  return { mutationFn, ...mutationOptions };\n};\n\nexport type ${symbol}MutationResult = NonNullable<Awaited<ReturnType<typeof ${id}>>>;\nexport type ${symbol}MutationBody = ${bodyType || "never"};\nexport type ${symbol}MutationError = unknown;\n\nexport const use${symbol} = <TError = unknown, TContext = unknown>(\n  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },\n  queryClient?: QueryClient\n): UseMutationReturnType<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext> => {\n  return useMutation(get${symbol}MutationOptions(options), queryClient);\n};\n`;
}

// Keep explicit public models aligned even when the local Orval CLI is unavailable.
const explicitModelNames = Object.keys(spec.components?.schemas || {})
  .sort()
  .map(name => writeSchemaModel(name));
const importedTypes = new Set(explicitModelNames);
if (spec.components?.schemas?.CpfControllerSourceResponse) {
  writeSchemaModel("CpfControllerSourceResponse");
  importedTypes.add("CpfControllerSourceResponse");
}
const missing = [];
let synchronizedUrls = 0;
for (const [route, item] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(item || {})) {
    if (!methods.has(method.toLowerCase()) || !operation?.operationId) continue;
    const allParameters = resolveParameters(item, operation);
    const pathParams = pathParameters(route, allParameters);
    if (source.includes(`export const ${operation.operationId} = async (`)) {
      if (preRuntime && synchronizeExistingUrl(operation.operationId, route, pathParams)) synchronizedUrls += 1;
      continue;
    }
    missing.push({ route, method: method.toLowerCase(), pathItem: item, operation, allParameters, pathParams });
  }
}
if (missing.length && spec["x-cpf-export-origin"] !== "CONTROLLER_SOURCE_PRE_RUNTIME") {
  throw new Error(`Runtime OpenAPI generation omitted operations: ${missing.map(value => value.operation.operationId).join(", ")}`);
}
for (const value of missing) {
  const allParameters = value.allParameters;
  const pathParams = value.pathParams;
  const contractParams = allParameters.filter(parameter => parameter.in === "query" || parameter.in === "header");
  let paramsType;
  if (contractParams.length) {
    paramsType = `${pascal(value.operation.operationId)}Params`;
    writeParamsModel(paramsType, contractParams);
    importedTypes.add(paramsType);
  }
  let bodyType;
  let bodyMediaType = "application/json";
  if (value.operation.requestBody) {
    const content = value.operation.requestBody.content || {};
    const selected = content["application/json"]
      ? ["application/json", content["application/json"]]
      : Object.entries(content)[0];
    if (!selected) throw new Error(`${value.operation.operationId}: requestBody content missing`);
    bodyMediaType = selected[0];
    const schema = selected[1]?.schema;
    if (bodyMediaType === "multipart/form-data") {
      bodyType = "FormData";
    } else {
      bodyType = refName(schema) || `${pascal(value.operation.operationId)}Request`;
      writeSchemaModel(bodyType, refName(schema) ? undefined : schema);
      importedTypes.add(bodyType);
    }
  }
  const generated = value.method === "get"
    ? getBlock(value.operation.operationId, value.route, pathParams, contractParams, paramsType)
    : mutationBlock(value.operation.operationId, value.method, value.route, pathParams, contractParams, paramsType, bodyType, bodyMediaType);
  source += `\n\n// CPF PRE-RUNTIME FALLBACK START ${value.operation.operationId}\n${generated}// CPF PRE-RUNTIME FALLBACK END ${value.operation.operationId}\n`;
}
ensureModelIndex(importedTypes);
ensureClientImports(importedTypes);
fs.writeFileSync(clientPath, source);
console.log(`[CPF][FRONTEND][PASS] synchronized pre-runtime Orval coverage added=${missing.length} urls=${synchronizedUrls} models=${importedTypes.size}`);
