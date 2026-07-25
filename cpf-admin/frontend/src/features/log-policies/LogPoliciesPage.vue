<template>
      <section class="panel">
        <div class="panel-title">
          <h2>로그 정책</h2>
          <div class="actions">
            <button type="button" @click="loadLogPolicies">조회</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="saveLogPolicy">저장</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="createLogPolicyOverride">Override 등록</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicyOverride">Override 중지</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="createTraceBoost">Trace Boost 등록</button>
            <button type="button" @click="loadTraceBoostRuntimeState">Trace Boost 상태</button>
            <button type="button" @click="loadTraceBoostHistory">Trace Boost 이력</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="disableLogPolicy">정책 중지</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="refreshLogPolicyCache">Cache refresh</button>
            <button type="button" v-if="canWrite('LOG_POLICY')" @click="clearLogPolicyCache">Cache clear</button>
          </div>
        </div>
        <div class="filters">
          <label>Policy ID <input v-model.number="logPolicyForm.policyId" type="number"></label>
          <label>Policy Key <input v-model="logPolicyForm.policyKey" type="text"></label>
          <label>정책명 <input v-model="logPolicyForm.policyName" type="text"></label>
          <label>대상 유형 <input v-model="logPolicyForm.targetType" type="text"></label>
          <label>대상 ID <input v-model="logPolicyForm.targetId" type="text"></label>
          <label>레벨 <select v-model="logPolicyForm.logLevel"><option>TRACE</option><option>DEBUG</option><option>INFO</option><option>WARN</option><option>ERROR</option></select></label>
          <label>DB 로그 <select v-model="logPolicyForm.dbLogEnabledYn"><option>Y</option><option>N</option></select></label>
          <label>파일 로그 <select v-model="logPolicyForm.fileLogEnabledYn"><option>Y</option><option>N</option></select></label>
          <label>요청 본문 <select v-model="logPolicyForm.requestBodyLogYn"><option>Y</option><option>N</option></select></label>
          <label>응답 본문 <select v-model="logPolicyForm.responseBodyLogYn"><option>Y</option><option>N</option></select></label>
          <label>보존일 <input v-model.number="logPolicyForm.retentionDays" type="number"></label>
          <label>우선순위 <input v-model.number="logPolicyForm.priority" type="number"></label>
          <label>Trace 거래 ID <input v-model="logPolicyForm.traceBoostTransactionId" type="text"></label>
          <label>Trace 업무 거래 ID <input v-model="logPolicyForm.traceBoostBusinessTransactionId" type="text"></label>
          <label>Trace API 경로 <input v-model="logPolicyForm.traceBoostApiPath" type="text"></label>
          <label>Trace 상태 <input v-model="logPolicyForm.traceBoostStatus" type="text" placeholder="FAILED"></label>
          <label>Trace 실패코드 <input v-model="logPolicyForm.traceBoostFailureCode" type="text" placeholder="SERVICE_TIMEOUT"></label>
          <label>Trace 지연 기준(ms) <input v-model.number="logPolicyForm.traceBoostDurationMsGreaterThan" type="number"></label>
          <label>Trace TTL(초) <input v-model.number="logPolicyForm.traceBoostTtlSeconds" type="number"></label>
          <label>시작 <input v-model="logPolicyForm.effectiveStartAt" type="datetime-local"></label>
          <label>종료 <input v-model="logPolicyForm.effectiveEndAt" type="datetime-local"></label>
          <label>사유 <input v-model="logPolicyForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(logPolicyResult) }}</pre>
      </section>
  </template>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "LogPoliciesPage",
  mixins: [admConsoleMixin]
});
</script>
