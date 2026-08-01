import { storeToRefs } from "pinia";
import { admConsoleActionNames, useAdmConsoleStore } from "../stores/admConsoleStore";

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
  const actions = Object.fromEntries(admConsoleActionNames.map(name => [name, (...args: unknown[]) => {
    const action = (store as unknown as Record<string, unknown>)[name];
    if (typeof action !== "function") throw new Error(`ADM store action is not available: ${name}`);
    return (action as (...values: unknown[]) => unknown)(...args);
  }]));
  return { ...stateAndGetters, ...actions };
}
