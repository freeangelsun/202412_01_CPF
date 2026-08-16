package com.cpf.education.scenarios.online.runtime.featuremanagement;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-12 UnitTest — Cache·기능 전환·Secret 교체 */
public final class EduDev12UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev12Handler(); }
}
