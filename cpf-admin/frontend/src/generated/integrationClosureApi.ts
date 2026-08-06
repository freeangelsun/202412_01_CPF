export interface CorrectionApprovalRequest {
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
  corrected: Record<string, unknown>;
}
export interface CorrectionExecutionRequest { reason: string }
export type IntegrationClosureResult = Record<string, unknown>;

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(path, {
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...(init.headers ?? {}) },
    ...init,
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(`${response.status} ${message || response.statusText}`);
  }
  return response.json() as Promise<T>;
}

const base = '/adm/api/integration-closure';
export const integrationClosureApi = {
  cryptoStatus: () => request<IntegrationClosureResult>(`${base}/crypto/status`),
  timeHealth: (zone = 'Asia/Seoul', maxSkewMillis = 1000) =>
    request<IntegrationClosureResult>(`${base}/time/health?zone=${encodeURIComponent(zone)}&maxSkewMillis=${maxSkewMillis}`),
  validate: (recordId: string, record: Record<string, unknown>) =>
    request<IntegrationClosureResult>(`${base}/data-quality/validate/${encodeURIComponent(recordId)}`, { method: 'POST', body: JSON.stringify(record) }),
  requestCorrectionApproval: (quarantineId: string, body: CorrectionApprovalRequest) =>
    request<IntegrationClosureResult>(`${base}/data-quality/quarantine/${encodeURIComponent(quarantineId)}/correction-approvals`, { method: 'POST', body: JSON.stringify(body) }),
  executeCorrectionApproval: (approvalRequestId: number, body: CorrectionExecutionRequest) =>
    request<IntegrationClosureResult>(`${base}/data-quality/correction-approvals/${approvalRequestId}/execute`, { method: 'POST', body: JSON.stringify(body) }),
  replayQuality: (quarantineId: string, reason: string) =>
    request<IntegrationClosureResult>(`${base}/data-quality/quarantine/${encodeURIComponent(quarantineId)}/replay?reason=${encodeURIComponent(reason)}`, { method: 'POST' }),
  webhookDlq: (limit = 100) => request<IntegrationClosureResult[]>(`${base}/webhooks/dlq?limit=${limit}`),
  replayWebhook: (id: string, expectedVersion: number, reason: string) =>
    request<IntegrationClosureResult>(`${base}/webhooks/${encodeURIComponent(id)}/replay?expectedVersion=${expectedVersion}&reason=${encodeURIComponent(reason)}`, { method: 'POST' }),
};
