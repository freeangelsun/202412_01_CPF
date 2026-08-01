package com.cpf.reference.platform.database.lifecycle;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-04 RecoveryTest — DB 3종 신규 설치·Migration·Drift·Rollback */
public final class EduOps04RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps04Handler(); }
}
