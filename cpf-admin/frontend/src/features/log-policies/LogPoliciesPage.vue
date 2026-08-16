<template>
  <section class="panel log-policy-page">
    <div class="panel-title">
      <div><h2>로그 정책</h2><p class="muted">필수 Metadata는 유지하고 Query·Header·Body는 Allowlist와 Masking 정책으로 통제합니다.</p></div>
      <div class="actions">
        <button type="button" @click="loadLogPolicies">조회</button>
        <button type="button" @click="loadLogPolicyDistributionStatus">적용 상태</button>
        <button type="button" v-if="canWrite('LOG_POLICY')" @click="saveLogPolicy">저장</button>
        <button type="button" v-if="canWrite('LOG_POLICY')" @click="createLogPolicyOverride">Override 등록</button>
        <label class="inline-field">Override ID <input v-model="logPolicyForm.selectedOverrideId" type="text" inputmode="numeric"></label><button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicyOverride">Override 중지</button>
        <button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicy">정책 중지</button>
      </div>
    </div>

    <div class="policy-grid">
      <fieldset><legend>대상과 저장 정책</legend><div class="filters">
        <label>Policy ID <input v-model.number="logPolicyForm.policyId" type="number"></label>
        <label>Policy Key <input v-model="logPolicyForm.policyKey" type="text"></label>
        <label>정책명 <input v-model="logPolicyForm.policyName" type="text"></label>
        <label>대상 유형 <select v-model="logPolicyForm.targetType"><option>PLATFORM</option><option>ENVIRONMENT</option><option>GATEWAY</option><option>CHANNEL</option><option>SERVICE</option><option>ROUTE</option><option>API</option><option>ONLINE_TRANSACTION</option></select></label>
        <label>대상 ID <input v-model="logPolicyForm.targetId" type="text"></label>
        <label>레벨 <select v-model="logPolicyForm.logLevel"><option>TRACE</option><option>DEBUG</option><option>INFO</option><option>WARN</option><option>ERROR</option></select></label>
        <label>DB 정형 원장 <select v-model="logPolicyForm.dbLogEnabledYn"><option value="Y">사용</option><option value="N">미사용</option></select></label>
        <label>기술 파일 로그 <select v-model="logPolicyForm.fileLogEnabledYn"><option value="Y">사용</option><option value="N">미사용</option></select></label>
        <label>Error Stack <select v-model="logPolicyForm.errorStackCaptureMode"><option>NONE</option><option>SUMMARY</option><option>FULL_MASKED</option></select></label>
        <label>보존일 <input v-model.number="logPolicyForm.retentionDays" min="1" max="3650" type="number"></label>
        <label>Sampling(%) <input v-model.number="logPolicyForm.samplingRate" min="0" max="100" type="number"></label>
        <label>우선순위 <input v-model.number="logPolicyForm.priority" min="1" type="number"></label>
      </div></fieldset>

      <fieldset><legend>민감정보 Capture 안전 상한</legend><div class="filters">
        <label>Query <select v-model="logPolicyForm.queryCaptureMode"><option>NONE</option><option>ALLOWLIST</option><option>MASKED</option><option>HASHED</option></select></label>
        <label>요청 Header <select v-model="logPolicyForm.requestHeaderCaptureMode"><option>NONE</option><option>ALLOWLIST</option><option>MASKED</option></select></label>
        <label>응답 Header <select v-model="logPolicyForm.responseHeaderCaptureMode"><option>NONE</option><option>ALLOWLIST</option><option>MASKED</option></select></label>
        <label>요청 Body <select v-model="logPolicyForm.requestBodyCaptureMode" @change="syncLegacyBodyFlags"><option>NONE</option><option>METADATA_ONLY</option><option>ALLOWLIST_FIELDS</option><option>MASKED_BODY</option><option>ENCRYPTED_BODY</option></select></label>
        <label>응답 Body <select v-model="logPolicyForm.responseBodyCaptureMode" @change="syncLegacyBodyFlags"><option>NONE</option><option>METADATA_ONLY</option><option>ALLOWLIST_FIELDS</option><option>MASKED_BODY</option><option>ENCRYPTED_BODY</option></select></label>
        <label>Query Allowlist <input v-model="logPolicyForm.queryAllowlist" type="text" placeholder="customerId,channel"></label>
        <label>Header Allowlist <input v-model="logPolicyForm.headerAllowlist" type="text" placeholder="content-type,x-cpf-transaction-id"></label>
        <label>본문 Field Allowlist <input v-model="logPolicyForm.fieldAllowlist" type="text" placeholder="customer.name,transaction.amount"></label>
        <label>Masking Policy <input v-model="logPolicyForm.maskingPolicyKey" type="text"></label>
        <label>Query 최대 byte <input v-model.number="logPolicyForm.maxQueryBytes" min="0" max="65536" type="number"></label>
        <label>Header 최대 byte <input v-model.number="logPolicyForm.maxHeaderBytes" min="0" max="131072" type="number"></label>
        <label>요청 Body 최대 byte <input v-model.number="logPolicyForm.maxRequestBodyBytes" min="0" max="1048576" type="number"></label>
        <label>응답 Body 최대 byte <input v-model.number="logPolicyForm.maxResponseBodyBytes" min="0" max="1048576" type="number"></label>
        <label>Stack 최대 byte <input v-model.number="logPolicyForm.maxStackBytes" min="0" max="262144" type="number"></label>
        <label>사유 <input v-model="logPolicyForm.reason" type="text"></label>
      </div><div class="safety-preview" :class="captureRisk.level"><strong>{{ captureRisk.title }}</strong><span>{{ captureRisk.description }}</span><span>Authorization, Cookie, Token 원문과 일반 운영의 FULL_RAW_BODY는 저장할 수 없습니다.</span></div></fieldset>
    </div>

    <div class="actions secondary-actions">
      <button type="button" v-if="canWrite('LOG_POLICY')" @click="createTraceBoost">Trace Boost 등록</button>
      <button type="button" @click="loadTraceBoostRuntimeState">Trace Boost 상태</button>
      <button type="button" @click="loadTraceBoostHistory">Trace Boost 이력</button>
      <button type="button" v-if="canWrite('LOG_POLICY')" @click="refreshLogPolicyCache">Cache refresh</button>
      <button type="button" v-if="canWrite('LOG_POLICY')" @click="clearLogPolicyCache">Cache clear</button>
    </div>
    <div class="filters trace-filters">
      <label>거래 ID <input v-model="logPolicyForm.traceBoostTransactionId" type="text"></label><label>업무 거래 ID <input v-model="logPolicyForm.traceBoostBusinessTransactionId" type="text"></label>
      <label>API 경로 <input v-model="logPolicyForm.traceBoostApiPath" type="text"></label><label>상태 <input v-model="logPolicyForm.traceBoostStatus" type="text" placeholder="FAILED"></label>
      <label>실패코드 <input v-model="logPolicyForm.traceBoostFailureCode" type="text"></label><label>지연 기준(ms) <input v-model.number="logPolicyForm.traceBoostDurationMsGreaterThan" type="number"></label>
      <label>TTL(초) <input v-model.number="logPolicyForm.traceBoostTtlSeconds" type="number"></label><label>시작 <input v-model="logPolicyForm.effectiveStartAt" type="datetime-local"></label><label>종료 <input v-model="logPolicyForm.effectiveEndAt" type="datetime-local"></label>
    </div>

    <section class="result-section"><h3>정책 목록·처리 결과</h3>
      <table v-if="policyItems.length" class="data-table"><thead><tr><th>ID</th><th>정책</th><th>대상</th><th>레벨</th><th>DB/File</th><th>Body</th><th>Sampling</th><th>상태</th><th>갱신</th></tr></thead><tbody>
        <tr v-for="item in policyItems" :key="String(item.policy_id || item.policyId || item.policy_key)"><td>{{ item.policy_id || item.policyId }}</td><td>{{ item.policy_name || item.policyName }}<small>{{ item.policy_key || item.policyKey }}</small></td><td>{{ item.target_type || item.targetType }} / {{ item.target_id || item.targetId }}</td><td>{{ item.log_level || item.logLevel }}</td><td>{{ item.db_log_enabled_yn || item.dbLogEnabledYn }} / {{ item.file_log_enabled_yn || item.fileLogEnabledYn }}</td><td>{{ bodySummary(item) }}</td><td>{{ item.sampling_rate ?? item.samplingRate }}%</td><td><span class="status-chip">{{ item.active_yn || item.activeYn }}</span></td><td>{{ item.updated_at || item.updatedAt }}</td></tr>
      </tbody></table><StructuredDetails v-else-if="hasPolicyResult" title="처리 상세" :value="logPolicyResult" /><p v-else class="empty">조회 결과가 없습니다.</p>
    </section>

    <section class="result-section"><div class="section-heading"><h3>Gateway 적용 상태</h3><div class="summary-chips"><span>적용 {{ distributionSummary.applied }}</span><span>대기 {{ distributionSummary.pending }}</span><span :class="{ danger: distributionSummary.failed > 0 }">실패 {{ distributionSummary.failed }}</span></div></div>
      <table v-if="distributionItems.length" class="data-table"><thead><tr><th>Event</th><th>Gateway</th><th>Version</th><th>상태</th><th>시도</th><th>Fencing</th><th>오류</th><th>ACK</th></tr></thead><tbody>
        <tr v-for="item in distributionItems" :key="`${item.eventId}-${item.consumerId}`"><td>{{ item.eventId }}</td><td>{{ item.consumerId }}</td><td>{{ item.aggregateVersion }}</td><td><span class="status-chip" :class="statusClass(item.status)">{{ item.status }}</span></td><td>{{ item.attemptCount }}</td><td>{{ item.fencingToken }}</td><td>{{ item.errorCode }} {{ item.errorMessage }}</td><td>{{ item.acknowledgedAt || item.updatedAt }}</td></tr>
      </tbody></table><p v-else class="empty">적용 상태를 조회하면 Instance별 ACK와 실패 원인이 표시됩니다.</p>
    </section>
  </section>

  <section class="panel route-operation-panel">
    <h3>정책 등록·상세·수정</h3>
    <div class="actions"><button type="button" @click="createLogPolicy">신규 등록</button><button type="button" @click="loadLogPolicyDetail">상세 조회</button><button type="button" @click="updateLogPolicy">수정</button></div>
  </section>
