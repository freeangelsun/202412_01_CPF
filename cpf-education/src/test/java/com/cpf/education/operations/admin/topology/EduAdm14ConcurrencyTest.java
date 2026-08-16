package com.cpf.education.operations.admin.topology;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-14 ConcurrencyTest — Topology·Health·Capacity Drill-down */
public final class EduAdm14ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm14Handler(); }
}
