/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MaintenanceSaveRequest {
  approvalRequestId?: string;
  endsAt?: string;
  expectedVersion: number;
  idempotencyKey?: string;
  maintenanceCode?: string;
  reason?: string;
  startsAt?: string;
  targetId?: string;
  targetType?: string;
  useYn?: string;
}
