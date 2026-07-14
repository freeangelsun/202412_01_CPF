package cpf.pfw.common.logging.fallback;

import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.TransactionLogListener;
import cpf.pfw.common.logging.policy.LogPolicyDecision;

import java.util.Map;

/**
 * PFW DB 거래 로그 durable fallback을 학습하기 위한 교육 샘플입니다.
 *
 * <p>업무 모듈이 journal 파일을 직접 다루지 않고 PFW capability만 호출해야 한다는
 * 경계를 보여줍니다. 실제 운영 저장은 {@link TransactionLogListener}가 자동으로 수행하고,
 * 이 샘플은 EDU 테스트와 개발 가이드의 호출 흐름 설명에 사용합니다.</p>
 */
public class PfwTransactionLogRecoveryEducationSample {
    private final TransactionLogFallbackStore fallbackStore;
    private final TransactionLogRecoveryWorker recoveryWorker;

    public PfwTransactionLogRecoveryEducationSample(
            TransactionLogFallbackStore fallbackStore,
            TransactionLogRecoveryWorker recoveryWorker) {
        // PFW가 제공한 저장소와 worker를 주입받아 업무 코드의 파일 I/O 중복 구현을 막습니다.
        this.fallbackStore = fallbackStore;
        this.recoveryWorker = recoveryWorker;
    }

    public boolean preserveDatabaseFailure(
            TransactionLogRecord record,
            Map<String, String> details,
            LogPolicyDecision policy,
            RuntimeException databaseFailure) {
        // DB 저장 예외 원문은 journal에 넣지 않고 예외 유형만 기록되도록 PFW 저장소에 위임합니다.
        return fallbackStore.enqueue(record, details, policy, databaseFailure);
    }

    public TransactionLogFallbackStore.FallbackSnapshot status() {
        // 운영 화면이 사용하는 동일 snapshot을 조회해 pending과 poison 상태를 학습합니다.
        return fallbackStore.snapshot();
    }

    public TransactionLogRecoveryWorker.RecoveryResult recoverNow(String auditReason) {
        // 수동 복구는 운영 감사 사유가 있어야 하므로 샘플에서도 빈 사유를 허용하지 않습니다.
        if (auditReason == null || auditReason.isBlank()) {
            throw new IllegalArgumentException("DB 거래 로그 수동 복구 감사 사유는 필수입니다.");
        }
        // 실제 ADM API는 권한검사와 감사 로그를 추가한 뒤 같은 worker 메서드를 호출합니다.
        return recoveryWorker.recoverPending();
    }
}
