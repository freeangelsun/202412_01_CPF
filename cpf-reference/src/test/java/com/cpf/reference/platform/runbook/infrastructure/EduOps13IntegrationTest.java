package com.cpf.reference.platform.runbook.infrastructure;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-13 IntegrationTest — Disk·Memory·Network·DB 장애 Runbook */
public final class EduOps13IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps13Handler(); }
}
