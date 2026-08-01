package com.cpf.reference.online.query.scoped;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-02 IntegrationTest — 권한·범위가 적용된 목록·상세 조회 */
public final class EduDev02IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev02Handler(); }
}
