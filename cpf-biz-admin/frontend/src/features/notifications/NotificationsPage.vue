<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  bzaSupportCreateNotification,
  bzaSupportReadAllNotifications,
  bzaSupportReadNotification,
} from "../../generated/orval/cpf-api";
import { bzaApi, hasBzaPermission } from "../auth/session";

const router = useRouter();
const unreadOnly = ref(true);
const rows = ref<any[]>([]);
const error = ref("");
const creating = ref(false);
const form = ref({
  recipientLoginId: "",
  notificationType: "BUSINESS",
  title: "",
  messageBody: "",
  referenceType: "",
  referenceId: "",
  reason: "",
});

async function load() {
  error.value = "";
  try {
    rows.value = await bzaApi(`/api/bza/notifications?unreadOnly=${unreadOnly.value}&limit=100`);
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  }
}

async function read(row: any) {
  if (!hasBzaPermission("SETTING", "WRITE")) return;
  try {
    await bzaSupportReadNotification(Number(row.notificationId), { reason: "알림 확인" });
    await load();
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  }
}

async function readAll() {
  if (!hasBzaPermission("SETTING", "WRITE")) return;
  try {
    await bzaSupportReadAllNotifications({ reason: "전체 알림 확인" });
    await load();
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  }
}

async function createNotification() {
  if (!hasBzaPermission("SETTING", "WRITE")) return;
  if (!form.value.recipientLoginId.trim() || !form.value.title.trim() || !form.value.messageBody.trim() || !form.value.reason.trim()) {
    error.value = "수신자, 제목, 내용, 등록 사유는 필수입니다.";
    return;
  }
  creating.value = true;
  error.value = "";
  try {
    await bzaSupportCreateNotification({
      recipientLoginId: form.value.recipientLoginId.trim(),
      notificationType: form.value.notificationType.trim() || "BUSINESS",
      title: form.value.title.trim(),
      messageBody: form.value.messageBody.trim(),
      referenceType: form.value.referenceType.trim() || null,
      referenceId: form.value.referenceId.trim() || null,
      reason: form.value.reason.trim(),
    });
    form.value = { recipientLoginId: "", notificationType: "BUSINESS", title: "", messageBody: "", referenceType: "", referenceId: "", reason: "" };
    await load();
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  } finally {
    creating.value = false;
  }
}

function openRef(row: any) {
  if (row.referenceType === "APPROVAL" && row.referenceId) {
    void router.push({ name: "approvalInbox", query: { approvalId: String(row.referenceId) } });
  }
}

onMounted(load);
</script>

<template>
  <section class="card">
    <div class="card-head">
      <div><p class="eyebrow">NOTIFICATION</p><h2>내 알림</h2></div>
      <div>
        <label><input v-model="unreadOnly" type="checkbox" @change="load" /> 미읽음만</label>
        <button v-if="hasBzaPermission('SETTING', 'WRITE')" @click="readAll">전체 읽음</button>
      </div>
    </div>
    <p v-if="error" class="error-banner">{{ error }}</p>
    <form v-if="hasBzaPermission('SETTING', 'WRITE')" class="cpf-card-body" @submit.prevent="createNotification">
      <h3>업무 알림 등록</h3>
      <div class="form-grid">
        <label>수신자<input v-model="form.recipientLoginId" required /></label>
        <label>유형<input v-model="form.notificationType" /></label>
        <label>제목<input v-model="form.title" required /></label>
        <label>참조 유형<input v-model="form.referenceType" placeholder="APPROVAL" /></label>
        <label>참조 ID<input v-model="form.referenceId" /></label>
        <label>등록 사유<input v-model="form.reason" required /></label>
      </div>
      <label>내용<textarea v-model="form.messageBody" rows="3" required /></label>
      <button :disabled="creating" type="submit">{{ creating ? "등록 중..." : "알림 등록" }}</button>
    </form>
    <div class="table-wrap">
      <table>
        <thead><tr><th>시각</th><th>유형</th><th>제목</th><th>내용</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="r in rows" :key="r.notificationId">
            <td>{{ r.createdAt }}</td><td>{{ r.notificationType }}</td><td>{{ r.title }}</td><td>{{ r.messageBody }}</td><td>{{ r.readYn }}</td>
            <td>
              <button v-if="r.readYn !== 'Y' && hasBzaPermission('SETTING', 'WRITE')" @click="read(r)">읽음</button>
              <button v-if="r.referenceId" @click="openRef(r)">업무 열기</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
