<template>
      <section class="panel">
        <div class="panel-title">
          <h2>감사 로그</h2>
          <button type="button" @click="loadAuditLogs">조회</button>
        </div>
        <div class="filters">
          <label>운영자 ID <input v-model="auditSearch.operatorId" type="text"></label>
          <label>행위 <input v-model="auditSearch.actionType" type="text"></label>
          <label>대상 유형 <input v-model="auditSearch.targetType" type="text"></label>
          <label>대상 ID <input v-model="auditSearch.targetId" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(auditResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "AuditLogsPage",
  mixins: [admConsoleMixin]
});
</script>
