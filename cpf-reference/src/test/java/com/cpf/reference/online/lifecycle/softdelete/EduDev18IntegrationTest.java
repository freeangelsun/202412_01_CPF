package com.cpf.reference.online.lifecycle.softdelete;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-18 IntegrationTest — 논리 삭제·복원·보존기간 만료 */
public final class EduDev18IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev18Handler(); }
}
