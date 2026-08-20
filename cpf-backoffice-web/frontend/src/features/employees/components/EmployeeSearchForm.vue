<script setup lang="ts">
import type { EmployeeSearchCriteria } from '../model/employeeModel'

const props = defineProps<{ criteria: EmployeeSearchCriteria; loading: boolean }>()
const emit = defineEmits<{ search: [] }>()
</script>

<template>
  <form class="toolbar" @submit.prevent="emit('search')">
    <label>조직<input v-model.trim="props.criteria.organizationCode" placeholder="예: SALES" /></label>
    <label>재직상태<input v-model.trim="props.criteria.status" placeholder="예: EMPLOYED" /></label>
    <label>페이지 크기<input v-model.number="props.criteria.size" min="1" max="100" type="number" /></label>
    <button :disabled="loading" type="submit">검색</button>
  </form>
</template>
