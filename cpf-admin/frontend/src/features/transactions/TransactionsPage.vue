<template>
  <section class="transaction-workbench" data-cpf-page="operation-catalog-workbench">
    <header class="page-header">
      <div>
        <h1>온라인 Operation 정의</h1>
        <p>Runtime이 자동 등록한 업무 Operation Catalog와 ADM 정책 상태를 단일 정본으로 조회합니다.</p>
      </div>
      <div class="header-actions">
        <button type="button" :disabled="loading" @click="load(0)">새로고침</button>
      </div>
    </header>

    <form class="filters" @submit.prevent="load(0)">
      <label>System<input v-model.trim="filters.systemCode" placeholder="MBR" autocomplete="off"></label>
      <label>활성<select v-model="filters.activeYn"><option value="">전체</option><option value="Y">활성</option><option value="N">비활성</option></select></label>
      <label>Operation ID<input v-model.trim="filters.operationId" placeholder="MBR_MEMBER_JOIN" autocomplete="off"></label>
      <label>쪽 크기<select v-model.number="pageSize"><option :value="10">10</option><option :value="20">20</option><option :value="50">50</option><option :value="100">100</option></select></label>
      <button type="submit" :disabled="loading">조회</button>
    </form>

    <p v-if="errorMessage" class="state error" role="alert">{{ errorMessage }}</p>
    <p v-else-if="loading" class="state" role="status">Operation 정의를 조회하고 있습니다.</p>
    <p v-else-if="!available" class="state warning" role="alert">Operation Catalog를 사용할 수 없습니다. DB3 Migration과 Runtime Bootstrap 상태를 확인하세요.</p>
    <p v-else-if="rows.length === 0" class="state">검색 조건에 해당하는 Operation이 없습니다.</p>

    <div v-if="rows.length" class="table-wrap">
      <table>
        <thead><tr><th>Operation ID</th><th>Operation 명</th><th>System/Domain</th><th>HTTP Mapping</th><th>발견상태</th><th>정책</th><th>최종 발견</th></tr></thead>
        <tbody>
          <tr v-for="row in rows" :key="idOf(row)" :class="{ selected: idOf(row) === selectedId }" @click="select(row)">
            <td><code>{{ idOf(row) }}</code></td>
            <td>{{ text(row, 'operation_name', 'operationName', 'OPERATION_NAME') }}</td>
            <td>{{ text(row, 'system_code', 'systemCode', 'SYSTEM_CODE') }} / {{ text(row, 'domain_code', 'domainCode', 'DOMAIN_CODE') || '-' }}</td>
            <td>{{ text(row, 'http_method', 'httpMethod', 'HTTP_METHOD') }} {{ text(row, 'api_path', 'apiPath', 'API_PATH') }}</td>
            <td>{{ text(row, 'discovery_status', 'discoveryStatus', 'DISCOVERY_STATUS') || '-' }}</td>
            <td>
              <span :class="statusClass(row)">{{ enabled(row) ? '활성' : '비활성' }}</span>
              <br>v{{ policyVersion(row) }} / {{ text(row, 'all_callers_yn', 'allCallersYn', 'ALL_CALLERS_YN') === 'Y' ? 'ALL' : 'Caller 정책' }}
            </td>
            <td>{{ text(row, 'last_seen_at', 'lastSeenAt', 'LAST_SEEN_AT') || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <nav class="pager" aria-label="Operation 정의 페이지">
      <span>총 {{ totalElements.toLocaleString() }}건</span>
      <button type="button" :disabled="page <= 0 || loading" @click="load(page - 1)">이전</button>
      <span>{{ totalPages === 0 ? 0 : page + 1 }} / {{ totalPages }}</span>
      <button type="button" :disabled="page + 1 >= totalPages || loading" @click="load(page + 1)">다음</button>
    </nav>

    <article v-if="selected" class="detail-panel">
      <header>
        <h2>{{ idOf(selected) }} 상세</h2>
        <button v-if="canWrite && enabled(selected)" type="button" class="danger" @click="openInactive">비활성화</button>
      </header>
      <dl>
        <div><dt>Controller</dt><dd>{{ text(selected, 'controller_class', 'controllerClass', 'CONTROLLER_CLASS') }}#{{ text(selected, 'handler_method', 'handlerMethod', 'HANDLER_METHOD') }}</dd></div>
        <div><dt>Mapping</dt><dd>{{ text(selected, 'http_method', 'httpMethod', 'HTTP_METHOD') }} {{ text(selected, 'api_path', 'apiPath', 'API_PATH') }}</dd></div>
        <div><dt>OpenAPI</dt><dd>{{ text(selected, 'openapi_operation_id', 'openapiOperationId', 'OPENAPI_OPERATION_ID') || 'Operation ID 누락' }}</dd></div>
        <div><dt>Runtime Instance</dt><dd>{{ text(selected, 'last_instance_id', 'lastInstanceId', 'LAST_INSTANCE_ID') || '-' }}</dd></div>
        <div><dt>Channel Policy</dt><dd>{{ text(selected, 'channel_policy_required_yn', 'channelPolicyRequiredYn', 'CHANNEL_POLICY_REQUIRED_YN') === 'Y' ? '적용' : '미적용' }}</dd></div>
        <div><dt>정책 Version</dt><dd>{{ policyVersion(selected) }}</dd></div>
        <div><dt>변경사유</dt><dd>{{ text(selected, 'change_reason', 'changeReason', 'CHANGE_REASON') || '-' }}</dd></div>
        <div><dt>변경</dt><dd>{{ text(selected, 'updated_by', 'updatedBy', 'UPDATED_BY') || '-' }} / {{ text(selected, 'updated_at', 'updatedAt', 'UPDATED_AT') || '-' }}</dd></div>
      </dl>
      <div class="deep-links">
        <RouterLink :to="{ name: 'transactionGroups', query: { operationId: idOf(selected) } }">실행 Timeline 조회</RouterLink>
        <RouterLink :to="{ name: 'auditLogs', query: { resourceId: idOf(selected) } }">변경 Audit 조회</RouterLink>
      </div>
    </article>

    <div v-if="dialog === 'inactive'" class="dialog-backdrop" role="presentation" @click.self="dialog = null">
      <form class="dialog" @submit.prevent="executeInactive">
        <h2>Operation 비활성화</h2>
        <p>{{ selectedId }}의 운영 정책을 비활성화합니다. Catalog metadata와 실행 이력은 삭제하지 않습니다.</p>
        <p>현재 policyVersion: <strong>{{ selected ? policyVersion(selected) : 0 }}</strong></p>
        <label>작업 사유<textarea v-model.trim="reason" required minlength="10" maxlength="500"></textarea></label>
        <label class="confirm"><input v-model="confirmed" type="checkbox"> 영향과 Audit 기록을 확인했습니다.</label>
        <p v-if="actionError" class="state error" role="alert">{{ actionError }}</p>
        <div class="dialog-actions"><button type="button" @click="dialog = null">취소</button><button type="submit" class="danger" :disabled="actionLoading || !confirmed || reason.length < 10">실행</button></div>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { RouterLink } from "vue-router";
import { CpfApiError } from "../../shared/cpfApi";
import { useAdmSessionStore } from "../../stores/admSessionStore";
import { findOperationCatalog, findOperationCatalogPage, inactivateOperation, type OperationCatalogRow } from "./api";

const session = useAdmSessionStore();
const filters = reactive({ systemCode: "", activeYn: "Y", operationId: "" });
const rows = ref<OperationCatalogRow[]>([]);
const selected = ref<OperationCatalogRow | null>(null);
const selectedId = ref("");
const page = ref(0); const pageSize = ref(20); const totalElements = ref(0); const totalPages = ref(0); const available = ref(true);
const loading = ref(false); const errorMessage = ref(""); const dialog = ref<"inactive" | null>(null); const reason = ref(""); const confirmed = ref(false); const actionLoading = ref(false); const actionError = ref("");
const canWrite = computed(() => session.canWrite("transactions", "TRANSACTION_META", "/transactions"));

function first(row: OperationCatalogRow, ...keys: string[]): unknown { for (const key of keys) { const value = row[key]; if (value !== undefined && value !== null) return value; } return ""; }
function text(row: OperationCatalogRow, ...keys: string[]): string { return String(first(row, ...keys) || ""); }
function idOf(row: OperationCatalogRow): string { return text(row, "operation_id", "operationId", "OPERATION_ID"); }
function enabled(row: OperationCatalogRow): boolean { return text(row, "enabled_yn", "enabledYn", "ENABLED_YN") === "Y"; }
function policyVersion(row: OperationCatalogRow): number { const value = first(row, "policy_version", "policyVersion", "POLICY_VERSION"); return Number.isFinite(Number(value)) ? Number(value) : 0; }
function statusClass(row: OperationCatalogRow): string { return enabled(row) ? "status ok" : "status muted"; }
function message(error: unknown): string { if (error instanceof CpfApiError) return `${error.status}: ${error.message}`; return error instanceof Error ? error.message : "Operation 정의 처리 중 오류가 발생했습니다."; }
async function load(targetPage = page.value) { loading.value = true; errorMessage.value = ""; try { const result = await findOperationCatalogPage({ ...filters, page: Math.max(0, targetPage), size: pageSize.value }); rows.value = result.items || []; page.value = result.page; totalElements.value = result.totalElements; totalPages.value = result.totalPages; available.value = result.available; if (selectedId.value) { const row = rows.value.find(item => idOf(item) === selectedId.value); if (row) selected.value = row; } } catch (error) { errorMessage.value = message(error); } finally { loading.value = false; } }
async function select(row: OperationCatalogRow) { selectedId.value = idOf(row); selected.value = row; try { const detail = await findOperationCatalog(selectedId.value); if (detail.item && Object.keys(detail.item).length) selected.value = detail.item; } catch (error) { errorMessage.value = message(error); } }
function openInactive() { if (selected.value && enabled(selected.value)) { dialog.value = "inactive"; reason.value = ""; confirmed.value = false; actionError.value = ""; } }
async function executeInactive() { if (!selected.value || !confirmed.value || reason.value.length < 10) return; actionLoading.value = true; actionError.value = ""; try { await inactivateOperation(selectedId.value, policyVersion(selected.value), reason.value); dialog.value = null; await load(page.value); if (selectedId.value) { const current = rows.value.find(item => idOf(item) === selectedId.value); if (current) await select(current); } } catch (error) { actionError.value = message(error); } finally { actionLoading.value = false; } }
watch(pageSize, () => load(0)); onMounted(() => load(0));
</script>

<style scoped>
.transaction-workbench{display:grid;gap:1rem}.page-header,.detail-panel header{display:flex;justify-content:space-between;gap:1rem;align-items:flex-start}.page-header h1,.detail-panel h2{margin:0}.page-header p{margin:.35rem 0 0;color:#52606d}.header-actions,.dialog-actions,.deep-links,.pager{display:flex;gap:.6rem;align-items:center;flex-wrap:wrap}.filters{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:.75rem;align-items:end}.filters label,.dialog label{display:grid;gap:.3rem}.table-wrap{overflow:auto;border:1px solid #d7dde5;border-radius:.5rem}table{border-collapse:collapse;width:100%;min-width:1050px}th,td{padding:.65rem;border-bottom:1px solid #e4e8ed;text-align:left;vertical-align:top}tbody tr{cursor:pointer}tbody tr:hover,tbody tr.selected{background:#f3f6f9}.state{padding:.75rem;border-radius:.4rem;background:#eef3f7}.state.error{background:#fff0f0;color:#a51d1d}.state.warning{background:#fff8dc;color:#715600}.status.ok{color:#126b35}.status.muted{color:#68737d}.detail-panel{border:1px solid #d7dde5;border-radius:.5rem;padding:1rem}.detail-panel dl{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:.75rem}.detail-panel dl div{background:#f7f9fb;padding:.75rem}.detail-panel dt{font-weight:700}.detail-panel dd{margin:.3rem 0 0;overflow-wrap:anywhere}.danger{background:#9d1c1c;color:white}.dialog-backdrop{position:fixed;inset:0;background:#0008;display:grid;place-items:center;z-index:1000}.dialog{width:min(540px,calc(100vw - 2rem));background:white;border-radius:.65rem;padding:1.2rem;display:grid;gap:1rem}.dialog textarea{min-height:100px}.confirm{display:flex!important;grid-template-columns:auto 1fr!important;align-items:center}.dialog-actions{justify-content:flex-end}@media(max-width:720px){.page-header{display:grid}.header-actions{justify-content:flex-start}}
</style>
