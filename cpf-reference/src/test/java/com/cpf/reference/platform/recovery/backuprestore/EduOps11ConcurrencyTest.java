package com.cpf.reference.platform.recovery.backuprestore;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-11 ConcurrencyTest — Backup·Restore·시점 복구·대사 */
public final class EduOps11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps11Handler(); }
}
