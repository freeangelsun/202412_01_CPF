package com.cpf.education.operations.backoffice.lifecycle;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-09 IntegrationTest — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver */
public final class EduBackoffice09IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice09Handler(); }
}
