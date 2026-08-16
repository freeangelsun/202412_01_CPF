/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmFeatureFlagEvaluateRequest {
  attributes?: Record<string, string>;
  flagKey?: string;
  targetingKey?: string;
  value?: string;
  valueType?: string;
}
