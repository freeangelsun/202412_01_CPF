import { backofficeFindEmployeesPage, backofficeSaveEmployee } from '../../../generated/bza-api'
import { validateEmployeeSavePayload, type EmployeeSavePayload, type EmployeeSearchCriteria } from '../model/employeeModel'

export const employeeApi = {
  search(criteria: EmployeeSearchCriteria) {
    return backofficeFindEmployeesPage({
      query: { page: criteria.page, size: criteria.size, organizationCode: criteria.organizationCode || undefined, status: criteria.status || undefined },
    })
  },
  save(body: EmployeeSavePayload) {
    return backofficeSaveEmployee({ body: validateEmployeeSavePayload(body) })
  },
}
