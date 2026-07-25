<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["employeeNo", "organizationCode", "employeeName", "positionCode", "jobTitleCode", "employmentStatus", "useYn"];
const fields = [{"name": "employeeNo", "label": "직원 번호", "required": true}, {"name": "organizationCode", "label": "대표 조직", "required": true}, {"name": "employeeName", "label": "직원명", "required": true}, {"name": "positionCode", "label": "직급"}, {"name": "jobTitleCode", "label": "직책"}, {"name": "employmentStatus", "label": "재직 상태"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="직원" endpoint="/api/bza/backoffice/employees" menu-code="EMPLOYEE" :columns="columns" :fields="fields" />
</template>
