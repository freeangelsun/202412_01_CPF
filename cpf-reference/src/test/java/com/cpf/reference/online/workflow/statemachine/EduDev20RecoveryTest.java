package com.cpf.reference.online.workflow.statemachine;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-20 RecoveryTest — 다단계 고객 업무 상태기계와 취소·재개 */
public final class EduDev20RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev20Handler(); }
}
