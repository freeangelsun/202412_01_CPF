<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  fetchRetentionPolicies, fetchRetentionRuns,
  type RetentionPolicyRow, type RetentionRunRow,
} from './api'
import {
  admRetentionPolicyPause, admRetentionPolicyResume,
  admRetentionRunNow, admRetentionRunPause, admRetentionRunResume,
} from '../../generated/cpf-api'
import {
  admRetentionPolicySave,
  admRetentionPreview,
} from '../../generated/orval/cpf-api'
import type { AdmRetentionPolicySaveRequest } from '../../generated/orval/model/admRetentionPolicySaveRequest'
import type { AdmRetentionPreviewRequest } from '../../generated/orval/model/admRetentionPreviewRequest'
import { useAdmSessionStore } from '../../stores/admSessionStore'

const session = useAdmSessionStore()
const policies = ref<RetentionPolicyRow[]>([])
const runs = ref<RetentionRunRow[]>([])
const selectedPolicy = ref<RetentionPolicyRow | null>(null)
const selectedRun = ref<RetentionRunRow | null>(null)
const reason = ref('')
const preview = ref<Record<string, unknown> | null>(null)
const policyEditor = ref({ retentionDays: 30, chunkSize: 1000, throttleMs: 0, maxRowsPerRun: 100000, maxRuntimeSeconds: 600 })
const loading = ref(false)
const running = ref(false)
const error = ref('')
const notice = ref('')

const policyId = computed(() => String(selectedPolicy.value?.policyId ?? selectedPolicy.value?.policy_id ?? ''))
const runId = computed(() => String(selectedRun.value?.runId ?? selectedRun.value?.run_id ?? ''))
const canManage = computed(() => {
  const ids = new Set(session.buttonIds.map(v => String(v).toUpperCase()))
  return ['RETENTION_MANAGE','SERVER_RETENTION_MANAGE','BATCH_RETENTION_MANAGE'].some(id => ids.has(id))
})
const canRun = computed(() => canManage.value && Boolean(policyId.value) && reason.value.trim().length >= 5 && Boolean(preview.value))

function value(row: Record<string, unknown> | null, ...keys: string[]): unknown {
  for (const key of keys) if (row?.[key] !== undefined && row[key] !== null) return row[key]
  return '-'
}
function fmt(v: unknown): string { return v === null || v === undefined || v === '' ? '-' : String(v) }
function syncPolicyEditor(row: RetentionPolicyRow | null) {
  if (!row) return
  policyEditor.value = {
    retentionDays: Number(value(row,'retentionDays','retention_days')) || 30,
    chunkSize: Number(value(row,'chunkSize','chunk_size')) || 1000,
    throttleMs: Number(value(row,'throttleMs','throttle_ms')) || 0,
    maxRowsPerRun: Number(value(row,'maxRowsPerRun','max_rows_per_run')) || 100000,
    maxRuntimeSeconds: Number(value(row,'maxRuntimeSeconds','max_runtime_seconds')) || 600,
  }
}
function statusClass(v: unknown): string {
  const s = String(v ?? '').toUpperCase()
  return ['SUCCESS','COMPLETED','ACTIVE','ENABLED'].includes(s) ? 'success' : ['RUNNING','PARTIAL','PAUSED','SKIPPED'].includes(s) ? 'warning' : 'danger'
}
async function load() {
  loading.value = true; error.value = ''
  try {
    policies.value = await fetchRetentionPolicies()
    if (!selectedPolicy.value && policies.value.length) selectedPolicy.value = policies.value[0]
    syncPolicyEditor(selectedPolicy.value)
    runs.value = await fetchRetentionRuns(policyId.value, 100)
  } catch (e) { error.value = e instanceof Error ? e.message : 'Retention 정보를 조회하지 못했습니다.' }
  finally { loading.value = false }
}
async function refreshRuns() { try { runs.value = await fetchRetentionRuns(policyId.value, 100) } catch (e) { error.value = e instanceof Error ? e.message : String(e) } }
async function doPreview() {
  if (!selectedPolicy.value || reason.value.trim().length < 5) return
  running.value = true; error.value = ''; notice.value = ''
  try {
    const retentionDays = Number(value(selectedPolicy.value,'retentionDays','retention_days'))
    const cutoff = new Date(Date.now() - Math.max(1, retentionDays) * 86400000).toISOString()
    const body: AdmRetentionPreviewRequest = {
      target: value(selectedPolicy.value,'target','target_name'),
      action: value(selectedPolicy.value,'action','action_name'), cutoff,
      legalHold: String(value(selectedPolicy.value,'legalHold','legal_hold_yn')).toUpperCase() === 'Y' || value(selectedPolicy.value,'legalHold') === true,
      reason: reason.value.trim(), limit: policyEditor.value.chunkSize,
    }
    const response = await admRetentionPreview(body)
    preview.value = (response.data ?? {}) as Record<string, unknown>
    notice.value = '실제 BAT Retention handler dry-run 결과를 확인했습니다.'
  } catch (e) { error.value = e instanceof Error ? e.message : String(e); preview.value = null }
  finally { running.value = false }
}
async function doRun() {
  if (!canRun.value) return
  running.value = true; error.value = ''
  try { await admRetentionRunNow({ path: { policyId: policyId.value }, data: { reason: reason.value.trim() } }); preview.value = null; notice.value = 'Retention 실행을 요청했습니다.'; await refreshRuns() }
  catch (e) { error.value = e instanceof Error ? e.message : String(e) }
  finally { running.value = false }
}

