package com.cpf.reference.online.servicecall.topology;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-06 FailureTest — 같은 애플리케이션·분리 서비스 호출 동등성 */
public final class EduDev06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev06Handler(); }
}
