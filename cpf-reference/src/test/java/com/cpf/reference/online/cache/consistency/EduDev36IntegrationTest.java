package com.cpf.reference.online.cache.consistency;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-36 IntegrationTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
