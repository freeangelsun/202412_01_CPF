package com.cpf.reference.online.counterparty.soap;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-27 ConcurrencyTest — SOAP·XML 외부기관 연계와 Fault 처리 */
public final class EduDev27ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev27Handler(); }
}
