/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmTransactionGroupFindBySubjectRequest {
  from?: string;
  limit: number;
  reason?: string;
  subjectId?: string;
  subjectType?: string;
  to?: string;
}
