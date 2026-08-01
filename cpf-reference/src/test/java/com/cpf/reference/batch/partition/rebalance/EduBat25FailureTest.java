package com.cpf.reference.batch.partition.rebalance;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-25 FailureTest — Partition 편향 감지·재분할 */
public final class EduBat25FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
