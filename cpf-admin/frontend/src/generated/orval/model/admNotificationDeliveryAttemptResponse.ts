/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationDeliveryAttemptResponse {
  attemptNo: number;
  attemptStatus?: string;
  completedAt?: string;
  createdAt?: string;
  createdBy?: string;
  deliveryId: number;
  leaseVersion: number;
  operationId?: string;
  providerMessage?: string;
  providerStatus?: string;
  startedAt?: string;
  workerId?: string;
}