</template>
<script lang="ts">
import { defineComponent } from "vue";
type PolicyItem = { policyId?: number; policy_id?: number; policyKey?: string; policy_key?: string; policyName?: string; policy_name?: string; targetType?: string; target_type?: string; targetId?: string; target_id?: string; logLevel?: string; log_level?: string; dbLogEnabledYn?: string; db_log_enabled_yn?: string; fileLogEnabledYn?: string; file_log_enabled_yn?: string; requestBodyCaptureMode?: string; request_body_capture_mode?: string; responseBodyCaptureMode?: string; response_body_capture_mode?: string; requestBodyLogYn?: string; request_body_log_yn?: string; responseBodyLogYn?: string; response_body_log_yn?: string; samplingRate?: number; sampling_rate?: number; activeYn?: string; active_yn?: string; updatedAt?: string; updated_at?: string };
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import StructuredDetails from "../../components/StructuredDetails.vue";
export default defineComponent({setup(){return useAdmConsolePage()},name:"LogPoliciesPage",components:{StructuredDetails},computed:{
  policyItems():PolicyItem[] {const r=(this as any).logPolicyResult||{};return Array.isArray(r.items)?r.items:(r.item&&Object.keys(r.item).length?[r.item]:[]);},
  hasPolicyResult():boolean{return Object.keys((this as any).logPolicyResult||{}).length>0;},
  distributionItems():Record<string,any>[] {const v=(this as any).logPolicyDistributionResult?.items;return Array.isArray(v)?v:[];},
  distributionSummary():{applied:number;pending:number;failed:number}{const v=(this as any).logPolicyDistributionResult||{};return{applied:Number(v.applied||0),pending:Number(v.pending||0),failed:Number(v.failed||0)};},
  captureRisk():{level:string;title:string;description:string}{const f=(this as any).logPolicyForm;const m=[f.requestBodyCaptureMode,f.responseBodyCaptureMode];if(m.includes("ENCRYPTED_BODY"))return{level:"risk-high",title:"승인 필요",description:"암호화 본문은 제한 Route, 별도 권한, TTL과 감사가 필요합니다."};if(m.includes("MASKED_BODY")||m.includes("ALLOWLIST_FIELDS"))return{level:"risk-medium",title:"제한 저장",description:"Schema Allowlist와 Masking Preview를 확인합니다."};return{level:"risk-safe",title:"안전 기본값",description:"본문 원문은 저장하지 않습니다."};}},methods:{
  syncLegacyBodyFlags(){const f=(this as any).logPolicyForm;f.requestBodyLogYn=["NONE","METADATA_ONLY"].includes(f.requestBodyCaptureMode)?"N":"Y";f.responseBodyLogYn=["NONE","METADATA_ONLY"].includes(f.responseBodyCaptureMode)?"N":"Y";f.errorStackLogYn=f.errorStackCaptureMode==="NONE"?"N":"Y";},
  bodySummary(i:PolicyItem):string{const q=i.request_body_capture_mode||i.requestBodyCaptureMode||((i.request_body_log_yn||i.requestBodyLogYn)==="Y"?"MASKED_BODY":"NONE");const s=i.response_body_capture_mode||i.responseBodyCaptureMode||((i.response_body_log_yn||i.responseBodyLogYn)==="Y"?"MASKED_BODY":"NONE");return `${q} / ${s}`;},
  statusClass(s:string):string{return s==="FAILED"?"status-danger":s==="APPLIED"?"status-ok":"status-warn";}}});
