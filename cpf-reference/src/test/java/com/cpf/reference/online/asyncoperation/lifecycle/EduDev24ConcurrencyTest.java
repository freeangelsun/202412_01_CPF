package com.cpf.reference.online.asyncoperation.lifecycle;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-24 ConcurrencyTest — 장시간 비동기 Operation 조회·취소 */
public final class EduDev24ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev24Handler(); }
}
