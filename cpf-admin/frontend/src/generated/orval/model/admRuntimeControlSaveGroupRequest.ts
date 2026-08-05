/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlSaveGroupRequest {
  active: boolean;
  description?: string;
  environment?: string;
  expectedVersion?: number;
  groupId?: string;
  groupName?: string;
  operationId?: string;
  parentGroupId?: string;
  reason?: string;
}
