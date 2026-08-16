import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const openApiPath = path.resolve(root, process.env.CPF_OPENAPI_FILE || 'openapi/cpf-openapi.json');
const outPath = path.resolve(root, process.env.CPF_COMPAT_CLIENT || 'src/generated/cpf-api.ts');
const spec = JSON.parse(fs.readFileSync(openApiPath, 'utf8'));
const methods = new Set(['get', 'post', 'put', 'patch', 'delete']);

function quote(v) { return JSON.stringify(v); }
function safeName(name) { return /^[A-Za-z_$][\w$]*$/.test(name) ? name : quote(name); }
function resolveRef(schema, seen = new Set()) {
  if (!schema?.$ref) return schema;
  const ref = schema.$ref;
  if (!ref.startsWith('#/components/schemas/')) return schema;
  const name = ref.split('/').pop();
  if (seen.has(name)) return { type: 'object', additionalProperties: true };
  const target = spec.components?.schemas?.[name];
  if (!target) throw new Error(`Missing schema ref: ${ref}`);
  return resolveRef(target, new Set([...seen, name]));
}
function typeOf(schema, seen = new Set()) {
  if (!schema) return 'unknown';
  if (schema.$ref) {
    const name = schema.$ref.split('/').pop();
    if (seen.has(name)) return 'Record<string, unknown>';
    const target = spec.components?.schemas?.[name];
    if (!target) throw new Error(`Missing schema ref: ${schema.$ref}`);
    return typeOf(target, new Set([...seen, name]));
  }
  if (Array.isArray(schema.enum) && schema.enum.length) return schema.enum.map(quote).join(' | ');
  if (Array.isArray(schema.oneOf) && schema.oneOf.length) return schema.oneOf.map(s => typeOf(s, seen)).join(' | ');
  if (Array.isArray(schema.anyOf) && schema.anyOf.length) return schema.anyOf.map(s => typeOf(s, seen)).join(' | ');
  if (Array.isArray(schema.allOf) && schema.allOf.length) return schema.allOf.map(s => typeOf(s, seen)).join(' & ');
  switch (schema.type) {
    case 'string': return 'string';
    case 'integer':
    case 'number': return 'number';
    case 'boolean': return 'boolean';
    case 'array': return `Array<${typeOf(schema.items, seen)}>`;
    case 'object': {
      if (!schema.properties) {
        if (schema.additionalProperties && typeof schema.additionalProperties === 'object') return `Record<string, ${typeOf(schema.additionalProperties, seen)}>`;
        return 'Record<string, unknown>';
      }
      const required = new Set(schema.required || []);
      const fields = Object.entries(schema.properties).map(([name, child]) => `${safeName(name)}${required.has(name) ? '' : '?'}: ${typeOf(child, seen)}`);
      return `{ ${fields.join('; ')} }`;
    }
    default: return schema.properties ? typeOf({ ...schema, type: 'object' }, seen) : 'unknown';
  }
}
function resolveParameter(parameter) {
  if (!parameter?.$ref) return parameter;
  const name = parameter.$ref.split('/').pop();
  const target = spec.components?.parameters?.[name];
  if (!target) throw new Error(`Missing parameter ref: ${parameter.$ref}`);
  return target;
}
function paramsFor(pathItem, operation) {
  return [...(pathItem.parameters || []), ...(operation.parameters || [])].map(resolveParameter);
}
function paramType(parameters, where) {
  const selected = parameters.filter(p => p.in === where);
  if (!selected.length) return 'Record<string, never>';
  const fields = selected.map(p => `${safeName(p.name)}${p.required ? '' : '?'}: ${typeOf(p.schema)}`);
  return `{ ${fields.join('; ')} }`;
}
function bodyContract(operation) {
  const content = operation.requestBody?.content || {};
  if (content['application/json']) return { mediaType: 'application/json', schema: content['application/json'].schema };
  if (content['application/*+json']) return { mediaType: 'application/*+json', schema: content['application/*+json'].schema };
  const first = Object.entries(content)[0];
  return first ? { mediaType: first[0], schema: first[1]?.schema } : undefined;
}
function responseSchema(operation) {
  const responses = operation.responses || {};
  const success = Object.entries(responses).find(([status]) => /^2\d\d$/.test(status));
  const content = success?.[1]?.content || {};
  return content['application/json']?.schema || content['application/*+json']?.schema || Object.values(content)[0]?.schema;
}
function renderPath(route) {
  return `renderPath(${quote(route)}, options.path as Record<string, string | number> | undefined)`;
}

