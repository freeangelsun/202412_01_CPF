package com.cpf.reference.optional.backoffice.approvalhistory;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-11 ConcurrencyTest — 위임 중첩·기간 만료·결재 경로 재계산 */
public final class EduBackoffice11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice11Handler(); }
}
