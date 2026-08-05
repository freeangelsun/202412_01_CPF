import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);
let source = fs.readFileSync(clientPath, "utf8");
const pascal = value => value.charAt(0).toUpperCase() + value.slice(1);

const models = {
  commonCodeRequest: `/** Code create/update input; operator is server-derived. */\nexport interface CommonCodeRequest { codeId?: number; parentId?: number; codeKey: string; codeValue: string; description?: string; useYn?: "Y" | "N"; reason: string; }\n`,
  admCodeDeleteCodeParams: `/** Audited code disable query. */\nexport interface AdmCodeDeleteCodeParams { reason: string; }\n`,
  commonConfigRequest: `/** Configuration create/update input; operator is server-derived. */\nexport interface CommonConfigRequest { configId?: number; configKey: string; configValue: string; configType?: "STRING" | "NUMBER" | "BOOLEAN" | "JSON"; description?: string; encryptedYn?: "Y" | "N"; useYn?: "Y" | "N"; reason: string; }\n`,
  admConfigDeleteConfigParams: `/** Audited configuration disable query. */\nexport interface AdmConfigDeleteConfigParams { reason: string; }\n`,
  commonResponseCodeRequest: `/** Response-code create/update input; operator is server-derived. */\nexport interface CommonResponseCodeRequest { responseCode: string; messageCode: string; resultType: "S" | "E"; moduleId: string; responseGroup: string; sequenceNo: string; httpStatus: number; description?: string; useYn?: "Y" | "N"; }\n`,
  admResponseCodeCreateParams: `/** Audited response-code create query. */\nexport interface AdmResponseCodeCreateParams { reason: string; }\n`,
  admResponseCodeUpdateParams: `/** Audited response-code update query. */\nexport interface AdmResponseCodeUpdateParams { reason: string; }\n`,
  admResponseCodeDeleteParams: `/** Audited response-code delete query. */\nexport interface AdmResponseCodeDeleteParams { reason: string; }\n`
};
const modelTypes = [
  "CommonCodeRequest", "AdmCodeDeleteCodeParams",
  "CommonConfigRequest", "AdmConfigDeleteConfigParams",
  "CommonResponseCodeRequest", "AdmResponseCodeCreateParams", "AdmResponseCodeUpdateParams", "AdmResponseCodeDeleteParams"
];
const modelDir = path.resolve(root, "src/generated/orval/model");
fs.mkdirSync(modelDir, { recursive: true });
for (const [name, content] of Object.entries(models)) fs.writeFileSync(path.join(modelDir, `${name}.ts`), content);
const modelIndexPath = path.join(modelDir, "index.ts");
let modelIndex = fs.existsSync(modelIndexPath) ? fs.readFileSync(modelIndexPath, "utf8") : "";
for (const name of Object.keys(models)) if (!modelIndex.includes(`'./${name}'`)) modelIndex += `export * from './${name}';\n`;
fs.writeFileSync(modelIndexPath, modelIndex);

