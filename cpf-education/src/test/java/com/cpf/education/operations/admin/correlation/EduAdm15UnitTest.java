package com.cpf.education.operations.admin.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-15 UnitTest — Log·Trace·Transaction 상관 검색 */
public final class EduAdm15UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm15Handler(); }
}
