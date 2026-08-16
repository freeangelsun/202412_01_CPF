package com.cpf.education.operations.gateway.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-14 IntegrationTest — Gateway 관측·개인정보 가림·감사 */
public final class EduGw14IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw14Handler(); }
}
