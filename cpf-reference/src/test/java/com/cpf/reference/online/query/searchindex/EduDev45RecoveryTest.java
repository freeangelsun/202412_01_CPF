package com.cpf.reference.online.query.searchindex;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-45 RecoveryTest — 조회 모델·검색색인 Eventual Consistency */
public final class EduDev45RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev45Handler(); }
}
