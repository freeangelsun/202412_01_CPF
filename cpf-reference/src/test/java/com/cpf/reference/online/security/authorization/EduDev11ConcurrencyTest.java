package com.cpf.reference.online.security.authorization;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-11 ConcurrencyTest — 권한·데이터 범위·개인정보 가림·감사 */
public final class EduDev11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev11Handler(); }
}
