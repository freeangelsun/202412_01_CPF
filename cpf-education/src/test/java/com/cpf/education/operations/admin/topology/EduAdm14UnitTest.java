package com.cpf.education.operations.admin.topology;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-14 UnitTest — Topology·Health·Capacity Drill-down */
public final class EduAdm14UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm14Handler(); }
}
