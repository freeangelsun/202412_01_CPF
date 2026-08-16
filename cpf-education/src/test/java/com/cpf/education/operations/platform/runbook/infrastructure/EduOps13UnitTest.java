package com.cpf.education.operations.platform.runbook.infrastructure;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-13 UnitTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
