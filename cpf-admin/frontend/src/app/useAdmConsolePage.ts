import { storeToRefs } from "pinia";
import { admConsoleActionNames, useAdmConsoleStore, type AdmConsoleActions } from "../stores/admConsoleStore";

type BoundAdmConsoleActions = {
  -readonly [Name in keyof AdmConsoleActions]: AdmConsoleActions[Name] extends (...args: infer Args) => infer Result
    ? (...args: Args) => Result
    : never;
};

/**
 * ADM Page Composition bridge.
 *
 * <p>All state/getters remain owned by the single Pinia store. Feature pages receive
 * writable refs plus bound actions without creating a second state tree or issuing
 * direct HTTP requests.</p>
 */
export function useAdmConsolePage() {
  const store = useAdmConsoleStore();
  const stateAndGetters = storeToRefs(store);
  const actions = {} as BoundAdmConsoleActions;
  const writableActions = actions as unknown as Record<keyof AdmConsoleActions, (...args: unknown[]) => unknown>;
  for (const name of admConsoleActionNames) {
    writableActions[name] = (...args: unknown[]) => {
      const action = (store as unknown as Record<string, unknown>)[name as string];
      if (typeof action !== "function") throw new Error(`ADM store action is not available: ${String(name)}`);
      return (action as (...values: unknown[]) => unknown)(...args);
    };
  }
  return { ...stateAndGetters, ...actions };
}
