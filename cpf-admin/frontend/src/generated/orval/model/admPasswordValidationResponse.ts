/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmPasswordValidationResponse {
  operatorId?: string;
  violations?: Array<string>;
}
