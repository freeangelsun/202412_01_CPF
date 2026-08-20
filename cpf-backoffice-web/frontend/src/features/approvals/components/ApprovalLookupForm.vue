<script setup lang="ts">
import { ref } from 'vue'
const emit = defineEmits<{ detail: [approvalId: string] }>()
const approvalId = ref('')
function submit() { if (approvalId.value.trim()) emit('detail', approvalId.value.trim()) }
</script>
<template>
  <form class="toolbar" @submit.prevent="submit">
    <label>Approval ID<input v-model.trim="approvalId" required /></label>
    <button type="submit">상세 조회</button>
  </form>
</template>
