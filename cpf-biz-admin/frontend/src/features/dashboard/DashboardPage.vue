<script setup lang="ts">
import { onMounted, ref } from "vue";
import DataTable from "../../components/DataTable.vue";
import MetricCard from "../../components/MetricCard.vue";
import { bzaApi, hasBzaMenu } from "../auth/session";

const summary = ref<Record<string, number>>({});
const approvals = ref<Record<string, unknown>[]>([]);
const error = ref("");
async function load(): Promise<void> {
  error.value = "";
  try {
    const tasks: Promise<unknown>[] = [bzaApi("/api/bza/dashboard")];
    tasks.push(hasBzaMenu("APPROVAL") ? bzaApi("/api/bza/backoffice/approvals?limit=20") : Promise.resolve([]));
    const [dashboard, recent] = await Promise.all(tasks);
    summary.value = dashboard as Record<string, number>;
    approvals.value = recent as Record<string, unknown>[];
  } catch (e) { error.value = e instanceof Error ? e.message : String(e); }
}
onMounted(load);
</script>
<template>
  <div class="page-stack">
    <p v-if="error" class="error-banner">{{ error }}</p>
    <section class="metric-grid">
      <MetricCard label="활성 사용자" :value="summary.activeUserCount ?? 0" />
      <MetricCard label="활성 직원" :value="summary.activeEmployeeCount ?? 0" />
      <MetricCard label="진행 결재" :value="summary.pendingApprovalCount ?? 0" />
      <MetricCard label="미확인 알림" :value="summary.unreadNotificationCount ?? 0" />
      <MetricCard label="오늘 감사" :value="summary.todayAuditCount ?? 0" />
    </section>
    <DataTable title="최근 결재" :rows="approvals" :columns="['approvalNo','title','requesterEmployeeNo','approvalStatus','updatedAt']" />
  </div>
</template>
