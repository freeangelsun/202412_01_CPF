declare module "*.vue" {
  import type { Component } from "vue";
  const component: Component;
  export default component;
}
declare module "*.vue?raw" { const source: string; export default source; }

declare module "vue" {
  export type Component = unknown;
  export interface Ref<T = unknown> { value: T; }
  export interface ComputedRef<T = unknown> extends Ref<T> { readonly value: T; }
  export function ref<T>(value: T): Ref<T>;
  export function ref<T = undefined>(): Ref<T | undefined>;
  export function reactive<T extends object>(target: T): T;
  export function computed<T>(getter: () => T): ComputedRef<T>;
  export function defineAsyncComponent<T extends Component = Component>(loader: () => Promise<T | { default: T }>): T;
}

declare module "pinia" {
  import type { Ref } from "vue";
  type GetterResults<G> = { readonly [K in keyof G]: G[K] extends (...args: any[]) => infer R ? R : never };
  export function defineStore<
    Id extends string,
    S extends object,
    G extends Record<string, (...args: any[]) => any> = Record<never, never>,
    A extends Record<string, (...args: any[]) => any> = Record<never, never>
  >(
    id: Id,
    options: {
      state: () => S;
      getters?: G & ThisType<Readonly<S> & GetterResults<G> & A>;
      actions?: A & ThisType<S & GetterResults<G> & A>;
    }
  ): () => S & GetterResults<G> & A;
  export function storeToRefs<T extends object>(store: T): { [K in keyof T]: Ref<T[K]> };
}

declare module "vue-router" {
  import type { Component } from "vue";
  export type LocationQueryValue = string | null;
  export type LocationQuery = Record<string, LocationQueryValue | LocationQueryValue[]>;
  export type LocationQueryRaw = Record<string, string | number | null | undefined | Array<string | number | null | undefined>>;
  export type RouteLocationRaw = string | { name?: string; path?: string; query?: LocationQueryRaw; params?: Record<string, unknown> };
  export interface RouteRecordRaw {
    path: string;
    name?: string;
    component?: Component;
    redirect?: RouteLocationRaw;
    children?: RouteRecordRaw[];
    meta?: Record<string, unknown>;
    props?: unknown;
  }
}

declare module "@tanstack/vue-query" {
  export interface QueryClientOptions { defaultOptions?: Record<string, unknown>; }
  export class QueryClient {
    constructor(options?: QueryClientOptions);
    fetchQuery<T>(options: { queryKey: readonly unknown[] | unknown[]; queryFn: () => Promise<T> }): Promise<T>;
    invalidateQueries(options?: { queryKey?: readonly unknown[] | unknown[] }): Promise<void>;
  }
  export class MutationObserver<TData = unknown, TError = unknown, TVariables = void, TContext = unknown> {
    constructor(client: QueryClient, options: {
      mutationKey?: readonly unknown[] | unknown[];
      mutationFn: (variables: TVariables) => Promise<TData>;
    });
    mutate(variables: TVariables): Promise<TData>;
    reset(): void;
  }
}

declare module "vitest" {
  export const describe: (name: string, body: () => void) => void;
  export const it: (name: string, body: () => unknown | Promise<unknown>) => void;
  export const beforeEach: (body: () => unknown | Promise<unknown>) => void;
  export const afterEach: (body: () => unknown | Promise<unknown>) => void;
  export const expect: any;
  export const vi: any;
}
interface ImportMetaEnv { readonly [key: string]: string | boolean | undefined; }
interface ImportMeta { readonly env: ImportMetaEnv; }

declare module "*orval-mutator" {
  export class CpfOrvalError extends Error {
    readonly status: number;
    readonly payload: unknown;
  }
  export function cpfOrvalRequest<T = unknown>(config: Record<string, unknown>): Promise<T>;
}

declare module "*cpf-operation-contract" {
  export type CpfOperationId = string;
  export interface CpfOperationDescriptor { operationId: CpfOperationId; method: string; template: string; }
  export const cpfOperationDescriptors: readonly CpfOperationDescriptor[];
  export function resolveCpfOperation(method: string, path: string): { operationId: CpfOperationId };
}

declare module "*queryClient" {
  import type { QueryClient } from "@tanstack/vue-query";
  export const cpfQueryClient: QueryClient;
}


declare module "node:fs" {
  export function readFileSync(path: unknown, encoding: string): string;
}
declare module "node:url" {
  export function fileURLToPath(url: URL): string;
}
declare module "node:fs" { export function readFileSync(path: string | URL, encoding: "utf8" | "utf-8"): string; }
declare module "node:url" { export function fileURLToPath(url: string | URL): string; }
