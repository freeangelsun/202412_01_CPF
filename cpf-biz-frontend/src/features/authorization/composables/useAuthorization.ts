import { ref } from 'vue'
import { authorizationApi } from '../api/authorizationApi'
import type { PermissionPagePayload, RolePagePayload } from '../model/authorizationModel'

export function useAuthorization() {
  const roles = ref<RolePagePayload>()
  const permissions = ref<PermissionPagePayload>()
  const error = ref('')
  const loading = ref(false)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const [rolePage, permissionPage] = await Promise.all([
        authorizationApi.roles(),
        authorizationApi.permissions(),
      ])
      roles.value = rolePage
      permissions.value = permissionPage
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : String(cause)
    } finally {
      loading.value = false
    }
  }

  return { roles, permissions, error, loading, load }
}
