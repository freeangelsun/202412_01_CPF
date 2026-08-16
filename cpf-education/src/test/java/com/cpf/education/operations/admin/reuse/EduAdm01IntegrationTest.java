package com.cpf.education.operations.admin.reuse;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-01 IntegrationTest — 기존 ADM 기능 재사용 판단 */
public final class EduAdm01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm01Handler(); }
}
