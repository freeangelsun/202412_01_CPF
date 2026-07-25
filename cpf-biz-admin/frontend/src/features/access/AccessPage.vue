<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import CrudTable, { type CrudField } from "../../components/CrudTable.vue";
import DataTable from "../../components/DataTable.vue";
import { bzaApi } from "../auth/session";
const props=defineProps<{routeId:string}>();

type Config={title:string;endpoint:string;menuCode:string;columns:string[];fields:CrudField[]};
const configs:Record<string,Config>={
 users:{title:"사용자",endpoint:"/api/bza/admin-users",menuCode:"USER",columns:["adminLoginId","adminName","roleCode","useYn","lockYn","lastLoginAt"],fields:[{name:"loginId",sourceName:"adminLoginId",label:"로그인 ID",required:true},{name:"adminName",label:"사용자명"},{name:"roleCode",label:"역할 코드"},{name:"rawPassword",label:"신규 비밀번호",type:"password"},{name:"useYn",label:"사용 여부",type:"yn"},{name:"lockYn",label:"잠금 여부",type:"yn"},{name:"passwordChangeRequiredYn",label:"비밀번호 변경 필요",type:"yn"}]},
 roles:{title:"역할",endpoint:"/api/bza/roles",menuCode:"ROLE",columns:["roleCode","roleName","writeAllowedYn","dataScope","useYn"],fields:[{name:"roleCode",label:"역할 코드",required:true},{name:"roleName",label:"역할명"},{name:"writeAllowedYn",label:"쓰기 허용",type:"yn"},{name:"dataScope",label:"데이터 범위"},{name:"useYn",label:"사용 여부",type:"yn"}]},
 menus:{title:"메뉴",endpoint:"/api/bza/menus",menuCode:"MENU",columns:["menuCode","menuName","moduleCode","routePath","apiPath","useYn"],fields:[{name:"menuCode",label:"메뉴 코드",required:true},{name:"menuName",label:"메뉴명"},{name:"parentMenuCode",label:"상위 메뉴"},{name:"moduleCode",label:"모듈 코드"},{name:"routePath",label:"화면 경로"},{name:"iconCode",label:"아이콘 코드"},{name:"environmentCode",label:"환경 코드"},{name:"apiPath",label:"API 경로"},{name:"sortOrder",label:"정렬 순서",type:"number"},{name:"useYn",label:"사용 여부",type:"yn"}]},
 permissions:{title:"권한",endpoint:"/api/bza/permissions",menuCode:"PERMISSION",columns:["roleCode","menuCode","buttonCode","permissionType","httpMethod","allowYn"],fields:[{name:"roleCode",label:"역할 코드"},{name:"menuCode",label:"메뉴 코드"},{name:"buttonCode",label:"버튼/행위 코드",required:true},{name:"permissionType",label:"권한 유형"},{name:"httpMethod",label:"HTTP 메서드"},{name:"apiPattern",label:"API 경로 패턴"},{name:"domainCode",label:"업무 영역"},{name:"environmentCode",label:"환경 코드"},{name:"dataScope",label:"데이터 범위"},{name:"allowYn",label:"허용 여부",type:"yn"},{name:"useYn",label:"사용 여부",type:"yn"}]}
};
const config=computed(()=>configs[props.routeId]);
const compare=reactive({leftRoleCode:"",rightRoleCode:""}); const simulation=reactive({roleCode:"",menuCode:"",actionCode:"",httpMethod:"GET",apiPath:"/api/bza/",environmentCode:"ALL",domainCode:"BZA",reason:"권한 시뮬레이션"});
const compareRows=ref<Record<string,unknown>[]>([]); const simulationResult=ref<unknown>(null); const error=ref("");
async function compareRoles(){error.value="";try{compareRows.value=await bzaApi<Record<string,unknown>[]>(`/api/bza/permissions/compare?leftRoleCode=${encodeURIComponent(compare.leftRoleCode)}&rightRoleCode=${encodeURIComponent(compare.rightRoleCode)}`);}catch(e){error.value=e instanceof Error?e.message:String(e);}}
async function simulate(){error.value="";try{simulationResult.value=await bzaApi('/api/bza/permissions/simulate',{method:'POST',body:JSON.stringify(simulation)});}catch(e){error.value=e instanceof Error?e.message:String(e);}}
</script>
<template>
  <CrudTable v-if="config" :title="config.title" :endpoint="config.endpoint" :menu-code="config.menuCode" :columns="config.columns" :fields="config.fields" />
  <div v-else class="page-stack">
    <p v-if="error" class="error-banner">{{error}}</p>
    <section class="card"><div class="card-head"><div><p class="eyebrow">COMPARE</p><h2>역할 권한 비교</h2></div></div><form class="form-grid compact" @submit.prevent="compareRoles"><label><span>기준 역할</span><input v-model="compare.leftRoleCode" required></label><label><span>비교 역할</span><input v-model="compare.rightRoleCode" required></label><div class="form-action"><button class="primary">비교</button></div></form></section>
    <DataTable title="권한 차이" :rows="compareRows" :columns="['permissionKey','leftRoleCode','rightRoleCode','different']" />
    <section class="card"><div class="card-head"><div><p class="eyebrow">SIMULATION</p><h2>권한 시뮬레이션</h2></div></div><form class="form-grid" @submit.prevent="simulate"><label><span>역할</span><input v-model="simulation.roleCode" required></label><label><span>메뉴</span><input v-model="simulation.menuCode" required></label><label><span>행위</span><input v-model="simulation.actionCode" required></label><label><span>HTTP</span><input v-model="simulation.httpMethod" required></label><label class="wide"><span>API 경로</span><input v-model="simulation.apiPath" required></label><label><span>환경</span><input v-model="simulation.environmentCode" required></label><label><span>업무 영역</span><input v-model="simulation.domainCode" required></label><label class="wide"><span>감사 사유</span><textarea v-model="simulation.reason" required></textarea></label><div class="form-action"><button class="primary">시뮬레이션</button></div></form><pre v-if="simulationResult" class="json-block">{{JSON.stringify(simulationResult,null,2)}}</pre></section>
  </div>
</template>
