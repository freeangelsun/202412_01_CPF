package com.cpf.reference.optional.backoffice.directory;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-07 IntegrationTest — 초기 관리자 Bootstrap·첫 로그인·권한 인계 */
public final class EduBackoffice07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice07Handler(); }
}
