<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  admIncidentAcknowledge,
  admIncidentCreateIncident,
  admIncidentCreateMaintenance,
  admIncidentCreatePolicy,
  admIncidentEscalate,
  admIncidentFindIncident,
  admIncidentFindIncidents,
  admIncidentFindMaintenance,
  admIncidentFindPolicies,
  admIncidentFindTimeline,
  admIncidentIngestSignal,
  admIncidentReopen,
  admIncidentRecordPostmortem,
  admIncidentResolve,
  admIncidentTransitionIncident,
  admIncidentUpdateMaintenance,
  admIncidentUpdatePolicy,
} from "../../generated/cpf-api";
import { useAdmSessionStore } from "../../stores/admSessionStore";

type Row = Record<string, unknown>;
type PageResult = { content?: Row[]; items?: Row[]; page?: number; size?: number; totalElements?: number } & Row;

const session = useAdmSessionStore();
const busy = ref(false);
const error = ref("");
const message = ref("");
const status = ref("");
const incidents = ref<Row[]>([]);
const policies = ref<Row[]>([]);
const maintenance = ref<Row[]>([]);
const selectedIncident = ref<Row | null>(null);
const timeline = ref<Row[]>([]);

const policy = reactive({
  policyId: 0, policyCode: "", eventType: "SYSTEM", eventSubType: "", severity: "MAJOR",
  thresholdCount: 1, windowSeconds: 60, escalationMinutes: 10, receiverGroup: "CPF_ADM_OPERATOR",
  useYn: "Y", expectedVersion: 0, reason: "", approvalRequestId: "", idempotencyKey: crypto.randomUUID(),
});
const signal = reactive({
  policyCode: "", sourceType: "SYSTEM", sourceId: "", correlationId: "", transactionId: "",
  title: "", summary: "", occurredAt: "", idempotencyKey: crypto.randomUUID(),
});
const manualIncident = reactive({ severity: "SEV2", title: "", summary: "", sourceType: "MANUAL", sourceId: "ADM", reason: "" });
const manualTransition = reactive({ status: "ACKNOWLEDGED", reason: "" });
const action = reactive({
  actionType: "ACKNOWLEDGE", expectedVersion: 0, reason: "", approvalRequestId: "", idempotencyKey: crypto.randomUUID(),
});
const maint = reactive({
  maintenanceId: 0, maintenanceCode: "", targetType: "SERVICE", targetId: "", startsAt: "", endsAt: "",
  useYn: "Y", expectedVersion: 0, reason: "", approvalRequestId: "", idempotencyKey: crypto.randomUUID(),
});

const selectedIncidentId = computed(() => Number(selectedIncident.value?.incidentId ?? selectedIncident.value?.incident_id ?? 0));
const can = (operationId: string) => session.hasOperation(operationId);
const content = (value: unknown): Row[] => {
  if (Array.isArray(value)) return value as Row[];
  const page = value as PageResult | null;
  return (page?.content ?? page?.items ?? []) as Row[];
};
const text = (value: unknown) => value instanceof Error ? value.message : String(value ?? "알 수 없는 오류");

async function run<T>(fn: () => Promise<T>, success?: string): Promise<T | undefined> {
  if (busy.value) return undefined;
  busy.value = true; error.value = ""; message.value = "";
  try { const result = await fn(); if (success) message.value = success; return result; }
  catch (failure) { error.value = text(failure); return undefined; }
  finally { busy.value = false; }
}

async function loadAll(): Promise<void> {
  const result = await run(async () => Promise.all([
    admIncidentFindIncidents<PageResult>({ query: { status: status.value || undefined, page: 0, size: 100 } }),
    admIncidentFindPolicies<PageResult>({ query: { page: 0, size: 100 } }),
    admIncidentFindMaintenance<PageResult>({ query: { page: 0, size: 100 } }),
  ]));
  if (!result) return;
  incidents.value = content(result[0]); policies.value = content(result[1]); maintenance.value = content(result[2]);
}

async function selectIncident(row: Row): Promise<void> {
  const id = Number(row.incidentId ?? row.incident_id);
  if (!Number.isSafeInteger(id) || id <= 0) return;
  const result = await run(async () => Promise.all([
    admIncidentFindIncident<Row>({ path: { incidentId: id } }),
    admIncidentFindTimeline<Row[]>({ path: { incidentId: id } }),
  ]));
  if (!result) return;
  selectedIncident.value = result[0]; timeline.value = Array.isArray(result[1]) ? result[1] : [];
  action.expectedVersion = Number(selectedIncident.value?.version ?? 0);
}

