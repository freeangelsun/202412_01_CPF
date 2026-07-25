<template>
  <div class="page-stack">
    <section class="panel">
      <div class="panel-title"><div><h2>위험조치 승인</h2><p class="hint">승인 대상 Payload 원문이 아니라 마스킹 Snapshot과 SHA-256을 고정하고 실제 변경은 Owner Command Port로 실행합니다.</p></div><div class="actions"><button @click="loadApprovalPolicies">정책 조회</button><button @click="loadApprovalRequest">요청 조회</button></div></div>
      <div class="filters">
        <label>Action Type <input v-model="approvalForm.actionType"></label>
        <label>정책 코드 <input v-model="approvalForm.policyCode"></label>
        <label>정책 버전 <input v-model="approvalForm.policyVersion" type="number"></label>
        <label>Owner Module <input v-model="approvalForm.ownerModule"></label>
        <label>Owner Command <input v-model="approvalForm.ownerCommand"></label>
        <label>Target Type <input v-model="approvalForm.targetType"></label>
        <label>Target ID <input v-model="approvalForm.targetId"></label>
        <label>Request Key <input v-model="approvalForm.requestKey" placeholder="미입력 시 UUID"></label>
        <label>Expire At <input v-model="approvalForm.expireAt" placeholder="ISO-8601"></label>
        <label>사유 <input v-model="approvalForm.reason"></label>
      </div>
      <label class="wide">마스킹 Payload Snapshot <textarea v-model="approvalForm.payloadSnapshot" rows="5"></textarea></label>
      <div class="actions"><button class="primary" @click="requestDangerousApproval">승인 요청</button></div>
    </section>

    <section class="panel">
      <div class="panel-title"><h2>승인·실행</h2></div>
      <div class="filters">
        <label>Request ID <input v-model="approvalForm.selectedRequestId"></label>
        <label>결정 <select v-model="approvalForm.decisionAction"><option>APPROVE</option><option>REJECT</option></select></label>
        <label>멱등 Key <input v-model="approvalForm.idempotencyKey" placeholder="미입력 시 UUID"></label>
      </div>
      <div class="actions"><button @click="decideApprovalRequest">결정 반영</button><button class="primary" @click="executeApprovedRequest">승인 Command 실행</button></div>
      <p class="hint">UNKNOWN은 실패가 아니며 Execution 상세의 recoveryRequiredYn을 기준으로 대사/복구해야 합니다.</p>
      <pre class="detail">{{ pretty(approvalResult) }}</pre>
    </section>

    <section class="panel">
      <div class="panel-title"><h2>Versioned 정책 목록</h2></div>
      <pre class="detail">{{ pretty(approvalPolicyResult) }}</pre>
    </section>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";
export default defineComponent({ name:"ApprovalsPage", mixins:[admConsoleMixin], mounted(){ this.loadApprovalPolicies(); } });
</script>
