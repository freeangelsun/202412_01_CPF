export type BzaHttpStatus = 401 | 403 | 404 | 409 | 429 | 500 | 503;

export interface BzaOperationState {
  status: number;
  retryable: boolean;
  action: "SIGN_IN" | "CHECK_PERMISSION" | "REFRESH" | "RETRY_LATER" | "CONTACT_SUPPORT";
  message: string;
}

function statusOf(error: unknown): number {
  if (typeof error === "object" && error !== null && "status" in error) {
    const status = Number((error as { status?: unknown }).status);
    return Number.isFinite(status) ? status : 0;
  }
  return 0;
}

/** BZA Generated Client 오류를 운영 화면이 수행할 수 있는 공통 복구 행동으로 변환합니다. */
export function bzaOperationState(error: unknown): BzaOperationState {
  const status = statusOf(error);
  switch (status as BzaHttpStatus) {
    case 401: return { status, retryable: false, action: "SIGN_IN", message: "로그인 상태가 만료되었습니다. 다시 로그인하세요." };
    case 403: return { status, retryable: false, action: "CHECK_PERMISSION", message: "이 작업을 수행할 권한이 없습니다." };
    case 404: return { status, retryable: false, action: "REFRESH", message: "대상이 없거나 이미 변경되었습니다. 목록을 다시 조회하세요." };
    case 409: return { status, retryable: false, action: "REFRESH", message: "다른 변경과 충돌했습니다. 최신 상태를 다시 조회하세요." };
    case 429: return { status, retryable: true, action: "RETRY_LATER", message: "요청이 많습니다. 잠시 후 다시 시도하세요." };
    case 500: return { status, retryable: true, action: "CONTACT_SUPPORT", message: "서버 처리 중 오류가 발생했습니다. 반복되면 운영 담당자에게 문의하세요." };
    case 503: return { status, retryable: true, action: "RETRY_LATER", message: "서비스가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도하세요." };
    default: return { status, retryable: status === 0 || status >= 500, action: "RETRY_LATER", message: "요청을 처리하지 못했습니다. 다시 시도하세요." };
  }
}
