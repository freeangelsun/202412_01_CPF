package com.cpf.core.api.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CpfUtilityContractTest {
    @Test void stringDateDecimal경계값을검증한다() {
        assertNull(CpfStrings.trimToNull("  "));
        assertEquals("AB", CpfStrings.normalizeCodeOrNull(" ab "));
        assertThrows(IllegalArgumentException.class, () -> CpfStrings.abbreviate("abcd", 3));
        assertEquals(LocalDate.of(2026, 8, 8), CpfDates.parseBasic("20260808"));
        assertNull(CpfDates.parseOrNull("20260230"));
        assertEquals(new BigDecimal("1.24"), CpfDecimals.scale(new BigDecimal("1.235"), 2, null));
    }

    @Test void fileMapList중복Traversal크기를failClosed한다() {
        assertEquals("txt", CpfFiles.extension("A.TXT"));
        assertThrows(IllegalArgumentException.class, () -> CpfFiles.safeFileName("../x"));
        assertThrows(IllegalArgumentException.class, () -> CpfFiles.resolveChild(Path.of("/safe"), "../x"));
        assertThrows(IllegalArgumentException.class, () -> CpfMaps.indexBy(List.of("A","A"), x -> x));
        assertThrows(IllegalArgumentException.class, () -> CpfLists.partition(List.of(1), 0));
    }

    @Test void numericValueValidation은모호한입력을성공으로오판하지않는다() {
        assertTrue(CpfNumbers.toInt("bad").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> CpfNumbers.clamp(1, 2, 1));
        assertEquals(Boolean.TRUE, CpfValues.bool("Y"));
        assertThrows(IllegalArgumentException.class, () -> CpfValues.bool("maybe"));
        assertThrows(IllegalArgumentException.class, () -> CpfValidation.notEmpty(List.of(), "items"));
    }

    @Test void transactionHeader는canonicalId만허용한다() {
        String tx = "20260725123456789ADMABC12340000001";
        Map<String,String> headers = CpfHeaders.transaction(tx, "SEG-1", "SEG-0");
        assertEquals(tx, headers.get(CpfHeaders.transactionId()));
        assertThrows(IllegalArgumentException.class, () -> CpfHeaders.transaction("spoof"));
    }
}
