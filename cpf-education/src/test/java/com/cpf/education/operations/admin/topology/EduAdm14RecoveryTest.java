package com.cpf.education.operations.admin.topology;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-14 RecoveryTest — Topology·Health·Capacity Drill-down */
public final class EduAdm14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm14Handler(); }
}
