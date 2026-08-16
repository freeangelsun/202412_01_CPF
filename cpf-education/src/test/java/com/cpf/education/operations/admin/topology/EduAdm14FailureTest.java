package com.cpf.education.operations.admin.topology;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-14 FailureTest — Topology·Health·Capacity Drill-down */
public final class EduAdm14FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm14Handler(); }
}
