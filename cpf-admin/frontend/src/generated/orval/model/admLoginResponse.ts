import type { AdmMenu } from './admMenu';
import type { AdmOperator } from './admOperator';

/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmLoginResponse {
  accessToken?: string;
  buttonIds?: Array<string>;
  expiresInSeconds: number;
  menus?: Array<AdmMenu>;
  operator?: AdmOperator;
  tokenType?: string;
}
