package com.cpf.reference.batch.partition.range;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-04 ConcurrencyTest — 8개 범위 Partition */
public final class EduBat04ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat04Handler(); }
}
