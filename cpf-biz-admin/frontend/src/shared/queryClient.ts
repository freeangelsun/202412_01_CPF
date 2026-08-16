import { QueryClient } from "@tanstack/vue-query";
import { bzaOperationState } from "./operationState";

/** BZA Generated Client의 query/mutation 재시도 정책을 단일 위치에서 관리합니다. */
export const cpfQueryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => bzaOperationState(error).retryable && failureCount < 2,
      staleTime: 15_000,
      gcTime: 5 * 60_000,
      refetchOnWindowFocus: false,
    },
    mutations: { retry: false },
  },
});
