package com.cpf.reference.platform.recovery.backuprestore;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-11 FailureTest — Backup·Restore·시점 복구·대사 */
public final class EduOps11FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps11Handler(); }
}
