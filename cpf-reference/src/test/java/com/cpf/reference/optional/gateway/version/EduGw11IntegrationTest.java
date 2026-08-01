package com.cpf.reference.optional.gateway.version;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-11 IntegrationTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
