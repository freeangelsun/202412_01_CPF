<template>
  <main v-if="!authenticated" class="adm-login-shell">
    <section class="adm-login-card">
      <div class="adm-brand"><span>CPF</span><div><strong>Core Platform Framework</strong><small>Platform Administration</small></div></div>
      <div class="adm-login-copy"><p class="eyebrow">PLATFORM CONTROL PLANE</p><h1>CPF ADM</h1><h2 class="login-subtitle">운영자 로그인</h2><p>거래·서비스·배치·복구·보안 운영을 하나의 통제면에서 관리합니다.</p></div>
      <form class="adm-login-form" @submit.prevent="login">
        <label>운영자 ID <input v-model="loginForm.operatorId" type="text" autocomplete="username"></label>
        <label>비밀번호 <input v-model="loginForm.password" type="password" autocomplete="current-password"></label>
        <label>MFA 코드 <input v-model="loginForm.otpCode" type="text" inputmode="numeric" autocomplete="one-time-code" maxlength="6" placeholder="MFA 사용 계정만 입력"></label>
        <button class="primary" type="submit">로그인</button>
        <p class="hint">최초 운영자는 승인된 bootstrap 환경변수로 생성하며 초기 비밀번호는 저장소와 화면에 제공하지 않습니다.</p>
        <p v-if="authMessage" class="detail" role="alert">{{ authMessage }}</p>
      </form>
    </section>
    <aside class="adm-login-visual" aria-hidden="true"><div class="grid"></div><div><span>CPF · ADM</span><strong>Observe.<br>Control.<br>Recover.</strong><p>감사 가능한 운영, 안전한 제어, 빠른 복구</p></div></aside>
  </main>

  <main v-else-if="passwordChangeRequired" class="adm-login-shell single">
    <section class="adm-login-card">
      <div class="adm-brand"><span>CPF</span><div><strong>ADM Security</strong><small>Password Rotation</small></div></div>
      <div class="adm-login-copy compact"><p class="eyebrow">SECURITY REQUIRED</p><h1>비밀번호 변경</h1><p>초기 또는 만료 비밀번호입니다. 다른 ADM 기능을 사용하기 전에 변경해야 합니다.</p></div>
      <form class="adm-login-form" @submit.prevent="changeOwnPassword">
        <label>현재 비밀번호 <input v-model="forcedPasswordForm.currentPassword" type="password" autocomplete="current-password"></label>
        <label>새 비밀번호 <input v-model="forcedPasswordForm.newPassword" type="password" autocomplete="new-password"></label>
        <label>새 비밀번호 확인 <input v-model="forcedPasswordForm.newPasswordConfirm" type="password" autocomplete="new-password"></label>
        <label>변경 사유 <input v-model="forcedPasswordForm.reason" type="text"></label>
        <div class="inline-actions"><button class="primary" type="submit">변경 후 다시 로그인</button><button class="ghost" type="button" @click="logout">로그아웃</button></div>
        <p v-if="authMessage" class="detail" role="alert">{{ authMessage }}</p>
      </form>
    </section>
  </main>

  <div v-else class="adm-shell" :class="{ 'sidebar-open': sidebarOpen }">
    <aside class="adm-sidebar"><button class="adm-sidebar-close" type="button" aria-label="메뉴 닫기" @click="sidebarOpen=false"><CpfIcon name="close" /></button>
      <div class="adm-brand sidebar-brand"><span>CPF</span><div><strong>ADM</strong><small>Platform Admin</small></div></div>
      <div class="adm-menu-search">
        <label class="sr-only" for="adm-global-menu-search">운영 메뉴 검색</label>
        <input id="adm-global-menu-search" ref="globalMenuSearch" v-model.trim="menuSearch" type="search" placeholder="메뉴 검색 (Ctrl+K)" autocomplete="off" @keydown.esc="menuSearch=''">
      </div>
      <section v-if="recentMenuItems.length" class="adm-nav-group adm-recent-menu" aria-label="최근 사용 메뉴">
        <p>최근 사용</p>
        <button v-for="menu in recentMenuItems" :key="`recent-${menu.id}`" type="button" @click="selectMenu(menu.id)"><CpfIcon class="adm-nav-icon" :name="iconForMenu(menu.id)" />{{ menu.label }}</button>
      </section>
      <nav aria-label="ADM 운영 메뉴">
        <section v-for="group in filteredGroupedMenus" :key="group.id" class="adm-nav-group">
          <p>{{ group.label }}</p>
          <div v-for="menu in group.items" :key="menu.id" class="adm-nav-row">
            <button type="button" :class="{ active: activeMenu === menu.id }" @click="selectMenu(menu.id)"><CpfIcon class="adm-nav-icon" :name="iconForMenu(menu.id)" />{{ menu.label }}</button>
            <button class="adm-favorite-button" type="button" :aria-label="isFavorite(menu.id) ? `${menu.label} 즐겨찾기 해제` : `${menu.label} 즐겨찾기 추가`" :aria-pressed="isFavorite(menu.id)" @click.stop="toggleFavorite(menu.id)">{{ isFavorite(menu.id) ? '★' : '☆' }}</button>
          </div>
        </section>
        <p v-if="menuSearch && !filteredGroupedMenus.length" class="hint">검색 결과가 없습니다.</p>
      </nav>
      <footer><div><strong>{{ currentOperator.operatorId }}</strong><small>Platform Operator</small></div><button class="text-button" @click="logout">로그아웃</button></footer>
    </aside>

    <section class="adm-workspace">
      <header class="adm-workspace-header"><button class="ghost adm-mobile-toggle" type="button" aria-label="메뉴 열기" @click="sidebarOpen=true"><CpfIcon name="menu" /></button><div><p class="eyebrow">ADM / {{ activeFeatureGroup.toUpperCase() }}</p><h1>{{ currentMenuLabel }}</h1><p>Core Platform Framework 운영 콘솔</p></div><div class="inline-actions"><span class="cpf-status success">ONLINE</span><button class="ghost" @click="loadInitialData"><CpfIcon name="refresh" /> 전체 새로고침</button></div></header>
      <div class="adm-content">
        <p class="status" v-if="uiMessage">{{ uiMessage }}</p>
        <section class="summary-grid">
          <div class="metric"><span>등록 서비스</span><strong>{{ serviceRegistryResult.services?.length || 0 }}</strong></div>
          <div class="metric"><span>활성 인스턴스</span><strong>{{ (serviceRegistryResult.instances || []).filter((i:any) => i.status === 'UP').length }}</strong></div>
          <div class="metric"><span>복구 대기</span><strong>{{ (reliabilityResult.unknownResults || []).length + (reliabilityResult.dlq || []).length }}</strong></div>
          <div class="metric"><span>운영자</span><strong>{{ currentOperator.operatorId }}</strong></div>
        </section>
        <RouterView v-slot="{ Component }">
          <AdmCommercialPageBoundary @retry="loadInitialData">
            <Suspense>
              <component :is="Component" />
              <template #fallback><div class="route-loading" role="status" aria-live="polite">운영 화면을 준비하고 있습니다...</div></template>
            </Suspense>
          </AdmCommercialPageBoundary>
        </RouterView>
        <RouteOperationWorkbench v-if="currentOperationIds.length" :title="currentMenuLabel" :operation-ids="currentOperationIds" />
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { RouterView } from "vue-router";
import CpfIcon from "./components/CpfIcon.vue";
import AdmCommercialPageBoundary from "./components/page-contract/AdmCommercialPageBoundary.vue";
import RouteOperationWorkbench from "./components/RouteOperationWorkbench.vue";
import { useAdmConsolePage } from "./app/useAdmConsolePage";
import { admGroupLabels, featureGroupForMenu, findCapabilityByRouteName, iconForMenu, type AdmFeatureGroup } from "./app/routes";
import { admRouter } from "./app/router";

