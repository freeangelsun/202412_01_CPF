package com.cpf.reference.online.query.cursor;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-16 IntegrationTest — 대용량 목록 검색·정렬·Cursor Paging */
public final class EduDev16IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev16Handler(); }
}