function ensureModelImports() {
  const token = "from './model';";
  const end = source.indexOf(token);
  if (end < 0) throw new Error("Generated model import block missing");
  const start = source.lastIndexOf("import type {", end);
  if (start < 0) throw new Error("Generated model import start missing");
  const block = source.slice(start, end + token.length);
  const existing = [...block.matchAll(/^\s*([A-Za-z_$][\w$]*)\s*,?\s*$/gm)].map(match => match[1]);
  const names = [...new Set([...existing, ...modelTypes])].sort();
  const replacement = `import type {\n${names.map(name => `  ${name}`).join(",\n")}\n} from './model';`;
  source = source.slice(0, start) + replacement + source.slice(end + token.length);
}
function replaceOperation(operationId, generated) {
  const token = `export const get${pascal(operationId)}Url`;
  const start = source.indexOf(token);
  if (start < 0) throw new Error(`Generated URL block missing: ${operationId}`);
  const tail = source.slice(start + token.length);
  const next = tail.match(/\nexport type [a-z][A-Za-z0-9]*Response200 =/);
  const end = next ? start + token.length + next.index : source.length;
  source = source.slice(0, start) + generated.trimEnd() + "\n\n" + source.slice(end);
}
function urlFunction(c) {
  const args = c.pathParams.map(p => `${p.name}: ${p.type}`).join(", ");
  let body = `export const get${pascal(c.id)}Url = (${args}) => {\n  const base = \`${c.path.replace(/\{([^}]+)\}/g, '${encodeURIComponent(String($1))}')}\`;`;
  if (c.paramsType) {
    body += `\n  const search = new URLSearchParams();`;
    for (const field of c.queryFields) body += `\n  search.set('${field}', String(params.${field}));`;
    body += `\n  return base + '?' + search.toString();\n};`;
    return body.replace(`(${args})`, `(${[args, `params: ${c.paramsType}`].filter(Boolean).join(", ")})`);
  }
  return body + `\n  return base;\n};`;
}
function queryBlock(c) {
  const symbol = pascal(c.id);
  const plainArgs = c.pathParams.map(p => `${p.name}: ${p.type}`);
  const reactiveArgs = c.pathParams.map(p => `${p.name}: MaybeRefOrGetter<${p.type}>`);
  const urlArgs = c.pathParams.map(p => p.name).join(", ");
  const key = c.path.split('/').filter(Boolean).map(v => v.startsWith('{') ? v.slice(1,-1) : JSON.stringify(v)).join(',');
  const unwrapped = c.pathParams.map(p => `toValue(${p.name})`).join(', ');
  const call = [...c.pathParams.map(p => `toValue(${p.name})`), `{ signal, ...requestOptions }`].join(', ');
  return `${urlFunction(c)}

export const ${c.id} = async (${plainArgs.length ? plainArgs.join(', ') + ', ' : ''}options?: CpfOrvalGeneratedRequestOptions): Promise<${c.id}Response> => {
  return cpfOrvalRequest<${c.id}Response>(get${symbol}Url(${urlArgs}), { ...options, method: 'GET' });
};

export const get${symbol}QueryKey = (${reactiveArgs.join(', ')}) => [${key}] as const;

export const get${symbol}QueryOptions = <TData = Awaited<ReturnType<typeof ${c.id}>>, TError = unknown>(
  ${reactiveArgs.length ? reactiveArgs.join(', ') + ', ' : ''}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = get${symbol}QueryKey(${unwrapped});
  const queryFn: QueryFunction<Awaited<ReturnType<typeof ${c.id}>>> = ({ signal }) => ${c.id}(${call});
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, TData>;
};

export type ${symbol}QueryResult = NonNullable<Awaited<ReturnType<typeof ${c.id}>>>;
export type ${symbol}QueryError = unknown;

export function use${symbol}<TData = Awaited<ReturnType<typeof ${c.id}>>, TError = unknown>(
  ${reactiveArgs.length ? reactiveArgs.join(', ') + ', ' : ''}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = get${symbol}QueryOptions(${unwrapped}${unwrapped ? ', ' : ''}options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}`;
}
function mutationBlock(c) {
  const symbol = pascal(c.id);
  const pathArgs = c.pathParams.map(p => `${p.name}: ${p.type}`);
  const fnArgs = [...pathArgs, ...(c.paramsType ? [`params: ${c.paramsType}`] : []), ...(c.bodyType ? [`data: ${c.bodyType}`] : []), "options?: CpfOrvalGeneratedRequestOptions"];
  const variableParts = [...pathArgs, ...(c.paramsType ? [`params: ${c.paramsType}`] : []), ...(c.bodyType ? [`data: ${c.bodyType}`] : [])];
  const variableType = `{${variableParts.join('; ')}}`;
  const names = [...c.pathParams.map(p => p.name), ...(c.paramsType ? ['params'] : []), ...(c.bodyType ? ['data'] : [])];
  const urlArgs = [...c.pathParams.map(p => p.name), ...(c.paramsType ? ['params'] : [])].join(', ');
  const body = c.bodyType ? `, headers: { 'Content-Type': 'application/json', ...options?.headers }, data` : '';
  return `${urlFunction(c)}

export const ${c.id} = async (${fnArgs.join(', ')}): Promise<${c.id}Response> => {
  return cpfOrvalRequest<${c.id}Response>(get${symbol}Url(${urlArgs}), { ...options, method: '${c.method}'${body} });
};

export const get${symbol}MutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, ${variableType}, TContext> => {
  const mutationKey = ['${c.id}'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof ${c.id}>>, ${variableType}> = (props) => {
    const { ${names.join(', ')} } = props;
    return ${c.id}(${[...names, 'requestOptions'].join(', ')});
  };
  return { mutationFn, ...mutationOptions };
};

export type ${symbol}MutationResult = NonNullable<Awaited<ReturnType<typeof ${c.id}>>>;
export type ${symbol}MutationBody = ${c.bodyType || 'never'};
export type ${symbol}MutationError = unknown;

export const use${symbol} = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${c.id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof ${c.id}>>, TError, ${variableType}, TContext> => useMutation(get${symbol}MutationOptions(options), queryClient);`;
}

