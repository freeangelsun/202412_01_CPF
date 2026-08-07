<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import CpfStructuredData from "./CpfStructuredData.vue";
import { cpfOperationDescriptors, type CpfOperationId } from "../generated/cpf-operation-contract";
import { admInvokeOperation } from "../shared/cpfApi";
import { useAdmSessionStore } from "../stores/admSessionStore";

const props=defineProps<{ title:string; operationIds:readonly CpfOperationId[] }>();
const sessionStore = useAdmSessionStore();
const selectedId=ref<CpfOperationId|"">("");
const pathValues=reactive<Record<string,string>>({});
const queryText=ref("{}");
const result=ref<unknown>(null);
const error=ref("");
const loading=ref(false);
const open=ref(false);
const descriptors=computed(()=>props.operationIds.map(id=>cpfOperationDescriptors.find(item=>item.operationId===id)).filter((item):item is (typeof cpfOperationDescriptors)[number]=>Boolean(item) && item.method === "GET"));
const selected=computed(()=>descriptors.value.find(item=>item.operationId===selectedId.value));
const pathNames=computed(()=>Array.from(selected.value?.template.matchAll(/\{([^}]+)\}/g)||[],item=>item[1]));
const canExecute = computed(() => Boolean(selected.value && sessionStore.hasButton(selected.value.operationId)));
function resetInputs(){for(const key of Object.keys(pathValues))delete pathValues[key];queryText.value="{}";result.value=null;error.value="";}
watch(()=>props.operationIds,()=>{selectedId.value=descriptors.value[0]?.operationId||"";resetInputs();},{immediate:true});
watch(selectedId,resetInputs);
function parseObject(value:string,label:string):Record<string,unknown>{const trimmed=value.trim();if(!trimmed)return {};const parsed=JSON.parse(trimmed);if(!parsed||Array.isArray(parsed)||typeof parsed!=="object")throw new Error(`${label}는 JSON Object 형식이어야 합니다.`);return parsed as Record<string,unknown>;}
async function execute(){
 const descriptor=selected.value;if(!descriptor)return;
 loading.value=true;error.value="";result.value=null;
 try{
  const path=Object.fromEntries(pathNames.value.map(name=>[name,pathValues[name]]));
  const query=parseObject(queryText.value,"Query");
  if (descriptor.method !== "GET") throw new Error("범용 Workbench에서는 상태변경 Operation을 실행할 수 없습니다. 전용 화면을 사용하세요.");
  if (!sessionStore.hasButton(descriptor.operationId)) throw new Error(`권한 없음: ${descriptor.operationId}`);
  result.value=await admInvokeOperation(descriptor.operationId,{path,query});
 }catch(value){error.value=value instanceof Error?value.message:String(value);}finally{loading.value=false;}
}
</script>
<template>
<section v-if="descriptors.length" class="card operation-workbench">
 <header class="card-head"><div><p class="eyebrow">GENERATED READ OPERATION</p><h2>{{title}} 고급 조회</h2><p>전용 화면에서 제공하지 않는 조회 Operation만 Generated Contract로 실행합니다. 상태변경·복구 조치는 권한·검증·감사를 강제하는 전용 화면에서만 수행합니다.</p></div><button type="button" class="ghost" @click="open=!open">{{open?'접기':'열기'}}</button></header>
 <div v-if="open" class="operation-body">
  <label><span>Operation</span><select v-model="selectedId"><option v-for="item in descriptors" :key="item.operationId" :value="item.operationId">{{item.method}} · {{item.operationId}}</option></select></label>
  <p v-if="selected" class="operation-path"><strong>{{selected.method}}</strong> {{selected.template}}</p>
  <div v-if="pathNames.length" class="form-grid"><label v-for="name in pathNames" :key="name"><span>{{name}}</span><input v-model="pathValues[name]" required></label></div>
  <div class="form-grid"><label class="wide"><span>Query JSON</span><textarea v-model="queryText" rows="4" spellcheck="false"></textarea></label></div>
  <p class="hint">쓰기·삭제·상태전이 Operation은 범용 JSON 실행 경로에서 의도적으로 제외됩니다.</p>
  <p v-if="error" class="error-banner" role="alert">{{error}}</p>
  <CpfStructuredData v-if="result!==null" class="detail" :value="result" />
  <div class="dialog-actions"><button type="button" class="primary" :disabled="loading||!selected||!canExecute" :title="selected && !canExecute ? `권한 없음: ${selected.operationId}` : ''" @click="execute">{{loading?'실행 중...':'실행'}}</button></div>
 </div>
</section>
</template>
