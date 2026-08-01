package com.cpf.reference.online.api.versioning;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-43 IntegrationTest — API Version 전환·하위 호환·폐기 */
public final class EduDev43IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev43Handler(); }
}
