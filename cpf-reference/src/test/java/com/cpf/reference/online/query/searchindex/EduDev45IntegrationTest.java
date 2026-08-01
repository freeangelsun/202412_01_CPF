package com.cpf.reference.online.query.searchindex;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-45 IntegrationTest — 조회 모델·검색색인 Eventual Consistency */
public final class EduDev45IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev45Handler(); }
}
