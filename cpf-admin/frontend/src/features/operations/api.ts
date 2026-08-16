import { admQuery } from "../../shared/cpfApi";
import {
  admMessageCreateMessage,
  admMessageDeleteMessage,
  admMessageUpdateMessage,
  admApprovalRequest,
  admRuntimeControlCancelChange,
  admRuntimeControlCreateChange,
  admRuntimeControlPreviewChange,
  admRuntimeControlRollbackChange,
  requestAdmBrokerDlqReplay,
  resolveAdmUnknownResult,
  runAdmTransactionLogRecovery,
  getAdmFileLogRecoveryStatus,
} from "../../generated/orval/cpf-api";

const generatedData = <T>(response: { data: unknown }): T => response.data as T;

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
export const previewRuntimeChange = async (command: RuntimeChangeCommand) =>
  generatedData<JsonMap>(await admRuntimeControlPreviewChange(command as Parameters<typeof admRuntimeControlPreviewChange>[0]));
export const createRuntimeChange = async (command: RuntimeChangeCommand) =>
  generatedData<JsonMap>(await admRuntimeControlCreateChange(command as Parameters<typeof admRuntimeControlCreateChange>[0]));
export const cancelRuntimeChange = async (changeId: string, operationId: string, reason: string) =>
  generatedData<JsonMap>(await admRuntimeControlCancelChange(changeId, { operationId, reason } as Parameters<typeof admRuntimeControlCancelChange>[1]));
export const rollbackRuntimeChange = async (changeId: string, operationId: string, reason: string) =>
  generatedData<JsonMap>(await admRuntimeControlRollbackChange(changeId, { operationId, reason } as Parameters<typeof admRuntimeControlRollbackChange>[1]));

export const requestRuntimeApproval = async (command: RuntimeChangeCommand, ownerCommand = "RUNTIME_CONTROL_CREATE", targetId?: string) => {
  const payload = ownerCommand === "RUNTIME_CONTROL_CREATE"
    ? command
    : { changeId: targetId ?? "", operationId: command.operationId, reason: command.reason };
  return generatedData<JsonMap>(await admApprovalRequest({
    requestKey: globalThis.crypto?.randomUUID?.() ?? `approval-${Date.now()}`,
    actionType: "RUNTIME_CONFIG_CHANGE",
    ownerModule: "cpf-starter-platform-operations-runtime-control",
    ownerCommand,
    targetType: "CPF_RUNTIME_CHANGE",
    targetId: targetId ?? command.operationId,
    payloadSnapshot: JSON.stringify(payload),
    reason: command.reason
  }));
};

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
export const findFileLogRecoveryStatus = async () => generatedData<JsonMap>(await getAdmFileLogRecoveryStatus());
export const resolveUnknown = async (unknownId: string, targetStatus: string, reason: string, expectedVersion: number) =>
  generatedData<JsonMap>(await resolveAdmUnknownResult(unknownId, { targetStatus, reason, expectedVersion }));
export const replayDlq = async (messageId: string, reason: string) =>
  generatedData<JsonMap>(await requestAdmBrokerDlqReplay(messageId, { reason }));
export const runRecovery = async (reason: string) =>
  generatedData<JsonMap>(await runAdmTransactionLogRecovery({ reason }));

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
export const createMessage = async (command: MessageCommand) =>
  generatedData<JsonMap>(await admMessageCreateMessage(command as Parameters<typeof admMessageCreateMessage>[0]))
export const updateMessage = async (messageId: string | number, command: MessageCommand) =>
  generatedData<JsonMap>(await admMessageUpdateMessage(Number(messageId), command as Parameters<typeof admMessageUpdateMessage>[1]))
export const deleteMessage = async (messageId: string | number, reason: string) =>
  generatedData<JsonMap[]>(await admMessageDeleteMessage(Number(messageId), { reason } as Parameters<typeof admMessageDeleteMessage>[1]))
