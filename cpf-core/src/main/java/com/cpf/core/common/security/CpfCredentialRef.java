package cpf.pfw.common.security;

/**
 * 원문 secret 대신 사용하는 credential 참조값입니다.
 *
 * <p>업무 모듈과 로그에는 원문 비밀번호, private key, token을 남기지 않고
 * 이 참조값과 마스킹 표시값만 전달합니다.</p>
 */
public record CpfCredentialRef(
        String scope,
        String credentialId,
        String version,
        String displayName) {

    public CpfCredentialRef {
        if (credentialId == null || credentialId.isBlank()) {
            throw new IllegalArgumentException("credentialId는 필수입니다.");
        }
        scope = scope == null || scope.isBlank() ? "default" : scope;
        version = version == null || version.isBlank() ? "latest" : version;
        displayName = displayName == null || displayName.isBlank() ? credentialId : displayName;
    }

    public String masked() {
        if (displayName.length() <= 4) {
            return "****";
        }
        return displayName.substring(0, 2) + "****" + displayName.substring(displayName.length() - 2);
    }
}
