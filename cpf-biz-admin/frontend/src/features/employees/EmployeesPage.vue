<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["employeeNo", "organizationCode", "employeeName", "positionCode", "jobTitleCode", "employmentStatus", "email", "mobileNo", "officePhoneNo", "useYn"];
const fields = [
  {"name": "employeeNo", "label": "직원 번호", "required": true},
  {"name": "organizationCode", "label": "대표 조직", "required": true},
  {"name": "employeeName", "label": "직원명", "required": true},
  {"name": "positionCode", "label": "직급"},
  {"name": "jobTitleCode", "label": "직책"},
  {"name": "employmentStatus", "label": "재직 상태"},
  {"name": "email", "label": "이메일", "type": "email"},
  {"name": "mobileNo", "label": "연락처(휴대폰)", "type": "tel"},
  {"name": "officePhoneNo", "label": "내부 전화번호", "type": "tel"},
  {"name": "useYn", "label": "사용", "type": "yn"}
];
</script>
<template>
  <CrudTable title="직원" endpoint="/api/bza/backoffice/employees" menu-code="EMPLOYEE" :columns="columns" :fields="fields" />
</template>
