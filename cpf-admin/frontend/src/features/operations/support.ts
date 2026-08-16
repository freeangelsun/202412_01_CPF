import { CpfApiError } from "../../shared/cpfApi";
export type JsonMap = Record<string, unknown>;
export function read(row: JsonMap | null | undefined, ...keys: string[]): unknown {
  if (!row) return undefined;
  for (const key of keys) {
    if (row[key] !== undefined && row[key] !== null) return row[key];
    const found = Object.keys(row).find(candidate => candidate.toLowerCase() === key.toLowerCase());
    if (found && row[found] !== undefined && row[found] !== null) return row[found];
  }
  return undefined;
}
export function text(row: JsonMap | null | undefined, ...keys: string[]): string {
  const value = read(row, ...keys); return value === undefined || value === null || String(value).trim() === "" ? "-" : String(value);
}
export function errorMessage(error: unknown): string {
  if (error instanceof CpfApiError) {
    const known: Record<number,string> = {401:"세션이 만료됐습니다.",403:"이 작업을 수행할 권한이 없습니다.",404:"대상 또는 Owner API를 찾을 수 없습니다.",409:"상태 또는 버전 충돌입니다. 새로고침 후 다시 시도하세요.",429:"요청이 제한됐습니다. 잠시 후 다시 시도하세요.",500:"서버 처리 오류가 발생했습니다.",503:"Owner Runtime 또는 저장소를 사용할 수 없습니다."};
    return `${error.status}: ${known[error.status] ?? error.message}`;
  }
  return error instanceof Error ? error.message : "요청 처리에 실패했습니다.";
}
export function flattenTimeline(payload: JsonMap | null): JsonMap[] {
  if (!payload) return [];
  const groups = ["transactionLogs","failureLogs","auditLogs","policyAudits","batchExecutions","segments","externalCalls","incidents","approvals"];
  const rows: JsonMap[] = [];
  for (const group of groups) {
    const value = payload[group] ?? payload[group.replace(/[A-Z]/g, m => `_${m.toLowerCase()}`)];
    if (Array.isArray(value)) value.forEach(item => rows.push({ ...(item as JsonMap), eventType: group }));
  }
  return rows.sort((a,b) => String(read(a,"timestamp","createdAt","created_at","startedAt","started_at") ?? "").localeCompare(String(read(b,"timestamp","createdAt","created_at","startedAt","started_at") ?? "")));
}
