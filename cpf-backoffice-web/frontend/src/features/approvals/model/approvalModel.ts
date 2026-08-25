export type ApprovalInboxPayload = unknown
export type ApprovalDetailPayload = Record<string, unknown>

export interface ApprovalDecisionRequest {
  action: string
  reason: string
  comment?: string
  idempotencyKey?: string
  expectedVersionNo: number
  expectedPayloadHash: string
}

const APPROVAL_ACTIONS = new Set(['APPROVE', 'REJECT'])

export function validateApprovalDecision(input: ApprovalDecisionRequest): ApprovalDecisionRequest {
  const action = input.action.trim().toUpperCase()
  const reason = input.reason.trim()
  const expectedPayloadHash = input.expectedPayloadHash.trim()
  if (!APPROVAL_ACTIONS.has(action)) throw new Error('승인 또는 반려 action이 필요합니다.')
  if (reason.length < 5) throw new Error('결재 사유는 5자 이상 입력해야 합니다.')
  if (!Number.isInteger(input.expectedVersionNo) || input.expectedVersionNo < 1) {
    throw new Error('상세에서 확인한 결재 문서 Version이 필요합니다.')
  }
  if (!/^[a-f0-9]{64}$/i.test(expectedPayloadHash)) {
    throw new Error('상세에서 확인한 Snapshot Hash가 필요합니다.')
  }
  return {
    ...input,
    action,
    reason,
    comment: input.comment?.trim() || undefined,
    idempotencyKey: input.idempotencyKey?.trim() || undefined,
    expectedVersionNo: input.expectedVersionNo,
    expectedPayloadHash,
  }
}

export function asRecord(value: unknown): Record<string, unknown> | undefined {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
    ? value as Record<string, unknown>
    : undefined
}

export function asArray(value: unknown): unknown[] {
  return Array.isArray(value) ? value : []
}

export function text(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'string') return value
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  return JSON.stringify(value)
}

export function decisionSnapshot(detail: ApprovalDetailPayload | undefined): { approvalId: string; versionNo: number; payloadHash: string } | undefined {
  if (!detail) return undefined
  const approvalId = text(detail.approvalId)
  const rawVersion = detail.versionNo
  const versionNo = typeof rawVersion === 'number' ? rawVersion : Number(rawVersion)
  const payloadHash = typeof detail.payloadHash === 'string' ? detail.payloadHash : ''
  if (!approvalId || approvalId === '-' || !Number.isInteger(versionNo) || versionNo < 1 || !/^[a-f0-9]{64}$/i.test(payloadHash)) {
    return undefined
  }
  return { approvalId, versionNo, payloadHash }
}
