package com.cpf.security.api;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** 신규/변경 비밀번호 hash 전에 집행되는 immutable Runtime 정책입니다. */
public final class CpfPasswordRuntimePolicy {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.defaults());

    public Snapshot current() { return snapshot.get(); }

    public Snapshot replace(
            long version,
            int minLength,
            int maxLength,
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecial,
            Set<String> forbiddenFragments) {
        Snapshot next = Snapshot.create(version, minLength, maxLength, requireUppercase, requireLowercase,
                requireDigit, requireSpecial, forbiddenFragments);
        snapshot.set(next);
        return next;
    }

    /** validate는 Runtime 비밀번호 정책의 범위와 상호 제약을 fail-fast로 검증합니다. */
    public void validate(char[] rawPassword) {
        Snapshot policy = snapshot.get();
        if (rawPassword == null) throw new IllegalArgumentException("비밀번호는 필수입니다.");
        String value = new String(rawPassword);
        if (value.length() < policy.minLength() || value.length() > policy.maxLength()) {
            throw new IllegalArgumentException("비밀번호 길이 정책을 충족하지 않습니다.");
        }
        if (policy.requireUppercase() && value.chars().noneMatch(Character::isUpperCase)) throw new IllegalArgumentException("대문자 필요");
        if (policy.requireLowercase() && value.chars().noneMatch(Character::isLowerCase)) throw new IllegalArgumentException("소문자 필요");
        if (policy.requireDigit() && value.chars().noneMatch(Character::isDigit)) throw new IllegalArgumentException("숫자 필요");
        if (policy.requireSpecial() && value.chars().allMatch(Character::isLetterOrDigit)) throw new IllegalArgumentException("특수문자 필요");
        String normalized = value.toLowerCase(Locale.ROOT);
        if (policy.forbiddenFragments().stream().anyMatch(normalized::contains)) {
            throw new IllegalArgumentException("금지된 비밀번호 문자열이 포함되어 있습니다.");
        }
    }

    /** Snapshot는 CPF 공개 계약의 상태와 동작 의미를 명확히 표현합니다. */
    public record Snapshot(
            long version,
            int minLength,
            int maxLength,
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecial,
            Set<String> forbiddenFragments) {
        private static Snapshot defaults() { return create(0L, 10, 128, true, true, true, true, Set.of("password", "123456")); }
        private static Snapshot create(long version, int minLength, int maxLength, boolean upper, boolean lower,
                                       boolean digit, boolean special, Set<String> forbidden) {
            if (version < 0) throw new IllegalArgumentException("version 범위 오류");
            if (minLength < 8 || minLength > 64 || maxLength < minLength || maxLength > 1024) {
                throw new IllegalArgumentException("비밀번호 길이 범위 오류");
            }
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            if (forbidden != null) {
                if (forbidden.size() > 1000) throw new IllegalArgumentException("금지 문자열 최대 1000개");
                forbidden.stream().filter(v -> v != null && v.length() >= 3)
                        .map(v -> v.toLowerCase(Locale.ROOT)).forEach(normalized::add);
            }
            return new Snapshot(version, minLength, maxLength, upper, lower, digit, special, Set.copyOf(normalized));
        }
    }
}
