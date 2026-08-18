<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div>
        <p class="eyebrow">RUNTIME CHANGE CENTER</p>
        <h2>Runtime Control Plane</h2>
        <p>변경 대상 Preview, CAS·멱등 생성, rollout ACK, drift, 취소·rollback과 audit hash-chain을 한 화면에서 통제합니다.</p>
      </div>
      <button class="ghost" @click="loadOverview">새로고침</button>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <section class="cpf-kpi-grid">
      <div class="cpf-stat-card"><span class="label">Readiness</span><strong class="value">{{ health.ready ? 'READY' : 'NOT READY' }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Pending</span><strong class="value">{{ health.pendingDeliveryCount ?? 0 }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Poison</span><strong class="value">{{ health.poisonDeliveryCount ?? 0 }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Drift</span><strong class="value">{{ health.driftCount ?? status.driftCount ?? 0 }}</strong></div>
    </section>

    <div class="cpf-grid-2">
      <section class="cpf-card">
        <div class="cpf-card-head"><h2>변경 명령</h2><span class="cpf-status">{{ form.rolloutMode }}</span></div>
        <div class="cpf-form-grid">
          <label>Command ID<input v-model="form.commandId"></label>
          <label>Change Type<input v-model.trim="form.changeType" placeholder="GATEWAY_ROUTE"></label>
          <label>Environment<input v-model.trim="form.environment" placeholder="PROD"></label>
          <label>Service ID<input v-model.trim="form.serviceId" placeholder="MBR"></label>
          <label>Group ID<input v-model.trim="form.groupId"></label>
          <label>Instance IDs<input v-model="form.instanceIds" placeholder="MBR-01,MBR-02"></label>
          <label>Expected Version<input v-model.number="form.expectedVersion" type="number" min="0"></label>
          <label>Schema Version<input v-model.number="form.payloadSchemaVersion" type="number" min="1"></label>
          <label>Rollout<select v-model="form.rolloutMode"><option>ALL_AT_ONCE</option><option>WAVE</option><option>CANARY</option></select></label>
          <label>Wave Size<input v-model.number="form.waveSize" type="number" min="1"></label>
          <label>Quorum %<input v-model.number="form.quorumPercent" type="number" min="1" max="100"></label>
          <label>Allow All<select v-model="form.allowAll"><option :value="false">N</option><option :value="true">Y</option></select></label>
          <label>Approval ID<input v-model.trim="form.approvalId"></label>
          <label>Break Glass ID<input v-model.trim="form.breakGlassId"></label>
          <label>설정 Key<input v-model.trim="form.payloadKey" placeholder="route.header.policy"></label>
          <label>설정 Value<input v-model.trim="form.payloadValue" placeholder="MASKED_BODY"></label>
          <label>Value Type<select v-model="form.payloadValueType"><option>STRING</option><option>NUMBER</option><option>BOOLEAN</option><option>CODE</option></select></label>
          <label>정책 Version<input v-model.trim="form.policyVersion" placeholder="v1"></label>
          <label class="span-2">변경 설명<input v-model.trim="form.payloadDescription" placeholder="운영자가 이해할 수 있는 변경 목적"></label>
          <label class="span-2">감사 사유<input v-model.trim="form.reason"></label>
        </div>
        <div class="cpf-action-row">
          <button class="ghost" @click="previewTargets">대상 Preview</button>
          <button class="ghost" @click="previewChange">Diff Preview</button>
          <button class="danger" :disabled="busy" @click="createChange">변경 생성</button>
        </div>
        <p class="cpf-note">위험 변경은 approvalId 또는 승인된 breakGlassId가 없으면 Backend에서 fail-closed 됩니다.</p>
      </section>

      <section class="cpf-card">
        <div class="cpf-card-head"><h2>Preview / 상태</h2></div>
        <StructuredDetails :value="preview" empty-message="Preview 결과가 없습니다."/>
      </section>
    </div>

    <section class="cpf-card">
      <div class="cpf-card-head"><h2>변경 조회·복구·통제</h2></div>
      <div class="cpf-toolbar">
        <input v-model.trim="lookup.changeId" placeholder="Change ID">
        <input v-model.trim="lookup.commandId" placeholder="Command ID">
        <button class="primary" @click="loadChange">조회</button>
        <button class="ghost" @click="verifyAudit">Audit 검증</button>
      </div>
      <div class="cpf-form-grid">
        <label>통제 Command ID<input v-model="control.commandId"></label>
        <label class="span-2">통제 사유<input v-model.trim="control.reason"></label>
      </div>
      <div class="cpf-action-row">
        <button class="danger" @click="cancelChange">예약/진행 취소</button>
        <button class="danger" @click="rollbackChange">Exact Rollback</button>
      </div>
      <div class="table-wrap" v-if="change.changeId">
        <table><tbody>
          <tr><th>Change</th><td>{{ change.changeId }}</td><th>State</th><td><span class="cpf-status" :class="stateClass(change.state)">{{ change.state }}</span></td></tr>
          <tr><th>Type</th><td>{{ change.changeType }}</td><th>Version</th><td>{{ change.desiredVersion }}</td></tr>
          <tr><th>Targets</th><td>{{ change.targetCount }}</td><th>ACK / Failed / Drift</th><td>{{ change.acknowledgedCount }} / {{ change.failedCount }} / {{ change.driftCount }}</td></tr>
          <tr><th>Hash</th><td colspan="3">{{ change.requestHash }}</td></tr>
          <tr><th>Message</th><td colspan="3">{{ change.message }}</td></tr>
        </tbody></table>
      </div>
      <StructuredDetails :value="auditVerification" empty-message="Audit 검증 결과가 없습니다."/>
    </section>

    <section class="cpf-card">
      <div class="cpf-card-head"><h2>Runtime Instance Group</h2><span class="cpf-status">CAS / 멱등</span></div>
      <div class="cpf-form-grid">
        <label>Group ID<input v-model.trim="groupForm.groupId" placeholder="PROD-MBR-A"></label>
        <label>Group Name<input v-model.trim="groupForm.groupName" placeholder="회원 PROD A그룹"></label>
        <label>Parent Group<input v-model.trim="groupForm.parentGroupId"></label>
        <label>Environment<input v-model.trim="groupForm.environment" placeholder="PROD"></label>
        <label>Expected Version<input v-model.number="groupForm.expectedVersion" type="number" min="0"></label>
        <label>Active<select v-model="groupForm.active"><option :value="true">Y</option><option :value="false">N</option></select></label>
        <label class="span-2">Description<input v-model.trim="groupForm.description"></label>
        <label class="span-2">변경 사유<input v-model.trim="groupForm.reason"></label>
      </div>
      <div class="cpf-action-row">
        <button class="ghost" @click="loadGroup">그룹 조회</button>
        <button class="primary" :disabled="busy" @click="saveGroup">그룹 저장</button>
        <button class="danger" :disabled="busy || groupForm.expectedVersion === null" @click="deleteGroup">그룹 삭제</button>
      </div>
      <div class="cpf-form-grid">
        <label>Instance ID<input v-model.trim="memberForm.instanceId" placeholder="MBR-01"></label>
        <label>Member Active<select v-model="memberForm.active"><option :value="true">추가/활성</option><option :value="false">제거/비활성</option></select></label>
        <label class="span-2">멤버 변경 사유<input v-model.trim="memberForm.reason"></label>
      </div>
      <div class="cpf-action-row"><button class="primary" :disabled="busy" @click="changeGroupMember">멤버 반영</button></div>
      <StructuredDetails :value="group" empty-message="Runtime Group을 조회하세요."/>
    </section>

    <section class="cpf-card">
      <div class="cpf-card-head"><h2>Desired / Actual / Drift</h2></div>
      <div class="cpf-toolbar"><input v-model.trim="search.environment" placeholder="Environment"><input v-model.trim="search.serviceId" placeholder="Service ID"><button class="primary" @click="loadStatus">조회</button></div>
      <StructuredDetails :value="status" empty-message="Desired/Actual 상태가 없습니다."/>
    </section>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  admRuntimeControlCancelChange,
  admRuntimeControlChangeGroupMember,
  admRuntimeControlCreateChange,
  admRuntimeControlDeleteGroup,
  admRuntimeControlFindChange,
  admRuntimeControlFindGroup,
  admRuntimeControlFindHealth,
  admRuntimeControlFindStatus,
  admRuntimeControlPreviewChange,
  admRuntimeControlPreviewTargets,
  admRuntimeControlRollbackChange,
  admRuntimeControlSaveGroup,
  admRuntimeControlVerifyAudit,
} from "../../generated/orval/cpf-api";
import type { AdmRuntimeControlChangeGroupMemberRequest } from "../../generated/orval/model/admRuntimeControlChangeGroupMemberRequest";
import type { AdmRuntimeControlCreateChangeRequest } from "../../generated/orval/model/admRuntimeControlCreateChangeRequest";
import type { AdmRuntimeControlPreviewTargetsRequest } from "../../generated/orval/model/admRuntimeControlPreviewTargetsRequest";
import type { AdmRuntimeControlSaveGroupRequest } from "../../generated/orval/model/admRuntimeControlSaveGroupRequest";
import type { CpfRuntimePayload } from "../../generated/orval/model/cpfRuntimePayload";
import type { CpfRuntimeTargetSelector } from "../../generated/orval/model/cpfRuntimeTargetSelector";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import StructuredDetails from "../../components/StructuredDetails.vue";

type Json = Record<string, any>;

export default defineComponent({
  name: "RuntimeControlPage",
  components: { StructuredDetails },
  setup() { return useAdmConsolePage(); },
  data() {
    return {
      busy: false,
      error: "",
      health: {} as Json,
      status: {} as Json,
      preview: {} as Json,
      change: {} as Json,
      auditVerification: {} as Json,
      group: {} as Json,
      groupForm: {
        commandId: crypto.randomUUID(), groupId: "", groupName: "", parentGroupId: "",
        environment: "", description: "", expectedVersion: null as number|null, active: true, reason: "Runtime 그룹 변경"
      },
      memberForm: { commandId: crypto.randomUUID(), instanceId: "", active: true, reason: "Runtime 그룹 멤버 변경" },
      search: { environment: "", serviceId: "" },
      lookup: { changeId: "", commandId: "" },
      control: { commandId: crypto.randomUUID(), reason: "Runtime 변경 운영 통제" },
      form: {
        commandId: crypto.randomUUID(), changeType: "GATEWAY_HEADER", payloadSchemaVersion: 1,
        environment: "", serviceId: "", groupId: "", instanceIds: "", expectedVersion: null as number|null,
        rolloutMode: "ALL_AT_ONCE", waveSize: 1, quorumPercent: 100, allowAll: false,
        approvalId: "", breakGlassId: "", payloadKey: "", payloadValue: "", payloadValueType: "STRING", policyVersion: "v1", payloadDescription: "", reason: "Runtime 정책 변경"
      }
    };
  },
  mounted() { this.loadOverview(); },
  methods: {
    stateClass(state:string) { const v=String(state||"").toUpperCase(); return v==="SUCCESS"||v==="ACKED"?"success":v==="FAILED"||v==="UNKNOWN_RESULT"?"danger":"warning"; },
    target(): CpfRuntimeTargetSelector {
      return {
        environment: this.form.environment || undefined, serviceId: this.form.serviceId || undefined, groupId: this.form.groupId || undefined,
        instanceIds: this.form.instanceIds.split(",").map((v:string)=>v.trim()).filter(Boolean), excludeInstanceIds: [], labels: {},
        zone: undefined, cell: undefined, includeDraining: false, includeMaintenance: false, allowAll: this.form.allowAll
      };
    },
    command(): AdmRuntimeControlCreateChangeRequest {
      if (!this.form.payloadKey.trim()) throw new Error("설정 Key가 필요합니다.");
      let normalizedValue:any=this.form.payloadValue;
      if(this.form.payloadValueType==="NUMBER") { normalizedValue=Number(this.form.payloadValue); if(Number.isNaN(normalizedValue)) throw new Error("숫자 Value를 확인하세요."); }
      if(this.form.payloadValueType==="BOOLEAN") { const v=String(this.form.payloadValue).toLowerCase(); if(!["true","false","y","n"].includes(v)) throw new Error("Boolean Value는 true/false 또는 Y/N입니다."); normalizedValue=["true","y"].includes(v); }
      const payload:CpfRuntimePayload={key:this.form.payloadKey,value:normalizedValue,valueType:this.form.payloadValueType,policyVersion:this.form.policyVersion,description:this.form.payloadDescription};
      return {
        commandId:this.form.commandId, changeType:this.form.changeType, payloadSchemaVersion:this.form.payloadSchemaVersion,
        target:this.target(), payload, expectedVersion:this.form.expectedVersion ?? undefined, rolloutMode:this.form.rolloutMode,
        waveSize:this.form.waveSize, quorumPercent:this.form.quorumPercent, scheduledAt:undefined, expiresAt:undefined,
        reason:this.form.reason, approvalId:this.form.approvalId||undefined, breakGlassId:this.form.breakGlassId||undefined
      };
    },
    async run(action:()=>Promise<any>) { this.busy=true; this.error=""; try{return await action();}catch(e){this.error=e instanceof Error?e.message:String(e);return null;}finally{this.busy=false;} },
    async loadOverview(){ await Promise.allSettled([this.loadHealth(),this.loadStatus()]); },
    async loadHealth(){ this.health=(await admRuntimeControlFindHealth()).data as Json; },
    async loadStatus(){ this.status=(await admRuntimeControlFindStatus({environment:this.search.environment||undefined,serviceId:this.search.serviceId||undefined})).data as Json; },
    async previewTargets(){ await this.run(async()=>{const request:AdmRuntimeControlPreviewTargetsRequest={changeType:this.form.changeType,payloadSchemaVersion:this.form.payloadSchemaVersion,target:this.target()};this.preview=(await admRuntimeControlPreviewTargets(request)).data as Json;}); },
    async previewChange(){ await this.run(async()=>{this.preview=(await admRuntimeControlPreviewChange(this.command())).data as Json;}); },
    async createChange(){ await this.run(async()=>{this.change=(await admRuntimeControlCreateChange(this.command())).data as Json;this.lookup.changeId=this.change.changeId||"";this.form.commandId=crypto.randomUUID();this.control.commandId=crypto.randomUUID();await this.loadOverview();}); },
    async loadChange(){ await this.run(async()=>{if(!this.lookup.changeId)throw new Error("Change ID가 필요합니다.");this.change=(await admRuntimeControlFindChange(this.lookup.changeId)).data as Json;this.lookup.changeId=this.change.changeId||this.lookup.changeId;}); },
    async verifyAudit(){ await this.run(async()=>{if(!this.lookup.changeId)throw new Error("Change ID가 필요합니다.");this.auditVerification=(await admRuntimeControlVerifyAudit(this.lookup.changeId)).data as Json;}); },
    async cancelChange(){ await this.controlChange("cancel"); },
    async rollbackChange(){ await this.controlChange("rollback"); },
    async controlChange(action:"cancel"|"rollback"){ await this.run(async()=>{if(!this.lookup.changeId)throw new Error("Change ID가 필요합니다.");if(!this.control.reason.trim())throw new Error("통제 사유가 필요합니다.");const response=action==="cancel"?await admRuntimeControlCancelChange(this.lookup.changeId,this.control):await admRuntimeControlRollbackChange(this.lookup.changeId,this.control);this.change=response.data as Json;this.control.commandId=crypto.randomUUID();await this.loadOverview();}); },
    async loadGroup(){ await this.run(async()=>{if(!this.groupForm.groupId)throw new Error("Group ID가 필요합니다.");this.group=(await admRuntimeControlFindGroup(this.groupForm.groupId)).data as Json;this.groupForm.groupName=this.group.groupName||"";this.groupForm.parentGroupId=this.group.parentGroupId||"";this.groupForm.environment=this.group.environment||"";this.groupForm.description=this.group.description||"";this.groupForm.expectedVersion=Number(this.group.rowVersion);this.groupForm.active=Boolean(this.group.active);}); },
    async saveGroup(){ await this.run(async()=>{if(!this.groupForm.groupId||!this.groupForm.groupName||!this.groupForm.environment||!this.groupForm.reason)throw new Error("Group ID, 이름, 환경, 사유가 필요합니다.");const request:AdmRuntimeControlSaveGroupRequest={commandId:this.groupForm.commandId,groupId:this.groupForm.groupId,groupName:this.groupForm.groupName,parentGroupId:this.groupForm.parentGroupId||undefined,environment:this.groupForm.environment,description:this.groupForm.description||undefined,expectedVersion:this.groupForm.expectedVersion??undefined,active:this.groupForm.active,reason:this.groupForm.reason};this.group=(await admRuntimeControlSaveGroup(request)).data as Json;this.groupForm.expectedVersion=Number(this.group.rowVersion);this.groupForm.commandId=crypto.randomUUID();}); },
    async changeGroupMember(){ await this.run(async()=>{if(!this.groupForm.groupId||!this.memberForm.instanceId||!this.memberForm.reason)throw new Error("Group ID, Instance ID, 사유가 필요합니다.");const request:AdmRuntimeControlChangeGroupMemberRequest={commandId:this.memberForm.commandId,groupId:this.groupForm.groupId,instanceId:this.memberForm.instanceId,active:this.memberForm.active,reason:this.memberForm.reason};this.group=(await admRuntimeControlChangeGroupMember(this.groupForm.groupId,request)).data as Json;this.groupForm.expectedVersion=Number(this.group.rowVersion);this.memberForm.commandId=crypto.randomUUID();}); },
    async deleteGroup(){ await this.run(async()=>{if(!this.groupForm.groupId||this.groupForm.expectedVersion===null||!this.groupForm.reason)throw new Error("Group ID, Expected Version, 사유가 필요합니다.");await admRuntimeControlDeleteGroup(this.groupForm.groupId,{commandId:this.groupForm.commandId,expectedVersion:this.groupForm.expectedVersion,reason:this.groupForm.reason});this.group={};this.groupForm.commandId=crypto.randomUUID();this.groupForm.expectedVersion=null;}); }
  }
});
</script>
