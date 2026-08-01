package com.cpf.reference.platform.security.incident;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-14 ConcurrencyTest — 보안 사고·계정·키·세션 긴급 차단 */
public final class EduOps14ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps14Handler(); }
}
