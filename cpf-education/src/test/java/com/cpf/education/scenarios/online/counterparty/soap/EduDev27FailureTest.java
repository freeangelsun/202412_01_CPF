package com.cpf.education.scenarios.online.counterparty.soap;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-27 FailureTest — SOAP·XML 외부기관 연계와 Fault 처리 */
public final class EduDev27FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev27Handler(); }
}
