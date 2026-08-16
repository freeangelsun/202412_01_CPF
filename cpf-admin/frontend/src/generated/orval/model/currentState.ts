/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface CurrentState {
  status?: string;
  version: number;
}
