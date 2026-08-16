package com.cpf.education.operations.backoffice.organization;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-01 UnitTest — 조직·직원·발령·기준일 */
public final class EduBackoffice01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice01Handler(); }
}
