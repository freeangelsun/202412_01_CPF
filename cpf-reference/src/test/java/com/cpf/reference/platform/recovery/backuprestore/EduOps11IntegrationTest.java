package com.cpf.reference.platform.recovery.backuprestore;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-11 IntegrationTest — Backup·Restore·시점 복구·대사 */
public final class EduOps11IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps11Handler(); }
}
