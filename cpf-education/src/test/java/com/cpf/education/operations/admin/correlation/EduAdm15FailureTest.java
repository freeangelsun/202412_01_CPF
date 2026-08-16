package com.cpf.education.operations.admin.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-15 FailureTest — Log·Trace·Transaction 상관 검색 */
public final class EduAdm15FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm15Handler(); }
}
