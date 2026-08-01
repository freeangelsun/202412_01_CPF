package com.cpf.reference.platform.runbook.infrastructure;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-13 ConcurrencyTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
