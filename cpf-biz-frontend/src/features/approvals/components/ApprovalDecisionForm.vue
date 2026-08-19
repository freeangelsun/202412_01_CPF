<script setup lang="ts">
import { reactive } from 'vue'
import type { ApprovalDecisionRequest } from '../model/approvalModel'
const props = defineProps<{ loading: boolean }>()
const emit = defineEmits<{ decide: [approvalId: string, request: ApprovalDecisionRequest] }>()
const form = reactive({ approvalId: '', action: 'APPROVE', reason: '', comment: '' })
function submit() {
  if (!form.approvalId.trim() || !form.reason.trim()) return
  emit('decide', form.approvalId.trim(), { action: form.action, reason: form.reason, comment: form.comment })
}
</script>
<template>
  <form class="reference-form" @submit.prevent="submit">
    <h3>승인/반려 Reference</h3>
    <div class="form-grid">
      <label>Approval ID<input v-model.trim="form.approvalId" required /></label>
      <label>Action<select v-model="form.action"><option value="APPROVE">APPROVE</option><option value="REJECT">REJECT</option></select></label>
    </div>
    <label>사유<textarea v-model.trim="form.reason" required rows="2" /></label>
    <label>의견<textarea v-model.trim="form.comment" rows="2" /></label>
    <button :disabled="props.loading" type="submit">결정 요청</button>
  </form>
</template>
