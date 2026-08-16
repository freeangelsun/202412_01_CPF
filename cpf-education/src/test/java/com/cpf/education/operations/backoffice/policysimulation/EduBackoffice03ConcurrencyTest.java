package com.cpf.education.operations.backoffice.policysimulation;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-03 ConcurrencyTest — 결재정책 Version·경로 사전 계산 */
public final class EduBackoffice03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice03Handler(); }
}
