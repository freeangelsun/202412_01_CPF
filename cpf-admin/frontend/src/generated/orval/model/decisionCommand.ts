/** 멱등 승인/반려 결정 요청. */
export interface DecisionCommand {
  action: 'APPROVE' | 'REJECT';
  idempotencyKey: string;
  reason: string;
  breakGlass?: boolean;
}
