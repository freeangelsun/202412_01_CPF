<script setup lang="ts">
import {computed,onBeforeUnmount,ref,watch} from 'vue'
import type {ParameterDefinition} from './parameterSchema'
import {admInvokeOperation} from '../../shared/cpfApi'

const props=defineProps<{definition:ParameterDefinition;modelValue:unknown;context:Record<string,unknown>;disabled?:boolean}>()
const emit=defineEmits<{(e:'update:modelValue',value:string):void}>()
type Item={id:string;label:string;enabled:boolean;disabledReason?:string;parentId?:string;metadata?:Record<string,unknown>}
type Page={available:boolean;unavailableReason?:string;offset:number;limit:number;hasMore:boolean;items:Item[]}
const query=ref('');const items=ref<Item[]>([]);const loading=ref(false);const available=ref(true);const unavailableReason=ref('');
const hasMore=ref(false);const offset=ref(0);const pageSize=50
const referenceType=computed(()=>props.definition.referenceType||props.definition.type)
const parentId=computed(()=>{const key=props.definition.referenceParentParameter;return key?String(props.context[key]??''):''})
let sequence=0;let searchTimer:ReturnType<typeof setTimeout>|undefined

function merge(current:Item[],incoming:Item[]){
 const byId=new Map(current.map(item=>[item.id,item]));for(const item of incoming)byId.set(item.id,item);return [...byId.values()]
}
async function requestPage(requestOffset:number,q:string){
 return await admInvokeOperation<Page>('admParameterReferenceSearch',{query:{
  referenceType:referenceType.value,q,offset:requestOffset,limit:pageSize,parentId:parentId.value||undefined
 }})
}
async function load(reset=true){
 const current=++sequence;loading.value=true
 try{
  const requestOffset=reset?0:offset.value
  const body=await requestPage(requestOffset,query.value)
  if(current!==sequence)return
  available.value=body.available===true;unavailableReason.value=body.unavailableReason||''
  const next=Array.isArray(body.items)?body.items:[]
  items.value=reset?next:merge(items.value,next)
  offset.value=requestOffset+next.length;hasMore.value=body.hasMore===true
  const selected=String(props.modelValue??'')
  if(selected&&available.value&&!items.value.some(item=>item.id===selected)){
   const selectedPage=await requestPage(0,selected)
   if(current===sequence&&selectedPage.available)items.value=merge(selectedPage.items||[],items.value)
  }
 }catch(e){if(current===sequence){available.value=false;items.value=[];hasMore.value=false;unavailableReason.value=e instanceof Error?e.message:'Reference Catalog 조회 실패'}}
 finally{if(current===sequence)loading.value=false}
}
function scheduleSearch(){if(searchTimer)clearTimeout(searchTimer);searchTimer=setTimeout(()=>void load(true),250)}
let initialized=false
watch([referenceType,parentId],()=>{if(initialized)emit('update:modelValue','');initialized=true;query.value='';void load(true)},{immediate:true})
watch(()=>props.modelValue,()=>{if(initialized)void load(true)})
onBeforeUnmount(()=>{if(searchTimer)clearTimeout(searchTimer)})
</script>
<template><div class="reference-picker">
 <input v-model.trim="query" type="search" :disabled="disabled||!available" :placeholder="`${definition.label} 검색`" @input="scheduleSearch">
 <select :value="String(modelValue??'')" :disabled="disabled||loading||!available" @focus="load(true)" @change="emit('update:modelValue',($event.target as HTMLSelectElement).value)">
  <option value="">{{loading?'조회 중…':'선택'}}</option><option v-for="item in items" :key="item.id" :value="item.id" :disabled="!item.enabled">{{item.label}} ({{item.id}}){{item.disabledReason?` - ${item.disabledReason}`:''}}</option>
 </select>
 <button v-if="available&&hasMore" type="button" :disabled="disabled||loading" @click="load(false)">더 보기</button>
 <small v-if="!available" class="capability-error" role="alert">{{unavailableReason}}</small>
 <small v-else-if="items.length===0&&!loading" class="empty-result">검색 결과가 없습니다.</small>
</div></template>
<style scoped>.reference-picker{display:grid;grid-template-columns:minmax(8rem,.8fr) minmax(12rem,1.4fr) auto;gap:.35rem}.capability-error,.empty-result{grid-column:1/-1}.capability-error{color:#c84646}@media(max-width:680px){.reference-picker{grid-template-columns:1fr}}</style>
