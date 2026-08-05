/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlChangeGroupMemberRequest {
  active: boolean;
  groupId?: string;
  instanceId?: string;
  operationId?: string;
  reason?: string;
}
