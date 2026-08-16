package com.cpf.education.operations.admin.detail;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-09 FailureTest — Expected Version 충돌 화면·재조회·재적용 */
public final class EduAdm09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm09Handler(); }
}
