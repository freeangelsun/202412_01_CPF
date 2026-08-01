package com.cpf.reference.batch.backfill.latearrival;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-15 IntegrationTest — 지연 도착 데이터·Backfill·재산출 */
public final class EduBat15IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat15Handler(); }
}
