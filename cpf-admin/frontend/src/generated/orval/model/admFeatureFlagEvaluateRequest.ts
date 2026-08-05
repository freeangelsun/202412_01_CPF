/** Typed Feature Flag evaluation request. */
export interface AdmFeatureFlagEvaluateRequest {
  flagKey: string;
  valueType: "BOOLEAN" | "STRING" | "INTEGER" | "DECIMAL" | "NUMBER";
  value: string;
  targetingKey?: string;
  attributes?: Record<string, string>;
}
