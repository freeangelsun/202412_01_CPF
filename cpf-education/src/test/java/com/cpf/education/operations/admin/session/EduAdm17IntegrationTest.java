package com.cpf.education.operations.admin.session;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-17 IntegrationTest — Browser 세션 만료·재로그인·위험 조치 안전성 */
public final class EduAdm17IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm17Handler(); }
}
