/** Feature Flag override request requiring a separate approval. */
export interface AdmFeatureFlagOverrideRequest {
  flagKey: string;
  valueType: "BOOLEAN" | "STRING" | "INTEGER" | "DECIMAL" | "NUMBER";
  value: string;
  expiresAt: string;
  reason: string;
}
