<script setup lang="ts">
import StructuredDataView from '../../../shared/components/StructuredDataView.vue'
import { useAuthorization } from '../composables/useAuthorization'

const { roles, permissions, error, loading, load } = useAuthorization()
</script>

<template>
  <section>
    <header class="page-header">
      <div>
        <h2>Backoffice 업무 Role / Permission</h2>
        <p>Platform ADM 권한과 분리된 Backoffice 업무 권한 Reference입니다.</p>
      </div>
      <button :disabled="loading" @click="load">조회</button>
    </header>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="grid">
      <StructuredDataView :value="roles" empty-label="Role이 없습니다." />
      <StructuredDataView :value="permissions" empty-label="Permission이 없습니다." />
    </div>
  </section>
</template>
