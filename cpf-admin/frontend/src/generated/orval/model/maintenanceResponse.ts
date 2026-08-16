/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface MaintenanceResponse {
  createdAt?: string;
  createdBy?: string;
  endsAt?: string;
  maintenanceCode?: string;
  maintenanceId: number;
  startsAt?: string;
  targetId?: string;
  targetType?: string;
  updatedAt?: string;
  updatedBy?: string;
  useYn?: string;
  version: number;
}
