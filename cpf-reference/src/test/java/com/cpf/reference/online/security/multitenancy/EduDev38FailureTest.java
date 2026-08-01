package com.cpf.reference.online.security.multitenancy;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-38 FailureTest — 다중 Tenant 격리·설정·데이터 범위 */
public final class EduDev38FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev38Handler(); }
}
