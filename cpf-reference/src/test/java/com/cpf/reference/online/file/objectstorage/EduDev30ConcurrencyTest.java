package com.cpf.reference.online.file.objectstorage;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-30 ConcurrencyTest — Object Storage 보존·버전·법적 보류 */
public final class EduDev30ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev30Handler(); }
}
