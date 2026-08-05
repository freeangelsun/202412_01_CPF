<template>
  <section class="cpf-page center-cut-page">
    <header class="cpf-page-heading">
      <div>
        <p class="eyebrow">BATCH OPERATIONS / CENTER CUT</p>
        <h2>Center-Cut Execution Recovery</h2>
        <p>Job 단위 일괄 변경 없이 실행 단위 실패·결과불명만 별도 승인으로 처리합니다.</p>
      </div>
      <button class="ghost" type="button" @click="loadJobs">새로고침</button>
    </header>

    <OperationStateBanner
      :loading="loading"
      :failure="failure"
      :empty="!loading && !failure && jobs.length === 0"
      @retry="loadJobs"
    />

    <div class="workspace">
      <section class="cpf-card">
        <div class="cpf-card-head"><div><h3>Center-Cut Job</h3><p>조회 전용 목록</p></div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Job</th><th>이름</th><th>상태</th><th>상세</th></tr></thead>
            <tbody>
              <tr v-for="job in jobs" :key="jobId(job)" :class="{ selected: selectedJobId === jobId(job) }">
                <td>{{ jobId(job) }}</td>
                <td>{{ text(job, ['jobName', 'job_name', 'name']) }}</td>
                <td><span class="cpf-status" :class="statusClass(text(job, ['status', 'state']))">{{ text(job, ['status', 'state']) }}</span></td>
                <td><button class="text-button" type="button" @click="selectJob(job)">열기</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="cpf-card">
        <div class="cpf-card-head">
          <div><h3>Execution 결과</h3><p>{{ selectedJobId || 'Job을 선택하세요.' }}</p></div>
        </div>
        <OperationStateBanner
          :loading="detailLoading"
          :failure="detailFailure"
          :empty="Boolean(selectedJobId) && !detailLoading && !detailFailure && results.length === 0"
          @retry="reloadSelected"
        />
        <div v-if="workspace" class="summary-grid" aria-label="Center-Cut 처리 요약">
          <div v-for="entry in summaryEntries" :key="entry.key"><span>{{ entry.key }}</span><strong>{{ entry.value }}</strong></div>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Execution</th><th>Result</th><th>상태</th><th>메시지</th><th>운영 조치</th></tr></thead>
            <tbody>
              <tr v-for="row in results" :key="resultKey(row)">
                <td>{{ executionId(row) }}</td>
                <td>{{ text(row, ['resultId', 'result_id', 'id']) }}</td>
                <td><span class="cpf-status" :class="statusClass(resultStatus(row))">{{ resultStatus(row) }}</span></td>
                <td>{{ maskedMessage(row) }}</td>
                <td>
                  <button
                    v-if="canReprocess(row)"
                    class="danger"
                    type="button"
                    :disabled="submitting"
                    @click="prepare(row, 'reprocess-failed')"
                  >실패 재처리</button>
                  <button
                    v-if="canReconcile(row)"
                    class="danger"
                    type="button"
                    :disabled="submitting"
                    @click="prepare(row, 'reconcile-unknown')"
                  >UNKNOWN 대사</button>
                  <span v-if="!canReprocess(row) && !canReconcile(row)">조회 전용</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <DangerousActionDialog
      :open="Boolean(pending)"
      :title="pendingTitle"
      :description="pendingDescription"
      :target="pendingTarget"
      risk="CRITICAL"
      :approval-required="Boolean(pending?.ticket)"
      :expected-version-required="false"
      :submitting="submitting"
      :initial-approval-id="pending?.ticket?.approvalRequestId ?? ''"
      :initial-idempotency-key="pending?.ticket?.requestKey ?? ''"
      :initial-reason="pending?.ticket?.reason ?? ''"
      :confirm-label="pending?.ticket ? '승인 확인 후 실행' : '승인 요청 생성'"
      @cancel="pending = null"
      @confirm="executePending"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import DangerousActionDialog from '../../components/DangerousActionDialog.vue'
import OperationStateBanner from '../../components/OperationStateBanner.vue'
import { classifyAdmFailure, maskOperationalValue, type AdmFailureState } from '../../shared/operationState'
import {
  createCenterCutApproval,
  executeCenterCutAction,
  fetchCenterCutJobs,
  fetchCenterCutWorkspace,
  type CenterCutApprovalTicket,
  type CenterCutExecutionAction,
  type CenterCutWorkspace,
  type DangerousBatchCommand,
} from '../batch-runtime-control/api'

type Row = Record<string, unknown>
interface PendingAction { row: Row; action: CenterCutExecutionAction; ticket?: CenterCutApprovalTicket }

const jobs = ref<Row[]>([])
const workspace = ref<CenterCutWorkspace | null>(null)
const selectedJobId = ref('')
const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const failure = ref<AdmFailureState | null>(null)
const detailFailure = ref<AdmFailureState | null>(null)
const pending = ref<PendingAction | null>(null)

const results = computed(() => workspace.value?.results ?? [])
const summaryEntries = computed(() => Object.entries(workspace.value?.summary ?? {})
  .filter(([key]) => !/(password|secret|token|authorization|credential)/i.test(key))
  .slice(0, 12)
  .map(([key, value]) => ({ key, value: format(value) })))
