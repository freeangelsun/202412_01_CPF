package com.cpf.reference.platform.security.secretrotation;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-03 UnitTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
