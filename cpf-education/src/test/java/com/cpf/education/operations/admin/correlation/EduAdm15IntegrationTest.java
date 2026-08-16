package com.cpf.education.operations.admin.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-15 IntegrationTest — Log·Trace·Transaction 상관 검색 */
public final class EduAdm15IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm15Handler(); }
}
