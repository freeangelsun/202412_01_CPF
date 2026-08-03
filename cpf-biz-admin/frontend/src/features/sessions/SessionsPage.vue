<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { bzaInvokeOperation } from "../../shared/cpfApi";
import { hasBzaPermission } from "../auth/session";
import { requireSessionId, requireSessionRevokeReason } from "./sessionWorkflow";

type SessionRow = Record<string, unknown>;
const rows = ref<SessionRow[]>([]);
const error = ref("");
const message = ref("");
const busy = ref(false);
const dialog = reactive<{ open: boolean; row: SessionRow | null; reason: string }>({ open: false, row: null, reason: "" });
const canRevoke = computed(() => hasBzaPermission("SESSIONS", "WRITE") || hasBzaPermission("USERS", "WRITE"));

async function run<T>(action: () => Promise<T>, success?: string): Promise<T | undefined> {
  error.value = "";
  message.value = "";
  busy.value = true;
  try {
    const result = await action();
    if (success) message.value = success;
    return result;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
    return undefined;
  } finally {
    busy.value = false;
  }
}
async function load(): Promise<void> {
  const result = await run(() => bzaInvokeOperation<SessionRow[]>("bzaAuthSessions", { query: { limit: 100 } }));
  if (result) rows.value = result;
}
function openRevoke(row: SessionRow): void {
  if (!canRevoke.value) {
    error.value = "세션 폐기 권한이 없습니다.";
    return;
  }
  dialog.row = row;
  dialog.reason = "";
  dialog.open = true;
}
function closeDialog(): void {
  dialog.open = false;
  dialog.row = null;
  dialog.reason = "";
}
async function confirmRevoke(): Promise<void> {
  const row = dialog.row;
  if (!row) return;
  let sessionId: string;
  let reason: string;
  try {
    sessionId = requireSessionId(row.sessionId);
    reason = requireSessionRevokeReason(dialog.reason);
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
    return;
  }
  const result = await run(() => bzaInvokeOperation("bzaAuthRevokeSession", {
    path: { sessionId },
    query: { reason }
  }), "세션을 폐기했습니다.");
  if (result !== undefined) {
    closeDialog();
    await load();
  }
}
onMounted(load);
</script>
<template>
<section class="card table-card" aria-labelledby="session-heading">
 <div class="card-head"><div><p class="eyebrow">SESSION</p><h2 id="session-heading">Refresh 세션</h2></div><button type="button" class="ghost" :disabled="busy" @click="load">새로고침</button></div>
 <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
 <p v-if="message" class="success-banner" role="status" aria-live="polite">{{ message }}</p>
 <div class="table-wrap"><table><thead><tr><th>sessionId</th><th>transactionId</th><th>createdAt</th><th>expiresAt</th><th>revokedYn</th><th>관리</th></tr></thead><tbody>
  <tr v-for="row in rows" :key="String(row.sessionId)"><td>{{ row.sessionId }}</td><td>{{ row.transactionId }}</td><td>{{ row.createdAt }}</td><td>{{ row.expiresAt }}</td><td>{{ row.revokedYn }}</td><td><button v-if="row.revokedYn !== 'Y' && canRevoke" type="button" class="danger small" :disabled="busy" @click="openRevoke(row)">폐기</button></td></tr>
  <tr v-if="!rows.length"><td colspan="6" class="cpf-empty">조회된 세션이 없습니다.</td></tr>
 </tbody></table></div>
 <dialog :open="dialog.open" class="modal" aria-labelledby="session-revoke-title">
  <form class="modal-card" @submit.prevent="confirmRevoke">
   <div class="card-head"><h2 id="session-revoke-title">Refresh 세션 폐기</h2><button type="button" class="icon-button" aria-label="닫기" @click="closeDialog">×</button></div>
   <p>{{ dialog.row?.sessionId }}</p>
   <label>감사 사유<textarea v-model.trim="dialog.reason" rows="4" minlength="5" required></textarea></label>
   <div class="dialog-actions"><button type="button" class="ghost" @click="closeDialog">취소</button><button class="danger" :disabled="busy">{{ busy ? '폐기 중...' : '폐기' }}</button></div>
  </form>
 </dialog>
</section>
</template>
