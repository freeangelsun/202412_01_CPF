import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { serverManagementApi } from '../api/serverManagementApi'
import {
  emptyManagedServerForm,
  type ManagedServerForm,
  type ManagedServerRow,
  type RuntimeInventoryRow,
  type ServerManagementFilters,
} from '../model/serverManagementModel'

export function useServerManagement() {
  const route = useRoute()
  const router = useRouter()
  const servers = ref<ManagedServerRow[]>([])
  const runtimes = ref<RuntimeInventoryRow[]>([])
  const selected = ref<ManagedServerRow | null>(null)
  const loading = ref(false)
  const error = ref('')
  const notice = ref('')
  const filters = reactive<ServerManagementFilters>({ environment: '', status: '', keyword: '', capability: '' })
  const serverPage = ref(1)
  const serverSize = ref(50)
  const serverTotal = ref(0)
  const runtimePage = ref(1)
  const runtimeSize = ref(50)
  const runtimeTotal = ref(0)
  const runtimeId = ref(String(route.query.runtime ?? ''))
  const form = reactive<ManagedServerForm>(emptyManagedServerForm())

  async function loadServers() {
    const response = await serverManagementApi.findServers({
      environment: filters.environment || undefined,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined,
      page: Math.max(0, serverPage.value - 1),
      size: serverSize.value,
    })
    const page = response.data
    servers.value = (page.items ?? []) as ManagedServerRow[]
    serverTotal.value = page.totalElements
    const requested = String(route.query.server ?? '')
    if (requested && !selected.value) {
      const found = servers.value.find(value => value.managedServerId === requested)
      if (found) await selectServer(found)
    }
  }

  async function loadRuntimes() {
    const response = await serverManagementApi.findRuntimeInventory({
      environment: filters.environment || undefined,
      capability: filters.capability || undefined,
      keyword: filters.keyword || undefined,
      page: Math.max(0, runtimePage.value - 1),
      size: runtimeSize.value,
    })
    const page = response.data
    runtimes.value = (page.items ?? []) as RuntimeInventoryRow[]
    runtimeTotal.value = page.totalElements
  }

  async function load() {
    loading.value = true
    error.value = ''
    try {
      await Promise.all([loadServers(), loadRuntimes()])
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  async function search() {
    serverPage.value = 1
    runtimePage.value = 1
    await load()
  }

  async function selectServer(row: ManagedServerRow) {
    const id = String(row.managedServerId || '')
    if (!id) return
    const response = await serverManagementApi.findServer(id)
    selected.value = response.data as ManagedServerRow
    Object.assign(form, {
      managedServerId: id,
      serverName: selected.value.serverName || '',
      displayName: selected.value.displayName || '',
      hostname: selected.value.hostname || '',
      managementIdentity: selected.value.managementIdentity || '',
      environment: selected.value.environment || '',
      serverGroup: selected.value.serverGroup || '',
      zone: selected.value.zone || '',
      location: selected.value.location || '',
      description: selected.value.description || '',
      tagsJson: selected.value.tagsJson || '',
      expectedVersion: Number(selected.value.rowVersion || 0),
      reason: 'Managed Server 수정',
    })
    await router.replace({ query: { ...route.query, server: id } })
  }

  function clearForm() {
    selected.value = null
    Object.assign(form, emptyManagedServerForm(), { managedServerId: undefined, expectedVersion: undefined })
    void router.replace({ query: { runtime: runtimeId.value || undefined } })
  }

  async function saveServer() {
    loading.value = true
    error.value = ''
    try {
      const response = await serverManagementApi.saveServer(form)
      notice.value = 'Managed Server가 저장되었습니다.'
      await loadServers()
      await selectServer(response.data as ManagedServerRow)
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  async function disableServer() {
    if (!selected.value?.managedServerId) return
    loading.value = true
    error.value = ''
    try {
      await serverManagementApi.disableServer(String(selected.value.managedServerId), {
        expectedVersion: Number(selected.value.rowVersion),
        reason: String(form.reason || ''),
      })
      notice.value = 'Managed Server가 DISABLED로 전환되었습니다.'
      clearForm()
      await loadServers()
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  async function changeServerPage(value: number) {
    serverPage.value = value
    await loadServers()
  }

  async function changeServerSize(value: number) {
    serverSize.value = value
    serverPage.value = 1
    await loadServers()
  }

  async function changeRuntimePage(value: number) {
    runtimePage.value = value
    await loadRuntimes()
  }

  async function changeRuntimeSize(value: number) {
    runtimeSize.value = value
    runtimePage.value = 1
    await loadRuntimes()
  }

  return {
    servers, runtimes, selected, loading, error, notice, filters,
    serverPage, serverSize, serverTotal, runtimePage, runtimeSize, runtimeTotal,
    runtimeId, form, load, search, selectServer, clearForm, saveServer, disableServer,
    changeServerPage, changeServerSize, changeRuntimePage, changeRuntimeSize,
  }
}
