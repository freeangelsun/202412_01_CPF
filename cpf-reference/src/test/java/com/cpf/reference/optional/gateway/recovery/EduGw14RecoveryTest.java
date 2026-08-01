package com.cpf.reference.optional.gateway.recovery;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-14 RecoveryTest — Gateway 관측·개인정보 가림·감사 */
public final class EduGw14RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw14Handler(); }
}
