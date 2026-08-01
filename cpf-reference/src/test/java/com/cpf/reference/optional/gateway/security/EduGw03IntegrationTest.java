package com.cpf.reference.optional.gateway.security;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-03 IntegrationTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
