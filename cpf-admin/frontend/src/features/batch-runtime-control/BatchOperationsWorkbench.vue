<template>
  <section class="cpf-page batch-workbench" :data-workbench-mode="mode">
    <header class="cpf-page-heading">
      <div><p class="eyebrow">BATCH OPERATIONS / {{ mode.toUpperCase() }}</p><h2>{{ config.title }}</h2><p>{{ config.description }}</p></div>
      <div class="heading-actions"><span class="freshness">{{ fetchedAt ? `조회 ${formatDate(fetchedAt)}` : '미조회' }}</span><button class="ghost" type="button" @click="load">새로고침</button></div>
    </header>

    <OperationStateBanner :loading="loading" :failure="failure" :partial="partial" :stale="stale" :empty="!loading && !failure && rows.length===0" :fetched-at="fetchedAt" @retry="load" />

    <section v-if="mode==='overview'" class="cpf-kpi-grid" aria-label="Batch 운영 KPI">
      <div v-for="metric in overviewMetrics" :key="metric.label" class="cpf-stat-card"><span class="label">{{ metric.label }}</span><strong class="value">{{ metric.value }}</strong></div>
    </section>

    <form v-if="config.paged" class="cpf-toolbar" @submit.prevent="applySearch">
      <input v-model.trim="filters.query" type="search" :placeholder="`${config.title} 통합 검색`" aria-label="통합 검색">
      <input v-if="mode==='executions'" v-model.trim="filters.jobId" placeholder="Job ID" aria-label="Job ID">
      <select v-if="mode==='executions'" v-model="filters.status" aria-label="실행 상태"><option value="">전체 상태</option><option>RUNNING</option><option>COMPLETED</option><option>FAILED</option><option>STOPPING</option><option>UNKNOWN</option></select>
      <button class="primary" type="submit">조회</button><button class="ghost" type="button" @click="resetSearch">초기화</button>
    </form>

    <div class="workbench-layout">
      <section class="cpf-card result-card">
        <div class="cpf-card-head"><div><h3>{{ config.listTitle }}</h3><p>총 {{ total }}건</p></div><div class="inline-actions"><button v-if="mode==='scheduler'" class="danger ghost" type="button" @click="prepareGlobalAction('scheduler-run')">Scheduler 1회 실행</button></div></div>
        <div class="table-wrap"><table><thead><tr><th v-for="column in config.columns" :key="column.key">{{ column.label }}</th><th>상세</th></tr></thead>
          <tbody><tr v-for="(row,index) in rows" :key="rowKey(row,index)" :class="{selected:selected===row}" @click="selectRow(row)">
            <td v-for="column in config.columns" :key="column.key"><span v-if="column.status" class="cpf-status" :class="statusClass(value(row,column.keys))">{{ value(row,column.keys) }}</span><template v-else>{{ displayValue(row,column.keys,column.mask) }}</template></td>
            <td><button class="text-button" type="button" @click.stop="selectRow(row)">열기</button></td>
          </tr></tbody></table></div>
        <footer v-if="config.paged" class="pagination"><button class="ghost" type="button" :disabled="page===0" @click="changePage(page-1)">이전</button><span>{{ page+1 }} / {{ pageCount }}</span><button class="ghost" type="button" :disabled="!hasNext" @click="changePage(page+1)">다음</button><select v-model.number="size" @change="changePage(0)"><option :value="20">20</option><option :value="50">50</option><option :value="100">100</option></select></footer>
      </section>

      <aside class="cpf-card detail-card" aria-live="polite">
        <div class="cpf-card-head"><div><h3>{{ selected ? config.detailTitle : '상세 대기' }}</h3><p>{{ selected ? selectedIdentity : '목록에서 대상을 선택하세요.' }}</p></div></div>
        <template v-if="selected">
          <nav class="cpf-tabs" aria-label="상세 탭"><button v-for="tab in detailTabs" :key="tab" :class="{active:detailTab===tab}" type="button" @click="detailTab=tab">{{ tab }}</button></nav>
          <div v-if="detailLoading" class="route-loading">상세 정보를 조회하고 있습니다...</div>
          <OperationStateBanner v-else-if="detailFailure" :failure="detailFailure" @retry="selectRow(selected)" />
          <template v-else>
            <dl v-if="detailTab==='요약'" class="detail-grid"><template v-for="entry in detailEntries" :key="entry.key"><dt>{{ entry.key }}</dt><dd>{{ entry.value }}</dd></template></dl>
            <ol v-else-if="detailTab==='Step Timeline'" class="timeline"><li v-for="(step,index) in detailSteps" :key="rowKey(step,index)"><strong>{{ displayValue(step,['stepName','step_name','name']) }}</strong><span class="cpf-status" :class="statusClass(value(step,['status','batchStatus']))">{{ displayValue(step,['status','batchStatus']) }}</span><small>{{ displayValue(step,['startTime','start_time']) }} → {{ displayValue(step,['endTime','end_time']) }}</small><p>Read {{ displayValue(step,['readCount','read_count']) }} / Write {{ displayValue(step,['writeCount','write_count']) }} / Skip {{ displayValue(step,['skipCount','skip_count']) }}</p></li></ol>
            <div v-else-if="detailTab==='운영 이력'" class="table-wrap"><table><thead><tr><th>시각</th><th>조치</th><th>상태</th><th>사유</th></tr></thead><tbody><tr v-for="(op,index) in detailOperations" :key="rowKey(op,index)"><td>{{ displayValue(op,['createdAt','created_at','requestedAt']) }}</td><td>{{ displayValue(op,['operationType','operation_type','actionType']) }}</td><td>{{ displayValue(op,['status','resultStatus']) }}</td><td>{{ displayValue(op,['reason'],true) }}</td></tr></tbody></table></div>
            <CpfStructuredData v-else class="detail-json" :value="detailPayload" />
          </template>
          <div class="danger-zone" v-if="availableActions.length"><h4>운영 조치</h4><p>모든 조치는 사유·승인·Expected Version·멱등 키와 결과 추적을 사용합니다.</p><div class="inline-actions"><button v-for="action in availableActions" :key="action.id" :class="action.risk==='CRITICAL'?'danger':'ghost'" type="button" @click="prepareAction(action.id)">{{ action.label }}</button></div></div>
        </template>
      </aside>
    </div>

    <DangerousActionDialog :open="Boolean(pendingAction)" :title="pendingActionConfig.title" :description="pendingActionConfig.description" :target="pendingTarget" :risk="pendingActionConfig.risk" :approval-required="pendingActionConfig.approvalRequired" :expected-version="selectedVersion" :expected-version-required="pendingActionConfig.expectedVersionRequired" :submitting="actionSubmitting" :confirm-label="pendingActionConfig.confirmLabel" @cancel="pendingAction=null" @confirm="executeAction" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import DangerousActionDialog from "../../components/DangerousActionDialog.vue";
