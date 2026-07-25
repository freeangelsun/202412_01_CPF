<script setup lang="ts">
import { onMounted, ref } from "vue";
import { bzaApi } from "../features/auth/session";

const props = defineProps<{ title: string; endpoint: string }>();
const rows = ref<Record<string,unknown>[]>([]);
const loading = ref(false);
const message = ref("");

async function load() {
  loading.value = true; message.value = "";
  try { rows.value = await bzaApi<Record<string,unknown>[]>(props.endpoint); }
  catch (e) { message.value = e instanceof Error ? e.message : String(e); }
  finally { loading.value = false; }
}
onMounted(load);
</script>
<template>
  <section class="card table-card">
    <div class="card-head"><div><p class="eyebrow">FEATURE</p><h2>{{ title }}</h2></div><button class="ghost" @click="load">새로고침</button></div>
    <p v-if="message" class="error-banner">{{ message }}</p>
    <div v-if="loading" class="empty-state">조회 중...</div>
    <div v-else class="table-wrap">
      <table v-if="rows.length"><thead><tr><th v-for="key in Object.keys(rows[0])" :key="key">{{ key }}</th></tr></thead>
        <tbody><tr v-for="(row,i) in rows" :key="i"><td v-for="key in Object.keys(rows[0])" :key="key">{{ row[key] ?? "-" }}</td></tr></tbody></table>
      <div v-else class="empty-state">조회 결과가 없습니다.</div>
    </div>
  </section>
</template>
