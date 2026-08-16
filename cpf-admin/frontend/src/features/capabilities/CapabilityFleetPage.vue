<script lang="ts">
import { defineComponent } from "vue";
import { useRoute, useRouter } from "vue-router";
import { admCapabilityManagementIssues, admCapabilityManagementOverview } from "../../generated/cpf-api";
import CpfIcon from "../../components/CpfIcon.vue";

type Search = {
  environment: string; systemCode: string; domainCode: string; domainId: string; application: string; module: string;
  host: string; instanceId: string; starterId: string; capabilityId: string; provider: string; version: string; status: string;
};
const emptySearch = (): Search => ({ environment:"",systemCode:"",domainCode:"",domainId:"",application:"",module:"",host:"",instanceId:"",starterId:"",capabilityId:"",provider:"",version:"",status:"" });
const allowedContextKeys = ["environment","systemCode","domainCode","domainId","application","module","host","instanceId","starterId","capabilityId","provider","version","status"] as const;

export default defineComponent({
  name: "CapabilityFleetPage",
  components: { CpfIcon },
  setup() { return { route: useRoute(), router: useRouter() }; },
  data() { return { search: emptySearch(), loading:false, message:"", overview: {} as any, issues: [] as any[], selected: null as any }; },
  computed: {
    items(): any[] { return Array.isArray(this.overview?.items) ? this.overview.items : []; },
    contextQuery(): Record<string,string> {
      const out: Record<string,string> = {};
      for (const key of allowedContextKeys) { const value=String((this.search as any)[key]||"").trim(); if(value) out[key]=value; }
      return out;
    },
  },
  mounted() { this.hydrateFromRoute(); void this.load(); },
  methods: {
    hydrateFromRoute() { for (const key of allowedContextKeys) { const raw=this.route.query[key]; if(typeof raw === "string") (this.search as any)[key]=raw; } },
    async load() {
      this.loading=true; this.message="";
      try {
        const query: any = { ...this.contextQuery, includeStale:true, page:0, size:100 };
        this.overview = await admCapabilityManagementOverview({ query });
        this.issues = await admCapabilityManagementIssues({ query: {
          systemCode: query.systemCode, starterId: query.starterId, capabilityId: query.capabilityId, provider: query.provider, includeStale:true
        } as any }) as any[];
        await this.router.replace({ query: this.contextQuery });
      } catch (error:any) { this.message = error?.message || "Capability 운영 현황 조회에 실패했습니다."; }
      finally { this.loading=false; }
    },
    reset() { this.search=emptySearch(); this.selected=null; void this.load(); },
    selectCapability(instance:any, capability:any) { this.selected={ instance, capability }; },
    async drill(routeName:string) {
      const query: Record<string,string>={...this.contextQuery};
      if(this.selected?.instance){
        for(const key of ["systemCode","domainCode","domainId","application","module","host","instanceId"]){ const v=this.selected.instance[key]; if(v) query[key]=String(v); }
      }
      if(this.selected?.capability){
        query.starterId=String(this.selected.capability.starterArtifactId||"");
        query.capabilityId=String(this.selected.capability.capabilityId||"");
        query.provider=String(this.selected.capability.provider||"");
      }
      await this.router.push({ name:routeName, query });
    },
    healthClass(value:string) { const v=String(value||"UNKNOWN").toUpperCase(); return v==="UP"?"success":v==="DEGRADED"?"warning":"danger"; },
  }
});
</script>

