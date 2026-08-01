package com.cpf.reference.online.concurrency.lease;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-37 ConcurrencyTest — 온라인 분산 Lease·Fencing·소유권 상실 */
public final class EduDev37ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev37Handler(); }
}
