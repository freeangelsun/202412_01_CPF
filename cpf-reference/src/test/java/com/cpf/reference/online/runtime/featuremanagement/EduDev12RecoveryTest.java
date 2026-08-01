package com.cpf.reference.online.runtime.featuremanagement;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-12 RecoveryTest — Cache·기능 전환·Secret 교체 */
public final class EduDev12RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev12Handler(); }
}
