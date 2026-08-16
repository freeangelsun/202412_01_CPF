package com.cpf.starter.runtime;

/** 운영/지원 시 Base Starter의 실제 활성 상태를 확인하는 경량 진단 값입니다. */
public record CpfStarterDiagnostics(String artifactId, boolean contextRuntime, boolean validation,
                                    boolean loggingAnnotation, boolean masking) {
    public static CpfStarterDiagnostics active(boolean loggingAnnotation) {
        return new CpfStarterDiagnostics("cpf-starter", true, true, loggingAnnotation, true);
    }
}
