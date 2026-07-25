<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["adminUserId", "adminLoginId", "adminName", "roleCode", "useYn"];
const fields = [{"name": "adminLoginId", "label": "로그인 ID", "required": true}, {"name": "adminName", "label": "이름", "required": true}, {"name": "roleCode", "label": "호환 기본 Role"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="BZA 사용자" endpoint="/api/bza/admin-users" menu-code="USER" :columns="columns" :fields="fields" />
</template>
