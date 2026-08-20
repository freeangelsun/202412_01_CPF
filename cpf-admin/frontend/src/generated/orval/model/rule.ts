/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface Rule {
  allowYn?: string;
  apiPath?: string;
  httpMethod?: string;
}
