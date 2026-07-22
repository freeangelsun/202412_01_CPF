package cpf.pfw.common.header;

import java.util.Map;

/**
 * 헤더 스냅샷을 거래 로그 detail JSON으로 변환합니다.
 */
public final class CpfHeaderAuditLogger {
    private CpfHeaderAuditLogger() {
    }

    public static String toJson(Map<String, String> values) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!first) {
                json.append(',');
            }
            json.append('"').append(escapeJson(entry.getKey())).append("\":\"")
                    .append(escapeJson(entry.getValue())).append('"');
            first = false;
        }
        json.append('}');
        return json.toString();
    }

    private static String escapeJson(String value) {
        return value == null
                ? ""
                : value.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n")
                        .replace("\t", "\\t");
    }
}
