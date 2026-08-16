package com.cpf.core.api.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfDynamicBusinessExceptionTest {
    @Test
    void acceptsDynamicBusinessDefinitionWithoutAddingEnumConstant() {
        CpfDynamicErrorCode dynamic = CpfDynamicErrorCode.duplicate(
                "EDU090001", "중복된 값이 존재합니다.", "중복 검증 오류");

        CpfBusinessException failure = new CpfBusinessException(
                dynamic, "중복된 값이 존재합니다.", Map.of("fieldName", "memberNo"));

        assertEquals(dynamic, failure.fallbackError());
        assertEquals(dynamic.statusCode(), failure.errorReference());
        assertEquals("memberNo", failure.arguments().get("fieldName"));
    }
}
