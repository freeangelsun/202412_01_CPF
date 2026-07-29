<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import StructuredDetails from "../../components/StructuredDetails.vue";
import { bzaApi } from "../auth/session";

type AuditRow = Record<string, unknown>;

interface AuditVerification {
  status?: string;
  message?: string;
  verifiedRows?: number;
  brokenAuditId?: number | string | null;
  computedHead?: string;
}

const rows = ref<AuditRow[]>([]);
const verification = ref<AuditVerification | null>(null);
const selected = ref<AuditRow | null>(null);
const error = ref("");

function value(row: AuditRow, key: string): unknown {
  return row[key] ?? row[key.toUpperCase()];
}

function parseSnapshot(snapshot: unknown): unknown {
  if (typeof snapshot !== "string" || !snapshot.trim()) return snapshot;
  try {
    return JSON.parse(snapshot) as unknown;
  } catch {
    return snapshot;
  }
}

const selectedDetails = computed(() => {
  if (!selected.value) return null;
  return {
    ...selected.value,
    beforeData: parseSnapshot(value(selected.value, "beforeData")),
    afterData: parseSnapshot(value(selected.value, "afterData"))
  };
});

const verificationClass = computed(() => {
  const status = verification.value?.status?.toUpperCase();
  return status === "BROKEN" ? "error-banner" : status === "VALID" ? "success-banner" : "warning-banner";
});

async function load(): Promise<void> {
  error.value = "";
  try {
    rows.value = await bzaApi<AuditRow[]>("/api/bza/backoffice/audits?limit=100");
    if (selected.value) {
      const selectedId = value(selected.value, "auditId");
      selected.value = rows.value.find((row) => value(row, "auditId") === selectedId) ?? null;
    }
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

async function verifyChain(): Promise<void> {
  error.value = "";
  try {
    verification.value = await bzaApi<AuditVerification>("/api/bza/audits/verify");
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

onMounted(load);
</script>

<template>
  <section class="card">
    <div class="card-head">
      <div><p class="eyebrow">AUDIT</p><h2>업무 감사</h2></div>
      <div class="inline-actions">
        <button class="ghost" type="button" @click="load">새로고침</button>
        <button class="primary" type="button" @click="verifyChain">Hash Chain 검증</button>
      </div>
    </div>
    <p v-if="verification" :class="verificationClass" role="status">
      {{ verification.status }} · {{ verification.message }} · {{ verification.verifiedRows ?? 0 }} rows
      <span v-if="verification.brokenAuditId"> · broken audit {{ verification.brokenAuditId }}</span>
    </p>
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>
    <div class="table-wrap">
      <table>
        <thead>
          <tr><th>ID</th><th>Transaction</th><th>Actor</th><th>Action</th><th>Target</th><th>사유</th><th>생성</th><th>상세</th></tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="String(value(row, 'auditId') ?? index)">
            <td>{{ value(row, "auditId") ?? "-" }}</td>
            <td>{{ value(row, "transactionId") ?? "-" }}</td>
            <td>{{ value(row, "actorId") ?? "-" }}</td>
            <td>{{ value(row, "actionType") ?? "-" }}</td>
            <td>{{ value(row, "targetType") ?? "-" }} / {{ value(row, "targetId") ?? "-" }}</td>
            <td>{{ value(row, "reason") ?? "-" }}</td>
            <td>{{ value(row, "createdAt") ?? "-" }}</td>
            <td><button class="ghost small" type="button" @click="selected = row">보기</button></td>
          </tr>
          <tr v-if="rows.length === 0"><td colspan="8" class="empty-cell">감사 이력이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
    <StructuredDetails
      v-if="selectedDetails"
      :value="selectedDetails"
      title="선택 감사 상세 (민감정보 마스킹)"
    />
  </section>
</template>
