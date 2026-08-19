<script setup lang="ts">
const emit = defineEmits<{
  inbox: []
  detail: []
  decide: []
}>()
</script>

<template>
  <div class="toolbar">
    <button @click="emit('inbox')">Inbox</button>
    <button @click="emit('detail')">상세</button>
    <button @click="emit('decide')">승인/반려</button>
  </div>
</template>
