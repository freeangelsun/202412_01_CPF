/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface StepDecision {
  approvedCount: number;
  decisionRule?: string;
  participantCount: number;
  requiredCount?: number;
  stepNo: number;
}