import OperationStateBanner from "../../components/OperationStateBanner.vue";
import { classifyAdmFailure, maskOperationalValue, type AdmFailureState } from "../../shared/operationState";
import {
  actGhostExecution, fetchBatchAlertsWorkspace, fetchBatchAuditWorkspace, fetchBatchOverview, fetchBatchView,
  fetchCenterCutJobs, fetchCenterCutWorkspace, fetchExecutionPage, fetchExecutionWorkspace, fetchInfrastructure,
  fetchJobPage, fetchJobWorkspace, fetchRecovery, fetchSchedulePage, releaseLock, resolveUnknownResult,
  retryExecution, runJob, runSchedulerOnce, setScheduleEnabled, simulateSchedule, stopExecution,
  type AdmPage, type BatchInfrastructureWorkspace, type BatchRecoveryWorkspace
} from "./api";

type Mode="overview"|"topology"|"instances"|"scheduler"|"workerPools"|"centerCut"|"agents"|"jobs"|"executions"|"deployment"|"recovery"|"leases"|"alerts"|"audit";
type Row=Record<string,unknown>;
interface Column{key:string;label:string;keys:string[];status?:boolean;mask?:boolean}
interface ActionConfig{id:string;label:string;title:string;description:string;risk:"HIGH"|"CRITICAL";approvalRequired:boolean;expectedVersionRequired:boolean;confirmLabel:string}
const props=defineProps<{mode:Mode}>();
const mode=computed(()=>props.mode);
const loading=ref(false), detailLoading=ref(false), actionSubmitting=ref(false);
const failure=ref<AdmFailureState|null>(null), detailFailure=ref<AdmFailureState|null>(null);
const rows=ref<Row[]>([]), selected=ref<Row|null>(null), detail=ref<Row>({});
const page=ref(0),size=ref(50),total=ref(0),hasNext=ref(false),fetchedAt=ref(""),partial=ref(false),stale=ref(false);
const detailTab=ref("요약"),pendingAction=ref<string|null>(null);
const filters=reactive({query:"",jobId:"",status:""});
const C=(key:string,label:string,keys:string[],status=false,mask=false):Column=>({key,label,keys,status,mask});
const configs:Record<Mode,{title:string;description:string;listTitle:string;detailTitle:string;paged:boolean;columns:Column[]}>={
 overview:{title:"Batch 통합 운영 Dashboard",description:"Backlog·실패·Worker·Unknown Result를 하나의 운영 관점으로 조회합니다.",listTitle:"최근 실행",detailTitle:"최근 실행 상세",paged:false,columns:[C("id","Execution",["executionId","execution_id"]),C("job","Job",["jobId","job_id"]),C("status","상태",["status","executionStatus","execution_status"],true),C("start","시작",["startTime","start_time"])]},
 topology:{title:"Runtime Topology",description:"Scheduler·Worker·Agent·Server Instance와 수행 대상을 연결해 봅니다.",listTitle:"Runtime Node",detailTitle:"Node 상세",paged:false,columns:[C("instance","Instance",["instanceId","instance_id","workerId","worker_id"]),C("role","Role",["runtimeRole","runtime_role","workerType"]),C("state","상태",["effectiveState","effective_state","status"],true),C("heartbeat","Heartbeat",["lastHeartbeatAt","last_heartbeat_at"])]},
 instances:{title:"Runtime Instances",description:"Instance identity, artifact version, heartbeat, fencing 상태를 관제합니다.",listTitle:"Instances",detailTitle:"Instance 상세",paged:false,columns:[C("instance","Instance",["instanceId","instance_id"]),C("service","Service",["serviceId","service_id"]),C("state","상태",["effectiveState","effective_state","status"],true),C("version","Artifact",["artifactVersion","artifact_version"]),C("fencing","Fencing",["fencingToken","fencing_token"])]},
 scheduler:{title:"Scheduler HA Workbench",description:"Schedule Preview·Business Calendar·Misfire·Leader/Lease·중복 억제와 Failover 영향을 확인합니다.",listTitle:"Schedules",detailTitle:"Schedule 상세",paged:true,columns:[C("schedule","Schedule",["scheduleId","schedule_id"]),C("job","Job",["jobId","job_id"]),C("cron","Cron/Policy",["cronExpression","cron_expression","schedulePolicy"]),C("enabled","상태",["enabled","status"],true),C("next","Next Fire",["nextFireTime","next_fire_time"])]},
 workerPools:{title:"Worker Pool Control",description:"Capacity·Queue Depth·Active Task·Backpressure·Drain·Quarantine 상태를 운영합니다.",listTitle:"Workers",detailTitle:"Worker 상세",paged:false,columns:[C("worker","Worker",["workerId","worker_id","instanceId","instance_id"]),C("pool","Pool",["poolId","pool_id"]),C("status","상태",["status","effectiveState","effective_state"],true),C("active","Active",["activeTaskCount","active_task_count","currentExecutionId"]),C("heartbeat","Heartbeat",["lastHeartbeatAt","last_heartbeat_at"])]},
 centerCut:{title:"Center-Cut Workbench",description:"Profile·속도·분할·Encoding·Simulation·Execution 통계와 Recovery를 연결합니다.",listTitle:"Center-Cut Job",detailTitle:"Center-Cut 상세",paged:false,columns:[C("job","Job",["centerCutJobId","center_cut_job_id","jobId","job_id"]),C("name","이름",["jobName","job_name","name"]),C("status","상태",["status","state"],true),C("profile","Profile",["profileId","profile_id"]),C("updated","갱신",["updatedAt","updated_at"])]},
 agents:{title:"Host Agent Control",description:"Heartbeat·OS/JVM/Disk·Artifact Trust·Process·Deployment·Rollback을 통합합니다.",listTitle:"Host Agents",detailTitle:"Agent 상세",paged:false,columns:[C("agent","Agent",["agentId","agent_id","instanceId","instance_id"]),C("host","Host",["hostAlias","host_alias","hostName"]),C("status","상태",["status","effectiveState","effective_state"],true),C("version","Version",["artifactVersion","artifact_version","version"]),C("heartbeat","Heartbeat",["lastHeartbeatAt","last_heartbeat_at"])]},
 jobs:{title:"Job Definition Workspace",description:"Definition Version·Parameter·DAG·Schedule Simulation·실행 이력·영향도를 연결합니다.",listTitle:"Jobs",detailTitle:"Job 상세",paged:true,columns:[C("job","Job",["jobId","job_id"]),C("name","이름",["jobName","job_name"]),C("type","유형",["jobType","job_type"]),C("status","상태",["status"],true),C("last","최근 실행",["lastExecutionAt","last_execution_at"])]},
 executions:{title:"Batch Execution Workbench",description:"실행 상세·Step Timeline·Parameter·Execution Context·Worker/Server·Log·Report·재실행·중지를 제공합니다.",listTitle:"Executions",detailTitle:"Execution 상세",paged:true,columns:[C("execution","Execution",["executionId","execution_id"]),C("job","Job",["jobId","job_id"]),C("status","상태",["status","executionStatus","execution_status"],true),C("worker","Worker/Server",["workerId","worker_id","serverInstanceId","server_instance_id"]),C("start","시작",["startTime","start_time"]),C("duration","Duration",["durationMs","duration_ms"])]},
 deployment:{title:"Batch Deployment / Rollback",description:"Artifact hash·Source SHA·대상 Instance Drift·Rollout·부분 실패·Rollback을 추적합니다.",listTitle:"Deployment Plans",detailTitle:"Deployment 상세",paged:false,columns:[C("plan","Plan",["plan_id","planId","deployment_id"]),C("artifact","Artifact",["artifact_version","artifactVersion","artifact_hash"]),C("status","상태",["status"],true),C("target","Target",["target_count","targetCount"]),C("updated","갱신",["updated_at","updatedAt"])]},
 recovery:{title:"Recovery / Unknown Result Center",description:"Lease·Fencing·Idempotency·Reconcile·Replay·Compensation·Manual Decision을 안전하게 처리합니다.",listTitle:"Unknown / Ghost Candidates",detailTitle:"Recovery 상세",paged:false,columns:[C("type","유형",["recordType","type"]),C("execution","Execution / Unknown",["executionId","execution_id","unknownId","unknown_id"]),C("job","Job",["jobId","job_id"]),C("status","상태",["status","recoveryStatus","resolutionStatus"],true),C("time","발생/Heartbeat",["createdAt","created_at","lastHeartbeatAt","last_heartbeat_at"])]},
 leases:{title:"Lease / Fencing",description:"소유권·만료·Fencing Token·Takeover·Ghost Lock 위험을 추적합니다.",listTitle:"Locks / Leases",detailTitle:"Lease 상세",paged:false,columns:[C("key","Lock Key",["lockKey","lock_key"]),C("owner","Owner",["ownerId","owner_id","workerId"]),C("status","상태",["status","leaseStatus"],true),C("expires","Expires",["expiresAt","expires_at"]),C("fencing","Fencing",["fencingToken","fencing_token"])]},
 alerts:{title:"Batch Alert Center",description:"Unknown Result·Retry/DLQ·Outbox·Escalation·Incident를 연결합니다.",listTitle:"Alert / Broker Backlog",detailTitle:"Alert 상세",paged:false,columns:[C("type","유형",["recordType","type"]),C("id","ID",["unknownId","unknown_id","messageId","message_id","operationId","operation_id"]),C("status","상태",["status","resolutionStatus","deliveryStatus"],true),C("target","Target",["targetId","target_id","jobId","job_id","topic"]),C("time","발생",["createdAt","created_at","requestedAt"])]},
 audit:{title:"Batch Audit / Evidence",description:"위험조치 요청·승인·결과·immutable audit·Evidence Artifact를 연결합니다.",listTitle:"Operation Audit",detailTitle:"감사 상세",paged:false,columns:[C("time","시각",["createdAt","created_at","requestedAt"]),C("action","조치",["operationType","operation_type","actionType"]),C("target","대상",["executionId","execution_id","jobId","job_id","lockKey"]),C("status","결과",["status","resultStatus"],true),C("reason","사유",["reason"],false,true)]}
};
const config=computed(()=>configs[mode.value]);
const pageCount=computed(()=>Math.max(1,Math.ceil(total.value/size.value)));
const overview=ref<Row>({});
const overviewMetrics=computed(()=>[
 {label:"Job",value:value(overview.value,["jobCount"])},{label:"Schedule",value:value(overview.value,["scheduleCount"])},{label:"실행",value:value(overview.value,["executionCount"])},{label:"실패",value:value(overview.value,["failedExecutionCount"])},{label:"실행 중",value:value(overview.value,["runningExecutionCount"])},{label:"Worker",value:value(overview.value,["workerCount"])},{label:"Stale Worker",value:value(overview.value,["staleWorkerCount"])},{label:"Unknown Result",value:value(overview.value,["unknownResultCount"])}
]);
const detailTabs=computed(()=>mode.value==="executions"?["요약","Step Timeline","Parameter / Context","운영 이력"]:["요약","원본 JSON","운영 이력"]);
const detailSteps=computed(()=>Array.isArray(detail.value.steps)?detail.value.steps as Row[]:[]);
const detailOperations=computed(()=>Array.isArray(detail.value.operations)?detail.value.operations as Row[]:[]);
const detailPayload=computed(()=>detailTab.value==="Parameter / Context"?(detail.value.execution||detail.value):detail.value);
const detailEntries=computed(()=>Object.entries((detail.value.execution as Row)||detail.value).filter(([k])=>!/(password|secret|token|authorization|credential)/i.test(k)).slice(0,40).map(([key,val])=>({key,value:formatObject(val)})));
const selectedIdentity=computed(()=>selected.value?String(value(selected.value,["executionId","execution_id","unknownId","unknown_id","centerCutJobId","center_cut_job_id","jobId","job_id","instanceId","instance_id","scheduleId","schedule_id","lockKey","lock_key","messageId","message_id"])||"-"):"");
const selectedVersion=computed(()=>{
 const detailSource=detail.value&&Object.keys(detail.value).length?detail.value:undefined;
 const candidates=[
  detailSource?.execution as Row|undefined,
  detailSource?.schedule as Row|undefined,
  detailSource?.lock as Row|undefined,
  detailSource?.ghostCandidate as Row|undefined,
  detailSource,
  selected.value,
 ];
 for(const source of candidates){
  if(!source)continue;
  const raw=value(source,["rowVersion","row_version","version","expectedVersion"]);
  const parsed=Number(raw);
  if(Number.isSafeInteger(parsed)&&parsed>=0)return parsed;
 }
 return undefined;
});
const actionConfigs:Record<string,ActionConfig>={
 retry:{id:"retry",label:"재실행",title:"실행을 재시도하시겠습니까?",description:"기존 Parameter로 새 실행을 생성하며 중복 요청은 멱등 키로 차단합니다.",risk:"HIGH",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"재실행 요청"},
 stop:{id:"stop",label:"중지",title:"실행 중지를 요청하시겠습니까?",description:"응답 손실 시 결과불명 상태가 될 수 있으므로 Recovery Center에서 결과를 확인해야 합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"중지 요청"},
 run:{id:"run",label:"수동 실행",title:"Job을 수동 실행하시겠습니까?",description:"운영 Parameter와 승인 범위를 확인한 뒤 새 실행을 요청합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:false,confirmLabel:"실행 요청"},
 "schedule-enable":{id:"schedule-enable",label:"활성화",title:"Schedule을 활성화하시겠습니까?",description:"Next-fire와 중복 실행 영향을 확인하세요.",risk:"HIGH",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"활성화"},
 "schedule-disable":{id:"schedule-disable",label:"비활성화",title:"Schedule을 비활성화하시겠습니까?",description:"예약된 실행과 운영 SLA 영향을 확인하세요.",risk:"HIGH",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"비활성화"},
 "scheduler-run":{id:"scheduler-run",label:"Scheduler 1회 실행",title:"Scheduler 판정을 즉시 실행하시겠습니까?",description:"현재 시점의 due schedule을 평가합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:false,confirmLabel:"1회 실행"},
 "release-lock":{id:"release-lock",label:"Lock 해제",title:"Lock을 강제로 해제하시겠습니까?",description:"실제 실행이 살아 있으면 중복 실행이 발생할 수 있습니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"강제 해제"},
 "ghost-fail":{id:"ghost-fail",label:"실패 확정",title:"Ghost 실행을 실패로 확정하시겠습니까?",description:"Lease·Fencing·Worker 상태를 확인한 후 수동 판정을 기록합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"실패 확정"},
 "ghost-abandon":{id:"ghost-abandon",label:"폐기 확정",title:"Ghost 실행을 폐기 상태로 확정하시겠습니까?",description:"재조회된 Heartbeat와 실행 상태가 여전히 Ghost 조건일 때만 ABANDONED로 전이합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:true,confirmLabel:"폐기 확정"},
 "unknown-resolve":{id:"unknown-resolve",label:"실패 확정",title:"결과불명 건을 실패로 확정하시겠습니까?",description:"외부계·Broker·Worker 원장을 대사한 뒤 승인된 판정을 감사 로그와 함께 기록합니다.",risk:"CRITICAL",approvalRequired:true,expectedVersionRequired:false,confirmLabel:"결과 확정"}
};
const availableActions=computed<ActionConfig[]>(()=>{
 if(mode.value==="executions")return [actionConfigs.retry,actionConfigs.stop];
 if(mode.value==="jobs")return [actionConfigs.run];
 if(mode.value==="scheduler")return [actionConfigs["schedule-enable"],actionConfigs["schedule-disable"]];
 if(mode.value==="recovery")return value(selected.value||{},["unknownId","unknown_id"])?[actionConfigs["unknown-resolve"]]:[actionConfigs["ghost-abandon"],actionConfigs["ghost-fail"]];
 if(mode.value==="leases")return [actionConfigs["release-lock"]];
 return [];
});
const pendingActionConfig=computed(()=>pendingAction.value?actionConfigs[pendingAction.value]:{title:"",description:"",risk:"HIGH" as const,approvalRequired:false,expectedVersionRequired:false,confirmLabel:"실행",id:"",label:""});
const pendingTarget=computed(()=>selected.value?{대상:selectedIdentity.value,상태:String(value(selected.value,["status","executionStatus","enabled"])||"-")}:{대상:"Scheduler"});

onMounted(load);watch(mode,()=>{page.value=0;selected.value=null;detail.value={};void load();});
async function load(){loading.value=true;failure.value=null;try{
 let result:any;
 if(mode.value==="executions")result=await fetchExecutionPage({page:page.value,size:size.value,query:filters.query,jobId:filters.jobId,status:filters.status,sort:"start_time",direction:"desc"});
 else if(mode.value==="jobs")result=await fetchJobPage({page:page.value,size:size.value,query:filters.query,sort:"job_id",direction:"asc"});
 else if(mode.value==="scheduler")result=await fetchSchedulePage({page:page.value,size:size.value,query:filters.query,sort:"next_fire_time",direction:"asc"});
 else if(mode.value==="overview"){result=await fetchBatchOverview();overview.value=result;rows.value=(result.recentExecutions as Row[])||[];total.value=rows.value.length;applyEnvelope(result);return;}
 else if(["topology","instances","workerPools","agents"].includes(mode.value)){const data=await fetchInfrastructure();result=data;rows.value=mode.value==="instances"?data.instances:mode.value==="workerPools"?data.workers:mode.value==="agents"?data.instances:[...data.instances,...data.workers,...data.targets];total.value=rows.value.length;applyEnvelope(data);return;}
 else if(mode.value==="centerCut"){rows.value=(await fetchCenterCutJobs()).map(row=>({...row,recordType:"CENTER_CUT_JOB"}));total.value=rows.value.length;applyEnvelope({});return;}
 else if(mode.value==="alerts"){const data=await fetchBatchAlertsWorkspace();result=data;rows.value=[...data.unknownResults.map(row=>({...row,recordType:"UNKNOWN_RESULT"})),...data.dlq.map(row=>({...row,recordType:"DLQ"})),...data.outbox.map(row=>({...row,recordType:"OUTBOX"})),...data.operations.map(row=>({...row,recordType:"OPERATION"}))];total.value=rows.value.length;applyEnvelope(data);return;}
 else if(mode.value==="audit"){const data=await fetchBatchAuditWorkspace();result=data;rows.value=[...data.auditLogs.map(row=>({...row,recordType:"AUDIT"})),...data.deliveries.map(row=>({...row,recordType:"DELIVERY"})),...data.operations.map(row=>({...row,recordType:"BATCH_OPERATION"}))];total.value=rows.value.length;applyEnvelope(data);return;}
 else if(["recovery","leases"].includes(mode.value)){const data=await fetchRecovery();result=data;rows.value=mode.value==="recovery"?[...data.ghostCandidates.map(row=>({...row,recordType:"GHOST"})),...data.unknownResults.map(row=>({...row,recordType:"UNKNOWN_RESULT"}))]:data.locks;total.value=rows.value.length;applyEnvelope(data);return;}
 else {const view=mode.value==="deployment"?"deployments":mode.value;result=await fetchBatchView(view);rows.value=result.items||[];total.value=rows.value.length;applyEnvelope(result);return;}
 applyPage(result as AdmPage<Row>);
 }catch(error){failure.value=classifyAdmFailure(error);rows.value=[];total.value=0;}finally{loading.value=false;}}
function applyPage(result:AdmPage<Row>){rows.value=result.items||[];page.value=result.page||0;size.value=result.size||size.value;total.value=result.total||0;hasNext.value=Boolean(result.hasNext);applyEnvelope(result);}
function applyEnvelope(result:any){fetchedAt.value=String(result.fetchedAt||new Date().toISOString());partial.value=Boolean(result.partial);stale.value=Boolean(result.stale);}
async function selectRow(row:Row){selected.value=row;detailTab.value="요약";detailLoading.value=true;detailFailure.value=null;try{if(mode.value==="executions")detail.value=await fetchExecutionWorkspace(String(value(row,["executionId","execution_id"])));else if(mode.value==="jobs")detail.value=await fetchJobWorkspace(String(value(row,["jobId","job_id"])));else if(mode.value==="centerCut")detail.value=await fetchCenterCutWorkspace(String(value(row,["centerCutJobId","center_cut_job_id","jobId","job_id"])));else if(mode.value==="scheduler"){const scheduleId=String(value(row,["scheduleId","schedule_id"]));detail.value={schedule:row,simulation:await simulateSchedule(scheduleId,new Date().toISOString().slice(0,10),30)};}else detail.value={...row};}catch(error){detailFailure.value=classifyAdmFailure(error);detail.value={};}finally{detailLoading.value=false;}}
function applySearch(){page.value=0;void load();}function resetSearch(){filters.query="";filters.jobId="";filters.status="";page.value=0;void load();}function changePage(next:number){page.value=Math.max(0,next);selected.value=null;void load();}
function prepareAction(id:string){pendingAction.value=id;}function prepareGlobalAction(id:string){selected.value=null;pendingAction.value=id;}
async function executeAction(command:{reason:string;approvalId:string;expectedVersion?:number;idempotencyKey:string}){if(!pendingAction.value)return;actionSubmitting.value=true;failure.value=null;try{const id=selectedIdentity.value;switch(pendingAction.value){case"retry":await retryExecution(id,command);break;case"stop":await stopExecution(id,command);break;case"run":await runJob(id,command);break;case"schedule-enable":await setScheduleEnabled(id,true,command);break;case"schedule-disable":await setScheduleEnabled(id,false,command);break;case"scheduler-run":await runSchedulerOnce(command);break;case"release-lock":await releaseLock({...command,lockKey:id});break;case"ghost-fail":await actGhostExecution(id,{...command,actionType:"FAIL"});break;case"ghost-abandon":await actGhostExecution(id,{...command,actionType:"ABANDON"});break;case"unknown-resolve":await resolveUnknownResult(id,"CONFIRMED_FAILURE",command);break;}pendingAction.value=null;await load();}catch(error){failure.value=classifyAdmFailure(error);}finally{actionSubmitting.value=false;}}
function value(row:Row,keys:string[]):unknown{for(const key of keys)if(row&&row[key]!==undefined&&row[key]!==null)return row[key];return "";}function displayValue(row:Row,keys:string[],mask=false){const v=value(row,keys);return mask?maskOperationalValue(v):formatObject(v);}function formatObject(v:unknown):string{if(v===undefined||v===null||v==="")return "-";if(typeof v==="object")return JSON.stringify(v);return String(v);}function rowKey(row:Row,index:number){return String(value(row,["executionId","execution_id","unknownId","unknown_id","centerCutJobId","center_cut_job_id","jobId","job_id","scheduleId","schedule_id","instanceId","instance_id","workerId","worker_id","lockKey","lock_key","messageId","message_id","id"])||index);}function statusClass(v:unknown){const s=String(v||"").toUpperCase();return ["COMPLETED","SUCCESS","UP","ACTIVE","ENABLED","AVAILABLE","APPLIED"].includes(s)?"success":["RUNNING","STARTED","PARTIAL","STALE","RECOVERING","STOPPING"].includes(s)?"warning":"danger";}function formatDate(v:string){try{return new Intl.DateTimeFormat("ko-KR",{dateStyle:"short",timeStyle:"medium"}).format(new Date(v));}catch{return v;}}
</script>

<style scoped>
.workbench-layout{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(22rem,.75fr);gap:1rem}.result-card,.detail-card{min-width:0}.table-wrap{overflow:auto;max-height:38rem}.table-wrap tr{cursor:pointer}.table-wrap tr.selected{outline:2px solid currentColor;outline-offset:-2px}.pagination{display:flex;align-items:center;justify-content:center;gap:.75rem;padding-top:1rem}.detail-grid{display:grid;grid-template-columns:minmax(8rem,.35fr) minmax(0,1fr);gap:.35rem 1rem}.detail-grid dt{font-weight:700}.detail-grid dd{margin:0;overflow-wrap:anywhere}.timeline{list-style:none;padding:0;display:grid;gap:.75rem}.timeline li{border-left:3px solid #718096;padding-left:.75rem}.timeline li small,.timeline li p{display:block;margin:.25rem 0}.detail-json{max-height:28rem;overflow:auto;white-space:pre-wrap;overflow-wrap:anywhere}.danger-zone{border-top:1px solid #d8dee8;margin-top:1rem;padding-top:1rem}.freshness{font-size:.85rem}.cpf-operation-state+.workbench-layout{margin-top:1rem}@media(max-width:1100px){.workbench-layout{grid-template-columns:1fr}.detail-card{min-height:20rem}}@media(max-width:720px){.cpf-toolbar{display:grid}.detail-grid{grid-template-columns:1fr}.detail-grid dd{margin-bottom:.6rem}}
</style>
