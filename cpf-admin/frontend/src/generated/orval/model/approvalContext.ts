/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface ApprovalContext {
  approvalRequestId?: string;
  approvalRequesterId?: string;
}
