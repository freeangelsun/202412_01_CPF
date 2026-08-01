package com.cpf.reference.optional.operations.detail;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-09 RecoveryTest — Expected Version 충돌 화면·재조회·재적용 */
public final class EduAdm09RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm09Handler(); }
}
