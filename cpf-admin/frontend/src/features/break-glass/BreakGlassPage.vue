<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import CpfIcon from "../../components/CpfIcon.vue";
import { admInvokeOperation } from "../../shared/cpfApi";
import { useAdmSessionStore } from "../../stores/admSessionStore";
import { requireBreakGlassReason, validateBreakGlassRequest, type BreakGlassActionMode } from "./breakGlassWorkflow";

type Row = Record<string, unknown>;
const session = useAdmSessionStore();
const rows = ref<Row[]>([]);
const error = ref("");
const message = ref("");
const busy = ref(false);
const form = reactive({ scopeType: "SERVICE", scopeValue: "", reason: "긴급 장애 복구", ttlMinutes: 15 });
const actionDialog = reactive<{ open: boolean; mode: BreakGlassActionMode; row: Row | null; reason: string }>({
  open: false, mode: "CLOSE", row: null, reason: ""
});
const canWrite = computed(() => session.canWrite("breakGlass", "BREAK_GLASS", "/breakGlass"));
const activeCount = computed(() => rows.value.filter(row => row.status === "ACTIVE").length);
const reviewPendingCount = computed(() => rows.value.filter(row => row.postReviewStatus === "PENDING" && row.status !== "ACTIVE").length);

function clearFeedback(): void { error.value = ""; message.value = ""; }
async function run<T>(action: () => Promise<T>, successMessage?: string): Promise<T | undefined> {
  clearFeedback(); busy.value = true;
  try { const result = await action(); if (successMessage) message.value = successMessage; return result; }
  catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause); return undefined; }
  finally { busy.value = false; }
}
async function load(): Promise<void> {
  const result = await run(() => admInvokeOperation<Row[]>("admBreakGlassFindSessions", { query: { limit: 100 } }));
  if (result) rows.value = result;
}
async function openSession(): Promise<void> {
  if (!canWrite.value) { error.value = "Break-glass 발급 권한이 없습니다."; return; }
  let validated: ReturnType<typeof validateBreakGlassRequest>;
  try { validated = validateBreakGlassRequest(form.scopeValue, form.reason, form.ttlMinutes); }
  catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause); return; }
  const result = await run(() => admInvokeOperation("admBreakGlassOpenSession", {
    body: { scopeType: form.scopeType, ...validated }
  }), "Break-glass 세션을 발급했습니다.");
  if (result !== undefined) { form.scopeValue = ""; await load(); }
}
function openAction(row: Row, mode: BreakGlassActionMode): void {
  if (!canWrite.value) { error.value = "Break-glass 운영 권한이 없습니다."; return; }
  actionDialog.row = row; actionDialog.mode = mode; actionDialog.reason = ""; actionDialog.open = true;
}
function closeAction(): void { actionDialog.open = false; actionDialog.row = null; actionDialog.reason = ""; }
async function confirmAction(): Promise<void> {
  const row = actionDialog.row;
  if (!row) return;
  const sessionId = String(row.sessionId || "").trim();
  if (!sessionId) { error.value = "Break-glass 세션 ID가 없습니다."; return; }
  let reason: string;
  try { reason = requireBreakGlassReason(actionDialog.reason); }
  catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause); return; }
  const operation = actionDialog.mode === "CLOSE" ? "admBreakGlassCloseSession" : "admBreakGlassReviewSession";
  const body = actionDialog.mode === "CLOSE"
    ? { reason }
    : { status: actionDialog.mode === "REVIEW_APPROVE" ? "APPROVED" : "REJECTED", reason };
  const result = await run(() => admInvokeOperation(operation, { path: { sessionId }, body }),
    actionDialog.mode === "CLOSE" ? "Break-glass 세션을 종료했습니다." : "사후검토 결과를 기록했습니다.");
  if (result !== undefined) { closeAction(); await load(); }
}
function actionTitle(): string {
  if (actionDialog.mode === "CLOSE") return "Break-glass 세션 종료";
  return actionDialog.mode === "REVIEW_APPROVE" ? "사후검토 승인" : "사후검토 문제 기록";
}
onMounted(load);
</script>

