<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { bzaApi } from "../auth/session";

const router = useRouter();
const unreadOnly = ref(true);
const rows = ref<any[]>([]);
const error = ref("");

async function load() {
  try {
    rows.value = await bzaApi(`/api/bza/notifications?unreadOnly=${unreadOnly.value}&limit=100`);
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  }
}

async function read(row: any) {
  await bzaApi(`/api/bza/notifications/${row.notificationId}/read?reason=${encodeURIComponent("알림 확인")}`, { method: "POST" });
  await load();
}

async function readAll() {
  await bzaApi(`/api/bza/notifications/read-all?reason=${encodeURIComponent("전체 알림 확인")}`, { method: "POST" });
  await load();
}

function openRef(row: any) {
  if (row.referenceType === "APPROVAL" && row.referenceId) {
    void router.push({ name: "approvalInbox", query: { approvalId: String(row.referenceId) } });
  }
}

onMounted(load);
</script>
<template><section class="card"><div class="card-head"><div><p class="eyebrow">NOTIFICATION</p><h2>내 알림</h2></div><div><label><input v-model="unreadOnly" type="checkbox" @change="load"> 미읽음만</label><button @click="readAll">전체 읽음</button></div></div><p v-if="error" class="error-banner">{{error}}</p><div class="table-wrap"><table><thead><tr><th>시각</th><th>유형</th><th>제목</th><th>내용</th><th>상태</th><th></th></tr></thead><tbody><tr v-for="r in rows" :key="r.notificationId"><td>{{r.createdAt}}</td><td>{{r.notificationType}}</td><td>{{r.title}}</td><td>{{r.messageBody}}</td><td>{{r.readYn}}</td><td><button v-if="r.readYn!=='Y'" @click="read(r)">읽음</button><button v-if="r.referenceId" @click="openRef(r)">업무 열기</button></td></tr></tbody></table></div></section></template>