<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">CPF CAPABILITY CONTROL PLANE</p><h2>운영 현황 · Capability</h2><p>어느 시스템이 어떤 Starter/Provider를 사용 중인지 자동 등록된 Runtime 정보로 조회합니다.</p></div>
      <button class="ghost" :disabled="loading" @click="load"><CpfIcon name="refresh" /> 새로고침</button>
    </div>

    <section class="cpf-card">
      <div class="cpf-form-grid">
        <label>System<input v-model.trim="search.systemCode" @keyup.enter="load" /></label>
        <label>Domain<input v-model.trim="search.domainCode" @keyup.enter="load" /></label>
        <label>Application<input v-model.trim="search.application" @keyup.enter="load" /></label>
        <label>Instance<input v-model.trim="search.instanceId" @keyup.enter="load" /></label>
        <label>Starter<input v-model.trim="search.starterId" placeholder="cpf-starter-cache-redis" @keyup.enter="load" /></label>
        <label>Capability<input v-model.trim="search.capabilityId" placeholder="CACHE / MESSAGING" @keyup.enter="load" /></label>
        <label>Provider<input v-model.trim="search.provider" placeholder="cache-redis" @keyup.enter="load" /></label>
        <label>Status<select v-model="search.status"><option value="">ALL</option><option>UP</option><option>DEGRADED</option><option>DOWN</option><option>UNKNOWN</option></select></label>
        <div class="inline-actions"><button class="primary" :disabled="loading" @click="load">조회</button><button class="ghost" @click="reset">초기화</button></div>
      </div>
      <p v-if="message" class="status" role="alert">{{ message }}</p>
    </section>

    <section class="summary-grid">
      <div class="metric"><span>Runtime</span><strong>{{ overview.total || 0 }}</strong></div>
      <div class="metric"><span>Issue</span><strong>{{ overview.issueCount || 0 }}</strong></div>
      <div class="metric"><span>Down</span><strong>{{ overview.downCount || 0 }}</strong></div>
      <div class="metric"><span>Unknown / Stale</span><strong>{{ overview.unknownCount || 0 }}</strong></div>
    </section>

    <section class="cpf-card">
      <table class="cpf-table">
        <thead><tr><th>System / Domain</th><th>Application / Instance</th><th>Starter</th><th>Capability</th><th>Provider</th><th>Health</th><th>Last Seen</th></tr></thead>
        <tbody>
          <template v-for="instance in items" :key="`${instance.systemId}:${instance.instanceId}`">
            <tr v-for="cap in instance.capabilities || []" :key="`${instance.instanceId}:${cap.id}`" tabindex="0" @click="selectCapability(instance,cap)" @keyup.enter="selectCapability(instance,cap)">
              <td><strong>{{ instance.systemCode || instance.systemId }}</strong><small>{{ instance.domainCode || '-' }}</small></td>
              <td>{{ instance.application || '-' }}<small>{{ instance.instanceId }}</small></td>
              <td>{{ cap.starterArtifactId }}</td><td>{{ cap.capabilityId }}</td><td>{{ cap.provider }}</td>
              <td><span class="cpf-status" :class="healthClass(instance.readiness)">{{ instance.stale ? 'UNKNOWN' : instance.readiness }}</span></td>
              <td>{{ instance.observedAt || '-' }}</td>
            </tr>
          </template>
          <tr v-if="!items.length"><td colspan="7" class="cpf-empty">현재 Filter에 등록된 Runtime Capability가 없습니다.</td></tr>
        </tbody>
      </table>
    </section>

    <section v-if="selected" class="cpf-card" aria-live="polite">
      <div class="cpf-page-heading compact"><div><h3>{{ selected.capability.starterArtifactId }}</h3><p>{{ selected.instance.systemCode }} / {{ selected.instance.domainCode }} / {{ selected.instance.instanceId }} · {{ selected.capability.capabilityId }} / {{ selected.capability.provider }}</p></div></div>
      <div class="inline-actions">
        <button v-if="selected.capability.support?.logs" class="ghost" @click="drill('logs')">관련 로그</button>
        <button v-if="selected.capability.support?.trace" class="ghost" @click="drill('transactionGroups')">거래·실행 추적</button>
        <button v-if="selected.capability.support?.failure" class="ghost" @click="drill('recoveryCenter')">장애·복구</button>
        <button v-if="selected.capability.support?.effectiveConfig" class="ghost" @click="drill('configs')">설정·정책</button>
        <button v-if="selected.capability.support?.audit" class="ghost" @click="drill('auditLogs')">감사·변경이력</button>
        <button v-if="selected.capability.dedicatedWorkflow && selected.capability.capabilityId==='BATCH_RUNTIME'" class="primary" @click="drill('batch')">Batch 전용 관리</button>
      </div>
      <pre>{{ JSON.stringify(selected.capability.support, null, 2) }}</pre>
    </section>
  </div>
</template>
