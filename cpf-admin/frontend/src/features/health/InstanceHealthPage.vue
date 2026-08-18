<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import CpfIcon from "../../components/CpfIcon.vue";
import RuntimeInventorySelector from "../../components/RuntimeInventorySelector.vue";

export default defineComponent({
  name: "InstanceHealthPage",
  components: { CpfIcon, RuntimeInventorySelector },
  setup() { return useAdmConsolePage(); },
  data() { return { centralRuntimeId: "" }; },
  computed: {
    instances(): any[] { return this.healthInstanceResult?.items || []; },
  },
  mounted() { void this.loadHealthInstances(); },
});
</script>

<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div>
        <p class="eyebrow">RUNTIME HEALTH</p>
        <h2>Runtime Instance Health</h2>
        <p>시스템·WAS 인스턴스별 Liveness / Readiness / Startup / Drain 상태를 조회합니다.</p>
      </div>
      <button class="ghost" @click="loadHealthInstances"><CpfIcon name="refresh" /> 새로고침</button>
    </div>

    <section class="cpf-card">
      <div class="cpf-form-grid">
        <label>System ID<input v-model="healthInstanceSearch.systemId" @keyup.enter="loadHealthInstances" /></label>
        <label>Readiness<select v-model="healthInstanceSearch.readiness"><option value="">ALL</option><option>UP</option><option>DEGRADED</option><option>DOWN</option><option>UNKNOWN</option></select></label>
        <label><input v-model="healthInstanceSearch.includeStale" type="checkbox" /> stale 포함</label>
        <RuntimeInventorySelector v-model="centralRuntimeId" capability="HEALTH" /><button class="primary" @click="loadHealthInstances">조회</button>
      </div>
    </section>

    <section class="cpf-card">
      <table class="cpf-table">
        <thead><tr><th>System</th><th>Instance</th><th>Version</th><th>Liveness</th><th>Readiness</th><th>Startup</th><th>Drain</th><th>Last Seen</th></tr></thead>
        <tbody>
          <tr v-for="item in instances" :key="`${item.systemId}:${item.instanceId}`" tabindex="0" @click="selectHealthInstance(item)" @keyup.enter="selectHealthInstance(item)">
            <td>{{ item.systemId }}</td><td>{{ item.instanceId }}</td><td>{{ item.version || '-' }}</td>
            <td>{{ item.liveness }}</td><td>{{ item.readiness }}</td><td>{{ item.startup }}</td>
            <td>{{ item.draining ? 'DRAINING' : 'ACTIVE' }}</td><td>{{ item.lastSeenAt }}</td>
          </tr>
          <tr v-if="!instances.length"><td colspan="8" class="cpf-empty">조회된 Runtime Health 인스턴스가 없습니다.</td></tr>
        </tbody>
      </table>
    </section>

    <section v-if="healthInstanceDetail" class="cpf-card" aria-live="polite">
      <h3>Instance Detail</h3>
      <pre>{{ JSON.stringify(healthInstanceDetail, null, 2) }}</pre>
    </section>
  </div>
</template>
