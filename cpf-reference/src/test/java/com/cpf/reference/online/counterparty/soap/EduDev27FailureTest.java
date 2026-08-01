package com.cpf.reference.online.counterparty.soap;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-27 FailureTest — SOAP·XML 외부기관 연계와 Fault 처리 */
public final class EduDev27FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev27Handler(); }
}
