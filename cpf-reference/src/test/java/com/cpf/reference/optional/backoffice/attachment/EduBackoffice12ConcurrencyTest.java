package com.cpf.reference.optional.backoffice.attachment;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-12 ConcurrencyTest — 계정 잠금·비밀번호 초기화·세션 강제 종료 */
public final class EduBackoffice12ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice12Handler(); }
}
