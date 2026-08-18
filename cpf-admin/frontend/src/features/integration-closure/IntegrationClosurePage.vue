<script setup lang="ts">
import CpfModal from '../../components/ui/CpfModal.vue'
import { computed, ref } from "vue";
import { parseStrictJsonObject } from "../../shared/strictJsonObject";
import { integrationClosureApi, type WebhookDelivery } from "./integrationClosureApi";
import { useAdmSessionStore } from "../../stores/admSessionStore";
import {
  approvalDraftFingerprint,
  clearApprovalIdempotency,
  markApprovalConfirmed,
  resolveApprovalIdempotency,
} from "./integrationClosureIdempotency";

const loading = ref<Record<string, boolean>>({});
const error = ref("");
const notice = ref("");
const cryptoStatus = ref<Record<string, unknown> | null>(null);
const timeStatus = ref<Record<string, unknown> | null>(null);
const zone = ref("Asia/Seoul");
const maxSkewMillis = ref(1000);
const recordId = ref("");
const recordJson = ref("{}");
const validationResult = ref<Record<string, unknown> | null>(null);
const quarantineId = ref("");
const expectedVersion = ref(1);
const reason = ref("");
const correctedJson = ref("{}");
const approvalId = ref<number | null>(null);
const approvalConfirmed = ref(false);
const activeApprovalFingerprint = ref<string | null>(null);
const approvalResult = ref<Record<string, unknown> | null>(null);
const replayResult = ref<Record<string, unknown> | null>(null);
const dlqLimit = ref(100);
const dlqRows = ref<WebhookDelivery[]>([]);
const webhookId = ref("");
const webhookExpectedVersion = ref(1);
const webhookReason = ref("");
const webhookReplayResult = ref<Record<string, unknown> | null>(null);
const replayIdempotencyKey = ref(globalThis.crypto.randomUUID());
type DangerousAction = "APPROVAL_EXECUTE" | "QUALITY_REPLAY" | "WEBHOOK_REPLAY";
const dangerousAction = ref<DangerousAction | "">("");
const dangerousConfirmed = ref(false);
const sessionStore = useAdmSessionStore();
const can = (operationId: string) => sessionStore.hasButton(operationId);
const canAll = (...operationIds: string[]) => operationIds.every(operationId => can(operationId));
const permissionReason = (...operationIds: string[]) => {
  const missing = operationIds.filter(operationId => !can(operationId));
  return missing.length ? `권한 없음: ${missing.join(", ")}` : "";
};

const canRequestApproval = computed(() => can("admIntegrationDataQualityCorrectionApprovalRequest")
  && Boolean(quarantineId.value.trim()) && reason.value.trim().length >= 8
  && expectedVersion.value > 0 && !approvalConfirmed.value && !loading.value.approvalRequest);

