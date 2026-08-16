package com.cpf.education.operations.admin.incident;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-12 UnitTest — Incident·Recovery Center 종단간 복구 */
public final class EduAdm12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm12Handler(); }
}
