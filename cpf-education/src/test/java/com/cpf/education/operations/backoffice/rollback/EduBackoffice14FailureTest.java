package com.cpf.education.operations.backoffice.rollback;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-14 FailureTest — 고객 업무 승인 결과 반영·실패 Rollback */
public final class EduBackoffice14FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice14Handler(); }
}