function statusOf(failure: unknown): number | undefined {
  if (failure && typeof failure === "object" && "status" in failure) return Number((failure as { status?: unknown }).status);
  return undefined;
}
function messageOf(failure: unknown): string {
  const status = statusOf(failure);
  const messages: Record<number, string> = {
    400: "입력값을 확인하세요.", 401: "세션이 만료되었습니다. 다시 로그인하세요.",
    403: "이 작업을 수행할 권한이 없습니다.", 404: "대상을 찾을 수 없습니다.",
    409: "버전 또는 실행 상태가 변경되었습니다. 최신 상태를 다시 조회하세요.",
    429: "요청이 너무 많습니다. 잠시 후 같은 작업으로 재시도하세요.",
    500: "서버 처리 중 오류가 발생했습니다.", 503: "서비스가 일시적으로 사용할 수 없습니다.",
  };
  if (status && messages[status]) return `${status} ${messages[status]}`;
  return failure instanceof Error ? failure.message : String(failure);
}
async function run<T>(name: string, action: () => Promise<T>): Promise<T | undefined> {
  if (loading.value[name]) return undefined;
  error.value = ""; notice.value = ""; loading.value = { ...loading.value, [name]: true };
  try { return await action(); }
  catch (failure) { error.value = messageOf(failure); return undefined; }
  finally { loading.value = { ...loading.value, [name]: false }; }
}
async function loadOperationalStatus() {
  const result = await run("status", async () => Promise.all([
    integrationClosureApi.cryptoStatus(),
    integrationClosureApi.timeHealth(zone.value.trim(), maxSkewMillis.value),
  ]));
  if (result) { [cryptoStatus.value, timeStatus.value] = result; notice.value = "암호화·시간 상태를 조회했습니다."; }
}
async function validateRecord() {
  await run("validate", async () => {
    validationResult.value = await integrationClosureApi.validate(recordId.value.trim(), parseStrictJsonObject(recordJson.value, "검증 데이터"));
    notice.value = "데이터 품질 검증을 완료했습니다.";
  });
}
async function requestApproval() {
  await run("approvalRequest", async () => {
    const corrected = parseStrictJsonObject(correctedJson.value, "정정 데이터");
    const draft = { quarantineId: quarantineId.value.trim(), expectedVersion: expectedVersion.value, reason: reason.value.trim(), corrected };
    const fingerprint = await approvalDraftFingerprint(draft);
    activeApprovalFingerprint.value = fingerprint;
    const idempotency = resolveApprovalIdempotency(fingerprint);
    if (idempotency.state === "confirmed") {
      approvalId.value = idempotency.approvalRequestId ?? null;
      approvalConfirmed.value = true;
      throw new Error("이미 성공이 확정된 승인 요청입니다. 새 작업을 시작하세요.");
    }
    const response = await integrationClosureApi.requestCorrectionApproval(draft.quarantineId, {
      expectedVersion: draft.expectedVersion, idempotencyKey: idempotency.key, reason: draft.reason, corrected: draft.corrected,
    });
    const id = Number(response.approvalRequestId ?? response.id);
    if (!Number.isSafeInteger(id) || id < 1) throw new Error("서버 응답에 유효한 승인 요청 ID가 없습니다.");
    markApprovalConfirmed(fingerprint, idempotency.key, id);
    approvalId.value = id; approvalConfirmed.value = true; approvalResult.value = response;
    notice.value = "승인 요청 성공이 확정되었습니다. 중복 요청은 차단됩니다.";
  });
}
function executeApproval() {
  if (!approvalId.value || reason.value.trim().length < 8) return;
  dangerousAction.value = "APPROVAL_EXECUTE"; dangerousConfirmed.value = false;
}
async function executeApprovalNow() {
  await run("approvalExecute", async () => {
    approvalResult.value = await integrationClosureApi.executeCorrectionApproval(approvalId.value!, { reason: reason.value.trim() });
    notice.value = "승인된 정정 실행 결과를 조회했습니다.";
  });
}
function startNewApproval() {
  if (activeApprovalFingerprint.value) clearApprovalIdempotency(localStorage, activeApprovalFingerprint.value);
  activeApprovalFingerprint.value = null; approvalId.value = null; approvalConfirmed.value = false; approvalResult.value = null;
  replayIdempotencyKey.value = globalThis.crypto.randomUUID();
  quarantineId.value = ""; expectedVersion.value = 1; reason.value = ""; correctedJson.value = "{}";
  notice.value = "새 승인 작업을 시작했습니다.";
}
function replayQuality() {
  if (!quarantineId.value.trim() || reason.value.trim().length < 8) return;
  dangerousAction.value = "QUALITY_REPLAY"; dangerousConfirmed.value = false;
}
async function replayQualityNow() {
  await run("qualityReplay", async () => {
    replayResult.value = await integrationClosureApi.replayQuality(quarantineId.value.trim(), {
      expectedVersion: expectedVersion.value, idempotencyKey: replayIdempotencyKey.value, reason: reason.value.trim(),
    });
    replayIdempotencyKey.value = globalThis.crypto.randomUUID();
    notice.value = "데이터 품질 재검증을 실행했습니다.";
  });
}
async function loadDlq() {
  await run("dlq", async () => { dlqRows.value = await integrationClosureApi.webhookDlq(Math.min(500, Math.max(1, dlqLimit.value))); notice.value = "Webhook DLQ를 조회했습니다."; });
}
function replayWebhook() {
  if (!webhookId.value.trim() || webhookReason.value.trim().length < 8) return;
  dangerousAction.value = "WEBHOOK_REPLAY"; dangerousConfirmed.value = false;
}
async function replayWebhookNow() {
  await run("webhookReplay", async () => {
    webhookReplayResult.value = await integrationClosureApi.replayWebhook(webhookId.value.trim(), webhookExpectedVersion.value, webhookReason.value.trim());
    notice.value = "Webhook 재처리 요청을 실행했습니다.";
  });
}
function cancelDangerousAction(){dangerousAction.value="";dangerousConfirmed.value=false;}
async function confirmDangerousAction(){
  if(!dangerousAction.value||!dangerousConfirmed.value)return;
  const action=dangerousAction.value;cancelDangerousAction();
  if(action==="APPROVAL_EXECUTE"){await executeApprovalNow();return;}
  if(action==="QUALITY_REPLAY"){await replayQualityNow();return;}
  await replayWebhookNow();
}
</script>

