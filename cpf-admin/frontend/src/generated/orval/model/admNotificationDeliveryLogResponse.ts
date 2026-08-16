/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationDeliveryLogResponse {
  attemptCount: number;
  createdAt?: string;
  createdBy?: string;
  deliveredAt?: string;
  deliveryId: number;
  deliveryMessage?: string;
  deliveryStatus?: string;
  eventType?: string;
  lastErrorCode?: string;
  leaseOwner?: string;
  leaseUntil?: string;
  maxAttempts: number;
  nextAttemptAt?: string;
  operationId?: string;
  receiver?: string;
  requestHash?: string;
  requestedAt?: string;
  ruleId?: number;
  targetId?: string;
  targetType?: string;
  updatedAt?: string;
  updatedBy?: string;
  version: number;
}
