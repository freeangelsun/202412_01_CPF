package com.cpf.reference.optional.gateway.security;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-03 ConcurrencyTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
