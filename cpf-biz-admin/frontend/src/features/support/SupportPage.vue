<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import CrudTable from "../../components/CrudTable.vue";
import DataTable from "../../components/DataTable.vue";
import { bzaApi, hasBzaPermission } from "../auth/session";
const props=defineProps<{routeId:string}>();
const rows=ref<Record<string,unknown>[]>([]); const secondary=ref<Record<string,unknown>[]>([]); const loading=ref(false); const error=ref("");
const readConfig=computed(()=>({
 audits:{title:'업무 감사',url:'/api/bza/backoffice/audits?limit=100',columns:['createdAt','actorId','actionType','targetType','targetId','reason']},
 settings:{title:'업무 설정',url:'/api/bza/settings',columns:['settingKey','settingValue','valueType','encryptedYn','useYn']}
} as Record<string,{title:string;url:string;columns:string[]}>)[props.routeId]);
async function load(){if(props.routeId==='savedSearches'||props.routeId==='notifications')return;loading.value=true;error.value='';try{if(props.routeId==='sessions')rows.value=await bzaApi('/api/bza/auth/sessions?limit=50');else if(props.routeId==='attachments')rows.value=await bzaApi('/api/bza/attachments?groupId=GENERAL');else if(props.routeId==='downloads'){const [a,b]=await Promise.all([bzaApi('/api/bza/downloads'),bzaApi('/api/bza/download-audits?limit=100')]);rows.value=a as Record<string,unknown>[];secondary.value=b as Record<string,unknown>[];}else if(readConfig.value)rows.value=await bzaApi(readConfig.value.url);}catch(e){error.value=e instanceof Error?e.message:String(e);}finally{loading.value=false;}}
async function revokeSession(item:Record<string,unknown>){const reason=prompt('세션 폐기 사유를 입력하세요.');if(!reason)return;await bzaApi(`/api/bza/auth/sessions/${item.sessionId}/revoke?reason=${encodeURIComponent(reason)}`,{method:'POST'});await load();}
async function upload(event:Event){const form=event.currentTarget as HTMLFormElement;const data=new FormData(form);try{await bzaApi('/api/bza/attachments',{method:'POST',body:data});form.reset();await load();}catch(e){error.value=e instanceof Error?e.message:String(e);}}
const savedFields=[{name:'screenCode',label:'화면 코드',required:true},{name:'searchName',label:'검색명'},{name:'criteriaJson',label:'검색 조건 JSON',type:'textarea' as const},{name:'sharedYn',label:'공유 여부',type:'yn' as const}];
const notificationFields=[{name:'recipientLoginId',label:'수신 로그인 ID',required:true},{name:'notificationType',label:'알림 유형'},{name:'title',label:'제목'},{name:'messageBody',label:'내용',type:'textarea' as const},{name:'referenceType',label:'참조 유형'},{name:'referenceId',label:'참조 ID'}];
onMounted(load);watch(()=>props.routeId,load);
</script>
<template>
  <p v-if="error" class="error-banner">{{error}}</p>
  <CrudTable v-if="routeId==='savedSearches'" title="저장 검색" endpoint="/api/bza/saved-searches" menu-code="SAVED_SEARCH" :columns="['screenCode','searchName','criteriaJson','sharedYn','createdBy','updatedAt']" :fields="savedFields" />
  <CrudTable v-else-if="routeId==='notifications'" title="알림" endpoint="/api/bza/notifications?limit=100" write-endpoint="/api/bza/notifications" menu-code="NOTIFICATION" :columns="['createdAt','notificationType','title','messageBody','readYn']" :fields="notificationFields" />
  <section v-else-if="routeId==='sessions'" class="card table-card"><div class="card-head"><div><p class="eyebrow">SESSION</p><h2>내 Refresh 세션</h2></div></div><div class="table-wrap"><table><thead><tr><th>sessionId</th><th>transactionId</th><th>createdAt</th><th>expiresAt</th><th>revokedYn</th><th>관리</th></tr></thead><tbody><tr v-for="item in rows" :key="String(item.sessionId)"><td>{{item.sessionId}}</td><td>{{item.transactionId}}</td><td>{{item.createdAt}}</td><td>{{item.expiresAt}}</td><td>{{item.revokedYn}}</td><td><button v-if="item.revokedYn!=='Y'" class="ghost" @click="revokeSession(item)">폐기</button></td></tr></tbody></table></div></section>
  <div v-else-if="routeId==='attachments'" class="page-stack"><section v-if="hasBzaPermission('ATTACHMENT','WRITE')" class="card"><div class="card-head"><div><p class="eyebrow">UPLOAD</p><h2>첨부파일 업로드</h2></div></div><form class="form-grid" @submit.prevent="upload"><label><span>그룹 ID</span><input name="groupId" value="GENERAL" required></label><label><span>감사 사유</span><input name="reason" required></label><label class="wide"><span>파일</span><input name="file" type="file" required></label><div class="form-action"><button class="primary">업로드</button></div></form></section><DataTable title="첨부파일" :rows="rows" :columns="['attachmentId','attachmentGroupId','originalFileName','contentType','fileSize','checksumSha256','scanStatus','createdAt']" :loading="loading" /></div>
  <div v-else-if="routeId==='downloads'" class="page-stack"><DataTable title="다운로드 정책" :rows="rows" :columns="['policyKey','policyValue','description','useYn']" :loading="loading"/><DataTable title="다운로드 감사" :rows="secondary" :columns="['createdAt','actorId','downloadCode','fileName','resultStatus','reason']" /></div>
  <DataTable v-else-if="readConfig" :title="readConfig.title" :rows="rows" :columns="readConfig.columns" :loading="loading" />
</template>
