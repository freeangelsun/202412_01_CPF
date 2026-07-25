<script setup lang="ts">
import CrudTable from "../../components/CrudTable.vue";
const columns = ["createdAt", "notificationType", "title", "messageBody", "readYn"];
const fields = [{"name": "recipientLoginId", "label": "수신 로그인 ID", "required": true}, {"name": "notificationType", "label": "알림 유형"}, {"name": "title", "label": "제목", "required": true}, {"name": "messageBody", "label": "내용", "type": "textarea"}, {"name": "referenceType", "label": "참조 유형"}, {"name": "referenceId", "label": "참조 ID"}];
</script>
<template>
  <CrudTable title="알림" endpoint="/api/bza/notifications?limit=100" menu-code="NOTIFICATION" :columns="columns" :fields="fields" />
</template>
