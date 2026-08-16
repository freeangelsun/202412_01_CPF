<template>
  <section class="panel log-page">
    <div class="panel-title"><div><h2>거래 로그</h2><p class="hint">IN → Gateway → Attempt → OUT → RESULT 구간을 transactionId·traceId로 연계합니다. 민감정보는 서버 마스킹 결과만 표시합니다.</p></div><div class="actions"><button type="button" @click="searchLogs">조회</button><button type="button" :disabled="!logDetail?.item" @click="copyLogDetail">마스킹 상세 복사</button><button type="button" :disabled="!logDetail?.item" @click="downloadLogDetail">감사 상세 저장</button></div></div>
    <div class="filters">
      <label>transactionId <input v-model="logSearch.transactionId"></label><label>traceId <input v-model="logSearch.traceId"></label><label>업무 거래 ID <input v-model="logSearch.businessTransactionId"></label>
      <label>시스템 <input v-model="logSearch.systemCode" placeholder="MBR"></label><label>도메인 <input v-model="logSearch.domainCode" placeholder="MEMBER"></label><label>Application <input v-model="logSearch.application"></label>
      <label>Module <input v-model="logSearch.moduleId"></label><label>WAS <input v-model="logSearch.wasId"></label><label>Instance <input v-model="logSearch.serverInstanceId"></label><label>Host <input v-model="logSearch.hostName"></label>
      <label>Starter <input v-model="logSearch.starterId" placeholder="cpf-starter-cache-redis"></label><label>Capability <input v-model="logSearch.capabilityId" placeholder="CACHE"></label><label>Provider <input v-model="logSearch.provider" placeholder="REDIS"></label><label>Operation <input v-model="logSearch.operation"></label>
      <label>URI <input v-model="logSearch.uri"></label><label>응답 코드 <input v-model="logSearch.responseCode"></label><label>HTTP 상태 <input v-model="logSearch.httpStatus" type="number"></label>
      <label>회원번호 보호검색 <input v-model="logSearch.memberNo"></label><label>고객번호 보호검색 <input v-model="logSearch.customerNo"></label><label>채널 <input v-model="logSearch.channelCode"></label><label>로그 유형 <input v-model="logSearch.logType"></label>
    </div>
    <div class="pager"><span>{{ sortedLogs.length }}건</span><label>쪽 크기 <select v-model.number="logPage.size"><option :value="10">10</option><option :value="20">20</option><option :value="50">50</option></select></label><button type="button" :disabled="logPage.page<=1" @click="moveLogPage(-1)">이전</button><span>{{ logPage.page }} / {{ logTotalPages }}</span><button type="button" :disabled="logPage.page>=logTotalPages" @click="moveLogPage(1)">다음</button></div>
    <div class="table-wrap"><table><thead><tr><th>IDX</th><th>transactionId</th><th>거래명 / URI</th><th>Module / WAS</th><th>Instance / Host</th><th>채널</th><th>HTTP / 응답</th><th>시작</th><th>소요(ms)</th></tr></thead><tbody>
      <tr v-for="item in pagedLogs" :key="item.LOG_IDX||item.logIdx" :class="{selected:selectedLogIdx===(item.LOG_IDX||item.logIdx)}" @click="selectLog(item)"><td>{{ item.LOG_IDX||item.logIdx }}</td><td>{{ item.TRANSACTION_ID||item.transactionId }}</td><td>{{ item.BUSINESS_TRANSACTION_NAME||item.businessTransactionName||'-' }}<small>{{ item.URI||item.uri||'-' }}</small></td><td>{{ item.MODULE_ID||item.moduleId||'-' }} / {{ item.WAS_ID||item.wasId||'-' }}</td><td>{{ item.SERVER_INSTANCE_ID||item.serverInstanceId||'-' }}<small>{{ item.HOST_NAME||item.hostName||'-' }}</small></td><td>{{ item.ORIGINAL_CHANNEL_CODE||item.originalChannelCode||'-' }} / {{ item.CHANNEL_CODE||item.channelCode||'-' }}</td><td>{{ item.HTTP_STATUS||item.httpStatus||'-' }} / {{ item.RESPONSE_CODE||item.responseCode||'-' }}</td><td>{{ item.START_TIME||item.startTime||'-' }}</td><td>{{ item.DURATION_MS??item.durationMs??'-' }}</td></tr>
      <tr v-if="!pagedLogs.length"><td colspan="9">조회된 거래 로그가 없습니다.</td></tr></tbody></table></div>

    <section v-if="detail" class="timeline-section"><h3>거래 구간 Timeline</h3><ol class="timeline">
      <li v-for="segment in segments" :key="segment.name" :class="segment.state"><strong>{{ segment.name }}</strong><span>{{ segment.title }}</span><small>{{ segment.time }}</small><em v-if="segment.failure">{{ segment.failure }}</em></li>
    </ol></section>
    <section v-if="attempts.length" class="attempt-section"><h3>Retry·Failover Attempt</h3><table><thead><tr><th>#</th><th>Target Group</th><th>Target Instance</th><th>Protocol</th><th>Connect</th><th>Response</th><th>상태</th><th>실패 단계</th></tr></thead><tbody><tr v-for="(a,index) in attempts" :key="a.attemptId||index"><td>{{ a.attemptNo||a.attempt_no||index+1 }}</td><td>{{ a.serverGroupId||a.server_group_id||'-' }}</td><td>{{ a.targetInstanceId||a.target_instance_id||'-' }}</td><td>{{ a.protocol||a.targetProtocol||'-' }}</td><td>{{ a.connectDurationMs??a.connect_duration_ms??'-' }}</td><td>{{ a.responseDurationMs??a.response_duration_ms??'-' }}</td><td>{{ a.status||a.resultStatus||'-' }}</td><td>{{ a.failureStage||a.failure_stage||'-' }}</td></tr></tbody></table></section>
    <div class="actions tab-actions"><button v-for="tab in structuredTabs" :key="tab" type="button" :class="{active:logDetailTab===tab}" @click="logDetailTab=tab">{{ tab }}</button></div>
    <StructuredDetails v-if="detail" :title="logDetailTab" :value="activeStructuredDetail" />
    <p v-else class="empty">목록에서 거래를 선택하면 구간·Attempt·마스킹 상세가 표시됩니다.</p>
  </section>

    <section class="panel route-operation-panel">
      <h3>감사 Export Artifact</h3>
      <div class="filters"><label>Export ID <input v-model="operationForm.exportId"></label></div>
      <div class="actions"><button type="button" @click="downloadLogExportArtifact">Artifact 다운로드</button></div>
    </section>
