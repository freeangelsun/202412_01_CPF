package cpf.pfw.channel.model;

import java.util.Locale;

/** CPF에 접속하거나 내부 호출에 사용하는 채널의 정본 정보입니다. */
public record CpfChannelDefinition(
        String channelCode,
        String channelName,
        String channelType,
        String trustLevel,
        boolean clientChannel,
        boolean internalChannel,
        boolean authenticationRequired,
        boolean signatureRequired,
        boolean active,
        String description,
        long version) {

    public CpfChannelDefinition {
        channelCode = normalizeCode(channelCode, "채널 코드");
        if (channelName == null || channelName.isBlank()) {
            throw new IllegalArgumentException("채널명은 필수입니다.");
        }
        channelName = channelName.trim();
        channelType = normalizeCode(channelType, "채널 유형");
        trustLevel = normalizeCode(trustLevel, "신뢰 수준");
        description = description == null ? "" : description.trim();
        if (version < 0) {
            throw new IllegalArgumentException("채널 버전은 0 이상이어야 합니다.");
        }
    }

    private static String normalizeCode(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]{1,29}")) {
            throw new IllegalArgumentException(fieldName + " 형식이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }
}
