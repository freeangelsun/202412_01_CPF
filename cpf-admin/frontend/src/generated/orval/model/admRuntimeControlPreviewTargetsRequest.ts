import type { CpfRuntimeTargetSelector } from './cpfRuntimeTargetSelector';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlPreviewTargetsRequest {
  changeType?: string;
  payloadSchemaVersion: number;
  target?: CpfRuntimeTargetSelector;
}
