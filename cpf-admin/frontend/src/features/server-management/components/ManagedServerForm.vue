<script setup lang="ts">
import { z } from 'zod'
import CpfStructuredData from '../../../components/CpfStructuredData.vue'
import CpfValidatedForm from '../../../components/ui/CpfValidatedForm.vue'
import type { ManagedServerForm, ManagedServerRow } from '../model/serverManagementModel'

const props = defineProps<{ form: ManagedServerForm; selected: ManagedServerRow | null; loading: boolean }>()
const emit = defineEmits<{ save: []; disable: [] }>()
const schema = z.object({
  serverName: z.string().trim().min(1, 'Server Name은 필수입니다.'),
  environment: z.string().trim().min(1, 'Environment는 필수입니다.'),
  reason: z.string().trim().min(5, '감사 사유를 5자 이상 입력하세요.'),
}).passthrough() as unknown as z.ZodType<ManagedServerForm>
</script>
<template>
  <aside class="cpf-card">
    <h3>{{ props.selected ? 'Server Detail' : 'Server 등록' }}</h3>
    <CpfValidatedForm :schema="schema" :model="props.form" :on-submit="async()=>emit('save')" :disabled="props.loading" submit-label="저장">
      <template #default="{ errors }">
        <div class="cpf-form-grid one">
          <label>Server Name<input v-model.trim="props.form.serverName"><small v-if="errors.serverName" class="form-error">{{ errors.serverName }}</small></label>
          <label>Display Name<input v-model.trim="props.form.displayName"></label>
          <label>Environment<input v-model.trim="props.form.environment"><small v-if="errors.environment" class="form-error">{{ errors.environment }}</small></label>
          <label>Group<input v-model.trim="props.form.serverGroup"></label>
          <label>Hostname<input v-model.trim="props.form.hostname"></label>
          <label>Management Identity<input v-model.trim="props.form.managementIdentity"></label>
          <label>Zone<input v-model.trim="props.form.zone"></label>
          <label>Location<input v-model.trim="props.form.location"></label>
          <label>Description<textarea v-model.trim="props.form.description"></textarea></label>
          <label>Tags JSON<textarea v-model.trim="props.form.tagsJson"></textarea></label>
          <label>사유<input v-model.trim="props.form.reason"><small v-if="errors.reason" class="form-error">{{ errors.reason }}</small></label>
        </div>
      </template>
    </CpfValidatedForm>
    <div class="actions"><button v-if="props.selected" class="danger-button" :disabled="props.loading" @click="emit('disable')">비활성화</button></div>
    <CpfStructuredData v-if="props.selected" :value="props.selected" />
  </aside>
</template>
<style scoped>.one{grid-template-columns:1fr}.actions{display:flex;gap:.5rem}.danger-button{color:var(--el-color-danger,#f56c6c)}textarea{min-height:4rem}.form-error{color:var(--el-color-danger,#f56c6c)}</style>
