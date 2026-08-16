package com.cpf.education.operations.admin.detail;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-09 ConcurrencyTest — Expected Version 충돌 화면·재조회·재적용 */
public final class EduAdm09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm09Handler(); }
}
