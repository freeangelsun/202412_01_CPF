<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["roleCode", "roleName", "writeAllowedYn", "dataScope", "useYn"];
const fields = [{"name": "roleCode", "label": "Role 코드", "required": true}, {"name": "roleName", "label": "Role 명", "required": true}, {"name": "writeAllowedYn", "label": "쓰기 허용", "type": "yn"}, {"name": "dataScope", "label": "Data Scope"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="역할" endpoint="/api/bza/roles" menu-code="AUTHORIZATION" :columns="columns" :fields="fields" />
</template>
