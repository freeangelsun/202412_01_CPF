/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface NotificationSendResult {
  deliveredAt?: string;
  deliveryMessage?: string;
  deliveryStatus?: string;
  success: boolean;
}