</script>
<style scoped>
.log-policy-page{display:grid;gap:1rem}.panel-title,.section-heading{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start}.policy-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(360px,1fr));gap:1rem}fieldset{border:1px solid var(--border-color,#d7dde7);border-radius:10px;padding:1rem;min-width:0}legend{font-weight:700;padding:0 .4rem}.filters{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:.7rem}label{display:grid;gap:.25rem;font-size:.84rem}input,select{min-width:0}.safety-preview{display:grid;gap:.25rem;margin-top:.8rem;padding:.8rem;border-radius:8px}.risk-safe{background:rgba(46,157,98,.12)}.risk-medium{background:rgba(216,153,24,.15)}.risk-high{background:rgba(200,70,70,.15)}.result-section{border-top:1px solid var(--border-color,#d7dde7);padding-top:1rem;overflow:auto}.data-table{width:100%;border-collapse:collapse;font-size:.84rem}th,td{padding:.55rem;border-bottom:1px solid var(--border-color,#d7dde7);text-align:left;vertical-align:top}td small{display:block;opacity:.7}.status-chip,.summary-chips span{display:inline-flex;padding:.15rem .45rem;border-radius:999px;background:rgba(120,130,150,.15)}.status-ok{background:rgba(46,157,98,.2)}.status-warn{background:rgba(216,153,24,.2)}.status-danger,.danger{background:rgba(200,70,70,.2)!important}.summary-chips{display:flex;gap:.4rem;flex-wrap:wrap}.muted,.empty{opacity:.72}@media(max-width:760px){.panel-title,.section-heading{flex-direction:column}.policy-grid{grid-template-columns:1fr}}
</style>
