// @vitest-environment jsdom
import { mount, type VueWrapper } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import EmployeeChangeForm from './EmployeeChangeForm.vue'
import type { EmployeeSavePayload } from '../model/employeeModel'

function inputFor(wrapper: VueWrapper, labelText: string) {
  const label = wrapper.findAll('label').find(candidate => candidate.text().includes(labelText))
  if (!label) throw new Error(`label missing: ${labelText}`)
  return label.find('input')
}

describe('EmployeeChangeForm', () => {
  it('emits every contact value and explicit clear intent through the typed save payload', async () => {
    const wrapper = mount(EmployeeChangeForm, { props: { loading: false } })
    await inputFor(wrapper, '사번').setValue('M001')
    await inputFor(wrapper, '이름').setValue('직원 이름')
    await inputFor(wrapper, '조직').setValue('ORG001')
    await inputFor(wrapper, '이메일').setValue('operator@example.com')
    await inputFor(wrapper, '연락처(휴대폰)').setValue('010-1234-5678')
    await inputFor(wrapper, '내부 전화번호').setValue('02-1234-5678')
    await wrapper.find('textarea').setValue('연락처 정보 변경')

    const clearMobile = inputFor(wrapper, '휴대폰 번호 삭제')
    await clearMobile.setValue(true)
    await nextTick()
    expect(inputFor(wrapper, '연락처(휴대폰)').attributes('disabled')).toBeDefined()

    await wrapper.find('form').trigger('submit')
    const payload = wrapper.emitted<EmployeeSavePayload[]>('save')?.[0]?.[0]
    expect(payload).toMatchObject({
      employeeNo: 'M001',
      employeeName: '직원 이름',
      organizationCode: 'ORG001',
      email: 'operator@example.com',
      mobileNo: '010-1234-5678',
      officePhoneNo: '02-1234-5678',
      clearEmail: false,
      clearMobileNo: true,
      clearOfficePhoneNo: false,
      reason: '연락처 정보 변경',
    })
  })

  it('prevents save interaction while a request is running', () => {
    const wrapper = mount(EmployeeChangeForm, { props: { loading: true } })
    expect(wrapper.get('button[type="submit"]').attributes('disabled')).toBeDefined()
  })
})
