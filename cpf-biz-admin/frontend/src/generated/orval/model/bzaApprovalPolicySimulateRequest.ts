/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaApprovalPolicySimulateRequest {
  approvalType?: string;
  businessDomain?: string;
  effectiveAt?: string;
  policyCode?: string;
  policyVersion?: number;
  requesterEmployeeNo?: string;
}