async function createManualIncident(): Promise<void> {
  if (!manualIncident.title.trim() || manualIncident.reason.trim().length < 5) { error.value = "Incident 제목과 5자 이상의 사유가 필요합니다."; return; }
  const result = await run(() => admIncidentCreateIncident<Row>({ data: { ...manualIncident } }), "Incident를 생성했습니다.");
  if (result) { manualIncident.title = ""; manualIncident.summary = ""; manualIncident.reason = ""; await loadAll(); }
}
async function transitionManualIncident(): Promise<void> {
  const incidentId = selectedIncidentId.value;
  if (!incidentId || manualTransition.reason.trim().length < 5) { error.value = "Incident 선택과 5자 이상의 상태전이 사유가 필요합니다."; return; }
  const result = await run(() => admIncidentTransitionIncident<Row>({ path: { incidentId }, data: { status: manualTransition.status, reason: manualTransition.reason } }), "Incident 상태를 전이했습니다.");
  if (result) { selectedIncident.value = result; manualTransition.reason = ""; await loadAll(); }
}

async function savePolicy(): Promise<void> {
  const data = { ...policy, policyId: undefined, expectedVersion: Number(policy.expectedVersion), idempotencyKey: policy.idempotencyKey || crypto.randomUUID() };
  const result = policy.policyId > 0
    ? await run(() => admIncidentUpdatePolicy<Row>({ path: { policyId: policy.policyId }, data }), "Incident 정책을 수정했습니다.")
    : await run(() => admIncidentCreatePolicy<Row>({ data }), "Incident 정책을 등록했습니다.");
  if (result) { policy.idempotencyKey = crypto.randomUUID(); await loadAll(); }
}

async function ingest(): Promise<void> {
  const occurredAt = signal.occurredAt ? new Date(signal.occurredAt).toISOString().replace("Z", "") : new Date().toISOString().replace("Z", "");
  const result = await run(() => admIncidentIngestSignal<Row>({ data: { ...signal, occurredAt, idempotencyKey: signal.idempotencyKey || crypto.randomUUID() } }), "Signal을 수집하고 Threshold를 평가했습니다.");
  if (result) { signal.idempotencyKey = crypto.randomUUID(); await loadAll(); }
}

async function executeIncidentAction(): Promise<void> {
  const incidentId = selectedIncidentId.value;
  if (!incidentId) { error.value = "Incident를 선택하세요."; return; }
  const data = { expectedVersion: Number(action.expectedVersion), reason: action.reason, approvalRequestId: action.approvalRequestId, idempotencyKey: action.idempotencyKey || crypto.randomUUID() };
  const invoke = action.actionType === "ACKNOWLEDGE" ? () => admIncidentAcknowledge<Row>({ path: { incidentId }, data })
    : action.actionType === "RESOLVE" ? () => admIncidentResolve<Row>({ path: { incidentId }, data })
    : action.actionType === "REOPEN" ? () => admIncidentReopen<Row>({ path: { incidentId }, data })
    : action.actionType === "POSTMORTEM" ? () => admIncidentRecordPostmortem<Row>({ path: { incidentId }, data })
    : () => admIncidentEscalate<Row>({ path: { incidentId }, data });
  const result = await run(invoke, `Incident ${action.actionType} 조치를 기록했습니다.`);
  if (result) { action.idempotencyKey = crypto.randomUUID(); selectedIncident.value = result; action.expectedVersion = Number(result.version ?? action.expectedVersion + 1); await selectIncident(result); await loadAll(); }
}

async function saveMaintenance(): Promise<void> {
  const data = {
    ...maint, maintenanceId: undefined,
    startsAt: maint.startsAt ? new Date(maint.startsAt).toISOString().replace("Z", "") : "",
    endsAt: maint.endsAt ? new Date(maint.endsAt).toISOString().replace("Z", "") : "",
    expectedVersion: Number(maint.expectedVersion), idempotencyKey: maint.idempotencyKey || crypto.randomUUID(),
  };
  const result = maint.maintenanceId > 0
    ? await run(() => admIncidentUpdateMaintenance<Row>({ path: { maintenanceId: maint.maintenanceId }, data }), "Maintenance Window를 수정했습니다.")
    : await run(() => admIncidentCreateMaintenance<Row>({ data }), "Maintenance Window를 등록했습니다.");
  if (result) { maint.idempotencyKey = crypto.randomUUID(); await loadAll(); }
}

