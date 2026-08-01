package com.cpf.reference.optional.gateway.security;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-03 RecoveryTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
