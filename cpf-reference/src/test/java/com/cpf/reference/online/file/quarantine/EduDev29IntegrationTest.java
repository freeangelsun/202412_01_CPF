package com.cpf.reference.online.file.quarantine;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-29 IntegrationTest — 악성코드 검사·격리·승인 해제 */
public final class EduDev29IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev29Handler(); }
}
