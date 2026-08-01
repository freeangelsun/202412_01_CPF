<template>
      <section class="panel">
        <div class="panel-title">
          <h2>다운로드 감사</h2>
          <div class="actions">
            <button type="button" @click="loadDownloadPolicies">조회</button>
            <button type="button" @click="downloadCsv(downloadForm.downloadType)">CSV 다운로드</button>
          </div>
        </div>
        <div class="filters">
          <label>다운로드 유형
            <select v-model="downloadForm.downloadType">
              <option>TRANSACTION_LOGS</option>
              <option>ERROR_LOGS</option>
              <option>BATCH_EXECUTIONS</option>
              <option>NOTIFICATION_DELIVERY_LOGS</option>
            </select>
          </label>
          <label>대상 화면 <input v-model="downloadForm.targetType" type="text"></label>
          <label>시작 <input v-model="downloadForm.fromDate" type="text" placeholder="2026-06-19 00:00:00"></label>
          <label>종료 <input v-model="downloadForm.toDate" type="text" placeholder="2026-06-19 23:59:59"></label>
          <label>거래 ID <input v-model="downloadForm.transactionId" type="text"></label>
          <label>Trace ID <input v-model="downloadForm.traceId" type="text"></label>
          <label>Job ID <input v-model="downloadForm.jobId" type="text"></label>
          <label>건수 <input v-model.number="downloadForm.limit" type="number"></label>
          <label>사유 <input v-model="downloadForm.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(downloadResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "DownloadsPage",
  });
</script>
