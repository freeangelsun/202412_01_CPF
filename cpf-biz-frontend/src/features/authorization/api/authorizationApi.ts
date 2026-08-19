import { operationFindPermissionsPage, operationFindRolesPage } from '../../../generated/bza-api'

export const authorizationApi = {
  roles: () => operationFindRolesPage({ query: { page: 0, size: 20 } }),
  permissions: () => operationFindPermissionsPage({ query: { page: 0, size: 20 } }),
}
