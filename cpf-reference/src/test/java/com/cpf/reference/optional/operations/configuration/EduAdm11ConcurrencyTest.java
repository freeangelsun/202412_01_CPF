package com.cpf.reference.optional.operations.configuration;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-11 ConcurrencyTest — 설정·기능전환·유지보수 창 운영 */
public final class EduAdm11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm11Handler(); }
}
