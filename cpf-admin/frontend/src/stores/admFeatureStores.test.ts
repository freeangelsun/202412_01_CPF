import { beforeEach, describe, expect, it } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { useAdmInitializationStore } from "./admInitializationStore";
import { useAdmSessionStore } from "./admSessionStore";

describe("ADM feature stores", () => {
  beforeEach(() => setActivePinia(createPinia()));

  it("replaces and clears server-side operator session projection", () => {
    const store = useAdmSessionStore();
    store.replace({ operator: { operatorId: "ADM001" }, menus: [{ menuId: "LOG_LIST" }], buttonIds: ["LOG_SEARCH"] });
    expect(store.authenticated).toBe(true);
    store.clear();
    expect(store.authenticated).toBe(false);
    expect(store.buttonIds).toEqual([]);
  });

  it("distinguishes required failure from optional degraded state", () => {
    const store = useAdmInitializationStore();
    store.begin();
    store.record("optional", new Error("timeout"), false);
    store.complete();
    expect(store.status).toBe("DEGRADED");
    store.begin();
    store.record("required", new Error("forbidden"), true);
    expect(store.status).toBe("FAILED");
  });
});
