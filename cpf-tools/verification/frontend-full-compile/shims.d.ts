declare module "*.vue" {
  import type { Component } from "vue";
  const component: Component;
  export default component;
}
declare module "*.vue?raw" { const source: string; export default source; }
declare module "*.css" { const source: string; export default source; }

declare module "vue" {
  export type Component = unknown;
  export type MaybeRefOrGetter<T> = T | Ref<T> | (() => T);
  export interface Ref<T = unknown> { value: T; }
  export interface ComputedRef<T = unknown> extends Ref<T> { readonly value: T; }
  export function ref<T>(value: T): Ref<T>;
  export function ref<T = undefined>(): Ref<T | undefined>;
  export function reactive<T extends object>(target: T): T;
  export function computed<T>(getter: () => T): ComputedRef<T>;
  export function defineAsyncComponent<T extends Component = Component>(loader: () => Promise<T | { default: T }>): T;
  export function toValue<T>(value: MaybeRefOrGetter<T>): T;
  export function unref<T>(value: T | Ref<T>): T;
  export function onMounted(callback: () => unknown | Promise<unknown>): void;
  export function nextTick<T = void>(fn?: () => T): Promise<Awaited<T>>;
  export function createApp(root: Component): {
    component(name: string, component: Component): any;
    use(plugin: unknown, ...options: unknown[]): any;
    mount(target: string | Element): unknown;
  };
}

declare module "pinia" {
  import type { Ref } from "vue";
  type GetterResults<G> = { readonly [K in keyof G]: G[K] extends (...args: any[]) => infer R ? R : never };
  export interface Pinia { readonly __piniaBrand?: true; }
  export function createPinia(): Pinia;
  export function setActivePinia(pinia: Pinia): Pinia;
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
  export type RouteLocationRaw = string | { name?: string; path?: string; query?: LocationQueryRaw; params?: Record<string, unknown>; hash?: string; replace?: boolean };
  export interface RouteLocationNormalizedLoaded {
    name?: string | symbol | null;
    path: string;
    fullPath: string;
    hash: string;
    query: LocationQuery;
    params: Record<string, unknown>;
    meta: Record<string, unknown>;
  }
  export interface RouteRecordRaw {
    path: string;
    name?: string;
    component?: Component;
    redirect?: RouteLocationRaw;
    children?: RouteRecordRaw[];
    meta?: Record<string, unknown>;
    props?: unknown;
  }
  export interface Router {
    beforeEach(guard: (to: RouteLocationNormalizedLoaded, from: RouteLocationNormalizedLoaded) => unknown): void;
    onError(handler: (error: unknown, to: RouteLocationNormalizedLoaded) => void): void;
    replace(to: RouteLocationRaw): Promise<unknown>;
    push(to: RouteLocationRaw): Promise<unknown>;
    hasRoute(name: string | symbol): boolean;
    resolve(to: RouteLocationRaw): RouteLocationNormalizedLoaded;
  }
  export function createWebHistory(base?: string): unknown;
  export function createRouter(options: { history: unknown; routes: RouteRecordRaw[]; scrollBehavior?: (...args: any[]) => unknown }): Router;
  export function useRoute(): RouteLocationNormalizedLoaded;
  export function useRouter(): Router;
}

