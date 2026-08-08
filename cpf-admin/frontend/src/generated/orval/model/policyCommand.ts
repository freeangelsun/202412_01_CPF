/** 승인 정책의 단계 정의. */
export interface PolicyCommandStep {
  stepNo: number;
  stepType: string;
  targetType: string;
  targetCode: string;
  decisionRule: 'ALL' | 'ANY' | 'N_OF_M' | string;
  requiredCount?: number | null;
  requiredYn: 'Y' | 'N';
}

/** 위험조치 승인 정책 저장 요청. Controller source 계약과 동일한 강타입 모델이다. */
export interface PolicyCommand {
  policyCode: string;
  policyVersion: number;
  policyName: string;
  actionType: string;
  effectiveFrom: string;
  effectiveTo?: string | null;
  enabledYn: 'Y' | 'N';
  selfApprovalAllowedYn: 'Y' | 'N';
  breakGlassAllowedYn: 'Y' | 'N';
  description?: string | null;
  steps: PolicyCommandStep[];
  reason: string;
}
