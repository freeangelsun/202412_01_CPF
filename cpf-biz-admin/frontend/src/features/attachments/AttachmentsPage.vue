<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { bzaApi, hasBzaPermission } from "../auth/session";
import { bzaInvokeOperation, bzaRawResponse } from "../../shared/cpfApi";
import {
  filenameFromContentDisposition,
  requireAuditReason,
  safeAttachmentFilename,
  type AttachmentActionMode
} from "./attachmentWorkflow";

type AttachmentRow = Record<string, unknown>;

const groupId = ref("GENERAL");
const rows = ref<AttachmentRow[]>([]);
const error = ref("");
const message = ref("");
const busy = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const uploadReason = ref("");
const actionDialog = reactive<{
  open: boolean;
  mode: AttachmentActionMode;
  row: AttachmentRow | null;
  scanStatus: "CLEAN" | "QUARANTINED";
  reason: string;
}>({ open: false, mode: "RECHECK", row: null, scanStatus: "CLEAN", reason: "" });

const canWrite = computed(() => hasBzaPermission("ATTACHMENT", "WRITE"));
const canDownload = computed(() => hasBzaPermission("ATTACHMENT", "DOWNLOAD") || hasBzaPermission("ATTACHMENT", "READ"));
const canSecure = computed(() => hasBzaPermission("ATTACHMENT", "SECURITY") || canWrite.value);

function clearFeedback(): void {
  error.value = "";
  message.value = "";
}

async function run<T>(action: () => Promise<T>, successMessage?: string): Promise<T | undefined> {
  clearFeedback();
  busy.value = true;
  try {
    const result = await action();
    if (successMessage) message.value = successMessage;
    return result;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
    return undefined;
  } finally {
    busy.value = false;
  }
}

async function load(): Promise<void> {
  const result = await run(() => bzaInvokeOperation<AttachmentRow[]>("bzaSupportFindAttachments", {
    query: { groupId: groupId.value.trim() }
  }));
  if (result) rows.value = result;
}

async function upload(event: Event): Promise<void> {
  if (!canWrite.value) {
    error.value = "첨부 등록 권한이 없습니다.";
    return;
  }
  const form = event.currentTarget as HTMLFormElement;
  const file = fileInput.value?.files?.[0];
  if (!file) {
    error.value = "업로드할 파일을 선택하세요.";
    return;
  }
  let reason: string;
  try { reason = requireAuditReason(uploadReason.value); }
  catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause); return; }

  const data = new FormData();
  data.set("groupId", groupId.value.trim());
  data.set("reason", reason);
  data.set("file", file, file.name);
  const result = await run(
    () => bzaInvokeOperation("bzaSupportUploadAttachment", { body: data }),
    "첨부파일 등록 요청을 완료했습니다. 검사 상태를 확인하세요."
  );
  if (result !== undefined) {
    form.reset();
    uploadReason.value = "";
    await load();
  }
}

function openAction(row: AttachmentRow, mode: AttachmentActionMode, scanStatus: "CLEAN" | "QUARANTINED" = "CLEAN"): void {
  if (!canSecure.value) {
    error.value = "첨부 보안 조치 권한이 없습니다.";
    return;
  }
  actionDialog.row = row;
  actionDialog.mode = mode;
  actionDialog.scanStatus = scanStatus;
  actionDialog.reason = "";
  actionDialog.open = true;
}

function closeAction(): void {
  actionDialog.open = false;
  actionDialog.row = null;
  actionDialog.reason = "";
}

async function confirmAction(): Promise<void> {
  const row = actionDialog.row;
  if (!row) return;
  let reason: string;
  try { reason = requireAuditReason(actionDialog.reason); }
  catch (cause) { error.value = cause instanceof Error ? cause.message : String(cause); return; }
  const attachmentId = String(row.attachmentId || "").trim();
  if (!attachmentId) {
    error.value = "첨부파일 식별자가 없습니다.";
    return;
  }

  const action = actionDialog.mode === "RECHECK"
    ? () => bzaInvokeOperation("bzaSupportRecheckAttachment", { path: { attachmentId }, query: { reason } })
    : () => bzaInvokeOperation("bzaSupportUpdateAttachmentSecurity", {
        path: { attachmentId },
        body: {
          scanStatus: actionDialog.scanStatus,
          dataClassification: row.dataClassification || "INTERNAL",
          quarantineYn: actionDialog.scanStatus === "QUARANTINED" ? "Y" : "N",
          useYn: row.useYn || "Y",
          reason
        }
      });
  const result = await run(action, actionDialog.mode === "RECHECK" ? "재검사 요청을 완료했습니다." : "보안 상태를 변경했습니다.");
  if (result !== undefined) {
    closeAction();
    await load();
  }
}

