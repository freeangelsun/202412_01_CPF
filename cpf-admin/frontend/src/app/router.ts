import { createRouter, createWebHistory } from "vue-router";
import { admRouterRecords, findCapabilityByRouteName } from "./routes";
import { useAdmSessionStore } from "../stores/admSessionStore";
import RouteStatusPage from "../features/errors/RouteStatusPage.vue";
import { causalContextQuery, contextEqualsQuery } from "./causalContext";

const STATUS_ROUTES = new Set(["forbidden", "feature-disabled", "lazy-load-failure", "not-found"]);

export const admRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...admRouterRecords,
    {
      path: "/forbidden",
      name: "forbidden",
      component: RouteStatusPage,
      props: { code: 403, title: "접근 권한이 없습니다.", message: "메뉴 읽기 권한과 데이터 범위를 확인하고 승인 절차를 진행하세요." },
      meta: { publicStatus: true }
    },
    {
      path: "/feature-disabled",
      name: "feature-disabled",
      component: RouteStatusPage,
      props: { code: 404, title: "비활성화된 기능입니다.", message: "현재 환경 또는 Tenant에서 이 기능이 활성화되지 않았습니다." },
      meta: { publicStatus: true }
    },
    {
      path: "/lazy-load-failure",
      name: "lazy-load-failure",
      component: RouteStatusPage,
      props: { code: 503, title: "화면 모듈을 불러오지 못했습니다.", message: "배포 Artifact와 브라우저 캐시 또는 네트워크 상태를 확인한 뒤 다시 시도하세요.", retryable: true },
      meta: { publicStatus: true }
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: RouteStatusPage,
      props: { code: 404, title: "화면을 찾을 수 없습니다.", message: "Dashboard로 자동 대체하지 않습니다. 배포 버전과 URL 또는 Menu Registry를 확인하세요." },
      meta: { publicStatus: true }
    }
  ],
  scrollBehavior: () => ({ top: 0 })
});

admRouter.beforeEach((to) => {
  if (STATUS_ROUTES.has(String(to.name || "")) || to.meta.publicStatus === true) return true;
  const capability = findCapabilityByRouteName(to.name);
  if (!capability) return { name: "not-found", query: { from: to.fullPath } };
  const session = useAdmSessionStore();
  session.hydrateCausalContext();
  session.updateCausalContext(to.query as Record<string, unknown>);
  // Causal identifiers are navigation context only. Permission and masking are always recomputed
  // from the authenticated server session and are never trusted from a query parameter.
  if (!contextEqualsQuery(session.causalContext, to.query)) {
    return { path: to.path, hash: to.hash, query: { ...causalContextQuery(session.causalContext), ...to.query }, replace: true };
  }
  // App shell owns login. Authorization is evaluated only after the server session projection is loaded.
  if (!session.loaded) return true;
  if (!session.canAccessRoute(capability.routeId, capability.menuId, capability.path)) {
    return { name: "forbidden", query: { from: to.fullPath, menuId: capability.menuId, routeId: capability.routeId } };
  }
  if (!session.isFeatureEnabled(capability.routeId, capability.menuId, capability.path)) {
    return { name: "feature-disabled", query: { from: to.fullPath, featureFlag: capability.featureFlag } };
  }
  return true;
});

admRouter.onError((error, to) => {
  console.error("ADM route lazy-load failure", { route: to.fullPath, error });
  if (to.name !== "lazy-load-failure") {
    void admRouter.replace({ name: "lazy-load-failure", query: { from: to.fullPath } });
  }
});
