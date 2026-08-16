import type { BzaOperatorResponse } from './bzaOperatorResponse';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BzaAuthorizationResult {
  actionCode?: string;
  menuCode?: string;
  operator?: BzaOperatorResponse;
}
