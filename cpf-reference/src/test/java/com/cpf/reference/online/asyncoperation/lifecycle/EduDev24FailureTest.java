package com.cpf.reference.online.asyncoperation.lifecycle;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-24 FailureTest — 장시간 비동기 Operation 조회·취소 */
public final class EduDev24FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev24Handler(); }
}
