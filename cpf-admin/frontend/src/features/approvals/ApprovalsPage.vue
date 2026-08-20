<template>
  <div class="page-stack">
    <section class="panel">
      <div class="panel-title"><div><h2>승인 정책·요청 엔진</h2><p class="hint">Versioned 정책, 참여자 Snapshot, ALL/ANY/N_OF_M 결정과 Owner Command 실행을 관리합니다.</p></div><div class="actions"><button :title="actionTitle('admApprovalPolicies')" :disabled="!canAction('admApprovalPolicies')" @click="listApprovalEnginePolicies">정책 목록</button><button :title="actionTitle('admApprovalPolicyDetail')" :disabled="!canAction('admApprovalPolicyDetail')" @click="loadApprovalEnginePolicy">정책 상세</button></div></div>
      <p v-if="approvalEngineError" class="error-banner">{{ approvalEngineError }}</p>
      <div class="filters">
        <label>Action Type <input v-model.trim="approvalEngine.actionType"></label>
        <label>Policy Code <input v-model.trim="approvalEngine.policyCode"></label>
        <label>Policy Version <input v-model.number="approvalEngine.policyVersion" type="number" min="1"></label>
        <label>Policy Name <input v-model.trim="approvalEngine.policyName"></label>
        <label>Effective From <input v-model.trim="approvalEngine.effectiveFrom" placeholder="ISO-8601"></label>
        <label>Effective To <input v-model.trim="approvalEngine.effectiveTo" placeholder="선택 ISO-8601"></label>
        <label>Enabled <select v-model="approvalEngine.enabledYn"><option>Y</option><option>N</option></select></label>
        <label>Self Approval <select v-model="approvalEngine.selfApprovalAllowedYn"><option>N</option><option>Y</option></select></label>
        <label>Break Glass <select v-model="approvalEngine.breakGlassAllowedYn"><option>N</option><option>Y</option></select></label>
      </div>
      <div class="form-grid"><label class="wide">정책 설명<textarea v-model.trim="approvalEngine.description" rows="2"></textarea></label><label class="wide">정책 단계 JSON<textarea v-model="approvalEngine.stepsJson" rows="6" spellcheck="false"></textarea></label></div>
      <div class="actions"><button class="primary" :title="actionTitle('admApprovalPolicySave')" :disabled="!canAction('admApprovalPolicySave')" @click="saveApprovalEnginePolicy">정책 Version 저장</button></div>
      <hr>
      <div class="filters">
        <label>Request ID <input v-model.trim="approvalEngine.requestId"></label>
        <label>Request Key <input v-model.trim="approvalEngine.requestKey"></label>
        <label>Owner Module <input v-model.trim="approvalEngine.ownerModule"></label>
        <label>Owner Command <input v-model.trim="approvalEngine.ownerCommand"></label>
        <label>Target Type <input v-model.trim="approvalEngine.targetType"></label>
        <label>Target ID <input v-model.trim="approvalEngine.targetId"></label>
        <label>Expire At <input v-model.trim="approvalEngine.expireAt" placeholder="선택 ISO-8601"></label>
        <label>Decision <select v-model="approvalEngine.decision"><option>APPROVE</option><option>REJECT</option></select></label>
        <label>Idempotency Key <input v-model.trim="approvalEngine.idempotencyKey"></label>
        <label>사유 <input v-model.trim="approvalEngine.reason"></label>
      </div>
      <div class="form-grid"><label class="wide">Owner Payload Snapshot JSON<textarea v-model="approvalEngine.payloadSnapshot" rows="5" spellcheck="false"></textarea></label></div>
      <div class="actions"><button :title="actionTitle('admApprovalRequest')" :disabled="!canAction('admApprovalRequest')" @click="createApprovalEngineRequest">승인 요청</button><button :title="actionTitle('admApprovalRequestDetail')" :disabled="!canAction('admApprovalRequestDetail')" @click="loadApprovalEngineRequest">요청 상세</button><button :title="actionTitle('admApprovalDecision')" :disabled="!canAction('admApprovalDecision')" @click="decideApprovalEngineRequest">승인/반려</button><button class="primary" :title="actionTitle('admApprovalExecute')" :disabled="!canAction('admApprovalExecute')" @click="executeApprovalEngineRequest">Owner Command 실행</button><button :title="actionTitle('admApprovalReconcile')" :disabled="!canAction('admApprovalReconcile')" @click="reconcileApprovalEngineRequest">UNKNOWN Reconcile</button></div>
      <CpfStructuredData class="detail" :value="approvalEngineResult" />
    </section>

    <section class="panel">
      <div class="panel-title"><div><h2>Runtime 위험 변경</h2><p class="hint">위험 변경은 Preview 후 Approval Engine에 불변 Payload Snapshot으로 요청하고, 독립 승인 뒤 Owner Command가 단회 실행합니다.</p></div><div class="actions"><button :title="!canAction('admRuntimeControlFindCapabilities') || !canAction('admRuntimeControlFindStateCatalog') ? '권한 없음: runtime-control capabilities/states' : ''" :disabled="!canAction('admRuntimeControlFindCapabilities') || !canAction('admRuntimeControlFindStateCatalog')" @click="loadApprovalPolicies">Capability·상태 조회</button></div></div>
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
      <div class="actions"><button :title="actionTitle('admRuntimeControlPreviewTargets')" :disabled="!canAction('admRuntimeControlPreviewTargets')" @click="previewApprovalTargets">대상 Preview</button><button :title="actionTitle('admRuntimeControlPreviewChange')" :disabled="!canAction('admRuntimeControlPreviewChange')" @click="previewApprovalChange">변경 Preview</button><button class="primary" :title="actionTitle('admRuntimeControlCreateChange')" :disabled="!canAction('admRuntimeControlCreateChange')" @click="requestDangerousApproval">변경 승인 요청</button></div>
    </section>

    <section class="panel">
      <div class="panel-title"><div><h2>변경 조회·취소·Rollback·Audit</h2><p class="hint">Unknown Result는 실패로 단정하지 않고 Operation ID 조회와 Audit Chain 검증으로 복구합니다.</p></div></div>
      <div class="filters">
        <label>Change ID <input v-model="approvalForm.selectedRequestId"></label>
        <label>Control Operation ID <input v-model="approvalForm.controlOperationId" placeholder="미입력 시 UUID"></label>
        <label>조치 <select v-model="approvalForm.decisionAction"><option>CANCEL</option><option>ROLLBACK</option></select></label>
      </div>
      <div class="actions"><button :title="actionTitle('admRuntimeControlFindChange')" :disabled="!canAction('admRuntimeControlFindChange')" @click="loadApprovalRequest">Change 조회</button><button :title="approvalForm.decisionAction === 'ROLLBACK' ? actionTitle('admRuntimeControlRollbackChange') : actionTitle('admRuntimeControlCancelChange')" :disabled="approvalForm.decisionAction === 'ROLLBACK' ? !canAction('admRuntimeControlRollbackChange') : !canAction('admRuntimeControlCancelChange')" @click="decideApprovalRequest">조치 승인 요청</button><button class="primary" :title="actionTitle('admRuntimeControlVerifyAudit')" :disabled="!canAction('admRuntimeControlVerifyAudit')" @click="executeApprovedRequest">Audit Chain 검증</button></div>
      <CpfStructuredData class="detail" :value="approvalResult" />
    </section>

    <section class="panel"><div class="panel-title"><h2>Capability·상태 Catalog</h2></div><CpfStructuredData class="detail" :value="approvalPolicyResult" /></section>
  </div>

  <section class="panel route-operation-panel"><h3>Break-glass·Operation 복구</h3><div class="filters"><label>Break-glass Session ID <input v-model="operationForm.breakGlassSessionId"></label><label>검토 상태 <select v-model="operationForm.reviewStatus"><option>APPROVED</option><option>REJECTED</option></select></label><label>Runtime Operation ID <input v-model="operationForm.operationId"></label><label>사유 <input v-model="operationForm.reason"></label></div><div class="actions"><button type="button" :title="actionTitle('admBreakGlassFindSessions')" :disabled="!canAction('admBreakGlassFindSessions')" @click="loadBreakGlassSessions">Break-glass 조회</button><button type="button" :title="actionTitle('admBreakGlassReviewSession')" :disabled="!canAction('admBreakGlassReviewSession')" @click="reviewBreakGlassSession">사후 검토</button><button type="button" :title="actionTitle('admRuntimeControlFindByOperation')" :disabled="!canAction('admRuntimeControlFindByOperation')" @click="loadRuntimeOperation">Operation 결과 조회</button></div></section>
  <CpfModal :open="Boolean(approvalEngineConfirmAction)" @cancel="cancelApprovalEngineConfirm" aria-labelledby="approval-engine-confirm-title">
    <form class="modal-card" @submit.prevent="confirmApprovalEngineAction">
      <div class="panel-title"><div><h2 id="approval-engine-confirm-title">{{ approvalEngineConfirmAction === 'EXECUTE' ? 'Owner Command 실행 확인' : 'UNKNOWN Reconcile 확인' }}</h2><p class="hint">{{ approvalEngineConfirmAction === 'EXECUTE' ? '승인된 Owner Command를 단회 실행합니다.' : 'Mutation을 재실행하지 않고 Owner 상태만 대조합니다.' }}</p></div></div>
      <p>Request ID: <strong>{{ approvalEngine.requestId }}</strong></p>
      <p>사유: {{ approvalEngine.reason }}</p>
      <label><input v-model="approvalEngineConfirmed" type="checkbox"> 대상·사유·영향 범위를 확인했습니다.</label>
      <div class="actions"><button type="button" @click="cancelApprovalEngineConfirm">취소</button><button class="primary" type="submit" :disabled="!approvalEngineConfirmed">확인 후 실행</button></div>
    </form>
  </CpfModal>
