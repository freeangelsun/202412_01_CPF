/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface IncidentResponse {
  acknowledgedAt?: string;
  correlationId?: string;
  createdAt?: string;
  createdBy?: string;
  escalationLevel: number;
  firstOccurredAt?: string;
  incidentId: number;
  lastOccurredAt?: string;
  occurrenceCount: number;
  ownerId?: string;
  policyCode?: string;
  policyId: number;
  resolvedAt?: string;
  severity?: string;
  sourceId?: string;
  sourceType?: string;
  status?: string;
  summary?: string;
  title?: string;
  transactionId?: string;
  updatedAt?: string;
  updatedBy?: string;
  version: number;
}
