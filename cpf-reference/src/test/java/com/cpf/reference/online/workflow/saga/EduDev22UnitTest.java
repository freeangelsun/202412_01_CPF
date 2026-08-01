package com.cpf.reference.online.workflow.saga;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-22 UnitTest — 서비스 간 Saga 보상·수동 확정 */
public final class EduDev22UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev22Handler(); }
}
