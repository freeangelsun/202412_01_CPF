package com.cpf.education.operations.gateway.security;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-03 FailureTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
