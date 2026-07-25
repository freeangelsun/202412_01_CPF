<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import CpfIcon from "../../components/CpfIcon.vue";
import { admApi } from "../../shared/cpfApi";
type Row = Record<string, any>;
const rows = ref<Row[]>([]); const error=ref(""); const busy=ref(false);
const form=ref({scopeType:"SERVICE",scopeValue:"",reason:"긴급 장애 복구",ttlMinutes:15});
const activeCount=computed(()=>rows.value.filter(r=>r.status==='ACTIVE').length);
async function load(){try{rows.value=await admApi('/adm/api/break-glass?limit=100')}catch(e){error.value=e instanceof Error?e.message:String(e)}}
async function open(){busy.value=true;error.value="";try{await admApi('/adm/api/break-glass',{method:'POST',body:JSON.stringify(form.value)});form.value.scopeValue='';await load()}catch(e){error.value=e instanceof Error?e.message:String(e)}finally{busy.value=false}}
async function closeSession(r:Row){const reason=window.prompt('Break-glass 종료 사유','긴급 조치 종료');if(!reason)return;await admApi(`/adm/api/break-glass/${r.sessionId}/close`,{method:'POST',body:JSON.stringify({reason})});await load()}
async function review(r:Row,status:string){const reason=window.prompt(`사후검토 ${status} 사유`);if(!reason)return;await admApi(`/adm/api/break-glass/${r.sessionId}/review`,{method:'POST',body:JSON.stringify({status,reason})});await load()}
onMounted(load);
</script>
<template><div class="cpf-page">
<div class="cpf-page-heading"><div><p class="eyebrow">EMERGENCY CONTROL</p><h2>Break-glass 통제</h2><p>전역 권한 우회가 아니라 좁은 Scope와 짧은 TTL을 가진 비상 세션을 발급하고 사후검토까지 추적합니다.</p></div><button class="ghost" @click="load"><CpfIcon name="refresh"/> 새로고침</button></div>
<section class="cpf-stat-grid"><article class="cpf-stat-card"><span>Active Session</span><strong>{{activeCount}}</strong><small>운영자별 1개 제한</small></article><article class="cpf-stat-card"><span>기본 TTL</span><strong>15m</strong><small>서버 최대값 초과 불가</small></article><article class="cpf-stat-card"><span>Post Review</span><strong>{{rows.filter(r=>r.postReviewStatus==='PENDING'&&r.status!=='ACTIVE').length}}</strong><small>종료 후 검토 대기</small></article></section>
<section class="cpf-card"><div class="cpf-card-head"><div><h2>비상 세션 발급</h2><p>사용 범위를 구체적으로 제한하세요. 실제 위험조치 우회는 Owner Command가 이 Scope를 명시적으로 소비해야 합니다.</p></div></div><div class="cpf-form-grid"><label>Scope<select v-model="form.scopeType"><option>SERVICE</option><option>BATCH</option><option>CENTER_CUT</option><option>RECOVERY</option><option>SECURITY</option></select></label><label>대상<input v-model="form.scopeValue" placeholder="MBR-01 / BATCH_JOB_ID"></label><label>TTL(분)<input v-model.number="form.ttlMinutes" type="number" min="1" max="30"></label><label class="span-2">긴급 사유<input v-model="form.reason"></label><button class="danger" :disabled="busy||!form.scopeValue" @click="open">Break-glass 발급</button></div><p v-if="error" class="error-banner">{{error}}</p></section>
<section class="cpf-card"><div class="cpf-card-head"><h2>세션·사후검토 이력</h2></div><div class="table-wrap"><table><thead><tr><th>운영자</th><th>Scope</th><th>대상</th><th>상태</th><th>만료</th><th>사후검토</th><th>조치</th></tr></thead><tbody><tr v-for="r in rows" :key="r.sessionId"><td>{{r.operatorId}}</td><td>{{r.scopeType}}</td><td>{{r.scopeValue}}</td><td><span class="cpf-status" :class="r.status==='ACTIVE'?'danger':'success'">{{r.status}}</span></td><td>{{r.expiresAt}}</td><td>{{r.postReviewStatus}}</td><td><div class="cpf-toolbar"><button v-if="r.status==='ACTIVE'" class="ghost small" @click="closeSession(r)">종료</button><template v-else-if="r.postReviewStatus==='PENDING'"><button class="primary small" @click="review(r,'APPROVED')">검토승인</button><button class="ghost small" @click="review(r,'REJECTED')">문제기록</button></template></div></td></tr></tbody></table></div></section>
</div></template>
