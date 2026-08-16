package com.cpf.education.scenarios.online.file.quarantine;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-29 FailureTest — 악성코드 검사·격리·승인 해제 */
public final class EduDev29FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev29Handler(); }
}
