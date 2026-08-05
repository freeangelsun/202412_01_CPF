import type { CpfRuntimePayload } from './cpfRuntimePayload';
import type { CpfRuntimeTargetSelector } from './cpfRuntimeTargetSelector';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlCreateChangeRequest {
  approvalId?: string;
  breakGlassId?: string;
  changeType?: string;
  expectedVersion?: number;
  expiresAt?: string;
  operationId?: string;
  payload?: CpfRuntimePayload;
  payloadSchemaVersion: number;
  quorumPercent?: number;
  reason?: string;
  rolloutMode?: string;
  scheduledAt?: string;
  target?: CpfRuntimeTargetSelector;
  waveSize?: number;
}
