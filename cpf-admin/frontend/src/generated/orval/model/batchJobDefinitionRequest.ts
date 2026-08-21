/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BatchJobDefinitionRequest {
  alertPolicy?: Record<string, unknown>;
  checksum?: string;
  definitionVersion?: number;
  dependencies?: Array<Record<string, unknown>>;
  description?: string;
  effectiveFrom?: string;
  effectiveUntil?: string;
  executorReference?: string;
  executorType: string;
  expectedRowVersion?: number;
  jobId: string;
  jobName: string;
  ownerDomain: string;
  parameters?: Array<Record<string, unknown>>;
  reason: string;
  recoveryPolicy?: Record<string, unknown>;
  resourcePolicy?: Record<string, unknown>;
  state: string;
  trigger?: Record<string, unknown>;
}
