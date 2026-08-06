import { afterEach, describe, expect, it, vi } from 'vitest';
import { admInvokeOperation } from '../../shared/cpfApi';
import { integrationClosureApi } from './integrationClosureApi';

vi.mock('../../shared/cpfApi', () => ({ admInvokeOperation: vi.fn() }));
const invoke = vi.mocked(admInvokeOperation);
afterEach(() => invoke.mockReset());

describe('integration closure operation-id facade', () => {
  it('routes every declared operation through the canonical ADM BFF client', async () => {
    invoke.mockResolvedValue({});
    await integrationClosureApi.cryptoStatus();
    await integrationClosureApi.timeHealth('Asia/Seoul', 1500);
    await integrationClosureApi.validate('R-1', { name: 'Kim' });
    await integrationClosureApi.requestCorrectionApproval('DQ-1', {
      expectedVersion: 3, idempotencyKey: 'idem-1', reason: 'fix invalid name', corrected: { name: 'Kim' },
    });
    await integrationClosureApi.executeCorrectionApproval(77, { reason: 'execute approved correction' });
    await integrationClosureApi.replayQuality('DQ-1', 'validated correction');
    await integrationClosureApi.webhookDlq(25);
    await integrationClosureApi.replayWebhook('WH-1', 4, 'provider recovered');

    expect(invoke.mock.calls.map(call => call[0])).toEqual([
      'admIntegrationCryptoStatus',
      'admIntegrationTimeHealth',
      'admIntegrationDataQualityValidate',
      'admIntegrationDataQualityCorrectionApprovalRequest',
      'admIntegrationDataQualityCorrectionExecute',
      'admIntegrationDataQualityReplay',
      'admIntegrationWebhookDlq',
      'admIntegrationWebhookReplay',
    ]);
    expect(invoke).toHaveBeenCalledWith('admIntegrationDataQualityCorrectionApprovalRequest', {
      path: { id: 'DQ-1' },
      body: { expectedVersion: 3, idempotencyKey: 'idem-1', reason: 'fix invalid name', corrected: { name: 'Kim' } },
    });
    expect(invoke).toHaveBeenCalledWith('admIntegrationDataQualityCorrectionExecute', {
      path: { approvalRequestId: 77 }, body: { reason: 'execute approved correction' },
    });
  });
});
