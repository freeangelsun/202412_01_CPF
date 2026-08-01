package com.cpf.reference.online.file.objectstorage;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-30 IntegrationTest — Object Storage 보존·버전·법적 보류 */
public final class EduDev30IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev30Handler(); }
}
