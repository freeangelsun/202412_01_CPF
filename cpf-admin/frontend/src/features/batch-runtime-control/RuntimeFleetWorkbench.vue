<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAdmSessionStore } from '../../stores/admSessionStore'
import { fetchBatchRuntimeCommandState, fetchBatchView, submitBatchRuntimeCommand, type BatchRuntimeCommandRequest } from './api'
import { apiMessage, pretty, read, statusClass, text, type AnyRow } from './pageSupport'

const props = withDefaults(defineProps<{
  view: 'worker-pools' | 'agents' | 'instances'
  title: string
  description: string
  targetType: 'POOL' | 'AGENT' | 'INSTANCE'
  idKeys: string[]
  allowedActions?: Array<BatchRuntimeCommandRequest['commandType']>
}>(), { allowedActions: () => ['DRAIN', 'RESUME', 'RESTART'] })

const session = useAdmSessionStore()
const rows = ref<AnyRow[]>([])
const selected = ref<AnyRow | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const notice = ref('')
const filter = ref('')
const action = ref<BatchRuntimeCommandRequest['commandType']>('DRAIN')
const reason = ref('')
const approvalRequestId = ref('')
const approvedBy = ref('')
const approvalPolicyVersion = ref('BAT-RUNTIME-V1')
const confirmed = ref(false)
const commandResult = ref<AnyRow | null>(null)

const operatorId = computed(() => String(session.operator?.operatorId ?? '').trim())
const filtered = computed(() => {
  const q = filter.value.toLowerCase()
  return rows.value.filter(row => !q || JSON.stringify(row).toLowerCase().includes(q))
})
const canCommand = computed(() => {
  const buttons = new Set(session.buttonIds.map(value => String(value).toUpperCase()))
  return ['BATCH_RUNTIME_COMMAND', 'BAT_RUNTIME_COMMAND', `BATCH_${action.value}`].some(value => buttons.has(value))
})
const selectedState = computed(() => String(read(selected.value, 'effectiveState', 'effective_state', 'state', 'status', 'desired_state') ?? '').toUpperCase())
const stateAllowsAction = computed(() => {
  if (!selected.value) return false
  if (action.value === 'DRAIN') return ['RUNNING','UP','ACTIVE'].includes(selectedState.value)
  if (action.value === 'RESUME' || action.value === 'START') return ['DRAINING','DRAINED','STOPPED','DOWN'].includes(selectedState.value)
  if (action.value === 'STOP') return ['RUNNING','UP','ACTIVE','DRAINING'].includes(selectedState.value)
  return true
})

function targetId(row: AnyRow): string {
  return text(row, ...props.idKeys, 'instanceId', 'instance_id', 'poolId', 'pool_id', 'agentId', 'agent_id')
}
function version(row: AnyRow): number {
  const value = Number(read(row, 'version', 'rowVersion', 'row_version', 'fencingToken', 'fencing_token') ?? 0)
  return Number.isSafeInteger(value) && value >= 0 ? value : 0
}
async function sha256(value: string): Promise<string> {
  const bytes = new TextEncoder().encode(value)
  const digest = await crypto.subtle.digest('SHA-256', bytes)
  return Array.from(new Uint8Array(digest)).map(byte => byte.toString(16).padStart(2, '0')).join('')
}
async function load() {
  loading.value = true; error.value = ''
  try {
    const response = await fetchBatchView(props.view)
    rows.value = response.items ?? []
    if (response.partial || response.stale) error.value = `BAT Owner 조회가 ${response.partial ? '부분 성공' : 'stale'} 상태입니다. (${response.errorCode ?? 'UNKNOWN'})`
    if (selected.value) selected.value = rows.value.find(row => targetId(row) === targetId(selected.value!)) ?? null
  } catch (cause) { error.value = apiMessage(cause, `${props.title} 조회에 실패했습니다.`) }
  finally { loading.value = false }
}
async function execute() {
  if (!selected.value) { error.value = '조치 대상을 선택하세요.'; return }
  if (!operatorId.value) { error.value = '인증된 운영자 ID가 없습니다. requestUser fallback은 허용하지 않습니다.'; return }
  if (!canCommand.value) { error.value = 'Runtime 위험조치 권한이 없습니다.'; return }
  if (!stateAllowsAction.value) { error.value = `${selectedState.value || 'UNKNOWN'} 상태에서는 ${action.value} 조치를 수행할 수 없습니다.`; return }
  if (reason.value.trim().length < 5 || !approvalRequestId.value.trim() || !approvedBy.value.trim() || !confirmed.value) {
    error.value = '5자 이상의 사유, 승인 ID, 승인자와 최종 확인이 필요합니다.'; return
  }
  if (approvedBy.value.trim() === operatorId.value) { error.value = '요청자와 승인자는 분리되어야 합니다.'; return }
  actionLoading.value = true; error.value = ''; notice.value = ''; commandResult.value = null
  try {
    const snapshot = JSON.stringify(selected.value)
    const commandId = crypto.randomUUID()
    const request: BatchRuntimeCommandRequest = {
      commandId,
      idempotencyKey: `${props.view}:${targetId(selected.value)}:${action.value}:${version(selected.value)}`,
      commandType: action.value,
      targetType: props.targetType,
      targetIds: [targetId(selected.value)],
      targetSnapshot: snapshot,
      targetSnapshotHash: await sha256(snapshot),
      expectedVersion: version(selected.value),
      reason: reason.value.trim(),
      requestedAt: new Date().toISOString(),
      approvalPolicyVersion: approvalPolicyVersion.value.trim(),
      approvalRequestId: approvalRequestId.value.trim(),
      approvedBy: approvedBy.value.trim(),
      expiresAt: new Date(Date.now() + 10 * 60 * 1000).toISOString(),
      parameters: { sourceRoute: props.view },
      transactionId: String(read(selected.value, 'transactionId', 'transaction_id') ?? ''),
    }
    commandResult.value = await submitBatchRuntimeCommand(request)
    notice.value = `Runtime command ${commandId}가 접수됐습니다. UNKNOWN_RESULT이면 자동 재실행하지 말고 상태를 조회하세요.`
    await load()
  } catch (cause) { error.value = apiMessage(cause, 'Runtime 위험조치 요청에 실패했습니다.') }
  finally { actionLoading.value = false; confirmed.value = false }
}
async function refreshCommandState() {
  const key = String(read(commandResult.value, 'idempotency_key', 'idempotencyKey', 'command_id', 'commandId') ?? '')
  if (!key) return
  try { commandResult.value = await fetchBatchRuntimeCommandState(key) }
  catch (cause) { error.value = apiMessage(cause, 'Runtime command 상태 조회에 실패했습니다.') }
}
onMounted(load)
</script>

