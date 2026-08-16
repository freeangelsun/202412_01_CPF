package com.cpf.platform.operations.api.health;
/** 현재 런타임 Health Snapshot을 조회하는 Provider-neutral 계약입니다. */
@FunctionalInterface
public interface CpfHealthSnapshotProvider { CpfRuntimeHealth snapshot(); }