declare module "@tanstack/vue-query" {
  import type { Ref } from "vue";
  export type QueryKey = readonly unknown[];
  export type DataTag<TType, TValue, TError = unknown> = TType & { readonly __dataTag?: [TValue, TError] };
  export type QueryFunction<T = unknown, TKey extends QueryKey = QueryKey> = (context: { queryKey: TKey; signal: AbortSignal }) => T | Promise<T>;
  export type MutationFunction<TData = unknown, TVariables = void> = (variables: TVariables) => Promise<TData>;
  export interface QueryClientOptions { defaultOptions?: Record<string, unknown>; }
  export class QueryClient {
    constructor(options?: QueryClientOptions);
    fetchQuery<T>(options: { queryKey: QueryKey; queryFn: () => Promise<T> }): Promise<T>;
    invalidateQueries(options?: { queryKey?: QueryKey }): Promise<void>;
  }
  export interface UseQueryOptions<TQueryFnData = unknown, TError = unknown, TData = TQueryFnData, TQueryData = TQueryFnData, TQueryKey extends QueryKey = QueryKey> {
    queryKey?: TQueryKey;
    queryFn?: QueryFunction<TQueryFnData, TQueryKey>;
    enabled?: boolean | Ref<boolean>;
    select?: (data: TQueryData) => TData;
    [key: string]: unknown;
  }
  export interface UseQueryReturnType<TData = unknown, TError = unknown> {
    data: Ref<TData | undefined>;
    error: Ref<TError | null>;
    isPending: Ref<boolean>;
    refetch: () => Promise<unknown>;
  }
  export interface UseMutationOptions<TData = unknown, TError = unknown, TVariables = void, TContext = unknown> {
    mutationKey?: QueryKey;
    mutationFn?: MutationFunction<TData, TVariables>;
    [key: string]: unknown;
  }
  export interface UseMutationReturnType<TData = unknown, TError = unknown, TVariables = void, TContext = unknown> {
    data: Ref<TData | undefined>;
    error: Ref<TError | null>;
    mutate: (variables: TVariables) => void;
    mutateAsync: (variables: TVariables) => Promise<TData>;
    reset: () => void;
  }
  export function useQuery<TQueryFnData = unknown, TError = unknown, TData = TQueryFnData, TQueryKey extends QueryKey = QueryKey>(options: UseQueryOptions<TQueryFnData, TError, TData, TQueryFnData, TQueryKey>, queryClient?: QueryClient): UseQueryReturnType<TData, TError>;
  export function useMutation<TData = unknown, TError = unknown, TVariables = void, TContext = unknown>(options: UseMutationOptions<TData, TError, TVariables, TContext>, queryClient?: QueryClient): UseMutationReturnType<TData, TError, TVariables, TContext>;
  export const VueQueryPlugin: unknown;
  export class MutationObserver<TData = unknown, TError = unknown, TVariables = void, TContext = unknown> {
    constructor(client: QueryClient, options: UseMutationOptions<TData, TError, TVariables, TContext>);
    mutate(variables: TVariables): Promise<TData>;
    reset(): void;
  }
}

declare module "@vue/test-utils" {
  export interface DOMWrapper<T = Element> {
    text(): string;
    exists(): boolean;
    find(selector: string): DOMWrapper;
    findAll(selector: string): DOMWrapper[];
    get(selector: string): DOMWrapper;
    attributes(name?: string): any;
    setValue(value: unknown): Promise<void>;
    trigger(event: string): Promise<void>;
  }
  export interface VueWrapper<T = any> extends DOMWrapper<Element> {
    vm: T;
    emitted<T = unknown[]>(event?: string): Record<string, unknown[][]> | T[][] | undefined;
  }
  export function mount<T = any>(component: unknown, options?: Record<string, unknown>): VueWrapper<T>;
}

declare module "element-plus" { const plugin: unknown; export default plugin; }
declare module "element-plus/dist/index.css" { const css: string; export default css; }

declare module "vitest" {
  export const describe: (name: string, body: () => void) => void;
  export interface TestApi {
    (name: string, body: () => unknown | Promise<unknown>): void;
    each(cases: readonly (readonly unknown[])[]): (name: string, body: (...args: any[]) => unknown | Promise<unknown>) => void;
  }
  export const it: TestApi;
  export const test: TestApi;
  export const beforeEach: (body: () => unknown | Promise<unknown>) => void;
  export const afterEach: (body: () => unknown | Promise<unknown>) => void;
  export const expect: any;
  export const vi: any;
}
interface ImportMetaEnv {
  readonly BASE_URL: string;
  readonly MODE?: string;
  readonly DEV?: boolean;
  readonly PROD?: boolean;
  readonly SSR?: boolean;
  readonly [key: string]: string | boolean | undefined;
  readonly VITE_MBW_WEB_BASE_URL?: string;
}
interface ImportMeta { readonly env: ImportMetaEnv; }

declare module "*orval-mutator" {
  export class CpfOrvalError extends Error { readonly status: number; readonly payload: unknown; }
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
declare module "node:fs" { export function readFileSync(path: string | URL, encoding: "utf8" | "utf-8"): string; }
declare module "node:url" { export function fileURLToPath(url: string | URL): string; }
