package com.cpf.reference.platform.database.lifecycle;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-04 FailureTest — DB 3종 신규 설치·Migration·Drift·Rollback */
public final class EduOps04FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps04Handler(); }
}