async function download(row: AttachmentRow): Promise<void> {
  if (!canDownload.value) {
    error.value = "첨부 다운로드 권한이 없습니다.";
    return;
  }
  const attachmentId = String(row.attachmentId || "").trim();
  if (!attachmentId) {
    error.value = "첨부파일 식별자가 없습니다.";
    return;
  }
  await run(async () => {
    const response = await bzaRawResponse(`/api/bza/attachments/${encodeURIComponent(attachmentId)}/download`, "GET");
    if (!response.ok) {
      const payload = await response.text();
      throw new Error(payload || `첨부 다운로드 실패: HTTP ${response.status}`);
    }
    const blob = await response.blob();
    const fallback = safeAttachmentFilename(row.originalFileName, attachmentId);
    const filename = filenameFromContentDisposition(response.headers.get("content-disposition"), fallback);
    const objectUrl = URL.createObjectURL(blob);
    try {
      const anchor = document.createElement("a");
      anchor.href = objectUrl;
      anchor.download = filename;
      anchor.rel = "noopener";
      anchor.click();
    } finally {
      URL.revokeObjectURL(objectUrl);
    }
  }, "첨부파일 다운로드를 시작했습니다.");
}

onMounted(load);
</script>

<template>
  <div class="page-stack">
    <section class="card" aria-labelledby="attachment-heading">
      <div class="card-head">
        <div><p class="eyebrow">ATTACHMENT</p><h2 id="attachment-heading">첨부파일 보안 운영</h2></div>
        <button type="button" class="ghost" :disabled="busy" @click="load">새로고침</button>
      </div>
      <label>그룹 ID<input v-model.trim="groupId" :disabled="busy" @change="load"></label>
      <form v-if="canWrite" class="cpf-form-grid" @submit.prevent="upload">
        <label>감사 사유<input v-model.trim="uploadReason" minlength="5" required></label>
        <label>첨부파일<input ref="fileInput" name="file" type="file" required></label>
        <button class="primary" :disabled="busy">업로드</button>
      </form>
      <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
      <p v-if="message" class="success-banner" role="status" aria-live="polite">{{ message }}</p>
    </section>

    <section class="card" aria-label="첨부파일 목록">
      <div class="table-wrap">
        <table>
          <thead><tr><th>파일</th><th>검사</th><th>분류</th><th>격리</th><th>보존</th><th>조치</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="String(row.attachmentId)">
              <td>{{ row.originalFileName }}</td><td>{{ row.scanStatus }}</td><td>{{ row.dataClassification }}</td>
              <td>{{ row.quarantineYn }}</td><td>{{ row.retentionUntil || '-' }}</td>
              <td class="cpf-action-row">
                <button v-if="canDownload" type="button" :disabled="busy" @click="download(row)">다운로드</button>
                <button v-if="canSecure" type="button" :disabled="busy" @click="openAction(row, 'RECHECK')">재검사</button>
                <button v-if="canSecure" type="button" :disabled="busy" @click="openAction(row, 'SECURITY', 'CLEAN')">CLEAN</button>
                <button v-if="canSecure" type="button" class="danger" :disabled="busy" @click="openAction(row, 'SECURITY', 'QUARANTINED')">격리</button>
              </td>
            </tr>
            <tr v-if="!rows.length"><td colspan="6" class="cpf-empty">조회된 첨부파일이 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <dialog :open="actionDialog.open" class="modal" aria-labelledby="attachment-action-title">
      <form class="modal-card" @submit.prevent="confirmAction">
        <div class="card-head">
          <h2 id="attachment-action-title">{{ actionDialog.mode === 'RECHECK' ? '첨부 재검사' : '첨부 보안 상태 변경' }}</h2>
          <button type="button" class="icon-button" aria-label="닫기" @click="closeAction">×</button>
        </div>
        <p>{{ actionDialog.row?.originalFileName }}</p>
        <p v-if="actionDialog.mode === 'SECURITY'">변경 상태: <strong>{{ actionDialog.scanStatus }}</strong></p>
        <label>감사 사유<textarea v-model.trim="actionDialog.reason" rows="4" minlength="5" required></textarea></label>
        <div class="dialog-actions">
          <button type="button" class="ghost" @click="closeAction">취소</button>
          <button :class="actionDialog.scanStatus === 'QUARANTINED' ? 'danger' : 'primary'" :disabled="busy">확인</button>
        </div>
      </form>
    </dialog>
  </div>
</template>