<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">EMERGENCY CONTROL</p><h2>Break-glass 통제</h2><p>좁은 Scope와 짧은 TTL을 가진 비상 세션을 발급하고 사후검토까지 추적합니다.</p></div>
      <button type="button" class="ghost" :disabled="busy" @click="load"><CpfIcon name="refresh"/> 새로고침</button>
    </div>
    <section class="cpf-stat-grid">
      <article class="cpf-stat-card"><span>Active Session</span><strong>{{ activeCount }}</strong><small>운영자별 1개 제한</small></article>
      <article class="cpf-stat-card"><span>기본 TTL</span><strong>15m</strong><small>서버 최대값 초과 불가</small></article>
      <article class="cpf-stat-card"><span>Post Review</span><strong>{{ reviewPendingCount }}</strong><small>종료 후 검토 대기</small></article>
    </section>
    <section class="cpf-card" aria-labelledby="break-glass-issue-title">
      <div class="cpf-card-head"><div><h2 id="break-glass-issue-title">비상 세션 발급</h2><p>사용 범위를 구체적으로 제한하세요.</p></div></div>
      <div class="cpf-form-grid">
        <label>Scope<select v-model="form.scopeType" :disabled="busy"><option>SERVICE</option><option>BATCH</option><option>CENTER_CUT</option><option>RECOVERY</option><option>SECURITY</option></select></label>
        <label>대상<input v-model.trim="form.scopeValue" :disabled="busy" placeholder="MBR-01 / BATCH_JOB_ID"></label>
        <label>TTL(분)<input v-model.number="form.ttlMinutes" :disabled="busy" type="number" min="1" max="30"></label>
        <label class="span-2">긴급 사유<input v-model.trim="form.reason" :disabled="busy" minlength="5" required></label>
        <button v-if="canWrite" type="button" class="danger" :disabled="busy || !form.scopeValue" @click="openSession">Break-glass 발급</button>
      </div>
      <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
      <p v-if="message" class="success-banner" role="status" aria-live="polite">{{ message }}</p>
    </section>
    <section class="cpf-card" aria-label="Break-glass 세션과 사후검토 이력">
      <div class="cpf-card-head"><h2>세션·사후검토 이력</h2></div>
      <div class="table-wrap"><table><thead><tr><th>운영자</th><th>Scope</th><th>대상</th><th>상태</th><th>만료</th><th>사후검토</th><th>조치</th></tr></thead><tbody>
        <tr v-for="row in rows" :key="String(row.sessionId)"><td>{{ row.operatorId }}</td><td>{{ row.scopeType }}</td><td>{{ row.scopeValue }}</td><td><span class="cpf-status" :class="row.status === 'ACTIVE' ? 'danger' : 'success'">{{ row.status }}</span></td><td>{{ row.expiresAt }}</td><td>{{ row.postReviewStatus }}</td><td><div class="cpf-toolbar">
          <button v-if="canWrite && row.status === 'ACTIVE'" type="button" class="ghost small" :disabled="busy" @click="openAction(row, 'CLOSE')">종료</button>
          <template v-else-if="canWrite && row.postReviewStatus === 'PENDING'"><button type="button" class="primary small" :disabled="busy" @click="openAction(row, 'REVIEW_APPROVE')">검토승인</button><button type="button" class="danger small" :disabled="busy" @click="openAction(row, 'REVIEW_REJECT')">문제기록</button></template>
        </div></td></tr>
        <tr v-if="!rows.length"><td colspan="7" class="cpf-empty">Break-glass 이력이 없습니다.</td></tr>
      </tbody></table></div>
    </section>
    <dialog :open="actionDialog.open" class="modal" aria-labelledby="break-glass-action-title">
      <form class="modal-card" @submit.prevent="confirmAction">
        <div class="card-head"><h2 id="break-glass-action-title">{{ actionTitle() }}</h2><button type="button" class="icon-button" aria-label="닫기" @click="closeAction">×</button></div>
        <p>대상: <strong>{{ actionDialog.row?.scopeValue }}</strong></p>
        <label>감사 사유<textarea v-model.trim="actionDialog.reason" rows="4" minlength="5" required></textarea></label>
        <div class="dialog-actions"><button type="button" class="ghost" @click="closeAction">취소</button><button class="danger" :disabled="busy">확인</button></div>
      </form>
    </dialog>
  </div>
</template>
