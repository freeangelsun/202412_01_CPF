<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["responsibilityId", "organizationCode", "responsibilityType", "employeeNo", "effectiveFrom", "effectiveTo"];
const fields = [{"name": "responsibilityId", "label": "책임 ID", "type": "number"}, {"name": "organizationCode", "label": "조직", "required": true}, {"name": "responsibilityType", "label": "책임 유형", "required": true}, {"name": "employeeNo", "label": "직원", "required": true}, {"name": "effectiveFrom", "label": "시작시각", "required": true}, {"name": "effectiveTo", "label": "종료시각"}];
</script>
<template>
  <CrudTable title="조직 책임·대행" endpoint="/api/bza/directory/responsibilities" menu-code="ORGANIZATION" :columns="columns" :fields="fields" />
</template>
