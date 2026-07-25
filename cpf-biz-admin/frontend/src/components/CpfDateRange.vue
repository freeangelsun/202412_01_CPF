<script setup lang="ts">
const props = withDefaults(defineProps<{ from?: string; to?: string; min?: string; max?: string; disabled?: boolean }>(), { from:'', to:'', min:undefined, max:undefined, disabled:false })
const emit = defineEmits<{ (e:'update:from', value:string): void; (e:'update:to', value:string): void }>()
function onFrom(v:string){ emit('update:from', v); if(props.to && v > props.to) emit('update:to', v) }
function onTo(v:string){ emit('update:to', v); if(props.from && v < props.from) emit('update:from', v) }
</script>
<template>
  <div class="cpf-date-range" role="group" aria-label="조회 기간">
    <label><span>시작일</span><input type="date" :value="from" :min="min" :max="to || max" :disabled="disabled" @input="onFrom(($event.target as HTMLInputElement).value)" /></label>
    <span aria-hidden="true">~</span>
    <label><span>종료일</span><input type="date" :value="to" :min="from || min" :max="max" :disabled="disabled" @input="onTo(($event.target as HTMLInputElement).value)" /></label>
  </div>
</template>
