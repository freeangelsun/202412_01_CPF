<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["permissionId", "roleCode", "menuCode", "buttonCode", "permissionType", "allowYn", "dataScope"];
const fields = [{"name": "roleCode", "label": "Role", "required": true}, {"name": "menuCode", "label": "메뉴", "required": true}, {"name": "buttonCode", "label": "행위", "required": true}, {"name": "permissionType", "label": "유형"}, {"name": "httpMethod", "label": "HTTP Method"}, {"name": "apiPattern", "label": "API Pattern"}, {"name": "dataScope", "label": "Data Scope"}, {"name": "allowYn", "label": "허용", "type": "yn"}];
</script>
<template>
  <CrudTable title="권한" endpoint="/api/bza/permissions" menu-code="PERMISSION" :columns="columns" :fields="fields" />
</template>
