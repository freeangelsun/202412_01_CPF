/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Stored {
  path?: string;
  sha256?: string;
  size: number;
}
