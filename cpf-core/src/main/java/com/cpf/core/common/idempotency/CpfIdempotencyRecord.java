package cpf.pfw.common.idempotency;

import java.time.Instant;

/**
 * HTTP, Broker, Batch, FileTransfer가 함께 사용할 수 있는 PFW 중복 처리 기록입니다.
 *
 * <p>동일 업무 범위(scope)와 idempotencyKey에 대해 요청 본문 hash를 함께 보관하여,
 * 같은 요청은 저장된 응답을 재사용하고 다른 요청은 충돌로 차단할 수 있게 합니다.</p>
 */
public record CpfIdempotencyRecord(
        String scope,
        String idempotencyKey,
        String requestHash,
        String payloadHash,
        String status,
        String storedResponse,
        boolean retryAllowed,
        Instant createdAt,
        Instant completedAt,
        Instant expiresAt) {

    public CpfIdempotencyRecord {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("scope는 필수입니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey는 필수입니다.");
        }
        status = status == null || status.isBlank() ? "PROCESSING" : status.trim().toUpperCase();
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public boolean sameRequest(String otherRequestHash) {
        return requestHash != null && requestHash.equals(otherRequestHash);
    }
}
