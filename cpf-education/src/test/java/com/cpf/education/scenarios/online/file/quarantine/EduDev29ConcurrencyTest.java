package com.cpf.education.scenarios.online.file.quarantine;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-29 ConcurrencyTest — 악성코드 검사·격리·승인 해제 */
public final class EduDev29ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev29Handler(); }
}
