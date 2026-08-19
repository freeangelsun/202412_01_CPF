<script setup lang="ts">
import StructuredDataView from '../../../shared/components/StructuredDataView.vue'
import { useDashboard } from '../composables/useDashboard'

const { data, error, loading, load } = useDashboard()
</script>

<template>
  <section>
    <header class="page-header">
      <div>
        <h2>업무 대시보드</h2>
        <p>CPF 내부 cpf-biz-admin의 Dashboard API를 BZA Channel 경유로 조회합니다.</p>
      </div>
      <button :disabled="loading" @click="load">새로고침</button>
    </header>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">조회 중...</p>
    <StructuredDataView v-else :value="data" empty-label="대시보드 데이터가 없습니다." />
  </section>
</template>
