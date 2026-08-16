package com.cpf.education.operations.admin.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-15 RecoveryTest — Log·Trace·Transaction 상관 검색 */
public final class EduAdm15RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm15Handler(); }
}
