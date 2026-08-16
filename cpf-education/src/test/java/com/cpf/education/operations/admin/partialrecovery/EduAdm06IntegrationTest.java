package com.cpf.education.operations.admin.partialrecovery;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-06 IntegrationTest — 부분 성공·대상별 복구 */
public final class EduAdm06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm06Handler(); }
}
