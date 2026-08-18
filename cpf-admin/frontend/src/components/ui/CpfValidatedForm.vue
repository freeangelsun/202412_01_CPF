<script setup lang="ts" generic="T extends Record<string, unknown>">
import { reactive, ref } from "vue";
import type { ZodType } from "zod";
const props=withDefaults(defineProps<{
  schema:ZodType<T>;
  model:T;
  onSubmit:(data:T)=>Promise<void>|void;
  submitLabel?:string;
  dangerous?:boolean;
  disabled?:boolean;
}>(),{submitLabel:"저장",dangerous:false,disabled:false});
const errors=reactive<Record<string,string>>({}); const busy=ref(false);
async function submit(){
  Object.keys(errors).forEach(k=>delete errors[k]);
  const parsed=props.schema.safeParse(props.model);
  if(!parsed.success){for(const issue of parsed.error.issues)errors[issue.path.join('.')||'_form']=issue.message;return;}
  busy.value=true;
  try{await props.onSubmit(parsed.data)}catch(error){errors._form=error instanceof Error?error.message:String(error);throw error}finally{busy.value=false}
}
</script>
<template>
  <el-form :model="model" label-position="top" @submit.prevent="submit">
    <slot :errors="errors" :busy="busy" />
    <el-alert v-if="errors._form" :title="errors._form" type="error" show-icon/>
    <el-button native-type="submit" :loading="busy" :disabled="disabled" :type="dangerous?'danger':'primary'">{{submitLabel}}</el-button>
  </el-form>
</template>
