<template>
  <section class="adm-commercial-page-boundary" :data-route-id="contract?.routeId" :data-menu-id="contract?.menuId" :aria-busy="loading">
    <header class="sr-only" v-if="contract">
      <h2>{{ contract.routeId }}</h2>
      <p>Owner {{ contract.ownerModule }}, Risk {{ contract.riskLevel }}</p>
    </header>
    <div v-if="failure" class="route-contract-error" role="alert" :data-status="failure.status">
      <strong>{{ failure.status }} · {{ failure.title }}</strong>
      <p>{{ failure.message }}</p>
      <p v-if="correlationId">Correlation ID: <code>{{ correlationId }}</code></p>
      <div class="inline-actions">
        <button v-if="failure.retryable" type="button" class="primary" @click="retry">다시 시도</button>
        <button type="button" class="ghost" @click="goBack">이전 화면</button>
      </div>
    </div>
    <slot v-else />
    <p v-if="loading" class="route-loading" role="status" aria-live="polite">운영 데이터를 불러오고 있습니다...</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onErrorCaptured, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { commercialPageContractFor, failurePresentation, type AdmFailurePresentation } from "./pageContract";
import { findCapabilityByRouteName } from "../../app/routes";

const emit = defineEmits<{ retry: [] }>();
const route = useRoute();
const router = useRouter();
const failure = ref<AdmFailurePresentation | null>(null);
const loading = ref(false);
const contract = computed(() => {
  const capability = findCapabilityByRouteName(route.name);
  return capability ? commercialPageContractFor(capability) : null;
});
const correlationId = computed(() => {
  const raw = route.query.correlationId ?? route.query.transactionId;
  return Array.isArray(raw) ? raw[0] : raw;
});

onErrorCaptured((error) => {
  failure.value = failurePresentation(error);
  loading.value = false;
  return false;
});
watch(() => route.fullPath, () => { failure.value = null; loading.value = false; });
function retry() { failure.value = null; loading.value = true; emit("retry"); window.setTimeout(() => { loading.value = false; }, 500); }
function goBack() { if (window.history.length > 1) router.back(); else void router.push({ name: "dashboard" }); }
</script>