export default defineComponent({
  name: "AdmApp",
  components: { CpfIcon, RouterView, AdmCommercialPageBoundary, RouteOperationWorkbench },
  setup() {
    return { ...useAdmConsolePage(), admRouter };
  },
  data() { return { sidebarOpen: false, menuSearch: "", favoriteMenus: [] as string[], recentMenus: [] as string[], unregisterRouteHook: null as null | (() => void) }; },
  computed: {
    activeFeatureGroup(): AdmFeatureGroup {
      return findCapabilityByRouteName(this.admRouter.currentRoute.value.name)?.group
        ?? featureGroupForMenu(this.activeMenu)
        ?? "overview";
    },
    currentMenuLabel(): string {
      return findCapabilityByRouteName(this.admRouter.currentRoute.value.name)?.label
        ?? this.visibleMenus.find((menu: any) => menu.id === this.activeMenu)?.label
        ?? "운영 상태";
    },
    currentOperationIds(): readonly any[] {
      return findCapabilityByRouteName(this.admRouter.currentRoute.value.name)?.expectedOperationIds ?? [];
    },
    groupedMenus(): Array<{ id: AdmFeatureGroup; label: string; items: any[] }> {
      return (Object.keys(admGroupLabels) as AdmFeatureGroup[]).map(id => ({ id, label: admGroupLabels[id], items: this.visibleMenus.filter((menu: any) => featureGroupForMenu(menu.id) === id) })).filter(group => group.items.length > 0);
    },
    filteredGroupedMenus(): Array<{ id: AdmFeatureGroup; label: string; items: any[] }> {
      const query = this.menuSearch.toLocaleLowerCase("ko-KR");
      return this.groupedMenus.map(group => ({ ...group, items: group.items.filter((menu: any) => !query || `${menu.label} ${menu.id} ${group.label}`.toLocaleLowerCase("ko-KR").includes(query)) }))
        .filter(group => group.items.length > 0)
        .sort((a, b) => Number(b.items.some((menu: any) => this.isFavorite(menu.id))) - Number(a.items.some((menu: any) => this.isFavorite(menu.id))));
    },
    recentMenuItems(): any[] { return this.recentMenus.map(id => this.visibleMenus.find((menu: any) => menu.id === id)).filter(Boolean).slice(0, 5); }
  },
  async mounted() {
    window.addEventListener("keydown", this.handleGlobalShortcut);
    this.favoriteMenus = this.readStoredMenuIds("cpf.adm.favoriteMenus");
    this.recentMenus = this.readStoredMenuIds("cpf.adm.recentMenus");
    await this.restoreServerSession();
    this.syncMenuFromRoute();
    this.unregisterRouteHook = this.admRouter.afterEach(() => this.syncMenuFromRoute());
    if (this.authenticated) await this.loadInitialData();
  },
  beforeUnmount() { window.removeEventListener("keydown", this.handleGlobalShortcut); this.unregisterRouteHook?.(); },
  methods: {
    iconForMenu,
    async selectMenu(menuId: string) {
      if (!this.visibleMenus.some((menu: any) => menu.id === menuId)) return;
      this.activeMenu = menuId;
      this.recentMenus = [menuId, ...this.recentMenus.filter(id => id !== menuId)].slice(0, 8);
      localStorage.setItem("cpf.adm.recentMenus", JSON.stringify(this.recentMenus));
      this.sidebarOpen = false; this.menuSearch = "";
      await this.admRouter.push({ name: menuId });
    },
    isFavorite(menuId: string): boolean { return this.favoriteMenus.includes(menuId); },
    toggleFavorite(menuId: string) {
      this.favoriteMenus = this.isFavorite(menuId) ? this.favoriteMenus.filter(id => id !== menuId) : [...this.favoriteMenus, menuId];
      localStorage.setItem("cpf.adm.favoriteMenus", JSON.stringify(this.favoriteMenus));
    },
    readStoredMenuIds(key: string): string[] {
      try { const parsed = JSON.parse(localStorage.getItem(key) || "[]"); return Array.isArray(parsed) ? parsed.filter((id: unknown) => typeof id === "string") : []; }
      catch { return []; }
    },
    handleGlobalShortcut(event: KeyboardEvent) {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") { event.preventDefault(); this.sidebarOpen = true; this.$nextTick(() => (this.$refs.globalMenuSearch as HTMLInputElement | undefined)?.focus()); }
    },
    syncMenuFromRoute() {
      const capability = findCapabilityByRouteName(this.admRouter.currentRoute.value.name);
      // Status/unknown routes remain visible as status pages. Never replace them with Dashboard.
      if (!capability) return;
      if (this.visibleMenus.some((menu: any) => menu.id === capability.routeId)) {
        this.activeMenu = capability.routeId;
      }
    }
  }
});
</script>
