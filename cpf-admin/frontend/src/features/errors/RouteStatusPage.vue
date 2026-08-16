<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const props = withDefaults(defineProps<{
  code: number;
  title?: string;
  message?: string;
  retryable?: boolean;
}>(), {
  title: "요청한 화면을 표시할 수 없습니다.",
  message: "메뉴 권한과 URL을 확인한 뒤 다시 시도하세요.",
  retryable: false
});
const route = useRoute();
const router = useRouter();
const heading = computed(() => `${props.code} ${props.title}`);
const requestedPath = computed(() => String(route.query.from || route.query.path || ""));
function retry() {
  if (requestedPath.value) void router.replace(requestedPath.value);
  else void router.go(0);
}
</script>

<template>
  <main class="cpf-route-status" role="main" :aria-labelledby="`route-status-${code}`">
    <section role="alert" class="cpf-route-status__panel">
      <p class="cpf-route-status__code">{{ code }}</p>
      <h1 :id="`route-status-${code}`">{{ heading }}</h1>
      <p>{{ message }}</p>
      <p v-if="requestedPath" class="cpf-route-status__path">요청 경로: {{ requestedPath }}</p>
      <div class="cpf-route-status__actions">
        <button v-if="retryable" type="button" @click="retry">다시 시도</button>
        <button type="button" aria-label="대시보드로 이동" @click="router.push({ name: 'dashboard' })">대시보드로 이동</button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.cpf-route-status { min-height: 60vh; display: grid; place-items: center; padding: 2rem; }
.cpf-route-status__panel { width: min(42rem, 100%); padding: 2rem; border: 1px solid var(--el-border-color, #dcdfe6); border-radius: 0.75rem; text-align: center; }
.cpf-route-status__code { font-size: clamp(2.5rem, 8vw, 5rem); font-weight: 700; margin: 0; }
.cpf-route-status__path { overflow-wrap: anywhere; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.cpf-route-status__actions { display: flex; justify-content: center; gap: 0.75rem; flex-wrap: wrap; }
button { min-height: 2.75rem; margin-top: 1rem; }
</style>
