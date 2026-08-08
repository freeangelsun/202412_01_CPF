<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import StructuredDetails from "../../components/StructuredDetails.vue";
import { bzaApprovalPolicySimulate } from "../../generated/orval/cpf-api";
import { hasBzaPermission } from "../auth/session";

const form = reactive({
  policyCode: "",
  policyVersion: "",
  businessDomain: "BZA",
  approvalType: "GENERAL",
  requesterEmployeeNo: "",
  effectiveAt: ""
});
const result = ref<unknown>(null);
const error = ref("");
const canSimulate = computed(() => hasBzaPermission("APPROVAL", "SIMULATE"));

async function run(): Promise<void> {
  error.value = "";
  if (!canSimulate.value) {
    error.value = "APPROVAL:SIMULATE 권한이 필요합니다.";
    return;
  }
  try {
    const response = await bzaApprovalPolicySimulate({
      ...form,
      policyVersion: form.policyVersion ? Number(form.policyVersion) : null,
      effectiveAt: form.effectiveAt || null
    });
    result.value = response.data;
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}
</script>

<template>
  <section class="card">
    <div class="card-head">
      <div><p class="eyebrow">FAIL-CLOSED PREVIEW</p><h2>결재 경로 Simulation</h2></div>
    </div>
    <form class="form-grid" @submit.prevent="run">
      <label><span>정책 코드(선택)</span><input v-model.trim="form.policyCode"></label>
      <label><span>버전(선택)</span><input v-model="form.policyVersion" type="number"></label>
      <label><span>업무 영역</span><input v-model.trim="form.businessDomain"></label>
      <label><span>결재 유형</span><input v-model.trim="form.approvalType"></label>
      <label><span>요청 직원</span><input v-model.trim="form.requesterEmployeeNo"></label>
      <label><span>기준 시각</span><input v-model.trim="form.effectiveAt" placeholder="2026-07-29T09:00:00Z"></label>
      <div class="form-action"><button v-if="canSimulate" class="primary">Simulation</button><span v-else class="error-banner">APPROVAL:SIMULATE 권한이 필요합니다.</span></div>
    </form>
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
    <StructuredDetails
      v-if="result"
      :value="result"
      title="결재 정책·단계·참여자 해석 결과"
      empty-text="Simulation 결과가 비어 있습니다."
    />
  </section>
</template>
