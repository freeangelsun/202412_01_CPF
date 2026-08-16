package com.cpf.education.operations.backoffice.delegation;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-05 UnitTest — 위임·대결·대행 책임 */
public final class EduBackoffice05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice05Handler(); }
}