const pendingTitle = computed(() => pending.value?.action === 'reprocess-failed'
  ? '실패 Execution을 재처리하시겠습니까?'
  : 'UNKNOWN Execution을 재대사하시겠습니까?')
const pendingDescription = computed(() => pending.value?.action === 'reprocess-failed'
  ? '선택한 executionId의 FAILED Item만 RETRY 상태로 전이합니다. Job 단위 일괄 재처리는 허용되지 않습니다.'
  : '외부 결과와 Owner 원장을 확인한 뒤 선택한 executionId의 UNKNOWN Item만 대사합니다. Blind retry는 허용되지 않습니다.')
const pendingTarget = computed(() => ({
  Job: selectedJobId.value || '-',
  Execution: pending.value ? executionId(pending.value.row) : '-',
  상태: pending.value ? resultStatus(pending.value.row) : '-',
  승인요청: pending.value?.ticket?.approvalRequestId ?? '미생성',
  승인상태: pending.value?.ticket?.status ?? '승인 요청 생성 전',
}))

onMounted(loadJobs)

async function loadJobs(): Promise<void> {
  loading.value = true
  failure.value = null
  try {
    jobs.value = await fetchCenterCutJobs()
    if (selectedJobId.value) await reloadSelected()
  } catch (error) {
    failure.value = classifyAdmFailure(error)
    jobs.value = []
  } finally {
    loading.value = false
  }
}

async function selectJob(job: Row): Promise<void> {
  selectedJobId.value = jobId(job)
  await reloadSelected()
}

async function reloadSelected(): Promise<void> {
  if (!selectedJobId.value) return
  detailLoading.value = true
  detailFailure.value = null
  try {
    workspace.value = await fetchCenterCutWorkspace(selectedJobId.value)
  } catch (error) {
    detailFailure.value = classifyAdmFailure(error)
    workspace.value = null
  } finally {
    detailLoading.value = false
  }
}

function prepare(row: Row, action: CenterCutExecutionAction): void {
  const id = executionId(row)
  if (!id || id === '-') throw new Error('executionId가 없는 결과는 변경할 수 없습니다.')
  pending.value = { row, action }
}

async function executePending(command: DangerousBatchCommand): Promise<void> {
  if (!pending.value) return
  submitting.value = true
  detailFailure.value = null
  try {
    if (!pending.value.ticket) {
      const ticket = await createCenterCutApproval(
        executionId(pending.value.row),
        pending.value.action,
        command.reason,
        command.idempotencyKey,
      )
      pending.value = { ...pending.value, ticket }
      return
    }
    await executeCenterCutAction(pending.value.ticket, command)
    pending.value = null
    await reloadSelected()
  } catch (error) {
    detailFailure.value = classifyAdmFailure(error)
  } finally {
    submitting.value = false
  }
}

function canReprocess(row: Row): boolean {
  return resultStatus(row) === 'FAILED' && executionId(row) !== '-'
}
function canReconcile(row: Row): boolean {
  return resultStatus(row) === 'UNKNOWN' && executionId(row) !== '-'
}
function resultStatus(row: Row): string {
  return text(row, ['resultStatus', 'result_status', 'status']).toUpperCase()
}
function executionId(row: Row): string {
  return text(row, ['executionId', 'execution_id', 'centerCutExecutionId', 'center_cut_execution_id'])
}
function jobId(row: Row): string {
  return text(row, ['centerCutJobId', 'center_cut_job_id', 'jobId', 'job_id'])
}
function resultKey(row: Row): string {
  return `${executionId(row)}:${text(row, ['resultId', 'result_id', 'id'])}`
}
function maskedMessage(row: Row): string {
  return maskOperationalValue(text(row, ['resultMessage', 'result_message', 'message', 'errorMessage']))
}
function text(row: Row, keys: string[]): string {
  for (const key of keys) {
    const value = row?.[key]
    if (value !== undefined && value !== null && String(value).trim()) return String(value).trim()
  }
  return '-'
}
function format(value: unknown): string {
  if (value === undefined || value === null || value === '') return '-'
  return typeof value === 'object' ? JSON.stringify(value) : String(value)
}
function statusClass(value: string): string {
  const status = value.toUpperCase()
  if (['COMPLETED', 'SUCCESS', 'SUCCEEDED'].includes(status)) return 'success'
  if (['RUNNING', 'RETRY', 'REPROCESSING'].includes(status)) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.workspace{display:grid;grid-template-columns:minmax(20rem,.7fr) minmax(0,1.3fr);gap:1rem}.table-wrap{overflow:auto;max-height:42rem}.table-wrap tr.selected{outline:2px solid currentColor;outline-offset:-2px}.summary-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(9rem,1fr));gap:.75rem;margin-bottom:1rem}.summary-grid>div{display:grid;gap:.25rem;padding:.75rem;background:#f7f8fa;border-radius:.5rem}.summary-grid span{font-size:.85rem}.summary-grid strong{overflow-wrap:anywhere}.table-wrap td:last-child{white-space:nowrap}.table-wrap td:last-child button+button{margin-left:.4rem}@media(max-width:960px){.workspace{grid-template-columns:1fr}}@media(max-width:640px){.cpf-page-heading{display:grid}.table-wrap{max-height:none}}
</style>
