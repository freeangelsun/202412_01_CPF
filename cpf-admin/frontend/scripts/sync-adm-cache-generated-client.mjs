import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);
let source = fs.readFileSync(clientPath, "utf8");

const modelTypes = [
  "AdmCacheControlRequest",
  "AdmCacheEvictKeyRequest",
  "AdmCacheEvictNamespaceRequest",
  "AdmCacheRefreshParams"
];
function pascal(value) { return value.charAt(0).toUpperCase() + value.slice(1); }
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
  const startToken = `export const get${pascal(operationId)}Url`;
  const start = source.indexOf(startToken);
  if (start < 0) throw new Error(`Generated URL block missing: ${operationId}`);
  const tail = source.slice(start + startToken.length);
  const nextResponse = tail.match(/\nexport type [a-z][A-Za-z0-9]*Response200 =/);
  const end = nextResponse ? start + startToken.length + nextResponse.index : source.length;
  source = source.slice(0, start) + generated.trimEnd() + "\n\n" + source.slice(end);
}
function queryBlock(id, pathExpression, keyValues) {
  const symbol = pascal(id);
  return `export const get${symbol}Url = () => ${pathExpression};

export const ${id} = async (options?: CpfOrvalGeneratedRequestOptions): Promise<${id}Response> => {
  return cpfOrvalRequest<${id}Response>(get${symbol}Url(), { ...options, method: 'GET' });
};

export const get${symbol}QueryKey = () => [${keyValues}] as const;

export const get${symbol}QueryOptions = <TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = get${symbol}QueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof ${id}>>> = ({ signal }) => ${id}({ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>;
};

export type ${symbol}QueryResult = NonNullable<Awaited<ReturnType<typeof ${id}>>>;
export type ${symbol}QueryError = unknown;

export function use${symbol}<TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = get${symbol}QueryOptions(options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}`;
}
function mutationBlock({ id, pathExpression, paramsType, bodyType, queryLines = [] }) {
  const symbol = pascal(id);
  const fnParts = [];
  if (paramsType) fnParts.push(`params: ${paramsType}`);
  if (bodyType) fnParts.push(`data: ${bodyType}`);
  fnParts.push("options?: CpfOrvalGeneratedRequestOptions");
  const variableParts = [];
  if (paramsType) variableParts.push(`params: ${paramsType}`);
  if (bodyType) variableParts.push(`data: ${bodyType}`);
  const variableType = variableParts.length ? `{${variableParts.join("; ")}}` : "void";
  const names = [...(paramsType ? ["params"] : []), ...(bodyType ? ["data"] : [])];
  const urlExpression = queryLines.length
    ? `(() => { const base = get${symbol}Url(); const search = new URLSearchParams(); ${queryLines.join(" ")} return base + '?' + search.toString(); })()`
    : `get${symbol}Url()`;
  const bodyOptions = bodyType ? `, headers: { 'Content-Type': 'application/json', ...options?.headers }, data` : "";
  const mutationFn = variableType === "void"
    ? `() => ${id}(requestOptions)`
    : `(props) => { const { ${names.join(", ")} } = props; return ${id}(${[...names, "requestOptions"].join(", ")}); }`;
  return `export const get${symbol}Url = () => ${pathExpression};

export const ${id} = async (${fnParts.join(", ")}): Promise<${id}Response> => {
  return cpfOrvalRequest<${id}Response>(${urlExpression}, { ...options, method: 'POST'${bodyOptions} });
};

export const get${symbol}MutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext> => {
  const mutationKey = ['${id}'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof ${id}>>, ${variableType}> = ${mutationFn};
  return { mutationFn, ...mutationOptions };
};

export type ${symbol}MutationResult = NonNullable<Awaited<ReturnType<typeof ${id}>>>;
export type ${symbol}MutationBody = ${bodyType || "never"};
export type ${symbol}MutationError = unknown;

export const use${symbol} = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext> => useMutation(get${symbol}MutationOptions(options), queryClient);`;
}

const modelDir = path.resolve(root, "src/generated/orval/model");
fs.mkdirSync(modelDir, { recursive: true });
const models = {
  admCacheRefreshParams: `/** Audited cache refresh query. */\nexport interface AdmCacheRefreshParams { target?: "ALL" | "CODE" | "MESSAGE" | "RESPONSE_CODE" | "CONFIG"; reason: string; }\n`,
  admCacheEvictKeyRequest: `/** Audited cache key eviction input. The authenticated operator is server-derived. */\nexport interface AdmCacheEvictKeyRequest { tenantId?: string; namespace: string; key: string; version: number; reason: string; }\n`,
  admCacheEvictNamespaceRequest: `/** Audited cache namespace eviction input. The authenticated operator is server-derived. */\nexport interface AdmCacheEvictNamespaceRequest { tenantId?: string; namespace: string; version: number; reason: string; }\n`,
  admCacheControlRequest: `/** Audited cache reconcile input. The authenticated operator is server-derived. */\nexport interface AdmCacheControlRequest { reason: string; }\n`
};
for (const [name, content] of Object.entries(models)) fs.writeFileSync(path.join(modelDir, `${name}.ts`), content);
const modelIndexPath = path.join(modelDir, "index.ts");
let modelIndex = fs.existsSync(modelIndexPath) ? fs.readFileSync(modelIndexPath, "utf8") : "";
for (const name of Object.keys(models)) if (!modelIndex.includes(`'./${name}'`)) modelIndex += `export * from './${name}';\n`;
fs.writeFileSync(modelIndexPath, modelIndex);

ensureModelImports();
replaceOperation("admCacheSummary", queryBlock("admCacheSummary", "`/adm/api/cache/summary`", "'adm','api','cache','summary'"));
replaceOperation("admCacheRefresh", mutationBlock({
  id: "admCacheRefresh",
  pathExpression: "`/adm/api/cache/refresh`",
  paramsType: "AdmCacheRefreshParams",
  queryLines: ["if (params.target) search.set('target', params.target);", "search.set('reason', params.reason);"]
}));
replaceOperation("admCacheEvictKey", mutationBlock({ id: "admCacheEvictKey", pathExpression: "`/adm/api/cache/evict-key`", bodyType: "AdmCacheEvictKeyRequest" }));
replaceOperation("admCacheEvictNamespace", mutationBlock({ id: "admCacheEvictNamespace", pathExpression: "`/adm/api/cache/evict-namespace`", bodyType: "AdmCacheEvictNamespaceRequest" }));
replaceOperation("admCacheReconcile", mutationBlock({ id: "admCacheReconcile", pathExpression: "`/adm/api/cache/reconcile`", bodyType: "AdmCacheControlRequest" }));
fs.writeFileSync(clientPath, source);
console.log("[CPF][FRONTEND][PASS] synchronized ADM cache generated client operations=5");
