/** 불변 승인 요청 생성 계약. Actor/권한은 서버에서 결정하며 클라이언트가 주입하지 않는다. */
export interface RequestCommand {
  requestKey: string;
  policyCode?: string | null;
  policyVersion?: number | null;
  actionType: string;
  ownerModule: string;
  ownerCommand: string;
  targetType: string;
  targetId: string;
  payloadSnapshot: string;
  expireAt?: string | null;
  reason: string;
}
