package com.cpf.reference.online.concurrency.optimisticlock;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-04 RecoveryTest — 동시 수정과 예상 Version 충돌 */
public final class EduDev04RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev04Handler(); }
}
