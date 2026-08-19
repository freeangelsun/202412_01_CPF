export interface EmployeeSearchCriteria {
  organizationCode: string
  status: string
  page: number
  size: number
}

export type EmployeePagePayload = unknown

export interface EmployeeSavePayload {
  employeeNo: string
  employeeName: string
  organizationCode?: string
  employmentStatus?: string
  positionCode?: string
  jobTitleCode?: string
  managerEmployeeNo?: string
  email?: string
  mobileNo?: string
  officePhoneNo?: string
  useYn: 'Y' | 'N'
  expectedVersion?: number
  reason: string
  clearEmail: boolean
  clearMobileNo: boolean
  clearOfficePhoneNo: boolean
}

export function validateEmployeeSavePayload(input: EmployeeSavePayload): EmployeeSavePayload {
  const employeeNo = input.employeeNo.trim()
  const employeeName = input.employeeName.trim()
  const reason = input.reason.trim()
  if (!employeeNo) throw new Error('직원번호가 필요합니다.')
  if (!employeeName) throw new Error('직원명이 필요합니다.')
  if (reason.length < 5) throw new Error('변경 사유는 5자 이상 입력해야 합니다.')
  return { ...input, employeeNo, employeeName, reason }
}
