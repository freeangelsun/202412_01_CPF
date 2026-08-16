/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface SignalResult {
  incidentId?: number;
  observedCount: number;
  result?: string;
  signalId: number;
  suppressedByMaintenance: boolean;
  thresholdCount: number;
}