function editPolicy(row: Row): void {
  Object.assign(policy, { ...row, policyId: Number(row.policyId ?? 0), expectedVersion: Number(row.version ?? 0), reason: "", approvalRequestId: "", idempotencyKey: crypto.randomUUID() });
}
function editMaintenance(row: Row): void {
  Object.assign(maint, { ...row, maintenanceId: Number(row.maintenanceId ?? 0), expectedVersion: Number(row.version ?? 0), startsAt: String(row.startsAt ?? ""), endsAt: String(row.endsAt ?? ""), reason: "", approvalRequestId: "", idempotencyKey: crypto.randomUUID() });
}

onMounted(loadAll);
</script>

<template>
  <section class="incident-panel" aria-labelledby="incident-lifecycle-title">
    <header class="header"><div><h2 id="incident-lifecycle-title">Incident Lifecycle</h2><p>정책·Signal·Incident 상태전이·Timeline·Maintenance를 실제 ADM lifecycle API로 운영합니다.</p></div><button :disabled="busy" @click="loadAll">새로고침</button></header>
    <p v-if="error" class="state error" role="alert">{{ error }}</p><p v-if="message" class="state success" role="status">{{ message }}</p>
    <div class="grid">
      <article class="panel"><h3>Incident</h3><label>Status <input v-model.trim="status" /></label><div class="table-wrap"><table><thead><tr><th>ID</th><th>Severity</th><th>Status</th><th>Title</th><th>Version</th></tr></thead><tbody><tr v-for="row in incidents" :key="String(row.incidentId)" @click="selectIncident(row)"><td>{{ row.incidentId }}</td><td>{{ row.severity }}</td><td>{{ row.status }}</td><td>{{ row.title }}</td><td>{{ row.version }}</td></tr></tbody></table></div>
        <div class="form-grid"><label>신규 Severity<input v-model.trim="manualIncident.severity" /></label><label>Source<input v-model.trim="manualIncident.sourceId" /></label><label class="wide">신규 Incident 제목<input v-model.trim="manualIncident.title" /></label><label class="wide">요약<textarea v-model.trim="manualIncident.summary" /></label><label class="wide">생성 사유<textarea v-model.trim="manualIncident.reason" minlength="5" /></label><button :disabled="busy || !can('admIncidentCreateIncident')" @click="createManualIncident">수동 Incident 생성</button></div><div v-if="selectedIncident" class="action-box"><strong>선택 Incident {{ selectedIncidentId }}</strong><div class="form-grid"><label>표준 상태전이<select v-model="manualTransition.status"><option>ACKNOWLEDGED</option><option>MITIGATED</option><option>RESOLVED</option><option>CLOSED</option><option>OPEN</option></select></label><label>상태전이 사유<input v-model.trim="manualTransition.reason" /></label><button :disabled="busy || !can('admIncidentTransitionIncident')" @click="transitionManualIncident">표준 상태전이</button></div><label>Action<select v-model="action.actionType"><option>ACKNOWLEDGE</option><option>RESOLVE</option><option>REOPEN</option><option>ESCALATE</option><option>POSTMORTEM</option></select></label><label>Expected Version<input v-model.number="action.expectedVersion" type="number" min="0" /></label><label>Approval Request ID<input v-model.trim="action.approvalRequestId" /></label><label>사유<textarea v-model.trim="action.reason" minlength="5" /></label><button class="danger" :disabled="busy || !can(action.actionType === 'POSTMORTEM' ? 'admIncidentRecordPostmortem' : `admIncident${action.actionType[0]}${action.actionType.slice(1).toLowerCase()}`)" @click="executeIncidentAction">조치 실행</button><pre>{{ JSON.stringify(selectedIncident, null, 2) }}</pre><pre>{{ JSON.stringify(timeline, null, 2) }}</pre></div>
      </article>
      <article class="panel"><h3>Policy</h3><div class="table-wrap"><table><thead><tr><th>Code</th><th>Event</th><th>Severity</th><th>Version</th></tr></thead><tbody><tr v-for="row in policies" :key="String(row.policyId)" @click="editPolicy(row)"><td>{{ row.policyCode }}</td><td>{{ row.eventType }}</td><td>{{ row.severity }}</td><td>{{ row.version }}</td></tr></tbody></table></div><div class="form-grid"><label>Code<input v-model.trim="policy.policyCode" /></label><label>Event Type<input v-model.trim="policy.eventType" /></label><label>Sub Type<input v-model.trim="policy.eventSubType" /></label><label>Severity<input v-model.trim="policy.severity" /></label><label>Threshold<input v-model.number="policy.thresholdCount" type="number" min="1" /></label><label>Window(s)<input v-model.number="policy.windowSeconds" type="number" min="1" /></label><label>Escalation(m)<input v-model.number="policy.escalationMinutes" type="number" min="0" /></label><label>Receiver<input v-model.trim="policy.receiverGroup" /></label><label>Expected Version<input v-model.number="policy.expectedVersion" type="number" min="0" /></label><label>Approval ID<input v-model.trim="policy.approvalRequestId" /></label><label class="wide">사유<textarea v-model.trim="policy.reason" /></label></div><button :disabled="busy || !(policy.policyId ? can('admIncidentUpdatePolicy') : can('admIncidentCreatePolicy'))" @click="savePolicy">{{ policy.policyId ? '정책 수정' : '정책 등록' }}</button></article>
      <article class="panel"><h3>Signal</h3><div class="form-grid"><label>Policy Code<input v-model.trim="signal.policyCode" /></label><label>Source Type<input v-model.trim="signal.sourceType" /></label><label>Source ID<input v-model.trim="signal.sourceId" /></label><label>Correlation ID<input v-model.trim="signal.correlationId" /></label><label>Transaction ID<input v-model.trim="signal.transactionId" /></label><label>Occurred At<input v-model="signal.occurredAt" type="datetime-local" /></label><label class="wide">Title<input v-model.trim="signal.title" /></label><label class="wide">Summary<textarea v-model.trim="signal.summary" /></label></div><button :disabled="busy || !can('admIncidentIngestSignal')" @click="ingest">Signal 수집</button></article>
      <article class="panel"><h3>Maintenance Window</h3><div class="table-wrap"><table><thead><tr><th>Code</th><th>Target</th><th>기간</th><th>Version</th></tr></thead><tbody><tr v-for="row in maintenance" :key="String(row.maintenanceId)" @click="editMaintenance(row)"><td>{{ row.maintenanceCode }}</td><td>{{ row.targetType }} / {{ row.targetId }}</td><td>{{ row.startsAt }} → {{ row.endsAt }}</td><td>{{ row.version }}</td></tr></tbody></table></div><div class="form-grid"><label>Code<input v-model.trim="maint.maintenanceCode" /></label><label>Target Type<input v-model.trim="maint.targetType" /></label><label>Target ID<input v-model.trim="maint.targetId" /></label><label>Start<input v-model="maint.startsAt" type="datetime-local" /></label><label>End<input v-model="maint.endsAt" type="datetime-local" /></label><label>Expected Version<input v-model.number="maint.expectedVersion" type="number" min="0" /></label><label>Approval ID<input v-model.trim="maint.approvalRequestId" /></label><label class="wide">사유<textarea v-model.trim="maint.reason" /></label></div><button :disabled="busy || !(maint.maintenanceId ? can('admIncidentUpdateMaintenance') : can('admIncidentCreateMaintenance'))" @click="saveMaintenance">{{ maint.maintenanceId ? 'Window 수정' : 'Window 등록' }}</button></article>
    </div>
  </section>
</template>

<style scoped>
.incident-panel{display:grid;gap:1rem;margin-top:1rem;border-top:2px solid #d7dde5;padding-top:1rem}.header{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(28rem,1fr));gap:1rem}.panel{border:1px solid #d7dde5;border-radius:.6rem;padding:1rem;background:#fff;display:grid;gap:.75rem}.table-wrap{overflow:auto;max-height:18rem}table{border-collapse:collapse;width:100%}th,td{padding:.5rem;border-bottom:1px solid #e4e8ed;text-align:left}tbody tr{cursor:pointer}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.6rem}.form-grid label,.action-box label{display:grid;gap:.25rem}.wide{grid-column:1/-1}.action-box{display:grid;gap:.6rem;border-top:1px solid #e4e8ed;padding-top:.75rem}.action-box pre{max-height:12rem;overflow:auto}.state{padding:.75rem}.error{background:#fff0f0;color:#a51d1d}.success{background:#edf9f0;color:#126b35}.danger{background:#9d1c1c;color:#fff}@media(max-width:720px){.grid,.form-grid{grid-template-columns:1fr}.wide{grid-column:auto}}
</style>
