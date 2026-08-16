package com.cpf.education.operations.admin.topology;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-14 IntegrationTest — Topology·Health·Capacity Drill-down */
public final class EduAdm14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm14Handler(); }
}
