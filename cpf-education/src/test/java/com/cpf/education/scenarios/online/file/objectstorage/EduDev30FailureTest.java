package com.cpf.education.scenarios.online.file.objectstorage;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-30 FailureTest — Object Storage 보존·버전·법적 보류 */
public final class EduDev30FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev30Handler(); }
}
