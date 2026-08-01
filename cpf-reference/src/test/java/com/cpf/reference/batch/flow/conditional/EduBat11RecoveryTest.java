package com.cpf.reference.batch.flow.conditional;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-11 RecoveryTest — 조건 분기·다단계 Job Flow */
public final class EduBat11RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
