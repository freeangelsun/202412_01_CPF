import { onMounted, ref } from 'vue'
import { dashboardApi } from '../api/dashboardApi'
import type { DashboardPayload } from '../model/dashboardModel'

export function useDashboard() {
  const data = ref<DashboardPayload>()
  const error = ref('')
  const loading = ref(false)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      data.value = await dashboardApi.load()
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  onMounted(load)
  return { data, error, loading, load }
}
