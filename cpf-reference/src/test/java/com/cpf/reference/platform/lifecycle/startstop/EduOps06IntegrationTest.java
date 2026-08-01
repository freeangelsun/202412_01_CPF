package com.cpf.reference.platform.lifecycle.startstop;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-06 IntegrationTest — 기동·종료·Health·Dependency 순서 */
public final class EduOps06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps06Handler(); }
}
