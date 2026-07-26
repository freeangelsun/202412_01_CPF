<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchBatchView, type BatchViewEnvelope } from './api'
const props = defineProps<{ view: string; title: string; description: string }>()
const state = ref<BatchViewEnvelope | null>(null)
const loading = ref(false)
const error = ref('')
const columns = computed(() => {
  const keys = new Set<string>()
  for (const row of state.value?.items ?? []) Object.keys(row).forEach(k => keys.add(k))
  return [...keys].slice(0, 18)
})
function value(row: Record<string, unknown>, key: string) {
  const raw = row[key]
  if (raw === null || raw === undefined) return '-'
  if (typeof raw === 'object') return JSON.stringify(raw)
  return String(raw)
}
async function refresh() {
  loading.value = true
  try { state.value = await fetchBatchView(props.view); error.value = '' }
  catch (e) { error.value = e instanceof Error ? e.message : 'BAT 조회 실패' }
  finally { loading.value = false }
}
onMounted(refresh)
</script>
<template>
<section class="batch-view" :aria-labelledby="`batch-${view}`">
  <header><div><h1 :id="`batch-${view}`">{{ title }}</h1><p>{{ description }}</p></div><button :disabled="loading" @click="refresh">{{ loading ? '조회 중…' : '새로고침' }}</button></header>
  <p v-if="error" role="alert" class="danger">{{ error }}</p>
  <p v-if="state?.stale || state?.partial" role="status" class="warning">BAT Control Server 조회가 불완전합니다. 이 상태를 정상·빈 결과로 해석하지 마세요.</p>
  <p v-if="state && !state.items.length">조회 결과가 없습니다.</p>
  <div v-if="state?.items.length" class="table-wrap"><table><thead><tr><th v-for="column in columns" :key="column">{{ column }}</th></tr></thead>
  <tbody><tr v-for="(row,index) in state.items" :key="index"><td v-for="column in columns" :key="column">{{ value(row,column) }}</td></tr></tbody></table></div>
</section>
</template>
<style scoped>.batch-view{display:grid;gap:1rem}header{display:flex;justify-content:space-between;gap:1rem;align-items:start}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse}th,td{padding:.55rem;text-align:left;border-bottom:1px solid currentColor;white-space:nowrap}.warning,.danger{font-weight:700}</style>
