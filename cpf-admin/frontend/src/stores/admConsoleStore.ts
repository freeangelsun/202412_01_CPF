import { defineStore } from "pinia";
import { composeAdmFeatureActions } from "./admFeatureActionRegistry";
import { useAdmSessionStore } from "./admSessionStore";
import { admAuthMe } from "../generated/cpf-api";
import { createAdmState } from "../state/createAdmState";
import { accessMethods } from "../app/methods/accessMethods";
import { batchMethods } from "../features/batch/methods";
import { approvalMethods } from "../features/approvals/methods";
import { coreMethods } from "../features/core/methods";
import { observabilityMethods } from "../app/methods/observabilityMethods";
import { platformMethods } from "../app/methods/platformMethods";
import { referenceMethods } from "../app/methods/referenceMethods";
import { routeClosureMethods } from "../app/methods/routeClosureMethods";
import { integrationClosureMethods } from "../app/methods/integrationClosureMethods";
import { healthMethods } from "../features/health/methods";

const sessionActions = {
  async restoreServerSession(this: any) {
    try {
      const data: any = await admAuthMe<any>();
      const session = useAdmSessionStore();
      session.replace({ operator: data.operator || (data.operatorId ? data : {}), menus: data.menus || [], buttonIds: data.buttonIds || [] });
      this.currentOperator = session.operator;
      this.authorizedMenus = session.menus;
      this.authorizedButtons = session.buttonIds;
      this.permissionsLoaded = Array.isArray(data.menus);
      this.buttonsLoaded = Array.isArray(data.buttonIds);
      this.sessionLoaded = session.loaded;
    } catch {
      this.clearSession("");
    }
  },
  clearSession(this: any, message = "") {
    const session = useAdmSessionStore();
    session.clear();
    this.sessionLoaded = session.loaded;
    this.currentOperator = session.operator;
    this.authorizedMenus = session.menus;
    this.authorizedButtons = session.buttonIds;
    this.permissionsLoaded = false;
    this.buttonsLoaded = false;
    if (typeof this.resetSensitiveState === "function") this.resetSensitiveState();
    if (message) this.authMessage = message;
  }
};

const actions = composeAdmFeatureActions([
  { owner: "access", actions: accessMethods },
  { owner: "approvals", actions: approvalMethods },
  { owner: "batch", actions: batchMethods },
  { owner: "core", actions: coreMethods },
  { owner: "observability", actions: observabilityMethods },
  { owner: "platform", actions: platformMethods },
  { owner: "reference", actions: referenceMethods },
  { owner: "route-closure", actions: routeClosureMethods },
  { owner: "integration-closure", actions: integrationClosureMethods },
  { owner: "health", actions: healthMethods },
  { owner: "session", actions: sessionActions }
 ] as const);

export type AdmConsoleActions = typeof actions;
export const admConsoleActionNames = Object.freeze(Object.keys(actions) as Array<keyof AdmConsoleActions>);

export const useAdmConsoleStore = defineStore("adm-console", {
  state: () => ({ ...createAdmState(), sessionLoaded: false }),
  getters: {
    authenticated: state => Boolean(state.sessionLoaded && state.currentOperator?.operatorId),
    passwordChangeRequired: state => state.currentOperator?.passwordChangeRequired === true,
    visibleMenus(state): any[] {
      if (!this.authenticated || !state.permissionsLoaded || !state.authorizedMenus.length) return [];
      const allowed = new Set(state.authorizedMenus.map((menu: any) => menu.menuId || menu.id));
      return state.menus.filter((menu: any) => allowed.has(menu.menuId));
    },
    channelItems: (state): any[] => Object.values(state.channelSnapshot?.channels || {}) as any[],
    sortedLogs(state): any[] {
      const items = [...state.logs];
      const { key, direction } = state.logSort;
      return items.sort((left: any, right: any) => {
        const a = left?.[key] ?? "";
        const b = right?.[key] ?? "";
        return a === b ? 0 : (a > b ? 1 : -1) * (direction === "asc" ? 1 : -1);
      });
    },
    pagedLogs(): any[] {
      const start = (this.logPage.page - 1) * this.logPage.size;
      return this.sortedLogs.slice(start, start + this.logPage.size);
    },
    logTotalPages(): number { return Math.max(1, Math.ceil(this.sortedLogs.length / this.logPage.size)); },
    transactionGroups: state => state.transactionGroupResult?.items || [],
    pagedTransactionGroups(): any[] {
      const start = (this.transactionGroupPage.page - 1) * this.transactionGroupPage.size;
      return this.transactionGroups.slice(start, start + this.transactionGroupPage.size);
    },
    transactionGroupTotalPages(): number { return Math.max(1, Math.ceil(this.transactionGroups.length / this.transactionGroupPage.size)); },
    activeTransactionGroupPayload(state): string {
      const detail = state.transactionGroupDetail || {};
      const headerItems = detail.headers?.headers || detail.headers || [];
      const standardHeaders = Array.isArray(headerItems) ? headerItems.map((item: any) => ({
        transactionSegmentId: item.transactionSegmentId,
        requestHeaderSnapshotMasked: item.requestHeaderSnapshotMasked,
        responseHeaderSnapshotMasked: item.responseHeaderSnapshotMasked
      })) : headerItems;
      const extensionHeaders = Array.isArray(headerItems) ? headerItems.map((item: any) => ({
        transactionSegmentId: item.transactionSegmentId,
        extensionHeaderSnapshotMasked: item.extensionHeaderSnapshotMasked
      })) : headerItems;
      const tabMap: Record<string, unknown> = {
        요약: detail.summary || {}, Timeline: detail.timeline?.items || detail.timeline || [],
        Segments: detail.segments?.items || detail.segments || [], "표준 헤더": standardHeaders,
        "확장 헤더": extensionHeaders, "External Logs": detail.externalLogs?.items || detail.externalLogs || [],
        "원본 JSON": detail
      };
      return this.pretty(tabMap[state.transactionGroupDetailTab] || {});
    },
    activeLogDetailPayload(state): string {
      const detail = state.logDetail?.item || state.logDetail || {};
      const tabMap: Record<string, unknown> = {
        요약: detail.summary || detail, "수신 헤더": detail.inboundHeaders || detail.headers || detail.HEADERS || {},
        "해석 헤더": detail.resolvedHeaders || detail.headers || detail.HEADERS || {}, "전파 헤더": detail.outboundHeaders || {},
        "응답 헤더": detail.responseHeaders || {}, 요청: detail.request || detail.REQUEST_BODY || {},
        응답: detail.response || detail.RESPONSE || {}, 오류: detail.error || detail.ERROR_MESSAGE || {},
        상세: detail.formattedDetails || detail.details || [], 전문: this.fixedLengthDetails(detail)
      };
      return this.pretty(tabMap[state.logDetailTab] || {});
    }
  },
  actions: { ...actions }
});
