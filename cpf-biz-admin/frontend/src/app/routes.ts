import type { Component } from "vue";

export type BzaRouteId =
  | "dashboard" | "users" | "sessions" | "organizations" | "employees" | "roles" | "menus"
  | "permissions" | "approvals" | "audits" | "notifications" | "attachments" | "savedSearches"
  | "permissionTools" | "settings" | "downloads";

export interface BzaRoute {
  id: BzaRouteId;
  label: string;
  menuCode: string;
  group: "overview" | "people" | "access" | "approval" | "support";
  description: string;
  load: () => Promise<{ default: Component }>;
}

const dashboard = () => import("../features/dashboard/DashboardPage.vue");
const directory = () => import("../features/directory/DirectoryPage.vue");
const access = () => import("../features/access/AccessPage.vue");
const approval = () => import("../features/approval/ApprovalPage.vue");
const support = () => import("../features/support/SupportPage.vue");

export const bzaRoutes: BzaRoute[] = [
  { id: "dashboard", label: "대시보드", menuCode: "DASHBOARD", group: "overview", description: "업무 운영 현황", load: dashboard },
  { id: "organizations", label: "조직", menuCode: "ORGANIZATION", group: "people", description: "조직 계층과 유효 조직", load: directory },
  { id: "employees", label: "직원", menuCode: "EMPLOYEE", group: "people", description: "직원·직급·직책·배치", load: directory },
  { id: "users", label: "사용자", menuCode: "USER", group: "access", description: "BZA 인증 사용자", load: access },
  { id: "roles", label: "역할", menuCode: "ROLE", group: "access", description: "다중 Role과 데이터 범위", load: access },
  { id: "menus", label: "메뉴", menuCode: "MENU", group: "access", description: "화면 메뉴 Registry", load: access },
  { id: "permissions", label: "권한", menuCode: "PERMISSION", group: "access", description: "메뉴·행위·API 권한", load: access },
  { id: "permissionTools", label: "권한 분석", menuCode: "PERMISSION", group: "access", description: "역할 비교와 권한 시뮬레이션", load: access },
  { id: "approvals", label: "결재", menuCode: "APPROVAL", group: "approval", description: "업무 결재와 승인 처리", load: approval },
  { id: "sessions", label: "내 세션", menuCode: "USER", group: "support", description: "Refresh session 관리", load: support },
  { id: "audits", label: "업무 감사", menuCode: "AUDIT", group: "support", description: "Immutable 업무 감사 조회", load: support },
  { id: "notifications", label: "알림", menuCode: "NOTIFICATION", group: "support", description: "업무 알림", load: support },
  { id: "attachments", label: "첨부파일", menuCode: "ATTACHMENT", group: "support", description: "첨부 업로드와 조회", load: support },
  { id: "savedSearches", label: "저장 검색", menuCode: "SAVED_SEARCH", group: "support", description: "사용자 검색 조건", load: support },
  { id: "settings", label: "업무 설정", menuCode: "SETTING", group: "support", description: "BZA 업무 설정", load: support },
  { id: "downloads", label: "다운로드 감사", menuCode: "DOWNLOAD", group: "support", description: "다운로드 정책과 감사", load: support }
];

export function routeFromHash(hash: string): BzaRouteId {
  const candidate = hash.replace(/^#\/?/, "").trim() as BzaRouteId;
  return bzaRoutes.some(route => route.id === candidate) ? candidate : "dashboard";
}
