<template>
  <div class="cpf-page-stack">
    <section class="cpf-card" aria-labelledby="openapi-title">
      <div class="cpf-card-head">
        <h1 id="openapi-title">OpenAPI Web MVC 운영</h1>
        <span class="cpf-status" :class="statusClass">{{ snapshot?.status || "UNKNOWN" }}</span>
      </div>
      <p>Runtime Route Inventory와 API Docs 노출 상태를 조회합니다. 원문 Schema나 인증정보는 화면에 표시하지 않습니다.</p>
      <p v-if="error" class="cpf-error" role="alert">{{ error }}</p>
      <div class="cpf-action-row">
        <button :disabled="busy" @click="load">상태 새로고침</button>
      </div>
      <dl v-if="snapshot" class="cpf-detail-grid" aria-live="polite">
        <div><dt>Instance</dt><dd>{{ snapshot.instanceId }}</dd></div>
        <div><dt>Operation</dt><dd>{{ snapshot.operationCount }}</dd></div>
        <div><dt>API Docs</dt><dd>{{ snapshot.apiDocsEnabled ? "노출" : "비노출" }}</dd></div>
        <div><dt>Path</dt><dd>{{ snapshot.apiDocsPath }}</dd></div>
        <div><dt>Refreshed</dt><dd>{{ snapshot.refreshedAt }}</dd></div>
        <div><dt>Failure</dt><dd>{{ snapshot.failureCode || "없음" }}</dd></div>
      </dl>
    </section>

    <section class="cpf-card" aria-labelledby="openapi-refresh-title">
      <h2 id="openapi-refresh-title">Route Inventory 재대사</h2>
      <p>운영 상태를 변경하는 조치이므로 사유와 명시적 확인이 필요합니다.</p>
      <label class="cpf-field">사유
        <input v-model.trim="reason" maxlength="500" autocomplete="off" aria-describedby="openapi-reason-help">
      </label>
      <small id="openapi-reason-help">감사 로그에 기록할 구체적인 사유를 입력하세요.</small>
      <label class="cpf-check"><input v-model="confirmed" type="checkbox"> 위험 조치를 확인했습니다.</label>
      <button class="danger" :disabled="busy || !reason || !confirmed" @click="refreshInventory">재대사 실행</button>
    </section>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admOpenApiRefresh, admOpenApiStatus } from "../../generated/cpf-api";

type Snapshot = {
  status: "UP" | "DEGRADED" | "DOWN" | "UNKNOWN";
  enabled: boolean;
  apiDocsEnabled: boolean;
  apiDocsPath: string;
  instanceId: string;
  operationCount: number;
  refreshedAt: string;
  refreshReason: string;
  failureCode: string;
};

export default defineComponent({
  name: "OpenApiOperationsPage",
  data() {
    return { busy: false, error: "", reason: "", confirmed: false, snapshot: null as Snapshot | null };
  },
  computed: {
    statusClass(): string {
      return this.snapshot?.status === "UP" ? "success" : this.snapshot?.status === "DOWN" ? "danger" : "warning";
    }
  },
  mounted() { void this.load(); },
  methods: {
    async run<T>(action: () => Promise<T>): Promise<T | undefined> {
      this.busy = true; this.error = "";
      try { return await action(); }
      catch (failure) { this.error = failure instanceof Error ? failure.message : String(failure); return undefined; }
      finally { this.busy = false; }
    },
    async load() {
      const result = await this.run(() => admOpenApiStatus<Snapshot>());
      if (result) this.snapshot = result;
    },
    async refreshInventory() {
      if (!this.reason || !this.confirmed) return;
      const result = await this.run(() => admOpenApiRefresh<Snapshot>({
        data: { reason: this.reason },
        headers: { "X-CPF-Risk-Confirmed": "confirmed" }
      }));
      if (result) {
        this.snapshot = result;
        this.reason = "";
        this.confirmed = false;
      }
    }
  }
});
</script>
