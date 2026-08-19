<script setup lang="ts">
import StructuredDataView from '../../../shared/components/StructuredDataView.vue'
import EmployeeChangeForm from '../components/EmployeeChangeForm.vue'
import EmployeeSearchForm from '../components/EmployeeSearchForm.vue'
import { useEmployees } from '../composables/useEmployees'

const { criteria, result, error, loading, search, save } = useEmployees()
</script>

<template>
  <section>
    <header class="page-header">
      <div>
        <h2>직원 검색 / Paging / 변경</h2>
        <p>검색과 변경은 BZA Channel을 거쳐 CPF 내부 cpf-biz-admin Public HTTP API로 처리됩니다.</p>
      </div>
    </header>
    <EmployeeSearchForm :criteria="criteria" :loading="loading" @search="search" />
    <EmployeeChangeForm :loading="loading" @save="save" />
    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <StructuredDataView :value="result" empty-label="검색 결과가 없습니다." />
  </section>
</template>