</template>
<script lang="ts">
import CpfModal from '../../components/ui/CpfModal.vue';
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import {
  admApprovalPolicies,
  admApprovalPolicyDetail,
  admApprovalPolicySave,
  admApprovalRequest,
  admApprovalRequestDetail,
  admApprovalDecision,
  admApprovalExecute,
  admApprovalReconcile
} from "../../generated/orval/cpf-api";
import { parseStrictJsonObject } from "../../shared/strictJsonObject";
import { useAdmSessionStore } from "../../stores/admSessionStore";

function unwrapGeneratedResponse(value: unknown): unknown {
  if (value && typeof value === "object" && "data" in value) return (value as { data: unknown }).data;
  return value;
}

export default defineComponent({components:{CpfModal},
  setup(){return { ...useAdmConsolePage(), admSession: useAdmSessionStore() }},
  name:"ApprovalsPage",
  data(){return{
    approvalEngineError:"",
    approvalEngineResult:{} as Record<string,unknown>|unknown[],
    approvalEngineConfirmAction:"" as ""|"EXECUTE"|"RECONCILE",
    approvalEngineConfirmed:false,
    approvalEngine:{
      actionType:"GATEWAY_BINDING_CHANGE",policyCode:"GATEWAY_BINDING_CHANGE",policyVersion:1,
      policyName:"Gateway Binding 변경 승인",effectiveFrom:new Date().toISOString(),effectiveTo:"",
      enabledYn:"Y",selfApprovalAllowedYn:"N",breakGlassAllowedYn:"N",description:"Gateway Binding 위험 변경 승인 정책",
      stepsJson:'[{"stepNo":1,"stepType":"APPROVAL","targetType":"ROLE","targetCode":"CPF_ADMIN_APPROVER","decisionRule":"ALL","requiredCount":null,"requiredYn":"Y"}]',
      requestId:"",requestKey:crypto.randomUUID(),ownerModule:"cpf-gateway",ownerCommand:"GATEWAY_BINDING_CHANGE",
      targetType:"GATEWAY_BINDING",targetId:"",payloadSnapshot:"{}",expireAt:"",decision:"APPROVE",
      idempotencyKey:crypto.randomUUID(),reason:"위험조치 승인 운영",requestPending:false,decisionPending:false
    }
  }},
  mounted(){this.loadApprovalPolicies();void this.listApprovalEnginePolicies();},
  methods:{
    canAction(operationId:string){return this.admSession.hasOperation(operationId);},
    actionTitle(operationId:string){return this.canAction(operationId)?"":`권한 없음: ${operationId}`;},
    async runApprovalEngine(task:()=>Promise<unknown>){this.approvalEngineError="";try{this.approvalEngineResult=await task();}catch(error){this.approvalEngineError=error instanceof Error?error.message:String(error);}},
    async listApprovalEnginePolicies(){await this.runApprovalEngine(async()=>unwrapGeneratedResponse(await admApprovalPolicies({actionType:this.approvalEngine.actionType||undefined})));},
    async loadApprovalEnginePolicy(){if(!this.approvalEngine.policyCode||this.approvalEngine.policyVersion<1){this.approvalEngineError="Policy Code와 Version이 필요합니다.";return;}await this.runApprovalEngine(async()=>unwrapGeneratedResponse(await admApprovalPolicyDetail(this.approvalEngine.policyCode,this.approvalEngine.policyVersion)));},
    async saveApprovalEnginePolicy(){await this.runApprovalEngine(async()=>{const stepsValue=JSON.parse(this.approvalEngine.stepsJson);const steps=stepsValue;if(!Array.isArray(steps)||!steps.length)throw new Error("정책 단계 JSON 배열이 필요합니다.");return unwrapGeneratedResponse(await admApprovalPolicySave({policyCode:this.approvalEngine.policyCode,policyVersion:this.approvalEngine.policyVersion,policyName:this.approvalEngine.policyName,actionType:this.approvalEngine.actionType,effectiveFrom:this.approvalEngine.effectiveFrom,effectiveTo:this.approvalEngine.effectiveTo||null,enabledYn:this.approvalEngine.enabledYn,selfApprovalAllowedYn:this.approvalEngine.selfApprovalAllowedYn,breakGlassAllowedYn:this.approvalEngine.breakGlassAllowedYn,description:this.approvalEngine.description,steps,reason:this.approvalEngine.reason}));});},
    async createApprovalEngineRequest(){await this.runApprovalEngine(async()=>{parseStrictJsonObject(this.approvalEngine.payloadSnapshot,"승인 Payload");this.approvalEngine.requestPending=true;const response=await admApprovalRequest({requestKey:this.approvalEngine.requestKey,policyCode:this.approvalEngine.policyCode||null,policyVersion:this.approvalEngine.policyCode?this.approvalEngine.policyVersion:null,actionType:this.approvalEngine.actionType,ownerModule:this.approvalEngine.ownerModule,ownerCommand:this.approvalEngine.ownerCommand,targetType:this.approvalEngine.targetType,targetId:this.approvalEngine.targetId,payloadSnapshot:this.approvalEngine.payloadSnapshot,expireAt:this.approvalEngine.expireAt||null,reason:this.approvalEngine.reason});const result=unwrapGeneratedResponse(response) as Record<string,unknown>;this.approvalEngine.requestId=String(result.approvalRequestId??result.requestId??result.id??this.approvalEngine.requestId);this.approvalEngine.requestPending=false;this.approvalEngine.requestKey=crypto.randomUUID();return result;});},
    async loadApprovalEngineRequest(){if(!this.approvalEngine.requestId){this.approvalEngineError="Request ID가 필요합니다.";return;}await this.runApprovalEngine(async()=>unwrapGeneratedResponse(await admApprovalRequestDetail(Number(this.approvalEngine.requestId))));},
    async decideApprovalEngineRequest(){if(!this.approvalEngine.requestId){this.approvalEngineError="Request ID가 필요합니다.";return;}await this.runApprovalEngine(async()=>{this.approvalEngine.decisionPending=true;const response=await admApprovalDecision(Number(this.approvalEngine.requestId),{action:this.approvalEngine.decision as "APPROVE"|"REJECT",idempotencyKey:this.approvalEngine.idempotencyKey,reason:this.approvalEngine.reason});const result=unwrapGeneratedResponse(response);this.approvalEngine.decisionPending=false;this.approvalEngine.idempotencyKey=crypto.randomUUID();return result;});},
    executeApprovalEngineRequest(){if(!this.approvalEngine.requestId){this.approvalEngineError="Request ID가 필요합니다.";return;}if(this.approvalEngine.reason.trim().length<5){this.approvalEngineError="실행 사유는 5자 이상이어야 합니다.";return;}this.approvalEngineConfirmAction="EXECUTE";this.approvalEngineConfirmed=false;},
    reconcileApprovalEngineRequest(){if(!this.approvalEngine.requestId){this.approvalEngineError="Request ID가 필요합니다.";return;}if(this.approvalEngine.reason.trim().length<5){this.approvalEngineError="Reconcile 사유는 5자 이상이어야 합니다.";return;}this.approvalEngineConfirmAction="RECONCILE";this.approvalEngineConfirmed=false;},
    cancelApprovalEngineConfirm(){this.approvalEngineConfirmAction="";this.approvalEngineConfirmed=false;},
    async confirmApprovalEngineAction(){if(!this.approvalEngineConfirmed||!this.approvalEngineConfirmAction)return;const action=this.approvalEngineConfirmAction;this.cancelApprovalEngineConfirm();if(action==="EXECUTE"){await this.runApprovalEngine(async()=>unwrapGeneratedResponse(await admApprovalExecute(Number(this.approvalEngine.requestId),{reason:this.approvalEngine.reason})));return;}await this.runApprovalEngine(async()=>unwrapGeneratedResponse(await admApprovalReconcile(Number(this.approvalEngine.requestId),{reason:this.approvalEngine.reason})));}
  }
});
</script>
