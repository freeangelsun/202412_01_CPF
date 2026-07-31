import { defineStore } from "pinia";

export interface AdmInitializationFailure { name: string; message: string; required: boolean; }
export type AdmInitializationStatus = "IDLE" | "LOADING" | "BLOCKED" | "FAILED" | "DEGRADED" | "READY";

export const useAdmInitializationStore = defineStore("adm-initialization", {
  state: () => ({ status: "IDLE" as AdmInitializationStatus, failures: [] as AdmInitializationFailure[] }),
  actions: {
    begin() { this.status = "LOADING"; this.failures = []; },
    blocked() { this.status = "BLOCKED"; },
    record(name: string, error: unknown, required: boolean) {
      const message = error instanceof Error ? error.message : String(error);
      this.failures.push({ name, message, required });
      this.status = required ? "FAILED" : "DEGRADED";
      return message;
    },
    complete() { this.status = this.failures.length ? "DEGRADED" : "READY"; }
  }
});
