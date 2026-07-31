import { createRouter, createWebHistory } from "vue-router";
import { bzaRouterRecords } from "./routes";
import { authenticated, hasBzaMenu, restoreBzaSession } from "../features/auth/session";
import RouteStatusPage from "../features/errors/RouteStatusPage.vue";

let restored = false;
const STATUS_ROUTES = new Set(["forbidden", "not-found"]);

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
  const menuCode = String(to.meta.menuCode || "DASHBOARD");
  return hasBzaMenu(menuCode)
    ? true
    : { name: "forbidden", query: { from: to.fullPath, menuCode } };
});
