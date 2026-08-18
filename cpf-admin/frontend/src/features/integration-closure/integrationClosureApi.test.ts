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
const resetGeneratedMocks = () => {
  vi.mocked(admIntegrationCryptoStatus).mockReset(); vi.mocked(admIntegrationTimeHealth).mockReset();
  vi.mocked(admIntegrationDataQualityValidate).mockReset(); vi.mocked(admIntegrationDataQualityCorrectionApprovalRequest).mockReset();
  vi.mocked(admIntegrationDataQualityCorrectionExecute).mockReset(); vi.mocked(admIntegrationDataQualityReplay).mockReset();
  vi.mocked(admIntegrationWebhookDlq).mockReset(); vi.mocked(admIntegrationWebhookReplay).mockReset();
};
afterEach(resetGeneratedMocks);

describe('integration closure generated API facade', () => {
  it('binds every operation to its generated function and typed request boundary', async () => {
    vi.mocked(admIntegrationCryptoStatus).mockResolvedValue({} as never); vi.mocked(admIntegrationTimeHealth).mockResolvedValue({} as never);
    vi.mocked(admIntegrationDataQualityValidate).mockResolvedValue({} as never); vi.mocked(admIntegrationDataQualityCorrectionApprovalRequest).mockResolvedValue({} as never);
    vi.mocked(admIntegrationDataQualityCorrectionExecute).mockResolvedValue({} as never); vi.mocked(admIntegrationDataQualityReplay).mockResolvedValue({} as never);
    vi.mocked(admIntegrationWebhookDlq).mockResolvedValue({} as never); vi.mocked(admIntegrationWebhookReplay).mockResolvedValue({} as never);
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
