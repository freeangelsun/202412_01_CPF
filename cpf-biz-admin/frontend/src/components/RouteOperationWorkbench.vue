<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import CpfStructuredData from "./CpfStructuredData.vue";
import { cpfOperationDescriptors, type CpfOperationId } from "../generated/cpf-operation-contract";
import { bzaInvokeOperation } from "../shared/cpfApi";
import { hasBzaPermission } from "../features/auth/session";
import { attachDangerousReason, validatePathValues } from "./dangerousOperationWorkflow";

const props = defineProps<{ title: string; operationIds: readonly CpfOperationId[] }>();
const selectedId = ref<CpfOperationId | "">("");
const pathValues = reactive<Record<string, string>>({});
const queryText = ref("{}");
const bodyText = ref("{}");
const result = ref<unknown>(null);
const error = ref("");
const loading = ref(false);
const open = ref(false);
const confirmOpen = ref(false);
const reason = ref("");
const descriptors = computed(() => props.operationIds
  .map(id => cpfOperationDescriptors.find(item => item.operationId === id))
  .filter((item): item is (typeof cpfOperationDescriptors)[number] => Boolean(item)));
const selected = computed(() => descriptors.value.find(item => item.operationId === selectedId.value));
const pathNames = computed(() => Array.from(selected.value?.template.matchAll(/\{([^}]+)\}/g) || [], item => item[1]));
const dangerous = computed(() => selected.value?.method !== "GET");
const canExecuteMutation = computed(() => !dangerous.value || hasBzaPermission("SETTINGS", "WRITE") || hasBzaPermission("PERMISSIONS", "WRITE"));

function resetInputs(): void {
  for (const key of Object.keys(pathValues)) delete pathValues[key];
  queryText.value = "{}";
  bodyText.value = "{}";
  reason.value = "";
  confirmOpen.value = false;
  result.value = null;
  error.value = "";
}
watch(() => props.operationIds, () => {
  selectedId.value = descriptors.value[0]?.operationId || "";
  resetInputs();
}, { immediate: true });
watch(selectedId, resetInputs);
function parseObject(value: string, label: string): Record<string, unknown> {
  const trimmed = value.trim();
  if (!trimmed) return {};
  const parsed = JSON.parse(trimmed);
  if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
    throw new Error(`${label}는 JSON Object 형식이어야 합니다.`);
  }
  return parsed as Record<string, unknown>;
}
function requestExecution(): void {
  error.value = "";
  if (!selected.value) return;
  if (!canExecuteMutation.value) {
    error.value = "이 위험 조치를 실행할 WRITE 권한이 없습니다.";
    return;
  }
  try {
    validatePathValues(pathNames.value, pathValues);
    parseObject(queryText.value, "Query");
    if (dangerous.value) parseObject(bodyText.value, "Body");
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
    return;
  }
  if (dangerous.value) {
    reason.value = "";
    confirmOpen.value = true;
    return;
  }
  void execute();
}
async function execute(): Promise<void> {
  const descriptor = selected.value;
  if (!descriptor) return;
  loading.value = true;
  error.value = "";
  result.value = null;
  try {
    const path = validatePathValues(pathNames.value, pathValues);
    let query = parseObject(queryText.value, "Query");
    let body = descriptor.method === "GET" ? undefined : parseObject(bodyText.value, "Body");
    if (dangerous.value) {
      const payload = attachDangerousReason(query, body, reason.value);
      query = payload.query;
      body = payload.body;
    }
    result.value = await bzaInvokeOperation(descriptor.operationId, { path, query, body });
    confirmOpen.value = false;
    reason.value = "";
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  } finally {
    loading.value = false;
  }
}
</script>
<template>
<section v-if="descriptors.length" class="card operation-workbench">
 <header class="card-head"><div><p class="eyebrow">GENERATED OPERATION</p><h2>{{ title }} 고급 작업</h2><p>전용 화면에서 제공하지 않는 상세 조회·상태 전이·복구 조치를 Generated Contract로 실행합니다.</p></div><button type="button" class="ghost" @click="open = !open">{{ open ? '접기' : '열기' }}</button></header>
 <div v-if="open" class="operation-body">
  <label><span>Operation</span><select v-model="selectedId"><option v-for="item in descriptors" :key="item.operationId" :value="item.operationId">{{ item.method }} · {{ item.operationId }}</option></select></label>
  <p v-if="selected" class="operation-path"><strong>{{ selected.method }}</strong> {{ selected.template }}</p>
  <div v-if="pathNames.length" class="form-grid"><label v-for="name in pathNames" :key="name"><span>{{ name }}</span><input v-model.trim="pathValues[name]" required></label></div>
  <div class="form-grid"><label class="wide"><span>Query JSON</span><textarea v-model="queryText" rows="4" spellcheck="false"></textarea></label><label v-if="dangerous" class="wide"><span>Body JSON</span><textarea v-model="bodyText" rows="7" spellcheck="false"></textarea></label></div>
  <p v-if="dangerous" class="hint">위험 조치는 reason·expectedVersion·approvalId를 명시해야 하며 인증 사용자는 Browser에서 지정할 수 없습니다.</p>
  <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
  <div v-if="result !== null" role="status" aria-live="polite" aria-atomic="true">
   <CpfStructuredData class="detail" :value="result" />
  </div>
  <div class="dialog-actions"><button type="button" class="primary" :disabled="loading || !selected" @click="requestExecution">{{ loading ? '실행 중...' : '실행' }}</button></div>
 </div>
 <dialog :open="confirmOpen" class="modal" aria-labelledby="dangerous-operation-title">
  <form class="modal-card" @submit.prevent="execute">
   <div class="card-head"><h2 id="dangerous-operation-title">위험 운영 조치 확인</h2><button type="button" class="icon-button" aria-label="닫기" @click="confirmOpen = false">×</button></div>
   <p><strong>{{ selected?.method }}</strong> {{ selected?.template }}</p>
   <label>감사 사유<textarea v-model.trim="reason" rows="4" minlength="5" required></textarea></label>
   <div class="dialog-actions"><button type="button" class="ghost" @click="confirmOpen = false">취소</button><button class="danger" :disabled="loading">{{ loading ? '실행 중...' : '확인 후 실행' }}</button></div>
  </form>
 </dialog>
</section>
</template>
