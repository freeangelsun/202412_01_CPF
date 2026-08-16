package com.cpf.education.operations.admin.asyncoperation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-05 IntegrationTest — 비동기 작업·응답 유실 */
public final class EduAdm05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm05Handler(); }
}
