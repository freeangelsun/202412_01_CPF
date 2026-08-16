<template>
  <section class="workbench" data-cpf-page="operations-governance">
    <header class="header">
      <div><h1>운영 정책·SLO</h1><p>Metric/SLO·Alert·Runbook·Self-healing·Topology·Drift·Capacity·DR·외부기관 상태를 한 흐름으로 관제합니다.</p></div>
      <button :disabled="loading" @click="load">새로고침</button>
    </header>
    <p v-if="error" class="state error">{{ error }}</p><p v-else-if="loading" class="state">운영 상태를 수집하고 있습니다.</p>
    <nav class="tabs" aria-label="운영 관제 기능"><button v-for="item in tabs" :key="item" :class="{primary:tab===item}" @click="tab=item">{{ labels[item] }}</button></nav>
    <article v-if="tab==='selfHealing'" class="panel self-healing">
      <div class="cards">
        <article class="card"><span>자동복구</span><strong>{{ field(selfHealing,'killSwitch') }}</strong><small>Allowlist가 비면 안전하게 중지</small></article>
        <article class="card"><span>Rate Limit</span><strong>{{ field(selfHealing,'rateLimitPerMinute') }}/min</strong><small>Controller 전체 복구 요청</small></article>
        <article class="card"><span>Attempt</span><strong>{{ field(selfHealing,'maxAttemptsPerChange') }}</strong><small>Change당 자동 Rollback 최대 시도</small></article>
        <article class="card"><span>Circuit</span><strong>{{ field(selfHealing,'circuitFailureThreshold') }}</strong><small>{{ field(selfHealing,'circuitWindowSeconds') }}초 Window</small></article>
      </div>
      <p class="state warning"><strong>승인 경계:</strong> {{ field(selfHealing,'approvalBoundary') }}</p>
      <section class="subpanel"><h2>허용 Capability</h2><div class="chips"><span v-for="item in list(selfHealing,'allowlist')" :key="String(item)">{{ item }}</span><em v-if="!list(selfHealing,'allowlist').length">미설정 — 자동복구 중지</em></div></section>
      <section class="subpanel"><h2>자동 Rollback 후보</h2><div class="table-wrap"><table><thead><tr><th>Change</th><th>Type</th><th>State</th><th>승인</th><th>요청자</th><th>갱신</th></tr></thead><tbody><tr v-for="row in rows(selfHealing,'candidates')" :key="String(pick(row,'change_id','changeId'))"><td>{{ pick(row,'change_id','changeId') }}</td><td>{{ pick(row,'change_type','changeType') }}</td><td>{{ pick(row,'change_state','changeState') }}</td><td>{{ pick(row,'approval_id','approvalId') || pick(row,'break_glass_id','breakGlassId') || '-' }}</td><td>{{ pick(row,'requested_by','requestedBy') }}</td><td>{{ pick(row,'updated_at','updatedAt') }}</td></tr><tr v-if="!rows(selfHealing,'candidates').length"><td colspan="6">현재 자동 Rollback 후보가 없습니다.</td></tr></tbody></table></div></section>
      <section class="subpanel"><h2>최근 자동복구 실행</h2><div class="table-wrap"><table><thead><tr><th>Operation</th><th>Change</th><th>Type</th><th>State</th><th>Reason</th></tr></thead><tbody><tr v-for="row in rows(selfHealing,'recentChanges')" :key="String(pick(row,'operation_id','operationId'))"><td>{{ pick(row,'operation_id','operationId') }}</td><td>{{ pick(row,'change_id','changeId') }}</td><td>{{ pick(row,'change_type','changeType') }}</td><td>{{ pick(row,'change_state','changeState') }}</td><td>{{ pick(row,'reason') }}</td></tr><tr v-if="!rows(selfHealing,'recentChanges').length"><td colspan="5">실행 이력이 없습니다.</td></tr></tbody></table></div></section>
      <section class="subpanel"><h2>Runbook</h2><ol><li v-for="step in list(selfHealing,'runbook')" :key="String(step)">{{ step }}</li></ol></section>
    </article>
    <article v-else class="panel"><CpfStructuredData :value="activeValue" /></article>
    <div class="links">
      <RouterLink to="/configs">정책·설정 관리</RouterLink><RouterLink to="/runtimeControl">Runtime 변경·Rollback</RouterLink>
      <RouterLink to="/approvals">위험조치 승인</RouterLink><RouterLink to="/incidents">Alert·Incident</RouterLink>
      <RouterLink to="/topology">Topology</RouterLink><RouterLink to="/gateway-dashboard">Gateway</RouterLink><RouterLink to="/transactionGroups">거래 Trace</RouterLink>
    </div>
  </section>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { admOperationsGovernanceSnapshot } from '../../generated/orval/cpf-api';
