package com.cpf.education.operations.gateway.security;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-03 RecoveryTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
