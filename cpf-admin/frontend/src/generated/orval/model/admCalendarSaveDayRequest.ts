/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface AdmCalendarSaveDayRequest {
  auditReason?: string;
  businessDay: boolean;
  dayType?: string;
  institutionCode?: string;
  reason?: string;
}
