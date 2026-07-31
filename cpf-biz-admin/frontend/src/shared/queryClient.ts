import { QueryClient } from "@tanstack/vue-query";

export const cpfQueryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 15_000, refetchOnWindowFocus: false },
    mutations: { retry: false }
  }
});
