/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmRuntimeControlSaveGroupRequest {
  active: boolean;
  commandId?: string;
  description?: string;
  environment?: string;
  expectedVersion?: number;
  groupId?: string;
  groupName?: string;
  parentGroupId?: string;
  reason?: string;
}
