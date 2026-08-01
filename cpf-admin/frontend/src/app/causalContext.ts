import type { LocationQuery, LocationQueryRaw, RouteLocationRaw } from "vue-router";

export const CPF_CAUSAL_CONTEXT_KEYS = [
  "transactionId",
  "executionId",
  "correlationId",
  "from",
  "to",
  "tenantId",
  "environment",
  "instanceId",
  "serviceId",
  "filter"
] as const;

export type CpfCausalContextKey = typeof CPF_CAUSAL_CONTEXT_KEYS[number];
export type CpfCausalContext = Partial<Record<CpfCausalContextKey, string>>;

const ID_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._:@/+-]{0,199}$/;
const TIME_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d{1,9})?)?(?:Z|[+-]\d{2}:\d{2})$/;
const MAX_FILTER_LENGTH = 1000;

function scalar(value: unknown): string {
  if (Array.isArray(value)) return scalar(value[0]);
  return typeof value === "string" ? value.trim() : "";
}

function validate(key: CpfCausalContextKey, raw: unknown): string | undefined {
  const value = scalar(raw);
  if (!value) return undefined;
  if (key === "from" || key === "to") return TIME_PATTERN.test(value) ? value : undefined;
  if (key === "filter") return value.length <= MAX_FILTER_LENGTH && !/[\u0000-\u001f]/.test(value) ? value : undefined;
  return ID_PATTERN.test(value) ? value : undefined;
}

export function parseCausalContext(query: LocationQuery | Record<string, unknown>): CpfCausalContext {
  const result: CpfCausalContext = {};
  for (const key of CPF_CAUSAL_CONTEXT_KEYS) {
    const value = validate(key, query[key]);
    if (value !== undefined) result[key] = value;
  }
  return result;
}

export function mergeCausalContext(base: CpfCausalContext, query: LocationQuery | Record<string, unknown>): CpfCausalContext {
  return { ...base, ...parseCausalContext(query) };
}

export function causalContextQuery(context: CpfCausalContext): LocationQueryRaw {
  const query: LocationQueryRaw = {};
  for (const key of CPF_CAUSAL_CONTEXT_KEYS) {
    const value = validate(key, context[key]);
    if (value !== undefined) query[key] = value;
  }
  return query;
}

export function withCausalContext(target: RouteLocationRaw, context: CpfCausalContext): RouteLocationRaw {
  if (typeof target === "string") return { path: target, query: causalContextQuery(context) };
  if ("path" in target || "name" in target) {
    return { ...target, query: { ...causalContextQuery(context), ...(target.query || {}) } } as RouteLocationRaw;
  }
  return target;
}

export function contextEqualsQuery(context: CpfCausalContext, query: LocationQuery | Record<string, unknown>): boolean {
  const parsed = parseCausalContext(query);
  return CPF_CAUSAL_CONTEXT_KEYS.every(key => (context[key] || "") === (parsed[key] || ""));
}
