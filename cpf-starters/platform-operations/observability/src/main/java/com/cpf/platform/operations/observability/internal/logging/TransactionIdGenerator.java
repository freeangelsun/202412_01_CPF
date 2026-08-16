package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.DefaultCpfTransactionIdGenerator;
import java.time.Clock;
import org.springframework.core.env.Environment;

/**
 * @deprecated transactionId 기본 발급 Owner는 Foundation Runtime입니다.
 *             기존 내부 참조 호환만 유지하며 Spring Bean으로 등록하지 않습니다.
 */
@Deprecated(forRemoval = true)
public class TransactionIdGenerator extends DefaultCpfTransactionIdGenerator {

    public TransactionIdGenerator(Environment environment) {
        super(environment, Clock.systemDefaultZone());
    }

    public TransactionIdGenerator(String moduleId, String wasId, int sequenceDigits, Clock clock) {
        super(moduleId, wasId, sequenceDigits, clock);
    }

    public static boolean isValid(String transactionId, int sequenceDigits) {
        return sequenceDigits == 7 && CpfTransactionIds.isCanonical(transactionId);
    }
}
