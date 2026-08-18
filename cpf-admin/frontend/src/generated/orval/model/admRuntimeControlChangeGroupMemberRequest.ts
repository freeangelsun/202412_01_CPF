/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlChangeGroupMemberRequest {
  active: boolean;
  commandId?: string;
  groupId?: string;
  instanceId?: string;
  reason?: string;
}
