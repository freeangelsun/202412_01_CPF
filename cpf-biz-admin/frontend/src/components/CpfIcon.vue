<script setup lang="ts">
import { computed } from "vue";
const props = withDefaults(defineProps<{ name?: string; size?: number }>(), { name: "circle", size: 18 });
const paths: Record<string, string[]> = {
  dashboard:["M4 13h6V4H4v9Z","M14 20h6V11h-6v9Z","M14 4h6v3h-6V4Z","M4 17h6v3H4v-3Z"],
  activity:["M3 12h4l2-6 4 12 2-6h6"], service:["M4 6h16v5H4z","M7 11v7h10v-7","M8 8h.01M11 8h.01"],
  topology:["M12 4v4","M6 20v-4h12v4","M6 16V10h12v6","M9 10V8h6v2"],
  recovery:["M4 12a8 8 0 1 0 2.3-5.7","M4 4v5h5"], incident:["M12 3 3 20h18L12 3Z","M12 9v4","M12 17h.01"],
  maintenance:["M14 6 18 2l4 4-4 4","M16 8 8 16l-4-4L12 8","M4 4l4 4"], worker:["M5 7h14v10H5z","M8 3v4M16 3v4M8 17v4M16 17v4"],
  capacity:["M4 19V9","M10 19V5","M16 19v-7","M22 19H2"], logs:["M5 4h14v16H5z","M8 8h8M8 12h8M8 16h5"],
  batch:["M4 7h16M4 12h16M4 17h16","M7 4v6M17 9v6M10 14v6"], security:["M12 3 5 6v5c0 5 3 8 7 10 4-2 7-5 7-10V6l-7-3Z"],
  approval:["M5 4h14v16H5z","M8 9l2 2 5-5","M8 15h8"], users:["M8 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z","M2 21v-2a6 6 0 0 1 12 0v2","M17 11a3 3 0 1 0 0-6","M16 16a5 5 0 0 1 6 4"],
  org:["M4 20h16","M6 20V8h12v12","M9 8V4h6v4","M9 12h2M13 12h2M9 16h2M13 16h2"],
  role:["M4 6h16v12H4z","M8 10h8M8 14h5"], settings:["M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z","M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6V21h-4v-.1a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H3v-4h.1a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3A1.7 1.7 0 0 0 10 3h4a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.1v4H21a1.7 1.7 0 0 0-1.6 1Z"],
  search:["M11 19a8 8 0 1 1 0-16 8 8 0 0 1 0 16Z","M21 21l-4.4-4.4"], menu:["M4 7h16M4 12h16M4 17h16"], close:["M6 6l12 12M18 6 6 18"], refresh:["M20 6v5h-5","M20 11a8 8 0 1 0-2.3 5.7"]
};
const selected = computed(() => paths[props.name] || paths.logs);
</script>
<template><svg class="cpf-icon" :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path v-for="(d,i) in selected" :key="i" :d="d" /></svg></template>
