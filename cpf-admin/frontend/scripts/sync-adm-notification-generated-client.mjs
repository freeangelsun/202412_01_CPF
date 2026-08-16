import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const clientPath = path.resolve(root, process.env.CPF_GENERATED_CLIENT || "src/generated/orval/cpf-api.ts");
const openApiPath = path.resolve(root, process.env.CPF_OPENAPI_INPUT || "openapi/cpf-openapi.json");
if (!fs.existsSync(openApiPath)) throw new Error(`OpenAPI input missing: ${openApiPath}`);
const openapi = JSON.parse(fs.readFileSync(openApiPath, "utf8"));
if (!fs.existsSync(clientPath)) throw new Error(`Generated client missing: ${clientPath}`);
let source = fs.readFileSync(clientPath, "utf8");

const modelTypes = [
  "AdmNotificationCancelDeliveryParams",
  "AdmNotificationDisableRuleParams",
  "AdmNotificationFindDeliveryAttemptsParams",
  "AdmNotificationFindDeliveryLogsParams",
  "AdmNotificationFindDlqParams",
  "AdmNotificationFindRulesParams",
  "AdmNotificationRetryDeliveryParams",
  "AdmNotificationRuleRequest",
  "AdmNotificationTestSendRequest"
];

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

function pascal(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
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
function pathExpression(template, pathParams) {
  let result = template;
  for (const param of pathParams) result = result.replace(`{${param.name}}`, `\${encodeURIComponent(String(${param.name}))}`);
  return `\`${result}\``;
}
function urlFunction(config) {
  const pathArgs = config.pathParams.map(param => `${param.name}: ${param.type}`).join(", ");
  const paramsArg = config.paramsType ? `${pathArgs ? ", " : ""}params${config.paramsRequired ? "" : "?"}: ${config.paramsType}` : "";
  const signature = `${pathArgs}${paramsArg}`;
  const base = pathExpression(config.path, config.pathParams);
  if (!config.paramsType) return `export const get${pascal(config.id)}Url = (${signature}) => ${base};`;
  const fields = config.queryFields.map(field =>
    `  if (params?.${field} !== undefined && params?.${field} !== null) searchParams.append(${JSON.stringify(field)}, String(params.${field}));`
  ).join("\n");
  return `export const get${pascal(config.id)}Url = (${signature}) => {\n  const searchParams = new URLSearchParams();\n${fields}\n  const query = searchParams.toString();\n  return ${base} + (query ? \`?\${query}\` : "");\n};`;
}
function queryBlock(config) {
  const symbol = pascal(config.id);
  const pathArgs = config.pathParams.map(param => `${param.name}: ${param.type}`);
  const functionArgs = [...pathArgs];
  if (config.paramsType) functionArgs.push(`params${config.paramsRequired ? "" : "?"}: ${config.paramsType}`);
  functionArgs.push(`options?: CpfOrvalGeneratedRequestOptions`);
  const urlArgs = [...config.pathParams.map(param => param.name), ...(config.paramsType ? ["params"] : [])].join(", ");
  const reactivePathArgs = config.pathParams.map(param => `${param.name}: MaybeRefOrGetter<${param.type}>`);
  const reactiveArgs = [...reactivePathArgs];
  if (config.paramsType) reactiveArgs.push(`params${config.paramsRequired ? "" : "?"}: MaybeRefOrGetter<${config.paramsType} | undefined>`);
  const keyValues = [
    ...config.path.split("/").filter(Boolean).map(value => value.startsWith("{") ? value.slice(1, -1) : JSON.stringify(value)),
    ...(config.paramsType ? ["params"] : [])
  ].join(",");
  const unwrapUrlArgs = [
    ...config.pathParams.map(param => `toValue(${param.name})`),
    ...(config.paramsType ? ["toValue(params)"] : [])
  ].join(", ");
  const enabled = config.pathParams.length
    ? `, enabled: computed(() => ${config.pathParams.map(param => `toValue(${param.name}) !== null && toValue(${param.name}) !== undefined`).join(" && ")})`
    : "";
  const queryCallArgs = [
    ...config.pathParams.map(param => `toValue(${param.name})`),
    ...(config.paramsType ? ["toValue(params)"] : []),
    `{ signal, ...requestOptions }`
  ].join(", ");
  const optionCallArgs = [
    ...config.pathParams.map(param => param.name),
    ...(config.paramsType ? ["params"] : []),
    "options"
  ].join(",");
  return `${urlFunction(config)}

export const ${config.id} = async (${functionArgs.join(", ")}): Promise<${config.id}Response> => {
  return cpfOrvalRequest<${config.id}Response>(get${symbol}Url(${urlArgs}), {
    ...options,
    method: '${config.method}'
  });
};

export const get${symbol}QueryKey = (${reactiveArgs.join(", ")}) => {
  return [${keyValues}] as const;
};

export const get${symbol}QueryOptions = <TData = Awaited<ReturnType<typeof ${config.id}>>, TError = unknown>(
  ${reactiveArgs.length ? reactiveArgs.join(", ") + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> }
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {};
  const queryKey = get${symbol}QueryKey(${unwrapUrlArgs});
  const queryFn: QueryFunction<Awaited<ReturnType<typeof ${config.id}>>> = ({ signal }) => ${config.id}(${queryCallArgs});
  return { queryKey, queryFn${enabled}, ...queryOptions } as UseQueryOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, TData>;
};

export type ${symbol}QueryResult = NonNullable<Awaited<ReturnType<typeof ${config.id}>>>;
export type ${symbol}QueryError = unknown;

export function use${symbol}<TData = Awaited<ReturnType<typeof ${config.id}>>, TError = unknown>(
  ${reactiveArgs.length ? reactiveArgs.join(", ") + ", " : ""}options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, TData>>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = get${symbol}QueryOptions(${optionCallArgs});
  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>;
  return query;
}`;
}
function mutationBlock(config) {
  const symbol = pascal(config.id);
  const pathArgs = config.pathParams.map(param => `${param.name}: ${param.type}`);
  const fnArgs = [...pathArgs];
  if (config.paramsType) fnArgs.push(`params: ${config.paramsType}`);
  if (config.bodyType) fnArgs.push(`data: ${config.bodyType}`);
  fnArgs.push(`options?: CpfOrvalGeneratedRequestOptions`);
  const urlArgs = [...config.pathParams.map(param => param.name), ...(config.paramsType ? ["params"] : [])].join(", ");
  const variableParts = [
    ...config.pathParams.map(param => `${param.name}: ${param.type}`),
    ...(config.paramsType ? [`params: ${config.paramsType}`] : []),
    ...(config.bodyType ? [`data: ${config.bodyType}`] : [])
  ];
  const variableType = variableParts.length ? `{${variableParts.join("; ")}}` : "void";
  const destructure = variableParts.length
    ? `const { ${[...config.pathParams.map(param => param.name), ...(config.paramsType ? ["params"] : []), ...(config.bodyType ? ["data"] : [])].join(", ")} } = props;`
    : "";
  const callArgs = [
    ...config.pathParams.map(param => param.name),
    ...(config.paramsType ? ["params"] : []),
    ...(config.bodyType ? ["data"] : []),
    "requestOptions"
  ].join(", ");
  const bodyOptions = config.bodyType
    ? `,\n    headers: { 'Content-Type': 'application/json', ...options?.headers },\n    data`
    : "";
  const mutationFnArgs = variableType === "void" ? "()" : "(props)";
  return `${urlFunction(config)}

export const ${config.id} = async (${fnArgs.join(", ")}): Promise<${config.id}Response> => {
  return cpfOrvalRequest<${config.id}Response>(get${symbol}Url(${urlArgs}), {
    ...options,
    method: '${config.method}'${bodyOptions}
  });
};

export const get${symbol}MutationOptions = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> }
): UseMutationOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, ${variableType}, TContext> => {
  const mutationKey = ['${config.id}'];
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined };
  const mutationFn: MutationFunction<Awaited<ReturnType<typeof ${config.id}>>, ${variableType}> = ${mutationFnArgs} => {
    ${destructure}
    return ${config.id}(${callArgs});
  };
  return { mutationFn, ...mutationOptions };
};

export type ${symbol}MutationResult = NonNullable<Awaited<ReturnType<typeof ${config.id}>>>;
export type ${symbol}MutationBody = ${config.bodyType || "never"};
export type ${symbol}MutationError = unknown;

export const use${symbol} = <TError = unknown, TContext = unknown>(
  options?: { mutation?: UseMutationOptions<Awaited<ReturnType<typeof ${config.id}>>, TError, ${variableType}, TContext>, request?: SecondParameter<typeof cpfOrvalRequest> },
  queryClient?: QueryClient
): UseMutationReturnType<Awaited<ReturnType<typeof ${config.id}>>, TError, ${variableType}, TContext> => {
  return useMutation(get${symbol}MutationOptions(options), queryClient);
}`;
}

function openApiScalarType(schema) {
  if (schema?.type === "integer" || schema?.type === "number") return "number";
  if (schema?.type === "boolean") return "boolean";
  return "string";
}
function resolveOpenApiOperation(operationId) {
  for (const [route, pathItem] of Object.entries(openapi.paths || {})) {
    for (const [method, operation] of Object.entries(pathItem || {})) {
      if (operation && typeof operation === "object" && operation.operationId === operationId) {
        const parameters = [...(pathItem.parameters || []), ...(operation.parameters || [])].map(parameter => {
          if (!parameter?.$ref) return parameter;
          return openapi.components?.parameters?.[parameter.$ref.split("/").pop()] || parameter;
        });
        return { route, method: method.toUpperCase(), operation, parameters };
      }
    }
  }
  throw new Error(`OpenAPI operation missing: ${operationId}`);
}
function hydrateFromOpenApi(config) {
  const contract = resolveOpenApiOperation(config.id);
  const pathParams = contract.parameters
    .filter(parameter => parameter.in === "path")
    .map(parameter => ({ name: parameter.name, type: openApiScalarType(parameter.schema) }));
  const queryFields = contract.parameters
    .filter(parameter => parameter.in === "query")
    .map(parameter => parameter.name);
  const bodySchema = contract.operation.requestBody?.content?.["application/json"]?.schema
    || Object.values(contract.operation.requestBody?.content || {})[0]?.schema;
  const bodyType = bodySchema?.$ref?.split("/").pop();
  if (config.paramsType && queryFields.length === 0) throw new Error(`${config.id}: configured params type has no OpenAPI query contract`);
  if (!config.paramsType && queryFields.length > 0) throw new Error(`${config.id}: OpenAPI query contract is missing configured params type`);
  if (config.bodyType && bodyType && config.bodyType !== bodyType) throw new Error(`${config.id}: body type mismatch configured=${config.bodyType} openapi=${bodyType}`);
  if (!config.bodyType && bodySchema) throw new Error(`${config.id}: OpenAPI requestBody is missing configured body type`);
  return {
    ...config,
    method: contract.method,
    path: contract.route,
    pathParams,
    queryFields,
    bodyType: bodyType || config.bodyType
  };
}

const operations = [
  { id: "admNotificationFindRules", method: "GET", path: "/adm/api/notifications/rules", pathParams: [], paramsType: "AdmNotificationFindRulesParams", paramsRequired: false, queryFields: ["limit"] },
  { id: "admNotificationSaveRule", method: "POST", path: "/adm/api/notifications/rules", pathParams: [], bodyType: "AdmNotificationRuleRequest", queryFields: [] },
  { id: "admNotificationFindRule", method: "GET", path: "/adm/api/notifications/rules/{ruleId}", pathParams: [{ name: "ruleId", type: "string" }], queryFields: [] },
  { id: "admNotificationUpdateRule", method: "PUT", path: "/adm/api/notifications/rules/{ruleId}", pathParams: [{ name: "ruleId", type: "string" }], bodyType: "AdmNotificationRuleRequest", queryFields: [] },
  { id: "admNotificationDisableRule", method: "PUT", path: "/adm/api/notifications/rules/{ruleId}/disable", pathParams: [{ name: "ruleId", type: "string" }], paramsType: "AdmNotificationDisableRuleParams", paramsRequired: true, queryFields: ["reason"] },
  { id: "admNotificationFindDeliveryLogs", method: "GET", path: "/adm/api/notifications/delivery-logs", pathParams: [], paramsType: "AdmNotificationFindDeliveryLogsParams", paramsRequired: false, queryFields: ["limit"] },
  { id: "admNotificationFindDlq", method: "GET", path: "/adm/api/notifications/delivery-logs/dlq", pathParams: [], paramsType: "AdmNotificationFindDlqParams", paramsRequired: false, queryFields: ["limit"] },
  { id: "admNotificationFindDeliveryAttempts", method: "GET", path: "/adm/api/notifications/delivery-logs/{deliveryId}/attempts", pathParams: [{ name: "deliveryId", type: "string" }], paramsType: "AdmNotificationFindDeliveryAttemptsParams", paramsRequired: false, queryFields: ["limit"] },
  { id: "admNotificationSendTest", method: "POST", path: "/adm/api/notifications/rules/{ruleId}/test-send", pathParams: [{ name: "ruleId", type: "string" }], bodyType: "AdmNotificationTestSendRequest", queryFields: [] },
  { id: "admNotificationRetryDelivery", method: "POST", path: "/adm/api/notifications/delivery-logs/{deliveryId}/retry", pathParams: [{ name: "deliveryId", type: "string" }], paramsType: "AdmNotificationRetryDeliveryParams", paramsRequired: true, queryFields: ["expectedVersion", "reason"] },
  { id: "admNotificationCancelDelivery", method: "POST", path: "/adm/api/notifications/delivery-logs/{deliveryId}/cancel", pathParams: [{ name: "deliveryId", type: "string" }], paramsType: "AdmNotificationCancelDeliveryParams", paramsRequired: true, queryFields: ["expectedVersion", "reason"] }
];

const canonicalOperations = operations.map(hydrateFromOpenApi);
ensureModelImports();
for (const config of canonicalOperations) {
  replaceOperation(config.id, config.method === "GET" ? queryBlock(config) : mutationBlock(config));
}
fs.writeFileSync(clientPath, source);
console.log(`[CPF][FRONTEND][PASS] synchronized ADM notification generated client operations=${canonicalOperations.length}`);
