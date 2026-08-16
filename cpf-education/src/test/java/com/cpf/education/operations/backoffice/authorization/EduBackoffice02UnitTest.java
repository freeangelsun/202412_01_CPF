package com.cpf.education.operations.backoffice.authorization;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-02 UnitTest — 사용자·역할·권한·실효 권한 */
public final class EduBackoffice02UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice02Handler(); }
}
