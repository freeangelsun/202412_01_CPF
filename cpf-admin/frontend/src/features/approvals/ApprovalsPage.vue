<template>
  <div class="page-stack">
    <section class="panel">
      <div class="panel-title"><div><h2>Runtime 위험 변경</h2><p class="hint">위험 변경은 expectedVersion CAS, 사유, approvalId 또는 승인된 breakGlassId를 사용하며 요청 사용자는 Server Session에서 결정됩니다.</p></div><div class="actions"><button @click="loadApprovalPolicies">Capability·상태 조회</button></div></div>
      <div class="filters">
        <label>Operation ID <input v-model="approvalForm.operationId" placeholder="미입력 시 UUID"></label>
        <label>Change Type <input v-model="approvalForm.changeType"></label>
        <label>Schema Version <input v-model.number="approvalForm.payloadSchemaVersion" type="number" min="1"></label>
        <label>Expected Version <input v-model.number="approvalForm.expectedVersion" type="number" min="0"></label>
        <label>Rollout <select v-model="approvalForm.rolloutMode"><option>ALL_AT_ONCE</option><option>WAVE</option><option>CANARY</option></select></label>
        <label>Wave Size <input v-model.number="approvalForm.waveSize" type="number" min="1"></label>
        <label>Quorum % <input v-model.number="approvalForm.quorumPercent" type="number" min="1" max="100"></label>
        <label>Approval ID <input v-model="approvalForm.approvalId"></label>
        <label>Break-glass ID <input v-model="approvalForm.breakGlassId"></label>
        <label>Scheduled At <input v-model="approvalForm.scheduledAt" placeholder="ISO-8601"></label>
        <label>Expires At <input v-model="approvalForm.expiresAt" placeholder="ISO-8601"></label>
        <label>사유 <input v-model="approvalForm.reason"></label>
      </div>
      <div class="form-grid"><label class="wide">Target JSON <textarea v-model="approvalForm.targetJson" rows="6" spellcheck="false"></textarea></label><label class="wide">Payload JSON <textarea v-model="approvalForm.payloadJson" rows="6" spellcheck="false"></textarea></label></div>
      <div class="actions"><button @click="previewApprovalTargets">대상 Preview</button><button @click="previewApprovalChange">변경 Preview</button><button class="primary" @click="requestDangerousApproval">변경 생성</button></div>
    </section>

    <section class="panel">
      <div class="panel-title"><div><h2>변경 조회·취소·Rollback·Audit</h2><p class="hint">Unknown Result는 실패로 단정하지 않고 Operation ID 조회와 Audit Chain 검증으로 복구합니다.</p></div></div>
      <div class="filters">
        <label>Change ID <input v-model="approvalForm.selectedRequestId"></label>
        <label>Control Operation ID <input v-model="approvalForm.controlOperationId" placeholder="미입력 시 UUID"></label>
        <label>조치 <select v-model="approvalForm.decisionAction"><option>CANCEL</option><option>ROLLBACK</option></select></label>
      </div>
      <div class="actions"><button @click="loadApprovalRequest">Change 조회</button><button @click="decideApprovalRequest">조치 실행</button><button class="primary" @click="executeApprovedRequest">Audit Chain 검증</button></div>
      <pre class="detail">{{ pretty(approvalResult) }}</pre>
    </section>

    <section class="panel"><div class="panel-title"><h2>Capability·상태 Catalog</h2></div><pre class="detail">{{ pretty(approvalPolicyResult) }}</pre></section>
  </div>

  <section class="panel route-operation-panel"><h3>Break-glass·Operation 복구</h3><div class="filters"><label>Break-glass Session ID <input v-model="operationForm.breakGlassSessionId"></label><label>검토 상태 <select v-model="operationForm.reviewStatus"><option>APPROVED</option><option>REJECTED</option></select></label><label>Runtime Operation ID <input v-model="operationForm.operationId"></label><label>사유 <input v-model="operationForm.reason"></label></div><div class="actions"><button type="button" @click="loadBreakGlassSessions">Break-glass 조회</button><button type="button" @click="reviewBreakGlassSession">사후 검토</button><button type="button" @click="loadRuntimeOperation">Operation 결과 조회</button></div></section>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
export default defineComponent({setup(){return useAdmConsolePage()},name:"ApprovalsPage",mounted(){this.loadApprovalPolicies();}});
</script>
