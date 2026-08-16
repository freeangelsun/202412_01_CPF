package com.cpf.education.scenarios.online.query.searchindex;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-45 RecoveryTest — 조회 모델·검색색인 Eventual Consistency */
public final class EduDev45RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev45Handler(); }
}
