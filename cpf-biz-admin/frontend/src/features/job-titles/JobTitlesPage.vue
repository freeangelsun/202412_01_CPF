<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["jobTitleCode", "jobTitleName", "managerYn", "useYn"];
const fields = [{"name": "jobTitleCode", "label": "직책 코드", "required": true}, {"name": "jobTitleName", "label": "직책명", "required": true}, {"name": "managerYn", "label": "관리자 성격", "type": "yn"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="직책" endpoint="/api/bza/directory/job-titles" menu-code="EMPLOYEE" :columns="columns" :fields="fields" />
</template>
