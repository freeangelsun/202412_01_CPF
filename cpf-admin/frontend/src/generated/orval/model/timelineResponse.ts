/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface TimelineResponse {
  actionType?: string;
  actorId?: string;
  afterStatus?: string;
  approvalRequestId?: string;
  beforeStatus?: string;
  createdAt?: string;
  incidentId: number;
  reason?: string;
  timelineId: number;
}
