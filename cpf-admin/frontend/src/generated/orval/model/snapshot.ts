/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Snapshot {
  hash?: string;
  json?: string;
  replayCount: number;
  updatedAt?: string;
}
