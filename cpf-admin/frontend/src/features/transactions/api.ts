import { admMutation, admQuery } from "../../shared/cpfApi";

export interface TransactionMetaRow {
  transaction_id?: string; transactionId?: string;
  transaction_name?: string; transactionName?: string;
  module_code?: string; moduleCode?: string;
  domain_code?: string; domainCode?: string;
  http_method?: string; httpMethod?: string;
  api_path?: string; apiPath?: string;
  controller_class?: string; controllerClass?: string;
  handler_method?: string; handlerMethod?: string;
  swagger_operation_id?: string; swaggerOperationId?: string;
  log_policy_key?: string; logPolicyKey?: string;
  sensitive_yn?: string; sensitiveYn?: string;
  masking_policy_key?: string; maskingPolicyKey?: string;
  active_yn?: string; activeYn?: string;
  first_detected_at?: string; firstDetectedAt?: string;
  last_detected_at?: string; lastDetectedAt?: string;
  last_scanned_at?: string; lastScannedAt?: string;
  updated_by?: string; updatedBy?: string;
  updated_at?: string; updatedAt?: string;
  [key: string]: unknown;
}
export interface TransactionMetaPage {
  available: boolean; items: TransactionMetaRow[]; page: number; size: number;
  totalElements: number; totalPages: number;
}
export interface TransactionMetaDetail { available: boolean; item: TransactionMetaRow; }
export interface TransactionMetaScanResult { available: boolean; scanned?: number; inserted?: number; updated?: number; items?: unknown[]; message?: string; [key: string]: unknown; }

export function findTransactionMetaPage(query: { moduleCode?: string; activeYn?: string; transactionId?: string; page: number; size: number }) {
  return admQuery<TransactionMetaPage>("/adm/api/transactions/page", query);
}
export function findTransactionMeta(transactionId: string) {
  return admQuery<TransactionMetaDetail>(`/adm/api/transactions/${encodeURIComponent(transactionId)}`);
}
export function scanTransactionMeta(reason: string) {
  return admMutation<TransactionMetaScanResult>(`/adm/api/transactions/scan?reason=${encodeURIComponent(reason)}`, "POST");
}
export function inactivateTransactionMeta(transactionId: string, reason: string) {
  return admMutation<Record<string, unknown>>(`/adm/api/transactions/${encodeURIComponent(transactionId)}/inactive?reason=${encodeURIComponent(reason)}`, "POST");
}
