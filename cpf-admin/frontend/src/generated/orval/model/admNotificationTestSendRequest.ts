/** ADM notification test-send input. The authenticated operator is resolved from the server session; requestUser is not accepted. */
export interface AdmNotificationTestSendRequest {
  targetType: string;
  targetId: string;
  receiver: string;
  message: string;
  reason: string;
}
