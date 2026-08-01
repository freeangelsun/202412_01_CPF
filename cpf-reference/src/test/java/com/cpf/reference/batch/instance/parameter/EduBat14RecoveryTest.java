package com.cpf.reference.batch.instance.parameter;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-14 RecoveryTest — JobParameter 식별·중복 실행·새 Instance */
public final class EduBat14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat14Handler(); }
}
