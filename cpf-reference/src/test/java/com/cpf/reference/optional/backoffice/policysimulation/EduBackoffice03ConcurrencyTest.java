package com.cpf.reference.optional.backoffice.policysimulation;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-03 ConcurrencyTest — 결재정책 Version·경로 사전 계산 */
public final class EduBackoffice03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice03Handler(); }
}
