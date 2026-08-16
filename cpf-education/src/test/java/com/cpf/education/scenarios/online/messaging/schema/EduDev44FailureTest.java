package com.cpf.education.scenarios.online.messaging.schema;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-44 FailureTest — Event Schema 진화·호환성·Dead Letter */
public final class EduDev44FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev44Handler(); }
}
