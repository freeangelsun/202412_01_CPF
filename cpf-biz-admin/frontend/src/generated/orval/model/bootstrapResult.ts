/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface BootstrapResult {
  adminUserId: number;
  created: boolean;
  loginId?: string;
  operationId?: string;
}
