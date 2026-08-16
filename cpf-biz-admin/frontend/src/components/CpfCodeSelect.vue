<script setup lang="ts">
import { computed } from 'vue'

type CodeOption = { value: string; label: string; disabled?: boolean }
const props = withDefaults(defineProps<{ modelValue?: string; options: CodeOption[]; placeholder?: string; id?: string; disabled?: boolean }>(), {
  modelValue: '', placeholder: '선택', id: undefined, disabled: false
})
const emit = defineEmits<{ (e:'update:modelValue', value:string): void }>()
const controlId = computed(() => props.id || `cpf-code-${Math.random().toString(36).slice(2,9)}`)
</script>
<template>
  <select :id="controlId" class="cpf-code-select" :value="modelValue" :disabled="disabled" @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value)">
    <option value="">{{ placeholder }}</option>
    <option v-for="item in options" :key="item.value" :value="item.value" :disabled="item.disabled">{{ item.label }}</option>
  </select>
</template>
