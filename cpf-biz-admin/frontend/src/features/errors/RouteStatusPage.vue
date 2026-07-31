<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";

const props = withDefaults(defineProps<{
  code: 403 | 404;
  title?: string;
  message?: string;
}>(), {
  title: "요청한 화면을 표시할 수 없습니다.",
  message: "메뉴 권한과 URL을 확인한 뒤 다시 시도하세요."
});
const router = useRouter();
const heading = computed(() => `${props.code} ${props.title}`);
</script>

<template>
  <main class="cpf-route-status" role="main" :aria-labelledby="`route-status-${code}`">
    <section role="alert" class="cpf-route-status__panel">
      <p class="cpf-route-status__code">{{ code }}</p>
      <h1 :id="`route-status-${code}`">{{ heading }}</h1>
      <p>{{ message }}</p>
      <button type="button" aria-label="대시보드로 이동" @click="router.push({ name: 'dashboard' })">
        대시보드로 이동
      </button>
    </section>
  </main>
</template>

<style scoped>
.cpf-route-status { min-height: 60vh; display: grid; place-items: center; padding: 2rem; }
.cpf-route-status__panel { width: min(36rem, 100%); padding: 2rem; border: 1px solid var(--el-border-color, #dcdfe6); border-radius: 0.75rem; text-align: center; }
.cpf-route-status__code { font-size: clamp(2.5rem, 8vw, 5rem); font-weight: 700; margin: 0; }
button { min-height: 2.75rem; margin-top: 1rem; }
</style>
