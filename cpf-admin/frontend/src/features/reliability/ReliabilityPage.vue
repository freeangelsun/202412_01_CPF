<template>
      <section class="panel">
        <div class="panel-title">
          <h2>신뢰성 처리 관제</h2>
          <div class="actions">
            <button type="button" @click="loadReliability">통합 조회</button>
            <button type="button" @click="loadBatchJobLogDetail">BAT 로그 상세</button>
            <button type="button" v-if="canWrite('RELIABILITY')" @click="replayDlq">DLQ 재처리</button>
            <button type="button" v-if="canWrite('RELIABILITY')" @click="resolveUnknownResult">결과 수동 확정</button>
          </div>
        </div>
        <div class="filters">
          <label>Scope <input v-model="reliabilitySearch.scope" type="text"></label>
          <label>상태 <input v-model="reliabilitySearch.status" type="text"></label>
          <label>멱등 Key <input v-model="reliabilitySearch.key" type="text"></label>
          <label>transactionId <input v-model="reliabilitySearch.transactionId" type="text"></label>
          <label>Topic <input v-model="reliabilitySearch.topic" type="text"></label>
          <label>Endpoint <input v-model="reliabilitySearch.endpointCode" type="text"></label>
          <label>Unknown 유형 <input v-model="reliabilitySearch.type" type="text"></label>
          <label>업무일자 <input v-model="reliabilitySearch.businessDate" type="text" placeholder="yyyyMMdd"></label>
          <label>Job 이름 <input v-model="reliabilitySearch.jobName" type="text"></label>
          <label>JobInstance ID <input v-model.number="reliabilitySearch.jobInstanceId" type="number" min="1"></label>
          <label>Server Instance <input v-model="reliabilitySearch.serverInstanceId" type="text"></label>
          <label>조회 건수 <input v-model.number="reliabilitySearch.limit" type="number" min="1" max="500"></label>
          <label>Replay Message ID <input v-model="reliabilityAction.messageId" type="text"></label>
          <label>Unknown ID <input v-model="reliabilityAction.unknownId" type="text"></label>
          <label>확정 상태
            <select v-model="reliabilityAction.targetStatus">
              <option>CONFIRMED_SUCCESS</option>
              <option>CONFIRMED_FAILURE</option>
              <option>RETRY_PENDING</option>
              <option>MANUAL_REVIEW</option>
              <option>RESOLVED</option>
            </select>
          </label>
          <label>감사 사유 <input v-model="reliabilityAction.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(reliabilityResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "ReliabilityPage",
  mixins: [admConsoleMixin]
});
</script>
