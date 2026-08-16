package com.cpf.platform.operations.observability.api.logging;

import org.springframework.core.env.Environment;

import java.nio.file.Path;

/** 외부 모듈이 core 내부 로그 경로 구현에 의존하지 않도록 제공하는 공개 facade입니다. */
public final class CpfLogPaths {
    private final com.cpf.platform.operations.observability.internal.logging.file.CpfLogPathPolicy delegate;

    public CpfLogPaths(Environment environment) {
        this.delegate = new com.cpf.platform.operations.observability.internal.logging.file.CpfLogPathPolicy(environment);
    }

    /** logRoot 작업을 CPF 표준 계약에 따라 수행한다. */
    public Path logRoot() { return delegate.logRoot(); }
    public String environmentCode() { return delegate.environmentCode(); }
    public String instanceId() { return delegate.instanceId(); }
    public Path batchJobLogPath(Path relativePath) { return delegate.batchJobLogPath(relativePath); }
}
