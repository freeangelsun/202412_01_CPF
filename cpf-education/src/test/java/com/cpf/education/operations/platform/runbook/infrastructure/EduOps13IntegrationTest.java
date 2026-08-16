package com.cpf.education.operations.platform.runbook.infrastructure;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-13 IntegrationTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
