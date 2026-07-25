<script setup lang="ts">
import { onMounted, ref } from "vue";
import { bzaApi } from "../auth/session";
const rows=ref<Record<string,unknown>[]>([]); const error=ref("");
async function load(){try{rows.value=await bzaApi("/api/bza/auth/sessions?limit=100");}catch(e){error.value=e instanceof Error?e.message:String(e);}}
async function revoke(item:Record<string,unknown>){const reason=prompt("세션 폐기 사유");if(!reason)return;await bzaApi(`/api/bza/auth/sessions/${item.sessionId}/revoke?reason=${encodeURIComponent(reason)}`,{method:"POST"});await load();}
onMounted(load);
</script>
<template><section class="card table-card"><div class="card-head"><div><p class="eyebrow">SESSION</p><h2>Refresh 세션</h2></div><button class="ghost" @click="load">새로고침</button></div><p v-if="error" class="error-banner">{{error}}</p><div class="table-wrap"><table><thead><tr><th>sessionId</th><th>transactionId</th><th>createdAt</th><th>expiresAt</th><th>revokedYn</th><th>관리</th></tr></thead><tbody><tr v-for="row in rows" :key="String(row.sessionId)"><td>{{row.sessionId}}</td><td>{{row.transactionId}}</td><td>{{row.createdAt}}</td><td>{{row.expiresAt}}</td><td>{{row.revokedYn}}</td><td><button v-if="row.revokedYn!=='Y'" class="ghost" @click="revoke(row)">폐기</button></td></tr></tbody></table></div></section></template>
