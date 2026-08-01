package com.cpf.reference.optional.gateway.security;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-03 FailureTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
