import { createRouter, createWebHistory } from "vue-router";
import { bzaRouterRecords } from "./routes";
import { authenticated, hasBzaMenu, restoreBzaSession } from "../features/auth/session";
import RouteStatusPage from "../features/errors/RouteStatusPage.vue";

let restored = false;
const STATUS_ROUTES = new Set(["forbidden", "feature-disabled", "lazy-load-failure", "not-found"]);

export const bzaRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...bzaRouterRecords,
    {
      path: "/forbidden",
      name: "forbidden",
      component: RouteStatusPage,
      props: { code: 403, title: "접근 권한이 없습니다.", message: "업무 메뉴 권한을 확인하세요." },
      meta: { publicStatus: true }
    },
    {
      path: "/feature-disabled",
      name: "feature-disabled",
      component: RouteStatusPage,
      props: { code: 404, title: "비활성화된 기능입니다.", message: "현재 Tenant 또는 업무 환경에서 이 기능이 활성화되지 않았습니다." },
      meta: { publicStatus: true }
    },
    {
      path: "/lazy-load-failure",
      name: "lazy-load-failure",
      component: RouteStatusPage,
      props: { code: 503, title: "화면 모듈을 불러오지 못했습니다.", message: "배포 Artifact와 브라우저 캐시 또는 네트워크 상태를 확인한 뒤 다시 시도하세요." },
      meta: { publicStatus: true }
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: RouteStatusPage,
      props: { code: 404, title: "화면을 찾을 수 없습니다.", message: "URL과 배포된 업무 메뉴를 확인하세요." },
      meta: { publicStatus: true }
    }
  ],
  scrollBehavior: () => ({ top: 0 })
});

bzaRouter.beforeEach(async (to) => {
  if (STATUS_ROUTES.has(String(to.name || "")) || to.meta.publicStatus === true) return true;
  if (!restored) {
    restored = true;
    await restoreBzaSession();
  }
  if (!authenticated.value) return to.name === "dashboard" ? true : { name: "dashboard", query: { loginRequired: "true" } };
  const menuCode = String(to.meta.menuCode || "");
  if (!menuCode) return { name: "not-found", query: { from: to.fullPath } };
  return hasBzaMenu(menuCode)
    ? true
    : { name: "forbidden", query: { from: to.fullPath, menuCode } };
});


bzaRouter.onError((error, to) => {
  console.error("BZA route lazy-load failure", { route: to.fullPath, error });
  if (to.name !== "lazy-load-failure") void bzaRouter.replace({ name: "lazy-load-failure", query: { from: to.fullPath } });
});
