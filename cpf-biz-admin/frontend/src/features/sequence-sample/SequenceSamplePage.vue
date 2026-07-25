<script setup lang="ts">
import { ref } from "vue";
import { bzaApi } from "../auth/session";

const result = ref<unknown>(null);
const message = ref("");
const ruleCode = ref("ORDER");
const reason = ref("관리자 시험 발급");

async function load(): Promise<void> {
  message.value = "";
  try {
    result.value = {
      rules: await bzaApi("/api/bza/sample/sequence/rules"),
      history: await bzaApi("/api/bza/sample/sequence/history?limit=50")
    };
  } catch (error) {
    message.value = error instanceof Error ? error.message : String(error);
  }
}

async function issue(): Promise<void> {
  message.value = "";
  try {
    result.value = await bzaApi(`/api/bza/sample/sequence/rules/${encodeURIComponent(ruleCode.value)}/issue`, {
      method: "POST",
      body: JSON.stringify({ reason: reason.value })
    });
    await load();
  } catch (error) {
    message.value = error instanceof Error ? error.message : String(error);
  }
}
</script>

<template>
  <section class="feature-page">
    <header>
      <p class="eyebrow">OPTIONAL CUSTOMIZATION SAMPLE</p>
      <h2>업무 채번 Sample</h2>
      <p>기본 비활성입니다. 고객 업무용 중앙 채번 패턴과 관리자 시험 발급 흐름을 설명합니다.</p>
    </header>
    <div class="toolbar">
      <label>Rule <input v-model="ruleCode" /></label>
      <label>시험 사유 <input v-model="reason" /></label>
      <button type="button" class="ghost" @click="load">규칙/이력 조회</button>
      <button type="button" class="primary" @click="issue">시험 발급</button>
    </div>
    <p v-if="message" class="error-banner">{{ message }}</p>
    <pre class="detail">{{ result }}</pre>
  </section>
</template>
