<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import OperationStateBanner from '../../components/OperationStateBanner.vue'
import {
  findIncidents, findMaintenance, findPolicies, findTimeline, saveMaintenance, savePolicy, transitionIncident,
  type Incident, type IncidentPolicy, type MaintenanceWindow, type Timeline
} from './api'

type Tab = 'incidents'|'policies'|'maintenance'
const tab = ref<Tab>('incidents')
const loading = ref(false)
const error = ref('')
const status = ref('OPEN')
const page = ref(0)
const size = ref(50)
const totalPages = ref(0)
const incidents = ref<Incident[]>([])
const policies = ref<IncidentPolicy[]>([])
const maintenance = ref<MaintenanceWindow[]>([])
const selected = ref<Incident|null>(null)
const selectedPolicy = ref<IncidentPolicy|null>(null)
const selectedMaintenance = ref<MaintenanceWindow|null>(null)
const timeline = ref<Timeline[]>([])
const reason = ref('')
const approvalRequestId = ref('')
const actionBusy = ref(false)

const policyForm = reactive({
  policyCode: '', eventType: '', eventSubType: '', severity: 'WARNING', thresholdCount: 3,
  windowSeconds: 300, escalationMinutes: 15, receiverGroup: '', useYn: 'Y' as 'Y'|'N'
})
const maintenanceForm = reactive({
  maintenanceCode: '', targetType: 'ALL', targetId: '*', startsAt: '', endsAt: '', useYn: 'Y' as 'Y'|'N'
})

const canPrevious = computed(() => page.value > 0)
const canNext = computed(() => page.value + 1 < totalPages.value)
function idempotency(action:string,id:number|string){return `${action}-${id}-${Date.now()}-${crypto.randomUUID()}`}
function message(value:unknown){return value instanceof Error ? value.message : String(value)}
function requireProof(){
  if(reason.value.trim().length<5)throw new Error('조치 사유를 5자 이상 입력하세요.')
  if(!approvalRequestId.value.trim())throw new Error('승인 요청 ID가 필요합니다.')
  return {reason:reason.value.trim(),approvalRequestId:approvalRequestId.value.trim()}
}
function toLocalInput(value?:string){return value ? value.slice(0,16) : ''}

async function load(){
  loading.value=true;error.value=''
  try{
    if(tab.value==='incidents'){
      const result=await findIncidents(status.value,page.value,size.value)
      incidents.value=result.content;totalPages.value=result.totalPages
      if(selected.value){selected.value=incidents.value.find(v=>v.incidentId===selected.value?.incidentId) ?? null}
    }else if(tab.value==='policies'){
      const result=await findPolicies(page.value,size.value);policies.value=result.content;totalPages.value=result.totalPages
    }else if(tab.value==='maintenance'){
      const result=await findMaintenance(page.value,size.value);maintenance.value=result.content;totalPages.value=result.totalPages
    }
  }catch(e){error.value=message(e)}finally{loading.value=false}
}
async function selectIncident(item:Incident){selected.value=item;timeline.value=[];error.value='';try{timeline.value=await findTimeline(item.incidentId)}catch(e){error.value=message(e)}}
async function act(action:'acknowledge'|'resolve'|'reopen'|'escalate'){
  if(!selected.value)return
  actionBusy.value=true;error.value=''
  try{
    const proof=requireProof()
    selected.value=await transitionIncident(selected.value.incidentId,action,{
      expectedVersion:selected.value.version,...proof,idempotencyKey:idempotency(action,selected.value.incidentId)
    })
    reason.value='';approvalRequestId.value='';await Promise.all([load(),selectIncident(selected.value)])
  }catch(e){error.value=message(e)}finally{actionBusy.value=false}
}
function newPolicy(){selectedPolicy.value=null;Object.assign(policyForm,{policyCode:'',eventType:'',eventSubType:'',severity:'WARNING',thresholdCount:3,windowSeconds:300,escalationMinutes:15,receiverGroup:'',useYn:'Y'})}
function editPolicy(item:IncidentPolicy){selectedPolicy.value=item;Object.assign(policyForm,{policyCode:item.policyCode,eventType:item.eventType,eventSubType:item.eventSubType??'',severity:item.severity,thresholdCount:item.thresholdCount,windowSeconds:item.windowSeconds,escalationMinutes:item.escalationMinutes,receiverGroup:item.receiverGroup,useYn:item.useYn})}
async function submitPolicy(){
  actionBusy.value=true;error.value=''
  try{
    const proof=requireProof(); const current=selectedPolicy.value
    await savePolicy({...policyForm,expectedVersion:current?.version??0,...proof,idempotencyKey:idempotency('policy-save',current?.policyId??'new')},current?.policyId)
    reason.value='';approvalRequestId.value='';newPolicy();await load()
  }catch(e){error.value=message(e)}finally{actionBusy.value=false}
}
function newMaintenance(){selectedMaintenance.value=null;Object.assign(maintenanceForm,{maintenanceCode:'',targetType:'ALL',targetId:'*',startsAt:'',endsAt:'',useYn:'Y'})}
function editMaintenance(item:MaintenanceWindow){selectedMaintenance.value=item;Object.assign(maintenanceForm,{maintenanceCode:item.maintenanceCode,targetType:item.targetType,targetId:item.targetId,startsAt:toLocalInput(item.startsAt),endsAt:toLocalInput(item.endsAt),useYn:item.useYn})}
async function submitMaintenance(){
  actionBusy.value=true;error.value=''
  try{
    const proof=requireProof(); const current=selectedMaintenance.value
    if(!maintenanceForm.startsAt||!maintenanceForm.endsAt)throw new Error('점검 시작·종료 시각이 필요합니다.')
    await saveMaintenance({...maintenanceForm,startsAt:new Date(maintenanceForm.startsAt).toISOString(),endsAt:new Date(maintenanceForm.endsAt).toISOString(),expectedVersion:current?.version??0,...proof,idempotencyKey:idempotency('maintenance-save',current?.maintenanceId??'new')},current?.maintenanceId)
    reason.value='';approvalRequestId.value='';newMaintenance();await load()
  }catch(e){error.value=message(e)}finally{actionBusy.value=false}
}
function switchTab(value:Tab){tab.value=value;page.value=0;selected.value=null;selectedPolicy.value=null;selectedMaintenance.value=null;timeline.value=[];void load()}
function move(delta:number){page.value=Math.max(0,page.value+delta);void load()}
onMounted(load)
</script>

