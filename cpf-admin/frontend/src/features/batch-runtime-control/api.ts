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

export async function fetchJobDefinitions(jobId = "", state = ""): Promise<BatchViewEnvelope> {
  const params = new URLSearchParams({ limit: "500" });
  if (jobId) params.set("jobId", jobId); if (state) params.set("state", state);
  return request(`/adm/api/batch-runtime/job-definitions?${params.toString()}`, { credentials: "same-origin" });
}

export async function fetchJobDefinitionDetail(jobId:string, version:number): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch-runtime/job-definitions/${encodeURIComponent(jobId)}/versions/${version}`, { credentials: "same-origin" });
}
export async function validateJobDefinition(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/job-definitions/validate', { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}
export async function saveJobDefinition(body: unknown): Promise<Record<string, unknown>> {
  return request('/adm/api/batch-runtime/job-definitions/drafts', { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}
export async function transitionJobDefinition(jobId:string, version:number, body:unknown): Promise<Record<string, unknown>> {
  return request(`/adm/api/batch-runtime/job-definitions/${encodeURIComponent(jobId)}/versions/${version}/transition`, { method:'POST', credentials:'same-origin', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body) });
}
