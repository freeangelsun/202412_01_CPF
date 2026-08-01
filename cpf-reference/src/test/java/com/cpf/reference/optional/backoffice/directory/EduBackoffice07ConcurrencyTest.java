package com.cpf.reference.optional.backoffice.directory;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-07 ConcurrencyTest — 초기 관리자 Bootstrap·첫 로그인·권한 인계 */
public final class EduBackoffice07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice07Handler(); }
}
