/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface CommandReservation {
  commandId: number;
  replayed: boolean;
  resultRef?: string;
}
