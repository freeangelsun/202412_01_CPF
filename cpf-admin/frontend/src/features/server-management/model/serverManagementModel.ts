import type { AdmManagedServerResponse } from '../../../generated/orval/model/admManagedServerResponse'
import type { AdmManagedServerSaveRequest } from '../../../generated/orval/model/admManagedServerSaveRequest'
import type { AdmRuntimeInventoryResponse } from '../../../generated/orval/model/admRuntimeInventoryResponse'

export type ManagedServerRow = AdmManagedServerResponse & Record<string, unknown>
export type RuntimeInventoryRow = AdmRuntimeInventoryResponse & Record<string, unknown>
export type ManagedServerForm = AdmManagedServerSaveRequest & Record<string, unknown>

export interface ServerManagementFilters {
  environment: string
  status: string
  keyword: string
  capability: string
}

export function emptyManagedServerForm(): ManagedServerForm {
  return {
    serverName: '',
    displayName: '',
    hostname: '',
    managementIdentity: '',
    environment: '',
    serverGroup: '',
    zone: '',
    location: '',
    description: '',
    tagsJson: '',
    reason: 'Managed Server 등록',
  }
}
