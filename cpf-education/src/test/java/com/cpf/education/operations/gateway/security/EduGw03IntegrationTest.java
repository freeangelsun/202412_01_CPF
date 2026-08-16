package com.cpf.education.operations.gateway.security;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-03 IntegrationTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
