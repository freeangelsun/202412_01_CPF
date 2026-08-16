/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface PositionRequest {
  expectedVersion?: number;
  positionCode?: string;
  positionName?: string;
  rankOrder?: number;
  reason?: string;
  useYn?: string;
}
