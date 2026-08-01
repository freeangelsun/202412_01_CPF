package com.cpf.reference.online.file.objectstorage;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-30 RecoveryTest — Object Storage 보존·버전·법적 보류 */
public final class EduDev30RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev30Handler(); }
}
