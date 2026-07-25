package com.cpf.admin.opr.service;

import com.cpf.core.api.security.CpfMasking;


/** Spreadsheet formula injection을 차단하는 CPF CSV 직렬화 정책입니다. */
final class AdmCsvSanitizer {
    static final String POLICY_VERSION = "CPF-CSV-1";

    private AdmCsvSanitizer() {
    }

    static String cell(Object rawValue, boolean mask) {
        if (rawValue == null) return "\"\"";
        if (rawValue instanceof Number || rawValue instanceof Boolean) {
            return quote(String.valueOf(rawValue));
        }
        String text = String.valueOf(rawValue);
        if (mask) text = CpfMasking.mask(text);
        text = neutralizeFormula(text);
        return quote(text);
    }

    static String header(String header) {
        return quote(neutralizeFormula(header == null ? "" : header));
    }

    static String neutralizeFormula(String value) {
        if (value == null || value.isEmpty()) return "";
        int index = 0;
        while (index < value.length()) {
            char c = value.charAt(index);
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || Character.isISOControl(c)) {
                index++;
                continue;
            }
            break;
        }
        if (index < value.length()) {
            char first = value.charAt(index);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                return "'" + value;
            }
        }
        return value;
    }

    private static String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
