package com.cpf.education.data.cache;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCachePort;
import com.cpf.data.cache.api.CpfCacheValue;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Redis/Valkey 공통 계약 EDU.
 *
 * <p>업무 코드는 Provider 타입을 직접 참조하지 않고 {@link CpfCachePort}만 사용합니다.
 * 운영 배포에서는 {@code cpf-starter-cache-redis} 또는
 * {@code cpf-starter-cache-valkey} 중 하나를 명시적으로 선택합니다.</p>
 */
public final class CpfRedisValkeyCacheEducation {
    private final CpfCachePort cache;
    public CpfRedisValkeyCacheEducation(CpfCachePort cache) { this.cache = cache; }
    public void saveMemberDisplayName(String tenantId, String memberId, String displayName) {
        var key = new CpfCacheKey("member-display-name", memberId, tenantId);
        cache.put(key, new CpfCacheValue(true, false, displayName.getBytes(StandardCharsets.UTF_8),
                "text/plain;charset=UTF-8", 1L, null), Duration.ofMinutes(10));
    }
    /** findMemberDisplayName 작업을 CPF 표준 계약에 따라 수행한다. */
    public String findMemberDisplayName(String tenantId, String memberId) {
        var value = cache.get(new CpfCacheKey("member-display-name", memberId, tenantId));
        return value.found() ? new String(value.payload(), StandardCharsets.UTF_8) : null;
    }
}
