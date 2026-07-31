import { defineStore } from "pinia";
import { admApi } from "../shared/cpfApi";
import { createAdmState } from "../state/createAdmState";
import { accessMethods } from "../app/methods/accessMethods";
import { batchMethods } from "../features/batch/methods";
import { approvalMethods } from "../features/approvals/methods";
import { coreMethods } from "../features/core/methods";
import { observabilityMethods } from "../app/methods/observabilityMethods";
import { platformMethods } from "../app/methods/platformMethods";
import { referenceMethods } from "../app/methods/referenceMethods";

const actions = {
  ...accessMethods,
  ...approvalMethods,
  ...batchMethods,
  ...coreMethods,
  ...observabilityMethods,
  ...platformMethods,
  ...referenceMethods,
  async restoreServerSession(this: any) {
    try {
      const data: any = await admApi("/adm/api/auth/session");
      this.currentOperator = data.operator || {};
      this.authorizedMenus = Array.isArray(data.menus) ? data.menus : [];
      this.authorizedButtons = Array.isArray(data.buttonIds) ? data.buttonIds : [];
      this.permissionsLoaded = Array.isArray(data.menus);
      this.buttonsLoaded = Array.isArray(data.buttonIds);
      this.sessionLoaded = Boolean(data.operator?.operatorId);
    } catch {
      this.clearSession("");
    }
  },
  clearSession(this: any, message = "") {
    this.sessionLoaded = false;
    this.currentOperator = {};
    this.authorizedMenus = [];
    this.authorizedButtons = [];
    this.permissionsLoaded = false;
    this.buttonsLoaded = false;
    if (typeof this.resetSensitiveState === "function") this.resetSensitiveState();
    if (message) this.authMessage = message;
  }
};

export const admConsoleActionNames = Object.freeze(Object.keys(actions));

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
    channelItems: state => Object.values(state.channelSnapshot?.channels || {}),
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
  actions: actions as any
});
