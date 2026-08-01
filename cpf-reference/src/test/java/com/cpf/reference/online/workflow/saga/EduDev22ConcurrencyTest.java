package com.cpf.reference.online.workflow.saga;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-22 ConcurrencyTest — 서비스 간 Saga 보상·수동 확정 */
public final class EduDev22ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev22Handler(); }
}
