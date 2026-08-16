<script lang="ts">
import { defineComponent } from "vue";
import { useAdmConsolePage } from "../../app/useAdmConsolePage";
import CpfIcon from "../../components/CpfIcon.vue";

/**
 * Read-only recovery overview.
 *
 * Risk mutations intentionally live only in the canonical Reliability page so every action uses
 * the same action permission, expected-version/idempotency, reason and audit contract.  This page
 * must never grow a second mutation consumer.
 */
export default defineComponent({
  name: "RecoveryCenterPage",
  components: { CpfIcon },
  setup() { return useAdmConsolePage(); },
  computed: {
    unknown() { return this.reliabilityResult?.unknownResults || []; },
    dlq() { return this.reliabilityResult?.dlq || []; },
    outbox() { return this.reliabilityResult?.outbox || []; },
    transfers() { return this.reliabilityResult?.fileTransfers || []; }
  }
});
</script>

<template>
  <div class="cpf-page">
    <div class="cpf-page-heading">
      <div>
        <p class="eyebrow">RECOVERY CENTER</p>
        <h2>복구 센터</h2>
        <p>결과 미확정·DLQ·Outbox·파일전송 실패를 조회합니다. 위험조치는 신뢰성 처리 관제의 단일 canonical consumer에서 수행합니다.</p>
      </div>
      <button class="ghost" type="button" @click="loadReliability"><CpfIcon name="refresh"/> 새로고침</button>
    </div>
    <section class="cpf-kpi-grid">
      <div class="cpf-stat-card"><span class="label">Unknown</span><strong class="value">{{ unknown.length }}</strong><span class="cpf-status danger">Reconcile</span></div>
      <div class="cpf-stat-card"><span class="label">DLQ</span><strong class="value">{{ dlq.length }}</strong><span class="cpf-status warning">Replay</span></div>
      <div class="cpf-stat-card"><span class="label">Outbox</span><strong class="value">{{ outbox.length }}</strong><span class="cpf-status info">Publish</span></div>
      <div class="cpf-stat-card"><span class="label">File Transfer</span><strong class="value">{{ transfers.length }}</strong><span class="cpf-status">Tracking</span></div>
    </section>
    <div class="cpf-grid-2">
      <section class="cpf-card">
        <div class="cpf-card-head"><h2>결과 미확정</h2></div>
        <div class="table-wrap"><table><thead><tr><th>ID</th><th>유형</th><th>상태</th><th>거래ID</th></tr></thead><tbody>
          <tr v-for="r in unknown.slice(0,20)" :key="String(r.unknownId||r.id)"><td>{{r.unknownId||r.id}}</td><td>{{r.unknownType||r.type}}</td><td><span class="cpf-status warning">{{r.status}}</span></td><td>{{r.transactionId}}</td></tr>
        </tbody></table></div>
      </section>
      <section class="cpf-card">
        <div class="cpf-card-head"><h2>DLQ 재처리 후보</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Message</th><th>Topic</th><th>상태</th></tr></thead><tbody>
          <tr v-for="r in dlq.slice(0,20)" :key="String(r.messageId)"><td>{{r.messageId}}</td><td>{{r.topic}}</td><td>{{r.status}}</td></tr>
        </tbody></table></div>
      </section>
    </div>
    <p class="cpf-note">DLQ Replay, UNKNOWN 확정, Poison retry, transaction-log recovery 실행은 <strong>신뢰성 처리 관제</strong>에서만 수행합니다. 이 화면은 중복 mutation consumer를 제공하지 않습니다.</p>
  </div>
</template>
