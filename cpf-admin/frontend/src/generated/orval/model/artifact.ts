/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Artifact {
  actor?: string;
  content?: Array<number>;
  expiresAt?: string;
  exportId?: string;
  fileName?: string;
  status?: string;
}
