package cpf.pfw.common.header;

import cpf.pfw.common.logging.SensitiveDataMasker;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 표준 헤더 로그 저장용 마스킹 유틸리티입니다.
 */
public final class CpfHeaderMasker {
    private CpfHeaderMasker() {
    }

    public static Map<String, String> maskHeaders(Map<String, String> headers) {
        Map<String, String> masked = new LinkedHashMap<>();
        if (headers == null) {
            return masked;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!hasText(entry.getValue())) {
                continue;
            }
            masked.put(entry.getKey(), mask(entry.getKey(), entry.getValue()));
        }
        return masked;
    }

    public static String mask(String headerName, String value) {
        if (!hasText(value)) {
            return value;
        }
        if (!CpfHeaderSpecs.canLogRaw(headerName)) {
            return "****";
        }
        if (CpfHeaderSpecs.shouldMask(headerName)) {
            return SensitiveDataMasker.mask(value);
        }
        return SensitiveDataMasker.mask(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
