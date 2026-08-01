package com.cpf.reference.batch.file.csv;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-03 RecoveryTest — CSV 입출력 배치 */
public final class EduBat03RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat03Handler(); }
}
