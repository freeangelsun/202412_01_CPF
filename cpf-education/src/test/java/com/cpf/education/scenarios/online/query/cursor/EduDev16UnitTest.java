package com.cpf.education.scenarios.online.query.cursor;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-16 UnitTest — 대용량 목록 검색·정렬·Cursor Paging */
public final class EduDev16UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev16Handler(); }
}
