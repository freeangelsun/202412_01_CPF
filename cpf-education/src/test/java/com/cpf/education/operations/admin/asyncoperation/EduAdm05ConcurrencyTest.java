package com.cpf.education.operations.admin.asyncoperation;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-05 ConcurrencyTest — 비동기 작업·응답 유실 */
public final class EduAdm05ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm05Handler(); }
}
