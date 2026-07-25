<template>
      <section class="panel">
        <div class="panel-title">
          <h2>동적 로그 레벨</h2>
          <button type="button" @click="loadLogLevelRules">조회</button>
        </div>
        <div class="filters">
          <label>업무 거래 ID <input v-model="logLevelForm.businessTransactionId" type="text" placeholder="REF01EDU0001"></label>
          <label>거래 ID <input v-model="logLevelForm.transactionId" type="text"></label>
          <label>레벨 <select v-model="logLevelForm.logLevel"><option>DEBUG</option><option>INFO</option><option>TRACE</option></select></label>
          <label>TTL 초 <input v-model.number="logLevelForm.ttlSeconds" type="number"></label>
          <label>사유 <input v-model="logLevelForm.reason" type="text"></label>
          <button type="button" v-if="canWrite('DYNAMIC_LOG')" @click="registerLogLevelRule">등록</button>
        </div>
        <pre class="detail">{{ pretty(logLevelResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "LogLevelPage",
  mixins: [admConsoleMixin]
});
</script>
