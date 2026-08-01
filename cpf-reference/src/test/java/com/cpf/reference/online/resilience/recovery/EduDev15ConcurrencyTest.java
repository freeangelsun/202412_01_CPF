package com.cpf.reference.online.resilience.recovery;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-15 ConcurrencyTest — 지급 업무 장애 주입·복구·운영 인계 */
public final class EduDev15ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev15Handler(); }
}
