import { createRouter, createWebHistory } from "vue-router";
import { bzaRouterRecords } from "./routes";
import { authenticated, hasBzaMenu, restoreBzaSession } from "../features/auth/session";
let restored = false;
export const bzaRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...bzaRouterRecords, { path: "/:pathMatch(.*)*", redirect: { name: "dashboard" } }],
  scrollBehavior: () => ({ top: 0 })
});
bzaRouter.beforeEach(async to => {
  if (!restored) { restored = true; await restoreBzaSession(); }
  if (!authenticated.value) return to.name === "dashboard" ? true : { name: "dashboard" };
  const menuCode = String(to.meta.menuCode || "DASHBOARD");
  return hasBzaMenu(menuCode) ? true : { name: "dashboard", query: { denied: String(to.name || "") } };
});
