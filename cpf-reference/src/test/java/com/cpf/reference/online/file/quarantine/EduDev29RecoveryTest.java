package com.cpf.reference.online.file.quarantine;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-29 RecoveryTest — 악성코드 검사·격리·승인 해제 */
public final class EduDev29RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev29Handler(); }
}
