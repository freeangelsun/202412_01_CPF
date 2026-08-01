package com.cpf.reference.online.workflow.saga;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-22 FailureTest — 서비스 간 Saga 보상·수동 확정 */
public final class EduDev22FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev22Handler(); }
}
