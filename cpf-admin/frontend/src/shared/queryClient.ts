import { QueryClient } from "@tanstack/vue-query";

/** Shared owner for all ADM generated-client query and mutation state. */
export const cpfQueryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        const status = typeof error === "object" && error !== null && "status" in error
          ? Number((error as { status?: unknown }).status)
          : 0;
        if ([401, 403, 404, 409].includes(status)) return false;
        return failureCount < 2;
      },
      staleTime: 15_000,
      gcTime: 5 * 60_000,
      refetchOnWindowFocus: false,
    },
    mutations: { retry: false },
  },
});
