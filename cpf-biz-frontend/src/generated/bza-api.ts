/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */
import { invokeBza } from '../shared/api/channelHttpClient'
export async function approvalInbox(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/approvals/inbox`, options) }
export async function approvalParticipantDecision(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("POST", `/api/bza/approvals/${encodeURIComponent(approvalId)}/decisions`, options) }
export async function approvalSubmissionDetail(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/approvals/submissions/${encodeURIComponent(approvalId)}`, options) }
export async function backofficeFindEmployeesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/backoffice/employees/page`, options) }
export async function backofficeSaveEmployee(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("POST", `/api/bza/backoffice/employees`, options) }
export async function operationFindPermissionsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/permissions/page`, options) }
export async function operationFindRolesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/roles/page`, options) }
export async function supportDashboard(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBza("GET", `/api/bza/dashboard`, options) }
