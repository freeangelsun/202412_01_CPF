<script setup lang="ts">
import {computed,ref,watch} from 'vue'
import type {ParameterDefinition} from './parameterSchema'
const props=defineProps<{definition:ParameterDefinition;modelValue:unknown;context:Record<string,unknown>;disabled?:boolean}>()
const emit=defineEmits<{(e:'update:modelValue',value:string):void}>()
type Item={id:string;label:string;enabled:boolean;disabledReason?:string;parentId?:string;metadata?:Record<string,unknown>}
const query=ref('');const items=ref<Item[]>([]);const loading=ref(false);const available=ref(true);const unavailableReason=ref('');
const referenceType=computed(()=>props.definition.referenceType||props.definition.type)
const parentId=computed(()=>{const key=props.definition.referenceParentParameter;return key?String(props.context[key]??''):''})
let sequence=0
async function load(){const current=++sequence;loading.value=true;try{const p=new URLSearchParams({referenceType:referenceType.value,q:query.value,offset:'0',limit:'100'});if(parentId.value)p.set('parentId',parentId.value);const response=await fetch(`/adm/api/parameter-references?${p}`,{credentials:'same-origin',headers:{Accept:'application/json'}});if(!response.ok)throw new Error(`Reference Catalog 조회 실패(${response.status})`);const body=await response.json();if(current!==sequence)return;available.value=body.available===true;unavailableReason.value=body.unavailableReason||'';items.value=Array.isArray(body.items)?body.items:[]}catch(e){if(current===sequence){available.value=false;items.value=[];unavailableReason.value=e instanceof Error?e.message:'Reference Catalog 조회 실패'}}finally{if(current===sequence)loading.value=false}}
let initialized=false
watch([referenceType,parentId],()=>{if(initialized)emit('update:modelValue','');initialized=true;void load()},{immediate:true})
</script>
<template><div class="reference-picker">
 <input v-model.trim="query" type="search" :disabled="disabled||!available" :placeholder="`${definition.label} 검색`" @input="load">
 <select :value="String(modelValue??'')" :disabled="disabled||loading||!available" @focus="load" @change="emit('update:modelValue',($event.target as HTMLSelectElement).value)">
  <option value="">{{loading?'조회 중…':'선택'}}</option><option v-for="item in items" :key="item.id" :value="item.id" :disabled="!item.enabled">{{item.label}} ({{item.id}}){{item.disabledReason?` - ${item.disabledReason}`:''}}</option>
 </select>
 <small v-if="!available" class="capability-error">{{unavailableReason}}</small>
</div></template>
<style scoped>.reference-picker{display:grid;grid-template-columns:minmax(8rem,.8fr) minmax(12rem,1.4fr);gap:.35rem}.capability-error{grid-column:1/-1;color:#c84646}@media(max-width:680px){.reference-picker{grid-template-columns:1fr}}</style>
