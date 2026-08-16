package com.cpf.education.operations.admin.reuse;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-01 ConcurrencyTest — 기존 ADM 기능 재사용 판단 */
public final class EduAdm01ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm01Handler(); }
}
