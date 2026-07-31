<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { bzaRoutes, routeFromName, type BzaRoute } from "./app/routes";
import { useRoute, useRouter } from "vue-router";
import CpfIcon from "./components/CpfIcon.vue";
import { authenticated, bzaSession, changeBzaPassword, hasBzaMenu, loginBza, logoutBza, restoreBzaSession } from "./features/auth/session";

const loginForm=reactive({loginId:"",password:""});
const passwordForm=reactive({currentPassword:"",newPassword:"",newPasswordConfirm:""});
const activeRoute=ref<BzaRoute>(bzaRoutes[0]);
const loginMessage=ref(""); const passwordMessage=ref(""); const passwordOpen=ref(false); const sidebarOpen=ref(false);
const router=useRouter(); const route=useRoute();

const visibleRoutes=computed(()=>bzaRoutes.filter(route=>hasBzaMenu(route.menuCode)));
const routeGroups=computed(()=>[
  {id:'overview',label:'개요',items:visibleRoutes.value.filter(r=>r.group==='overview')},
  {id:'people',label:'조직·사람',items:visibleRoutes.value.filter(r=>r.group==='people')},
  {id:'access',label:'접근제어',items:visibleRoutes.value.filter(r=>r.group==='access')},
  {id:'approval',label:'업무결재',items:visibleRoutes.value.filter(r=>r.group==='approval')},
  {id:'support',label:'업무지원',items:visibleRoutes.value.filter(r=>r.group==='support')}
].filter(group=>group.items.length));

function iconForRoute(route:BzaRoute):string{
  if(route.id==='dashboard')return 'dashboard'; if(route.group==='people')return route.id==='organizations'?'org':'users';
  if(route.group==='approval')return 'approval'; if(route.group==='access')return 'role'; if(route.id==='settings')return 'settings'; return 'service';
}
async function syncRoute():Promise<void>{
  const requested=routeFromName(route.name);
  const allowed=visibleRoutes.value;
  const selected=allowed.find(item=>item.id===requested) || allowed[0] || bzaRoutes[0];
  activeRoute.value=selected;
  if(route.name!==selected.id) await router.replace({name:selected.id});
}
async function submitLogin(){loginMessage.value='';try{await loginBza(loginForm.loginId,loginForm.password);loginForm.password='';await syncRoute();if(bzaSession.operator?.passwordChangeRequiredYn==='Y')passwordOpen.value=true;}catch(e){loginMessage.value=e instanceof Error?e.message:String(e);}}
async function submitPassword(){passwordMessage.value='';if(passwordForm.newPassword!==passwordForm.newPasswordConfirm){passwordMessage.value='새 비밀번호 확인이 일치하지 않습니다.';return;}try{await changeBzaPassword(passwordForm.currentPassword,passwordForm.newPassword,passwordForm.newPasswordConfirm);passwordOpen.value=false;loginMessage.value='비밀번호가 변경되었습니다. 다시 로그인하세요.';passwordForm.currentPassword='';passwordForm.newPassword='';passwordForm.newPasswordConfirm='';}catch(e){passwordMessage.value=e instanceof Error?e.message:String(e);}}
async function go(routeId:string){sidebarOpen.value=false;await router.push({name:routeId});await syncRoute();}
async function logout(){await logoutBza();passwordOpen.value=false;passwordForm.currentPassword='';passwordForm.newPassword='';passwordForm.newPasswordConfirm='';await router.replace({name:'dashboard'});}
watch(() => route.name, async () => { if (authenticated.value) await syncRoute(); });
onMounted(async()=>{await restoreBzaSession();if(authenticated.value)await syncRoute();});
</script>

