import type { BzaOperatorResponse } from './bzaOperatorResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaCurrentOperatorResponse {
  loginDomain?: string;
  operator?: BzaOperatorResponse;
  tokenExpiresAt?: string;
}
