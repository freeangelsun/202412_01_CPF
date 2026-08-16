package member.domain.base;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.api.CpfBaseService;

/**
 * member 업무 Service가 공통으로 재사용하는 Domain Base입니다.
 * Framework Base 위에서 Domain 식별자, TransactionId, 입력 정규화 정책을 한 곳에 고정합니다.
 */
public abstract class MemberBaseService extends CpfBaseService {
    protected static final String DOMAIN_NAME = "member";
    protected static final String SYSTEM_CODE = "MBR";

    protected final String requireTransactionId() {
        String transactionId = CpfContexts.requireCurrent().transactionId();
        return requireText(transactionId, "transactionId");
    }

    /** transactionSequence 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final long transactionSequence() {
        return CpfContexts.transactionSequence();
    }

    protected final String actorId() {
        String actor = CpfContexts.operatorId();
        return actor == null || actor.isBlank() ? SYSTEM_CODE : actor.trim();
    }

}
