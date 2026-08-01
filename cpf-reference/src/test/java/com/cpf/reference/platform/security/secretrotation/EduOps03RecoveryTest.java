package com.cpf.reference.platform.security.secretrotation;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-03 RecoveryTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
