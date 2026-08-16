package com.cpf.platform.operations.health.api;
/** CpfHealthSnapshotProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
@FunctionalInterface public interface CpfHealthSnapshotProvider { CpfRuntimeHealth snapshot(); }
