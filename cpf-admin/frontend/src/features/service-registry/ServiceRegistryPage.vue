<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">SERVICE REGISTRY</p><h2>서비스 레지스트리</h2><p>Service·Endpoint·Instance 원장을 등록하고 Health·Routing·Circuit·호출 이력을 한 화면에서 운영합니다.</p></div>
      <button class="ghost" @click="loadServiceRegistry"><CpfIcon name="refresh"/> 새로고침</button>
    </div>

    <div class="cpf-toolbar">
      <input v-model.trim="serviceRegistrySearch.serviceId" placeholder="Service ID">
      <input v-model.trim="serviceRegistrySearch.endpointCode" placeholder="Endpoint">
      <select v-model="serviceRegistrySearch.instanceStatus"><option value="">전체 상태</option><option v-for="s in capabilities.instanceStatuses" :key="s">{{s}}</option></select>
      <button class="primary" @click="loadServiceRegistry"><CpfIcon name="search"/> 조회</button>
    </div>

    <section class="cpf-kpi-grid">
      <div class="cpf-stat-card"><span class="label">Services</span><strong class="value">{{services.length}}</strong></div>
      <div class="cpf-stat-card"><span class="label">Endpoints</span><strong class="value">{{endpoints.length}}</strong></div>
      <div class="cpf-stat-card"><span class="label">Instances</span><strong class="value">{{instances.length}}</strong></div>
      <div class="cpf-stat-card"><span class="label">Unavailable</span><strong class="value">{{unavailableCount}}</strong></div>
    </section>

    <nav class="cpf-tabs" aria-label="서비스 레지스트리 기능">
      <button v-for="tab in tabs" :key="tab.id" :class="{active:activeTab===tab.id}" @click="activeTab=tab.id">{{tab.label}}</button>
    </nav>

    <section v-if="activeTab==='services'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Service 원장</h2><p>업무 Source에는 Host/IP를 두지 않고 Service ID를 사용합니다.</p></div><button v-if="canWrite('SERVICE_REGISTRY')" class="primary" @click="newService">서비스 등록</button></div>
      <div class="table-wrap"><table><thead><tr><th>Service ID</th><th>서비스명</th><th>유형</th><th>Owner</th><th>상태</th><th>Version</th><th>수정시각</th><th>조치</th></tr></thead><tbody>
      <tr v-for="r in services" :key="r.serviceId" @click="editService(r)"><td>{{r.serviceId}}</td><td>{{r.serviceName}}</td><td>{{r.serviceType}}</td><td>{{r.ownerModuleCode}}</td><td><span class="cpf-status" :class="r.enabled?'success':'danger'">{{r.enabled?'사용':'중지'}}</span></td><td>{{r.version}}</td><td>{{r.updatedAt}}</td><td><button v-if="canDelete('SERVICE_REGISTRY')" class="danger" @click.stop="openDeleteDialog('service',r)">삭제</button></td></tr>
      <tr v-if="!services.length"><td colspan="8">조회 결과가 없습니다.</td></tr></tbody></table></div>
      <form v-if="serviceForm" class="cpf-form-grid" @submit.prevent="saveService">
        <label>Service ID<input v-model.trim="serviceForm.serviceId" :disabled="serviceForm.version>0" required></label>
        <label>서비스명<input v-model.trim="serviceForm.serviceName" required></label>
        <label>유형<select v-model="serviceForm.serviceType"><option v-for="v in capabilities.serviceTypes" :key="v">{{v}}</option></select></label>
        <label>Owner Module<input v-model.trim="serviceForm.ownerModuleCode" required></label>
        <label>사용<select v-model="serviceForm.useYn"><option>Y</option><option>N</option></select></label>
        <label class="wide">설명<textarea v-model.trim="serviceForm.description"></textarea></label>
        <label class="wide">변경 사유<textarea v-model.trim="serviceForm.reason" minlength="5" required></textarea></label>
        <div class="wide actions"><button class="primary" type="submit">저장</button><button type="button" class="ghost" @click="serviceForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='endpoints'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Endpoint</h2><p>Protocol Adapter가 해석하는 서비스 Endpoint 계약입니다.</p></div><button v-if="canWrite('SERVICE_REGISTRY')" class="primary" @click="newEndpoint">Endpoint 등록</button></div>
      <div class="table-wrap"><table><thead><tr><th>Endpoint</th><th>Service</th><th>명칭</th><th>유형</th><th>주소 / Context</th><th>Timeout</th><th>Retry</th><th>Version</th><th>조치</th></tr></thead><tbody>
      <tr v-for="r in endpoints" :key="r.endpointCode" @click="editEndpoint(r)"><td>{{r.endpointCode}}</td><td>{{r.serviceId}}</td><td>{{r.endpointName}}</td><td>{{r.endpointType}}</td><td>{{r.baseUrl}}{{r.contextPath}}</td><td>{{r.defaultTimeoutMs}} ms</td><td>{{r.defaultRetryCount}}</td><td>{{r.version}}</td><td><button v-if="canDelete('SERVICE_REGISTRY')" class="danger" @click.stop="openDeleteDialog('endpoint',r)">삭제</button></td></tr>
      <tr v-if="!endpoints.length"><td colspan="9">조회 결과가 없습니다.</td></tr></tbody></table></div>
      <form v-if="endpointForm" class="cpf-form-grid" @submit.prevent="saveEndpoint">
        <label>Endpoint Code<input v-model.trim="endpointForm.endpointCode" :disabled="endpointForm.version>0" required></label>
        <label>Service<select v-model="endpointForm.serviceId" required><option v-for="r in services" :key="r.serviceId" :value="r.serviceId">{{r.serviceName}} ({{r.serviceId}})</option></select></label>
        <label>명칭<input v-model.trim="endpointForm.endpointName" required></label>
        <label>유형<select v-model="endpointForm.endpointType"><option>HTTP</option><option>HTTPS</option><option>GRPC</option><option>LOCAL</option><option>TCP</option><option>SFTP</option></select></label>
        <label>Base URL<input v-model.trim="endpointForm.baseUrl" placeholder="https://service.internal"></label>
        <label>Context Path<input v-model.trim="endpointForm.contextPath" placeholder="/api"></label>
        <label>Timeout(ms)<input v-model.number="endpointForm.defaultTimeoutMs" type="number" min="1"></label>
        <label>Retry<input v-model.number="endpointForm.defaultRetryCount" type="number" min="0"></label>
        <label>사용<select v-model="endpointForm.useYn"><option>Y</option><option>N</option></select></label>
        <label class="wide">변경 사유<textarea v-model.trim="endpointForm.reason" minlength="5" required></textarea></label>
        <div class="wide actions"><button class="primary" type="submit">저장</button><button type="button" class="ghost" @click="endpointForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='instances'" class="cpf-card">
      <div class="cpf-card-head"><div><h2>Instance · Health</h2><p>점검·Drain·Maintenance와 실제 Health를 분리해 표시합니다.</p></div><button v-if="canWrite('SERVICE_REGISTRY')" class="primary" @click="newInstance">Instance 등록</button></div>
      <div class="table-wrap"><table><thead><tr><th>Service / Endpoint</th><th>Instance</th><th>Host</th><th>환경·Zone</th><th>상태</th><th>Weight/Priority</th><th>Heartbeat</th><th>조치</th></tr></thead><tbody>
      <tr v-for="r in instances" :key="r.instanceId"><td @click="editInstance(r)">{{r.serviceId}} / {{r.endpointCode}}</td><td @click="editInstance(r)">{{r.instanceName}}<small>{{r.instanceId}}</small></td><td>{{r.hostName}}:{{r.port}}</td><td>{{r.environmentCode}} / {{r.zoneCode}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span><small v-if="r.draining">DRAIN</small><small v-if="r.maintenance">MAINTENANCE</small></td><td>{{r.weight}} / {{r.priority}}</td><td>{{r.lastHeartbeatAt||'-'}}</td><td class="actions"><button v-if="canWrite('SERVICE_REGISTRY')&&!r.draining" @click.stop="changeInstanceState(r,'DRAIN')">Drain</button><button v-if="canWrite('SERVICE_REGISTRY')&&r.draining" @click.stop="changeInstanceState(r,'RESUME')">Resume</button><button v-if="canWrite('SERVICE_REGISTRY')&&r.active" @click.stop="changeInstanceState(r,'DISABLE')">Disable</button><button v-if="canDelete('SERVICE_REGISTRY')" class="danger" @click.stop="openDeleteDialog('instance',r)">삭제</button></td></tr>
      <tr v-if="!instances.length"><td colspan="8">조회 결과가 없습니다.</td></tr></tbody></table></div>
      <form v-if="instanceForm" class="cpf-form-grid" @submit.prevent="saveInstance">
        <label>Instance ID<input v-model.trim="instanceForm.instanceId" :disabled="instanceForm.version>0" required></label>
        <label>Service<select v-model="instanceForm.serviceId"><option v-for="r in services" :key="r.serviceId" :value="r.serviceId">{{r.serviceId}}</option></select></label>
        <label>Endpoint<select v-model="instanceForm.endpointCode"><option v-for="r in endpoints.filter((e:any)=>!instanceForm.serviceId||e.serviceId===instanceForm.serviceId)" :key="r.endpointCode" :value="r.endpointCode">{{r.endpointCode}}</option></select></label>
        <label>Instance Name<input v-model.trim="instanceForm.instanceName"></label>
        <label>Base URL<input v-model.trim="instanceForm.baseUrl"></label>
        <label>Host<input v-model.trim="instanceForm.hostName" required></label>
        <label>Port<input v-model.number="instanceForm.portNo" type="number" min="1" max="65535"></label>
        <label>환경<select v-model="instanceForm.environmentCode"><option v-for="v in capabilities.environments" :key="v">{{v}}</option></select></label>
        <label>Zone<input v-model.trim="instanceForm.zoneCode"></label><label>Cell<input v-model.trim="instanceForm.cellCode"></label>
        <label>Weight<input v-model.number="instanceForm.weight" type="number" min="1"></label><label>Priority<input v-model.number="instanceForm.priorityNo" type="number" min="0"></label>
        <label>활성<select v-model="instanceForm.activeYn"><option>Y</option><option>N</option></select></label>
        <label class="wide">변경 사유<textarea v-model.trim="instanceForm.reason" minlength="5" required></textarea></label>
        <div class="wide actions"><button class="primary" type="submit">저장</button><button type="button" class="ghost" @click="instanceForm=null">취소</button></div>
      </form>
    </section>

    <section v-if="activeTab==='runtime'" class="cpf-grid-2">
      <article class="cpf-card"><div class="cpf-card-head"><h2>Health History</h2></div><div class="table-wrap"><table><thead><tr><th>Instance</th><th>Status</th><th>Protocol</th><th>Latency</th><th>Failure</th><th>Checked</th></tr></thead><tbody><tr v-for="r in health" :key="r.healthId"><td>{{r.instanceId}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.protocolStatus||'-'}}</td><td>{{r.responseTimeMs??'-'}} ms</td><td>{{r.failureMessage||'-'}}</td><td>{{r.checkedAt}}</td></tr></tbody></table></div></article>
      <article class="cpf-card"><div class="cpf-card-head"><h2>Routing · Circuit</h2></div><div class="table-wrap"><table><thead><tr><th>Service/Endpoint</th><th>Routing</th><th>LB</th><th>Failover</th><th>State</th><th>Failures</th></tr></thead><tbody><tr v-for="r in routingPolicies" :key="r.policyId"><td>{{r.serviceId}}/{{r.endpointCode}}</td><td>{{r.routingMode}}</td><td>{{r.loadBalanceType}}</td><td>{{r.failoverEnabled?'Y':'N'}}</td><td>{{r.active?'ACTIVE':'INACTIVE'}}</td><td>-</td></tr><tr v-for="r in circuits" :key="`${r.serviceId}-${r.endpointCode}-${r.instanceId}`"><td>{{r.serviceId}}/{{r.endpointCode}}</td><td>{{r.instanceId}}</td><td>-</td><td>-</td><td><span class="cpf-status" :class="statusClass(r.state)">{{r.state}}</span></td><td>{{r.failureCount}}</td></tr></tbody></table></div></article>
      <article class="cpf-card wide"><div class="cpf-card-head"><h2>최근 Service Call</h2></div><div class="table-wrap"><table><thead><tr><th>Transaction / Trace</th><th>Service/Endpoint/Instance</th><th>Method/Path</th><th>Status</th><th>Duration</th><th>Retry</th><th>Failure</th><th>Time</th></tr></thead><tbody><tr v-for="r in callHistory" :key="r.callId"><td>{{r.transactionId}}<small>{{r.traceId}}</small></td><td>{{r.serviceId}}/{{r.endpointCode}}/{{r.instanceId}}</td><td>{{r.method}} {{r.requestPath}}</td><td><span class="cpf-status" :class="statusClass(r.status)">{{r.status}}</span></td><td>{{r.durationMs}} ms</td><td>{{r.retryCount}}</td><td>{{r.failureCode}} {{r.failureMessage}}</td><td>{{r.createdAt}}</td></tr></tbody></table></div></article>
    </section>

    <div v-if="stateDialog.open" class="dialog-backdrop" @keydown.esc="closeStateDialog"><section class="action-dialog" role="dialog" aria-modal="true" aria-labelledby="state-dialog-title"><h3 id="state-dialog-title">{{stateDialog.command}} 운영 조치</h3><dl><dt>대상</dt><dd>{{stateDialog.instance?.serviceId}} / {{stateDialog.instance?.endpointCode}} / {{stateDialog.instance?.instanceId}}</dd><dt>현재 상태</dt><dd>{{stateDialog.instance?.status}}</dd><dt>영향</dt><dd>해당 Instance는 Routing 후보에서 제외되거나 다시 편입됩니다.</dd></dl><label>사유<textarea v-model.trim="stateDialog.reason" minlength="5" autofocus></textarea></label><p v-if="stateDialog.error" class="cpf-error">{{stateDialog.error}}</p><div class="actions"><button class="primary" @click="confirmInstanceState">실행</button><button class="ghost" @click="closeStateDialog">취소</button></div></section></div>
    <div v-if="deleteDialog.open" class="dialog-backdrop" @keydown.esc="closeDeleteDialog"><section class="action-dialog" role="dialog" aria-modal="true" aria-labelledby="registry-delete-title"><h3 id="registry-delete-title">Service Registry 삭제</h3><dl><dt>대상 유형</dt><dd>{{deleteDialog.kind}}</dd><dt>대상 ID</dt><dd>{{deleteDialog.targetId}}</dd><dt>Version</dt><dd>{{deleteDialog.version}}</dd><dt>영향</dt><dd>연결된 Endpoint·Instance·Routing Consumer에 영향을 줄 수 있으며 충돌 시 서버가 409로 거부합니다.</dd></dl><label>삭제 사유<textarea v-model.trim="deleteDialog.reason" minlength="5" autofocus></textarea></label><p v-if="deleteDialog.error" class="cpf-error" role="alert">{{deleteDialog.error}}</p><div class="actions"><button class="danger" :disabled="deleteDialog.busy" @click="confirmDelete">{{deleteDialog.busy?'처리 중':'삭제 실행'}}</button><button class="ghost" :disabled="deleteDialog.busy" @click="closeDeleteDialog">취소</button></div></section></div>

    <p v-if="localError" class="cpf-error" role="alert">{{localError}}</p>
  </div>
