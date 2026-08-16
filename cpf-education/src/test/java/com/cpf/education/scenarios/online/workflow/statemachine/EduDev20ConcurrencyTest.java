package com.cpf.education.scenarios.online.workflow.statemachine;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-20 ConcurrencyTest — 다단계 고객 업무 상태기계와 취소·재개 */
public final class EduDev20ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev20Handler(); }
}
