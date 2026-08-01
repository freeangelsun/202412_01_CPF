package com.cpf.reference.platform.configuration.validation;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-02 UnitTest — Profile·환경변수·설정값 전체 검증 */
public final class EduOps02UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps02Handler(); }
}
