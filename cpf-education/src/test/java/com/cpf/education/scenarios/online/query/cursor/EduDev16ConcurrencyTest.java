package com.cpf.education.scenarios.online.query.cursor;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-16 ConcurrencyTest — 대용량 목록 검색·정렬·Cursor Paging */
public final class EduDev16ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev16Handler(); }
}
