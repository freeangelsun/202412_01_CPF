<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["positionCode", "positionName", "rankOrder", "useYn"];
const fields = [{"name": "positionCode", "label": "직급 코드", "required": true}, {"name": "positionName", "label": "직급명", "required": true}, {"name": "rankOrder", "label": "서열", "type": "number"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="직급" endpoint="/api/bza/directory/positions" menu-code="EMPLOYEE" :columns="columns" :fields="fields" />
</template>
