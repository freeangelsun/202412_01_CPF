package com.cpf.reference.optional.gateway.publish;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-05 UnitTest — Draft·검증·승인·게시·부분 적용 */
public final class EduGw05UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw05Handler(); }
}
