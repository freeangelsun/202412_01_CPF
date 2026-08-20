import type { AdmMenu } from './admMenu';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmCurrentSessionResponse {
  allowedOperationIds?: Array<string>;
  buttonIds?: Array<string>;
  menus?: Array<AdmMenu>;
  operatorId?: string;
  passwordChangeRequired: boolean;
  roleIds?: Array<string>;
}
