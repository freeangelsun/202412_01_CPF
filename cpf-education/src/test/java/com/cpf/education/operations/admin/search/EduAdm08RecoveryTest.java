package com.cpf.education.operations.admin.search;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-08 RecoveryTest — 권한·데이터 범위·Masking·사유 입력 연동 */
public final class EduAdm08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm08Handler(); }
}
