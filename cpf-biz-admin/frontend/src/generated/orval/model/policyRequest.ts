import type { PolicyStepRequest } from './policyStepRequest';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PolicyRequest {
  approvalType?: string;
  businessDomain?: string;
  description?: string;
  effectiveFrom?: string;
  effectiveTo?: string;
  enabledYn?: string;
  policyCode?: string;
  policyName?: string;
  policyVersion?: number;
  reason?: string;
  selfApprovalAllowedYn?: string;
  steps?: Array<PolicyStepRequest>;
}