<template>
  <main id="loginView" class="login-shell" :hidden="authenticated">
    <section class="login-card" aria-labelledby="loginTitle">
      <div class="brand-lockup"><span class="brand-mark">CPF</span><div><strong>Core Platform Framework</strong><small>Business Administration</small></div></div>
      <div class="login-copy"><p class="eyebrow">SECURE BACKOFFICE</p><h1 id="loginTitle">BZA Backoffice</h1><p>업무 운영 계정으로 로그인하세요. 권한과 모든 변경 행위는 감사 추적됩니다.</p></div>
      <form id="loginForm" class="login-form" @submit.prevent="submitLogin">
        <label><span>로그인 ID</span><input v-model="loginForm.loginId" autocomplete="username" required></label>
        <label><span>비밀번호</span><input v-model="loginForm.password" type="password" autocomplete="current-password" required></label>
        <button type="submit" class="primary login-button" :disabled="bzaSession.busy">{{bzaSession.busy?'로그인 중...':'로그인'}}</button>
        <output class="message" aria-live="polite">{{loginMessage}}</output>
      </form>
    </section>
    <aside class="login-visual" aria-hidden="true"><div class="visual-grid"></div><div class="visual-copy"><span>CPF · BZA</span><strong>운영은 빠르게,<br>통제는 명확하게.</strong><p>조직 · 권한 · 결재 · 감사 · 업무지원</p></div></aside>
  </main>

  <div id="appView" class="app-shell" :class="{'sidebar-open':sidebarOpen}" :hidden="!authenticated">
    <aside class="sidebar"><button class="sidebar-close" type="button" aria-label="메뉴 닫기" @click="sidebarOpen=false"><CpfIcon name="close" /></button>
      <div class="brand-lockup sidebar-brand"><span class="brand-mark">CPF</span><div><strong>BZA</strong><small>Business Admin</small></div></div>
      <nav aria-label="업무 백오피스 메뉴">
        <section v-for="group in routeGroups" :key="group.id" class="nav-group"><p>{{group.label}}</p><button v-for="route in group.items" :key="route.id" type="button" :class="{active:activeRoute.id===route.id}" @click="go(route.id)"><CpfIcon class="nav-icon" :name="iconForRoute(route)" /><span>{{route.label}}</span></button></section>
      </nav>
      <div class="sidebar-footer"><div class="operator"><span class="avatar">{{(bzaSession.operator?.operatorName||bzaSession.operator?.loginId||'U').toString().slice(0,1)}}</span><div><strong>{{bzaSession.operator?.operatorName||bzaSession.operator?.loginId}}</strong><small>업무 운영자</small></div></div><button class="text-button" @click="logout">로그아웃</button></div>
    </aside>
    <div class="workspace">
      <header class="workspace-header"><button class="ghost mobile-menu" type="button" aria-label="메뉴 열기" @click="sidebarOpen=true"><CpfIcon name="menu" /></button><div><p class="eyebrow">BZA / {{activeRoute.group.toUpperCase()}}</p><h1>{{activeRoute.label}}</h1><p>{{activeRoute.description}}</p></div><div class="toolbar"><button class="ghost" @click="passwordOpen=true">비밀번호 변경</button><button class="primary" @click="syncRoute"><CpfIcon name="refresh" /> 새로고침</button></div></header>
      <main class="content"><Suspense><router-view /><template #fallback><div class="route-loading">화면을 준비하고 있습니다...</div></template></Suspense></main>
    </div>
  </div>

  <dialog :open="passwordOpen" class="modal"><form class="modal-card" @submit.prevent="submitPassword"><div class="card-head"><div><p class="eyebrow">SECURITY</p><h2>비밀번호 변경</h2></div><button type="button" class="icon-button" @click="passwordOpen=false">×</button></div><div class="form-grid"><label class="wide"><span>현재 비밀번호</span><input v-model="passwordForm.currentPassword" type="password" required></label><label><span>새 비밀번호</span><input v-model="passwordForm.newPassword" type="password" minlength="12" required></label><label><span>새 비밀번호 확인</span><input v-model="passwordForm.newPasswordConfirm" type="password" minlength="12" required></label></div><p v-if="passwordMessage" class="error-banner">{{passwordMessage}}</p><div class="dialog-actions"><button type="button" class="ghost" @click="passwordOpen=false">취소</button><button class="primary">변경</button></div></form></dialog>
</template>
