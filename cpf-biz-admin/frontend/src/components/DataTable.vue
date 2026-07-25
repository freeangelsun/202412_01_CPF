<script setup lang="ts">
defineProps<{ title: string; rows: Record<string, unknown>[]; columns: string[]; loading?: boolean }>();
</script>

<template>
  <section class="card table-card">
    <div class="card-head">
      <div><p class="eyebrow">DATA</p><h2>{{ title }}</h2></div>
      <span class="count-pill">{{ rows.length }}건</span>
    </div>
    <div v-if="loading" class="empty-state">조회 중...</div>
    <div v-else class="table-wrap">
      <table>
        <thead><tr><th v-for="column in columns" :key="column">{{ column }}</th></tr></thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="String(row.id ?? row.approvalId ?? row.employeeNo ?? index)">
            <td v-for="column in columns" :key="column">{{ row[column] ?? "-" }}</td>
          </tr>
          <tr v-if="rows.length === 0"><td :colspan="columns.length" class="empty-cell">조회 결과가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
