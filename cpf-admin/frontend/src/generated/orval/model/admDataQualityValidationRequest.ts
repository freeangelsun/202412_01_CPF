import type { staticfinalint } from './staticfinalint';

/** Controller-source pre-runtime DTO schema. Authenticated operator fields are server-derived. */
export interface AdmDataQualityValidationRequest {
  MAX_FIELDS?: staticfinalint;
  fields?: Record<string, unknown>;
}
