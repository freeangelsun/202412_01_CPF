package com.cpf.education.operations.admin.command;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-03 FailureTest — 안전한 운영 조치 */
public final class EduAdm03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm03Handler(); }
}
