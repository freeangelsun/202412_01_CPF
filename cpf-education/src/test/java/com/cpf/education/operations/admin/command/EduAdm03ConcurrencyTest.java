package com.cpf.education.operations.admin.command;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-03 ConcurrencyTest — 안전한 운영 조치 */
public final class EduAdm03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm03Handler(); }
}
