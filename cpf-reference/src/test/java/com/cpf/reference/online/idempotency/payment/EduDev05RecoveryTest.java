package com.cpf.reference.online.idempotency.payment;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-05 RecoveryTest — 지급 등록 멱등성·응답 유실·결과 대사 */
public final class EduDev05RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev05Handler(); }
}
