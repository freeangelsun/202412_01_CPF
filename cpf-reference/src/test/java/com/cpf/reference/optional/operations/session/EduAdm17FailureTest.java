package com.cpf.reference.optional.operations.session;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-17 FailureTest — Browser 세션 만료·재로그인·위험 조치 안전성 */
public final class EduAdm17FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm17Handler(); }
}
