package com.cpf.data.cache.rediscommon;

/**
 * Vendor-neutral persisted representation for a cache invalidation subject.
 *
 * <p>Oracle treats the empty string as {@code NULL}; therefore namespace-wide invalidations must
 * never be persisted as an empty key. A discriminator also prevents a real cache key from
 * colliding with the namespace sentinel.</p>
 */
public final class CpfCacheInvalidationSubjectKey {
    private static final String NAMESPACE = "N:";
    private static final String KEY = "K:";
    private CpfCacheInvalidationSubjectKey() { }

    public static String encode(String cacheKey) {
        String normalized = cacheKey == null ? "" : cacheKey.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("cacheKey format is invalid");
        }
        return normalized.isEmpty() ? NAMESPACE : KEY + normalized;
    }
}