</template>
<script lang="ts">
import{defineComponent}from"vue";import{useAdmConsolePage}from"../../app/useAdmConsolePage";import CpfIcon from"../../components/CpfIcon.vue";import{admInvokeOperation}from"../../shared/cpfApi";import{operationForRegistryTarget,validateRegistryDeleteRequest,type ServiceRegistryTargetKind}from"./serviceRegistryWorkflow";
type R=Record<string,any>;
export default defineComponent({setup(){return useAdmConsolePage()},name:"ServiceRegistryPage",components:{CpfIcon},data(){return{tabs:[{id:"services",label:"Service"},{id:"endpoints",label:"Endpoint"},{id:"instances",label:"Instance·Health"},{id:"runtime",label:"Routing·Circuit·호출"}],activeTab:"services",serviceForm:null as R|null,endpointForm:null as R|null,instanceForm:null as R|null,localError:"",capabilities:{serviceTypes:[],endpointTypes:[],instanceStatuses:[],instanceCommands:[],environments:[]} as R,stateDialog:{open:false,instance:null as R|null,command:"",reason:"",error:"",busy:false},deleteDialog:{open:false,kind:"service" as ServiceRegistryTargetKind,target:null as R|null,targetId:"",version:0,reason:"",error:"",busy:false}}},computed:{services():R[]{return this.serviceRegistryResult?.services||[]},endpoints():R[]{return this.serviceRegistryResult?.endpoints||[]},instances():R[]{return this.serviceRegistryResult?.instances||[]},health():R[]{return this.serviceRegistryResult?.health||[]},routingPolicies():R[]{return this.serviceRegistryResult?.routingPolicies||[]},circuits():R[]{return this.serviceRegistryResult?.circuits||[]},callHistory():R[]{return this.serviceRegistryResult?.callHistory||[]},unavailableCount():number{return this.instances.filter((r:R)=>!["UP","DEGRADED","RECOVERING"].includes(String(r.status).toUpperCase())).length}},mounted(){this.loadCapabilities();this.loadServiceRegistry()},methods:{
 statusClass(v:string){const s=String(v||"").toUpperCase();return["UP","ACTIVE","SUCCESS","CLOSED"].includes(s)?"success":["DEGRADED","RECOVERING","STALE","HALF_OPEN"].includes(s)?"warning":"danger"},op(){return crypto.randomUUID()},operator(){return this.currentOperator.operatorId},
 newService(){this.serviceForm={serviceId:"",serviceName:"",serviceType:"INTERNAL",ownerModuleCode:"",description:"",useYn:"Y",version:0,reason:"서비스 레지스트리 등록"};this.activeTab="services"},editService(r:R){if(!this.canWrite('SERVICE_REGISTRY'))return;this.serviceForm={...r,useYn:r.enabled?'Y':'N',reason:"서비스 레지스트리 변경"}},async saveService(){try{const f=this.serviceForm!;await admInvokeOperation("admServiceRegistrySaveService",{body:{operationId:this.op(),serviceId:f.serviceId,serviceName:f.serviceName,serviceType:f.serviceType,ownerModuleCode:f.ownerModuleCode,description:f.description,useYn:f.useYn,expectedVersion:f.version||0,reason:f.reason}});this.serviceForm=null;await this.loadServiceRegistry()}catch(e:any){this.localError=e?.message||"Service 저장 실패"}},
 newEndpoint(){this.endpointForm={endpointCode:"",serviceId:this.serviceRegistrySearch.serviceId||"",endpointName:"",endpointType:"HTTP",baseUrl:"",contextPath:"",defaultTimeoutMs:10000,defaultRetryCount:0,useYn:"Y",version:0,reason:"서비스 Endpoint 등록"};this.activeTab="endpoints"},editEndpoint(r:R){if(!this.canWrite('SERVICE_REGISTRY'))return;this.endpointForm={...r,useYn:r.enabled?'Y':'N',reason:"서비스 Endpoint 변경"}},async saveEndpoint(){try{const f=this.endpointForm!;await admInvokeOperation("admServiceRegistrySaveEndpoint",{body:{operationId:this.op(),endpointCode:f.endpointCode,serviceId:f.serviceId,endpointName:f.endpointName,endpointType:f.endpointType,baseUrl:f.baseUrl,contextPath:f.contextPath,defaultTimeoutMs:f.defaultTimeoutMs,defaultRetryCount:f.defaultRetryCount,useYn:f.useYn,expectedVersion:f.version||0,reason:f.reason}});this.endpointForm=null;await this.loadServiceRegistry()}catch(e:any){this.localError=e?.message||"Endpoint 저장 실패"}},
 newInstance(){this.instanceForm={instanceId:"",serviceId:this.serviceRegistrySearch.serviceId||"",endpointCode:this.serviceRegistrySearch.endpointCode||"",instanceName:"",baseUrl:"",hostName:"",portNo:8080,environmentCode:"DEV",zoneCode:"",cellCode:"",weight:100,priorityNo:0,activeYn:"Y",maintenanceYn:"N",drainYn:"N",version:0,reason:"서비스 Instance 등록"};this.activeTab="instances"},editInstance(r:R){if(!this.canWrite('SERVICE_REGISTRY'))return;this.instanceForm={...r,portNo:r.port,priorityNo:r.priority,activeYn:r.active?'Y':'N',maintenanceYn:r.maintenance?'Y':'N',drainYn:r.draining?'Y':'N',reason:"서비스 Instance 변경"}},async saveInstance(){try{const f=this.instanceForm!;await admInvokeOperation("admServiceRegistrySaveInstance",{body:{operationId:this.op(),instanceId:f.instanceId,serviceId:f.serviceId,endpointCode:f.endpointCode,instanceName:f.instanceName,baseUrl:f.baseUrl,hostName:f.hostName,portNo:f.portNo,environmentCode:f.environmentCode,zoneCode:f.zoneCode,cellCode:f.cellCode,weight:f.weight,priorityNo:f.priorityNo,activeYn:f.activeYn,maintenanceYn:f.maintenanceYn,drainYn:f.drainYn,expectedVersion:f.version||0,reason:f.reason}});this.instanceForm=null;await this.loadServiceRegistry()}catch(e:any){this.localError=e?.message||"Instance 저장 실패"}},
 async loadCapabilities(){try{this.capabilities=await this.getJson("/adm/api/service-registry/capabilities")}catch(e:any){this.localError=e?.message||"Service Registry Capability 조회 실패"}},
 openStateDialog(r:R,command:string){this.changeInstanceState(r,command)},
 async changeInstanceState(r:R,command:string){if(!this.canWrite('SERVICE_REGISTRY')){this.localError="Service Registry 변경 권한이 필요합니다.";return}this.stateDialog={open:true,instance:r,command,reason:"",error:"",busy:false}},
 closeStateDialog(){if(this.stateDialog.busy)return;this.stateDialog={open:false,instance:null,command:"",reason:"",error:"",busy:false}},
 async confirmInstanceState(){const r=this.stateDialog.instance;if(!r||this.stateDialog.reason.trim().length<5){this.stateDialog.error="5자 이상의 조치 사유가 필요합니다.";return}this.stateDialog.busy=true;this.stateDialog.error="";try{await admInvokeOperation("admServiceRegistryChangeInstanceState",{path:{serviceId:r.serviceId,endpointCode:r.endpointCode,instanceId:r.instanceId},body:{operationId:this.op(),command:this.stateDialog.command,expectedVersion:r.version,reason:this.stateDialog.reason}});this.stateDialog.busy=false;this.closeStateDialog();await this.loadServiceRegistry()}catch(e:any){this.stateDialog.busy=false;this.stateDialog.error=e?.message||"Instance 상태 변경 실패"}},
 openDeleteDialog(kind:ServiceRegistryTargetKind,target:R){if(!this.canDelete('SERVICE_REGISTRY')){this.localError="Service Registry 삭제 권한이 필요합니다.";return}const targetId=kind==='service'?target.serviceId:kind==='endpoint'?target.endpointCode:target.instanceId;this.deleteDialog={open:true,kind,target,targetId:String(targetId||''),version:Number(target.version??0),reason:"",error:"",busy:false}},
 closeDeleteDialog(){if(this.deleteDialog.busy)return;this.deleteDialog={open:false,kind:"service",target:null,targetId:"",version:0,reason:"",error:"",busy:false}},
 async confirmDelete(){const validation=validateRegistryDeleteRequest({kind:this.deleteDialog.kind,targetId:this.deleteDialog.targetId,expectedVersion:this.deleteDialog.version,reason:this.deleteDialog.reason});if(!validation.ok){this.deleteDialog.error=validation.message;return}this.deleteDialog.busy=true;this.deleteDialog.error="";try{const operation=operationForRegistryTarget(this.deleteDialog.kind);const path=this.deleteDialog.kind==='service'?{serviceId:this.deleteDialog.targetId}:this.deleteDialog.kind==='endpoint'?{endpointCode:this.deleteDialog.targetId}:{instanceId:this.deleteDialog.targetId};await admInvokeOperation(operation,{path,body:{operationId:this.op(),expectedVersion:this.deleteDialog.version,reason:this.deleteDialog.reason}});this.deleteDialog.busy=false;this.closeDeleteDialog();await this.loadServiceRegistry()}catch(e:any){this.deleteDialog.busy=false;this.deleteDialog.error=e?.message||"Service Registry 삭제 실패"}}
}});
</script>
