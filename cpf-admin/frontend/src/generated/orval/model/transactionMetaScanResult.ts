/** Controller-source pre-runtime request schema. Authenticated operator fields are server-derived. */
export interface TransactionMetaScanResult {
  available: boolean;
  detectedCount: number;
  inactivatedCount: number;
  message?: string;
  transactionIds?: Array<string>;
  upsertedCount: number;
}
