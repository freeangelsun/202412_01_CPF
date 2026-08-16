/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicyStepRequest {
  decisionRule?: string;
  requiredCount?: number;
  requiredYn?: string;
  sortOrder?: number;
  stepNo?: number;
  stepType?: string;
  targetCode?: string;
  targetType?: string;
}
