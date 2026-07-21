<template>
<main id="loginView" class="login-shell">
    <section class="login-panel" aria-labelledby="loginTitle">
      <div class="brand-mark">CPF</div>
      <h1 id="loginTitle">BZA Backoffice</h1>
      <p>업무 운영 계정으로 로그인하세요.</p>
      <form id="loginForm">
        <label>로그인 ID<input id="loginId" autocomplete="username" required></label>
        <label>비밀번호<input id="password" type="password" autocomplete="current-password" required></label>
        <button type="submit" class="primary">로그인</button>
        <output id="loginMessage" class="message" aria-live="polite"></output>
      </form>
    </section>
  </main>

  <div id="appView" class="app-shell" hidden>
    <aside class="sidebar">
      <div class="brand"><strong>CPF</strong><span>BZA Backoffice</span></div>
      <nav id="navigation" aria-label="업무 백오피스 메뉴"></nav>
      <div class="sidebar-footer">
        <span id="operatorName"></span>
        <button id="logoutButton" type="button">로그아웃</button>
      </div>
    </aside>
    <div class="workspace">
      <header class="topbar">
        <div><h1 id="pageTitle">대시보드</h1><p id="pageDescription">업무 운영 현황</p></div>
        <div class="toolbar">
          <button id="passwordButton" type="button">비밀번호 변경</button>
          <button id="reloadButton" type="button">새로고침</button>
        </div>
      </header>
      <section id="content" class="content" aria-live="polite"></section>
    </div>
  </div>

  <dialog id="passwordDialog">
    <form id="passwordForm" method="dialog">
      <h2>비밀번호 변경</h2>
      <label>현재 비밀번호<input id="currentPassword" type="password" required></label>
      <label>새 비밀번호<input id="newPassword" type="password" minlength="12" required></label>
      <label>새 비밀번호 확인<input id="newPasswordConfirm" type="password" minlength="12" required></label>
      <output id="passwordMessage" class="message"></output>
      <div class="dialog-actions"><button value="cancel">취소</button><button id="changePasswordButton" type="button" class="primary">변경</button></div>
    </form>
  </dialog>

  <dialog id="entityDialog" class="entity-dialog">
    <form id="entityForm">
      <h2 id="entityDialogTitle">업무 기준정보 등록·수정</h2>
      <div id="entityFields" class="form-grid"></div>
      <output id="entityMessage" class="message" aria-live="polite"></output>
      <div class="dialog-actions">
        <button id="entityCancelButton" type="button">취소</button>
        <button type="submit" class="primary">저장</button>
      </div>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { initializeBzaConsole } from "./features/console";

onMounted(() => initializeBzaConsole());
</script>
