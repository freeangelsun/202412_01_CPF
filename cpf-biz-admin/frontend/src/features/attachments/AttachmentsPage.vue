<script setup lang="ts">
import { onMounted, ref } from "vue";
import DataTable from "../../components/DataTable.vue";
import { bzaApi, hasBzaPermission } from "../auth/session";
const groupId=ref("GENERAL"); const rows=ref<Record<string,unknown>[]>([]); const error=ref("");
async function load(){try{rows.value=await bzaApi(`/api/bza/attachments?groupId=${encodeURIComponent(groupId.value)}`);}catch(e){error.value=e instanceof Error?e.message:String(e);}}
async function upload(event:Event){const form=event.currentTarget as HTMLFormElement;const data=new FormData(form);data.set("groupId",groupId.value);try{await bzaApi("/api/bza/attachments",{method:"POST",body:data});form.reset();await load();}catch(e){error.value=e instanceof Error?e.message:String(e);}}
onMounted(load);
</script>
<template><div class="page-stack"><section class="card"><div class="card-head"><div><p class="eyebrow">ATTACHMENT</p><h2>첨부파일</h2></div></div><div class="form-grid compact"><label><span>그룹 ID</span><input v-model="groupId" @change="load"></label></div><form v-if="hasBzaPermission('ATTACHMENT','WRITE')" class="form-grid" @submit.prevent="upload"><label><span>감사 사유</span><input name="reason" required></label><label class="wide"><span>파일</span><input name="file" type="file" required></label><div class="form-action"><button class="primary">업로드</button></div></form><p v-if="error" class="error-banner">{{error}}</p></section><DataTable title="첨부 목록" :rows="rows" :columns="['attachmentId','attachmentGroupId','originalFileName','contentType','fileSize','checksumSha256','scanStatus','createdAt']"/></div></template>
