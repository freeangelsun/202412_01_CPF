package com.cpf.education.operations.backoffice.separationofduties;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-10 UnitTest — 역할 충돌·직무분리·실효 권한 Simulation */
public final class EduBackoffice10UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice10Handler(); }
}
