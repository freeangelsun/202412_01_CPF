package com.cpf.education.scenarios.online.lifecycle.softdelete;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-18 FailureTest — 논리 삭제·복원·보존기간 만료 */
public final class EduDev18FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev18Handler(); }
}
