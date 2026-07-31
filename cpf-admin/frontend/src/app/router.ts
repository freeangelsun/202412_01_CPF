import { createRouter, createWebHistory } from "vue-router";
import { admRouterRecords } from "./routes";

export const admRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...admRouterRecords,
    { path: "/:pathMatch(.*)*", redirect: { name: "dashboard" } }
  ],
  scrollBehavior: () => ({ top: 0 })
});
