<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { admRuntimeInventoryFindAll } from '../generated/orval/cpf-api'

type RuntimeRow = Record<string, any>
const props = withDefaults(defineProps<{ modelValue?: string; capability?: string; environment?: string; status?: string; disabled?: boolean }>(), { modelValue:'', capability:'', environment:'', status:'', disabled:false })
const emit = defineEmits<{ 'update:modelValue':[string]; selected:[RuntimeRow|null] }>()
const router = useRouter()
const rows = ref<RuntimeRow[]>([])
const loading = ref(false)
const error = ref('')
const selected = computed(() => rows.value.find(row => String(row.instanceId ?? row.instance_id ?? '') === props.modelValue) ?? null)

async function load(){
  loading.value=true; error.value=''
  try{
    const response=await admRuntimeInventoryFindAll({ environment:props.environment||undefined, capability:props.capability||undefined, status:props.status||undefined, page:0, size:200 })
    rows.value=Array.isArray(response.data.items)? response.data.items as RuntimeRow[] : []
    emit('selected', selected.value)
  }catch(cause){ error.value=cause instanceof Error?cause.message:String(cause) }
  finally{ loading.value=false }
}
function choose(value:string){ emit('update:modelValue',value); queueMicrotask(()=>emit('selected',selected.value)) }
function openServer(){ const row=selected.value; const id=String(row?.managedServerId ?? row?.managed_server_id ?? ''); if(id) void router.push({path:'/servers',query:{server:id,runtime:props.modelValue}}) }
watch(()=>[props.capability,props.environment,props.status],load)
onMounted(load)
</script>
<template>
  <div class="runtime-selector">
    <select :value="modelValue" :disabled="disabled||loading" @change="choose(($event.target as HTMLSelectElement).value)">
      <option value="">Runtime 선택</option>
      <option v-for="row in rows" :key="String(row.instanceId??row.instance_id)" :value="String(row.instanceId??row.instance_id)">
        {{ row.instanceId??row.instance_id }} · {{ row.serverName??row.server_name??'미연결 Server' }} · {{ row.status??'UNKNOWN' }}
      </option>
    </select>
    <button v-if="selected && (selected.managedServerId||selected.managed_server_id)" type="button" class="link-button" @click="openServer">Server 상세</button>
    <button type="button" :disabled="loading" @click="load">새로고침</button>
    <span v-if="error" class="selector-error" role="alert">{{ error }}</span>
  </div>
</template>
<style scoped>
.runtime-selector{display:flex;gap:.5rem;align-items:center;flex-wrap:wrap}.runtime-selector select{min-width:18rem;max-width:100%}.link-button{border:0;background:transparent;text-decoration:underline;color:var(--el-color-primary,#409eff)}.selector-error{color:var(--el-color-danger,#f56c6c);font-size:.85rem}@media(max-width:640px){.runtime-selector{display:grid}.runtime-selector select{min-width:0;width:100%}}
</style>