const operations = [];
for (const [route, pathItem] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(pathItem || {})) {
    if (!methods.has(method.toLowerCase()) || !operation?.operationId) continue;
    const parameters = paramsFor(pathItem, operation);
    const body = bodyContract(operation);
    const response = responseSchema(operation);
    operations.push({
      id: operation.operationId,
      method: method.toUpperCase(),
      route,
      bodyType: body ? (body.mediaType === 'multipart/form-data' ? 'FormData' : typeOf(body.schema)) : 'never',
      pathType: paramType(parameters, 'path'),
      queryType: paramType(parameters, 'query'),
      headerType: paramType(parameters, 'header'),
      responseType: response ? typeOf(response) : 'unknown',
      hasBody: Boolean(body),
      hasPath: parameters.some(p => p.in === 'path'),
      hasQuery: parameters.some(p => p.in === 'query'),
      hasHeaders: parameters.some(p => p.in === 'header'),
      requiredHeaders: parameters.some(p => p.in === 'header' && p.required),
    });
  }
}
operations.sort((a, b) => a.id.localeCompare(b.id));
const duplicateIds = operations.map(o => o.id).filter((id, i, a) => a.indexOf(id) !== i);
if (duplicateIds.length) throw new Error(`Duplicate operationId: ${[...new Set(duplicateIds)].join(', ')}`);

const lines = [];
const aliases = operations.map(op => `${op.id} as orval${op.id.charAt(0).toUpperCase() + op.id.slice(1)}`);
lines.push('// GENERATED FROM canonical openapi/cpf-openapi.json. DO NOT EDIT.');
lines.push('// CPF_CANONICAL_ORVAL_DELEGATE: application-facing compatibility surface delegates every operation to the verified Orval client.');
lines.push(`import { ${aliases.join(', ')} } from "./orval/cpf-api";`);
lines.push('');
lines.push('export type CpfGeneratedHeaders = HeadersInit | Record<string, string>;');
lines.push('export interface CpfGeneratedBaseOptions { signal?: AbortSignal; headers?: CpfGeneratedHeaders; }');
lines.push('function headerValue(headers: CpfGeneratedHeaders | undefined, name: string): string | undefined { if (!headers) return undefined; if (headers instanceof Headers) return headers.get(name) ?? undefined; if (Array.isArray(headers)) { const found = headers.find(([key]) => String(key).toLowerCase() === name.toLowerCase()); return found ? String(found[1]) : undefined; } for (const [key,value] of Object.entries(headers)) if (key.toLowerCase() === name.toLowerCase()) return String(value); return undefined; }');
lines.push('');
for (const op of operations) {
  const symbol = op.id.charAt(0).toUpperCase() + op.id.slice(1);
  const parameters = paramsFor(spec.paths[op.route], spec.paths[op.route][op.method.toLowerCase()]);
  const pathParameters = parameters.filter(p => p.in === 'path');
  const headerParameters = parameters.filter(p => p.in === 'header');
  const hasContractParams = op.hasQuery || op.hasHeaders;
  lines.push(`export type ${symbol}Body = ${op.bodyType};`);
  lines.push(`export type ${symbol}Path = ${op.pathType};`);
  lines.push(`export type ${symbol}Query = ${op.queryType};`);
  lines.push(`export type ${symbol}Headers = ${op.headerType};`);
  lines.push(`export type ${symbol}Response = ${op.responseType};`);
  const bodyField = op.hasBody ? `data: ${symbol}Body` : 'data?: never';
  const pathField = op.hasPath ? `path: ${symbol}Path` : 'path?: never';
  const queryField = op.hasQuery ? `query?: ${symbol}Query` : 'query?: never';
  const headerField = op.hasHeaders ? `headers${op.requiredHeaders ? '' : '?'}: CpfGeneratedHeaders & ${symbol}Headers` : 'headers?: CpfGeneratedHeaders';
  lines.push(`export type ${symbol}Options = CpfGeneratedBaseOptions & { ${bodyField}; ${pathField}; ${queryField}; ${headerField}; };`);
  const optionalOptions = !op.hasBody && !op.hasPath && !op.requiredHeaders;
  const defaultValue = optionalOptions ? ' = {} as ' + symbol + 'Options' : '';
  lines.push(`export async function ${op.id}<T = ${symbol}Response>(options: ${symbol}Options${defaultValue}): Promise<T> {`);
  if (hasContractParams) {
    const headerFields = headerParameters.map(parameter => `${quote(parameter.name)}: headerValue(options.headers, ${quote(parameter.name)})`).join(', ');
    if (op.hasQuery && op.hasHeaders) lines.push(`  const contractParams = { ...(options.query || {}), ${headerFields} };`);
    else if (op.hasQuery) lines.push('  const contractParams = options.query || {};');
    else lines.push(`  const contractParams = { ${headerFields} };`);
  }
  const args = [];
  for (const parameter of pathParameters) args.push(`options.path[${quote(parameter.name)}]`);
  if (op.hasBody) args.push('options.data');
  if (hasContractParams) { const paramsIndex = pathParameters.length + (op.hasBody ? 1 : 0); args.push(`contractParams as Parameters<typeof orval${symbol}>[${paramsIndex}]`); }
  args.push('{ signal: options.signal, headers: options.headers }');
  lines.push(`  const response = await orval${symbol}(${args.join(', ')});`);
  lines.push('  return response.data as T;');
  lines.push('}');
  lines.push('');
}

fs.mkdirSync(path.dirname(outPath), { recursive: true });
fs.writeFileSync(outPath, `${lines.join('\n')}\n`);
console.log(`CPF canonical compatibility client generated: operations=${operations.length} file=${path.relative(root, outPath)}`);
