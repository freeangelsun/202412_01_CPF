<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["menuCode", "parentMenuCode", "menuName", "routePath", "sortOrder", "useYn"];
const fields = [{"name": "menuCode", "label": "메뉴 코드", "required": true}, {"name": "parentMenuCode", "label": "상위 메뉴"}, {"name": "menuName", "label": "메뉴명", "required": true}, {"name": "routePath", "label": "Route"}, {"name": "sortOrder", "label": "정렬", "type": "number"}, {"name": "useYn", "label": "사용", "type": "yn"}];
</script>
<template>
  <CrudTable title="메뉴 Registry" endpoint="/api/bza/menus" menu-code="AUTHORIZATION" :columns="columns" :fields="fields" />
</template>
