package com.cpf.education.batch.partition.rebalance;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-25 ConcurrencyTest — Partition 편향 감지·재분할 */
public final class EduBat25ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
