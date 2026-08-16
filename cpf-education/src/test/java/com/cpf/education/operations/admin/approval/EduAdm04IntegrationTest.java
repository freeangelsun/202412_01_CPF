package com.cpf.education.operations.admin.approval;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-04 IntegrationTest — 승인 필요한 위험 조치 */
public final class EduAdm04IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm04Handler(); }
}
