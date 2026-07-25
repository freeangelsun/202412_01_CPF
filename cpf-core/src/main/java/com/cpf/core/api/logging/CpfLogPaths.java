package com.cpf.core.api.logging;

import org.springframework.core.env.Environment;

import java.nio.file.Path;

/** 외부 모듈이 core 내부 로그 경로 구현에 의존하지 않도록 제공하는 공개 facade입니다. */
public final class CpfLogPaths {
    private final com.cpf.core.common.logging.file.CpfLogPathPolicy delegate;

    public CpfLogPaths(Environment environment) {
        this.delegate = new com.cpf.core.common.logging.file.CpfLogPathPolicy(environment);
    }

    public Path logRoot() { return delegate.logRoot(); }
    public String environmentCode() { return delegate.environmentCode(); }
    public String instanceId() { return delegate.instanceId(); }
    public Path batchJobLogPath(Path relativePath) { return delegate.batchJobLogPath(relativePath); }
}
