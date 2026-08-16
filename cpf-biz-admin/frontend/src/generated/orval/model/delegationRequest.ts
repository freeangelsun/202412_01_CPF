/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DelegationRequest {
  approvalType?: string;
  businessDomain?: string;
  delegateEmployeeNo?: string;
  delegationId?: number;
  delegatorEmployeeNo?: string;
  reason?: string;
  useYn?: string;
  validFrom?: string;
  validTo?: string;
}
