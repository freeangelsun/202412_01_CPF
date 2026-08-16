import type { StepCommand } from './stepCommand';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicyCommand {
  actionType?: string;
  breakGlassAllowedYn?: string;
  description?: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  enabledYn?: string;
  policyCode?: string;
  policyName?: string;
  policyVersion: number;
  reason?: string;
  selfApprovalAllowedYn?: string;
  steps?: Array<StepCommand>;
}
