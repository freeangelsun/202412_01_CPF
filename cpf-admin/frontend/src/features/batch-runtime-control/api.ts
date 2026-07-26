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
async function json<T>(response: Response): Promise<T> {
  const body = await response.json() as T
  if (!response.ok && response.status !== 503) throw new Error(`BAT request failed: ${response.status}`)
  return body
}
export async function fetchRuntimeInstances(): Promise<RuntimeEnvelope> {
  return json(await fetch('/adm/api/batch-runtime/instances', { credentials: 'same-origin' }))
}
export async function fetchBatchView(view: string): Promise<BatchViewEnvelope> {
  return json(await fetch(`/adm/api/batch-runtime/views/${encodeURIComponent(view)}`, { credentials: 'same-origin' }))
}
export async function createDeploymentPlan(body: unknown): Promise<Record<string, unknown>> {
  return json(await fetch('/adm/api/batch-runtime/deployment-plans', {
    method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
  }))
}
