package cpf.cmn.message.fixedlength;

/**
 * 고정길이 전문 민감 필드 마스킹 규칙입니다.
 */
public class FixedLengthMaskingRule {

    public String mask(FixedLengthFieldSpec field, String value) {
        if (!field.sensitive() || value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 2) {
            return "*".repeat(trimmed.length());
        }
        return trimmed.charAt(0) + "*".repeat(Math.max(trimmed.length() - 2, 1)) + trimmed.charAt(trimmed.length() - 1);
    }
}
