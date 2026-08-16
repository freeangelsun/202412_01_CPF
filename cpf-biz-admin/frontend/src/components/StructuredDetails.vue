<script setup lang="ts">
import { computed } from "vue";

interface DetailRow {
  path: string;
  value: string;
}

const props = withDefaults(defineProps<{
  value: unknown;
  title?: string;
  emptyText?: string;
}>(), {
  title: "",
  emptyText: "표시할 상세 정보가 없습니다."
});

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function scalarText(value: unknown): string {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "boolean") return value ? "Y" : "N";
  return String(value);
}

function appendRows(rows: DetailRow[], value: unknown, path: string): void {
  if (Array.isArray(value)) {
    if (value.length === 0) {
      rows.push({ path, value: "(없음)" });
      return;
    }
    if (value.every((item) => !isRecord(item) && !Array.isArray(item))) {
      rows.push({ path, value: value.map(scalarText).join(", ") });
      return;
    }
    value.forEach((item, index) => appendRows(rows, item, `${path}[${index + 1}]`));
    return;
  }
  if (isRecord(value)) {
    const entries = Object.entries(value);
    if (entries.length === 0) {
      rows.push({ path, value: "(없음)" });
      return;
    }
    entries.forEach(([key, item]) => appendRows(rows, item, path ? `${path}.${key}` : key));
    return;
  }
  rows.push({ path: path || "value", value: scalarText(value) });
}

const rows = computed<DetailRow[]>(() => {
  if (props.value === null || props.value === undefined) return [];
  const result: DetailRow[] = [];
  appendRows(result, props.value, "");
  return result;
});
</script>

<template>
  <section class="structured-details">
    <h3 v-if="title">{{ title }}</h3>
    <div v-if="rows.length" class="table-wrap">
      <table>
        <thead><tr><th>필드</th><th>값</th></tr></thead>
        <tbody>
          <tr v-for="row in rows" :key="row.path">
            <th scope="row">{{ row.path }}</th>
            <td>{{ row.value }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="empty-state">{{ emptyText }}</p>
  </section>
</template>

<style scoped>
.structured-details { margin-top: 18px; }
.structured-details h3 { margin: 0 0 12px; font-size: 15px; }
tbody th { position: static; width: 32%; text-transform: none; letter-spacing: 0; }
td { white-space: pre-wrap; overflow-wrap: anywhere; }
</style>