<template>
  <main class="integration-closure" aria-labelledby="integration-closure-title">
    <h1 id="integration-closure-title">통합 운영 Closure</h1>
    <p>조회·검증·승인 정정·Replay를 서버 원장과 Session 인증 경계에서 수행합니다.</p>
    <p v-if="notice" class="notice" aria-live="polite">{{ notice }}</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>

    <section aria-labelledby="status-title">
      <h2 id="status-title">암호화·시간 상태</h2>
      <div class="form-grid">
        <label>업무 시간대 <input v-model="zone" autocomplete="off" /></label>
        <label>허용 Skew(ms) <input v-model.number="maxSkewMillis" type="number" min="0" /></label>
      </div>
      <button type="button" :title="permissionReason('admIntegrationCryptoStatus', 'admIntegrationTimeHealth')" :disabled="loading.status || !canAll('admIntegrationCryptoStatus', 'admIntegrationTimeHealth')" @click="loadOperationalStatus">상태 조회</button>
      <div class="results"><pre v-if="cryptoStatus" tabindex="0">{{ JSON.stringify(cryptoStatus, null, 2) }}</pre><pre v-if="timeStatus" tabindex="0">{{ JSON.stringify(timeStatus, null, 2) }}</pre></div>
    </section>

    <section aria-labelledby="validate-title">
      <h2 id="validate-title">데이터 품질 검증</h2>
      <form @submit.prevent="validateRecord">
        <label>Record ID <input v-model="recordId" required autocomplete="off" /></label>
        <label>검증 JSON <textarea v-model="recordJson" required spellcheck="false" /></label>
        <button type="submit" :title="permissionReason('admIntegrationDataQualityValidate')" :disabled="loading.validate || !recordId.trim() || !can('admIntegrationDataQualityValidate')">검증</button>
      </form>
      <pre v-if="validationResult" tabindex="0">{{ JSON.stringify(validationResult, null, 2) }}</pre>
    </section>

    <section aria-labelledby="correction-title">
      <h2 id="correction-title">격리 데이터 정정 승인</h2>
      <p>동일 Draft의 timeout·응답 유실·재시도에는 브라우저 localStorage에서 동일 Draft의 멱등키를 재사용합니다. pending 키는 최대 24시간, confirmed 키는 최대 7일 동안 재사용하며 TTL 만료 또는 새 작업 시작 시 generation을 증가시켜 키를 회전합니다. Payload 원문은 저장하지 않습니다.</p>
      <form @submit.prevent="requestApproval">
        <div class="form-grid">
          <label>격리 ID <input v-model="quarantineId" required autocomplete="off" /></label>
          <label>기대 버전 <input v-model.number="expectedVersion" type="number" min="1" required /></label>
        </div>
        <label>사유 <textarea v-model="reason" required maxlength="500" /></label>
        <label>정정 JSON <textarea v-model="correctedJson" required spellcheck="false" /></label>
        <div class="actions">
          <button type="submit" :title="permissionReason('admIntegrationDataQualityCorrectionApprovalRequest')" :disabled="!canRequestApproval">승인 요청</button>
          <button v-if="approvalId" type="button" :title="permissionReason('admIntegrationDataQualityCorrectionExecute')" :disabled="loading.approvalExecute || reason.trim().length < 8 || !can('admIntegrationDataQualityCorrectionExecute')" @click="executeApproval">승인 검증 후 단회 실행</button>
          <button v-if="approvalConfirmed" type="button" @click="startNewApproval">새 작업</button>
          <button type="button" :title="permissionReason('admIntegrationDataQualityReplay')" :disabled="loading.qualityReplay || !quarantineId.trim() || reason.trim().length < 8 || !can('admIntegrationDataQualityReplay')" @click="replayQuality">재검증 Replay</button>
        </div>
      </form>
      <p v-if="approvalId">승인 요청 ID: <strong>{{ approvalId }}</strong></p>
      <p v-if="approvalId"><a :href="`/auditLogs?targetType=APPROVAL_REQUEST&targetId=${approvalId}`">감사 로그 연결</a></p>
      <div class="results"><pre v-if="approvalResult" tabindex="0">{{ JSON.stringify(approvalResult, null, 2) }}</pre><pre v-if="replayResult" tabindex="0">{{ JSON.stringify(replayResult, null, 2) }}</pre></div>
    </section>

    <section aria-labelledby="webhook-title">
      <h2 id="webhook-title">Webhook DLQ·Replay</h2>
      <div class="form-grid">
        <label>조회 제한 <input v-model.number="dlqLimit" type="number" min="1" max="500" /></label>
        <button type="button" :title="permissionReason('admIntegrationWebhookDlq')" :disabled="loading.dlq || !can('admIntegrationWebhookDlq')" @click="loadDlq">DLQ 조회</button>
      </div>
      <p v-if="!loading.dlq && dlqRows.length === 0" class="empty">조회된 DLQ가 없습니다.</p>
      <div v-else class="table-wrap" tabindex="0">
        <table><caption>Webhook DLQ</caption><thead><tr><th>식별자</th><th>상태</th><th>버전</th></tr></thead>
          <tbody><tr v-for="(row,index) in dlqRows" :key="String(row.id ?? row.deliveryId ?? index)">
            <td>{{ row.id ?? row.deliveryId ?? '-' }}</td><td>{{ row.status ?? row.state ?? '-' }}</td><td>{{ row.version ?? row.versionNo ?? '-' }}</td>
          </tr></tbody></table>
      </div>
      <form @submit.prevent="replayWebhook">
        <div class="form-grid">
          <label>Webhook ID <input v-model="webhookId" required autocomplete="off" /></label>
          <label>기대 버전 <input v-model.number="webhookExpectedVersion" type="number" min="1" required /></label>
        </div>
        <label>재처리 사유 <textarea v-model="webhookReason" required maxlength="500" /></label>
        <button type="submit" :title="permissionReason('admIntegrationWebhookReplay')" :disabled="loading.webhookReplay || !webhookId.trim() || webhookReason.trim().length < 8 || !can('admIntegrationWebhookReplay')">Webhook Replay</button>
      </form>
      <pre v-if="webhookReplayResult" tabindex="0">{{ JSON.stringify(webhookReplayResult, null, 2) }}</pre>
    </section>
    <CpfModal :open="Boolean(dangerousAction)" @cancel="cancelDangerousAction" aria-labelledby="integration-dangerous-action-title">
      <form class="modal-card" @submit.prevent="confirmDangerousAction">
        <h2 id="integration-dangerous-action-title">위험 운영조치 확인</h2>
        <p v-if="dangerousAction==='APPROVAL_EXECUTE'">승인 요청 {{approvalId}}을 단회 실행합니다.</p>
        <p v-else-if="dangerousAction==='QUALITY_REPLAY'">격리 데이터 {{quarantineId}}를 동일 멱등성 정책으로 재검증합니다.</p>
        <p v-else>Webhook {{webhookId}}를 재처리합니다.</p>
        <p>Backend의 사유·권한·버전·멱등성·감사 정책은 그대로 적용됩니다.</p>
        <label><input v-model="dangerousConfirmed" type="checkbox"> 대상과 영향 범위를 확인했습니다.</label>
        <div class="actions"><button type="button" @click="cancelDangerousAction">취소</button><button type="submit" :disabled="!dangerousConfirmed">확인 후 실행</button></div>
      </form>
    </CpfModal>
  </main>
</template>

<style scoped>
.integration-closure{max-width:76rem;margin:0 auto;padding:1rem;display:grid;gap:1rem}section{border:1px solid var(--el-border-color,#d4d7de);border-radius:.5rem;padding:1rem}form,.form-grid{display:grid;gap:.75rem}.form-grid{grid-template-columns:repeat(auto-fit,minmax(14rem,1fr));align-items:end}label{display:grid;gap:.25rem}input,textarea,button{font:inherit;padding:.65rem}textarea{min-height:6rem}.actions,.results{display:flex;flex-wrap:wrap;gap:.75rem}button{width:max-content}pre{overflow:auto;max-height:24rem;flex:1 1 20rem}.notice{padding:.75rem;background:#e8f5e9}.error{padding:.75rem;background:#ffebee}.empty{font-style:italic}.table-wrap{overflow:auto}table{border-collapse:collapse;width:100%}th,td{border:1px solid #d4d7de;padding:.5rem;text-align:left}@media(max-width:40rem){button{width:100%}.actions{display:grid}}
</style>
