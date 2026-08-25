<script setup lang="ts">
import { computed } from 'vue'
import StructuredDataView from '../../../shared/components/StructuredDataView.vue'
import ApprovalDecisionForm from '../components/ApprovalDecisionForm.vue'
import ApprovalDocumentView from '../components/ApprovalDocumentView.vue'
import ApprovalLookupForm from '../components/ApprovalLookupForm.vue'
import { useApprovals } from '../composables/useApprovals'
import { decisionSnapshot } from '../model/approvalModel'

const { inbox, detail, error, loading, loadInbox, loadDetail, decide } = useApprovals()
const snapshot = computed(() => decisionSnapshot(detail.value))
const ready = computed(() => Boolean(snapshot.value && (detail.value?.approvalDocument as Record<string, unknown> | undefined)?.judgementReady === true))
</script>

<template>
  <section>
    <header class="page-header">
      <div><h2>결재 Inbox / 상세 / 승인·반려</h2><p>상세 판단문서를 확인한 동일 Version/Snapshot으로만 승인·반려합니다.</p></div>
      <button :disabled="loading" @click="loadInbox">Inbox 조회</button>
    </header>
    <ApprovalLookupForm @detail="loadDetail" />
    <ApprovalDocumentView :detail="detail" />
    <ApprovalDecisionForm
      :loading="loading"
      :approval-id="snapshot?.approvalId"
      :version-no="snapshot?.versionNo"
      :payload-hash="snapshot?.payloadHash"
      :ready="ready"
      @decide="decide"
    />
    <p v-if="loading">처리 중...</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <details v-if="!detail && inbox"><summary>Inbox 원본 응답</summary><StructuredDataView :value="inbox" /></details>
  </section>
</template>
