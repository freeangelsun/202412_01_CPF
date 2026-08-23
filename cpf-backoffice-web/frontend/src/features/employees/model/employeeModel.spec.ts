import { describe, expect, it } from 'vitest'
import { validateEmployeeSavePayload, type EmployeeSavePayload } from './employeeModel'

function request(overrides: Partial<EmployeeSavePayload> = {}): EmployeeSavePayload {
  return {
    employeeNo: ' M001 ',
    employeeName: ' 직원 이름 ',
    organizationCode: 'ORG001',
    useYn: 'Y',
    reason: ' 연락처 변경 ',
    clearEmail: false,
    clearMobileNo: false,
    clearOfficePhoneNo: false,
    ...overrides,
  }
}

describe('validateEmployeeSavePayload', () => {
  it('trims the canonical employee identity and audit reason without dropping contact intent', () => {
    const result = validateEmployeeSavePayload(request({
      email: 'operator@example.com',
      mobileNo: '010-1234-5678',
      clearOfficePhoneNo: true,
    }))

    expect(result).toMatchObject({
      employeeNo: 'M001',
      employeeName: '직원 이름',
      reason: '연락처 변경',
      email: 'operator@example.com',
      mobileNo: '010-1234-5678',
      clearOfficePhoneNo: true,
    })
  })

  it.each([
    [{ employeeNo: ' ' }, '직원번호가 필요합니다.'],
    [{ employeeName: ' ' }, '직원명이 필요합니다.'],
    [{ reason: '짧음' }, '변경 사유는 5자 이상 입력해야 합니다.'],
  ] as const)('rejects an incomplete canonical request', (overrides, message) => {
    expect(() => validateEmployeeSavePayload(request(overrides))).toThrow(message)
  })
})
