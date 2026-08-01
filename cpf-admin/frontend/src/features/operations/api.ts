import { admMutation, admQuery } from "../../shared/cpfApi";

export type JsonMap = Record<string, unknown>;
export interface RuntimeChangeCommand {
  operationId: string; changeType: string; reason: string;
  expectedVersion: number; approvalId?: string; breakGlassId?: string;
  payloadSchemaVersion: number; target: JsonMap; payload: JsonMap;
}
export const findRuntimeStatus = (environment?: string, serviceId?: string) => admQuery<JsonMap>("/adm/api/runtime-control/status", { environment, serviceId });
export const findRuntimeHealth = () => admQuery<JsonMap>("/adm/api/runtime-control/health");
export const findRuntimeCapabilities = () => admQuery<JsonMap[]>("/adm/api/runtime-control/capabilities");
export const findRuntimeChange = (changeId: string) => admQuery<JsonMap>(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}`);
export const findRuntimeOperation = (operationId: string) => admQuery<JsonMap>(`/adm/api/runtime-control/operations/${encodeURIComponent(operationId)}`);
export const previewRuntimeChange = (command: RuntimeChangeCommand) => admMutation<JsonMap>("/adm/api/runtime-control/preview-change", "POST", command);
export const createRuntimeChange = (command: RuntimeChangeCommand) => admMutation<JsonMap>("/adm/api/runtime-control/changes", "POST", command);
export const cancelRuntimeChange = (changeId: string, operationId: string, reason: string) => admMutation<JsonMap>(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}/cancel`, "POST", { operationId, reason });
export const rollbackRuntimeChange = (changeId: string, operationId: string, reason: string) => admMutation<JsonMap>(`/adm/api/runtime-control/changes/${encodeURIComponent(changeId)}/rollback`, "POST", { operationId, reason });

export const traceTransaction = (transactionId: string, limit = 100) => admQuery<JsonMap>(`/adm/api/observability/transactions/${encodeURIComponent(transactionId)}`, { limit });
export const traceByTraceId = (traceId: string, limit = 100) => admQuery<JsonMap>(`/adm/api/observability/traces/${encodeURIComponent(traceId)}`, { limit });
export const traceBusinessTransaction = (businessTransactionId: string, limit = 100) => admQuery<JsonMap>(`/adm/api/observability/business-transactions/${encodeURIComponent(businessTransactionId)}`, { limit });

export const findUnknownResults = (status?: string, transactionId?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/unknown-results", { status, transactionId, limit });
export const findDlq = (status?: string, transactionId?: string, topic?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/broker/dlq", { status, transactionId, topic, limit });
export const findOutbox = (status?: string, transactionId?: string, topic?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/broker/outbox", { status, transactionId, topic, limit });
export const findInbox = (status?: string, key?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/broker/inbox", { status, key, limit });
export const findIdempotency = (scope?: string, status?: string, key?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/idempotency", { scope, status, key, limit });
export const findFileTransfers = (status?: string, transactionId?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/file-transfers", { status, transactionId, limit });
export const findRecovery = (status?: string, limit = 100) => admQuery<JsonMap[]>("/adm/api/reliability/transaction-log-recovery", { status, limit });
export const resolveUnknown = (unknownId: string, action: string, reason: string, expectedVersion: number) => admMutation<JsonMap>(`/adm/api/reliability/unknown-results/${encodeURIComponent(unknownId)}/resolve`, "POST", { action, reason, expectedVersion });
export const replayDlq = (messageId: string, reason: string, expectedVersion: number) => admMutation<JsonMap>(`/adm/api/reliability/broker/dlq/${encodeURIComponent(messageId)}/replay`, "POST", { reason, expectedVersion });
export const runRecovery = (reason: string) => admMutation<JsonMap>("/adm/api/reliability/transaction-log-recovery/run", "POST", { reason });

export const findMessages = () => admQuery<JsonMap[]>("/adm/api/messages");
export const findMessage = (messageId: string | number) => admQuery<JsonMap>(`/adm/api/messages/${encodeURIComponent(String(messageId))}`);

export interface MessageCommand extends JsonMap {
  messageId?: number
  messageCode: string
  locale: string
  messageFormatType: 'FIXED' | 'INDEXED' | 'NAMED'
  externalMessage: string
  internalMessage: string
  parameterCount: number
  parameterSample?: string
  description?: string
  useYn: 'Y' | 'N'
  reason: string
}
export const traceTransactionGroup = (transactionId: string) =>
  admQuery<JsonMap>(`/adm/api/transaction-groups/${encodeURIComponent(transactionId)}/timeline`)
export const createMessage = (command: MessageCommand) =>
  admMutation<JsonMap>('/adm/api/messages', 'POST', command)
export const updateMessage = (messageId: string | number, command: MessageCommand) =>
  admMutation<JsonMap>(`/adm/api/messages/${encodeURIComponent(String(messageId))}`, 'PUT', command)
export const deleteMessage = (messageId: string | number, reason: string) =>
  admMutation<JsonMap[]>(`/adm/api/messages/${encodeURIComponent(String(messageId))}?reason=${encodeURIComponent(reason)}`, 'DELETE')
