package com.cpf.education.operations.backoffice.privacyexport;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-13 IntegrationTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
