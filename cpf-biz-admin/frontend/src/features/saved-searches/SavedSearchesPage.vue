<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["screenCode", "searchName", "criteriaJson", "sharedYn", "createdBy", "updatedAt"];
const fields = [{"name": "screenCode", "label": "화면 코드", "required": true}, {"name": "searchName", "label": "검색명", "required": true}, {"name": "criteriaJson", "label": "검색 조건 JSON", "type": "textarea", "required": true}, {"name": "sharedYn", "label": "공유 여부", "type": "yn"}];
</script>
<template>
  <CrudTable title="저장 검색" endpoint="/api/bza/saved-searches" menu-code="SAVED_SEARCH" :columns="columns" :fields="fields" />
</template>
