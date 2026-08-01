package com.cpf.admin.config;

import com.cpf.core.api.error.CpfValidationException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * ADM 영속성 Fail-closed 정책입니다.
 *
 * <p>제품 실행의 기본값은 DATABASE이며 DB 오류를 메모리 성공으로 바꾸지 않습니다.
 * MEMORY는 EDU/test 프로필에서 명시적으로 선택한 경우에만 허용합니다. 제품·local·demo·library 실행은 DATABASE fail-closed입니다.</p>
 */
@Component
public class AdmPersistencePolicy {
    private static final Set<String> MEMORY_ALLOWED_PROFILES = Set.of("edu", "test");
    private final Mode mode;

    public AdmPersistencePolicy(Environment environment) {
        this.mode = Mode.valueOf(environment.getProperty("cpf.adm.persistence.mode", "DATABASE")
                .trim().toUpperCase(Locale.ROOT));
        if (mode == Mode.MEMORY) {
            boolean allowed = Arrays.stream(environment.getActiveProfiles())
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .anyMatch(MEMORY_ALLOWED_PROFILES::contains);
            if (!allowed) {
                throw new CpfValidationException("ADM MEMORY persistence는 EDU/test 프로필에서만 사용할 수 있습니다.");
            }
        }
    }

    public boolean memoryEnabled() {
        return mode == Mode.MEMORY;
    }

    public boolean databaseRequired() {
        return mode == Mode.DATABASE;
    }

    public Mode mode() {
        return mode;
    }

    public enum Mode { DATABASE, MEMORY }
}
