package com.cpf.reference.batch.partition.rebalance;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-25 ConcurrencyTest — Partition 편향 감지·재분할 */
public final class EduBat25ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
