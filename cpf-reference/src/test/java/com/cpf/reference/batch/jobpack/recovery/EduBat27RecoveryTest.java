package com.cpf.reference.batch.jobpack.recovery;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-27 RecoveryTest — Job Pack Checksum·호환성·이전 Version 복구 */
public final class EduBat27RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat27Handler(); }
}
