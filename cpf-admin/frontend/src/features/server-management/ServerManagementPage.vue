<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { z } from 'zod'
import { useRoute, useRouter } from 'vue-router'
import type { ColumnDef } from '@tanstack/vue-table'
import CpfDataTable from '../../components/ui/CpfDataTable.vue'
import CpfValidatedForm from '../../components/ui/CpfValidatedForm.vue'
import CpfStructuredData from '../../components/CpfStructuredData.vue'
import RuntimeInventorySelector from '../../components/RuntimeInventorySelector.vue'
import { admManagedServerDisable, admManagedServerFindAll, admManagedServerFindOne, admManagedServerSave, admRuntimeInventoryFindAll } from '../../generated/orval/cpf-api'
import type { AdmManagedServerSaveRequest } from '../../generated/orval/model/admManagedServerSaveRequest'

type Row=Record<string,any>
const route=useRoute(),router=useRouter()
const servers=ref<Row[]>([]), runtimes=ref<Row[]>([]), selected=ref<Row|null>(null), loading=ref(false), error=ref(''), notice=ref('')
const filters=reactive({environment:'',status:'',keyword:'',capability:''})
const page=ref(1), size=ref(50)
const form=reactive<AdmManagedServerSaveRequest>({serverName:'',displayName:'',hostname:'',managementIdentity:'',environment:'',serverGroup:'',zone:'',location:'',description:'',tagsJson:'',reason:'Managed Server 등록/수정'})
const serverSchema=z.object({serverName:z.string().trim().min(1,'Server Name은 필수입니다.'),environment:z.string().trim().min(1,'Environment는 필수입니다.'),reason:z.string().trim().min(5,'감사 사유를 5자 이상 입력하세요.')}).passthrough() as unknown as z.ZodType<AdmManagedServerSaveRequest>
const runtimeId=ref(String(route.query.runtime??''))
const columns:ColumnDef<Row,unknown>[]=[
  {id:'name',header:'Server',cell:ctx=>h('button',{class:'link-button',onClick:()=>select(ctx.row.original)},String(ctx.row.original.displayName||ctx.row.original.serverName||ctx.row.original.managedServerId))},
  {accessorKey:'environment',header:'Environment'}, {accessorKey:'status',header:'Status'}, {accessorKey:'hostname',header:'Hostname'},
  {accessorKey:'runtimeCount',header:'Runtimes'}, {accessorKey:'activeRuntimeCount',header:'Active'}, {accessorKey:'serverGroup',header:'Group'}
]
const pagedServers=computed(()=>servers.value.slice((page.value-1)*size.value,page.value*size.value))
async function load(){ loading.value=true;error.value=''; try{
  const [s,r]=await Promise.all([admManagedServerFindAll({environment:filters.environment||undefined,status:filters.status||undefined,keyword:filters.keyword||undefined,limit:500}),admRuntimeInventoryFindAll({environment:filters.environment||undefined,capability:filters.capability||undefined,status:undefined,keyword:filters.keyword||undefined,limit:1000})])
  servers.value=Array.isArray(s.data)?s.data as Row[]:[]; runtimes.value=Array.isArray(r.data)?r.data as Row[]:[]
  const requested=String(route.query.server??''); if(requested&&!selected.value){const found=servers.value.find(v=>String(v.managedServerId)===requested);if(found)await select(found)}
 }catch(cause){error.value=cause instanceof Error?cause.message:String(cause)}finally{loading.value=false}}
