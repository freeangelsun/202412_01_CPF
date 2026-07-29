import { admApi, CpfApiError } from "../../shared/cpfApi";

export interface RuntimeInstance {
  instance_id: string
  runtime_role: string
  service_id: string
  host_alias?: string
  zone_id?: string
  pool_id?: string
  artifact_version: string
  desired_state: string
  effective_state: string
  last_heartbeat_at?: string
  fencing_token: number
}
export interface RuntimeEnvelope {
  fetchedAt: string
  stale: boolean
  partial: boolean
  errorCode?: string
  items: RuntimeInstance[]
}
export interface BatchViewEnvelope {
  fetchedAt: string
  stale: boolean
  partial: boolean
  errorCode?: string
  view: string
  items: Array<Record<string, unknown>>
}
async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  try {
    return await admApi<T>(url, options)
  } catch (error) {
    if (error instanceof CpfApiError && error.status === 503 && error.payload) {
      return error.payload as T
    }
    throw error
  }
}
export async function fetchRuntimeInstances(): Promise<RuntimeEnvelope> {
  return request('/adm/api/batch-runtime/instances', { credentials: 'same-origin' })
}
export async function fetchBatchView(view: string): Promise<BatchViewEnvelope> {
  return request(`/adm/api/batch-runtime/views/${encodeURIComponent(view)}`, { credentials: 'same-origin' })
}
export async function createDeploymentPlan(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/deployment-plans', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  })
}
