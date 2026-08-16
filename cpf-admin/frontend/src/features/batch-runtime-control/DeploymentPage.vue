<script setup lang="ts">
import { ref } from "vue";
import StructuredDetails from "../../components/StructuredDetails.vue";
import { createDeploymentPlan } from "./api";

const manifestText = ref("");
const reason = ref("");
const result = ref<Record<string, unknown> | null>(null);
const error = ref("");

async function createPlan(): Promise<void> {
  error.value = "";
  try {
    const manifest = JSON.parse(manifestText.value) as unknown;
    if (!reason.value.trim()) throw new Error("사유 필수");
    result.value = await createDeploymentPlan({
      manifest,
      requestedBy: "authenticated-operator",
      reason: reason.value.trim()
    });
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}
</script>

<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h1>Deployment / Rollback</h1>
        <p>Plan 생성 후 기존 ADM Approval에서 BAT / DEPLOY_PLAN 또는 ROLLBACK_PLAN을 승인합니다.</p>
      </div>
    </div>
    <div class="filters deployment-form">
      <label>배포 Manifest
        <textarea v-model="manifestText" rows="18" spellcheck="false" />
      </label>
      <label>요청 사유
        <input v-model.trim="reason" placeholder="감사 가능한 변경 사유" />
      </label>
      <button type="button" @click="createPlan">Plan 생성</button>
    </div>
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
    <StructuredDetails
      v-if="result"
      :value="result"
      title="생성된 배포 계획"
      empty-text="배포 계획 응답이 비어 있습니다."
    />
  </section>
</template>

<style scoped>
.deployment-form { display: grid; gap: 12px; }
.deployment-form label { display: grid; gap: 6px; }
.deployment-form textarea { width: 100%; resize: vertical; }
</style>
