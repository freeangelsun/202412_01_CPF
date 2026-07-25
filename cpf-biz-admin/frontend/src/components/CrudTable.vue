<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { bzaApi, hasBzaPermission } from "../features/auth/session";

export interface CrudField {
  name: string;
  label: string;
  type?: "text" | "password" | "number" | "textarea" | "yn";
  sourceName?: string;
  required?: boolean;
}

const props = defineProps<{
  title: string;
  endpoint: string;
  writeEndpoint?: string;
  menuCode: string;
  columns: string[];
  fields: CrudField[];
}>();

const rows = ref<Record<string, unknown>[]>([]);
const loading = ref(false);
const dialogOpen = ref(false);
const message = ref("");
const form = reactive<Record<string, unknown>>({});
const writable = computed(() => hasBzaPermission(props.menuCode, "WRITE"));

async function load(): Promise<void> {
  loading.value = true;
  try { rows.value = await bzaApi<Record<string, unknown>[]>(props.endpoint); }
  finally { loading.value = false; }
}

function open(item: Record<string, unknown> = {}): void {
  for (const key of Object.keys(form)) delete form[key];
  for (const field of props.fields) form[field.name] = item[field.sourceName || field.name] ?? (field.type === "yn" ? "Y" : "");
  form.reason = "업무 기준정보 변경";
  message.value = "";
  dialogOpen.value = true;
}

async function save(): Promise<void> {
  const payload: Record<string, unknown> = { ...form };
  for (const field of props.fields) {
    if (field.type === "number" && payload[field.name] !== "") payload[field.name] = Number(payload[field.name]);
    if (field.type === "password" && payload[field.name] === "") delete payload[field.name];
  }
  try {
    await bzaApi(props.writeEndpoint || props.endpoint, { method: "POST", body: JSON.stringify(payload) });
    dialogOpen.value = false;
    await load();
  } catch (error) { message.value = error instanceof Error ? error.message : String(error); }
}

onMounted(load);
watch(() => props.endpoint, load);
</script>

<template>
  <section class="card table-card">
    <div class="card-head">
      <div><p class="eyebrow">MANAGEMENT</p><h2>{{ title }}</h2></div>
      <div class="inline-actions"><span class="count-pill">{{ rows.length }}건</span><button v-if="writable" class="primary" type="button" @click="open()">등록</button></div>
    </div>
    <div v-if="loading" class="empty-state">조회 중...</div>
    <div v-else class="table-wrap">
      <table>
        <thead><tr><th v-for="column in columns" :key="column">{{ column }}</th><th v-if="writable">관리</th></tr></thead>
        <tbody>
          <tr v-for="(row,index) in rows" :key="index">
            <td v-for="column in columns" :key="column">{{ row[column] ?? "-" }}</td>
            <td v-if="writable"><button type="button" class="ghost" @click="open(row)">수정</button></td>
          </tr>
          <tr v-if="rows.length === 0"><td :colspan="columns.length + (writable ? 1 : 0)" class="empty-cell">조회 결과가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <dialog :open="dialogOpen" class="modal">
      <form class="modal-card" @submit.prevent="save">
        <div class="card-head"><div><p class="eyebrow">EDIT</p><h2>{{ title }} 등록·수정</h2></div><button type="button" class="icon-button" @click="dialogOpen=false">×</button></div>
        <div class="form-grid">
          <label v-for="field in fields" :key="field.name" :class="{ wide: field.type === 'textarea' }">
            <span>{{ field.label }}</span>
            <textarea v-if="field.type === 'textarea'" v-model="form[field.name]" rows="4" :required="field.required"></textarea>
            <select v-else-if="field.type === 'yn'" v-model="form[field.name]"><option value="Y">Y</option><option value="N">N</option></select>
            <input v-else v-model="form[field.name]" :type="field.type || 'text'" :required="field.required">
          </label>
          <label class="wide"><span>감사 사유</span><textarea v-model="form.reason" rows="3" required></textarea></label>
        </div>
        <p v-if="message" class="error-banner">{{ message }}</p>
        <div class="dialog-actions"><button type="button" class="ghost" @click="dialogOpen=false">취소</button><button type="submit" class="primary">저장</button></div>
      </form>
    </dialog>
  </section>
</template>
