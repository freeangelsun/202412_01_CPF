<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">GATEWAY CONTROL PLANE / {{ activeTab.toUpperCase() }}</p><h2>{{ activeModeTitle }}</h2><p>{{ activeModeDescription }}</p></div>
      <div class="heading-actions">
        <span class="refresh-state" :class="{live:autoRefreshEnabled}">{{ autoRefreshEnabled ? 'EVENT POLL 15s' : 'PAUSED' }} · {{ lastRefreshedLabel }}</span>
        <button class="ghost" @click="toggleAutoRefresh">{{ autoRefreshEnabled ? '실시간 중지' : '실시간 시작' }}</button>
        <button class="ghost" @click="loadAll"><CpfIcon name="refresh"/> 새로고침</button>
      </div>
    </div>

    <div class="cpf-alert" :class="capability.available ? 'success' : 'warning'" role="status">
      <strong>{{ capability.status || 'UNKNOWN' }}</strong>
      <span>{{ capability.available ? 'Gateway Control Plane 연결됨' : (capability.reason || 'Gateway Capability를 사용할 수 없습니다.') }}</span>
      <small>Source: {{ capability.sourceInstanceId || '-' }} · Generated: {{ capability.generatedAt || '-' }}</small>
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
      <button v-for="tab in tabs" :key="tab.id" :class="{active:activeTab===tab.id}" :aria-current="activeTab===tab.id ? 'page' : undefined" @click="selectTab(tab.id)">{{ tab.label }}</button>
    </nav>


    <section v-if="activeTab==='dashboard'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Gateway 운영 대시보드</h2><p>환경·Route·Server Group·Apply ACK·Connection Test를 동일 Context에서 요약합니다.</p></div></div>
      <div class="cpf-grid-2"><div><h3>현재 필터</h3><pre>{{ filters }}</pre></div><div><h3>운영 경고</h3><pre>{{ operations.warnings || [] }}</pre></div></div>
    </section>

    <section v-if="activeTab==='servers'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Gateway 연동 서버</h2><p>Service Registry 기반 Endpoint와 Group 연결 상태를 조회합니다.</p></div></div>
      <div class="table-wrap"><table><thead><tr><th>환경</th><th>Service</th><th>Endpoint</th><th>Protocol</th><th>Group</th><th>Member</th><th>상태</th></tr></thead><tbody>
        <tr v-for="row in groups" :key="`server-${row.serverGroupId}`"><td>{{row.environmentCode}}</td><td>{{row.serviceId}}</td><td>{{row.endpointCode}}</td><td>{{row.targetProtocol}}</td><td>{{row.groupName}}</td><td>{{row.memberCount}}</td><td><span class="cpf-status" :class="statusClass(row.status)">{{row.status}}</span></td></tr>
        <tr v-if="!groups.length"><td colspan="7">등록된 연동 서버가 없습니다.</td></tr>
      </tbody></table></div>
    </section>

    <section v-if="activeTab==='health'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Gateway Health · 연결시험</h2><p>Binding별 Apply ACK, Drift와 최근 연결시험 결과를 분리해 표시합니다.</p></div></div>
      <div class="cpf-grid-2"><div><h3>Apply 상태</h3><pre>{{ applyStatuses }}</pre></div><div><h3>연결시험</h3><pre>{{ tests }}</pre></div></div>
      <div v-if="connectionTestOperation" class="operation-panel">
        <strong>Operation {{ connectionTestOperation.operationId }}</strong>
        <span class="cpf-status" :class="statusClass(connectionTestOperation.status)">{{ connectionTestOperation.status }}</span>
        <span>Version {{ connectionTestOperation.version }}</span>
        <button class="ghost" @click="refreshConnectionTestOperation">상태 확인</button>
        <button class="danger ghost" :disabled="!canCancelConnectionTest" @click="cancelConnectionTest">취소</button>
        <button class="ghost" :disabled="!canRevalidateConnectionTest" @click="revalidateConnectionTest">재검증</button>
      </div>
    </section>

    <section v-if="activeTab==='transactions'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Gateway 거래 조회</h2><p>Transaction ID·Correlation ID를 유지한 최근 Gateway 실행을 표시합니다.</p></div></div>
      <div class="cpf-toolbar"><input v-model.trim="transactionId" placeholder="Transaction ID"><button class="primary" :disabled="!transactionId" @click="traceGatewayTransaction">거래 추적</button></div>
      <pre>{{ transactionTrace || operations.recentTransactions || operations.transactions || [] }}</pre>
    </section>

    <section v-if="activeTab==='log-policies'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Gateway 로그 정책</h2><p>Masking·Sampling·Retention·Payload 기록 정책의 Runtime 적용 상태를 표시합니다.</p></div><button class="ghost" @click="loadLogPolicies">정책 새로고침</button></div>
      <div class="cpf-grid-2"><div><h3>정책</h3><pre>{{ logPolicies }}</pre></div><div><h3>배포 상태</h3><pre>{{ logPolicyDistribution }}</pre></div></div>
    </section>

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
        <div class="wide actions"><button class="primary" type="submit">저장</button><button v-if="groupForm.version>0" type="button" class="danger ghost" @click="deleteGroup">폐기 요청</button><button type="button" class="ghost" @click="groupForm=null">취소</button></div>
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
        <label>Ingress Path Pattern<input v-model.trim="bindingForm.pathPattern" required></label>
        <label>Target Path Template<input v-model.trim="bindingForm.targetPath" required placeholder="/internal/{id} 또는 /target/**"></label>
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
        <div class="wide actions"><button class="primary" type="submit">Draft 저장</button><button v-if="bindingForm.version>0" type="button" class="ghost" @click="validateBinding">검증 전환</button><button v-if="bindingForm.version>0" type="button" class="danger ghost" @click="deleteBinding">폐기 요청</button><button type="button" class="ghost" @click="bindingForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='apply'" class="cpf-grid-2">
      <article class="cpf-card"><div class="cpf-card-head"><h2>Gateway Instance 적용 상태</h2></div><div class="table-wrap"><table><thead><tr><th>Instance</th><th>Expected</th><th>Applied</th><th>Status</th><th>Last Seen</th></tr></thead><tbody><tr v-for="r in applyStatuses" :key="r.gatewayInstanceId"><td>{{r.gatewayInstanceId}}</td><td>{{r.expectedVersion}}</td><td>{{r.appliedVersion}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.lastSeenAt}}</td></tr></tbody></table></div></article>
      <article class="cpf-card"><div class="cpf-card-head"><div><h2>연결시험</h2><p>Target 직접 시험과 Gateway 경유 E2E를 비동기 Operation으로 실행합니다.</p></div><button class="primary" :disabled="!selectedBindingId||testSubmitting" @click="requestConnectionTest">{{testSubmitting?'요청 중':'연결시험 실행'}}</button></div>
      <div class="test-controls"><select v-model="testType" :disabled="!capability.available"><option v-for="type in connectionTestTypes" :key="type" :value="type">{{ type }}</option></select><input v-model.trim="testReason" minlength="5" placeholder="연결시험 사유" :disabled="!capability.available"></div>
      <div class="table-wrap"><table><thead><tr><th>Type</th><th>Gateway / Target</th><th>Status</th><th>Failure Stage</th><th>Duration</th><th>Trace</th></tr></thead><tbody><tr v-for="r in tests" :key="r.testId"><td>{{r.testType}}</td><td>{{r.gatewayInstanceId}} / {{r.instanceId}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.failureStage||'-'}}</td><td>{{r.durationMs}} ms</td><td>{{r.traceId}}</td></tr></tbody></table></div></article>
    </section>

    <section v-if="activeTab==='security'" class="cpf-card"><div class="cpf-card-head"><h2>보안 · 제한 원칙</h2></div><div class="cpf-grid-2"><div><h3>Default Deny</h3><p>Service Registry 등록만으로 외부 공개하지 않습니다. ACTIVE Binding과 Gateway ACK가 모두 있어야 합니다.</p></div><div><h3>Retry Safety</h3><p>Retry는 멱등 Route에서만 허용하며 Connect·Response·Overall Timeout을 분리합니다.</p></div><div><h3>관리 API 보호</h3><p>ADM, BAT, Actuator와 Internal Endpoint는 기본 공개 대상에서 제외합니다.</p></div><div><h3>변경 통제</h3><p>운영 활성·차단·삭제는 사유, 승인 ID, CAS Version과 감사 이력을 요구합니다.</p></div></div></section>

    <p v-if="errorMessage" class="cpf-error" role="alert">{{errorMessage}}</p>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import { admMutation, admQuery } from "../../shared/cpfApi";
