<template>
      <section class="panel">
        <div class="panel-title">
          <h2>비밀번호/세션 관리</h2>
          <div class="actions">
            <button type="button" @click="loadPasswordPolicy">정책 조회</button>
            <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="resetOperatorPassword">비밀번호 초기화</button>
            <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="unlockOperator">잠금 해제</button>
            <button type="button" @click="loadSessions">세션 조회</button>
          </div>
        </div>
        <div class="filters">
          <label>운영자 ID <input v-model="passwordForm.operatorId" type="text"></label>
          <label>새 비밀번호 <input v-model="passwordForm.newPassword" type="password"></label>
          <label>강제 변경 <select v-model="passwordForm.forceChange"><option :value="true">Y</option><option :value="false">N</option></select></label>
          <label>세션 ID <input v-model="passwordForm.sessionId" type="text"></label>
          <label>사유 <input v-model="passwordForm.reason" type="text"></label>
          <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="revokeSession">세션 강제 종료</button>
          <button type="button" v-if="canWrite('PASSWORD') || canWrite('OPERATOR')" @click="cleanupExpiredSessions">만료 세션 정리</button>
        </div>
        <pre class="detail">{{ pretty(passwordResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "PasswordPage",
  mixins: [admConsoleMixin]
});
</script>