async function savePolicy() {
  if (!canManage.value || !selectedPolicy.value || reason.value.trim().length < 5) return
  running.value = true; error.value = ''; notice.value = ''
  try {
    const body: AdmRetentionPolicySaveRequest = {
      ...selectedPolicy.value,
      policyId: policyId.value,
      target: value(selectedPolicy.value,'target','target_name'),
      action: value(selectedPolicy.value,'action','action_name'),
      retentionDays: Math.max(1, policyEditor.value.retentionDays),
      chunkSize: Math.max(1, policyEditor.value.chunkSize),
      throttleMs: Math.max(0, policyEditor.value.throttleMs),
      maxRowsPerRun: Math.max(1, policyEditor.value.maxRowsPerRun),
      maxRuntimeSeconds: Math.max(1, policyEditor.value.maxRuntimeSeconds),
      reason: reason.value.trim(),
    }
    await admRetentionPolicySave(body)
    notice.value = 'Retention 정책을 저장했습니다.'
    preview.value = null
    await load()
  } catch (e) { error.value = e instanceof Error ? e.message : String(e) }
  finally { running.value = false }
}

async function togglePolicy(paused: boolean) {
  if (!canManage.value || !policyId.value) return
  running.value = true; error.value = ''
  try { if (paused) await admRetentionPolicyPause({ path: { policyId: policyId.value } }); else await admRetentionPolicyResume({ path: { policyId: policyId.value } }); await load() }
  catch (e) { error.value = e instanceof Error ? e.message : String(e) }
  finally { running.value = false }
}
async function pauseRun() {
  if (!canManage.value || !runId.value) return
  running.value = true; error.value = ''
  try { await admRetentionRunPause({ path: { runId: runId.value } }); await refreshRuns() }
  catch (e) { error.value = e instanceof Error ? e.message : String(e) }
  finally { running.value = false }
}
async function resumeRun() {
  if (!canManage.value || !runId.value || reason.value.trim().length < 5) return
  running.value = true; error.value = ''
  try { await admRetentionRunResume({ path: { runId: runId.value }, data: { reason: reason.value.trim() } }); await refreshRuns() }
  catch (e) { error.value = e instanceof Error ? e.message : String(e) }
  finally { running.value = false }
}
watch(selectedPolicy, row => syncPolicyEditor(row))
onMounted(load)
</script>

