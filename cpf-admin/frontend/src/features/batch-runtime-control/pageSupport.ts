import { CpfApiError } from '../../shared/cpfApi'

export type AnyRow = Record<string, unknown>
export function read(row: AnyRow | null | undefined, ...keys: string[]): unknown {
  if (!row) return undefined
  for (const key of keys) {
    if (row[key] !== undefined && row[key] !== null) return row[key]
    const found = Object.keys(row).find(candidate => candidate.toLowerCase() === key.toLowerCase())
    if (found && row[found] !== undefined && row[found] !== null) return row[found]
  }
  return undefined
}
export function text(row: AnyRow | null | undefined, ...keys: string[]): string {
  const value = read(row, ...keys)
  return value === undefined || value === null || String(value).trim() === '' ? '-' : String(value)
}
export function numberValue(row: AnyRow | null | undefined, ...keys: string[]): number {
  const value = Number(read(row, ...keys))
  return Number.isFinite(value) ? value : 0
}
export function safe(value: unknown, key = ''): unknown {
  if (/password|passwd|secret|token|authorization|cookie|credential|private.?key/i.test(key)) return '***'
  if (Array.isArray(value)) return value.map(item => safe(item))
  if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value as AnyRow).map(([childKey, child]) => [childKey, safe(child, childKey)]))
  return value
}
export function pretty(value: unknown): string { return JSON.stringify(safe(value), null, 2) }
export function apiMessage(error: unknown, fallback: string): string {
  if (!(error instanceof CpfApiError)) return error instanceof Error ? error.message : fallback
  const messages: Record<number, string> = {
    400: '요청 값과 상태 조건을 확인하세요.', 401: '세션이 만료됐습니다. 다시 로그인하세요.',
    403: '이 운영 기능을 수행할 권한 또는 승인이 없습니다.', 404: '대상 또는 BAT Owner API를 찾을 수 없습니다.',
    409: '다른 인스턴스 또는 운영자가 먼저 상태를 변경했습니다. 새로고침하세요.',
    429: '요청이 제한됐습니다. 잠시 후 다시 시도하세요.', 500: 'BAT 처리 중 서버 오류가 발생했습니다.',
    502: 'BAT Owner 호출 결과가 불명확합니다. 자동 재시도하지 말고 상태를 조회하세요.',
    503: 'BAT Control Server 또는 대상 Runtime을 사용할 수 없습니다.'
  }
  return messages[error.status] ?? `${fallback} (HTTP ${error.status})`
}
export function statusClass(value: unknown): string {
  const state = String(value ?? '').toUpperCase()
  if (['UP','ACTIVE','ENABLED','RUNNING','SUCCEEDED','APPLIED','PASS','HEALTHY','AVAILABLE'].includes(state)) return 'success'
  if (['UNKNOWN_RESULT','STALE','PARTIAL','DRAINING','STOPPING','RECOVERING','PENDING'].includes(state)) return 'warning'
  return 'danger'
}