import { errorMessage } from './support';
type JsonMap=Record<string,unknown>;
const tabs=['metrics','slo','alerts','runbooks','selfHealing','topology','drift','capacity','disasterRecovery','externalInstitutions'] as const;
type Tab=typeof tabs[number];
const labels:Record<Tab,string>={metrics:'Metrics',slo:'SLO',alerts:'Alert',runbooks:'Runbook',selfHealing:'자동복구',topology:'Topology',drift:'Drift',capacity:'Capacity',disasterRecovery:'DR',externalInstitutions:'외부기관'};
const tab=ref<Tab>('metrics'),snapshot=ref<JsonMap>({}),loading=ref(false),error=ref('');
const activeValue=computed(()=>snapshot.value[tab.value]??{});
const selfHealing=computed(()=>((snapshot.value.selfHealing??{}) as JsonMap));
function field(value:unknown,key:string){if(!value||typeof value!=='object')return '-';const record=value as JsonMap;return String(record[key]??'-')}
function list(value:unknown,key:string){if(!value||typeof value!=='object')return [] as unknown[];const candidate=(value as JsonMap)[key];return Array.isArray(candidate)?candidate:[]}
function rows(value:unknown,key:string){return list(value,key).filter((item):item is JsonMap=>Boolean(item)&&typeof item==='object'&&!Array.isArray(item))}
function pick(row:JsonMap,...keys:string[]){for(const key of keys){if(row[key]!==undefined&&row[key]!==null)return row[key];const found=Object.keys(row).find(k=>k.toLowerCase()===key.toLowerCase());if(found&&row[found]!==undefined&&row[found]!==null)return row[found]}return ''}
async function load(){loading.value=true;error.value='';try{const response=await admOperationsGovernanceSnapshot();snapshot.value=(response.data??{}) as JsonMap}catch(e){error.value=errorMessage(e)}finally{loading.value=false}}
onMounted(load);
</script>
<style scoped>
.workbench{display:grid;gap:1rem}.header,.tabs,.links{display:flex;gap:.65rem;align-items:center;justify-content:space-between;flex-wrap:wrap}.header h1{margin:0}.header p{margin:.35rem 0 0;color:#52606d}.tabs{justify-content:flex-start}.panel,.card,.subpanel{border:1px solid #d7dde5;border-radius:.6rem;padding:1rem;background:#fff;overflow:auto}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:.7rem}.card strong{display:block;font-size:1.3rem}.card small{color:#68737d}.self-healing{display:grid;gap:1rem}.subpanel h2{margin-top:0;font-size:1rem}.chips{display:flex;gap:.45rem;flex-wrap:wrap}.chips span{background:#eef3f7;border-radius:999px;padding:.25rem .55rem}.chips em{color:#8a5a00}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;min-width:760px}th,td{text-align:left;padding:.55rem;border-bottom:1px solid #e4e8ed;vertical-align:top}.state{padding:.75rem;border-radius:.4rem;background:#eef3f7}.state.error{background:#fff0f0;color:#a51d1d}.state.warning{background:#fff8dc;color:#715600}.primary{background:#1f5f99;color:white}.links{justify-content:flex-start}.links a{padding:.4rem .65rem;border:1px solid #d7dde5;border-radius:.35rem;text-decoration:none}@media(max-width:720px){.header{display:grid}}
</style>