<template>
  <section class="fleet-workbench">
    <header class="page-header"><div><p class="eyebrow">BAT OWNER / RUNTIME FLEET</p><h2>{{ title }}</h2><p>{{ description }}</p></div><button type="button" :disabled="loading" @click="load">새로고침</button></header>
    <p v-if="error" class="message danger" role="alert">{{ error }}</p><p v-if="notice" class="message success" role="status">{{ notice }}</p>
    <div class="toolbar"><input v-model.trim="filter" type="search" :placeholder="`${title} 검색`"><span>{{ filtered.length }}건</span></div>
    <div class="workspace"><section class="card"><div class="table-wrap"><table><thead><tr><th>ID</th><th>Role/Pool</th><th>State</th><th>Host/Zone</th><th>Version</th><th>Heartbeat</th><th>Fencing</th></tr></thead><tbody>
      <tr v-for="row in filtered" :key="targetId(row)" :class="{selected:targetId(row)===targetId(selected||{})}" @click="selected=row"><td><button type="button" class="link">{{ targetId(row) }}</button></td><td>{{ text(row,'runtimeRole','runtime_role','poolId','pool_id','role') }}</td><td><span :class="['status',statusClass(read(row,'effectiveState','effective_state','status','state'))]">{{ text(row,'effectiveState','effective_state','status','state') }}</span></td><td>{{ text(row,'hostAlias','host_alias','host','zoneId','zone_id') }}</td><td>{{ text(row,'artifactVersion','artifact_version','version') }}</td><td>{{ text(row,'lastHeartbeatAt','last_heartbeat_at','heartbeatAt') }}</td><td>{{ text(row,'fencingToken','fencing_token') }}</td></tr>
      <tr v-if="!filtered.length"><td colspan="7">조회 결과가 없습니다.</td></tr></tbody></table></div></section>
      <aside class="card detail"><h3>선택 대상</h3><pre>{{ pretty(selected || {}) }}</pre><div class="danger-zone"><h3>승인 기반 Runtime 조치</h3><label>Command<select v-model="action"><option v-for="item in allowedActions" :key="item" :value="item">{{ item }}</option></select></label><label>사유<textarea v-model.trim="reason" minlength="5"></textarea></label><label>승인 요청 ID<input v-model.trim="approvalRequestId"></label><label>승인자<input v-model.trim="approvedBy" placeholder="요청자와 다른 운영자"></label><label>승인 정책 Version<input v-model.trim="approvalPolicyVersion"></label><label><input v-model="confirmed" type="checkbox"> 대상 Snapshot·CAS Version·UNKNOWN_RESULT 복구 절차를 확인했습니다.</label><button type="button" class="danger-button" :disabled="actionLoading || !selected || !canCommand || !stateAllowsAction" @click="execute">{{ action }} 요청</button></div><section v-if="commandResult"><div class="command-head"><h3>Command 결과</h3><button type="button" @click="refreshCommandState">상태 조회</button></div><pre>{{ pretty(commandResult) }}</pre></section></aside></div>
  </section>
</template>
<style scoped>
.fleet-workbench{display:grid;gap:1rem}.page-header,.toolbar,.command-head{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start}.eyebrow{font-size:.75rem;font-weight:700}.workspace{display:grid;grid-template-columns:minmax(0,1.5fr) minmax(22rem,1fr);gap:1rem}.card{border:1px solid var(--el-border-color,#dcdfe6);border-radius:.65rem;padding:1rem;min-width:0}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse}th,td{padding:.6rem;border-bottom:1px solid var(--el-border-color,#dcdfe6);text-align:left;white-space:nowrap}tr.selected{outline:2px solid var(--el-color-primary,#409eff)}.detail{position:sticky;top:1rem}pre{white-space:pre-wrap;overflow:auto;max-height:22rem;background:var(--el-fill-color-light,#f5f7fa);padding:.75rem}.danger-zone{display:grid;gap:.6rem;border-top:1px solid var(--el-color-danger,#f56c6c);padding-top:1rem}.danger-zone label{display:grid;gap:.25rem}.danger-button,.danger{color:var(--el-color-danger,#f56c6c)}.success{color:var(--el-color-success,#67c23a)}.warning{color:var(--el-color-warning,#e6a23c)}.link{border:0;background:transparent;color:var(--el-color-primary,#409eff);text-decoration:underline}textarea{min-height:5rem}@media(max-width:1050px){.workspace{grid-template-columns:1fr}.detail{position:static}}@media(max-width:640px){.page-header,.toolbar{display:grid}}
</style>
