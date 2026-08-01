package com.cpf.reference.online.money.exchange;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-40 FailureTest — 금액·통화·반올림·환율 Version */
public final class EduDev40FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev40Handler(); }
}
