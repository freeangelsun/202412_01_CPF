package com.cpf.education.operations.admin.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-13 IntegrationTest — 감사 증적·다운로드·승인 반출 */
public final class EduAdm13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm13Handler(); }
}
