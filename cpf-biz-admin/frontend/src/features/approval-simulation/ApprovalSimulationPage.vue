<script setup lang="ts">
import { reactive, ref } from "vue";
import { bzaApi } from "../auth/session";
const form=reactive({policyCode:"",policyVersion:"",businessDomain:"BZA",approvalType:"GENERAL",requesterEmployeeNo:"",effectiveAt:""});
const result=ref<unknown>(null);const error=ref("");
async function run(){try{result.value=await bzaApi("/api/bza/approval/simulate",{method:"POST",body:JSON.stringify({...form,policyVersion:form.policyVersion?Number(form.policyVersion):null,effectiveAt:form.effectiveAt||null})});}catch(e){error.value=e instanceof Error?e.message:String(e);}}
</script>
<template><section class="card"><div class="card-head"><div><p class="eyebrow">FAIL-CLOSED PREVIEW</p><h2>결재 경로 Simulation</h2></div></div><form class="form-grid" @submit.prevent="run"><label><span>정책 코드(선택)</span><input v-model="form.policyCode"></label><label><span>버전(선택)</span><input v-model="form.policyVersion" type="number"></label><label><span>업무 영역</span><input v-model="form.businessDomain"></label><label><span>결재 유형</span><input v-model="form.approvalType"></label><label><span>요청 직원</span><input v-model="form.requesterEmployeeNo"></label><label><span>기준 시각</span><input v-model="form.effectiveAt"></label><div class="form-action"><button class="primary">Simulation</button></div></form><p v-if="error" class="error-banner">{{error}}</p><pre v-if="result" class="json-block">{{JSON.stringify(result,null,2)}}</pre></section></template>
