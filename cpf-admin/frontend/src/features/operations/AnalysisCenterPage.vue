<template><section class="workbench" data-cpf-page="analysis-center"><header class="header"><div><h1>Analysis Center</h1><p>Unknown·DLQ·Outbox·Idempotency·File I/O의 상태 분포와 Transaction 연관성을 운영 데이터로 분석합니다.</p></div><button :disabled="loading" @click="load">재분석</button></header><form class="filters" @submit.prevent="load"><label>Transaction ID<input v-model.trim="transactionId"></label><label>Topic<input v-model.trim="topic"></label><button type="submit">분석</button></form><p v-if="error" class="state error">{{ error }}</p><div class="cards"><article v-for="metric in metrics" :key="metric.name" class="card"><span>{{ metric.name }}</span><strong>{{ metric.count }}</strong><small>{{ metric.failed }} 위험/미완료</small></article></div><div class="table-wrap"><table><thead><tr><th>영역</th><th>상태</th><th>건수</th><th>대표 Transaction</th></tr></thead><tbody><tr v-for="row in distribution" :key="row.area+row.status"><td>{{ row.area }}</td><td>{{ row.status }}</td><td>{{ row.count }}</td><td>{{ row.transactionId }}</td></tr></tbody></table></div><CpfStructuredData class="detail" :value="analysisSummary" /></section></template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { findDlq, findFileTransfers, findIdempotency, findOutbox, findUnknownResults, type JsonMap } from './api'
import { read } from './support'

const transactionId = ref('')
const topic = ref('')
const unknown = ref<JsonMap[]>([])
const dlq = ref<JsonMap[]>([])
const outbox = ref<JsonMap[]>([])
const idempotency = ref<JsonMap[]>([])
const files = ref<JsonMap[]>([])
const loading = ref(false)
const error = ref('')
const fetchedAt = ref('')

const sources = computed(() => [
  { name: 'Unknown', rows: unknown.value },
  { name: 'DLQ', rows: dlq.value },
  { name: 'Outbox', rows: outbox.value },
  { name: 'Idempotency', rows: idempotency.value },
  { name: 'File I/O', rows: files.value }
])
const metrics = computed(() => sources.value.map(source => ({
  name: source.name,
  count: source.rows.length,
  failed: source.rows.filter(row => /FAIL|ERROR|UNKNOWN|PENDING|RETRY|DLQ/i.test(String(read(row, 'status', 'state') ?? ''))).length
})))
const distribution = computed(() => sources.value.flatMap(source => {
  const grouped = new Map<string, JsonMap[]>()
  source.rows.forEach(row => {
    const status = String(read(row, 'status', 'state') ?? 'UNKNOWN')
    grouped.set(status, [...(grouped.get(status) ?? []), row])
  })
  return [...grouped].map(([status, rows]) => ({
    area: source.name,
    status,
    count: rows.length,
    transactionId: String(read(rows[0], 'transactionId', 'transaction_id') ?? '-')
  }))
}))
const analysisSummary = computed(() => ({
  freshness: fetchedAt.value,
  metrics: metrics.value,
  filters: { transactionId: transactionId.value, topic: topic.value }
}))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const results = await Promise.allSettled([
      findUnknownResults(undefined, transactionId.value, 200),
      findDlq(undefined, transactionId.value, topic.value, 200),
      findOutbox(undefined, transactionId.value, topic.value, 200),
      findIdempotency(undefined, undefined, undefined, 200),
      findFileTransfers(undefined, transactionId.value, 200)
    ])
    ;[unknown.value, dlq.value, outbox.value, idempotency.value, files.value] = results.map(result => result.status === 'fulfilled' ? result.value : [])
    const failureCount = results.filter(result => result.status === 'rejected').length
    if (failureCount) error.value = `부분 실패 ${failureCount}/5`
    fetchedAt.value = new Date().toISOString()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script><style scoped>
.workbench{display:grid;gap:1rem}.header,.actions,.tabs,.pager{display:flex;gap:.65rem;align-items:center;justify-content:space-between;flex-wrap:wrap}.header h1,.header h2{margin:0}.header p{margin:.35rem 0 0;color:#52606d}.filters{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:.7rem;align-items:end}.filters label,.dialog label{display:grid;gap:.3rem}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:.75rem}.card,.panel{border:1px solid #d7dde5;border-radius:.6rem;padding:1rem;background:#fff}.card strong{font-size:1.35rem;display:block}.table-wrap{overflow:auto;border:1px solid #d7dde5;border-radius:.5rem}table{border-collapse:collapse;width:100%;min-width:900px}th,td{padding:.65rem;border-bottom:1px solid #e4e8ed;text-align:left;vertical-align:top}tbody tr{cursor:pointer}tbody tr:hover,tbody tr.selected{background:#f3f6f9}.state{padding:.75rem;border-radius:.4rem;background:#eef3f7}.state.error{background:#fff0f0;color:#a51d1d}.state.warning{background:#fff8dc;color:#715600}.state.success{background:#edf9f0;color:#126b35}.danger{background:#9d1c1c;color:white}.primary{background:#1f5f99;color:white}.detail{white-space:pre-wrap;overflow:auto;max-height:480px;background:#111827;color:#e5e7eb;padding:1rem;border-radius:.5rem}.dialog-backdrop{position:fixed;inset:0;background:#0008;display:grid;place-items:center;z-index:1000}.dialog{width:min(620px,calc(100vw - 2rem));max-height:90vh;overflow:auto;background:#fff;border-radius:.65rem;padding:1.2rem;display:grid;gap:1rem}.dialog textarea{min-height:100px}.confirm{display:flex!important;grid-template-columns:auto 1fr!important;align-items:center}.muted{color:#68737d}.badge{display:inline-block;padding:.15rem .45rem;border-radius:999px;background:#e9eef5}.links{display:flex;gap:.6rem;flex-wrap:wrap}@media(max-width:720px){.header{display:grid}.actions{justify-content:flex-start}}
</style>
