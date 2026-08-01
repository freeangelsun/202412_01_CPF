package com.cpf.reference.platform.runbook.infrastructure;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-13 FailureTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
