package com.cpf.reference.platform.recovery.backuprestore;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-11 UnitTest — Backup·Restore·시점 복구·대사 */
public final class EduOps11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps11Handler(); }
}
