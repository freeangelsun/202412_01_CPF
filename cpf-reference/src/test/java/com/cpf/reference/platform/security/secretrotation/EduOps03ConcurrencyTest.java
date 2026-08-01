package com.cpf.reference.platform.security.secretrotation;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-03 ConcurrencyTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
