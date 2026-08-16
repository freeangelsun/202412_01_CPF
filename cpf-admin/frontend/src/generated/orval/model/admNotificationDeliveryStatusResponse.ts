/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationDeliveryStatusResponse {
  attemptCount: number;
  deliveryId: number;
  deliveryStatus?: string;
  lastErrorCode?: string;
  leaseOwner?: string;
  leaseUntil?: string;
  maxAttempts: number;
  nextAttemptAt?: string;
  operationId?: string;
  requestHash?: string;
  updatedAt?: string;
  updatedBy?: string;
  version: number;
}
