package com.cpf.reference.optional.backoffice.approvalhistory;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-11 FailureTest — 위임 중첩·기간 만료·결재 경로 재계산 */
public final class EduBackoffice11FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice11Handler(); }
}
