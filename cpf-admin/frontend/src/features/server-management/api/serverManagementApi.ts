import {
  admManagedServerDisable,
  admManagedServerFindAll,
  admManagedServerFindOne,
  admManagedServerSave,
  admRuntimeInventoryFindAll,
} from '../../../generated/orval/cpf-api'
import type { AdmManagedServerDisableRequest } from '../../../generated/orval/model/admManagedServerDisableRequest'
import type { AdmManagedServerFindAllParams } from '../../../generated/orval/model/admManagedServerFindAllParams'
import type { AdmManagedServerSaveRequest } from '../../../generated/orval/model/admManagedServerSaveRequest'
import type { AdmRuntimeInventoryFindAllParams } from '../../../generated/orval/model/admRuntimeInventoryFindAllParams'

export const serverManagementApi = {
  findServers(params: AdmManagedServerFindAllParams) {
    return admManagedServerFindAll(params)
  },
  findServer(managedServerId: string) {
    return admManagedServerFindOne(managedServerId)
  },
  saveServer(request: AdmManagedServerSaveRequest) {
    return admManagedServerSave(request)
  },
  disableServer(managedServerId: string, request: AdmManagedServerDisableRequest) {
    return admManagedServerDisable(managedServerId, request)
  },
  findRuntimeInventory(params: AdmRuntimeInventoryFindAllParams) {
    return admRuntimeInventoryFindAll(params)
  },
}
