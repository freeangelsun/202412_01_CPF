/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Reservation {
  approvalRequestId: number;
  commandRequestId?: string;
  executionStatus?: string;
  replay: boolean;
  requestedBy?: string;
}
