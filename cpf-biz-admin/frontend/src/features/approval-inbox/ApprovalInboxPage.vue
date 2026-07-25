<script setup lang="ts">
import { onMounted, ref } from "vue";
import { bzaApi } from "../auth/session";
const rows=ref<Record<string,unknown>[]>([]);const error=ref("");
async function load(){try{rows.value=await bzaApi("/api/bza/backoffice/approvals?limit=100");}catch(e){error.value=e instanceof Error?e.message:String(e);}}
async function decide(row:Record<string,unknown>,action:string){const reason=prompt("처리 사유");if(!reason)return;try{await bzaApi(`/api/bza/approval/submissions/${row.approvalId}/decisions`,{method:"POST",body:JSON.stringify({action,idempotencyKey:crypto.randomUUID(),reason,comment:reason})});await load();}catch(e){error.value=e instanceof Error?e.message:String(e);}}
onMounted(load);
</script>
<template><section class="card table-card"><div class="card-head"><div><p class="eyebrow">INBOX</p><h2>결재 처리</h2></div><button class="ghost" @click="load">새로고침</button></div><p v-if="error" class="error-banner">{{error}}</p><div class="table-wrap"><table><thead><tr><th>번호</th><th>제목</th><th>요청자</th><th>상태</th><th>단계</th><th>처리</th></tr></thead><tbody><tr v-for="row in rows" :key="String(row.approvalId)"><td>{{row.approvalNo}}</td><td>{{row.title}}</td><td>{{row.requesterEmployeeNo}}</td><td>{{row.approvalStatus}}</td><td>{{row.currentStepNo}}</td><td><div class="inline-actions"><button class="primary small" @click="decide(row,'APPROVE')">승인</button><button class="ghost small" @click="decide(row,'AGREE')">합의</button><button class="danger small" @click="decide(row,'REJECT')">반려</button></div></td></tr></tbody></table></div></section></template>
