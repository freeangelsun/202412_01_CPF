<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["assignmentId", "employeeNo", "organizationCode", "positionCode", "jobTitleCode", "assignmentType", "primaryYn", "effectiveFrom", "effectiveTo"];
const fields = [{"name": "assignmentId", "label": "발령 ID", "type": "number"}, {"name": "employeeNo", "label": "직원 번호", "required": true}, {"name": "organizationCode", "label": "조직", "required": true}, {"name": "positionCode", "label": "직급"}, {"name": "jobTitleCode", "label": "직책"}, {"name": "assignmentType", "label": "발령 유형", "required": true}, {"name": "primaryYn", "label": "대표 소속", "type": "yn"}, {"name": "effectiveFrom", "label": "시작시각", "required": true}, {"name": "effectiveTo", "label": "종료시각"}];
</script>
<template>
  <CrudTable title="직원 발령·겸직" endpoint="/api/bza/directory/assignments" menu-code="EMPLOYEE" :columns="columns" :fields="fields" />
</template>