const operations = [
  { id:'admCodeFindCodes', method:'GET', path:'/adm/api/codes', pathParams:[] },
  { id:'admCodeFindCode', method:'GET', path:'/adm/api/codes/{codeId}', pathParams:[{name:'codeId',type:'number'}] },
  { id:'admCodeCreateCode', method:'POST', path:'/adm/api/codes', pathParams:[], bodyType:'CommonCodeRequest' },
  { id:'admCodeUpdateCode', method:'PUT', path:'/adm/api/codes/{codeId}', pathParams:[{name:'codeId',type:'number'}], bodyType:'CommonCodeRequest' },
  { id:'admCodeDeleteCode', method:'DELETE', path:'/adm/api/codes/{codeId}', pathParams:[{name:'codeId',type:'number'}], paramsType:'AdmCodeDeleteCodeParams', queryFields:['reason'] },
  { id:'admConfigFindConfigs', method:'GET', path:'/adm/api/configs', pathParams:[] },
  { id:'admConfigFindConfig', method:'GET', path:'/adm/api/configs/{configId}', pathParams:[{name:'configId',type:'number'}] },
  { id:'admConfigCreateConfig', method:'POST', path:'/adm/api/configs', pathParams:[], bodyType:'CommonConfigRequest' },
  { id:'admConfigUpdateConfig', method:'PUT', path:'/adm/api/configs/{configId}', pathParams:[{name:'configId',type:'number'}], bodyType:'CommonConfigRequest' },
  { id:'admConfigDeleteConfig', method:'DELETE', path:'/adm/api/configs/{configId}', pathParams:[{name:'configId',type:'number'}], paramsType:'AdmConfigDeleteConfigParams', queryFields:['reason'] },
  { id:'admResponseCodeFindAll', method:'GET', path:'/adm/api/response-codes', pathParams:[] },
  { id:'admResponseCodeFindOne', method:'GET', path:'/adm/api/response-codes/{responseCode}', pathParams:[{name:'responseCode',type:'string'}] },
  { id:'admResponseCodeCreate', method:'POST', path:'/adm/api/response-codes', pathParams:[], paramsType:'AdmResponseCodeCreateParams', queryFields:['reason'], bodyType:'CommonResponseCodeRequest' },
  { id:'admResponseCodeUpdate', method:'PUT', path:'/adm/api/response-codes/{responseCode}', pathParams:[{name:'responseCode',type:'string'}], paramsType:'AdmResponseCodeUpdateParams', queryFields:['reason'], bodyType:'CommonResponseCodeRequest' },
  { id:'admResponseCodeDelete', method:'DELETE', path:'/adm/api/response-codes/{responseCode}', pathParams:[{name:'responseCode',type:'string'}], paramsType:'AdmResponseCodeDeleteParams', queryFields:['reason'] }
];
ensureModelImports();
for (const c of operations) replaceOperation(c.id, c.method === 'GET' ? queryBlock(c) : mutationBlock(c));
fs.writeFileSync(clientPath, source);
console.log(`[CPF][FRONTEND][PASS] synchronized ADM reference catalog generated client operations=${operations.length}`);
