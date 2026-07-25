<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import DataTable from "../../components/DataTable.vue";
import { bzaApi } from "../auth/session";
const props = defineProps<{ routeId: string }>();
const rows = ref<Record<string, unknown>[]>([]); const loading=ref(false); const error=ref("");
const config = computed(() => props.routeId === "employees"
  ? { title: "직원 목록", url: "/api/bza/backoffice/employees", columns: ["employeeNo","employeeName","organizationCode","positionCode","jobTitleCode","employmentStatus"] }
  : { title: "조직 목록", url: "/api/bza/backoffice/organizations", columns: ["organizationCode","parentOrganizationCode","organizationName","organizationType","useYn"] });
async function load(){loading.value=true;error.value="";try{rows.value=await bzaApi<Record<string,unknown>[]>(config.value.url);}catch(e){error.value=e instanceof Error?e.message:String(e);}finally{loading.value=false;}}
onMounted(load); watch(()=>props.routeId,load);
</script>
<template><div class="page-stack"><p v-if="error" class="error-banner">{{error}}</p><DataTable :title="config.title" :rows="rows" :columns="config.columns" :loading="loading" /></div></template>
