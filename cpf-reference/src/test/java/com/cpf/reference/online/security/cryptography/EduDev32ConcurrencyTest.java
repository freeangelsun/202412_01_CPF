package com.cpf.reference.online.security.cryptography;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-32 ConcurrencyTest — 개인정보 암호화·Tokenization·Key Rotation */
public final class EduDev32ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev32Handler(); }
}
