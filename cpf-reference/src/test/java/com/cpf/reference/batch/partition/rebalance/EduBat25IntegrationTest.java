package com.cpf.reference.batch.partition.rebalance;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-25 IntegrationTest — Partition 편향 감지·재분할 */
public final class EduBat25IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
