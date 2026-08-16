package com.cpf.education.operations.admin.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-15 ConcurrencyTest — Log·Trace·Transaction 상관 검색 */
public final class EduAdm15ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm15Handler(); }
}
