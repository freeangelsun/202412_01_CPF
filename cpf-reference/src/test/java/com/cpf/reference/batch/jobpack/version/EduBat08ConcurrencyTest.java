package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-08 ConcurrencyTest — Job Pack Version·Artifact 배포 */
public final class EduBat08ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
