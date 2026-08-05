import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);
let source = fs.readFileSync(clientPath, "utf8");

const modelTypes = ["AdmMessageDeleteMessageParams", "CommonMessageRequest"];
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
function responsePrefix(id) { return id; }
function url(id, pathExpression, signature = "") {
  return `export const get${pascal(id)}Url = (${signature}) => ${pathExpression};`;
}
function queryBlock(id, pathExpression, signature = "", urlArgs = "", reactiveSignature = "", keyValues = "", callArgs = "", optionArgs = "") {
  const symbol = pascal(id);
  return `${url(id, pathExpression, signature)}

export const ${id} = async (${signature ? signature + ", " : ""}options?: CpfOrvalGeneratedRequestOptions): Promise<${responsePrefix(id)}Response> => {
  return cpfOrvalRequest<${responsePrefix(id)}Response>(get${symbol}Url(${urlArgs}), { ...options, method: 'GET' });
};

export const get${symbol}QueryKey = (${reactiveSignature}) => [${keyValues}] as const;

export const get${symbol}QueryOptions = <TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(
  ${reactiveSignature ? reactiveSignature + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = get${symbol}QueryKey(${optionArgs});
  const queryFn: QueryFunction<Awaited<ReturnType<typeof ${id}>>> = ({ signal }) => ${id}(${callArgs}${callArgs ? ", " : ""}{ signal, ...requestOptions });
  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>;
};

export type ${symbol}QueryResult = NonNullable<Awaited<ReturnType<typeof ${id}>>>;
export type ${symbol}QueryError = unknown;

export function use${symbol}<TData = Awaited<ReturnType<typeof ${id}>>, TError = unknown>(
  ${reactiveSignature ? reactiveSignature + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = get${symbol}QueryOptions(${optionArgs}${optionArgs ? ", " : ""}options);
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}`;
}
function mutationBlock({ id, method, pathExpression, pathSignature = "", urlArgs = "", paramsType, bodyType }) {
  const symbol = pascal(id);
  const fnParts = [];
  if (pathSignature) fnParts.push(pathSignature);
  if (paramsType) fnParts.push(`params: ${paramsType}`);
  if (bodyType) fnParts.push(`data: ${bodyType}`);
  fnParts.push("options?: CpfOrvalGeneratedRequestOptions");
  const variableParts = [];
  if (pathSignature) variableParts.push(pathSignature);
  if (paramsType) variableParts.push(`params: ${paramsType}`);
  if (bodyType) variableParts.push(`data: ${bodyType}`);
  const variableType = variableParts.length ? `{${variableParts.join("; ")}}` : "void";
  const names = [];
  if (pathSignature) names.push(pathSignature.split(":")[0].trim());
  if (paramsType) names.push("params");
  if (bodyType) names.push("data");
  const urlExpression = paramsType
    ? `(() => { const base = get${symbol}Url(${urlArgs}); const search = new URLSearchParams(); search.set('reason', params.reason); return base + '?' + search.toString(); })()`
    : `get${symbol}Url(${urlArgs})`;
  const bodyOptions = bodyType ? `, headers: { 'Content-Type': 'application/json', ...options?.headers }, data` : "";
  return `${url(id, pathExpression, pathSignature)}

export const ${id} = async (${fnParts.join(", ")}): Promise<${id}Response> => {
  return cpfOrvalRequest<${id}Response>(${urlExpression}, { ...options, method: '${method}'${bodyOptions} });
};

export const get${symbol}MutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof ${id}>>, TError, ${variableType}, TContext> => {
  const mutationKey = ['${id}'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof ${id}>>, ${variableType}> = (props) => {
    const { ${names.join(", ")} } = props;
    return ${id}(${[...names, "requestOptions"].join(", ")});
  };
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
fs.writeFileSync(path.join(modelDir, "commonMessageRequest.ts"), `/** ADM message create/update input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface CommonMessageRequest {
  messageId?: number;
  messageCode?: string;
  messageKey?: string;
  locale: string;
  messageFormatType?: "FIXED" | "INDEXED";
  externalMessage?: string;
  internalMessage?: string;
  messageValue?: string;
  parameterCount?: number;
  parameterSample?: string;
  description?: string;
  useYn?: "Y" | "N";
  reason: string;
}
`);
fs.writeFileSync(path.join(modelDir, "admMessageDeleteMessageParams.ts"), `/** Audited message disable input. The authenticated operator is resolved from the server session. */
export interface AdmMessageDeleteMessageParams { reason: string; }
`);
const modelIndexPath = path.join(modelDir, "index.ts");
let modelIndex = fs.existsSync(modelIndexPath) ? fs.readFileSync(modelIndexPath, "utf8") : "";
for (const exportName of ["commonMessageRequest", "admMessageDeleteMessageParams"]) {
  if (!modelIndex.includes(`'./${exportName}'`)) modelIndex += `export * from './${exportName}';\n`;
}
fs.writeFileSync(modelIndexPath, modelIndex);

ensureModelImports();
replaceOperation("admMessageFindMessages", queryBlock("admMessageFindMessages", "`/adm/api/messages`", "", "", "", "'adm','api','messages'", "", ""));
replaceOperation("admMessageFindMessage", queryBlock("admMessageFindMessage", "`/adm/api/messages/${encodeURIComponent(String(messageId))}`", "messageId: number", "messageId", "messageId: MaybeRefOrGetter<number>", "'adm','api','messages',messageId", "toValue(messageId)", "toValue(messageId)"));
replaceOperation("admMessageCreateMessage", mutationBlock({ id: "admMessageCreateMessage", method: "POST", pathExpression: "`/adm/api/messages`", bodyType: "CommonMessageRequest" }));
replaceOperation("admMessageUpdateMessage", mutationBlock({ id: "admMessageUpdateMessage", method: "PUT", pathExpression: "`/adm/api/messages/${encodeURIComponent(String(messageId))}`", pathSignature: "messageId: number", urlArgs: "messageId", bodyType: "CommonMessageRequest" }));
replaceOperation("admMessageDeleteMessage", mutationBlock({ id: "admMessageDeleteMessage", method: "DELETE", pathExpression: "`/adm/api/messages/${encodeURIComponent(String(messageId))}`", pathSignature: "messageId: number", urlArgs: "messageId", paramsType: "AdmMessageDeleteMessageParams" }));
fs.writeFileSync(clientPath, source);
console.log("[CPF][FRONTEND][PASS] synchronized ADM message generated client operations=5");
