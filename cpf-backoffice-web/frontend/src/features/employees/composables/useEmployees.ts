import { reactive, ref } from 'vue'
import { employeeApi } from '../api/employeeApi'
import type { EmployeePagePayload, EmployeeSavePayload, EmployeeSearchCriteria } from '../model/employeeModel'

export function useEmployees() {
  const criteria = reactive<EmployeeSearchCriteria>({ organizationCode: '', status: '', page: 0, size: 20 })
  const result = ref<EmployeePagePayload>()
  const error = ref('')
  const loading = ref(false)

  async function search() {
    loading.value = true
    error.value = ''
    try {
      result.value = await employeeApi.search(criteria)
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  async function save(payload: EmployeeSavePayload) {
    error.value = ''
    try {
      await employeeApi.save(payload)
      await search()
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    }
  }

  return { criteria, result, error, loading, search, save }
}
