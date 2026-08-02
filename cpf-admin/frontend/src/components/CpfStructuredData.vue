<script setup lang="ts">
import { computed } from "vue";

defineOptions({ name: "CpfStructuredData" });

const props = withDefaults(defineProps<{
  value: unknown;
  depth?: number;
  maxDepth?: number;
  maxItems?: number;
  fieldName?: string;
  emptyLabel?: string;
}>(), {
  depth: 0,
  maxDepth: 4,
  maxItems: 100,
  fieldName: "",
  emptyLabel: "표시할 데이터가 없습니다."
});

const sensitiveKey = /(password|passwd|secret|token|authorization|credential|private.?key|api.?key)/i;

function normalize(value: unknown): unknown {
  if (typeof value !== "string") return value;
  const trimmed = value.trim();
  if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) return value;
  try { return JSON.parse(trimmed); } catch { return value; }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  if (value === null || typeof value !== "object" || Array.isArray(value)) return false;
  const prototype = Object.getPrototypeOf(value);
  return prototype === Object.prototype || prototype === null;
}

function isStructured(value: unknown): boolean {
  return Array.isArray(value) || isRecord(value);
}

function formatScalar(value: unknown): string {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "boolean") return value ? "예" : "아니요";
  return String(value);
}

function summary(value: unknown): string {
  if (Array.isArray(value)) return `목록 ${value.length}건`;
  if (isRecord(value)) return `상세 ${Object.keys(value).length}개 항목`;
  return formatScalar(value);
}

function label(key: string): string {
  return key.replace(/[_-]+/g, " ").replace(/([a-z0-9])([A-Z])/g, "$1 $2");
}

function itemKey(value: unknown, index: number): string {
  if (isRecord(value)) {
    const candidate = value.id ?? value.code ?? value.key ?? value.name;
    if (candidate !== null && candidate !== undefined) return `${String(candidate)}-${index}`;
  }
  return String(index);
}

const normalized = computed(() => normalize(props.value));
const objectEntries = computed(() => isRecord(normalized.value)
  ? Object.entries(normalized.value).slice(0, props.maxItems)
  : []);
const arrayItems = computed(() => Array.isArray(normalized.value)
  ? normalized.value.slice(0, props.maxItems)
  : []);
const totalItems = computed(() => Array.isArray(normalized.value)
  ? normalized.value.length
  : isRecord(normalized.value) ? Object.keys(normalized.value).length : 0);
const canDescend = computed(() => props.depth < props.maxDepth);
</script>

<template>
  <div class="cpf-structured-data" role="group">
    <p v-if="normalized == null || normalized === ''" class="cpf-structured-empty">{{ emptyLabel }}</p>
    <p v-else-if="fieldName && sensitiveKey.test(fieldName)" class="cpf-structured-scalar">***</p>
    <dl v-else-if="isRecord(normalized)" class="cpf-structured-record">
      <template v-for="([key, item]) in objectEntries" :key="key">
        <dt>{{ label(key) }}</dt>
        <dd>
          <span v-if="sensitiveKey.test(key)">***</span>
          <CpfStructuredData
            v-else-if="isStructured(item) && canDescend"
            :value="item"
            :depth="depth + 1"
            :max-depth="maxDepth"
            :max-items="maxItems"
            :field-name="key"
            :empty-label="emptyLabel"
          />
          <span v-else-if="isStructured(item)">{{ summary(item) }}</span>
          <span v-else>{{ formatScalar(item) }}</span>
        </dd>
      </template>
    </dl>
    <ol v-else-if="Array.isArray(normalized)" class="cpf-structured-list">
      <li v-for="(item, index) in arrayItems" :key="itemKey(item, index)">
        <CpfStructuredData
          v-if="isStructured(item) && canDescend"
          :value="item"
          :depth="depth + 1"
          :max-depth="maxDepth"
          :max-items="maxItems"
          :empty-label="emptyLabel"
        />
        <span v-else-if="isStructured(item)">{{ summary(item) }}</span>
        <span v-else>{{ formatScalar(item) }}</span>
      </li>
    </ol>
    <p v-else class="cpf-structured-scalar">{{ formatScalar(normalized) }}</p>
    <p v-if="totalItems > maxItems" class="cpf-structured-truncated">
      {{ totalItems - maxItems }}개 항목은 안전한 화면 한도를 초과해 생략했습니다.
    </p>
  </div>
</template>

<style scoped>
.cpf-structured-data { min-width: 0; color: var(--cpf-text, #1f2937); }
.cpf-structured-record { display: grid; grid-template-columns: minmax(8rem, 0.35fr) minmax(0, 1fr); margin: 0; border: 1px solid var(--cpf-border, #d8dee8); border-radius: .5rem; overflow: hidden; }
.cpf-structured-record > dt, .cpf-structured-record > dd { margin: 0; padding: .5rem .65rem; border-bottom: 1px solid var(--cpf-border, #d8dee8); overflow-wrap: anywhere; }
.cpf-structured-record > dt { background: var(--cpf-surface-muted, #f4f6f9); font-weight: 650; }
.cpf-structured-record > :nth-last-child(-n+2) { border-bottom: 0; }
.cpf-structured-list { display: grid; gap: .5rem; margin: 0; padding-left: 1.4rem; }
.cpf-structured-list > li { min-width: 0; padding-left: .2rem; }
.cpf-structured-empty, .cpf-structured-scalar, .cpf-structured-truncated { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
.cpf-structured-empty, .cpf-structured-truncated { color: var(--cpf-text-muted, #64748b); }
.cpf-structured-truncated { margin-top: .5rem; font-size: .85rem; }
@media (max-width: 720px) { .cpf-structured-record { grid-template-columns: 1fr; } .cpf-structured-record > dt { border-bottom: 0; } }
</style>
