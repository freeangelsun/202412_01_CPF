package cpf.pfw.channel.model;

/** 채널 정책 판단 결과와 적용된 스냅샷 버전을 함께 전달합니다. */
public record CpfChannelPolicyDecision(
        boolean allowed,
        String reason,
        long snapshotVersion,
        String matchedPolicyKey,
        boolean authenticationRequired,
        boolean signatureRequired,
        int maxTps) {
}
