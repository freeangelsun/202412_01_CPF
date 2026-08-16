package com.cpf.education.operations.gateway.security;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-03 UnitTest — 인증·권한·TLS·HMAC·Nonce */
public final class EduGw03UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw03Handler(); }
}
