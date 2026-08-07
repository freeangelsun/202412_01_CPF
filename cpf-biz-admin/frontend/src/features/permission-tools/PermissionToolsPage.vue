<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import DataTable from "../../components/DataTable.vue";
import StructuredDetails from "../../components/StructuredDetails.vue";
import { bzaApi } from "../auth/session";
import { bzaSupportSimulatePermission } from "../../generated/orval/cpf-api";

interface PermissionSimulationResult extends Record<string, unknown> {
  allowed?: boolean;
  matchedRules?: Record<string, unknown>[];
}

const compare = reactive({ leftRoleCode: "", rightRoleCode: "" });
const simulation = reactive({
  roleCode: "",
  menuCode: "",
  actionCode: "",
  httpMethod: "GET",
  apiPath: "/api/bza/",
  environmentCode: "ALL",
  domainCode: "BZA",
  reason: "권한 시뮬레이션"
});
const compareRows = ref<Record<string, unknown>[]>([]);
const simulationResult = ref<PermissionSimulationResult | null>(null);
const error = ref("");
const matchedRules = computed(() => simulationResult.value?.matchedRules ?? []);
const simulationSummary = computed(() => {
  if (!simulationResult.value) return null;
  return Object.fromEntries(
    Object.entries(simulationResult.value).filter(([name]) => name !== "matchedRules")
  );
});

async function compareRoles(): Promise<void> {
  error.value = "";
  try {
    compareRows.value = await bzaApi<Record<string, unknown>[]>(
      `/api/bza/permissions/compare?leftRoleCode=${encodeURIComponent(compare.leftRoleCode)}&rightRoleCode=${encodeURIComponent(compare.rightRoleCode)}`
    );
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

async function simulate(): Promise<void> {
  error.value = "";
  try {
    simulationResult.value = await bzaSupportSimulatePermission({ ...simulation }) as PermissionSimulationResult;
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}
</script>

<template>
  <div class="page-stack">
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
    <section class="card">
      <div class="card-head"><div><p class="eyebrow">COMPARE</p><h2>역할 권한 비교</h2></div></div>
      <form class="form-grid compact" @submit.prevent="compareRoles">
        <label><span>기준 역할</span><input v-model.trim="compare.leftRoleCode" required></label>
        <label><span>비교 역할</span><input v-model.trim="compare.rightRoleCode" required></label>
        <div class="form-action"><button class="primary">비교</button></div>
      </form>
    </section>
    <DataTable title="권한 차이" :rows="compareRows" :columns="['permissionKey','leftRoleCode','rightRoleCode','different']" />
    <section class="card">
      <div class="card-head"><div><p class="eyebrow">SIMULATION</p><h2>권한 시뮬레이션</h2></div></div>
      <form class="form-grid" @submit.prevent="simulate">
        <label><span>역할</span><input v-model.trim="simulation.roleCode" required></label>
        <label><span>메뉴</span><input v-model.trim="simulation.menuCode" required></label>
        <label><span>행위</span><input v-model.trim="simulation.actionCode" required></label>
        <label><span>HTTP</span><input v-model.trim="simulation.httpMethod"></label>
        <label class="wide"><span>API 경로</span><input v-model.trim="simulation.apiPath"></label>
        <label><span>환경</span><input v-model.trim="simulation.environmentCode"></label>
        <label><span>업무 영역</span><input v-model.trim="simulation.domainCode"></label>
        <label class="wide"><span>감사 사유</span><textarea v-model.trim="simulation.reason" required></textarea></label>
        <div class="form-action"><button class="primary">실행</button></div>
      </form>
      <p v-if="simulationResult" :class="simulationResult.allowed ? 'success-banner' : 'error-banner'" role="status">
        최종 판정: {{ simulationResult.allowed ? "허용" : "거부" }} · 일치 규칙 {{ matchedRules.length }}건
      </p>
      <StructuredDetails v-if="simulationSummary" :value="simulationSummary" title="Simulation 입력·판정 상세" />
    </section>
    <DataTable
      v-if="simulationResult"
      title="일치 권한 규칙"
      :rows="matchedRules"
      :columns="['roleCode','menuCode','buttonCode','permissionType','httpMethod','apiPattern','environmentCode','domainCode','dataScope','allowYn']"
    />
  </div>
</template>
