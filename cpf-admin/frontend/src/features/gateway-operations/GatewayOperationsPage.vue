<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">GATEWAY CONTROL PLANE</p><h2>게이트웨이 관리</h2><p>연동 서버 등록부터 Server Group, Route Binding, 연결시험, 적용 ACK와 Drift까지 한 흐름으로 관리합니다.</p></div>
      <div class="heading-actions">
        <span class="refresh-state" :class="{live:autoRefreshEnabled}">{{ autoRefreshEnabled ? (streamConnected ? 'LIVE SSE' : 'POLL 15s') : 'PAUSED' }} · {{ lastRefreshedLabel }}</span>
        <button class="ghost" @click="toggleAutoRefresh">{{ autoRefreshEnabled ? '실시간 중지' : '실시간 시작' }}</button>
        <button class="ghost" @click="loadAll"><CpfIcon name="refresh"/> 새로고침</button>
      </div>
    </div>

    <div class="cpf-toolbar">
      <select v-model="filters.environmentCode"><option value="">전체 환경</option><option>DEV</option><option>TEST</option><option>PROD</option></select>
      <input v-model.trim="filters.serviceId" placeholder="Service ID">
      <input v-model.trim="filters.routeId" placeholder="Route ID">
      <button class="primary" @click="loadAll"><CpfIcon name="search"/> 조회</button>
    </div>

    <section class="cpf-kpi-grid">
      <div class="cpf-stat-card"><span class="label">TPS (60s)</span><strong class="value">{{ Number(operations.tps||0).toFixed(2) }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Success / Error</span><strong class="value">{{ Number(operations.successRate||0).toFixed(1) }}% / {{ Number(operations.errorRate||0).toFixed(1) }}%</strong></div>
      <div class="cpf-stat-card"><span class="label">P95 / P99</span><strong class="value">{{ operations.p95DurationMs||0 }} / {{ operations.p99DurationMs||0 }} ms</strong></div>
      <div class="cpf-stat-card"><span class="label">Drift</span><strong class="value">{{ operations.driftCount||0 }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Open Circuit</span><strong class="value">{{ operations.openCircuitCount||0 }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Cert ≤ 30d</span><strong class="value">{{ operations.expiringCertificateCount||0 }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Spool Backlog</span><strong class="value">{{ operations.spoolBacklogCount||0 }} / {{ formatBytes(operations.spoolBacklogBytes||0) }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Failed Tests (24h)</span><strong class="value">{{ operations.failedConnectionTestCount||0 }}</strong></div>
    </section>
    <div v-if="operations.warnings?.length" class="cpf-alert warning">운영 조회 일부 제한: {{ operations.warnings.join(', ') }}</div>

    <nav class="cpf-tabs" aria-label="Gateway 관리 기능">
      <button v-for="tab in tabs" :key="tab.id" :class="{active:activeTab===tab.id}" @click="activeTab=tab.id">{{ tab.label }}</button>
    </nav>

    <section v-if="activeTab==='groups'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>연동 서버 · Server Group</h2><p>Service Registry Instance를 중복 저장하지 않고 Group Member로 연결합니다.</p></div><button class="primary" @click="startGroup">새 그룹</button></div>
      <div class="table-wrap"><table><thead><tr><th>환경</th><th>그룹</th><th>서비스 / Endpoint</th><th>Protocol</th><th>LB</th><th>상태</th><th>Member</th><th>Version</th></tr></thead>
      <tbody><tr v-for="row in groups" :key="row.serverGroupId" @click="selectGroup(row)"><td>{{row.environmentCode}}</td><td>{{row.groupName}}<small>{{row.serverGroupId}}</small></td><td>{{row.serviceId}} / {{row.endpointCode}}</td><td>{{row.targetProtocol}}</td><td>{{row.loadBalancePolicy}}</td><td><span class="cpf-status" :class="statusClass(row.status)">{{row.status}}</span></td><td>{{row.memberCount}}</td><td>{{row.version}}</td></tr></tbody></table></div>
      <form v-if="groupForm" class="cpf-form-grid" @submit.prevent="saveGroup">
        <label>Group ID<input v-model.trim="groupForm.serverGroupId" :disabled="groupForm.version>0" required></label>
        <label>그룹명<input v-model.trim="groupForm.groupName" required></label>
        <label>환경<select v-model="groupForm.environmentCode" required><option>DEV</option><option>TEST</option><option>PROD</option></select></label>
        <label>Service ID<input v-model.trim="groupForm.serviceId" required></label>
        <label>Endpoint<input v-model.trim="groupForm.endpointCode" required></label>
        <label>Target Protocol<select v-model="groupForm.targetProtocol"><option v-for="v in protocols" :key="v">{{v}}</option></select></label>
        <label>Load Balance<select v-model="groupForm.loadBalancePolicy"><option v-for="v in loadBalances" :key="v">{{v}}</option></select></label>
        <label v-if="groupForm.loadBalancePolicy==='RENDEZVOUS_HASH'">Hash Key Source<input v-model.trim="groupForm.hashKeySource" placeholder="HEADER:X-Customer-Key"></label>
        <label>Health Policy<input v-model.trim="groupForm.healthPolicyId"></label>
        <label>Failover Group<input v-model.trim="groupForm.failoverGroupId"></label>
        <div class="wide member-editor"><div class="member-head"><strong>Service Registry Member</strong><button type="button" class="ghost" @click="addMember">Member 추가</button></div>
          <table><thead><tr><th>Instance ID</th><th>Weight</th><th>Priority</th><th>Canary %</th><th>Enabled</th><th>Health</th><th>Fencing</th><th></th></tr></thead><tbody>
            <tr v-for="(member,index) in groupMembers" :key="`${member.instanceId}-${index}`">
              <td><input v-model.trim="member.instanceId" required></td><td><input v-model.number="member.weight" type="number" min="1" max="10000"></td>
              <td><input v-model.number="member.priority" type="number" min="0"></td><td><input v-model.number="member.canaryPercent" type="number" min="0" max="100"></td>
              <td><input v-model="member.enabled" type="checkbox"></td><td>{{member.effectiveStatus||'UNKNOWN'}}</td><td>{{member.fencingToken||0}}</td>
              <td><button type="button" class="danger ghost" @click="retireMember(index)">제거</button></td>
            </tr>
          </tbody></table>
          <div class="preview"><strong>Member Diff</strong><span>{{memberDiffPreview}}</span></div>
        </div>
        <label class="wide">변경 사유<textarea v-model.trim="groupForm.reason" minlength="5" required></textarea></label>
        <div class="wide actions"><button class="primary" type="submit">저장</button><button type="button" class="ghost" @click="groupForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='bindings'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>경로 · Routing Binding</h2><p>Default Deny로 Draft를 저장한 뒤 검증·승인·활성화합니다.</p></div><button class="primary" @click="startBinding">새 Binding</button></div>
      <div class="table-wrap"><table><thead><tr><th>환경</th><th>Route</th><th>Server Group</th><th>Version</th><th>Gateway</th><th>Direct</th><th>상태</th><th>Row</th></tr></thead>
      <tbody><tr v-for="row in bindings" :key="row.bindingId" @click="selectBinding(row)"><td>{{row.environmentCode}}</td><td>{{row.routeId}}<small>{{row.bindingId}}</small></td><td>{{row.serverGroupId}}</td><td>{{row.routeVersion}}</td><td>{{yesNo(row.gatewayAllowed)}}</td><td>{{yesNo(row.directAllowed)}}</td><td><span class="cpf-status" :class="statusClass(row.status)">{{row.status}}</span></td><td>{{row.version}}</td></tr></tbody></table></div>
      <form v-if="bindingForm" class="cpf-form-grid" @submit.prevent="saveBinding">
        <label>Binding ID<input v-model.trim="bindingForm.bindingId" :disabled="bindingForm.version>0" required></label>
        <label>Route ID<input v-model.trim="bindingForm.routeId" required></label>
        <label>환경<select v-model="bindingForm.environmentCode"><option>DEV</option><option>TEST</option><option>PROD</option></select></label>
        <label>Host Pattern<input v-model.trim="bindingForm.hostPattern" required></label>
        <label>Path Pattern<input v-model.trim="bindingForm.pathPattern" required></label>
        <label>Method<select v-model="bindingForm.httpMethod"><option>*</option><option>GET</option><option>POST</option><option>PUT</option><option>DELETE</option></select></label>
        <label>API Version<input v-model.trim="bindingForm.apiVersion" required></label>
        <label>Route Version<input v-model.trim="bindingForm.routeVersion" required></label>
        <label>Service ID<input v-model.trim="bindingForm.serviceId" required></label>
        <label>Server Group<select v-model="bindingForm.serverGroupId" required><option v-for="g in groups" :key="g.serverGroupId" :value="g.serverGroupId">{{g.groupName}} ({{g.serverGroupId}})</option></select></label>
        <label>Ingress Protocol<select v-model="bindingForm.ingressProtocol"><option v-for="v in protocols" :key="v">{{v}}</option></select></label>
        <label>Target Protocol<select v-model="bindingForm.targetProtocol"><option v-for="v in protocols" :key="v">{{v}}</option></select></label>
        <label>Connect Timeout<input v-model.number="bindingForm.connectTimeoutMs" type="number" min="1"></label>
        <label>Response Timeout<input v-model.number="bindingForm.responseTimeoutMs" type="number" min="1"></label>
        <label>Overall Timeout<input v-model.number="bindingForm.overallTimeoutMs" type="number" min="1"></label>
        <label>Retry<input v-model.number="bindingForm.maxRetryCount" type="number" min="0"></label>
        <label><input v-model="bindingForm.idempotent" type="checkbox"> 멱등 Route</label>
        <label><input v-model="bindingForm.gatewayAllowed" type="checkbox"> Gateway 공개 허용</label>
        <label><input v-model="bindingForm.directAllowed" type="checkbox"> Direct 호출 허용</label>
        <label>TLS Policy<input v-model.trim="bindingForm.tlsPolicyId"></label>
        <label>Authentication<input v-model.trim="bindingForm.authenticationPolicyId"></label>
        <label>Authorization<input v-model.trim="bindingForm.authorizationPolicyId"></label>
        <label>Header Policy<input v-model.trim="bindingForm.headerPolicyId"></label>
        <label>Rate Limit<input v-model.trim="bindingForm.rateLimitPolicyId"></label>
        <label>Health Policy<input v-model.trim="bindingForm.healthPolicyId"></label>
        <label class="wide">변경 사유<textarea v-model.trim="bindingForm.reason" minlength="5" required></textarea></label>
        <div class="wide preview"><strong>적용 Preview</strong><span>{{bindingPreview}}</span></div>
        <div class="wide actions"><button class="primary" type="submit">Draft 저장</button><button type="button" class="ghost" @click="bindingForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='apply'" class="cpf-grid-2">
      <article class="cpf-card"><div class="cpf-card-head"><h2>Gateway Instance 적용 상태</h2></div><div class="table-wrap"><table><thead><tr><th>Instance</th><th>Expected</th><th>Applied</th><th>Status</th><th>Last Seen</th></tr></thead><tbody><tr v-for="r in applyStatuses" :key="r.gatewayInstanceId"><td>{{r.gatewayInstanceId}}</td><td>{{r.expectedVersion}}</td><td>{{r.appliedVersion}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.lastSeenAt}}</td></tr></tbody></table></div></article>
      <article class="cpf-card"><div class="cpf-card-head"><div><h2>연결시험</h2><p>Target 직접 시험과 Gateway 경유 E2E를 비동기 Operation으로 실행합니다.</p></div><button class="primary" :disabled="!selectedBindingId||testSubmitting" @click="requestConnectionTest">{{testSubmitting?'요청 중':'연결시험 실행'}}</button></div>
      <div class="test-controls"><select v-model="testType"><option value="TARGET_DIRECT">Target 직접 시험</option><option value="GATEWAY_E2E">Gateway 경유 E2E</option><option value="ALL_MEMBERS">전체 Member</option><option value="LOAD_BALANCE">LB 분포 시험</option></select><input v-model.trim="testReason" minlength="5" placeholder="연결시험 사유"></div>
      <div class="table-wrap"><table><thead><tr><th>Type</th><th>Gateway / Target</th><th>Status</th><th>Failure Stage</th><th>Duration</th><th>Trace</th></tr></thead><tbody><tr v-for="r in tests" :key="r.testId"><td>{{r.testType}}</td><td>{{r.gatewayInstanceId}} / {{r.instanceId}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.failureStage||'-'}}</td><td>{{r.durationMs}} ms</td><td>{{r.traceId}}</td></tr></tbody></table></div></article>
    </section>

    <section v-if="activeTab==='security'" class="cpf-card"><div class="cpf-card-head"><h2>보안 · 제한 원칙</h2></div><div class="cpf-grid-2"><div><h3>Default Deny</h3><p>Service Registry 등록만으로 외부 공개하지 않습니다. ACTIVE Binding과 Gateway ACK가 모두 있어야 합니다.</p></div><div><h3>Retry Safety</h3><p>Retry는 멱등 Route에서만 허용하며 Connect·Response·Overall Timeout을 분리합니다.</p></div><div><h3>관리 API 보호</h3><p>ADM, BAT, Actuator와 Internal Endpoint는 기본 공개 대상에서 제외합니다.</p></div><div><h3>변경 통제</h3><p>운영 활성·차단·삭제는 사유, 승인 ID, CAS Version과 감사 이력을 요구합니다.</p></div></div></section>

    <p v-if="errorMessage" class="cpf-error" role="alert">{{errorMessage}}</p>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import {admConsoleMixin} from "../../app/admConsoleMixin";
import CpfIcon from "../../components/CpfIcon.vue";

type AnyRow=Record<string,any>;
export default defineComponent({
  name:"GatewayOperationsPage",components:{CpfIcon},mixins:[admConsoleMixin],
  data(){return{
    tabs:[{id:"groups",label:"연동 서버·그룹"},{id:"bindings",label:"경로·라우팅"},{id:"apply",label:"Health·연결시험·적용"},{id:"security",label:"보안·제한"}],
    activeTab:"groups",filters:{environmentCode:"",serviceId:"",routeId:""},groups:[] as AnyRow[],bindings:[] as AnyRow[],
    applyStatuses:[] as AnyRow[],tests:[] as AnyRow[],selectedBindingId:"",groupForm:null as AnyRow|null,bindingForm:null as AnyRow|null,
    groupMembers:[] as AnyRow[],originalGroupMembers:[] as AnyRow[],testType:"NETWORK",testReason:"Gateway 연결 상태 검증",testSubmitting:false,errorMessage:"",protocols:[] as string[],
    autoRefreshEnabled:true,autoRefreshTimer:null as ReturnType<typeof setInterval>|null,lastRefreshedAt:null as Date|null,refreshInFlight:false,
    loadBalances:[] as string[],connectionTestTypes:[] as string[],operations:{} as AnyRow,eventSource:null as EventSource|null,
    lastEventId:"",streamConnected:false,eventReloadTimer:null as ReturnType<typeof setTimeout>|null
  }},
  computed:{
    activeBindings():number{return this.bindings.filter((r:AnyRow)=>r.status==="ACTIVE").length},
    driftCount():number{return this.applyStatuses.filter((r:AnyRow)=>!["APPLIED","OK"].includes(String(r.status).toUpperCase())||r.expectedVersion!==r.appliedVersion).length},
    failedTests():number{return this.tests.filter((r:AnyRow)=>!["PASS","SUCCESS","PENDING","RUNNING","REQUESTED"].includes(String(r.status).toUpperCase())).length},
    appliedGatewayCount():number{return this.applyStatuses.filter((r:AnyRow)=>["APPLIED","OK"].includes(String(r.status).toUpperCase())&&String(r.expectedVersion)===String(r.appliedVersion)).length},
    staleGatewayCount():number{return this.applyStatuses.filter((r:AnyRow)=>["STALE","DOWN","UNKNOWN","DRIFT"].includes(String(r.status).toUpperCase())||String(r.expectedVersion)!==String(r.appliedVersion)).length},
    averageTestLatency():number{const values=this.tests.map((r:AnyRow)=>Number(r.durationMs??r.latencyMs??r.elapsedMs)).filter((v:number)=>Number.isFinite(v)&&v>=0);return values.length?Math.round(values.reduce((a:number,b:number)=>a+b,0)/values.length):0},
    pendingTests():number{return this.tests.filter((r:AnyRow)=>["PENDING","RUNNING","REQUESTED"].includes(String(r.status).toUpperCase())).length},
    lastRefreshedLabel():string{return this.lastRefreshedAt?this.lastRefreshedAt.toLocaleTimeString("ko-KR",{hour12:false}):"갱신 전"},
    bindingPreview():string{const f=this.bindingForm||{};return `${f.environmentCode||'-'} ${f.hostPattern||'*'}${f.pathPattern||'/'} → ${f.serverGroupId||'미선택'} / ${f.targetProtocol||'-'} / retry=${f.maxRetryCount||0}`},
    memberDiffPreview():string{const before=new Map(this.originalGroupMembers.map((m:AnyRow)=>[m.instanceId,JSON.stringify({weight:m.weight,priority:m.priority,canaryPercent:m.canaryPercent||0,enabled:m.enabled})]));const after=new Map(this.groupMembers.map((m:AnyRow)=>[m.instanceId,JSON.stringify({weight:m.weight,priority:m.priority,canaryPercent:m.canaryPercent||0,enabled:m.enabled})]));const added=[...after.keys()].filter(k=>!before.has(k));const removed=[...before.keys()].filter(k=>!after.has(k));const changed=[...after.keys()].filter(k=>before.has(k)&&before.get(k)!==after.get(k));return `추가 ${added.length} / 변경 ${changed.length} / 제거 ${removed.length} | ${[...added,...changed,...removed].join(', ')||'변경 없음'}`}
  },
  mounted(){this.initializeOperations()},
  beforeUnmount(){this.stopAutoRefresh();this.disconnectOperationsStream();if(this.eventReloadTimer)clearTimeout(this.eventReloadTimer)},
  methods:{
    async initializeOperations(){await this.loadCapability();await this.loadAll();this.connectOperationsStream();this.startAutoRefresh()},
    async loadCapability(){try{const c=await this.getJson("/adm/api/gateway-registry/capability");const catalog=c?.catalog||{};this.protocols=Array.isArray(catalog.protocols)?catalog.protocols:[];this.loadBalances=Array.isArray(catalog.loadBalancePolicies)?catalog.loadBalancePolicies:[];this.connectionTestTypes=Array.isArray(catalog.connectionTestTypes)?catalog.connectionTestTypes:[];if(this.connectionTestTypes.length&&!this.connectionTestTypes.includes(this.testType))this.testType=this.connectionTestTypes[0]}catch(e:any){this.errorMessage=e?.message||"Gateway Capability 조회 실패"}},
    async loadOperations(){try{this.operations=await this.getJson("/adm/api/gateway-registry/operations/snapshot")||{};this.lastEventId=this.operations.lastEventId||this.lastEventId}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 KPI 조회 실패"}},
    connectOperationsStream(){this.disconnectOperationsStream();if(!this.autoRefreshEnabled||typeof EventSource==="undefined")return;const q=this.lastEventId?`?afterEventId=${encodeURIComponent(this.lastEventId)}`:"";const source=new EventSource(`/adm/api/gateway-registry/operations/stream${q}`);this.eventSource=source;source.onopen=()=>{this.streamConnected=true};source.onerror=()=>{this.streamConnected=false;source.close();if(this.autoRefreshEnabled)setTimeout(()=>this.connectOperationsStream(),5000)};const reload=(event:MessageEvent)=>{this.lastEventId=(event as any).lastEventId||this.lastEventId;this.scheduleEventReload()};source.onmessage=reload;["BINDING_CHANGED","BINDING_STATE_CHANGED","SERVER_GROUP_CHANGED","APPLY_ACK","CONNECTION_TEST_COMPLETED","TRANSACTION_COMPLETED","HEALTH_PROBE_RECORDED","gateway-heartbeat"].forEach(name=>source.addEventListener(name,reload as EventListener))},
    disconnectOperationsStream(){if(this.eventSource){this.eventSource.close();this.eventSource=null}this.streamConnected=false},
    scheduleEventReload(){if(this.eventReloadTimer)clearTimeout(this.eventReloadTimer);this.eventReloadTimer=setTimeout(()=>{this.loadOperations();if(!this.groupForm&&!this.bindingForm)this.loadAll()},300)},
    async loadAll(){if(this.refreshInFlight)return;this.refreshInFlight=true;this.errorMessage="";try{const p=new URLSearchParams();if(this.filters.environmentCode)p.set("environmentCode",this.filters.environmentCode);if(this.filters.serviceId)p.set("serviceId",this.filters.serviceId);p.set("limit","200");const b=new URLSearchParams();if(this.filters.environmentCode)b.set("environmentCode",this.filters.environmentCode);if(this.filters.routeId)b.set("routeId",this.filters.routeId);b.set("limit","200");[this.groups,this.bindings,this.operations]=await Promise.all([this.getJson(`/adm/api/gateway-registry/server-groups?${p}`),this.getJson(`/adm/api/gateway-registry/bindings?${b}`),this.getJson("/adm/api/gateway-registry/operations/snapshot")]);this.lastEventId=this.operations?.lastEventId||this.lastEventId;if(this.selectedBindingId)await this.loadBindingDetails(this.selectedBindingId);this.lastRefreshedAt=new Date()}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 정보를 불러오지 못했습니다."}finally{this.refreshInFlight=false}},
    startAutoRefresh(){this.stopAutoRefresh();if(!this.autoRefreshEnabled)return;this.autoRefreshTimer=setInterval(()=>{if(document.visibilityState==="visible"&&!this.groupForm&&!this.bindingForm&&!this.streamConnected)this.loadAll()},15000)},
    stopAutoRefresh(){if(this.autoRefreshTimer){clearInterval(this.autoRefreshTimer);this.autoRefreshTimer=null}},
    toggleAutoRefresh(){this.autoRefreshEnabled=!this.autoRefreshEnabled;if(this.autoRefreshEnabled){this.startAutoRefresh();this.connectOperationsStream()}else{this.stopAutoRefresh();this.disconnectOperationsStream()}},
    async loadBindingDetails(id:string){this.selectedBindingId=id;const [a,t]=await Promise.all([this.getJson(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/apply-status?limit=200`),this.getJson(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/connection-tests?limit=200`)]);this.applyStatuses=a||[];this.tests=t||[]},
    startGroup(){this.groupForm={serverGroupId:"",groupName:"",environmentCode:"DEV",serviceId:"",endpointCode:"",targetProtocol:"HTTP",loadBalancePolicy:"ROUND_ROBIN",hashKeySource:"",healthPolicyId:"",failoverGroupId:"",directAllowed:false,version:0,reason:"Gateway 서버 그룹 등록"};this.groupMembers=[];this.originalGroupMembers=[];this.addMember();this.activeTab="groups"},
    async selectGroup(r:AnyRow){this.groupForm={...r,reason:"Gateway 서버 그룹 변경"};const members=await this.getJson(`/adm/api/gateway-registry/server-groups/${encodeURIComponent(r.serverGroupId)}/members`);this.groupMembers=(members||[]).map((m:AnyRow)=>({...m,canaryPercent:Number(m.canaryPercent||0),enabled:Boolean(m.enabled)}));this.originalGroupMembers=this.groupMembers.map((m:AnyRow)=>({...m}))},
    addMember(){this.groupMembers.push({instanceId:"",weight:100,priority:this.groupMembers.length,canaryPercent:0,enabled:true,effectiveStatus:"UNKNOWN",fencingToken:0})},
    retireMember(index:number){this.groupMembers.splice(index,1)},
    async saveGroup(){try{const f=this.groupForm!;const ids=this.groupMembers.map((m:AnyRow)=>String(m.instanceId||"").trim());if(ids.some((id:string)=>!id)||new Set(ids).size!==ids.length)throw new Error("Member Instance ID는 필수이며 중복될 수 없습니다.");const members=this.groupMembers.map((m:AnyRow)=>({instanceId:String(m.instanceId).trim(),weight:Number(m.weight),priority:Number(m.priority),canaryPercent:Number(m.canaryPercent||0),enabled:Boolean(m.enabled)}));await this.sendJson("/adm/api/gateway-registry/server-groups","POST",{operationId:crypto.randomUUID(),serverGroupId:f.serverGroupId,groupName:f.groupName,environmentCode:f.environmentCode,serviceId:f.serviceId,endpointCode:f.endpointCode,targetProtocol:f.targetProtocol,loadBalancePolicy:f.loadBalancePolicy,hashKeySource:f.hashKeySource,healthPolicyId:f.healthPolicyId,failoverGroupId:f.failoverGroupId,directAllowed:f.directAllowed,members,expectedVersion:f.version||0,reason:f.reason,requestedBy:undefined});this.groupForm=null;this.groupMembers=[];this.originalGroupMembers=[];await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Server Group 저장 실패"}},
    startBinding(){this.bindingForm={bindingId:"",routeId:"",environmentCode:"DEV",hostPattern:"*",pathPattern:"/api/**",httpMethod:"*",apiVersion:"v1",routeVersion:"1",serviceId:"",serverGroupId:"",ingressProtocol:"HTTPS",targetProtocol:"HTTP",connectTimeoutMs:3000,responseTimeoutMs:10000,overallTimeoutMs:15000,maxRetryCount:0,idempotent:false,gatewayAllowed:false,directAllowed:false,tlsPolicyId:"",authenticationPolicyId:"",authorizationPolicyId:"",headerPolicyId:"",rateLimitPolicyId:"",healthPolicyId:"",failoverGroupId:"",version:0,reason:"Gateway Binding Draft 등록"};this.activeTab="bindings"},
    selectBinding(r:AnyRow){this.selectedBindingId=r.bindingId;this.bindingForm={...r,hostPattern:r.hostPattern||"*",pathPattern:r.pathPattern||"/api/**",httpMethod:r.httpMethod||"*",apiVersion:r.apiVersion||"v1",ingressProtocol:r.ingressProtocol||"HTTPS",targetProtocol:r.targetProtocol||"HTTP",connectTimeoutMs:r.connectTimeoutMs||3000,responseTimeoutMs:r.responseTimeoutMs||10000,overallTimeoutMs:r.overallTimeoutMs||15000,maxRetryCount:r.maxRetryCount||0,reason:"Gateway Binding 변경"};this.loadBindingDetails(r.bindingId)},
    async saveBinding(){try{const f=this.bindingForm!;const route={standardExecutionId:f.routeId,serviceId:f.serviceId,httpMethod:f.httpMethod,endpoint:f.pathPattern,operationId:f.routeId,requiredPermission:"GATEWAY_ROUTE_ACCESS",auditReasonRequired:true,routeVersion:f.routeVersion,routeId:f.routeId,environmentCode:f.environmentCode,hostPattern:f.hostPattern,pathPattern:f.pathPattern,apiVersion:f.apiVersion,serverGroupId:f.serverGroupId,ingressProtocol:f.ingressProtocol,targetProtocol:f.targetProtocol,tlsPolicyId:f.tlsPolicyId,authenticationPolicyId:f.authenticationPolicyId,authorizationPolicyId:f.authorizationPolicyId,headerPolicyId:f.headerPolicyId,rateLimitPolicyId:f.rateLimitPolicyId,healthPolicyId:f.healthPolicyId,connectTimeoutMs:f.connectTimeoutMs,responseTimeoutMs:f.responseTimeoutMs,overallTimeoutMs:f.overallTimeoutMs,maxRetryCount:f.maxRetryCount,idempotent:f.idempotent,failoverGroupId:f.failoverGroupId,enabled:false,expectedVersion:f.version||0};await this.sendJson("/adm/api/gateway-registry/bindings","POST",{operationId:crypto.randomUUID(),bindingId:f.bindingId,route,serverGroupId:f.serverGroupId,gatewayAllowed:f.gatewayAllowed,directAllowed:f.directAllowed,approvalId:"",effectiveFrom:null,effectiveTo:null,expectedVersion:f.version||0,reason:f.reason,requestedBy:undefined});this.bindingForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Gateway Binding 저장 실패"}},
    async requestConnectionTest(){if(!this.selectedBindingId||this.testReason.trim().length<5){this.errorMessage="Binding과 5자 이상의 시험 사유가 필요합니다.";return;}this.testSubmitting=true;this.errorMessage="";try{const operationId=crypto.randomUUID();await this.sendJson(`/adm/api/gateway-registry/bindings/${encodeURIComponent(this.selectedBindingId)}/connection-tests`,"POST",{operationId,bindingId:this.selectedBindingId,testType:this.testType,reason:this.testReason,payloadHash:"",expiresAt:new Date(Date.now()+10*60*1000).toISOString(),requestedBy:undefined});await this.loadBindingDetails(this.selectedBindingId)}catch(e:any){this.errorMessage=e?.message||"연결시험 요청 실패"}finally{this.testSubmitting=false}},
    statusClass(v:string){const s=String(v||"").toUpperCase();return ["UP","ACTIVE","APPLIED","PASS","SUCCESS","APPROVED","AVAILABLE"].includes(s)?"success":["PARTIAL","RECOVERING","STALE","APPROVAL_PENDING"].includes(s)?"warning":"danger"},
    yesNo(v:boolean){return v?"허용":"차단"},
    formatBytes(value:number){const n=Number(value||0);if(n<1024)return `${n} B`;if(n<1024*1024)return `${(n/1024).toFixed(1)} KB`;if(n<1024*1024*1024)return `${(n/1024/1024).toFixed(1)} MB`;return `${(n/1024/1024/1024).toFixed(2)} GB`}
  }
});
</script>
