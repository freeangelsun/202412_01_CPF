package com.cpf.education.batch.partition.rebalance;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-25 RecoveryTest — Partition 편향 감지·재분할 */
public final class EduBat25RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
