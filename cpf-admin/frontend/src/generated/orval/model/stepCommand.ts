/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface StepCommand {
  decisionRule?: string;
  requiredCount?: number;
  requiredYn?: string;
  stepNo: number;
  stepType?: string;
  targetCode?: string;
  targetType?: string;
}
