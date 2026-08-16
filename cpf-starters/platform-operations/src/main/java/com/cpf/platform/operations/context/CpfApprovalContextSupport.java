package com.cpf.platform.operations.context;

import java.time.Instant;

/**
 * Platform-Ops 승인 처리 메타데이터의 lexical scope를 관리합니다.
 * 승인 상태는 Core Context 확장 component가 아니며 Platform-Ops Owner 내부 실행 메타데이터입니다.
 */
public final class CpfApprovalContextSupport {
    private static final ThreadLocal<CpfApprovalContext> CURRENT = new ThreadLocal<>();

    public CpfApprovalContext current() { return CURRENT.get(); }

    public AutoCloseable bind(String id, String policy, String requester, String approver,
                              String reason, String state, String action) {
        CpfApprovalContext previous=CURRENT.get();
        CpfApprovalContext next=new CpfApprovalContext(id,policy,requester,approver,reason,Instant.now(),
                "APPROVED".equals(state)?Instant.now():null,state,action);
        CURRENT.set(next);
        return () -> { if(previous==null) CURRENT.remove(); else CURRENT.set(previous); };
    }
}
