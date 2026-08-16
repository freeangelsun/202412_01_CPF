package com.cpf.education.operations.admin.configuration;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-11 UnitTest — 설정·기능전환·유지보수 창 운영 */
public final class EduAdm11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm11Handler(); }
}
