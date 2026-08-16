import type { BzaOperatorResponse } from './bzaOperatorResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface LoginResult {
  accessToken?: string;
  expiresIn: number;
  operator?: BzaOperatorResponse;
  refreshExpiresAt?: string;
  refreshToken?: string;
  tokenType?: string;
}
