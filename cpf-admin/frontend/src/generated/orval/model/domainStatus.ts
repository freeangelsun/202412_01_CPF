/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface DomainStatus {
  domain?: string;
  sample?: string;
  status?: string;
}
