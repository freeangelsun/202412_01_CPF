<template>
  <section v-if="loading || failure || partial || stale || empty" class="cpf-operation-state" :class="stateClass" role="status" aria-live="polite">
    <div>
      <strong>{{ title }}</strong>
      <p>{{ message }}</p>
      <small v-if="correlationId">Correlation ID: {{ correlationId }}</small>
      <small v-if="fetchedAt">조회 시각: {{ fetchedAt }}</small>
    </div>
    <button v-if="retryable" class="ghost" type="button" @click="$emit('retry')">다시 시도</button>
  </section>
</template>
<script setup lang="ts">
import { computed } from "vue";
import type { AdmFailureState } from "../shared/operationState";
const props = withDefaults(defineProps<{ loading?: boolean; failure?: AdmFailureState | null; partial?: boolean; stale?: boolean; empty?: boolean; fetchedAt?: string; emptyMessage?: string }>(), {
  loading: false, failure: null, partial: false, stale: false, empty: false, fetchedAt: "", emptyMessage: "조회 결과가 없습니다."
});
defineEmits<{ retry: [] }>();
const correlationId = computed(() => props.failure?.correlationId || "");
const retryable = computed(() => Boolean(props.failure?.retryable));
const stateClass = computed(() => props.failure ? `error ${props.failure.kind}` : props.loading ? "loading" : props.partial || props.stale ? "warning" : "empty");
const title = computed(() => props.failure?.title || (props.loading ? "운영 데이터를 조회하고 있습니다." : props.partial ? "일부 인스턴스 조회가 실패했습니다." : props.stale ? "최신 상태가 아닐 수 있습니다." : "조회 결과 없음"));
const message = computed(() => props.failure?.message || (props.loading ? "완료될 때까지 이전 결과를 변경하지 않습니다." : props.partial ? "성공한 인스턴스 결과와 실패 대상을 함께 확인하세요." : props.stale ? "Owner Runtime 연결 상태를 확인하고 새로고침하세요." : props.emptyMessage));
</script>
<style scoped>
.cpf-operation-state{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start;padding:1rem;border:1px solid var(--adm-border,#d8dee8);border-radius:.75rem;background:var(--adm-panel,#fff);margin:.75rem 0}.cpf-operation-state p{margin:.35rem 0}.cpf-operation-state small{display:block}.cpf-operation-state.error{border-color:#c23b3b}.cpf-operation-state.warning{border-color:#b7791f}.cpf-operation-state.loading{opacity:.85}.cpf-operation-state.empty{border-style:dashed}
</style>
