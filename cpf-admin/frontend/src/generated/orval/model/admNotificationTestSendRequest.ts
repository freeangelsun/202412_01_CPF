/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmNotificationTestSendRequest {
  message?: string;
  reason?: string;
  receiver?: string;
  targetId?: string;
  targetType?: string;
}
