<template>
      <section class="panel">
        <div class="panel-title">
          <h2>표준 실행 카탈로그</h2>
          <button type="button" @click="loadStandardExecutions">조회</button>
        </div>
        <div class="filters">
          <label>유형
            <select v-model="standardExecutionSearch.type">
              <option value="">전체</option>
              <option value="ONLINE">온라인</option>
              <option value="BATCH">배치</option>
            </select>
          </label>
          <label>소유 업무 <input v-model="standardExecutionSearch.ownerDomain" type="text" placeholder="CPF"></label>
          <label>검색어 <input v-model="standardExecutionSearch.keyword" type="text" placeholder="ID, 실행명, source, endpoint"></label>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>표준 실행 ID</th><th>유형</th><th>실행명</th><th>소유 업무</th><th>Source 모듈</th><th>Endpoint</th></tr></thead>
            <tbody>
            <tr v-for="item in standardExecutionResult.items || []" :key="item.standardExecutionId" @click="loadStandardExecutionDetail(item.standardExecutionId)">
              <td>{{ item.standardExecutionId }}</td>
              <td>{{ item.executionType }}</td>
              <td>{{ item.executionName }}</td>
              <td>{{ item.ownerDomain }}</td>
              <td>{{ item.sourceModule }}</td>
              <td>{{ item.endpoint || '-' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
        <pre class="detail">{{ pretty(standardExecutionDetail) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";

export default defineComponent({setup(){return useAdmConsolePage()},
  name: "StandardExecutionsPage",
  });
</script>
