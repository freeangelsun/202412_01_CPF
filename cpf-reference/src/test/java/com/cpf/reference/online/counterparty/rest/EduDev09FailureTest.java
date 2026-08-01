package com.cpf.reference.online.counterparty.rest;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-09 FailureTest — 외부 REST 신용조회와 결과 미확정 */
public final class EduDev09FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev09Handler(); }
}
