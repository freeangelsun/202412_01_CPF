import { defineComponent } from "vue";
import { accessMethods } from "../features/access/methods";
import { batchMethods } from "../features/batch/methods";
import { approvalMethods } from "../features/approvals/methods";
import { coreMethods } from "../features/core/methods";
import { observabilityMethods } from "../features/observability/methods";
import { platformMethods } from "../features/platform/methods";
import { referenceMethods } from "../features/reference/methods";
import { admSharedState } from "../state/admSharedState";

export const admConsoleMixin = defineComponent({
    data: () => admSharedState,
    computed: {
      authenticated() {
        return !!this.token;
      },
      passwordChangeRequired() {
        return this.currentOperator.passwordChangeRequired === true;
      },
      visibleMenus() {
        if (!this.authorizedMenus.length) {
          return this.menus;
        }
        const allowed = new Set(this.authorizedMenus.map(menu => menu.menuId || menu.id));
        return this.menus.filter(menu => allowed.has(menu.menuId));
      },
      channelItems(): any[] {
        return Object.values(this.channelSnapshot.channels || {}) as any[];
      },
      sortedLogs() {
        const items = [...this.logs];
        const { key, direction } = this.logSort;
        items.sort((left, right) => {
          const a = left?.[key] ?? "";
          const b = right?.[key] ?? "";
          if (a === b) return 0;
          return (a > b ? 1 : -1) * (direction === "asc" ? 1 : -1);
        });
        return items;
      },
      pagedLogs() {
        const start = (this.logPage.page - 1) * this.logPage.size;
        return this.sortedLogs.slice(start, start + this.logPage.size);
      },
      logTotalPages() {
        return Math.max(1, Math.ceil(this.sortedLogs.length / this.logPage.size));
      },
      transactionGroups() {
        return this.transactionGroupResult?.items || [];
      },
      pagedTransactionGroups() {
        const start = (this.transactionGroupPage.page - 1) * this.transactionGroupPage.size;
        return this.transactionGroups.slice(start, start + this.transactionGroupPage.size);
      },
      transactionGroupTotalPages() {
        return Math.max(1, Math.ceil(this.transactionGroups.length / this.transactionGroupPage.size));
      },
      activeTransactionGroupPayload() {
        const detail = this.transactionGroupDetail || {};
        const headerItems = detail.headers?.headers || detail.headers || [];
        const standardHeaders = Array.isArray(headerItems)
          ? headerItems.map(item => ({
              transactionSegmentId: item.transactionSegmentId,
              requestHeaderSnapshotMasked: item.requestHeaderSnapshotMasked,
              responseHeaderSnapshotMasked: item.responseHeaderSnapshotMasked
            }))
          : headerItems;
        const extensionHeaders = Array.isArray(headerItems)
          ? headerItems.map(item => ({
              transactionSegmentId: item.transactionSegmentId,
              extensionHeaderSnapshotMasked: item.extensionHeaderSnapshotMasked
            }))
          : headerItems;
        const tabMap = {
          요약: detail.summary || {},
          Timeline: detail.timeline?.items || detail.timeline || [],
          Segments: detail.segments?.items || detail.segments || [],
          "표준 헤더": standardHeaders,
          "확장 헤더": extensionHeaders,
          "External Logs": detail.externalLogs?.items || detail.externalLogs || [],
          "원본 JSON": detail
        };
        return this.pretty(tabMap[this.transactionGroupDetailTab] || {});
      },
      activeLogDetailPayload() {
        const detail = this.logDetail?.item || this.logDetail || {};
        const tabMap = {
          요약: detail.summary || detail,
          "수신 헤더": detail.inboundHeaders || detail.headers || detail.HEADERS || {},
          "해석 헤더": detail.resolvedHeaders || detail.headers || detail.HEADERS || {},
          "전파 헤더": detail.outboundHeaders || {},
          "응답 헤더": detail.responseHeaders || {},
          요청: detail.request || detail.REQUEST_BODY || {},
          응답: detail.response || detail.RESPONSE || {},
          오류: detail.error || detail.ERROR_MESSAGE || {},
          상세: detail.formattedDetails || detail.details || [],
          전문: this.fixedLengthDetails(detail)
        };
        return this.pretty(tabMap[this.logDetailTab] || {});
      }
    },
    watch: {
      logs() {
        this.logPage.page = 1;
      },
      transactionGroupResult() {
        this.transactionGroupPage.page = 1;
      }
    },
    methods: {
      ...accessMethods,
      ...approvalMethods,
      ...batchMethods,
      ...coreMethods,
      ...observabilityMethods,
      ...platformMethods,
      ...referenceMethods
    }
  });
