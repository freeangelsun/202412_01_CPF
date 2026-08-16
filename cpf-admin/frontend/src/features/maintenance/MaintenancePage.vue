<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { admMaintenanceFindActions } from "../../generated/cpf-api";
import { requestServiceInstanceApproval } from "../../shared/serviceRegistryApproval";
import { useAdmSessionStore } from "../../stores/admSessionStore";
import { validateMaintenanceAction, type MaintenanceAction } from "./maintenanceWorkflow";

type Row = Record<string, unknown>;
const session = useAdmSessionStore();
const rows = ref<Row[]>([]);
const loading = ref(false);
const executing = ref(false);
const error = ref("");
const success = ref("");
const dialogOpen = ref(false);
const form = reactive<MaintenanceAction>({ serviceId: "", endpointCode: "", instanceId: "", action: "DRAIN", reason: "" });
const canWrite = computed(() => session.canWrite("maintenance", "MAINTENANCE", "/maintenance"));
const hasRows = computed(() => rows.value.length > 0);

function messageOf(value: unknown): string {
  return value instanceof Error ? value.message : String(value || "알 수 없는 오류");
}
async function load(): Promise<void> {
  loading.value = true; error.value = "";
  try {
    const result = await admMaintenanceFindActions<unknown>({ query: { limit: 100 } });
    rows.value = Array.isArray(result) ? result as Row[] : [];
  } catch (cause) {
    rows.value = [];
    error.value = `점검 명령 조회 실패: ${messageOf(cause)}`;
  } finally { loading.value = false; }
}
function openCommand(): void {
  error.value = ""; success.value = "";
  if (!canWrite.value) { error.value = "MAINTENANCE WRITE 권한이 없습니다."; return; }
  form.reason = "";
  dialogOpen.value = true;
}
function closeCommand(): void {
  if (!executing.value) dialogOpen.value = false;
}
async function execute(): Promise<void> {
  error.value = ""; success.value = "";
  if (!canWrite.value) { error.value = "MAINTENANCE WRITE 권한이 없습니다."; return; }
  let request: MaintenanceAction;
  try { request = validateMaintenanceAction(form); }
  catch (cause) { error.value = messageOf(cause); return; }
  executing.value = true;
  try {
    const approval = await requestServiceInstanceApproval(request);
    dialogOpen.value = false;
    success.value = `${request.instanceId} ${request.action} 승인 요청이 생성되었습니다${approval.approvalRequestId ? ` (#${approval.approvalRequestId})` : ""}. 승인 완료 후 Owner Command로 실행됩니다.`;
    await load();
  } catch (cause) {
    error.value = `점검 명령 실행 실패 또는 결과 불명확: ${messageOf(cause)}. 실행 이력을 조회해 대사하세요.`;
  } finally { executing.value = false; }
}
onMounted(load);
</script>

<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">MAINTENANCE / DRAIN</p><h2>점검·Drain 제어</h2><p>Service Registry Owner 경계에서 Instance를 Drain·Disable·Resume하고 결과를 감사 추적합니다.</p></div>
      <div class="actions"><button class="ghost" :disabled="loading" @click="load">새로고침</button><button v-if="canWrite" class="danger" @click="openCommand">운영 명령</button></div>
    </div>
    <p v-if="error" role="alert" class="error-banner">{{ error }}</p>
    <p v-if="success" role="status" class="success-banner">{{ success }}</p>
    <section class="cpf-card">
      <div class="cpf-card-head"><h2>최근 점검 명령</h2><span class="count-pill">{{ rows.length }}</span></div>
      <p v-if="loading" role="status" class="cpf-note">점검 명령을 조회하고 있습니다.</p>
      <div v-else-if="hasRows" class="table-wrap">
        <table><thead><tr><th>시간</th><th>Service</th><th>Endpoint</th><th>Instance</th><th>Action</th><th>결과</th><th>사유</th><th>Operation</th></tr></thead>
          <tbody><tr v-for="r in rows" :key="String(r.actionId || r.operationId)"><td>{{ r.requestedAt }}</td><td>{{ r.serviceId }}</td><td>{{ r.endpointCode }}</td><td>{{ r.instanceId }}</td><td>{{ r.actionType || r.action }}</td><td><span class="cpf-status">{{ r.resultStatus || r.status }}</span></td><td>{{ r.reason }}</td><td>{{ r.operationId || '-' }}</td></tr></tbody>
        </table>
      </div>
      <p v-else class="cpf-empty">조회된 점검 명령이 없습니다.</p>
    </section>
    <dialog :open="dialogOpen" class="modal" aria-labelledby="maintenance-dialog-title">
      <form class="modal-card" @submit.prevent="execute">
        <div class="card-head"><div><p class="eyebrow">HIGH RISK OPERATION</p><h2 id="maintenance-dialog-title">점검 명령 승인 요청</h2></div><button type="button" class="icon-button" :disabled="executing" @click="closeCommand">×</button></div>
        <div class="cpf-form-grid"><label>Service<input v-model.trim="form.serviceId" required :disabled="executing" placeholder="MBR"></label><label>Endpoint<input v-model.trim="form.endpointCode" required :disabled="executing" placeholder="MBR_API"></label><label>Instance<input v-model.trim="form.instanceId" required :disabled="executing" placeholder="MBR-01"></label><label>Action<select v-model="form.action" :disabled="executing"><option value="DRAIN">DRAIN</option><option value="DISABLE">DISABLE</option><option value="RESUME">RESUME</option></select></label><label class="span-2">감사 사유<textarea v-model.trim="form.reason" minlength="8" required :disabled="executing" rows="3" placeholder="운영 명령의 구체적인 사유를 8자 이상 입력하세요."></textarea></label></div>
        <p class="cpf-note">서버가 인증된 Operator·Transaction ID·Operation ID를 감사 기록합니다. 결과 불명확 시 재시도하지 말고 이력에서 대사하세요.</p>
        <div class="dialog-actions"><button type="button" class="ghost" :disabled="executing" @click="closeCommand">취소</button><button class="danger" :disabled="executing">{{ executing ? '요청 중' : '승인 요청' }}</button></div>
      </form>
    </dialog>
  </div>
</template>
