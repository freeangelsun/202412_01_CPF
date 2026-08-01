package com.cpf.reference.online.file.quarantine;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-29 FailureTest — 악성코드 검사·격리·승인 해제 */
public final class EduDev29FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev29Handler(); }
}
