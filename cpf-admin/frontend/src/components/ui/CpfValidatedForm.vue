<script setup lang="ts" generic="T extends Record<string, unknown>">
import { reactive, ref } from "vue";
import type { ZodType } from "zod";
const props=defineProps<{schema:ZodType<T>;model:T;submitLabel?:string;dangerous?:boolean}>();
const emit=defineEmits<{submit:[T]}>();
const errors=reactive<Record<string,string>>({}); const busy=ref(false);
async function submit(){Object.keys(errors).forEach(k=>delete errors[k]);const parsed=props.schema.safeParse(props.model);if(!parsed.success){for(const issue of parsed.error.issues)errors[issue.path.join('.')||'_form']=issue.message;return;}busy.value=true;try{emit('submit',parsed.data)}finally{busy.value=false}}
</script>
<template><el-form :model="model" label-position="top" @submit.prevent="submit"><slot :errors="errors"/><el-alert v-if="errors._form" :title="errors._form" type="error" show-icon/><el-button native-type="submit" :loading="busy" :type="dangerous?'danger':'primary'">{{submitLabel||'저장'}}</el-button></el-form></template>
