<template>
      <section class="panel">
        <div class="panel-title">
          <h2>보안 운영</h2>
          <div class="actions">
            <button type="button" @click="loadSecurity">조회</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="saveIpAllowlist">IP 저장</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="registerMfa">MFA 등록</button>
            <button type="button" v-if="canWrite('SECURITY')" @click="verifyMfa">MFA 검증</button>
          </div>
        </div>
        <div class="filters">
          <label>IP/CIDR <input v-model="securityForm.ipPattern" type="text"></label>
          <label>설명 <input v-model="securityForm.description" type="text"></label>
          <label>운영자 ID <input v-model="securityForm.operatorId" type="text"></label>
          <label>Secret Ref <input v-model="securityForm.secretRef" type="text"></label>
          <label>OTP Code <input v-model="securityForm.otpCode" type="text"></label>
          <label>사유 <input v-model="securityForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(securityResult) }}</pre>
      </section>


  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "SecurityPage",
  mixins: [admConsoleMixin]
});
</script>
