<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["organizationCode", "parentOrganizationCode", "organizationName", "organizationType", "sortOrder", "useYn"];
const fields = [{"name": "organizationCode", "label": "조직 코드", "required": true}, {"name": "parentOrganizationCode", "label": "상위 조직"}, {"name": "organizationName", "label": "조직명", "required": true}, {"name": "organizationType", "label": "조직 유형"}, {"name": "sortOrder", "label": "정렬", "type": "number"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="조직" endpoint="/api/bza/backoffice/organizations" menu-code="ORGANIZATION" :columns="columns" :fields="fields" />
</template>
