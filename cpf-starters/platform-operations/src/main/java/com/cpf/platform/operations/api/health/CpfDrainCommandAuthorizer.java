package com.cpf.platform.operations.api.health;
/** 실제 Security owner가 Permission/Approval 정책을 연결하기 위한 SPI입니다. */
@FunctionalInterface
public interface CpfDrainCommandAuthorizer {
    void authorize(String actor, String action, String approvalId);
}
