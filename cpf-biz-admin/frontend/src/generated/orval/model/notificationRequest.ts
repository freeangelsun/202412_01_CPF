/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface NotificationRequest {
  messageBody?: string;
  notificationType?: string;
  reason?: string;
  recipientLoginId?: string;
  referenceId?: string;
  referenceType?: string;
  title?: string;
}
