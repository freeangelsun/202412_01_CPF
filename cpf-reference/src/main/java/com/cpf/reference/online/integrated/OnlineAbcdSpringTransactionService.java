package com.cpf.reference.online.integrated;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * Online A→B→C(/D) reference flow를 Spring REQUIRED transaction boundary에 연결하는 service입니다.
 * <p>Domain 결과가 FAILED/UNKNOWN이면 exception이 외부로 전파되지 않더라도 rollback-only를 명시해
 * 부분 DB commit을 차단합니다. Remote side effect는 XA로 묶지 않고 UNKNOWN/Reconcile로 복구합니다.</p>
 */
public final class OnlineAbcdSpringTransactionService {
    private final OnlineAbcdReferenceFlow.Controller controller;
    private final TransactionTemplate required;

    /**
     * REQUIRED propagation transactional service를 생성합니다.
     * @param controller A→B→C 호출을 수행하는 domain controller
     * @param transactionManager 실제 DataSource/JTA transaction manager
     * @throws NullPointerException 필수 dependency가 null인 경우
     */
    public OnlineAbcdSpringTransactionService(
            OnlineAbcdReferenceFlow.Controller controller,
            PlatformTransactionManager transactionManager) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.required = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager"));
        this.required.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    /**
     * 동일 thread의 REQUIRED transaction 안에서 A→B→C를 실행하고 실패/UNKNOWN은 rollback-only로 종료합니다.
     * @param request transactionId/businessKey/payload/attempt 요청
     * @return domain 결과. UNKNOWN은 성공으로 승격하지 않습니다.
     * @throws IllegalStateException transaction callback이 결과 없이 종료된 경우
     */
    public OnlineAbcdReferenceFlow.Result execute(OnlineAbcdReferenceFlow.Request request) {
        OnlineAbcdReferenceFlow.Result result = required.execute(status -> {
            OnlineAbcdReferenceFlow.Result current = controller.execute(request);
            if (current.outcome() == OnlineAbcdReferenceFlow.Outcome.FAILED
                    || current.outcome() == OnlineAbcdReferenceFlow.Outcome.UNKNOWN
                    || current.outcome() == OnlineAbcdReferenceFlow.Outcome.CONFLICT) {
                status.setRollbackOnly();
            }
            return current;
        });
        if (result == null) throw new IllegalStateException("Spring transaction returned no result");
        return result;
    }
}