</template>
<script lang="ts">
import {defineComponent} from "vue";import {useAdmConsolePage} from "../../app/useAdmConsolePage";import StructuredDetails from "../../components/StructuredDetails.vue";
export default defineComponent({setup(){return useAdmConsolePage()},name:"LogsPage",components:{StructuredDetails},data(){return{selectedLogIdx:null as any}},computed:{
 detail():Record<string,any>|null{const d=(this as any).logDetail?.item||(this as any).logDetail;return d&&Object.keys(d).length?d:null;},
 attempts():Record<string,any>[] {const d=this.detail||{};const v=d.attempts?.items||d.attempts||d.gatewayAttempts||d.GATEWAY_ATTEMPTS||[];return Array.isArray(v)?v:[];},
 structuredTabs():string[]{return ["요약","수신 헤더","해석 헤더","전파 헤더","응답 헤더","요청","응답","오류","상세","전문"];},
 activeStructuredDetail():any{const d=this.detail||{};const map:Record<string,any>={"요약":d.summary||d,"수신 헤더":d.inboundHeaders||d.headers||d.HEADERS||{},"해석 헤더":d.resolvedHeaders||d.headers||d.HEADERS||{},"전파 헤더":d.outboundHeaders||{},"응답 헤더":d.responseHeaders||{},"요청":d.request||d.REQUEST_BODY||{},"응답":d.response||d.RESPONSE||{},"오류":d.error||d.ERROR_MESSAGE||{},"상세":d.formattedDetails||d.details||[],"전문":(this as any).fixedLengthDetails(d)};return map[(this as any).logDetailTab]||{};},
 segments():Array<Record<string,string>>{const d=this.detail||{};const failure=String(d.failureStage||d.failure_stage||d.error?.stage||"");const status=String(d.status||d.resultStatus||d.responseCode||"");return [
  {name:"IN",title:`${d.channelCode||d.CHANNEL_CODE||"-"} ${d.method||d.httpMethod||""} ${d.uri||d.URI||""}`,time:String(d.receivedAt||d.startTime||d.START_TIME||""),state:failure==="IN"?"failed":"done",failure:failure==="IN"?status:""},
  {name:"GATEWAY",title:`${d.gatewayInstanceId||d.gateway_instance_id||"-"} / ${d.routeId||d.route_id||"-"}`,time:String(d.gatewayDurationMs??d.gateway_duration_ms??""),state:failure==="GATEWAY"?"failed":"done",failure:failure==="GATEWAY"?status:""},
  {name:"OUT",title:`${d.targetServiceId||d.target_service_id||"-"} / ${d.targetInstanceId||d.target_instance_id||"-"}`,time:String(d.targetDurationMs??d.target_duration_ms??""),state:failure==="OUT"?"failed":"done",failure:failure==="OUT"?status:""},
  {name:"RESULT",title:status||"완료",time:String(d.durationMs??d.DURATION_MS??""),state:failure?"failed":"done",failure}
 ];}},methods:{async selectLog(item:any){this.selectedLogIdx=item.LOG_IDX||item.logIdx;await (this as any).loadLogDetail(this.selectedLogIdx);}}});
</script>
<style scoped>
.log-page{display:grid;gap:1rem}.panel-title{display:flex;justify-content:space-between;gap:1rem}.filters{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:.6rem}.filters label{display:grid;gap:.2rem}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;font-size:.84rem}th,td{padding:.55rem;border-bottom:1px solid var(--border-color,#d7dde7);text-align:left;vertical-align:top}tbody tr{cursor:pointer}tbody tr.selected{background:rgba(80,120,220,.1)}td small{display:block;opacity:.7}.timeline{display:grid;grid-template-columns:repeat(4,1fr);gap:.6rem;list-style:none;padding:0}.timeline li{display:grid;gap:.25rem;padding:.8rem;border:1px solid var(--border-color,#d7dde7);border-radius:8px}.timeline li.done{border-color:rgba(46,157,98,.5)}.timeline li.failed{border-color:rgba(200,70,70,.65)}.timeline em{color:#c84646}.tab-actions{flex-wrap:wrap}.empty,.hint{opacity:.72}@media(max-width:760px){.panel-title{flex-direction:column}.timeline{grid-template-columns:1fr}}
</style>
