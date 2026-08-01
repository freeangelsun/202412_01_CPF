package com.cpf.reference.platform.configuration.validation;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-02 RecoveryTest — Profile·환경변수·설정값 전체 검증 */
public final class EduOps02RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps02Handler(); }
}
