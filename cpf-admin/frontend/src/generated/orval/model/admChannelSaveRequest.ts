/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmChannelSaveRequest {
  active: boolean;
  authenticationRequired: boolean;
  channelName: string;
  channelType: string;
  clientChannel: boolean;
  description?: string;
  internalChannel: boolean;
  reason: string;
  signatureRequired: boolean;
  trustLevel: string;
}
