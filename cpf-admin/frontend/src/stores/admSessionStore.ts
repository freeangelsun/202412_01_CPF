import { defineStore } from "pinia";

export interface AdmOperatorSession {
  operator: Record<string, any>;
  menus: any[];
  buttonIds: string[];
}

export const useAdmSessionStore = defineStore("adm-session", {
  state: () => ({
    loaded: false,
    operator: {} as Record<string, any>,
    menus: [] as any[],
    buttonIds: [] as string[]
  }),
  getters: {
    authenticated: state => Boolean(state.loaded && state.operator?.operatorId),
    passwordChangeRequired: state => state.operator?.passwordChangeRequired === true
  },
  actions: {
    replace(session: AdmOperatorSession) {
      this.operator = session.operator || {};
      this.menus = Array.isArray(session.menus) ? session.menus : [];
      this.buttonIds = Array.isArray(session.buttonIds) ? session.buttonIds : [];
      this.loaded = Boolean(this.operator?.operatorId);
    },
    clear() {
      this.loaded = false;
      this.operator = {};
      this.menus = [];
      this.buttonIds = [];
    }
  }
});
