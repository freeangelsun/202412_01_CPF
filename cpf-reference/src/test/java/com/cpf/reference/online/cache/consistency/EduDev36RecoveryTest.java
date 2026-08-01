package com.cpf.reference.online.cache.consistency;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-36 RecoveryTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
