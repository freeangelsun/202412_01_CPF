package com.cpf.reference.optional.operations.incident;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-12 IntegrationTest — Incident·Recovery Center 종단간 복구 */
public final class EduAdm12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm12Handler(); }
}
