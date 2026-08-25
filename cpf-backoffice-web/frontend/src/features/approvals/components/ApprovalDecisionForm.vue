<script setup lang="ts">
import { computed, reactive } from 'vue'
import type { ApprovalDecisionRequest } from '../model/approvalModel'

const props = defineProps<{
  loading: boolean
  approvalId?: string
  versionNo?: number
  payloadHash?: string
  ready: boolean
}>()
const emit = defineEmits<{ decide: [approvalId: string, request: ApprovalDecisionRequest] }>()
const form = reactive({ action: 'APPROVE', reason: '', comment: '' })
const targetLabel = computed(() => props.approvalId ? `#${props.approvalId} / v${props.versionNo ?? '-'}` : '상세 미선택')
function submit() {
  if (!props.ready || !props.approvalId || !props.versionNo || !props.payloadHash || !form.reason.trim()) return
  emit('decide', props.approvalId, {
    action: form.action,
    reason: form.reason,
    comment: form.comment,
    expectedVersionNo: props.versionNo,
    expectedPayloadHash: props.payloadHash,
  })
}
</script>
<template>
  <form class="reference-form" @submit.prevent="submit">
    <h3>승인/반려</h3>
    <p>
      결재 대상: <strong>{{ targetLabel }}</strong>
      <span v-if="!props.ready"> — 아래 상세문서를 먼저 조회하고 확인해야 결정할 수 있습니다.</span>
    </p>
    <div class="form-grid">
      <label>결정
        <select v-model="form.action" :disabled="!props.ready">
          <option value="APPROVE">승인</option>
          <option value="REJECT">반려</option>
        </select>
      </label>
    </div>
    <label>사유<textarea v-model.trim="form.reason" required rows="2" :disabled="!props.ready" /></label>
    <label>의견<textarea v-model.trim="form.comment" rows="2" :disabled="!props.ready" /></label>
    <button :disabled="props.loading || !props.ready" type="submit">확인한 문서 기준으로 결정</button>
  </form>
</template>
