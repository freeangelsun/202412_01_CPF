package com.cpf.reference.optional.backoffice.attachment;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-12 IntegrationTest — 계정 잠금·비밀번호 초기화·세션 강제 종료 */
public final class EduBackoffice12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice12Handler(); }
}
