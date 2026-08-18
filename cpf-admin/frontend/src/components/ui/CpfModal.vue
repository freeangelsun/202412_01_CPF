<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, useAttrs, watch } from 'vue'
defineOptions({inheritAttrs:false})
const attrs=useAttrs()
const props=withDefaults(defineProps<{open:boolean;labelledBy?:string;closeOnBackdrop?:boolean;closeOnEscape?:boolean}>(),{labelledBy:'',closeOnBackdrop:true,closeOnEscape:true})
const emit=defineEmits<{cancel:[]}>()
const dialogLabelledBy=computed(()=>props.labelledBy||String(attrs['aria-labelledby']||'')||undefined)
const dialogLabel=computed(()=>String(attrs['aria-label']||'')||(dialogLabelledBy.value?undefined:'대화상자'))
const panel=ref<HTMLElement|null>(null)
let previousFocus:HTMLElement|null=null
function focusables():HTMLElement[]{
  if(!panel.value)return[]
  return Array.from(panel.value.querySelectorAll<HTMLElement>('button:not([disabled]),[href],input:not([disabled]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])')).filter(el=>!el.hasAttribute('hidden'))
}
function cancel(){emit('cancel')}
function keydown(event:KeyboardEvent){
  if(!props.open)return
  if(event.key==='Escape'&&props.closeOnEscape){event.preventDefault();cancel();return}
  if(event.key!=='Tab')return
  const items=focusables();if(!items.length){event.preventDefault();panel.value?.focus();return}
  const first=items[0],last=items[items.length-1],active=document.activeElement
  if(event.shiftKey&&active===first){event.preventDefault();last.focus()}else if(!event.shiftKey&&active===last){event.preventDefault();first.focus()}
}
watch(()=>props.open,async open=>{
  if(open){previousFocus=document.activeElement instanceof HTMLElement?document.activeElement:null;document.addEventListener('keydown',keydown);await nextTick();(focusables()[0]||panel.value)?.focus()}
  else{document.removeEventListener('keydown',keydown);previousFocus?.focus();previousFocus=null}
},{immediate:true})
onBeforeUnmount(()=>document.removeEventListener('keydown',keydown))
</script>
<template>
  <div v-if="open" class="cpf-modal-backdrop" role="presentation" @mousedown.self="closeOnBackdrop&&cancel()">
    <section ref="panel" class="cpf-modal-panel" role="dialog" aria-modal="true" :aria-labelledby="dialogLabelledBy" :aria-label="dialogLabel" tabindex="-1"><slot/></section>
  </div>
</template>
<style scoped>
.cpf-modal-backdrop{position:fixed;inset:0;z-index:1100;display:grid;place-items:center;padding:1rem;background:rgba(15,23,42,.58)}
.cpf-modal-panel{width:min(44rem,100%);max-height:90vh;overflow:auto;outline:none}
</style>
