package com.cpf.reference.batch.partition.rebalance;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-25 UnitTest — Partition 편향 감지·재분할 */
public final class EduBat25UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat25Handler(); }
}
