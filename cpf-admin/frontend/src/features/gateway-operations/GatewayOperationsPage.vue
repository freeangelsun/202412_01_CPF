<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">GATEWAY CONTROL PLANE</p><h2>게이트웨이 관리</h2><p>연동 서버 등록부터 Server Group, Route Binding, 연결시험, 적용 ACK와 Drift까지 한 흐름으로 관리합니다.</p></div>
      <button class="ghost" @click="loadAll"><CpfIcon name="refresh"/> 새로고침</button>
    </div>

    <div class="cpf-toolbar">
      <select v-model="filters.environmentCode"><option value="">전체 환경</option><option>DEV</option><option>TEST</option><option>PROD</option></select>
      <input v-model.trim="filters.serviceId" placeholder="Service ID">
      <input v-model.trim="filters.routeId" placeholder="Route ID">
      <button class="primary" @click="loadAll"><CpfIcon name="search"/> 조회</button>
    </div>

    <section class="cpf-kpi-grid">
      <div class="cpf-stat-card"><span class="label">Server Groups</span><strong class="value">{{ groups.length }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Active Bindings</span><strong class="value">{{ activeBindings }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Partial / Drift</span><strong class="value">{{ driftCount }}</strong></div>
      <div class="cpf-stat-card"><span class="label">Failed Tests</span><strong class="value">{{ failedTests }}</strong></div>
    </section>

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
        <label class="wide">Member Instance IDs<textarea v-model="groupMemberText" placeholder="instance-01,instance-02"></textarea></label>
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
      <article class="cpf-card"><div class="cpf-card-head"><h2>연결시험</h2></div><div class="table-wrap"><table><thead><tr><th>Type</th><th>Gateway / Target</th><th>Status</th><th>Failure Stage</th><th>Duration</th><th>Trace</th></tr></thead><tbody><tr v-for="r in tests" :key="r.testId"><td>{{r.testType}}</td><td>{{r.gatewayInstanceId}} / {{r.instanceId}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.failureStage||'-'}}</td><td>{{r.durationMs}} ms</td><td>{{r.traceId}}</td></tr></tbody></table></div></article>
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
    groupMemberText:"",errorMessage:"",protocols:["HTTP","HTTPS","GRPC","WEBSOCKET","SSE","TCP"],
    loadBalances:["ROUND_ROBIN","WEIGHTED_ROUND_ROBIN","RENDEZVOUS_HASH","PRIORITY_FAILOVER","LEAST_LOAD"]
  }},
  computed:{
    activeBindings():number{return this.bindings.filter((r:AnyRow)=>r.status==="ACTIVE").length},
    driftCount():number{return this.applyStatuses.filter((r:AnyRow)=>!["APPLIED","OK"].includes(String(r.status).toUpperCase())||r.expectedVersion!==r.appliedVersion).length},
    failedTests():number{return this.tests.filter((r:AnyRow)=>!["PASS","SUCCESS"].includes(String(r.status).toUpperCase())).length},
    bindingPreview():string{const f=this.bindingForm||{};return `${f.environmentCode||'-'} ${f.hostPattern||'*'}${f.pathPattern||'/'} → ${f.serverGroupId||'미선택'} / ${f.targetProtocol||'-'} / retry=${f.maxRetryCount||0}`}
  },
  mounted(){this.loadAll()},
  methods:{
    async loadAll(){this.errorMessage="";try{const p=new URLSearchParams();if(this.filters.environmentCode)p.set("environmentCode",this.filters.environmentCode);if(this.filters.serviceId)p.set("serviceId",this.filters.serviceId);p.set("limit","200");const b=new URLSearchParams();if(this.filters.environmentCode)b.set("environmentCode",this.filters.environmentCode);if(this.filters.routeId)b.set("routeId",this.filters.routeId);b.set("limit","200");[this.groups,this.bindings]=await Promise.all([this.getJson(`/adm/api/gateway-registry/server-groups?${p}`),this.getJson(`/adm/api/gateway-registry/bindings?${b}`)]);if(this.selectedBindingId)await this.loadBindingDetails(this.selectedBindingId)}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 정보를 불러오지 못했습니다."}},
    async loadBindingDetails(id:string){this.selectedBindingId=id;const [a,t]=await Promise.all([this.getJson(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/apply-status?limit=200`),this.getJson(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/connection-tests?limit=200`)]);this.applyStatuses=a||[];this.tests=t||[]},
    startGroup(){this.groupForm={serverGroupId:"",groupName:"",environmentCode:"DEV",serviceId:"",endpointCode:"",targetProtocol:"HTTP",loadBalancePolicy:"ROUND_ROBIN",hashKeySource:"",healthPolicyId:"",failoverGroupId:"",directAllowed:false,version:0,reason:"Gateway 서버 그룹 등록"};this.groupMemberText="";this.activeTab="groups"},
    selectGroup(r:AnyRow){this.groupForm={...r,reason:"Gateway 서버 그룹 변경"};this.groupMemberText=""},
    async saveGroup(){try{const f=this.groupForm!;const members=this.groupMemberText.split(",").map((v:string)=>v.trim()).filter(Boolean).map((instanceId:string,i:number)=>({instanceId,weight:100,priority:i,enabled:true}));await this.sendJson("/adm/api/gateway-registry/server-groups","POST",{operationId:crypto.randomUUID(),serverGroupId:f.serverGroupId,groupName:f.groupName,environmentCode:f.environmentCode,serviceId:f.serviceId,endpointCode:f.endpointCode,targetProtocol:f.targetProtocol,loadBalancePolicy:f.loadBalancePolicy,hashKeySource:f.hashKeySource,healthPolicyId:f.healthPolicyId,failoverGroupId:f.failoverGroupId,directAllowed:f.directAllowed,members,expectedVersion:f.version||0,reason:f.reason,requestedBy:this.currentOperator.operatorId});this.groupForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Server Group 저장 실패"}},
    startBinding(){this.bindingForm={bindingId:"",routeId:"",environmentCode:"DEV",hostPattern:"*",pathPattern:"/api/**",httpMethod:"*",apiVersion:"v1",routeVersion:"1",serviceId:"",serverGroupId:"",ingressProtocol:"HTTPS",targetProtocol:"HTTP",connectTimeoutMs:3000,responseTimeoutMs:10000,overallTimeoutMs:15000,maxRetryCount:0,idempotent:false,gatewayAllowed:false,directAllowed:false,tlsPolicyId:"",authenticationPolicyId:"",authorizationPolicyId:"",headerPolicyId:"",rateLimitPolicyId:"",healthPolicyId:"",failoverGroupId:"",version:0,reason:"Gateway Binding Draft 등록"};this.activeTab="bindings"},
    selectBinding(r:AnyRow){this.selectedBindingId=r.bindingId;this.bindingForm={...r,hostPattern:r.hostPattern||"*",pathPattern:r.pathPattern||"/api/**",httpMethod:r.httpMethod||"*",apiVersion:r.apiVersion||"v1",ingressProtocol:r.ingressProtocol||"HTTPS",targetProtocol:r.targetProtocol||"HTTP",connectTimeoutMs:r.connectTimeoutMs||3000,responseTimeoutMs:r.responseTimeoutMs||10000,overallTimeoutMs:r.overallTimeoutMs||15000,maxRetryCount:r.maxRetryCount||0,reason:"Gateway Binding 변경"};this.loadBindingDetails(r.bindingId)},
    async saveBinding(){try{const f=this.bindingForm!;const route={standardExecutionId:f.routeId,serviceId:f.serviceId,httpMethod:f.httpMethod,endpoint:f.pathPattern,operationId:f.routeId,requiredPermission:"GATEWAY_ROUTE_ACCESS",auditReasonRequired:true,routeVersion:f.routeVersion,routeId:f.routeId,environmentCode:f.environmentCode,hostPattern:f.hostPattern,pathPattern:f.pathPattern,apiVersion:f.apiVersion,serverGroupId:f.serverGroupId,ingressProtocol:f.ingressProtocol,targetProtocol:f.targetProtocol,tlsPolicyId:f.tlsPolicyId,authenticationPolicyId:f.authenticationPolicyId,authorizationPolicyId:f.authorizationPolicyId,headerPolicyId:f.headerPolicyId,rateLimitPolicyId:f.rateLimitPolicyId,healthPolicyId:f.healthPolicyId,connectTimeoutMs:f.connectTimeoutMs,responseTimeoutMs:f.responseTimeoutMs,overallTimeoutMs:f.overallTimeoutMs,maxRetryCount:f.maxRetryCount,idempotent:f.idempotent,failoverGroupId:f.failoverGroupId,enabled:false,expectedVersion:f.version||0};await this.sendJson("/adm/api/gateway-registry/bindings","POST",{operationId:crypto.randomUUID(),bindingId:f.bindingId,route,serverGroupId:f.serverGroupId,gatewayAllowed:f.gatewayAllowed,directAllowed:f.directAllowed,approvalId:"",effectiveFrom:null,effectiveTo:null,expectedVersion:f.version||0,reason:f.reason,requestedBy:this.currentOperator.operatorId});this.bindingForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Gateway Binding 저장 실패"}},
    statusClass(v:string){const s=String(v||"").toUpperCase();return ["UP","ACTIVE","APPLIED","PASS","SUCCESS","APPROVED"].includes(s)?"success":["PARTIAL","RECOVERING","STALE","APPROVAL_PENDING"].includes(s)?"warning":"danger"},
    yesNo(v:boolean){return v?"허용":"차단"}
  }
});
</script>
