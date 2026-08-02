<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { cpfOperationDescriptors, type CpfOperationId } from "../generated/cpf-operation-contract";
import { bzaInvokeOperation } from "../shared/cpfApi";

const props=defineProps<{ title:string; operationIds:readonly CpfOperationId[] }>();
const selectedId=ref<CpfOperationId|"">("");
const pathValues=reactive<Record<string,string>>({});
const queryText=ref("{}");
const bodyText=ref("{}");
const result=ref<unknown>(null);
const error=ref("");
const loading=ref(false);
const open=ref(false);
const descriptors=computed(()=>props.operationIds.map(id=>cpfOperationDescriptors.find(item=>item.operationId===id)).filter((item):item is (typeof cpfOperationDescriptors)[number]=>Boolean(item)));
const selected=computed(()=>descriptors.value.find(item=>item.operationId===selectedId.value));
const pathNames=computed(()=>Array.from(selected.value?.template.matchAll(/\{([^}]+)\}/g)||[],item=>item[1]));
const dangerous=computed(()=>selected.value?.method!=="GET");
function resetInputs(){for(const key of Object.keys(pathValues))delete pathValues[key];queryText.value="{}";bodyText.value="{}";result.value=null;error.value="";}
watch(()=>props.operationIds,()=>{selectedId.value=descriptors.value[0]?.operationId||"";resetInputs();},{immediate:true});
watch(selectedId,resetInputs);
function parseObject(value:string,label:string):Record<string,unknown>{const trimmed=value.trim();if(!trimmed)return {};const parsed=JSON.parse(trimmed);if(!parsed||Array.isArray(parsed)||typeof parsed!=="object")throw new Error(`${label}는 JSON Object 형식이어야 합니다.`);return parsed as Record<string,unknown>;}
async function execute(){
 const descriptor=selected.value;if(!descriptor)return;
 if(dangerous.value&&!window.confirm(`${descriptor.method} ${descriptor.template} 조치를 실행하시겠습니까?`))return;
 loading.value=true;error.value="";result.value=null;
 try{
  const path=Object.fromEntries(pathNames.value.map(name=>[name,pathValues[name]]));
  const query=parseObject(queryText.value,"Query");
  const body=descriptor.method==="GET"?undefined:parseObject(bodyText.value,"Body");
  result.value=await bzaInvokeOperation(descriptor.operationId,{path,query,body});
 }catch(value){error.value=value instanceof Error?value.message:String(value);}finally{loading.value=false;}
}
</script>
<template>
<section v-if="descriptors.length" class="card operation-workbench">
 <header class="card-head"><div><p class="eyebrow">GENERATED OPERATION</p><h2>{{title}} 고급 작업</h2><p>전용 화면에서 제공하지 않는 상세 조회·상태 전이·복구 조치를 Generated Contract로 실행합니다.</p></div><button type="button" class="ghost" @click="open=!open">{{open?'접기':'열기'}}</button></header>
 <div v-if="open" class="operation-body">
  <label><span>Operation</span><select v-model="selectedId"><option v-for="item in descriptors" :key="item.operationId" :value="item.operationId">{{item.method}} · {{item.operationId}}</option></select></label>
  <p v-if="selected" class="operation-path"><strong>{{selected.method}}</strong> {{selected.template}}</p>
  <div v-if="pathNames.length" class="form-grid"><label v-for="name in pathNames" :key="name"><span>{{name}}</span><input v-model="pathValues[name]" required></label></div>
  <div class="form-grid"><label class="wide"><span>Query JSON</span><textarea v-model="queryText" rows="4" spellcheck="false"></textarea></label><label v-if="dangerous" class="wide"><span>Body JSON</span><textarea v-model="bodyText" rows="7" spellcheck="false"></textarea></label></div>
  <p v-if="dangerous" class="hint">위험 조치는 API 계약에 맞는 reason·expectedVersion·approvalId를 Query 또는 Body에 명시해야 하며 인증 사용자는 Browser에서 지정할 수 없습니다.</p>
  <p v-if="error" class="error-banner" role="alert">{{error}}</p>
  <CpfStructuredData v-if="result!==null" class="detail" :value="result" />
  <div class="dialog-actions"><button type="button" class="primary" :disabled="loading||!selected" @click="execute">{{loading?'실행 중...':'실행'}}</button></div>
 </div>
</section>
</template>