import CpfIcon from "../../components/CpfIcon.vue";

type AnyRow=Record<string,any>;
export default defineComponent({
  name:"GatewayOperationsPage",
  components:{CpfIcon},
  props:{
    initialMode:{
      type:String,
      default:"dashboard",
      validator:(value:string)=>["dashboard","servers","groups","bindings","security","health","transactions","log-policies","apply"].includes(value)
    }
  },
  data(){return{
    tabs:[
      {id:"dashboard",label:"대시보드"},{id:"servers",label:"연동 서버"},{id:"groups",label:"서버 그룹"},
      {id:"bindings",label:"경로·라우팅"},{id:"security",label:"보안·제한"},{id:"health",label:"Health·연결시험"},
      {id:"transactions",label:"거래 조회"},{id:"log-policies",label:"로그 정책"},{id:"apply",label:"적용 상태·이력"}
    ],
    activeTab:String(this.initialMode||"dashboard"),filters:{environmentCode:"",serviceId:"",routeId:""},groups:[] as AnyRow[],bindings:[] as AnyRow[],
    applyStatuses:[] as AnyRow[],tests:[] as AnyRow[],selectedBindingId:"",groupForm:null as AnyRow|null,bindingForm:null as AnyRow|null,
    groupMembers:[] as AnyRow[],originalGroupMembers:[] as AnyRow[],testType:"NETWORK",testReason:"Gateway 연결 상태 검증",testSubmitting:false,errorMessage:"",protocols:[] as string[],
    autoRefreshEnabled:true,autoRefreshTimer:null as ReturnType<typeof setInterval>|null,lastRefreshedAt:null as Date|null,refreshInFlight:false,
    loadBalances:[] as string[],connectionTestTypes:[] as string[],capability:{} as AnyRow,operations:{} as AnyRow,eventSource:null as EventSource|null,
    lastEventId:"",streamConnected:false,eventReloadTimer:null as ReturnType<typeof setTimeout>|null,
    connectionTestOperation:null as AnyRow|null,transactionId:"",transactionTrace:null as AnyRow|null,
    logPolicies:[] as AnyRow[],logPolicyDistribution:{} as AnyRow
  }},
  computed:{
    activeModeTitle():string{const tab=this.tabs.find((item:AnyRow)=>item.id===this.activeTab);return tab?`Gateway ${tab.label}`:"Gateway 운영"},
    activeModeDescription():string{const descriptions:Record<string,string>={dashboard:"Gateway 전체 운영 상태와 위험 신호를 요약합니다.",servers:"연동 Endpoint와 Service Registry 연결을 관리합니다.",groups:"Server Group과 Member·LB·Failover를 관리합니다.",bindings:"Route Binding과 Default Deny 정책을 관리합니다.",security:"TLS·인증·인가·Rate Limit 원칙을 확인합니다.",health:"Health·연결시험과 장애 진단을 수행합니다.",transactions:"Gateway 거래와 Correlation Context를 조회합니다.","log-policies":"Masking·Sampling·Retention 정책을 조회합니다.",apply:"Gateway 적용 ACK·Drift·이력을 추적합니다."};return descriptions[this.activeTab]||"Gateway 운영 기능"},
    activeBindings():number{return this.bindings.filter((r:AnyRow)=>r.status==="ACTIVE").length},
    driftCount():number{return this.applyStatuses.filter((r:AnyRow)=>!["APPLIED","OK"].includes(String(r.status).toUpperCase())||r.expectedVersion!==r.appliedVersion).length},
    failedTests():number{return this.tests.filter((r:AnyRow)=>!["PASS","SUCCESS","PENDING","RUNNING","REQUESTED"].includes(String(r.status).toUpperCase())).length},
    appliedGatewayCount():number{return this.applyStatuses.filter((r:AnyRow)=>["APPLIED","OK"].includes(String(r.status).toUpperCase())&&String(r.expectedVersion)===String(r.appliedVersion)).length},
    staleGatewayCount():number{return this.applyStatuses.filter((r:AnyRow)=>["STALE","DOWN","UNKNOWN","DRIFT"].includes(String(r.status).toUpperCase())||String(r.expectedVersion)!==String(r.appliedVersion)).length},
    averageTestLatency():number{const values=this.tests.map((r:AnyRow)=>Number(r.durationMs??r.latencyMs??r.elapsedMs)).filter((v:number)=>Number.isFinite(v)&&v>=0);return values.length?Math.round(values.reduce((a:number,b:number)=>a+b,0)/values.length):0},
    pendingTests():number{return this.tests.filter((r:AnyRow)=>["PENDING","RUNNING","REQUESTED"].includes(String(r.status).toUpperCase())).length},
    canCancelConnectionTest():boolean{return Boolean(this.connectionTestOperation&&["REQUESTED","PENDING","RUNNING"].includes(String(this.connectionTestOperation.status||"").toUpperCase()))},
    canRevalidateConnectionTest():boolean{return Boolean(this.connectionTestOperation&&["COMPLETED","FAILED","CANCELLED","EXPIRED"].includes(String(this.connectionTestOperation.status||"").toUpperCase()))},
    lastRefreshedLabel():string{return this.lastRefreshedAt?this.lastRefreshedAt.toLocaleTimeString("ko-KR",{hour12:false}):"갱신 전"},
    bindingPreview():string{const f=this.bindingForm||{};return `${f.environmentCode||'-'} ${f.hostPattern||'*'}${f.pathPattern||'/'} → ${f.serverGroupId||'미선택'} / ${f.targetProtocol||'-'} / retry=${f.maxRetryCount||0}`},
    memberDiffPreview():string{const before=new Map(this.originalGroupMembers.map((m:AnyRow)=>[m.instanceId,JSON.stringify({weight:m.weight,priority:m.priority,canaryPercent:m.canaryPercent||0,enabled:m.enabled})]));const after=new Map(this.groupMembers.map((m:AnyRow)=>[m.instanceId,JSON.stringify({weight:m.weight,priority:m.priority,canaryPercent:m.canaryPercent||0,enabled:m.enabled})]));const added=[...after.keys()].filter(k=>!before.has(k));const removed=[...before.keys()].filter(k=>!after.has(k));const changed=[...after.keys()].filter(k=>before.has(k)&&before.get(k)!==after.get(k));return `추가 ${added.length} / 변경 ${changed.length} / 제거 ${removed.length} | ${[...added,...changed,...removed].join(', ')||'변경 없음'}`}
  },
  watch:{
    initialMode:{immediate:true,handler(value:string){if(value)this.activeTab=value}},
    "$route.query.view"(value:unknown){if(typeof value==="string"&&this.tabs.some((tab:AnyRow)=>tab.id===value))this.activeTab=value}
  },
  mounted(){const view=String(this.$route.query.view||"");if(view&&this.tabs.some((tab:AnyRow)=>tab.id===view))this.activeTab=view;this.initializeOperations()},
  beforeUnmount(){this.stopAutoRefresh();this.disconnectOperationsStream();if(this.eventReloadTimer)clearTimeout(this.eventReloadTimer)},
  methods:{
    selectTab(tabId:string){if(!this.tabs.some((tab:AnyRow)=>tab.id===tabId))return;this.activeTab=tabId;void this.$router.replace({query:{...this.$route.query,view:tabId}})},
    async initializeOperations(){await this.loadCapability();await this.loadAll();this.connectOperationsStream();this.startAutoRefresh()},
    async loadCapability(){try{const c=await admQuery("/adm/api/gateway-registry/capability");this.capability=c||{};const catalog=c?.catalog||{};this.protocols=Array.isArray(catalog.protocols)?catalog.protocols:[];this.loadBalances=Array.isArray(catalog.loadBalancePolicies)?catalog.loadBalancePolicies:[];this.connectionTestTypes=Array.isArray(catalog.connectionTestTypes)?catalog.connectionTestTypes:[];if(this.connectionTestTypes.length&&!this.connectionTestTypes.includes(this.testType))this.testType=this.connectionTestTypes[0]}catch(e:any){this.capability={installed:false,available:false,status:"UNAVAILABLE",reason:e?.message||"Gateway Capability 조회 실패"};this.errorMessage=this.capability.reason}},
    async loadOperations(){try{this.operations=await admQuery("/adm/api/gateway-registry/operations/snapshot")||{};this.lastEventId=this.operations.lastEventId||this.lastEventId}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 KPI 조회 실패"}},
    connectOperationsStream(){this.disconnectOperationsStream();if(!this.autoRefreshEnabled)return;void this.loadOperationEvents()},
    async loadOperationEvents(){try{const events=await admQuery<AnyRow[]>("/adm/api/gateway-registry/operations/events",{afterEventId:this.lastEventId||undefined,limit:100});if(events.length){this.lastEventId=String(events.at(-1)?.eventId||this.lastEventId);this.scheduleEventReload()}}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 Event 조회 실패"}},
    disconnectOperationsStream(){if(this.eventSource){this.eventSource.close();this.eventSource=null}this.streamConnected=false},
    scheduleEventReload(){if(this.eventReloadTimer)clearTimeout(this.eventReloadTimer);this.eventReloadTimer=setTimeout(()=>{this.loadOperations();if(!this.groupForm&&!this.bindingForm)this.loadAll()},300)},
    async loadAll(){if(this.refreshInFlight)return;this.refreshInFlight=true;this.errorMessage="";try{const p=new URLSearchParams();if(this.filters.environmentCode)p.set("environmentCode",this.filters.environmentCode);if(this.filters.serviceId)p.set("serviceId",this.filters.serviceId);p.set("limit","200");const b=new URLSearchParams();if(this.filters.environmentCode)b.set("environmentCode",this.filters.environmentCode);if(this.filters.routeId)b.set("routeId",this.filters.routeId);b.set("limit","200");[this.groups,this.bindings,this.operations]=await Promise.all([admQuery(`/adm/api/gateway-registry/server-groups?${p}`),admQuery(`/adm/api/gateway-registry/bindings?${b}`),admQuery("/adm/api/gateway-registry/operations/snapshot")]);this.lastEventId=this.operations?.lastEventId||this.lastEventId;if(this.selectedBindingId)await this.loadBindingDetails(this.selectedBindingId);await this.loadLogPolicies();this.lastRefreshedAt=new Date()}catch(e:any){this.errorMessage=e?.message||"Gateway 운영 정보를 불러오지 못했습니다."}finally{this.refreshInFlight=false}},
    startAutoRefresh(){this.stopAutoRefresh();if(!this.autoRefreshEnabled)return;this.autoRefreshTimer=setInterval(()=>{if(document.visibilityState==="visible"&&!this.groupForm&&!this.bindingForm){void this.loadOperationEvents();void this.loadAll()}},15000)},
    stopAutoRefresh(){if(this.autoRefreshTimer){clearInterval(this.autoRefreshTimer);this.autoRefreshTimer=null}},
    toggleAutoRefresh(){this.autoRefreshEnabled=!this.autoRefreshEnabled;if(this.autoRefreshEnabled){this.startAutoRefresh();this.connectOperationsStream()}else{this.stopAutoRefresh();this.disconnectOperationsStream()}},
    async loadBindingDetails(id:string){this.selectedBindingId=id;const [a,t]=await Promise.all([admQuery(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/apply-status?limit=200`),admQuery(`/adm/api/gateway-registry/bindings/${encodeURIComponent(id)}/connection-tests?limit=200`)]);this.applyStatuses=a||[];this.tests=t||[]},
    startGroup(){this.groupForm={serverGroupId:"",groupName:"",environmentCode:"DEV",serviceId:"",endpointCode:"",targetProtocol:"HTTP",loadBalancePolicy:"ROUND_ROBIN",hashKeySource:"",healthPolicyId:"",failoverGroupId:"",directAllowed:false,version:0,reason:"Gateway 서버 그룹 등록"};this.groupMembers=[];this.originalGroupMembers=[];this.addMember();this.activeTab="groups"},
    async selectGroup(r:AnyRow){this.groupForm={...r,reason:"Gateway 서버 그룹 변경"};const members=await admQuery(`/adm/api/gateway-registry/server-groups/${encodeURIComponent(r.serverGroupId)}/members`);this.groupMembers=(members||[]).map((m:AnyRow)=>({...m,canaryPercent:Number(m.canaryPercent||0),enabled:Boolean(m.enabled)}));this.originalGroupMembers=this.groupMembers.map((m:AnyRow)=>({...m}))},
    addMember(){this.groupMembers.push({instanceId:"",weight:100,priority:this.groupMembers.length,canaryPercent:0,enabled:true,effectiveStatus:"UNKNOWN",fencingToken:0})},
    retireMember(index:number){this.groupMembers.splice(index,1)},
    async saveGroup(){try{const f=this.groupForm!;const ids=this.groupMembers.map((m:AnyRow)=>String(m.instanceId||"").trim());if(ids.some((id:string)=>!id)||new Set(ids).size!==ids.length)throw new Error("Member Instance ID는 필수이며 중복될 수 없습니다.");const members=this.groupMembers.map((m:AnyRow)=>({instanceId:String(m.instanceId).trim(),weight:Number(m.weight),priority:Number(m.priority),canaryPercent:Number(m.canaryPercent||0),enabled:Boolean(m.enabled)}));await admMutation("/adm/api/gateway-registry/server-groups","POST",{operationId:crypto.randomUUID(),serverGroupId:f.serverGroupId,groupName:f.groupName,environmentCode:f.environmentCode,serviceId:f.serviceId,endpointCode:f.endpointCode,targetProtocol:f.targetProtocol,loadBalancePolicy:f.loadBalancePolicy,hashKeySource:f.hashKeySource,healthPolicyId:f.healthPolicyId,failoverGroupId:f.failoverGroupId,directAllowed:f.directAllowed,members,expectedVersion:f.version||0,reason:f.reason});this.groupForm=null;this.groupMembers=[];this.originalGroupMembers=[];await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Server Group 저장 실패"}},
    startBinding(){if(!this.capability.available){this.errorMessage="Gateway Control Plane Capability를 사용할 수 없습니다.";return}this.bindingForm={bindingId:"",routeId:"",environmentCode:"DEV",hostPattern:"*",pathPattern:"/api/**",targetPath:"/internal/**",httpMethod:"*",apiVersion:"v1",routeVersion:"1",serviceId:"",serverGroupId:"",ingressProtocol:"HTTPS",targetProtocol:"HTTP",connectTimeoutMs:3000,responseTimeoutMs:10000,overallTimeoutMs:15000,maxRetryCount:0,idempotent:false,gatewayAllowed:false,directAllowed:false,tlsPolicyId:"",authenticationPolicyId:"",authorizationPolicyId:"",headerPolicyId:"",rateLimitPolicyId:"",healthPolicyId:"",failoverGroupId:"",version:0,reason:"Gateway Binding Draft 등록"};this.activeTab="bindings"},
    selectBinding(r:AnyRow){this.selectedBindingId=r.bindingId;this.bindingForm={...r,hostPattern:r.hostPattern||"*",pathPattern:r.pathPattern||"/api/**",targetPath:r.targetPath||r.pathPattern||"/api/**",httpMethod:r.httpMethod||"*",apiVersion:r.apiVersion||"v1",ingressProtocol:r.ingressProtocol||"HTTPS",targetProtocol:r.targetProtocol||"HTTP",connectTimeoutMs:r.connectTimeoutMs||3000,responseTimeoutMs:r.responseTimeoutMs||10000,overallTimeoutMs:r.overallTimeoutMs||15000,maxRetryCount:r.maxRetryCount||0,reason:"Gateway Binding 변경"};this.loadBindingDetails(r.bindingId)},
    async saveBinding(){try{const f=this.bindingForm!;const route={standardExecutionId:f.routeId,serviceId:f.serviceId,httpMethod:f.httpMethod,endpoint:f.targetPath,operationId:f.routeId,requiredPermission:"GATEWAY_ROUTE_ACCESS",auditReasonRequired:true,routeVersion:f.routeVersion,routeId:f.routeId,environmentCode:f.environmentCode,hostPattern:f.hostPattern,pathPattern:f.pathPattern,apiVersion:f.apiVersion,serverGroupId:f.serverGroupId,ingressProtocol:f.ingressProtocol,targetProtocol:f.targetProtocol,tlsPolicyId:f.tlsPolicyId,authenticationPolicyId:f.authenticationPolicyId,authorizationPolicyId:f.authorizationPolicyId,headerPolicyId:f.headerPolicyId,rateLimitPolicyId:f.rateLimitPolicyId,healthPolicyId:f.healthPolicyId,connectTimeoutMs:f.connectTimeoutMs,responseTimeoutMs:f.responseTimeoutMs,overallTimeoutMs:f.overallTimeoutMs,maxRetryCount:f.maxRetryCount,idempotent:f.idempotent,failoverGroupId:f.failoverGroupId,enabled:false,expectedVersion:f.version||0};await admMutation("/adm/api/gateway-registry/bindings","POST",{operationId:crypto.randomUUID(),bindingId:f.bindingId,route,serverGroupId:f.serverGroupId,gatewayAllowed:f.gatewayAllowed,directAllowed:f.directAllowed,approvalId:"",effectiveFrom:null,effectiveTo:null,expectedVersion:f.version||0,reason:f.reason});this.bindingForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Gateway Binding 저장 실패"}},
    async requestConnectionTest(){if(!this.capability.available){this.errorMessage="Gateway Control Plane Capability를 사용할 수 없습니다.";return}if(!this.selectedBindingId||this.testReason.trim().length<5){this.errorMessage="Binding과 5자 이상의 시험 사유가 필요합니다.";return;}this.testSubmitting=true;this.errorMessage="";try{const operationId=crypto.randomUUID();this.connectionTestOperation=await admMutation<AnyRow>(`/adm/api/gateway-registry/bindings/${encodeURIComponent(this.selectedBindingId)}/connection-tests`,"POST",{operationId,bindingId:this.selectedBindingId,testType:this.testType,reason:this.testReason,payloadHash:"",expiresAt:new Date(Date.now()+10*60*1000).toISOString()});await this.loadBindingDetails(this.selectedBindingId)}catch(e:any){this.errorMessage=e?.message||"연결시험 요청 실패"}finally{this.testSubmitting=false}},
    async loadLogPolicies(){try{[this.logPolicies,this.logPolicyDistribution]=await Promise.all([admQuery<AnyRow[]>("/adm/api/log-policies"),admQuery<AnyRow>("/adm/api/log-policies/distribution")])}catch(e:any){this.errorMessage=e?.message||"Gateway 로그 정책 조회 실패"}},
    async traceGatewayTransaction(){if(!this.transactionId)return;try{this.transactionTrace=await admQuery<AnyRow>(`/adm/api/observability/transactions/${encodeURIComponent(this.transactionId)}`,{limit:200})}catch(e:any){this.errorMessage=e?.message||"Gateway 거래 추적 실패"}},
    async validateBinding(){const f=this.bindingForm;if(!f)return;try{await admMutation(`/adm/api/gateway-registry/bindings/${encodeURIComponent(f.bindingId)}/state`,"POST",{operationId:crypto.randomUUID(),bindingId:f.bindingId,targetState:"VALIDATED",expectedVersion:Number(f.version||0),approvalId:"",reason:f.reason});await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Binding 검증 전환 실패"}},
    async deleteGroup(){const f=this.groupForm;if(!f)return;try{await admMutation(`/adm/api/gateway-registry/server-groups/${encodeURIComponent(f.serverGroupId)}`,"DELETE",{operationId:crypto.randomUUID(),expectedVersion:Number(f.version||0),reason:f.reason});this.groupForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Server Group 폐기 요청 실패"}},
    async deleteBinding(){const f=this.bindingForm;if(!f)return;try{await admMutation(`/adm/api/gateway-registry/bindings/${encodeURIComponent(f.bindingId)}`,"DELETE",{operationId:crypto.randomUUID(),expectedVersion:Number(f.version||0),reason:f.reason});this.bindingForm=null;await this.loadAll()}catch(e:any){this.errorMessage=e?.message||"Binding 폐기 요청 실패"}},
    async refreshConnectionTestOperation(){const id=String(this.connectionTestOperation?.operationId||"");if(!id)return;try{this.connectionTestOperation=await admQuery<AnyRow>(`/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(id)}`)}catch(e:any){this.errorMessage=e?.message||"연결시험 Operation 조회 실패"}},
    async cancelConnectionTest(){const current=this.connectionTestOperation;if(!current)return;try{this.connectionTestOperation=await admMutation<AnyRow>(`/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(current.operationId)}/cancel`,"POST",{operationId:current.operationId,expectedVersion:Number(current.version||0),reason:this.testReason});await this.loadBindingDetails(this.selectedBindingId)}catch(e:any){this.errorMessage=e?.message||"연결시험 취소 실패"}},
    async revalidateConnectionTest(){const current=this.connectionTestOperation;if(!current)return;try{this.connectionTestOperation=await admMutation<AnyRow>(`/adm/api/gateway-registry/connection-test-operations/${encodeURIComponent(current.operationId)}/revalidate`,"POST",{sourceOperationId:current.operationId,newOperationId:crypto.randomUUID(),payloadHash:"",expiresAt:new Date(Date.now()+10*60*1000).toISOString(),reason:this.testReason});await this.loadBindingDetails(this.selectedBindingId)}catch(e:any){this.errorMessage=e?.message||"연결시험 재검증 실패"}},
    statusClass(v:string){const s=String(v||"").toUpperCase();return ["UP","ACTIVE","APPLIED","PASS","SUCCESS","APPROVED","AVAILABLE"].includes(s)?"success":["PARTIAL","RECOVERING","STALE","APPROVAL_PENDING"].includes(s)?"warning":"danger"},
    yesNo(v:boolean){return v?"허용":"차단"},
    formatBytes(value:number){const n=Number(value||0);if(n<1024)return `${n} B`;if(n<1024*1024)return `${(n/1024).toFixed(1)} KB`;if(n<1024*1024*1024)return `${(n/1024/1024).toFixed(1)} MB`;return `${(n/1024/1024/1024).toFixed(2)} GB`}
  }
});
</script>
