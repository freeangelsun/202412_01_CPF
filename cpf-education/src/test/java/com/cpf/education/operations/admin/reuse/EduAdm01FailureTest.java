package com.cpf.education.operations.admin.reuse;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-01 FailureTest — 기존 ADM 기능 재사용 판단 */
public final class EduAdm01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm01Handler(); }
}
