package com.cpf.education.scenarios.online.security.cryptography;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-32 IntegrationTest — 개인정보 암호화·Tokenization·Key Rotation */
public final class EduDev32IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev32Handler(); }
}
