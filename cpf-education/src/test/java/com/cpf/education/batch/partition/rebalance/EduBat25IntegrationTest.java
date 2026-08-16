package com.cpf.education.batch.partition.rebalance;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-25 IntegrationTest — Partition 편향 감지·재분할 */
public final class EduBat25IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
