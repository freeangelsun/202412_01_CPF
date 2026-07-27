<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["employeeNo", "organizationCode", "employeeName", "positionCode", "jobTitleCode", "employmentStatus", "email", "mobileNo", "officePhoneNo", "useYn"];
const fields = [
  {"name": "employeeNo", "label": "직원 번호", "required": true},
  {"name": "organizationCode", "label": "대표 조직", "required": true},
  {"name": "employeeName", "label": "직원명", "required": true},
  {"name": "positionCode", "label": "직급"},
  {"name": "jobTitleCode", "label": "직책"},
  {"name": "employmentStatus", "label": "재직 상태", "required": true, "defaultValue": "EMPLOYED",
   "options": ["EMPLOYED","ON_LEAVE","SECONDMENT","DISPATCHED","RETIRED","TERMINATED"]},
  {"name": "email", "label": "이메일", "type": "email", "preserveOnBlankEdit": true},
  {"name": "mobileNo", "label": "연락처(휴대폰)", "type": "tel", "preserveOnBlankEdit": true},
  {"name": "officePhoneNo", "label": "내부 전화번호", "type": "tel", "preserveOnBlankEdit": true},
  {"name": "clearEmail", "label": "이메일 삭제", "type": "boolean", "defaultValue": false},
  {"name": "clearMobileNo", "label": "휴대폰 삭제", "type": "boolean", "defaultValue": false},
  {"name": "clearOfficePhoneNo", "label": "내부 전화번호 삭제", "type": "boolean", "defaultValue": false},
  {"name": "useYn", "label": "사용", "type": "yn"}
];
</script>
<template>
  <CrudTable title="직원" endpoint="/api/bza/backoffice/employees" menu-code="EMPLOYEE"
    raw-endpoint-template="/api/bza/backoffice/employees/{id}/contacts/raw"
    raw-id-field="employeeNo" :raw-fields="['employeeNo','employeeName','email','mobileNo','officePhoneNo']"
    :columns="columns" :fields="fields" />
</template>
