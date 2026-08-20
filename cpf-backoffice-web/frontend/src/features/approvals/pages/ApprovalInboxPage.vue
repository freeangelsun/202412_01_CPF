<script setup lang="ts">
import StructuredDataView from '../../../shared/components/StructuredDataView.vue'
import ApprovalDecisionForm from '../components/ApprovalDecisionForm.vue'
import ApprovalLookupForm from '../components/ApprovalLookupForm.vue'
import { useApprovals } from '../composables/useApprovals'

const { inbox, detail, error, loading, loadInbox, loadDetail, decide } = useApprovals()
</script>

<template>
  <section>
    <header class="page-header">
      <div><h2>결재 Inbox / 상세 / 승인·반려</h2><p>조회와 위험 변경을 명시적인 입력 Form으로 분리한 Reference입니다.</p></div>
      <button :disabled="loading" @click="loadInbox">Inbox 조회</button>
    </header>
    <ApprovalLookupForm @detail="loadDetail" />
    <ApprovalDecisionForm :loading="loading" @decide="decide" />
    <p v-if="loading">처리 중...</p>
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <StructuredDataView :value="detail ?? inbox" empty-label="조회된 결재 데이터가 없습니다." />
  </section>
</template>
