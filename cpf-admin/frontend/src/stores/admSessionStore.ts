import { defineStore } from "pinia";
import { mergeCausalContext, parseCausalContext, type CpfCausalContext } from "../app/causalContext";

export interface AdmMenuProjection {
  menuId?: string;
  id?: string;
  path?: string;
  readAllowed?: boolean;
  writeAllowed?: boolean;
  deleteAllowed?: boolean;
  enabled?: boolean;
  featureEnabled?: boolean;
  [key: string]: unknown;
}

export interface AdmOperatorSession {
  operator: Record<string, any>;
  menus: AdmMenuProjection[];
  buttonIds: string[];
}

function normalized(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

export const useAdmSessionStore = defineStore("adm-session", {
  state: () => ({
    loaded: false,
    operator: {} as Record<string, any>,
    menus: [] as AdmMenuProjection[],
    buttonIds: [] as string[],
    causalContext: {} as CpfCausalContext,
    causalContextHydrated: false
  }),
  getters: {
    authenticated: state => Boolean(state.loaded && state.operator?.operatorId),
    passwordChangeRequired: state => state.operator?.passwordChangeRequired === true,
    menuKeySet: state => {
      const keys = new Set<string>();
      for (const menu of state.menus) {
        for (const key of [menu.menuId, menu.id, menu.path]) {
          const value = normalized(key);
          if (value) keys.add(value);
        }
      }
      return keys;
    }
  },
  actions: {
    replace(session: AdmOperatorSession) {
      this.operator = session.operator || {};
      this.menus = Array.isArray(session.menus) ? session.menus : [];
      this.buttonIds = Array.isArray(session.buttonIds) ? session.buttonIds : [];
      this.loaded = Boolean(this.operator?.operatorId);
    },
    hydrateCausalContext() {
      if (this.causalContextHydrated) return;
      this.causalContextHydrated = true;
      if (typeof sessionStorage === "undefined") return;
      try {
        const value = JSON.parse(sessionStorage.getItem("cpf.adm.causalContext") || "{}");
        this.causalContext = parseCausalContext(value);
      } catch {
        sessionStorage.removeItem("cpf.adm.causalContext");
        this.causalContext = {};
      }
    },
    updateCausalContext(query: Record<string, unknown>) {
      this.hydrateCausalContext();
      this.causalContext = mergeCausalContext(this.causalContext, query);
      if (typeof sessionStorage !== "undefined") {
        sessionStorage.setItem("cpf.adm.causalContext", JSON.stringify(this.causalContext));
      }
    },
    clearCausalContext() {
      this.causalContext = {};
      this.causalContextHydrated = true;
      if (typeof sessionStorage !== "undefined") sessionStorage.removeItem("cpf.adm.causalContext");
    },
    authorizationContext() {
      return {
        tenantId: normalized(this.operator?.tenantId || this.causalContext.tenantId),
        environment: normalized(this.operator?.environment || this.causalContext.environment),
        permissionVersion: normalized(this.operator?.permissionVersion),
        maskingPolicyVersion: normalized(this.operator?.maskingPolicyVersion)
      };
    },
    canAccessRoute(routeId: string, menuId: string, path: string): boolean {
      const match = this.menus.find(menu => {
        const keys = [menu.menuId, menu.id, menu.path].map(normalized);
        return keys.includes(routeId) || keys.includes(menuId) || keys.includes(path);
      });
      if (!match) return false;
      return match.readAllowed !== false;
    },
    menuProjection(routeId: string, menuId: string, path: string): AdmMenuProjection | undefined {
      return this.menus.find(menu => {
        const keys = [menu.menuId, menu.id, menu.path].map(normalized);
        return keys.includes(routeId) || keys.includes(menuId) || keys.includes(path);
      });
    },
    canWrite(routeId: string, menuId: string, path: string): boolean {
      const menu = this.menuProjection(routeId, menuId, path);
      return Boolean(menu && menu.readAllowed !== false && menu.writeAllowed === true);
    },
    canDelete(routeId: string, menuId: string, path: string): boolean {
      const menu = this.menuProjection(routeId, menuId, path);
      return Boolean(menu && menu.readAllowed !== false && menu.deleteAllowed === true);
    },
    hasButton(buttonId: string): boolean {
      return this.buttonIds.includes(buttonId);
    },
    isFeatureEnabled(routeId: string, menuId: string, path: string): boolean {
      const match = this.menus.find(menu => {
        const keys = [menu.menuId, menu.id, menu.path].map(normalized);
        return keys.includes(routeId) || keys.includes(menuId) || keys.includes(path);
      });
      return match ? match.enabled !== false && match.featureEnabled !== false : false;
    },
    clear() {
      this.loaded = false;
      this.operator = {};
      this.menus = [];
      this.buttonIds = [];
      this.clearCausalContext();
    }
  }
});
