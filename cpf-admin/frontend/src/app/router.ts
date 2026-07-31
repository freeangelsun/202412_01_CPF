import { createRouter, createWebHistory } from "vue-router";
import { admRouterRecords } from "./routes";
import { useAdmSessionStore } from "../stores/admSessionStore";
import RouteStatusPage from "../features/errors/RouteStatusPage.vue";

const STATUS_ROUTES = new Set(["forbidden", "not-found"]);

export const admRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...admRouterRecords,
    {
      path: "/forbidden",
      name: "forbidden",
      component: RouteStatusPage,
      props: { code: 403, title: "접근 권한이 없습니다.", message: "권한 변경이 필요한 경우 승인 사유와 함께 관리자에게 요청하세요." },
      meta: { publicStatus: true }
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: RouteStatusPage,
      props: { code: 404, title: "화면을 찾을 수 없습니다.", message: "배포 버전과 URL 또는 메뉴 구성을 확인하세요." },
      meta: { publicStatus: true }
    }
  ],
  scrollBehavior: () => ({ top: 0 })
});

admRouter.beforeEach((to) => {
  if (STATUS_ROUTES.has(String(to.name || "")) || to.meta.publicStatus === true) return true;
  const session = useAdmSessionStore();
  // 로그인 화면은 App shell이 책임진다. Session projection이 준비된 뒤에만 메뉴 권한을 판정한다.
  if (!session.loaded) return true;
  const menuId = String(to.meta.menuId || "dashboard");
  if (menuId === "dashboard") return true;
  const allowed = new Set(session.menus.map((menu: any) => String(menu.menuId || menu.id || "")));
  return allowed.has(menuId)
    ? true
    : { name: "forbidden", query: { from: to.fullPath, menuId } };
});
