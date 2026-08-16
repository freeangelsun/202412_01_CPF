import {
  admIntegrationCryptoStatus,
  admIntegrationDataQualityCorrectionApprovalRequest,
  admIntegrationDataQualityCorrectionExecute,
  admIntegrationDataQualityReplay,
  admIntegrationDataQualityValidate,
  admIntegrationTimeHealth,
  admIntegrationWebhookDlq,
  admIntegrationWebhookReplay,
} from '../../generated/cpf-api';

export interface CorrectionApprovalRequest {
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
  corrected: Record<string, unknown>;
}
export interface CorrectionExecutionRequest { reason: string }
export interface QualityReplayRequest { expectedVersion: number; idempotencyKey: string; reason: string }
export type IntegrationClosureResult = Record<string, unknown>;
export type WebhookDelivery = Record<string, unknown>;

/**
 * Operation-id facade backed by the canonical OpenAPI contract and same-origin ADM BFF client.
 * The browser session cookie remains HttpOnly; the shared client supplies CSRF and transaction context.
 */
export const integrationClosureApi = {
  cryptoStatus: () =>
    admIntegrationCryptoStatus<IntegrationClosureResult>(),
  timeHealth: (zone = 'Asia/Seoul', maxSkewMillis = 1000) =>
    admIntegrationTimeHealth<IntegrationClosureResult>({ query: { zone, maxSkewMillis } }),
  validate: (recordId: string, record: Record<string, unknown>) =>
    admIntegrationDataQualityValidate<IntegrationClosureResult>({ path: { recordId }, data: record }),
  requestCorrectionApproval: (quarantineId: string, body: CorrectionApprovalRequest) =>
    admIntegrationDataQualityCorrectionApprovalRequest<IntegrationClosureResult>({ path: { id: quarantineId }, data: body }),
  executeCorrectionApproval: (approvalRequestId: number, body: CorrectionExecutionRequest) =>
    admIntegrationDataQualityCorrectionExecute<IntegrationClosureResult>({ path: { approvalRequestId }, data: body }),
  replayQuality: (quarantineId: string, body: QualityReplayRequest) =>
    admIntegrationDataQualityReplay<IntegrationClosureResult>({ path: { id: quarantineId }, data: body }),
  webhookDlq: (limit = 100) =>
    admIntegrationWebhookDlq<WebhookDelivery[]>({ query: { limit: Math.min(500, Math.max(1, Math.trunc(limit))) } }),
  replayWebhook: (id: string, expectedVersion: number, reason: string) =>
    admIntegrationWebhookReplay<WebhookDelivery>({ path: { id }, query: { expectedVersion, reason } }),
} as const;
