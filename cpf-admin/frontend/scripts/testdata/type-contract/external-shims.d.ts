declare module "vue" {
  export type MaybeRefOrGetter<T> = T | { value: T } | (() => T);
  export interface ComputedRef<T> { readonly value: T; }
  export function computed<T>(getter: () => T): ComputedRef<T>;
  export function toValue<T>(source: MaybeRefOrGetter<T>): T;
  export function unref<T>(source: T | { value: T }): T;
}

declare module "@tanstack/vue-query" {
  export type QueryKey = readonly unknown[];
  export type DataTag<TTag, _TData, _TError> = TTag & { readonly __dataTag?: unique symbol };
  export interface QueryClient {}
  export type QueryFunction<TData> = (context: { signal?: AbortSignal }) => TData | Promise<TData>;
  export interface UseQueryOptions<TQueryFnData = unknown, TError = unknown, TData = TQueryFnData> {
    queryKey?: QueryKey;
    queryFn?: QueryFunction<TQueryFnData>;
    enabled?: boolean | { readonly value: boolean };
    select?: (data: TQueryFnData) => TData;
    [key: string]: unknown;
  }
  export type UseQueryReturnType<TData, TError> = {
    data?: TData;
    error?: TError;
    queryKey?: QueryKey;
  };
  export function useQuery<TQueryFnData, TError, TData>(
    options: UseQueryOptions<TQueryFnData, TError, TData>,
    queryClient?: QueryClient
  ): UseQueryReturnType<TData, TError>;

  export type MutationFunction<TData, TVariables> = (variables: TVariables) => Promise<TData>;
  export interface UseMutationOptions<TData = unknown, TError = unknown, TVariables = void, TContext = unknown> {
    mutationKey?: readonly unknown[];
    mutationFn?: MutationFunction<TData, TVariables>;
    onMutate?: (variables: TVariables) => TContext | Promise<TContext>;
    [key: string]: unknown;
  }
  export type UseMutationReturnType<TData, TError, TVariables, TContext> = {
    mutate: (variables: TVariables) => void;
    mutateAsync: MutationFunction<TData, TVariables>;
    error?: TError;
    context?: TContext;
  };
  export function useMutation<TData, TError, TVariables, TContext>(
    options: UseMutationOptions<TData, TError, TVariables, TContext>,
    queryClient?: QueryClient
  ): UseMutationReturnType<TData, TError, TVariables, TContext>;
}
