<script setup lang="ts">
import { reactive } from 'vue'
import type { EmployeeSavePayload } from '../model/employeeModel'

const props = defineProps<{ loading: boolean }>()
const emit = defineEmits<{ save: [payload: EmployeeSavePayload] }>()
const form = reactive<EmployeeSavePayload>({
  employeeNo: '',
  employeeName: '',
  organizationCode: '',
  employmentStatus: 'EMPLOYED',
  useYn: 'Y',
  reason: '',
  clearEmail: false,
  clearMobileNo: false,
  clearOfficePhoneNo: false,
})

function submit() {
  if (!form.employeeNo.trim() || !form.employeeName.trim() || !form.reason.trim()) return
  emit('save', { ...form })
}
</script>

<template>
  <form class="reference-form" @submit.prevent="submit">
    <h3>직원 변경 Reference</h3>
    <div class="form-grid">
      <label>사번<input v-model.trim="form.employeeNo" required /></label>
      <label>이름<input v-model.trim="form.employeeName" required /></label>
      <label>조직<input v-model.trim="form.organizationCode" /></label>
      <label>재직상태<input v-model.trim="form.employmentStatus" /></label>
      <label>사용여부<select v-model="form.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
      <label>expectedVersion<input v-model.number="form.expectedVersion" min="0" type="number" /></label>
    </div>
    <label>변경 사유<textarea v-model.trim="form.reason" required rows="2" /></label>
    <button :disabled="props.loading" type="submit">저장</button>
  </form>
</template>
