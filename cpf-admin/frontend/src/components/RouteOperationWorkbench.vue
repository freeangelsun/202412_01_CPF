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
// Generic workbench is intentionally read-only. Mutations must use dedicated typed screens
// so reason/approval/CAS/audit UX cannot be bypassed by arbitrary JSON/path execution.
const descriptors=computed(()=>props.operationIds
 .map(id=>cpfOperationDescriptors.find(item=>item.operationId===id))
 .filter((item):item is (typeof cpfOperationDescriptors)[number]=>Boolean(item))
 .filter(item=>item.method === "GET"));
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
  if(descriptor.method!=="GET")throw new Error("상태 변경 작업은 전용 화면에서만 실행할 수 있습니다.");
  if(!sessionStore.hasButton(descriptor.operationId))throw new Error(`권한 없음: ${descriptor.operationId}`);
  const path=Object.fromEntries(pathNames.value.map(name=>[name,pathValues[name]]));
  const query=parseObject(queryText.value,"Query");
  result.value=await admInvokeOperation(descriptor.operationId,{path,query});
 }catch(value){error.value=value instanceof Error?value.message:String(value);}
 finally{loading.value=false;}
}
</script>
<template>
<section v-if="descriptors.length" class="card operation-workbench">
 <header class="card-head"><div><p class="eyebrow">GENERATED OPERATION FALLBACK</p><h2>{{title}} 고급 작업</h2><p>전용 화면의 조회 보조 경로입니다. 현재 Route에 허용된 Generated GET Operation만 노출합니다. 상태 변경은 반드시 전용 화면에서 수행합니다.</p></div><button type="button" class="ghost" @click="open=!open">{{open?'접기':'열기'}}</button></header>
 <div v-if="open" class="operation-body">
  <label><span>Operation</span><select v-model="selectedId"><option v-for="item in descriptors" :key="item.operationId" :value="item.operationId">{{item.method}} · {{item.operationId}}</option></select></label>
  <p v-if="selected" class="operation-path"><strong>{{selected.method}}</strong> {{selected.template}}</p>
  <div v-if="pathNames.length" class="form-grid"><label v-for="name in pathNames" :key="name"><span>{{name}}</span><input v-model="pathValues[name]" required></label></div>
  <div class="form-grid"><label class="wide"><span>Query JSON</span><textarea v-model="queryText" rows="4" spellcheck="false"></textarea></label></div>
  <p class="hint">이 보조 경로는 조회(GET) 전용입니다. 승인·상태 변경·재처리 등 위험 작업은 전용 화면의 Typed Generated Client 흐름만 사용합니다.</p>
  <p v-if="error" class="error-banner" role="alert">{{error}}</p>
  <CpfStructuredData v-if="result!==null" class="detail" :value="result" />
  <div class="dialog-actions"><button type="button" class="primary" :disabled="loading||!selected||!canExecute" :title="selected && !canExecute ? `권한 없음: ${selected.operationId}` : ''" @click="execute">{{loading?'실행 중...':'실행'}}</button></div>
 </div>
</section>
</template>
