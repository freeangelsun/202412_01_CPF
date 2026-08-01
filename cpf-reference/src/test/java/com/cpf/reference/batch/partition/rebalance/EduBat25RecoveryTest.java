package com.cpf.reference.batch.partition.rebalance;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-25 RecoveryTest — Partition 편향 감지·재분할 */
public final class EduBat25RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
