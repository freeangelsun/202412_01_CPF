package com.cpf.admin.opr.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdmCsvSanitizerTest {
    @Test
    void blocksSpreadsheetFormulaPrefixesIncludingWhitespaceBypass() {
        for (String value : new String[]{"=HYPERLINK(\"x\")", "+CMD()", "-1+2", "@SUM(1,2)", "\t=1+1", "  @SUM(1,2)"}) {
            String cell = AdmCsvSanitizer.cell(value, false);
            assertThat(cell).startsWith("\"'");
        }
    }

    @Test
    void keepsTypedNumbersAsNumbersWithoutFormulaNeutralization() {
        assertThat(AdmCsvSanitizer.cell(-12, false)).isEqualTo("\"-12\"");
        assertThat(AdmCsvSanitizer.cell(10.5, false)).isEqualTo("\"10.5\"");
    }

    @Test
    void headersUseSameProtectionPolicy() {
        assertThat(AdmCsvSanitizer.header("=danger")).startsWith("\"'");
        assertThat(AdmCsvSanitizer.POLICY_VERSION).isEqualTo("CPF-CSV-1");
    }
}
