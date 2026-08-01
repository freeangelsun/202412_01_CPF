package com.cpf.reference.optional.operations.query;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-02 UnitTest — 고객 업무 조회 연동 */
public final class EduAdm02UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm02Handler(); }
}
