package com.cpf.education.operations.platform.runbook.infrastructure;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-13 FailureTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
