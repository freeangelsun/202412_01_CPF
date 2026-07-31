import { MutationObserver } from "@tanstack/vue-query";
import { CpfOrvalError, cpfOrvalRequest } from "./orval-mutator";
import { resolveCpfOperation } from "../generated/cpf-operation-contract";
import { cpfQueryClient } from "./queryClient";

export interface CpfGeneratedRequestConfig {
  url: string; method: string; headers?: HeadersInit; data?: unknown; params?: Record<string, unknown>; signal?: AbortSignal;
}
function convert(error: unknown): never {
  if (error instanceof CpfOrvalError) throw error;
  throw error;
}
export async function bzaQuery<T = unknown>(url: string, params?: Record<string, unknown>): Promise<T> {
  const target = new URL(url, window.location.origin);
  Object.entries(params || {}).forEach(([key, value]) => { if (value !== undefined && value !== null) target.searchParams.set(key, String(value)); });
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation("GET", relative);
  try {
    return await cpfQueryClient.fetchQuery<T>({
      queryKey: ["cpf", operation.operationId, target.pathname, target.search],
      queryFn: () => cpfOrvalRequest<T>({ url: relative, method: "GET" })
    });
  } catch (error) { return convert(error); }
}
export async function bzaMutation<T = unknown>(url: string, method: "POST" | "PUT" | "PATCH" | "DELETE", body?: unknown): Promise<T> {
  const target = new URL(url, window.location.origin);
  const relative = target.pathname + target.search;
  const operation = resolveCpfOperation(method, relative);
  const observer = new MutationObserver<T, unknown, unknown, unknown>(cpfQueryClient, {
    mutationKey: ["cpf", operation.operationId],
    mutationFn: () => cpfOrvalRequest<T>({ url: relative, method, data: body })
  });
  try {
    const result = await observer.mutate(undefined);
    await cpfQueryClient.invalidateQueries({ queryKey: ["cpf"] });
    return result;
  } catch (error) { return convert(error); } finally { observer.reset(); }
}
export async function cpfGeneratedRequest<T = unknown>(config: CpfGeneratedRequestConfig): Promise<T> {
  const method = config.method.trim().toUpperCase();
  const target = new URL(config.url, window.location.origin);
  Object.entries(config.params || {}).forEach(([key, value]) => { if (value !== undefined && value !== null) target.searchParams.set(key, String(value)); });
  const relative = target.pathname + target.search;
  if (method === "GET") return bzaQuery<T>(relative);
  if (!["POST", "PUT", "PATCH", "DELETE"].includes(method)) throw new Error(`Unsupported generated BZA method: ${method}`);
  return bzaMutation<T>(relative, method as "POST" | "PUT" | "PATCH" | "DELETE", config.data);
}
export async function bzaApi<T = unknown>(url: string, options: RequestInit = {}): Promise<T> {
  const method = String(options.method || "GET").toUpperCase();
  let body: unknown = options.body; if (typeof body === "string") { try { body = JSON.parse(body); } catch { /* keep string */ } }
  return cpfGeneratedRequest<T>({ url, method, data: body, headers: options.headers, signal: options.signal || undefined });
}
