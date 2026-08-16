package com.cpf.education.operations.admin.configuration;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-11 FailureTest — 설정·기능전환·유지보수 창 운영 */
public final class EduAdm11FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm11Handler(); }
}