<template>
  <main class="incident-workbench" data-cpf-page="incident-workbench">
    <header>
      <div><h1>Notification · Incident Workbench</h1><p>Threshold, Escalation, Maintenance, Retry/DLQ와 감사 원장을 통합 조회합니다.</p></div>
      <button type="button" :disabled="loading" @click="load">새로고침</button>
    </header>
    <OperationStateBanner v-if="loading || error" :loading="loading" :error="error" />
    <nav aria-label="Incident workbench tabs">
      <button v-for="value in (['incidents','policies','maintenance'] as Tab[])" :key="value" type="button" :aria-current="tab===value?'page':undefined" @click="switchTab(value)">{{ value }}</button>
    </nav>

    <section v-if="tab==='incidents'" class="split">
      <div class="panel">
        <div class="filters"><label>상태 <select v-model="status" @change="page=0;load()"><option>OPEN</option><option>ACKNOWLEDGED</option><option>RESOLVED</option></select></label></div>
        <table><thead><tr><th>ID</th><th>심각도</th><th>상태</th><th>제목</th><th>발생</th><th>Escalation</th></tr></thead>
          <tbody><tr v-for="item in incidents" :key="item.incidentId" tabindex="0" @click="selectIncident(item)" @keydown.enter="selectIncident(item)"><td>{{item.incidentId}}</td><td>{{item.severity}}</td><td>{{item.status}}</td><td>{{item.title}}</td><td>{{item.occurrenceCount}}</td><td>{{item.escalationLevel}}</td></tr>
          <tr v-if="!loading && incidents.length===0"><td colspan="6">조건에 맞는 Incident가 없습니다.</td></tr></tbody></table>
      </div>
      <aside class="panel detail" aria-live="polite">
        <template v-if="selected">
          <h2>#{{selected.incidentId}} {{selected.title}}</h2>
          <dl><dt>Source</dt><dd>{{selected.sourceType}} / {{selected.sourceId}}</dd><dt>Correlation</dt><dd>{{selected.correlationId || '-'}}</dd><dt>Transaction</dt><dd>{{selected.transactionId || '-'}}</dd><dt>Version</dt><dd>{{selected.version}}</dd></dl>
          <label>조치 사유<textarea v-model="reason" rows="3" /></label><label>승인 요청 ID<input v-model="approvalRequestId" /></label>
          <div class="actions"><button :disabled="actionBusy" @click="act('acknowledge')">접수</button><button :disabled="actionBusy" @click="act('escalate')">Escalate</button><button :disabled="actionBusy" @click="act('resolve')">해결</button><button :disabled="actionBusy" @click="act('reopen')">재개방</button></div>
          <h3>Causal Timeline</h3><ol><li v-for="row in timeline" :key="row.timelineId"><strong>{{row.actionType}}</strong> {{row.beforeStatus}} → {{row.afterStatus}} · {{row.actorId}} · {{row.createdAt}}<br>{{row.reason}}</li></ol>
        </template><p v-else>Incident를 선택하면 상세·승인 조치·감사 Timeline을 확인할 수 있습니다.</p>
      </aside>
    </section>

    <section v-else-if="tab==='policies'" class="split">
      <div class="panel"><div class="filters"><h2>Threshold 정책</h2><button type="button" @click="newPolicy">신규</button></div><table><thead><tr><th>Code</th><th>Event</th><th>Severity</th><th>Threshold</th><th>Window</th><th>Escalation</th><th>Receiver</th><th>Version</th></tr></thead><tbody>
        <tr v-for="item in policies" :key="item.policyId" tabindex="0" @click="editPolicy(item)" @keydown.enter="editPolicy(item)"><td>{{item.policyCode}}</td><td>{{item.eventType}}</td><td>{{item.severity}}</td><td>{{item.thresholdCount}}</td><td>{{item.windowSeconds}}s</td><td>{{item.escalationMinutes}}m</td><td>{{item.receiverGroup}}</td><td>{{item.version}}</td></tr>
        <tr v-if="!loading && policies.length===0"><td colspan="8">정책이 없습니다.</td></tr></tbody></table></div>
      <form class="panel form" @submit.prevent="submitPolicy"><h2>{{selectedPolicy?'정책 수정':'정책 등록'}}</h2>
        <label>정책 Code<input v-model.trim="policyForm.policyCode" required /></label><label>Event Type<input v-model.trim="policyForm.eventType" required /></label><label>Event Sub Type<input v-model.trim="policyForm.eventSubType" /></label>
        <label>심각도<select v-model="policyForm.severity"><option>INFO</option><option>WARNING</option><option>CRITICAL</option></select></label><label>Threshold<input v-model.number="policyForm.thresholdCount" type="number" min="1" required /></label><label>Window(초)<input v-model.number="policyForm.windowSeconds" type="number" min="1" required /></label><label>Escalation(분)<input v-model.number="policyForm.escalationMinutes" type="number" min="1" required /></label><label>수신 그룹<input v-model.trim="policyForm.receiverGroup" required /></label><label>사용<select v-model="policyForm.useYn"><option>Y</option><option>N</option></select></label>
        <label>변경 사유<textarea v-model="reason" rows="3" required /></label><label>승인 요청 ID<input v-model.trim="approvalRequestId" required /></label><button :disabled="actionBusy" type="submit">저장</button>
      </form>
    </section>

    <section v-else-if="tab==='maintenance'" class="split">
      <div class="panel"><div class="filters"><h2>Maintenance Window</h2><button type="button" @click="newMaintenance">신규</button></div><table><thead><tr><th>Code</th><th>Target</th><th>Start</th><th>End</th><th>Use</th><th>Version</th></tr></thead><tbody>
        <tr v-for="item in maintenance" :key="item.maintenanceId" tabindex="0" @click="editMaintenance(item)" @keydown.enter="editMaintenance(item)"><td>{{item.maintenanceCode}}</td><td>{{item.targetType}} / {{item.targetId}}</td><td>{{item.startsAt}}</td><td>{{item.endsAt}}</td><td>{{item.useYn}}</td><td>{{item.version}}</td></tr>
        <tr v-if="!loading && maintenance.length===0"><td colspan="6">Maintenance Window가 없습니다.</td></tr></tbody></table></div>
      <form class="panel form" @submit.prevent="submitMaintenance"><h2>{{selectedMaintenance?'점검창 수정':'점검창 등록'}}</h2>
        <label>점검 Code<input v-model.trim="maintenanceForm.maintenanceCode" required /></label><label>Target Type<input v-model.trim="maintenanceForm.targetType" required /></label><label>Target ID<input v-model.trim="maintenanceForm.targetId" required /></label><label>시작<input v-model="maintenanceForm.startsAt" type="datetime-local" required /></label><label>종료<input v-model="maintenanceForm.endsAt" type="datetime-local" required /></label><label>사용<select v-model="maintenanceForm.useYn"><option>Y</option><option>N</option></select></label>
        <label>변경 사유<textarea v-model="reason" rows="3" required /></label><label>승인 요청 ID<input v-model.trim="approvalRequestId" required /></label><button :disabled="actionBusy" type="submit">저장</button>
      </form>
    </section>

    <footer><button :disabled="!canPrevious" @click="move(-1)">이전</button><span>{{page+1}} / {{Math.max(totalPages,1)}}</span><button :disabled="!canNext" @click="move(1)">다음</button></footer>
  </main>
</template>

<style scoped>
.incident-workbench{display:grid;gap:1rem;padding:1rem}header,.filters,footer,.actions{display:flex;gap:.75rem;align-items:center;justify-content:space-between}nav{display:flex;gap:.5rem}.split{display:grid;grid-template-columns:minmax(0,2fr) minmax(20rem,1fr);gap:1rem}.panel{border:1px solid #d8dde6;border-radius:.5rem;padding:1rem;overflow:auto}.detail{max-height:75vh}.form{display:grid;align-content:start;gap:.35rem}table{width:100%;border-collapse:collapse}th,td{padding:.6rem;border-bottom:1px solid #e6e9ef;text-align:left}tbody tr{cursor:pointer}tbody tr:focus,tbody tr:hover{outline:2px solid currentColor;outline-offset:-2px}label{display:grid;gap:.35rem;margin:.35rem 0}input,select,textarea,button{font:inherit;padding:.5rem}dl{display:grid;grid-template-columns:8rem 1fr}dt{font-weight:700}ol{padding-left:1.25rem}@media(max-width:900px){.split{grid-template-columns:1fr}header{align-items:flex-start;flex-direction:column}}
</style>
