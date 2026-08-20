export type ApprovalInboxPayload = unknown
export type ApprovalDetailPayload = unknown

export interface ApprovalDecisionRequest {
  action: string
  reason: string
  comment?: string
  idempotencyKey?: string
}

const APPROVAL_ACTIONS = new Set(['APPROVE', 'REJECT'])

export function validateApprovalDecision(input: ApprovalDecisionRequest): ApprovalDecisionRequest {
  const action = input.action.trim().toUpperCase()
  const reason = input.reason.trim()
  if (!APPROVAL_ACTIONS.has(action)) throw new Error('승인 또는 반려 action이 필요합니다.')
  if (reason.length < 5) throw new Error('결재 사유는 5자 이상 입력해야 합니다.')
  return {
    ...input,
    action,
    reason,
    comment: input.comment?.trim() || undefined,
    idempotencyKey: input.idempotencyKey?.trim() || undefined,
  }
}
