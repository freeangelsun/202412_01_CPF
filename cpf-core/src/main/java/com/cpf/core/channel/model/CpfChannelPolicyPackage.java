package cpf.pfw.channel.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/** 채널 레지스트리와 실행 정책을 다른 환경으로 이동할 때 사용하는 무결성 패키지입니다. */
public record CpfChannelPolicyPackage(
        String schemaVersion,
        Instant exportedAt,
        List<CpfChannelDefinition> channels,
        List<CpfChannelExecutionPolicy> policies,
        String checksumSha256) {

    public CpfChannelPolicyPackage {
        schemaVersion = schemaVersion == null || schemaVersion.isBlank() ? "1" : schemaVersion;
        exportedAt = exportedAt == null ? Instant.now() : exportedAt;
        channels = List.copyOf(channels == null ? List.of() : channels);
        policies = List.copyOf(policies == null ? List.of() : policies);
        checksumSha256 = checksumSha256 == null ? "" : checksumSha256.toLowerCase();
    }

    public static CpfChannelPolicyPackage from(CpfChannelPolicySnapshot snapshot) {
        List<CpfChannelDefinition> channels = snapshot.channels().values().stream()
                .sorted(java.util.Comparator.comparing(CpfChannelDefinition::channelCode))
                .toList();
        List<CpfChannelExecutionPolicy> policies = snapshot.policies().stream()
                .sorted(java.util.Comparator.comparing(CpfChannelExecutionPolicy::policyKey))
                .toList();
        String checksum = checksum("1", channels, policies);
        return new CpfChannelPolicyPackage("1", Instant.now(), channels, policies, checksum);
    }

    public boolean hasValidChecksum() {
        return checksumSha256.equals(checksum(schemaVersion, channels, policies));
    }

    private static String checksum(
            String schemaVersion,
            List<CpfChannelDefinition> channels,
            List<CpfChannelExecutionPolicy> policies) {
        StringBuilder canonical = new StringBuilder(schemaVersion).append('\n');
        channels.stream().sorted(java.util.Comparator.comparing(CpfChannelDefinition::channelCode))
                .forEach(item -> canonical.append(item).append('\n'));
        policies.stream().sorted(java.util.Comparator.comparing(CpfChannelExecutionPolicy::policyKey))
                .forEach(item -> canonical.append(item).append('\n'));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", ex);
        }
    }
}
