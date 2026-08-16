<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";

const columns = [
  "permissionId",
  "roleCode",
  "menuCode",
  "buttonCode",
  "permissionType",
  "httpMethod",
  "apiPattern",
  "domainCode",
  "environmentCode",
  "dataScope",
  "allowYn",
  "useYn"
];

const fields = [
  { name: "permissionId", label: "Permission ID", type: "number" as const },
  { name: "roleCode", label: "Role 코드", required: true },
  { name: "menuCode", label: "메뉴 코드", required: true },
  { name: "buttonCode", label: "행위 코드", required: true },
  { name: "permissionType", label: "권한 유형", required: true },
  { name: "httpMethod", label: "HTTP Method" },
  { name: "apiPattern", label: "API Pattern" },
  { name: "domainCode", label: "Domain 코드" },
  { name: "environmentCode", label: "환경 코드" },
  { name: "dataScope", label: "Data Scope" },
  { name: "allowYn", label: "허용", type: "yn" as const },
  { name: "useYn", label: "사용", type: "yn" as const }
];
</script>

<template>
  <CrudTable
    title="권한"
    endpoint="/api/bza/permissions"
    menu-code="AUTHORIZATION"
    :columns="columns"
    :fields="fields"
  />
</template>
