package com.cpf.reference.optional.backoffice.rollback;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-14 UnitTest — 고객 업무 승인 결과 반영·실패 Rollback */
public final class EduBackoffice14UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice14Handler(); }
}
