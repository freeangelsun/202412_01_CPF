package com.cpf.reference.optional.operations.customscreen;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-07 RecoveryTest — 고객 전용 화면 추가의 마지막 선택 */
public final class EduAdm07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm07Handler(); }
}
