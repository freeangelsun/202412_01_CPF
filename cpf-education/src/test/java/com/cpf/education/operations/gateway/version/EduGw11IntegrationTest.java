package com.cpf.education.operations.gateway.version;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-11 IntegrationTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
