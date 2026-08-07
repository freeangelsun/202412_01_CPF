import { afterEach, describe, expect, it, vi } from 'vitest';
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
import { integrationClosureApi } from './integrationClosureApi';

vi.mock('../../generated/cpf-api', () => ({
  admIntegrationCryptoStatus: vi.fn(), admIntegrationTimeHealth: vi.fn(),
  admIntegrationDataQualityValidate: vi.fn(), admIntegrationDataQualityCorrectionApprovalRequest: vi.fn(),
  admIntegrationDataQualityCorrectionExecute: vi.fn(), admIntegrationDataQualityReplay: vi.fn(),
  admIntegrationWebhookDlq: vi.fn(), admIntegrationWebhookReplay: vi.fn(),
}));
const generated = [admIntegrationCryptoStatus, admIntegrationTimeHealth, admIntegrationDataQualityValidate,
  admIntegrationDataQualityCorrectionApprovalRequest, admIntegrationDataQualityCorrectionExecute,
  admIntegrationDataQualityReplay, admIntegrationWebhookDlq, admIntegrationWebhookReplay].map(vi.mocked);
afterEach(() => generated.forEach(fn => fn.mockReset()));

describe('integration closure generated API facade', () => {
  it('binds every operation to its generated function and typed request boundary', async () => {
    generated.forEach(fn => fn.mockResolvedValue({}));
    await integrationClosureApi.cryptoStatus();
    await integrationClosureApi.timeHealth('Asia/Seoul', 1500);
    await integrationClosureApi.validate('R-1', { name: 'Kim' });
    await integrationClosureApi.requestCorrectionApproval('DQ-1', {
      expectedVersion: 3, idempotencyKey: 'idem-1', reason: 'fix invalid name', corrected: { name: 'Kim' },
    });
    await integrationClosureApi.executeCorrectionApproval(77, { reason: 'execute approved correction' });
    await integrationClosureApi.replayQuality('DQ-1', { expectedVersion: 4, idempotencyKey: 'replay-1', reason: 'validated correction' });
    await integrationClosureApi.webhookDlq(25);
    await integrationClosureApi.replayWebhook('WH-1', 4, 'provider recovered');

    expect(admIntegrationDataQualityCorrectionApprovalRequest).toHaveBeenCalledWith({
      path: { id: 'DQ-1' }, data: { expectedVersion: 3, idempotencyKey: 'idem-1', reason: 'fix invalid name', corrected: { name: 'Kim' } },
    });
    expect(admIntegrationDataQualityCorrectionExecute).toHaveBeenCalledWith({
      path: { approvalRequestId: 77 }, data: { reason: 'execute approved correction' },
    });
    expect(admIntegrationDataQualityReplay).toHaveBeenCalledWith({
      path: { id: 'DQ-1' }, data: { expectedVersion: 4, idempotencyKey: 'replay-1', reason: 'validated correction' },
    });
  });
});
