package com.cpf.web.api;

import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.annotation.CpfOnlineTransaction;

/**
 * {@link CpfOnlineTransaction}의 보안·감사 정책을 실제 Runtime에서 평가하는 확장 계약입니다.
 *
 * <p>Web Runtime은 거래 메타데이터, CPF Context와 transactionId 연속성을 공통으로 보장하고,
 * 권한·감사 사유처럼 Security Owner가 알아야 하는 정책은 이 계약을 통해 위임합니다.
 * 정책이 선언되었는데 적합한 Evaluator가 없거나 둘 이상이면 fail-closed 합니다.</p>
 */
public interface CpfOnlineTransactionPolicyEvaluator {
    /** 해당 업무 Owner Domain 정책을 처리할 수 있는지 반환합니다. */
    boolean supports(String ownerDomain);

    /** 현재 거래의 보안·감사 정책을 검증하며 거부 시 예외를 발생시킵니다. */
    void verify(CpfOnlineTransaction transaction, CpfContext context);
}