<template>
  <section class="retention-workbench">
    <header class="page-header"><div><p class="eyebrow">BATCH OWNER / SHARED DB RETENTION</p><h2>Logging / Retention 실행</h2><p>Scheduled·Manual·Pause·Resume가 동일 BAT Execution Engine을 사용하며 ADM은 Control Plane으로만 동작합니다.</p></div><button type="button" :disabled="loading" @click="load">새로고침</button></header>
    <p v-if="error" class="message danger" role="alert">{{ error }}</p><p v-if="notice" class="message success" role="status">{{ notice }}</p>
    <div class="grid">
      <section class="card"><h3>Retention 정책</h3><div class="table-wrap"><table><thead><tr><th>Policy</th><th>Target</th><th>Action</th><th>Days</th><th>Version</th><th>Enabled</th></tr></thead><tbody>
        <tr v-for="row in policies" :key="fmt(value(row,'policyId','policy_id'))" :class="{selected:fmt(value(row,'policyId','policy_id'))===policyId}" @click="selectedPolicy=row; preview=null; refreshRuns()"><td><button type="button" class="link">{{ fmt(value(row,'policyId','policy_id')) }}</button></td><td>{{ fmt(value(row,'target','target_name')) }}</td><td>{{ fmt(value(row,'action','action_name')) }}</td><td>{{ fmt(value(row,'retentionDays','retention_days')) }}</td><td>{{ fmt(value(row,'policyVersion','policy_version')) }}</td><td>{{ fmt(value(row,'enabled','enabled_yn')) }}</td></tr>
        <tr v-if="!policies.length"><td colspan="6">정책이 없습니다.</td></tr></tbody></table></div>
        <div class="policy-editor" aria-label="Retention 정책 실행 제한">
          <label>Retention Days<input v-model.number="policyEditor.retentionDays" type="number" min="1" /></label>
          <label>Chunk Size<input v-model.number="policyEditor.chunkSize" type="number" min="1" /></label>
          <label>Throttle(ms)<input v-model.number="policyEditor.throttleMs" type="number" min="0" /></label>
          <label>Max Rows<input v-model.number="policyEditor.maxRowsPerRun" type="number" min="1" /></label>
          <label>Max Runtime(s)<input v-model.number="policyEditor.maxRuntimeSeconds" type="number" min="1" /></label>
        </div>
        <div class="actions"><button type="button" :disabled="!canManage || running || !policyId || reason.length<5" @click="savePolicy">정책 저장</button><button type="button" :disabled="!canManage || running || !policyId" @click="togglePolicy(true)">Schedule Pause</button><button type="button" :disabled="!canManage || running || !policyId" @click="togglePolicy(false)">Schedule Resume</button></div>
      </section>
      <aside class="card"><h3>Manual Run</h3><label>실행 사유<textarea v-model.trim="reason" minlength="5" placeholder="5자 이상 입력"></textarea></label><div class="actions"><button type="button" :disabled="running || !policyId || reason.length<5" @click="doPreview">1. Preview</button><button type="button" class="danger-button" :disabled="running || !canRun" @click="doRun">2. 지금 실행</button></div><p class="hint">Preview가 성공해야 실제 실행 버튼이 활성화됩니다.</p><CpfStructuredData v-if="preview" :value="preview" /></aside>
    </div>
    <section class="card"><div class="section-head"><h3>실제 Run History</h3><button type="button" @click="refreshRuns">이력 새로고침</button></div><div class="table-wrap"><table><thead><tr><th>Run</th><th>Trigger</th><th>Status</th><th>Runtime</th><th>Policy Version</th><th>Processed</th><th>Deleted</th><th>Archived</th><th>Compressed</th><th>Freed Bytes</th><th>Started / Completed</th><th>Error</th></tr></thead><tbody>
      <tr v-for="row in runs" :key="fmt(value(row,'runId','run_id'))" :class="{selected:fmt(value(row,'runId','run_id'))===runId}" @click="selectedRun=row"><td><button type="button" class="link">{{ fmt(value(row,'runId','run_id')) }}</button></td><td>{{ fmt(value(row,'triggerType','trigger_type')) }}</td><td><span :class="['status',statusClass(value(row,'status'))]">{{ fmt(value(row,'status')) }}</span></td><td>{{ fmt(value(row,'runtimeInstanceId','runtime_instance_id')) }}</td><td>{{ fmt(value(row,'policyVersion','policy_version')) }}</td><td>{{ fmt(value(row,'processedCount','processed_count')) }}</td><td>{{ fmt(value(row,'deletedCount','deleted_count')) }}</td><td>{{ fmt(value(row,'archivedCount','archived_count')) }}</td><td>{{ fmt(value(row,'compressedCount','compressed_count')) }}</td><td>{{ fmt(value(row,'freedBytes','freed_bytes')) }}</td><td>{{ fmt(value(row,'startedAt','started_at')) }}<br>{{ fmt(value(row,'completedAt','completed_at')) }}</td><td>{{ fmt(value(row,'errorCode','error_code')) }} {{ fmt(value(row,'errorSummary','error_summary')) }}</td></tr>
      <tr v-if="!runs.length"><td colspan="12">실행 이력이 없습니다.</td></tr></tbody></table></div><div class="actions"><button type="button" :disabled="!canManage || running || !runId" @click="pauseRun">Run Pause</button><button type="button" :disabled="!canManage || running || !runId || reason.length<5" @click="resumeRun">Run Resume</button></div></section>
  </section>
</template>

<style scoped>
.retention-workbench{display:grid;gap:1rem}.policy-editor{display:grid;grid-template-columns:repeat(auto-fit,minmax(9rem,1fr));gap:.65rem;margin:.8rem 0}.policy-editor label{display:grid;gap:.25rem}.policy-editor input{width:100%;box-sizing:border-box}.page-header,.section-head,.actions{display:flex;justify-content:space-between;align-items:flex-start;gap:.75rem}.eyebrow{font-size:.75rem;font-weight:700}.grid{display:grid;grid-template-columns:minmax(0,1.5fr) minmax(20rem,.7fr);gap:1rem}.card{border:1px solid var(--el-border-color,#dcdfe6);border-radius:.65rem;padding:1rem;min-width:0}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse}th,td{padding:.55rem;border-bottom:1px solid var(--el-border-color,#dcdfe6);text-align:left;white-space:nowrap}tr.selected{outline:2px solid currentColor;outline-offset:-2px}.link{border:0;background:transparent;color:var(--el-color-primary,#409eff);text-decoration:underline}.danger-button,.danger{color:var(--el-color-danger,#f56c6c)}.success{color:var(--el-color-success,#67c23a)}.warning{color:var(--el-color-warning,#e6a23c)}label{display:grid;gap:.3rem}textarea{min-height:6rem}.hint{font-size:.85rem}.actions{justify-content:flex-start;margin-top:.75rem}@media(max-width:1050px){.grid{grid-template-columns:1fr}}@media(max-width:640px){.page-header,.section-head,.actions{display:grid}.card{padding:.75rem}}
</style>