async function select(row:Row){ const id=String(row.managedServerId||''); if(!id)return; const response=await admManagedServerFindOne(id); selected.value=response.data as Row; Object.assign(form,{managedServerId:id,serverName:selected.value.serverName||'',displayName:selected.value.displayName||'',hostname:selected.value.hostname||'',managementIdentity:selected.value.managementIdentity||'',environment:selected.value.environment||'',serverGroup:selected.value.serverGroup||'',zone:selected.value.zone||'',location:selected.value.location||'',description:selected.value.description||'',tagsJson:selected.value.tagsJson||'',expectedVersion:Number(selected.value.rowVersion||0),reason:'Managed Server 수정'}); await router.replace({query:{...route.query,server:id}})}
function clearForm(){ selected.value=null;Object.assign(form,{managedServerId:undefined,serverName:'',displayName:'',hostname:'',managementIdentity:'',environment:'',serverGroup:'',zone:'',location:'',description:'',tagsJson:'',expectedVersion:undefined,reason:'Managed Server 등록'});void router.replace({query:{runtime:runtimeId.value||undefined}})}
async function save(){ if(!String(form.serverName||'').trim()||!String(form.environment||'').trim()||!String(form.reason||'').trim()){error.value='Server Name, Environment, 사유는 필수입니다.';return} loading.value=true;error.value='';try{const response=await admManagedServerSave(form);notice.value='Managed Server가 저장되었습니다.';await load();await select(response.data as Row)}catch(cause){error.value=cause instanceof Error?cause.message:String(cause)}finally{loading.value=false}}
async function disable(){if(!selected.value)return;if(!String(form.reason||'').trim()){error.value='비활성화 사유가 필요합니다.';return}loading.value=true;try{await admManagedServerDisable(String(selected.value.managedServerId),{expectedVersion:Number(selected.value.rowVersion),reason:String(form.reason)});notice.value='Managed Server가 DISABLED로 전환되었습니다.';clearForm();await load()}catch(cause){error.value=cause instanceof Error?cause.message:String(cause)}finally{loading.value=false}}
onMounted(load)
</script>
<template>
<div class="cpf-page">
  <div class="cpf-page-heading"><div><p class="eyebrow">CENTRAL SERVER REGISTRY</p><h2>서버 관리 · Runtime Inventory</h2><p>Server는 한 번 등록하고 Gateway·Batch·Logging·Health·Configuration에서 동일 Identity를 참조합니다.</p></div><button type="button" class="ghost" :disabled="loading" @click="load">새로고침</button></div>
  <p v-if="error" class="cpf-alert danger" role="alert">{{ error }}</p><p v-if="notice" class="cpf-alert success" role="status">{{ notice }}</p>
  <section class="cpf-card"><div class="cpf-form-grid"><label>Environment<input v-model.trim="filters.environment"></label><label>Status<select v-model="filters.status"><option value="">ALL</option><option>REGISTERED</option><option>ACTIVE</option><option>DISABLED</option><option>DECOMMISSIONED</option><option>UNKNOWN</option></select></label><label>Capability<input v-model.trim="filters.capability" placeholder="FILE_LOGGING / BATCH_AGENT"></label><label>Keyword<input v-model.trim="filters.keyword" @keyup.enter="load"></label><button class="primary" @click="load">조회</button><button @click="clearForm">신규 등록</button></div></section>
  <section class="server-grid"><div class="cpf-card table-card"><CpfDataTable :rows="pagedServers" :columns="columns" :total="servers.length" :page="page" :size="size" :loading="loading" :row-key="row=>String(row.managedServerId)" @page="page=$event" @size="size=$event" @row="select" /></div>
  <aside class="cpf-card"><h3>{{ selected?'Server Detail':'Server 등록' }}</h3><CpfValidatedForm :schema="serverSchema" :model="form" :on-submit="save" :disabled="loading" submit-label="저장"><template #default="{errors}"><div class="cpf-form-grid one"><label>Server Name<input v-model.trim="form.serverName"><small v-if="errors.serverName" class="form-error">{{errors.serverName}}</small></label><label>Display Name<input v-model.trim="form.displayName"></label><label>Environment<input v-model.trim="form.environment"><small v-if="errors.environment" class="form-error">{{errors.environment}}</small></label><label>Group<input v-model.trim="form.serverGroup"></label><label>Hostname<input v-model.trim="form.hostname"></label><label>Management Identity<input v-model.trim="form.managementIdentity"></label><label>Zone<input v-model.trim="form.zone"></label><label>Location<input v-model.trim="form.location"></label><label>Description<textarea v-model.trim="form.description"></textarea></label><label>Tags JSON<textarea v-model.trim="form.tagsJson"></textarea></label><label>사유<input v-model.trim="form.reason"><small v-if="errors.reason" class="form-error">{{errors.reason}}</small></label></div></template></CpfValidatedForm><div class="actions"><button v-if="selected" class="danger-button" :disabled="loading" @click="disable">비활성화</button></div><CpfStructuredData v-if="selected" :value="selected" /></aside></section>
  <section class="cpf-card"><div class="panel-title"><div><h3>Runtime Inventory</h3><p>Stable Managed Server와 ephemeral Runtime/Capability를 분리해 조회합니다.</p></div></div><RuntimeInventorySelector v-model="runtimeId" :environment="filters.environment" :capability="filters.capability" /><div class="table-wrap"><table class="cpf-table"><thead><tr><th>Instance</th><th>Server</th><th>System</th><th>Application</th><th>Role</th><th>Status</th><th>Hostname</th><th>Last Seen</th></tr></thead><tbody><tr v-for="row in runtimes" :key="row.instanceId"><td>{{ row.instanceId }}</td><td><button v-if="row.managedServerId" class="link-button" @click="select(servers.find(v=>v.managedServerId===row.managedServerId)||row)">{{ row.serverName||row.managedServerId }}</button><span v-else>미연결</span></td><td>{{ row.systemCode||'-' }}</td><td>{{ row.applicationName||'-' }}</td><td>{{ row.applicationRole||'-' }}</td><td>{{ row.status||'UNKNOWN' }}</td><td>{{ row.runtimeHostname||'-' }}</td><td>{{ row.lastSeenAt||'-' }}</td></tr><tr v-if="!runtimes.length"><td colspan="8" class="cpf-empty">Runtime Inventory가 없습니다.</td></tr></tbody></table></div></section>
</div>
</template>
<style scoped>
.server-grid{display:grid;grid-template-columns:minmax(0,1.6fr) minmax(20rem,.8fr);gap:1rem}.table-card{min-height:30rem}.one{grid-template-columns:1fr}.actions{display:flex;gap:.5rem}.danger-button{color:var(--el-color-danger,#f56c6c)}.link-button{border:0;background:transparent;text-decoration:underline;color:var(--el-color-primary,#409eff)}textarea{min-height:4rem}.success{color:var(--el-color-success,#67c23a)}.form-error{color:var(--el-color-danger,#f56c6c)}@media(max-width:1000px){.server-grid{grid-template-columns:1fr}}
</style>
