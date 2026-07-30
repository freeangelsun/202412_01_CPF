<script setup lang="ts">
import {computed} from 'vue'
import {isReferenceType,toOffsetDateTime,validateParameter,visible,type ParameterDefinition} from './parameterSchema'
import ReferenceCatalogSelect from './ReferenceCatalogSelect.vue'
const props=withDefaults(defineProps<{definitions:ParameterDefinition[];modelValue:Record<string,unknown>;disabled?:boolean;allowRuntimeOverrideOnly?:boolean}>(),{disabled:false,allowRuntimeOverrideOnly:false})
const emit=defineEmits<{(e:'update:modelValue',value:Record<string,unknown>):void;(e:'validation',value:{valid:boolean;errors:Record<string,string>}):void}>()
const ordered=computed(()=>[...props.definitions].filter(d=>!props.allowRuntimeOverrideOnly||d.runtimeOverrideAllowed).sort((a,b)=>(a.displayOrder??0)-(b.displayOrder??0)))
const errors=computed(()=>{const result:Record<string,string>={};for(const d of ordered.value){if(!visible(d,props.modelValue))continue;const e=validateParameter(d,props.modelValue[d.name]);if(e)result[d.name]=e.message}emit('validation',{valid:Object.keys(result).length===0,errors:result});return result})
function update(name:string,value:unknown){emit('update:modelValue',{...props.modelValue,[name]:value})}
function updateInput(d:ParameterDefinition,value:string){update(d.name,d.type==='DATETIME'?toOffsetDateTime(value):value)}
function inputType(d:ParameterDefinition){if(d.sensitive||d.type==='SECRET_REFERENCE')return'text';if(['INTEGER','LONG','DECIMAL'].includes(d.type))return'number';if(d.type==='DATE')return'date';if(d.type==='DATETIME')return'datetime-local';return'text'}
</script>
<template><div class="cpf-parameter-form">
 <template v-for="d in ordered" :key="d.name">
  <label v-if="visible(d,modelValue)" :class="{wide:d.type==='JSON_OBJECT',invalid:errors[d.name]}">
   <span>{{d.label}} <b v-if="d.required" aria-label="필수">*</b></span>
   <small v-if="d.description">{{d.description}}</small>
   <select v-if="d.type==='ENUM'||d.type==='CODE_REFERENCE'" :value="modelValue[d.name]??d.defaultValue??''" :disabled="disabled" @change="update(d.name,($event.target as HTMLSelectElement).value)">
    <option value="">선택</option><option v-for="v in d.allowedValues||[]" :key="v" :value="v">{{v}}</option>
   </select>
   <select v-else-if="d.type==='BOOLEAN'" :value="String(modelValue[d.name]??d.defaultValue??'')" :disabled="disabled" @change="update(d.name,($event.target as HTMLSelectElement).value)"><option value="">선택</option><option value="true">true</option><option value="false">false</option></select>
   <textarea v-else-if="d.type==='JSON_OBJECT'" :value="String(modelValue[d.name]??d.defaultValue??'')" :disabled="disabled" rows="5" :placeholder="d.placeholder" @input="update(d.name,($event.target as HTMLTextAreaElement).value)"/>
   <ReferenceCatalogSelect v-else-if="isReferenceType(d.type)" :definition="d" :model-value="modelValue[d.name]??d.defaultValue??''" :context="modelValue" :disabled="disabled" @update:model-value="update(d.name,$event)"/>
   <input v-else :type="inputType(d)" :value="String(modelValue[d.name]??d.defaultValue??'')" :disabled="disabled" :placeholder="d.placeholder" :min="d.minValue??undefined" :max="d.maxValue??undefined" :minlength="d.minLength??undefined" :maxlength="d.maxLength??undefined" @input="updateInput(d,($event.target as HTMLInputElement).value)">
   <small v-if="errors[d.name]" class="field-error" role="alert">{{errors[d.name]}}</small>
   <small v-if="d.sensitive" class="security-hint">원문 대신 승인된 Reference만 저장됩니다.</small>
  </label>
 </template>
</div></template>
<style scoped>.cpf-parameter-form{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:.75rem}.cpf-parameter-form label{display:grid;gap:.3rem}.wide{grid-column:1/-1}.invalid input,.invalid select,.invalid textarea{border-color:#c84646}.field-error{color:#c84646}.security-hint{opacity:.75}.reference-input{display:grid;grid-template-columns:1fr auto;gap:.35rem}.reference-input span{align-self:center;font-size:.75rem;padding:.3rem;border:1px solid var(--border-color,#d7dde7);border-radius:5px}</style>
