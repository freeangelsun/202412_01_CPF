package com.cpf.education.operations.platform.runbook.infrastructure;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-13 RecoveryTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
