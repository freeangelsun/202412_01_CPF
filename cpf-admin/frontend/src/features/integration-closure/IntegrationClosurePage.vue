<script setup lang="ts">
import { computed, ref } from 'vue';
import { integrationClosureApi } from '../../generated/integrationClosureApi';

const quarantineId = ref('');
const expectedVersion = ref(1);
const reason = ref('');
const correctedJson = ref('{}');
const approvalId = ref<number | null>(null);
const loading = ref(false);
const error = ref('');
const result = ref<Record<string, unknown> | null>(null);
const canSubmit = computed(() => quarantineId.value.trim() && reason.value.trim() && expectedVersion.value > 0 && !loading.value);

async function requestApproval() {
  error.value = ''; result.value = null; loading.value = true;
  try {
    const corrected = JSON.parse(correctedJson.value) as Record<string, unknown>;
    const response = await integrationClosureApi.requestCorrectionApproval(quarantineId.value.trim(), {
      expectedVersion: expectedVersion.value,
      idempotencyKey: crypto.randomUUID(),
      reason: reason.value.trim(),
      corrected,
    });
    result.value = response;
    const id = Number(response.approvalRequestId ?? response.id);
    approvalId.value = Number.isFinite(id) ? id : null;
  } catch (failure) { error.value = failure instanceof Error ? failure.message : String(failure); }
  finally { loading.value = false; }
}
async function executeApproval() {
  if (!approvalId.value) return;
  error.value = ''; loading.value = true;
  try { result.value = await integrationClosureApi.executeCorrectionApproval(approvalId.value, { reason: reason.value.trim() }); }
  catch (failure) { error.value = failure instanceof Error ? failure.message : String(failure); }
  finally { loading.value = false; }
}
</script>

<template>
  <main class="integration-closure" aria-labelledby="integration-closure-title">
    <h1 id="integration-closure-title">데이터 품질 정정 승인</h1>
    <p>정정 내용은 승인 요청 시 서버 Snapshot으로 고정되며 실행 단계에서 다시 받지 않습니다.</p>
    <form @submit.prevent="requestApproval" aria-describedby="integration-closure-error">
      <label>격리 ID <input v-model="quarantineId" required autocomplete="off" /></label>
      <label>기대 버전 <input v-model.number="expectedVersion" type="number" min="1" required /></label>
      <label>사유 <textarea v-model="reason" required maxlength="500" /></label>
      <label>정정 JSON <textarea v-model="correctedJson" required spellcheck="false" /></label>
      <button type="submit" :disabled="!canSubmit">{{ loading ? '처리 중' : '승인 요청' }}</button>
    </form>
    <section v-if="approvalId" aria-live="polite">
      <p>승인 요청 ID: <strong>{{ approvalId }}</strong></p>
      <button type="button" :disabled="loading || !reason.trim()" @click="executeApproval">승인 검증 후 실행</button>
    </section>
    <p v-if="error" id="integration-closure-error" role="alert">{{ error }}</p>
    <pre v-if="result" tabindex="0">{{ JSON.stringify(result, null, 2) }}</pre>
  </main>
</template>

<style scoped>
.integration-closure{max-width:60rem;margin:0 auto;padding:1rem}form{display:grid;gap:.75rem}label{display:grid;gap:.25rem}input,textarea,button{font:inherit;padding:.65rem}textarea{min-height:7rem}button{width:max-content}pre{overflow:auto;max-height:24rem}@media(max-width:40rem){button{width:100%}}
</style>
