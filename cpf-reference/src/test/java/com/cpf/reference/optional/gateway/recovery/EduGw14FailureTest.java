package com.cpf.reference.optional.gateway.recovery;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-14 FailureTest — Gateway 관측·개인정보 가림·감사 */
public final class EduGw14FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw14Handler(); }
}
