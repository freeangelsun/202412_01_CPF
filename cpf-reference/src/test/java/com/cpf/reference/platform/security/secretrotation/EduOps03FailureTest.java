package com.cpf.reference.platform.security.secretrotation;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-03 FailureTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
