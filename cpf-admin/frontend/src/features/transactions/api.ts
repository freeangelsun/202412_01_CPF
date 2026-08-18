import {
  admTransactionMetaFindPage,
  admTransactionMetaFindTransaction,
  admTransactionMetaInactivate,
} from "../../generated/cpf-api";

/** ADM Operation Catalog/Policy가 반환하는 행의 느슨한 읽기 모델입니다. */
export interface OperationCatalogRow {
  operation_id?: string; operationId?: string; OPERATION_ID?: string;
  operation_name?: string; operationName?: string; OPERATION_NAME?: string;
  system_code?: string; systemCode?: string; SYSTEM_CODE?: string;
  domain_code?: string; domainCode?: string; DOMAIN_CODE?: string;
  application_code?: string; applicationCode?: string; APPLICATION_CODE?: string;
  http_method?: string; httpMethod?: string; HTTP_METHOD?: string;
  api_path?: string; apiPath?: string; API_PATH?: string;
  controller_class?: string; controllerClass?: string; CONTROLLER_CLASS?: string;
  handler_method?: string; handlerMethod?: string; HANDLER_METHOD?: string;
  openapi_operation_id?: string; openapiOperationId?: string; OPENAPI_OPERATION_ID?: string;
  discovery_status?: string; discoveryStatus?: string; DISCOVERY_STATUS?: string;
  first_seen_at?: string; firstSeenAt?: string; FIRST_SEEN_AT?: string;
  last_seen_at?: string; lastSeenAt?: string; LAST_SEEN_AT?: string;
  last_instance_id?: string; lastInstanceId?: string; LAST_INSTANCE_ID?: string;
  enabled_yn?: string; enabledYn?: string; ENABLED_YN?: string;
  all_callers_yn?: string; allCallersYn?: string; ALL_CALLERS_YN?: string;
  channel_policy_required_yn?: string; channelPolicyRequiredYn?: string; CHANNEL_POLICY_REQUIRED_YN?: string;
  policy_version?: number; policyVersion?: number; POLICY_VERSION?: number;
  change_reason?: string; changeReason?: string; CHANGE_REASON?: string;
  log_policy_key?: string; logPolicyKey?: string; LOG_POLICY_KEY?: string;
  sensitive_yn?: string; sensitiveYn?: string; SENSITIVE_YN?: string;
  masking_policy_key?: string; maskingPolicyKey?: string; MASKING_POLICY_KEY?: string;
  updated_by?: string; updatedBy?: string; UPDATED_BY?: string;
  updated_at?: string; updatedAt?: string; UPDATED_AT?: string;
  [key: string]: unknown;
}

export interface OperationCatalogPage {
  available: boolean; items: OperationCatalogRow[]; page: number; size: number;
  totalElements: number; totalPages: number;
}
export interface OperationCatalogDetail { available: boolean; item: OperationCatalogRow; }

export function findOperationCatalogPage(query: { systemCode?: string; activeYn?: string; operationId?: string; page: number; size: number }) {
  return admTransactionMetaFindPage<OperationCatalogPage>({
    query: {
      moduleCode: query.systemCode || undefined,
      activeYn: query.activeYn || undefined,
      operationId: query.operationId || undefined,
      page: query.page,
      size: query.size,
    },
  });
}

export function findOperationCatalog(operationId: string) {
  return admTransactionMetaFindTransaction<OperationCatalogDetail>({ path: { operationId } });
}

export function inactivateOperation(operationId: string, policyVersion: number, reason: string) {
  return admTransactionMetaInactivate<Record<string, unknown>>({
    path: { operationId },
    query: { policyVersion, reason },
  });
}
