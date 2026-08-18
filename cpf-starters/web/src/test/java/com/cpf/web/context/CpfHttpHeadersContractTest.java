package com.cpf.web.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.web.api.CpfHttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CpfHttpHeadersContractTest {
    private static final String TX = "20260813010101999MBRlocal010000001";

    @Test
    void canonicalSixAreReadOnlyToBusinessCodeAndValidationRejectsMissingValues() {
        Map<String, String> headers = Map.of(
                CpfHttpHeaders.transactionId(), TX,
                CpfHttpHeaders.originalChannel(), "MBR",
                CpfHttpHeaders.currentChannel(), "EXS",
                CpfHttpHeaders.callerChannel(), "MBR",
                CpfHttpHeaders.targetChannel(), "EXS",
                CpfHttpHeaders.targetOperationId(), "memberFind");

        CpfHttpHeaders.validateInternal(headers);
        assertEquals(TX, CpfHttpHeaders.get(headers, CpfHttpHeaders.transactionId()));
        assertEquals("MBR", CpfHttpHeaders.get(headers, CpfHttpHeaders.originalChannel()));
        assertEquals("EXS", CpfHttpHeaders.get(headers, CpfHttpHeaders.currentChannel()));
        assertEquals("MBR", CpfHttpHeaders.get(headers, CpfHttpHeaders.callerChannel()));
        assertEquals("EXS", CpfHttpHeaders.get(headers, CpfHttpHeaders.targetChannel()));
        assertEquals("memberFind", CpfHttpHeaders.get(headers, CpfHttpHeaders.targetOperationId()));

        Map<String, String> missingOperation = Map.of(
                CpfHttpHeaders.transactionId(), TX,
                CpfHttpHeaders.originalChannel(), "MBR",
                CpfHttpHeaders.currentChannel(), "EXS",
                CpfHttpHeaders.callerChannel(), "MBR",
                CpfHttpHeaders.targetChannel(), "EXS");
        assertThrows(CpfHeaderValidationException.class, () -> CpfHttpHeaders.validateInternal(missingOperation));
    }

    @Test
    void customHeadersAreCaseInsensitiveTypedAndMultiValueWithoutCatalogRegistration() {
        UUID key = UUID.randomUUID();
        CpfHttpHeaders captured = CpfHttpHeaders.capture(Map.of(
                "X-Campaign-Code", List.of("SUMMER-2026"),
                "x-role-code", List.of("A", "B"),
                "X-Client-Key", List.of(key.toString())));

        assertEquals("SUMMER-2026", captured.get("x-campaign-code"));
        assertEquals(List.of("A", "B"), captured.getAll("X-ROLE-CODE"));
        assertEquals(key, captured.get("x-client-key", UUID.class));
        assertEquals("fallback", captured.get("X-Missing", "fallback"));
        assertTrue(captured.containsKey("X-Campaign-Code"));
        assertFalse(captured.containsKey("X-Unknown"));
    }

    @Test
    void customMutationCannotOverwriteOrRemoveProtectedHeaders() {
        assertThrows(CpfHeaderValidationException.class,
                () -> CpfHttpHeaders.builder().set(CpfHttpHeaders.transactionId(), "forged"));
        assertThrows(CpfHeaderValidationException.class,
                () -> CpfHttpHeaders.builder().remove(CpfHttpHeaders.targetChannel()));

        Map<String, List<String>> custom = CpfHttpHeaders.builder()
                .set("X-Business-Mode", "FAST")
                .add("X-Role-Code", "A")
                .add("x-role-code", "B")
                .buildMultiValue();
        assertEquals(List.of("A", "B"), custom.get("X-Role-Code"));
    }
}
