<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["loginId", "roleCode", "roleName", "validFrom", "validTo", "primaryYn"];
const fields = [{"name": "loginId", "label": "로그인 ID", "required": true}, {"name": "roleCode", "label": "Role", "required": true}, {"name": "validFrom", "label": "시작시각"}, {"name": "validTo", "label": "종료시각"}, {"name": "primaryYn", "label": "대표 Role", "type": "yn"}];
</script>
<template>
  <CrudTable title="사용자 다중 Role" endpoint="/api/bza/directory/user-roles" menu-code="ROLE" :columns="columns" :fields="fields" />
</template>
