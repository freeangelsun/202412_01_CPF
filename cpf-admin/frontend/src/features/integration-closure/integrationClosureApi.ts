import { admInvokeOperation } from '../../shared/cpfApi';

export interface CorrectionApprovalRequest {
  expectedVersion: number;
  idempotencyKey: string;
  reason: string;
  corrected: Record<string, unknown>;
}
export interface CorrectionExecutionRequest { reason: string }
export type IntegrationClosureResult = Record<string, unknown>;
export type WebhookDelivery = Record<string, unknown>;

/**
 * Operation-id facade backed by the canonical OpenAPI contract and same-origin ADM BFF client.
 * The browser session cookie remains HttpOnly; the shared client supplies CSRF and transaction context.
 */
export const integrationClosureApi = {
  cryptoStatus: () =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationCryptoStatus'),
  timeHealth: (zone = 'Asia/Seoul', maxSkewMillis = 1000) =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationTimeHealth', {
      query: { zone, maxSkewMillis },
    }),
  validate: (recordId: string, record: Record<string, unknown>) =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationDataQualityValidate', {
      path: { recordId }, body: record,
    }),
  requestCorrectionApproval: (quarantineId: string, body: CorrectionApprovalRequest) =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationDataQualityCorrectionApprovalRequest', {
      path: { id: quarantineId }, body,
    }),
  executeCorrectionApproval: (approvalRequestId: number, body: CorrectionExecutionRequest) =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationDataQualityCorrectionExecute', {
      path: { approvalRequestId }, body,
    }),
  replayQuality: (quarantineId: string, reason: string) =>
    admInvokeOperation<IntegrationClosureResult>('admIntegrationDataQualityReplay', {
      path: { id: quarantineId }, query: { reason },
    }),
  webhookDlq: (limit = 100) =>
    admInvokeOperation<WebhookDelivery[]>('admIntegrationWebhookDlq', {
      query: { limit: Math.min(500, Math.max(1, Math.trunc(limit))) },
    }),
  replayWebhook: (id: string, expectedVersion: number, reason: string) =>
    admInvokeOperation<WebhookDelivery>('admIntegrationWebhookReplay', {
      path: { id }, query: { expectedVersion, reason },
    }),
} as const;
