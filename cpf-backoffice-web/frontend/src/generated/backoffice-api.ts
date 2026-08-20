/* AUTO-GENERATED from openapi/cpf-openapi.json. Do not edit. */
import { invokeBackoffice } from '../shared/api/channelHttpClient'
export async function approvalInbox(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/inbox`, options) }
export async function approvalParticipantDecision(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/approvals/${encodeURIComponent(approvalId)}/decisions`, options) }
export async function approvalSubmissionDetail(approvalId: string, options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/approvals/submissions/${encodeURIComponent(approvalId)}`, options) }
export async function backofficeFindEmployeesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/backoffice/employees/page`, options) }
export async function backofficeSaveEmployee(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("POST", `/api/v1/backoffice/backoffice/employees`, options) }
export async function operationFindPermissionsPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/permissions/page`, options) }
export async function operationFindRolesPage(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/roles/page`, options) }
export async function supportDashboard(options: { query?: Record<string,string|number|boolean|undefined>; body?: unknown } = {}) { return invokeBackoffice("GET", `/api/v1/backoffice/dashboard`, options) }
