<script setup lang="ts">
import { computed,onMounted,onUnmounted,reactive,ref,watch } from "vue";
import { bzaApi,hasBzaPermission } from "../features/auth/session";
export interface CrudField{
  name:string;label:string;type?:string;sourceName?:string;required?:boolean;readonlyOnEdit?:boolean;
  options?:string[];defaultValue?:unknown;preserveOnBlankEdit?:boolean;
}
interface Page<T>{content:T[];totalElements:number;page:number;size:number;}
const props=withDefaults(defineProps<{
  title:string;endpoint:string;writeEndpoint?:string;menuCode:string;columns:string[];fields:CrudField[];
  paged?:boolean;pageSize?:number;rawEndpointTemplate?:string;rawIdField?:string;rawFields?:string[];
}>(),{paged:true,pageSize:20,rawIdField:"employeeNo"});
const rows=ref<Record<string,unknown>[]>([]),loading=ref(false),dialogOpen=ref(false),message=ref(""),error=ref("");
const rawOpen=ref(false),rawReason=ref(""),rawLoading=ref(false),rawError=ref(""),rawRecord=ref<Record<string,unknown>>({}),rawRow=ref<Record<string,unknown>|null>(null);
const page=ref(0),size=ref(props.pageSize),total=ref(0),editing=ref(false);const form=reactive<Record<string,any>>({});
const writable=computed(()=>hasBzaPermission(props.menuCode,"WRITE"));
const rawReadable=computed(()=>!!props.rawEndpointTemplate&&hasBzaPermission(props.menuCode,"PII_RAW"));
const totalPages=computed(()=>total.value===0?0:Math.ceil(total.value/size.value));
const columnLabel=(column:string)=>props.fields.find(field=>field.name===column||field.sourceName===column)?.label||column;
async function load(){loading.value=true;error.value="";try{if(props.paged){const sep=props.endpoint.includes("?")?"&":"?";const url=`${props.endpoint.replace(/\/$/,"")}/page${sep}page=${page.value}&size=${size.value}`;const result=await bzaApi<Page<Record<string,unknown>>>(url);rows.value=result.content||[];total.value=Number(result.totalElements||0);}else{rows.value=await bzaApi<Record<string,unknown>[]>(props.endpoint);total.value=rows.value.length;}}catch(e){error.value=e instanceof Error?e.message:String(e);rows.value=[];}finally{loading.value=false;}}
function open(item:Record<string,unknown>={}){for(const k of Object.keys(form))delete form[k];editing.value=Object.keys(item).length>0;for(const f of props.fields){
  const source=item[f.sourceName||f.name];
  form[f.name]=editing.value&&f.preserveOnBlankEdit?"":(source??f.defaultValue??(f.type==="yn"?"Y":""));
}for(const id of ["permissionId","assignmentId","responsibilityId","userRoleId"])if(item[id]!=null)form[id]=item[id];if(item.versionNo!=null)form.expectedVersion=item.versionNo;form.reason="업무 기준정보 변경";message.value="";dialogOpen.value=true;}
async function save(){
  const payload:Record<string,unknown>={...form};
  for(const f of props.fields){
    const value=payload[f.name];
    if(f.type==="password"&&(value===""||value==null)){delete payload[f.name];continue;}
    if(editing.value&&f.preserveOnBlankEdit&&typeof value==="string"&&value.trim()===""){delete payload[f.name];continue;}
    if(f.type==="number"){payload[f.name]=value===""||value==null?null:Number(value);continue;}
    if(typeof value==="string"){const trimmed=value.trim();payload[f.name]=trimmed===""?null:trimmed;}
  }
  if("operationId" in payload && !payload.operationId)payload.operationId=crypto.randomUUID();
  try{await bzaApi(props.writeEndpoint||props.endpoint,{method:"POST",body:JSON.stringify(payload)});dialogOpen.value=false;await load();}
  catch(e){message.value=e instanceof Error?e.message:String(e);}
}
function clearRaw(){
  rawRecord.value={};rawRow.value=null;rawReason.value="";rawError.value="";rawLoading.value=false;rawOpen.value=false;
}
function requestRaw(row:Record<string,unknown>){clearRaw();rawRow.value=row;rawOpen.value=true;}
async function loadRaw(){
  if(!rawRow.value||!props.rawEndpointTemplate||!rawReason.value.trim()){rawError.value="원문 조회 사유를 입력하세요.";return;}
  const id=String(rawRow.value[props.rawIdField]??"");
  if(!id){rawError.value="원문 조회 대상 식별자를 확인할 수 없습니다.";return;}
  rawLoading.value=true;rawError.value="";rawRecord.value={};
  try{
    const endpoint=props.rawEndpointTemplate.replace("{id}",encodeURIComponent(id));
    rawRecord.value=await bzaApi<Record<string,unknown>>(endpoint,{
      method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({reason:rawReason.value.trim()})
    });
  }catch(e){rawRecord.value={};rawError.value=e instanceof Error?e.message:String(e);}
  finally{rawLoading.value=false;}
}
function prev(){if(page.value>0){page.value--;load();}}function next(){if((page.value+1)*size.value<total.value){page.value++;load();}}
onMounted(load);onUnmounted(clearRaw);watch(()=>props.endpoint,()=>{clearRaw();page.value=0;load();});
</script>
<template><section class="card table-card"><div class="card-head"><div><p class="eyebrow">MANAGEMENT</p><h2>{{title}}</h2></div><div class="inline-actions"><span class="count-pill">{{total}}건</span><button v-if="writable" class="primary" type="button" @click="open()">등록</button></div></div><p v-if="error" class="error-banner">{{error}}</p><div v-if="loading" class="empty-state">조회 중...</div><div v-else class="table-wrap"><table><thead><tr><th v-for="c in columns" :key="c">{{columnLabel(c)}}</th><th v-if="writable||rawReadable">관리</th></tr></thead><tbody><tr v-for="(row,index) in rows" :key="String(row.id||row.permissionId||row.assignmentId||row.organizationCode||row.employeeNo||index)"><td v-for="c in columns" :key="c">{{row[c]??"-"}}</td><td v-if="writable||rawReadable"><button v-if="writable" type="button" class="ghost" @click="open(row)">수정</button><button v-if="rawReadable" type="button" class="ghost" @click="requestRaw(row)">원문 보기</button></td></tr><tr v-if="rows.length===0"><td :colspan="columns.length+((writable||rawReadable)?1:0)" class="empty-cell">조회 결과가 없습니다.</td></tr></tbody></table></div><div v-if="paged" class="pager"><button class="ghost" :disabled="page===0" @click="prev">이전</button><span>{{page+1}} / {{Math.max(totalPages,1)}}</span><button class="ghost" :disabled="(page+1)*size>=total" @click="next">다음</button></div>
<dialog :open="dialogOpen" class="modal"><form class="modal-card" @submit.prevent="save"><div class="card-head"><div><p class="eyebrow">EDIT</p><h2>{{title}} 등록·수정</h2></div><button type="button" class="icon-button" @click="dialogOpen=false">×</button></div><div class="form-grid"><label v-for="f in fields" :key="f.name" :class="{wide:f.type==='textarea'}"><span>{{f.label}}</span><textarea v-if="f.type==='textarea'" v-model="form[f.name]" rows="4" :required="f.required"></textarea><select v-else-if="f.type==='yn'" v-model="form[f.name]" :disabled="editing&&f.readonlyOnEdit"><option value="Y">Y</option><option value="N">N</option></select><select v-else-if="f.options" v-model="form[f.name]" :required="f.required" :disabled="editing&&f.readonlyOnEdit"><option v-for="option in f.options" :key="option" :value="option">{{option}}</option></select><input v-else-if="f.type==='boolean'" v-model="form[f.name]" class="checkbox-input" type="checkbox"><input v-else v-model="form[f.name]" :type="f.type||'text'" :required="f.required" :readonly="editing&&f.readonlyOnEdit"></label><label class="wide"><span>감사 사유</span><textarea v-model="form.reason" rows="3" required></textarea></label></div><p v-if="message" class="error-banner">{{message}}</p><div class="dialog-actions"><button type="button" class="ghost" @click="dialogOpen=false">취소</button><button type="submit" class="primary">저장</button></div></form></dialog>
<dialog :open="rawOpen" class="modal"><form class="modal-card" @submit.prevent="loadRaw"><div class="card-head"><div><p class="eyebrow">PII RAW</p><h2>원문 연락처 조회</h2></div><button type="button" class="icon-button" @click="clearRaw">×</button></div><p class="hint">원문 조회는 별도 권한과 사유가 필요하며 감사 로그에 기록됩니다.</p><label><span>조회 사유</span><textarea v-model="rawReason" rows="3" required></textarea></label><p v-if="rawError" class="error-banner">{{rawError}}</p><div v-if="rawLoading" class="empty-state">조회 중...</div><dl v-else-if="Object.keys(rawRecord).length" class="detail-list"><template v-for="key in (rawFields||Object.keys(rawRecord))" :key="key"><dt>{{columnLabel(key)}}</dt><dd>{{rawRecord[key]??"-"}}</dd></template></dl><div class="dialog-actions"><button type="button" class="ghost" @click="clearRaw">닫기</button><button type="submit" class="primary">원문 조회</button></div></form></dialog></section></template>
