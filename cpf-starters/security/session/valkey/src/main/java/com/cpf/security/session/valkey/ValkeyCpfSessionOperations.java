package com.cpf.security.session.valkey;

import com.cpf.security.api.CpfSessionOperations;
import com.cpf.security.api.CpfSessionSnapshot;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Spring Data Redis 프로토콜을 사용하는 Valkey 분산 Session Provider. */
public final class ValkeyCpfSessionOperations implements CpfSessionOperations {
    private final StringRedisTemplate redis;
    private final CpfValkeySessionProperties properties;
    private final CpfSessionAuditSink audit;

    public ValkeyCpfSessionOperations(StringRedisTemplate redis, CpfValkeySessionProperties properties, CpfSessionAuditSink audit) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.audit = Objects.requireNonNull(audit, "audit");
        properties.validate();
    }

    @Override
    public CpfSessionSnapshot create(String tenantId, String principalId, Duration ttl, Map<String, String> attributes) {
        return createWithGeneration(tenantId, principalId, ttl, attributes, 1L, "CREATE", "created");
    }

    @Override
    public Optional<CpfSessionSnapshot> find(String sessionId) {
        requireText(sessionId, "sessionId");
        Map<Object, Object> raw = redis.opsForHash().entries(key(sessionId));
        if (raw.isEmpty()) return Optional.empty();
        Map<String, String> values = new HashMap<>();
        raw.forEach((key, value) -> values.put(String.valueOf(key), String.valueOf(value)));
        CpfSessionSnapshot snapshot = decode(sessionId, values);
        if (snapshot.revoked() || !snapshot.expiresAt().isAfter(Instant.now())) return Optional.empty();
        return Optional.of(snapshot);
    }

    @Override
    public CpfSessionSnapshot renew(String sessionId, Duration ttl) {
        CpfSessionSnapshot old = find(sessionId).orElseThrow(() -> new IllegalStateException("active session not found: " + sessionId));
        Duration use = ttl(ttl);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(use);
        long generation = Math.addExact(old.generation(), 1L);
        redis.opsForHash().put(key(sessionId), "lastAccessedAt", now.toString());
        redis.opsForHash().put(key(sessionId), "expiresAt", expiresAt.toString());
        redis.opsForHash().put(key(sessionId), "generation", Long.toString(generation));
        redis.expireAt(key(sessionId), expiresAt);
        redis.expireAt(index(old.tenantId(), old.principalId()), expiresAt.plus(properties.getDefaultTtl()));
        audit.record("RENEW", sessionId, old.principalId(), "renewed");
        return find(sessionId).orElseThrow();
    }

    @Override
    public CpfSessionSnapshot rotate(String sessionId, Duration ttl) {
        CpfSessionSnapshot old = find(sessionId).orElseThrow(() -> new IllegalStateException("active session not found: " + sessionId));
        revoke(sessionId, "ROTATE");
        CpfSessionSnapshot rotated = createWithGeneration(
                old.tenantId(), old.principalId(), ttl, old.attributes(), Math.addExact(old.generation(), 1L), "ROTATE", "fixation-defense");
        return rotated;
    }

    @Override
    public boolean revoke(String sessionId, String reason) {
        requireText(reason, "reason");
        Optional<CpfSessionSnapshot> old = find(sessionId);
        if (old.isEmpty()) return false;
        redis.opsForHash().put(key(sessionId), "revoked", "true");
        redis.opsForHash().put(key(sessionId), "revokeReason", reason);
        redis.opsForSet().remove(index(old.get().tenantId(), old.get().principalId()), sessionId);
        redis.expire(key(sessionId), Duration.ofMinutes(5));
        audit.record("REVOKE", sessionId, old.get().principalId(), reason);
        return true;
    }

    @Override
    public int revokePrincipal(String tenantId, String principalId, String reason) {
        requireText(tenantId, "tenantId");
        requireText(principalId, "principalId");
        requireText(reason, "reason");
        Set<String> ids = redis.opsForSet().members(index(tenantId, principalId));
        if (ids == null || ids.isEmpty()) return 0;
        int count = 0;
        for (String id : new ArrayList<>(ids)) if (revoke(id, reason)) count++;
        return count;
    }

    @Override
    public List<CpfSessionSnapshot> findByPrincipal(String tenantId, String principalId) {
        requireText(tenantId, "tenantId");
        requireText(principalId, "principalId");
        Set<String> ids = redis.opsForSet().members(index(tenantId, principalId));
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(this::find)
                .flatMap(value -> value.stream())
                .sorted(Comparator.comparing(CpfSessionSnapshot::lastAccessedAt).reversed())
                .toList();
    }

    private CpfSessionSnapshot createWithGeneration(
            String tenantId, String principalId, Duration ttl, Map<String, String> attributes,
            long generation, String action, String reason) {
        requireText(tenantId, "tenantId");
        requireText(principalId, "principalId");
        Duration use = ttl(ttl);
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(use);
        Map<String, String> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("principalId", principalId);
        values.put("createdAt", now.toString());
        values.put("lastAccessedAt", now.toString());
        values.put("expiresAt", expiresAt.toString());
        values.put("generation", Long.toString(generation));
        values.put("revoked", "false");
        if (attributes != null) attributes.forEach((key, value) -> {
            requireText(key, "attribute key");
            Objects.requireNonNull(value, "attribute value");
            values.put("a." + key, value);
        });
        redis.opsForHash().putAll(key(id), values);
        redis.expireAt(key(id), expiresAt);
        redis.opsForSet().add(index(tenantId, principalId), id);
        redis.expireAt(index(tenantId, principalId), expiresAt.plus(properties.getDefaultTtl()));
        enforceLimit(tenantId, principalId);
        audit.record(action, id, principalId, reason);
        return decode(id, values);
    }

    private void enforceLimit(String tenantId, String principalId) {
        List<CpfSessionSnapshot> all = findByPrincipal(tenantId, principalId);
        for (int index = properties.getMaxConcurrentSessions(); index < all.size(); index++) {
            revoke(all.get(index).sessionId(), "CONCURRENT_LIMIT");
        }
    }

    private CpfSessionSnapshot decode(String id, Map<String, String> values) {
        Map<String, String> attributes = new HashMap<>();
        values.forEach((key, value) -> {
            if (key.startsWith("a.")) attributes.put(key.substring(2), value);
        });
        return new CpfSessionSnapshot(
                id, values.get("tenantId"), values.get("principalId"),
                Instant.parse(values.get("createdAt")), Instant.parse(values.get("lastAccessedAt")), Instant.parse(values.get("expiresAt")),
                Long.parseLong(values.getOrDefault("generation", "1")), Boolean.parseBoolean(values.getOrDefault("revoked", "false")), attributes);
    }

    private Duration ttl(Duration requested) {
        Duration use = requested == null ? properties.getDefaultTtl() : requested;
        if (use.isZero() || use.isNegative()) throw new IllegalArgumentException("session ttl must be positive");
        return use;
    }

    private String key(String id) { return properties.getKeyPrefix() + id; }
    private String index(String tenantId, String principalId) { return properties.getKeyPrefix() + "idx:" + tenantId + ":" + principalId; }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " required");
        return value;
    }
}
