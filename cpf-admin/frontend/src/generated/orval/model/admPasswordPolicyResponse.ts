/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmPasswordPolicyResponse {
  expireDays: number;
  historyCount: number;
  maxFailCount: number;
  minLength: number;
  requiredCategoryCount: number;
}
